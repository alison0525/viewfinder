package com.viewfinder.global.jwt;

import com.viewfinder.global.config.JwtCookieProperties;
import com.viewfinder.global.config.JwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

// Access·Refresh Cookie의 이름·경로·보안 속성을 확인하는 단위 테스트 지정
class JwtCookieProviderTest {

    private final JwtCookieProvider jwtCookieProvider = new JwtCookieProvider(
            new JwtProperties(
                    "test-jwt-secret-must-be-longer-than-thirty-two-bytes",
                    Duration.ofMinutes(30),
                    Duration.ofDays(14)
            ),
            new JwtCookieProperties(false, "Lax")
    );

    @Test
    void createsAccessTokenCookie() {
        ResponseCookie cookie = jwtCookieProvider.createAccessTokenCookie("access-token");

        assertThat(cookie.getName()).isEqualTo(JwtCookieProvider.ACCESS_TOKEN_COOKIE_NAME);
        assertThat(cookie.getValue()).isEqualTo("access-token");
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofMinutes(30));
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isFalse();
        assertThat(cookie.getSameSite()).isEqualTo("Lax");
    }

    @Test
    void createsRefreshTokenCookie() {
        ResponseCookie cookie = jwtCookieProvider.createRefreshTokenCookie("refresh-token");

        assertThat(cookie.getName()).isEqualTo(JwtCookieProvider.REFRESH_TOKEN_COOKIE_NAME);
        assertThat(cookie.getValue()).isEqualTo("refresh-token");
        assertThat(cookie.getPath()).isEqualTo("/api/v1/auth");
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofDays(14));
        assertThat(cookie.isHttpOnly()).isTrue();
    }
}
