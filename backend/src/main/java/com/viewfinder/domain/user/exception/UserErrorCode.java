package com.viewfinder.domain.user.exception;

import com.viewfinder.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

// User 도메인에서 사용하는 오류 코드 정의
public enum UserErrorCode implements ErrorCode {

    // 동일 이메일의 로컬 회원가입 중복 오류 정의
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_001", "이미 사용 중인 이메일입니다."),
    // 동일 닉네임의 회원가입 중복 오류 정의
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_002", "이미 사용 중인 닉네임입니다."),
    // 이메일 존재 여부와 비밀번호 일치 여부를 함께 숨기는 로그인 실패 오류 정의
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "USER_003", "이메일 또는 비밀번호가 올바르지 않습니다."),
    // Refresh Token의 유효성·Redis 저장 여부를 함께 숨기는 재발급 실패 오류 정의
    TOKEN_REISSUE_FAILED(HttpStatus.UNAUTHORIZED, "USER_004", "로그인 정보를 갱신할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    // enum 항목의 HTTP 상태·서비스 코드·메시지 저장
    UserErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
