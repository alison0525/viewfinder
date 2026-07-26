package com.viewfinder.global.exception;

// 서비스 규칙 위반을 ErrorCode와 함께 전파하는 런타임 예외 정의
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    // 기본 오류 메시지와 ErrorCode를 가진 예외 생성
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    // 전역 예외 처리기가 HTTP 응답 생성에 사용할 ErrorCode 반환
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
