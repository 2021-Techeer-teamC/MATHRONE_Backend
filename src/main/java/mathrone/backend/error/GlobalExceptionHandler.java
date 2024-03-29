package mathrone.backend.error;

import static mathrone.backend.error.exception.ErrorCode.ACCESS_DENIED_ERROR;
import static mathrone.backend.error.exception.ErrorCode.AUTHENTICATION_ERROR;
import static mathrone.backend.error.exception.ErrorCode.INVALID_REQUEST;
import static mathrone.backend.error.exception.ErrorCode.SERVER_ERROR;

import lombok.extern.slf4j.Slf4j;
import mathrone.backend.error.exception.CustomException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice // 모든 RestController error handling
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ue) {
        StackTraceElement ste = ue.getStackTrace()[0];
        log.info("[{}-{}] : {}",ste.getClassName(), ste.getLineNumber(), ue.getMessage());
        return ErrorResponse.toResponseEntity(ACCESS_DENIED_ERROR);
    }

    @ExceptionHandler(value = {AuthenticationException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ue) {
        StackTraceElement ste = ue.getStackTrace()[0];
        log.info("[{}-{}] : {}",ste.getClassName(), ste.getLineNumber(), ue.getMessage());
        return ErrorResponse.toResponseEntity(AUTHENTICATION_ERROR);
    }

    @ExceptionHandler(value = {CustomException.class})
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ue) {
        StackTraceElement ste = ue.getStackTrace()[0];
        log.info("[{}-{}] : {}",ste.getClassName(), ste.getLineNumber(), ue.getMessage());
        return ErrorResponse.toResponseEntity(ue.getErrorCode());
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<Object> handleException(Exception e) {
        StackTraceElement ste = e.getStackTrace()[0];
        log.info("[{}-{}] : {}",ste.getClassName(), ste.getLineNumber(), e.getMessage());
        if (e.getMessage() == null){
            return ErrorResponse.unConfirmedErrorTtoResponseEntity(SERVER_ERROR);
        } else {
            return ErrorResponse.toResponseEntity(SERVER_ERROR, e.getMessage());
        }
    }



    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status,
        WebRequest request) {
        return ErrorResponse.toResponseEntity(INVALID_REQUEST,
            ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

}
