package com.viewfinder.global.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

// JWT Cookie의 브라우저 보안 속성을 YAML에서 읽는 불변 설정 객체 지정
@ConfigurationProperties(prefix = "jwt.cookie")
@Validated
public record JwtCookieProperties(
        // HTTPS 전송만 허용할지 결정하는 Secure 속성 지정
        boolean secure,
        // CSRF 완화에 사용할 SameSite 속성 필수 조건 지정
        @NotBlank String sameSite
) {
}
