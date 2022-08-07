package mathrone.backend.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import mathrone.backend.controller.dto.TokenDto;
import mathrone.backend.controller.dto.UserResponseDto;
import mathrone.backend.config.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

// 실제 인증에 대한 부분 중 인증 전 객체를 받아 인증된 객체를 반환하는 역할
@Slf4j
@Component
@PropertySource("classpath:/keys/jwtKey.properties")
public class TokenProviderUtil {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";     // token 인증 타입(jwt 토큰을 의미)
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24;       // 1일
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7일

    // key는 HS512 알고리즘을 사용함
    private final Key key;
    private final CustomUserDetailsService customUserDetailsService;

    public TokenProviderUtil(CustomUserDetailsService customUserDetailsService
                         , @Value("${key}") String secretKey
    ){
        this.customUserDetailsService = customUserDetailsService;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenDto generateToken(Authentication authentication){
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
                .userInfo(UserResponseDto.builder().id(authentication.getName()).build())
                .build();
    }

    public Authentication getAuthentication(String accessToken){
        // 토큰 복호화 (내부 정보 가져옴)
        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null){
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
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
    private Claims parseClaims(String accessToken){
        try {
            return Jwts.parserBuilder().setSigningKey(key)
                    .build().parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e){
            return e.getClaims();
        }
    }

    // 토큰 정보 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }


    public static Date getRefreshTokenExpireTime() {
        return new Date(new Date().getTime()+REFRESH_TOKEN_EXPIRE_TIME);
    }

    // logout token의 expiration 계산
    public long getRemainExpiration(String token){
        Date currentTime = parseClaims(token).getExpiration();
        Date now = new Date();
        // redis의 단위는 초로, 밀리초를 초로 변환하는 과정의 오차를 감안하기 위해 1초 더함.
        return ((currentTime.getTime() - now.getTime())/1000)+1;
    }
}
