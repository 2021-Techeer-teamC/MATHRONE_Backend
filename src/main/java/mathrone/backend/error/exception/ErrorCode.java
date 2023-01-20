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

    KAKAO_ACCOUNT_IS_DUPLICATION(UNAUTHORIZED, "이미 해당 카카오계정으로 가입이 진행되었습니다."),
    KAKAO_ACCOUNT_NOT_FOUND(UNAUTHORIZED, "해당 계정으로 가입이 진행되지 않았습니다. 회원가입을 진행한 후 로그인해주세요."),

    USER_NOT_FOUND(UNAUTHORIZED, "존재하지 않는 유저입니다."),

    //google server error
    GOOGLE_SERVER_ERROR(UNAUTHORIZED, "해당 계정으로 가입이 진행되지 않았습니다. 회원가입을 진행한 후 로그인해주세요."),

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
