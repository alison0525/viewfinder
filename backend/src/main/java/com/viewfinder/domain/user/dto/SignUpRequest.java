package com.viewfinder.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 로컬 회원가입 요청으로 전달할 불변 데이터 정의
public record SignUpRequest(
        // 이메일 형식과 빈 문자열 불가 조건 지정
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,
        // 최소 8자·최대 20자 비밀번호 길이 조건 지정
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
        String password,
        // users.nickname 컬럼 길이에 맞춘 닉네임 길이 조건 지정
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
        String nickname
) {
}
