package com.viewfinder.global.jwt;

import com.viewfinder.global.config.JwtProperties;
import com.viewfinder.domain.user.enums.Role;
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

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ROLE_CLAIM = "role";

    private final SecretKey secretKey;
    private final Duration accessTokenExpiration;
    private final Duration refreshTokenExpiration;

    // 환경변수 비밀키와 Token 만료 정책을 JWT 처리용 객체로 변환
    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = jwtProperties.accessTokenExpiration();
        this.refreshTokenExpiration = jwtProperties.refreshTokenExpiration();
    }

    // 짧은 만료 시간과 API 권한 확인용 Role을 적용한 Access Token 생성
    public String createAccessToken(Long userId, Role role) {
        return createToken(userId, TokenType.ACCESS, role, accessTokenExpiration);
    }

    // 긴 만료 시간을 적용하고 권한은 담지 않는 Refresh Token 생성
    public String createRefreshToken(Long userId) {
        return createToken(userId, TokenType.REFRESH, null, refreshTokenExpiration);
    }

    // 서명·만료를 검증한 Token의 subject에서 User ID 추출
    public Long getUserId(String token) {
        Claims claims = getClaims(token);

        return Long.valueOf(claims.getSubject());
    }

    // 서명·만료를 검증한 Token의 사용 목적 추출
    public TokenType getTokenType(String token) {
        return TokenType.valueOf(getClaims(token).get(TOKEN_TYPE_CLAIM, String.class));
    }

    // Access Token에 담긴 API 권한 Role 추출
    public Role getRole(String token) {
        return Role.valueOf(getClaims(token).get(ROLE_CLAIM, String.class));
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

    // subject·Token 종류·선택 Role·발급 시각·만료 시각을 담은 HMAC 서명 JWT 생성
    private String createToken(Long userId, TokenType tokenType, Role role, Duration expiration) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expiration.toMillis());

        var tokenBuilder = Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiresAt)
                .claim(TOKEN_TYPE_CLAIM, tokenType.name());

        // Access Token에만 현재 API 권한을 Claim으로 추가
        if (role != null) {
            tokenBuilder.claim(ROLE_CLAIM, role.name());
        }

        return tokenBuilder
                .signWith(secretKey)
                .compact();
    }

    // JJWT Parser가 서명·형식·만료를 검증한 Claims 반환
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
