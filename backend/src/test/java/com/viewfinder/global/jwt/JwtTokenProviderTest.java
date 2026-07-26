package com.viewfinder.global.jwt;

import com.viewfinder.global.config.JwtProperties;
import com.viewfinder.domain.user.enums.Role;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

// JWT 생성·서명 검증·만료 검증 동작을 확인하는 단위 테스트 지정
class JwtTokenProviderTest {

    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
            new JwtProperties(
                    "test-jwt-secret-must-be-longer-than-thirty-two-bytes",
                    Duration.ofMinutes(30),
                    Duration.ofDays(14)
            )
    );

    @Test
    void createsAndParsesAccessToken() {
        String accessToken = jwtTokenProvider.createAccessToken(1L, Role.USER);

        assertThat(jwtTokenProvider.isValidToken(accessToken)).isTrue();
        assertThat(jwtTokenProvider.getUserId(accessToken)).isEqualTo(1L);
        assertThat(jwtTokenProvider.getTokenType(accessToken)).isEqualTo(TokenType.ACCESS);
        assertThat(jwtTokenProvider.getRole(accessToken)).isEqualTo(Role.USER);
    }

    @Test
    void createsAndParsesRefreshToken() {
        String refreshToken = jwtTokenProvider.createRefreshToken(2L);

        assertThat(jwtTokenProvider.isValidToken(refreshToken)).isTrue();
        assertThat(jwtTokenProvider.getUserId(refreshToken)).isEqualTo(2L);
        assertThat(jwtTokenProvider.getTokenType(refreshToken)).isEqualTo(TokenType.REFRESH);
    }

    @Test
    void rejectsExpiredToken() {
        JwtTokenProvider expiredTokenProvider = new JwtTokenProvider(
                new JwtProperties(
                        "test-jwt-secret-must-be-longer-than-thirty-two-bytes",
                        Duration.ofSeconds(-1),
                        Duration.ofDays(14)
                )
        );

        String expiredToken = expiredTokenProvider.createAccessToken(1L, Role.USER);

        assertThat(expiredTokenProvider.isValidToken(expiredToken)).isFalse();
    }
}
