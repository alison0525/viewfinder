package com.viewfinder.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

// 브라우저 Cookie 인증 요청을 허용할 프런트 Origin 목록 설정 정의
@ConfigurationProperties(prefix = "cors")
public record CorsProperties(
        List<String> allowedOrigins
) {
}
