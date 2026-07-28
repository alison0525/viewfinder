package com.viewfinder.domain.user.dto;

// 로컬 로그인 성공 후 반환할 불변 User 기본 정보 정의
public record LoginResponse(
        Long id,
        String email,
        String nickname
) {
}
