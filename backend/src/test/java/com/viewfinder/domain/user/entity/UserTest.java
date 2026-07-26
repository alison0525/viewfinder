package com.viewfinder.domain.user.entity;

import com.viewfinder.domain.user.enums.Provider;
import com.viewfinder.domain.user.enums.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// 로컬 회원가입 User 생성 규칙을 확인하는 단위 테스트 지정
class UserTest {

    @Test
    void createsLocalUserWithEmailAsProviderId() {
        User user = User.createLocal(
                "user@example.com",
                "encoded-password",
                "viewfinder"
        );

        // 로컬 계정 식별 규칙인 LOCAL·이메일 providerId 적용 확인
        assertThat(user.getProvider()).isEqualTo(Provider.LOCAL);
        assertThat(user.getProviderId()).isEqualTo("user@example.com");
        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(user.getNickname()).isEqualTo("viewfinder");
        assertThat(user.getRole()).isEqualTo(Role.USER);
    }
}
