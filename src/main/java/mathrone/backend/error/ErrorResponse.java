package mathrone.backend.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mathrone.backend.error.exception.ErrorCode;
import org.springframework.http.ResponseEntity;

@Getter
@Builder
public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String message;
    private final String code;

    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode) {
        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(ErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .message(errorCode.getDetail())
                .code(errorCode.getCode())
                .build()
            );
    }

    public static ResponseEntity<Object> unConfirmedErrorTtoResponseEntity(ErrorCode errorCode){
        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(ErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .message(errorCode.getDetail())
                .code(errorCode.getCode())
                .build()
            );
    }

    public static ResponseEntity<Object> toResponseEntity(ErrorCode errorCode,
        String errorMessage) {
        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(ErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .message(errorMessage)
                .code(errorCode.getCode())
                .build()
            );
    }

}
