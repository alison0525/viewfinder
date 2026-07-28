package com.viewfinder.domain.user.dto;

// 회원가입 완료 후 클라이언트에 반환할 불변 데이터 정의
public record SignUpResponse(
        Long id,
        String email,
        String nickname
) {
}
