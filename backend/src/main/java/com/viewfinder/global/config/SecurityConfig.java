package com.viewfinder.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.viewfinder.global.jwt.JwtAuthenticationFilter;

import java.util.List;

// HTTP 요청의 인증·인가 규칙을 정의하는 Spring Security 설정 지정
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(CorsProperties.class)
public class SecurityConfig {

    // API 요청에 적용할 Security Filter Chain 생성
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        return http
                // 프런트 Origin·Cookie 전송·CSRF Header를 제한적으로 허용하는 CORS 정책 연결
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // Cookie 인증에서는 브라우저가 Cookie를 자동 전송하므로 SPA용 CSRF Token Cookie·헤더 대조 활성화
                // 프런트는 XSRF-TOKEN Cookie 값을 읽어 상태 변경 요청의 X-XSRF-TOKEN 헤더에 함께 전송
                .csrf(csrf -> csrf.spa())
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
                // 기본 사용자명·비밀번호 인증 필터보다 먼저 JWT Cookie 인증 처리
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // 브라우저가 다른 포트의 프런트에서 보낸 Cookie 인증 API 요청을 검증할 CORS 설정 생성
    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration configuration = new CorsConfiguration();

        // credentials 사용 시 모든 Origin(*) 허용은 불가능하므로 환경별 허용 Origin만 명시
        configuration.setAllowedOrigins(corsProperties.allowedOrigins());
        // API 호출·CSRF 사전 요청에 필요한 HTTP 메서드만 허용
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // JSON 본문과 CSRF Token 전달에 필요한 요청 Header만 허용
        configuration.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN"));
        // Access·Refresh·XSRF Cookie가 프런트 요청에 포함되도록 브라우저 credentials 전송 허용
        configuration.setAllowCredentials(true);
        // 같은 사전 요청을 매번 보내지 않도록 브라우저의 Preflight 결과 1시간 캐싱
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
