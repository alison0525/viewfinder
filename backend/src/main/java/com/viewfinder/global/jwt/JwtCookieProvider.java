package com.viewfinder.global.jwt;

import com.viewfinder.global.config.JwtCookieProperties;
import com.viewfinder.global.config.JwtProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

// JWT 문자열을 브라우저 보안 속성이 적용된 HTTP Cookie로 변환하는 컴포넌트 지정
@Component
public class JwtCookieProvider {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    private final JwtProperties jwtProperties;
    private final JwtCookieProperties jwtCookieProperties;

    // Cookie 만료 시간과 브라우저 보안 속성 설정 주입
    public JwtCookieProvider(JwtProperties jwtProperties, JwtCookieProperties jwtCookieProperties) {
        this.jwtProperties = jwtProperties;
        this.jwtCookieProperties = jwtCookieProperties;
    }

    // 모든 API 요청에 전송할 짧은 Access Token Cookie 생성
    public ResponseCookie createAccessTokenCookie(String accessToken) {
        return createCookie(
                ACCESS_TOKEN_COOKIE_NAME,
                accessToken,
                jwtProperties.accessTokenExpiration(),
                "/"
        );
    }

    // 재발급·로그아웃 경로에만 전송할 긴 Refresh Token Cookie 생성
    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return createCookie(
                REFRESH_TOKEN_COOKIE_NAME,
                refreshToken,
                jwtProperties.refreshTokenExpiration(),
                "/api/v1/auth"
        );
    }

    // 브라우저의 Access Token Cookie를 즉시 만료시키는 빈 Cookie 생성
    public ResponseCookie expireAccessTokenCookie() {
        return createCookie(ACCESS_TOKEN_COOKIE_NAME, "", java.time.Duration.ZERO, "/");
    }

    // 브라우저의 Refresh Token Cookie를 즉시 만료시키는 빈 Cookie 생성
    public ResponseCookie expireRefreshTokenCookie() {
        return createCookie(REFRESH_TOKEN_COOKIE_NAME, "", java.time.Duration.ZERO, "/api/v1/auth");
    }

    // HttpOnly·Secure·SameSite 보안 속성과 만료 시간을 적용한 Cookie 생성
    private ResponseCookie createCookie(String name, String value, java.time.Duration maxAge, String path) {
        return ResponseCookie.from(name, value)
                // JavaScript가 Token을 읽지 못하도록 설정
                .httpOnly(true)
                // 개발·운영 HTTPS 환경에 맞춘 전송 제한 설정
                .secure(jwtCookieProperties.secure())
                // 다른 사이트 요청에서 Cookie 전송 범위를 제한하는 속성 설정
                .sameSite(jwtCookieProperties.sameSite())
                .path(path)
                .maxAge(maxAge)
                .build();
    }
}
