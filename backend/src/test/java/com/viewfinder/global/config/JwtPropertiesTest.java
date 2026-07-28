package com.viewfinder.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

// JWT YAML 속성이 Duration과 불변 설정 객체로 바인딩되는지 확인하는 단위 테스트 지정
class JwtPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(JwtConfig.class)
            .withPropertyValues(
                    "jwt.secret=test-jwt-secret-must-be-longer-than-thirty-two-bytes",
                    "jwt.access-token-expiration=30m",
                    "jwt.refresh-token-expiration=14d",
                    "jwt.cookie.same-site=Lax"
            );

    @Test
    void bindsJwtProperties() {
        contextRunner.run(context -> {
            JwtProperties jwtProperties = context.getBean(JwtProperties.class);

            assertThat(jwtProperties.secret()).isEqualTo("test-jwt-secret-must-be-longer-than-thirty-two-bytes");
            assertThat(jwtProperties.accessTokenExpiration()).isEqualTo(Duration.ofMinutes(30));
            assertThat(jwtProperties.refreshTokenExpiration()).isEqualTo(Duration.ofDays(14));
        });
    }
}
