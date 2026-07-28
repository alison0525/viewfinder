package com.viewfinder.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// 비밀번호 암호화 방식을 Spring Bean으로 등록하는 설정 지정
@Configuration
public class PasswordEncoderConfig {

    // 단방향 BCrypt 해시를 사용하는 PasswordEncoder Bean 생성
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
