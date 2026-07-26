package com.viewfinder.global.exception;

import org.springframework.http.HttpStatus;

// 도메인별 오류 코드가 공통으로 제공할 정보 정의
public interface ErrorCode {

    // HTTP 응답 상태 반환
    HttpStatus status();

    // 클라이언트가 분기 처리에 사용할 서비스 오류 코드 반환
    String code();

    // 사용자에게 전달할 기본 오류 메시지 반환
    String message();
}
