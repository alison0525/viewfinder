package com.viewfinder.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// JwtProperties를 Spring Bean으로 등록하는 설정 지정
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {
}
