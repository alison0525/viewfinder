package com.viewfinder.global.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

// Redis 명령과 사용자별 Key·TTL 규칙을 확인하는 단위 테스트 지정
class RefreshTokenStoreTest {

    private StringRedisTemplate stringRedisTemplate;
    private ValueOperations<String, String> valueOperations;
    private RefreshTokenStore refreshTokenStore;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        stringRedisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
        refreshTokenStore = new RefreshTokenStore(stringRedisTemplate);
    }

    @Test
    void savesRefreshTokenWithUserKeyAndExpiration() {
        refreshTokenStore.save(1L, "refresh-token", Duration.ofDays(14));

        verify(valueOperations).set("auth:refresh:1", "refresh-token", Duration.ofDays(14));
    }

    @Test
    void findsRefreshTokenByUserId() {
        given(valueOperations.get("auth:refresh:1")).willReturn("refresh-token");

        assertThat(refreshTokenStore.findByUserId(1L)).contains("refresh-token");
    }

    @Test
    void deletesRefreshTokenByUserId() {
        refreshTokenStore.deleteByUserId(1L);

        verify(stringRedisTemplate).delete("auth:refresh:1");
    }
}
