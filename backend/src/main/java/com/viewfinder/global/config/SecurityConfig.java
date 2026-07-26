package com.viewfinder.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

// HTTP 요청의 인증·인가 규칙을 정의하는 Spring Security 설정 지정
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // API 요청에 적용할 Security Filter Chain 생성
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 세션 기반 웹 폼이 아닌 REST API이므로 CSRF 검증 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                // 이후 JWT 인증을 적용할 수 있도록 서버 세션 생성 금지
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 회원가입·로그인 등 인증 시작 경로만 비인증 접근 허용
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                // 인증 정보 없는 보호 API 요청을 리다이렉트 대신 401 상태로 응답
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .build();
    }
}
