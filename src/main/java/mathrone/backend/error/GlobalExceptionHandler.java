package mathrone.backend.error;

import static mathrone.backend.error.exception.ErrorCode.SERVER_ERROR;

import lombok.extern.slf4j.Slf4j;
import mathrone.backend.error.exception.UserException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice // 모든 RestController error handling
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(value = { UserException.class })
    public ResponseEntity<ErrorResponse> handleUserException(UserException ue) {
        log.error("handleUserException throw Exception : {}", ue.getErrorCode());
        return ErrorResponse.toResponseEntity(ue.getErrorCode());
    }

    @ExceptionHandler(value = { Exception.class })
    public ResponseEntity<ErrorResponse> handleException() {
        log.error("handleUserException throw Exception : {}", SERVER_ERROR.getDetail());
        return ErrorResponse.toResponseEntity(SERVER_ERROR);
    }

}
