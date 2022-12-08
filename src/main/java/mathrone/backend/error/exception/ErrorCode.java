package mathrone.backend.error.exception;

import static org.springframework.http.HttpStatus.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // user error
    ACCOUNT_NOT_FOUND(UNAUTHORIZED, "아이디를 찾을 수 없습니다."),
    ACCOUNT_IS_DUPLICATION(UNAUTHORIZED, "같은 아이디가 이미 존재합니다."),

    GOOGLE_ACCOUNT_IS_DUPLICATION(UNAUTHORIZED, "이미 해당 구글계정으로 가입이 진행되었습니다."),
    GOOGLE_ACCOUNT_NOT_FOUND(UNAUTHORIZED, "해당 계정으로 가입이 진행되지 않았습니다. 회원가입을 진행한 후 로그인해주세요."),

    //google server error
    GOOGLE_SERVER_ERROR(UNAUTHORIZED, "해당 계정으로 가입이 진행되지 않았습니다. 회원가입을 진행한 후 로그인해주세요."),

    // Common
    SERVER_ERROR(INTERNAL_SERVER_ERROR, "예상치 못한 에러가 발생하였습니다.")

    ;


    private final HttpStatus httpStatus;

    private final String detail;

}
