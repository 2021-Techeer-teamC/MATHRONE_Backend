package mathrone.backend.config.jwt;

import lombok.RequiredArgsConstructor;
import mathrone.backend.repository.tokenRepository.LogoutAccessTokenRedisRepository;
import mathrone.backend.util.TokenProviderUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";  // http header 종류
    public static final String BEARER_PREFIX = "Bearer";    // http 인증 type

    private final TokenProviderUtil tokenProviderUtil;
    private final LogoutAccessTokenRedisRepository logoutAccessTokenRedisRepository;

    // 실제 필터링 로직은 doFilterInternal 에 들어감
    // JWT 토큰의 인증 정보를 현재 쓰레드의 SecurityContext 에 저장하는 역할
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. Request Header 에서 access token 빼기
        String accessToken = resolveToken(request);

        // 2. validateToken 으로 토큰 유효성 검사
        if (StringUtils.hasText(accessToken) && tokenProviderUtil.validateToken(accessToken)){
            // 3. Logout한 회원인지 검사
            checkLogout(accessToken);
            // 정상 토큰이면 해당 토큰으로 Authentication 을 가져와서 SecurityContext 에 저장
            Authentication authentication = tokenProviderUtil.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request,response);
    }

    // Request Header의 토큰 정보 가져오는 메소드
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) &&
                bearerToken.startsWith(BEARER_PREFIX))
            return bearerToken.substring(7);
        return null;
    }

    // logout인 회원일 경우 해당 회원의 access token으로 접근을 방지하기 위함
    private void checkLogout(String accessToken) {
        // logoutToken은 해당 회원의 access token을 id로 가짐.
        if (logoutAccessTokenRedisRepository.existsById(accessToken)){
            throw new IllegalArgumentException("이미 로그아웃한 회원입니다.");
        }
    }
}
