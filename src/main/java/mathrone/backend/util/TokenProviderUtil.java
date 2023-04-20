package mathrone.backend.util;

import static mathrone.backend.error.exception.ErrorCode.EXPIRED_TOKEN;
import static mathrone.backend.error.exception.ErrorCode.INVALID_SIGNATURE;
import static mathrone.backend.error.exception.ErrorCode.INVALID_ACCESS_TOKEN;
import static mathrone.backend.error.exception.ErrorCode.NOT_AUTH_INFORMATION;
import static mathrone.backend.error.exception.ErrorCode.UNSUPPORTED_TOKEN;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoTokenResponseDTO;
import mathrone.backend.controller.dto.TokenDto;
import mathrone.backend.controller.dto.UserResponseDto;
import mathrone.backend.error.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

// 실제 인증에 대한 부분 중 인증 전 객체를 받아 인증된 객체를 반환하는 역할
@Slf4j
@Component
@PropertySource("classpath:/keys/jwtKey.properties")
public class TokenProviderUtil {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";     // JWT 토큰 인증 타입
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24;       // 1일
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7일
    public static final String AUTHORIZATION_HEADER = "Authorization";  // http header 종류

    // key는 HS512 알고리즘을 사용함
    private final Key key;

    public TokenProviderUtil(@Value("${key}") String secretKey
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenDto generateToken(Authentication authentication, String accountId) {
        // 권한 가져오기
        String auth = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

        long now = (new Date()).getTime();

        // access token
        Date accessTokenExpires = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = Jwts.builder()
            .setSubject(authentication.getName())
            .claim(AUTHORITIES_KEY, auth)
            .setExpiration(accessTokenExpires)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();

        // refresh token (만료일자만 저장)
        String refreshToken = Jwts.builder()
            .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();

        return TokenDto.builder()
            .grantType(BEARER_TYPE)
            .accessToken(accessToken)
            .accessTokenExpiresIn(accessTokenExpires.getTime())
            .refreshToken(refreshToken)
            .userInfo(
                UserResponseDto.builder()
                    .userId(authentication.getName())
                    .accountId(accountId)
                    .build()
            )
            .build();
    }


    public TokenDto generateTokenWithSns(Authentication authentication,
        ResponseEntity<KakaoTokenResponseDTO> kakaoTokenResponseDTO) {
        // 권한 가져오기
        String auth = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

        long now = (new Date()).getTime();

        // access token
        Date accessTokenExpires = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = Jwts.builder()
            .setSubject(authentication.getName())
            .claim(AUTHORITIES_KEY, auth)
            .setExpiration(accessTokenExpires)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();

        // refresh token (만료일자만 저장)
        String refreshToken = Jwts.builder()
            .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();

        return TokenDto.builder()
            .grantType(BEARER_TYPE)
            .accessToken(accessToken)
            .accessTokenExpiresIn(accessTokenExpires.getTime())
            .refreshToken(refreshToken)
            .userInfo(UserResponseDto.builder().accountId(authentication.getName()).build())
            .snsInfo(kakaoTokenResponseDTO.getBody())
            .build();
    }


    public Authentication getAuthentication(String accessToken) {
        // 토큰 복호화 (내부 정보 가져옴)
        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new CustomException(NOT_AUTH_INFORMATION);
        }

        // 권한 정보 가져옴
        Collection<? extends GrantedAuthority> authorities =
            Arrays.stream(claims.get(AUTHORITIES_KEY).toString()
                    .split(",")).map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        UserDetails principal = new User(claims.getSubject(), "",
            authorities);

        return new UsernamePasswordAuthenticationToken(principal,
            "", authorities);
    }

    // 만료된 토큰의 경우에도 정보를 꺼내기 위한 메소드
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key)
                .build().parseClaimsJws(accessToken)
                .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // 토큰 정보 검증
    public boolean validateToken(String token, HttpServletRequest request) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
            request.setAttribute("Exception", new CustomException(INVALID_SIGNATURE));
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
            request.setAttribute("Exception",  new CustomException(EXPIRED_TOKEN));
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
            request.setAttribute("Exception",  new CustomException(UNSUPPORTED_TOKEN));
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
            request.setAttribute("Exception",  new CustomException(INVALID_ACCESS_TOKEN));
        }
        return false;
    }


    public Date getRefreshTokenExpireTime() {
        return new Date(new Date().getTime() + REFRESH_TOKEN_EXPIRE_TIME);
    }

    // logout token의 expiration 계산
    public long getRemainExpiration(String token) {
        Date currentTime = parseClaims(token).getExpiration();
        Date now = new Date();
        // redis의 단위는 초로, 밀리초를 초로 변환하는 과정의 오차를 감안하기 위해 1초 더함.
        return ((currentTime.getTime() - now.getTime()) / 1000) + 1;
    }

    // Request Header의 토큰 정보 가져오는 메소드
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken)) {
            return bearerToken;
        }
        return null;
    }

}
