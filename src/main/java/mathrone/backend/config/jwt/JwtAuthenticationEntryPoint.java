package mathrone.backend.config.jwt;

import mathrone.backend.error.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        CustomException exception = (CustomException) request.getAttribute("Exception");
        if (exception == null)
            // 인증 과정에서 유효 자격증명을 제공하지 않고 접근할 경우 401
            resolver.resolveException(request, response, null, authException);
        else {
            // JWT 토큰이 잘못된 경우
            resolver.resolveException(request, response, null, exception);
        }
    }
}
