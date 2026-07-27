package com.viewfinder.domain.user.controller;

import com.viewfinder.domain.user.dto.SignUpResponse;
import com.viewfinder.domain.user.dto.LoginResponse;
import com.viewfinder.domain.user.dto.LoginResult;
import com.viewfinder.domain.user.service.UserService;
import com.viewfinder.global.jwt.JwtCookieProvider;
import com.viewfinder.global.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 회원가입 HTTP 요청·응답 형식과 요청 DTO 검증을 확인하는 MVC 슬라이스 테스트 지정
@WebMvcTest(UserController.class)
// Security 정책 구현 전 Controller 동작만 검증하도록 보안 필터 제외
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Controller가 호출할 Service Bean을 테스트 대역으로 교체
    @MockitoBean
    private UserService userService;

    // Controller가 발급할 HttpOnly Cookie 생성 Bean을 테스트 대역으로 교체
    @MockitoBean
    private JwtCookieProvider jwtCookieProvider;

    // SecurityConfig 생성에 필요한 JWT 필터를 MVC Controller 테스트 대역으로 교체
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void signsUpUserAndReturnsCreatedResponse() throws Exception {
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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("viewfinder"));
    }

    @Test
    void rejectsInvalidSignUpRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-email",
                                  "password": "short",
                                  "nickname": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_001"));
    }

    @Test
    void logsInUserAndReturnsSuccessResponse() throws Exception {
        given(userService.login(any()))
                .willReturn(new LoginResult(
                        new LoginResponse(1L, "user@example.com", "viewfinder"),
                        "access-token",
                        "refresh-token"
                ));
        given(jwtCookieProvider.createAccessTokenCookie("access-token"))
                .willReturn(ResponseCookie.from("access_token", "access-token").httpOnly(true).build());
        given(jwtCookieProvider.createRefreshTokenCookie("refresh-token"))
                .willReturn(ResponseCookie.from("refresh_token", "refresh-token").httpOnly(true).build());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("access_token")))
                .andExpect(result -> assertThat(result.getResponse().getHeaders(HttpHeaders.SET_COOKIE))
                        .anyMatch(cookie -> cookie.contains("refresh_token")));
    }

    @Test
    void rejectsInvalidLoginRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-email",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_001"));
    }

    @Test
    void expiresTokenCookiesOnLogout() throws Exception {
        given(jwtCookieProvider.expireAccessTokenCookie())
                .willReturn(ResponseCookie.from("access_token", "").maxAge(0).path("/").build());
        given(jwtCookieProvider.expireRefreshTokenCookie())
                .willReturn(ResponseCookie.from("refresh_token", "").maxAge(0).path("/api/v1/auth").build());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(new Cookie("refresh_token", "refresh-token")))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("access_token=")))
                .andExpect(result -> assertThat(result.getResponse().getHeaders(HttpHeaders.SET_COOKIE))
                        .anyMatch(cookie -> cookie.contains("refresh_token=")))
                .andExpect(result -> assertThat(result.getResponse().getHeaders(HttpHeaders.SET_COOKIE))
                        .allMatch(cookie -> cookie.contains("Max-Age=0")));

        verify(userService).logout("refresh-token");
    }
}
