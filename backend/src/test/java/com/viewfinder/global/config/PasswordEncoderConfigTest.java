package com.viewfinder.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

// BCrypt 비밀번호 암호화·검증 동작을 확인하는 단위 테스트 지정
class PasswordEncoderConfigTest {

    private final PasswordEncoder passwordEncoder = new PasswordEncoderConfig().passwordEncoder();

    @Test
    void encodesAndMatchesPassword() {
        String rawPassword = "password1234";

        // 평문과 다른 단방향 해시 문자열 생성 확인
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        // 저장된 해시와 로그인 입력 평문의 일치 여부 확인
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }
}
