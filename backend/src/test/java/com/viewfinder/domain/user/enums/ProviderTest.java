package com.viewfinder.domain.user.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// 로그인 방식 안내에 사용할 Provider 표시 이름을 확인하는 단위 테스트 지정
class ProviderTest {

    @Test
    void returnsDisplayName() {
        assertThat(Provider.LOCAL.getDisplayName()).isEqualTo("이메일");
        assertThat(Provider.KAKAO.getDisplayName()).isEqualTo("카카오");
    }
}
