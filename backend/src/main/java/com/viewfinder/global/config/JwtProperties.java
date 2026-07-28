package com.viewfinder.global.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

// JWT 서명·만료 정책을 YAML과 환경변수에서 읽는 불변 설정 객체 지정
@ConfigurationProperties(prefix = "jwt")
@Validated
public record JwtProperties(
        // HMAC 서명에 사용할 환경변수 비밀키 필수 조건 지정
        @NotBlank String secret,
        // Access Token 만료 시간 필수 조건 지정
        @NotNull Duration accessTokenExpiration,
        // Refresh Token 만료 시간 필수 조건 지정
        @NotNull Duration refreshTokenExpiration
) {
}
