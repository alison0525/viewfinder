package com.viewfinder.global.response;

// 정상 처리 결과를 같은 JSON 형식으로 감싸는 공통 응답 DTO 지정
public record ApiResponse<T>(
        boolean success,
        T data
) {

    // data를 포함한 성공 응답 객체 생성
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data);
    }
}
