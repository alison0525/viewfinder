package com.viewfinder.global.exception;

import org.springframework.http.HttpStatus;

// 여러 도메인에서 공통으로 사용하는 오류 코드 정의
public enum CommonErrorCode implements ErrorCode {

    // 요청 DTO 검증 실패 오류 정의
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "요청 값이 올바르지 않습니다."),
    // 예상하지 못한 서버 내부 오류 정의
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    // enum 항목의 HTTP 상태·서비스 코드·메시지 저장
    CommonErrorCode(HttpStatus status, String code, String message) {
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
