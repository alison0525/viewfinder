package com.viewfinder.global.config;

import com.viewfinder.domain.user.controller.UserController;
import com.viewfinder.domain.user.dto.SignUpResponse;
import com.viewfinder.domain.user.enums.Role;
import com.viewfinder.domain.user.service.UserService;
import com.viewfinder.global.jwt.JwtAuthenticationFilter;
import com.viewfinder.global.jwt.JwtCookieProvider;
import com.viewfinder.global.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 인증 경로 허용과 보호 경로 차단을 확인하는 MVC Security 테스트 지정
@WebMvcTest(controllers = {UserController.class, SecurityConfigTest.ProtectedTestController.class})
@Import({
        SecurityConfig.class,
        JwtConfig.class,
        JwtTokenProvider.class,
        JwtAuthenticationFilter.class,
        SecurityConfigTest.ProtectedTestController.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // 회원가입 Controller가 호출할 Service Bean을 테스트 대역으로 교체
    @MockitoBean
    private UserService userService;

    // UserController 생성에 필요한 Cookie Provider Bean을 테스트 대역으로 교체
    @MockitoBean
    private JwtCookieProvider jwtCookieProvider;

    @Test
    void permitsAuthenticationStartEndpointWithoutLogin() throws Exception {
        given(userService.signUp(any()))
                .willReturn(new SignUpResponse(1L, "user@example.com", "viewfinder"));

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password1234",
                                  "nickname": "viewfinder"
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password1234",
                                  "nickname": "viewfinder"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void issuesCsrfTokenCookieForSpa() throws Exception {
        mockMvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists("XSRF-TOKEN"));
    }

    @Test
    void allowsCorsPreflightFromConfiguredFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type, X-XSRF-TOKEN"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void rejectsCorsPreflightFromUnconfiguredOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header("Origin", "https://untrusted.example.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsProtectedEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/protected"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatesProtectedEndpointWithAccessTokenCookie() throws Exception {
        String accessToken = jwtTokenProvider.createAccessToken(1L, Role.USER);

        mockMvc.perform(get("/api/v1/protected")
                        .cookie(new Cookie(JwtCookieProvider.ACCESS_TOKEN_COOKIE_NAME, accessToken)))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string("1"));
    }

    @Test
    void rejectsRefreshTokenUsedAsAccessToken() throws Exception {
        String refreshToken = jwtTokenProvider.createRefreshToken(1L);

        mockMvc.perform(get("/api/v1/protected")
                        .cookie(new Cookie(JwtCookieProvider.ACCESS_TOKEN_COOKIE_NAME, refreshToken)))
                .andExpect(status().isUnauthorized());
    }

    // JWT 필터가 등록한 현재 사용자 ID를 확인하는 테스트 전용 보호 API 정의
    @RestController
    static class ProtectedTestController {

        @GetMapping("/api/v1/protected")
        String protectedEndpoint(Authentication authentication) {
            return authentication.getName();
        }
    }
}
