package com.viewfinder.domain.user.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// record 컴포넌트에 선언한 로컬 로그인 요청 검증 규칙을 확인하는 단위 테스트 지정
class LoginRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsValidLoginRequest() {
        LoginRequest request = new LoginRequest("user@example.com", "password1234");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsInvalidLoginRequest() {
        LoginRequest request = new LoginRequest("invalid-email", "");

        assertThat(validator.validate(request)).hasSize(2);
    }
}
