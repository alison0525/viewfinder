package com.viewfinder.global.jwt;

import com.viewfinder.domain.user.enums.Role;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// access_token Cookie를 Spring Security 인증 정보로 변환하는 요청당 한 번 실행 필터 지정
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    // JWT 생성·검증 도구 주입
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String accessToken = extractAccessToken(request);

        // 유효한 ACCESS Token이 있을 때만 SecurityContext에 현재 사용자 인증 정보 등록
        if (accessToken != null && jwtTokenProvider.isValidToken(accessToken)) {
            try {
                if (jwtTokenProvider.getTokenType(accessToken) == TokenType.ACCESS) {
                    Long userId = jwtTokenProvider.getUserId(accessToken);
                    Role role = jwtTokenProvider.getRole(accessToken);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                            );
                    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                    securityContext.setAuthentication(authentication);
                    SecurityContextHolder.setContext(securityContext);
                }
            } catch (JwtException | IllegalArgumentException exception) {
                // 만료·변조·필수 Claim 누락 Token은 인증하지 않고 다음 보안 규칙에서 401 처리
            }
        }

        filterChain.doFilter(request, response);
    }

    // 요청 Cookie 중 access_token 값만 추출
    private String extractAccessToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (JwtCookieProvider.ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
