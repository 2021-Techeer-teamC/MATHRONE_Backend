package mathrone.backend.config.jwt;

import static mathrone.backend.error.exception.ErrorCode.AlREADY_LOGOUT;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mathrone.backend.error.exception.CustomException;
import mathrone.backend.repository.redisRepository.LogoutAccessTokenRedisRepository;
import mathrone.backend.util.TokenProviderUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProviderUtil tokenProviderUtil;
    private final LogoutAccessTokenRedisRepository logoutAccessTokenRedisRepository;

    // 실제 필터링 로직은 doFilterInternal 에 들어감
    // JWT 토큰의 인증 정보를 현재 쓰레드의 SecurityContext 에 저장하는 역할
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        // 1. Request Header 에서 access token 빼기
        String accessToken = tokenProviderUtil.resolveToken(request);

        // 2. validateToken 으로 토큰 유효성 검사
        if (StringUtils.hasText(accessToken) && tokenProviderUtil.validateToken(accessToken, request)) {
            // 3. Logout한 회원인지 검사
            if (checkLogout(accessToken, request)){
                // 정상 토큰이면 해당 토큰으로 Authentication 을 가져와서 SecurityContext 에 저장
                Authentication authentication = tokenProviderUtil.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    // logout인 회원일 경우 해당 회원의 access token으로 접근을 방지하기 위함
    private boolean checkLogout(String accessToken, HttpServletRequest request) {
        // logoutToken은 해당 회원의 access token을 id로 가짐.
        if (logoutAccessTokenRedisRepository.existsById(accessToken)) {
            return true;
        } else {
            request.setAttribute("Exception", new CustomException(AlREADY_LOGOUT));
            return false;
        }
    }
}
