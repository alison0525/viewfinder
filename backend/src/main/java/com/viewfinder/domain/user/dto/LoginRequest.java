package com.viewfinder.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// 로컬 로그인 요청으로 전달할 불변 데이터 정의
public record LoginRequest(
        // 이메일 형식과 빈 문자열 불가 조건 지정
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,
        // 비밀번호 빈 문자열 불가 조건 지정
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {
}
