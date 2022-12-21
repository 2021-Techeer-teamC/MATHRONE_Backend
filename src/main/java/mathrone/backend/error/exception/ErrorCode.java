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


    //user login error
    /*
    1. 존재하지 않는 계정
    2. 비밀번호가 일치하지 않는다
    */

    ACCOUNT_NOT_EXIST(UNAUTHORIZED,"존재하지 않는 계정입니다."),
    PASSWORD_NOT_CORRECT(UNAUTHORIZED,"비밀번호를 다시 확인해주세요."),

    // Common
    SERVER_ERROR(INTERNAL_SERVER_ERROR, "예상치 못한 에러가 발생하였습니다.")

    ;


    private final HttpStatus httpStatus;

    private final String detail;

}
