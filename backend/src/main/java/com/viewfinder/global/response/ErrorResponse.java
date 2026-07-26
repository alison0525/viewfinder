package com.viewfinder.global.response;

// 실패 처리 결과를 같은 JSON 형식으로 감싸는 공통 응답 DTO 지정
public record ErrorResponse(
        boolean success,
        ErrorDetail error
) {

    // ErrorCode 정보를 error 객체로 변환한 실패 응답 생성
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(false, new ErrorDetail(code, message));
    }

    // 클라이언트가 확인할 서비스 오류 코드와 메시지 묶음 정의
    public record ErrorDetail(
            String code,
            String message
    ) {
    }
}
