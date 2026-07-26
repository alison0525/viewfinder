package com.viewfinder.global.exception;

import com.viewfinder.global.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Controller 전역에서 발생한 예외를 공통 오류 JSON으로 변환하는 설정 지정
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 서비스 규칙 위반 예외를 ErrorCode에 맞는 HTTP 응답으로 변환
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
        return errorResponse(exception.getErrorCode(), exception.getMessage());
    }

    // @Valid 요청 DTO 검증 실패를 400 공통 오류 응답으로 변환
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        return errorResponse(CommonErrorCode.INVALID_INPUT_VALUE);
    }

    // 처리하지 못한 예외를 내부 오류 응답으로 변환하고 세부 정보 노출 차단
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception) {
        return errorResponse(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    // ErrorCode의 HTTP 상태와 JSON 오류 본문을 함께 생성
    private ResponseEntity<ErrorResponse> errorResponse(ErrorCode errorCode) {
        return errorResponse(errorCode, errorCode.message());
    }

    // ErrorCode의 HTTP 상태와 상황별 오류 메시지를 함께 생성
    private ResponseEntity<ErrorResponse> errorResponse(ErrorCode errorCode, String message) {
        return ResponseEntity
                .status(errorCode.status())
                .body(ErrorResponse.of(errorCode.code(), message));
    }
}
