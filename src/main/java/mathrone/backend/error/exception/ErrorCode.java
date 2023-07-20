package mathrone.backend.error.exception;

import static org.springframework.http.HttpStatus.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // user error
    ACCOUNT_NOT_FOUND(UNAUTHORIZED, "아이디를 찾을 수 없습니다.", "U001"),
    ACCOUNT_IS_DUPLICATION(UNAUTHORIZED, "같은 아이디가 이미 존재합니다.", "U002"),
    NOT_PREMIUM(UNAUTHORIZED, "유저의 등급이 premium이 아닙니다.", "U003"),

    //user login error
    USER_NOT_FOUND(UNAUTHORIZED, "존재하지 않는 유저입니다.", "U004"),
    ACCOUNT_NOT_EXIST(UNAUTHORIZED, "존재하지 않는 계정입니다.", "U005"),
    PASSWORD_NOT_CORRECT(UNAUTHORIZED, "비밀번호를 다시 확인해주세요.", "U006"),
    EMAIL_NOT_EXIST(UNAUTHORIZED, "존재하지 않는 아매알입니다.", "U007"),

    // problem try
    EMPTY_FAILED_PROBLEM(NOT_FOUND, "유저가 시도한 문제 중, 틀린 문제가 존재하지 않습니다", "P001"),
    EMPTY_FAILED_PROBLEM_IN_REDIS(NOT_FOUND, "Redis 에 유저가 시도한 문제 중 틀린 문제 정보가 존재하지 않습니다.", "P002"),
    NONEXISTENT_FAILED_WORKBOOK(NOT_FOUND, "Redis 에 유저가 틀린 문제 중 해당 문제집에 대한 틀린 문제 정보가 존재하지 않습니다",
        "P003"),
    NONEXISTENT_FAILED_CHAPTER(NOT_FOUND, "Redis 에 유저가 틀린 문제 중 해당 챕터에 대한 틀린 문제 정보가 존재하지 않습니다",
        "P004"),

    // kakao
    KAKAO_ACCOUNT_IS_DUPLICATION(UNAUTHORIZED, "이미 해당 카카오계정으로 가입이 진행되었습니다.", "K001"),
    KAKAO_ACCOUNT_NOT_FOUND(UNAUTHORIZED, "해당 계정으로 가입이 진행되지 않았습니다. 회원가입을 진행한 후 로그인해주세요.", "K002"),

    // workbook
    NOT_FOUND_WORKBOOK(NOT_FOUND, "해당 workbook이 존재하지 않습니다", "W001"),
    INVALID_LEVEL_VALUE(BAD_REQUEST, "문제집을 평가하기 위한 level값이 잘못되었습니다", "W002"),

    // chapter
    NOT_FOUND_CHAPTER(NOT_FOUND, "workbook에 chapter가 존재하지 않습니다. ", "WC01"),

    // token
    INVALID_ACCESS_TOKEN(UNAUTHORIZED, "Access token이 유효하지 않습니다.", "T001"),
    EXPIRED_TOKEN(UNAUTHORIZED, "만료된 JWT 토큰입니다.", "T002"),
    INVALID_SIGNATURE(UNAUTHORIZED, "잘못된 JWT 서명입니다.", "T003"),
    UNSUPPORTED_TOKEN(UNAUTHORIZED, "지원되지 않는 JWT 토큰입니다.", "T004"),
    INVALID_REFRESH_TOKEN(UNAUTHORIZED, "Refresh token이 유효하지 않습니다.", "T005"),
    NOT_AUTH_INFORMATION(UNAUTHORIZED, "권한 정보가 없는 토큰입니다.", "T006"),
    AlREADY_LOGOUT(UNAUTHORIZED, "이미 로그아웃한 회원입니다.", "T007"),


    // google
    GOOGLE_ACCOUNT_IS_DUPLICATION(UNAUTHORIZED, "이미 해당 구글계정으로 가입이 진행되었습니다.", "G001"),
    GOOGLE_ACCOUNT_NOT_FOUND(UNAUTHORIZED, "해당 계정으로 가입이 진행되지 않았습니다. 회원가입을 진행한 후 로그인해주세요.", "G002"),

    //google server error
    GOOGLE_SERVER_ERROR(UNAUTHORIZED, "해당 계정으로 가입이 진행되지 않았습니다. 회원가입을 진행한 후 로그인해주세요.", "G003"),


    // Common
    SERVER_ERROR(INTERNAL_SERVER_ERROR, "예상치 못한 에러가 발생하였습니다.", "C001"),
    AUTHENTICATION_ERROR(UNAUTHORIZED, "인증 정보가 유효하지 않습니다.", "C002"),
    ACCESS_DENIED_ERROR(FORBIDDEN, "해당 요청에 대한 접근 권한이 존재하지 않습니다.", "C003");


    private final HttpStatus httpStatus;

    private final String detail;

    private final String code;

}
