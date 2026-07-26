package com.viewfinder.global.jwt;

import com.viewfinder.global.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

// JWT 생성·서명 검증·User ID 추출을 담당하는 공통 컴포넌트 지정
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final Duration accessTokenExpiration;
    private final Duration refreshTokenExpiration;

    // 환경변수 비밀키와 Token 만료 정책을 JWT 처리용 객체로 변환
    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = jwtProperties.accessTokenExpiration();
        this.refreshTokenExpiration = jwtProperties.refreshTokenExpiration();
    }

    // 짧은 만료 시간을 적용한 Access Token 생성
    public String createAccessToken(Long userId) {
        return createToken(userId, accessTokenExpiration);
    }

    // 긴 만료 시간을 적용한 Refresh Token 생성
    public String createRefreshToken(Long userId) {
        return createToken(userId, refreshTokenExpiration);
    }

    // 서명·만료를 검증한 Token의 subject에서 User ID 추출
    public Long getUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.valueOf(claims.getSubject());
    }

    // 변조·만료·형식 오류가 없는 Token인지 확인
    public boolean isValidToken(String token) {
        try {
            getUserId(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    // subject·발급 시각·만료 시각을 담은 HMAC 서명 JWT 생성
    private String createToken(Long userId, Duration expiration) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expiration.toMillis());

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(secretKey)
                .compact();
    }
}
