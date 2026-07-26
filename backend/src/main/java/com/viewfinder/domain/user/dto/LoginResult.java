package com.viewfinder.domain.user.dto;

// Service 내부에서 로그인 User 정보와 발급 Token을 함께 전달하는 불변 데이터 정의
public record LoginResult(
        LoginResponse loginResponse,
        String accessToken,
        String refreshToken
) {
}
