package com.viewfinder.domain.user.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

// User 오류 코드의 HTTP 상태와 외부 노출 메시지를 확인하는 단위 테스트 지정
class UserErrorCodeTest {

    @Test
    void definesGenericLoginFailure() {
        assertThat(UserErrorCode.LOGIN_FAILED.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(UserErrorCode.LOGIN_FAILED.code()).isEqualTo("USER_003");
        assertThat(UserErrorCode.LOGIN_FAILED.message())
                .isEqualTo("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}
