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
    NOT_PREMIUM(UNAUTHORIZED, "유저의 등급이 premium이 아닙니다."),

    // problem try
    EMPTY_FAILED_PROBLEM(NOT_FOUND, "유저가 시도한 문제 중, 틀린 문제가 존재하지 않습니다"),
    EMPTY_FAILED_PROBLEM_IN_REDIS(NOT_FOUND, "Redis 에 유저가 시도한 문제 중 틀린 문제 정보가 존재하지 않습니다."),
    NONEXISTENT_FAILED_WORKBOOK(NOT_FOUND, "Redis 에 유저가 틀린 문제 중 해당 문제집에 대한 틀린 문제 정보가 존재하지 않습니다"),
    NONEXISTENT_FAILED_CHAPTER(NOT_FOUND, "Redis 에 유저가 틀린 문제 중 해당 챕터에 대한 틀린 문제 정보가 존재하지 않습니다"),

    // workbook
    NOT_FOUND_WORKBOOK(NOT_FOUND, "해당 workbook이 존재하지 않습니다"),

    // chapter
    NOT_FOUND_CHAPTER(NOT_FOUND, "workbook에 chapter가 존재하지 않습니다. "),

    // token
    INVALID_ACCESS_TOKEN(UNAUTHORIZED, "Access Token이 유효하지 않습니다."),

    // Common
    SERVER_ERROR(INTERNAL_SERVER_ERROR, "예상치 못한 에러가 발생하였습니다.")

    ;


    private final HttpStatus httpStatus;

    private final String detail;

}
