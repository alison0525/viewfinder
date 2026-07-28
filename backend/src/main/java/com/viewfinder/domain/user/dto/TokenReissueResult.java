package com.viewfinder.domain.user.dto;

// Refresh Token 검증 뒤 새로 발급한 Access·Refresh Token을 전달하는 내부 결과 정의
public record TokenReissueResult(
        String accessToken,
        String refreshToken
) {
}
