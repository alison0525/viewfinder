package com.viewfinder.domain.user.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// record 컴포넌트에 선언한 회원가입 요청 검증 규칙을 확인하는 단위 테스트 지정
class SignUpRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsValidSignUpRequest() {
        SignUpRequest request = new SignUpRequest(
                "user@example.com",
                "password1234",
                "viewfinder"
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsInvalidSignUpRequest() {
        SignUpRequest request = new SignUpRequest(
                "invalid-email",
                "short",
                ""
        );

        assertThat(validator.validate(request)).hasSize(3);
    }
}
