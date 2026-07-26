package com.viewfinder.global.config;

import com.viewfinder.domain.user.controller.UserController;
import com.viewfinder.domain.user.dto.SignUpResponse;
import com.viewfinder.domain.user.service.UserService;
import com.viewfinder.global.jwt.JwtCookieProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 인증 경로 허용과 보호 경로 차단을 확인하는 MVC Security 테스트 지정
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

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
                .andExpect(status().isCreated());
    }

    @Test
    void rejectsProtectedEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/protected"))
                .andExpect(status().isUnauthorized());
    }
}
