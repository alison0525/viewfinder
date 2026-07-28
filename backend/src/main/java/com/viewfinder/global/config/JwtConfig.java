package com.viewfinder.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// JWT 설정 속성을 Spring Bean으로 등록하는 설정 지정
@Configuration
@EnableConfigurationProperties({JwtProperties.class, JwtCookieProperties.class})
public class JwtConfig {
}
