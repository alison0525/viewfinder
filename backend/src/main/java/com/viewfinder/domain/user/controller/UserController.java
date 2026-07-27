package com.viewfinder.domain.user.controller;

import com.viewfinder.domain.user.dto.SignUpRequest;
import com.viewfinder.domain.user.dto.SignUpResponse;
import com.viewfinder.domain.user.dto.LoginRequest;
import com.viewfinder.domain.user.dto.LoginResult;
import com.viewfinder.domain.user.dto.LoginResponse;
import com.viewfinder.domain.user.dto.TokenReissueResult;
import com.viewfinder.domain.user.service.UserService;
import com.viewfinder.global.jwt.JwtCookieProvider;
import com.viewfinder.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 회원가입 등 User 인증 진입 API를 제공하는 Controller 지정
@RestController
@RequestMapping("/api/v1/auth")
// final Service 의존성을 받는 생성자를 Lombok이 자동 생성
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtCookieProvider jwtCookieProvider;

    // 로컬 회원가입 요청을 처리하고 생성된 User 정보를 201 응답으로 반환
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(
            @Valid @RequestBody SignUpRequest request
    ) {
        SignUpResponse response = userService.signUp(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // 로컬 로그인 요청을 처리하고 확인된 User 정보를 성공 응답으로 반환
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResult loginResult = userService.login(request);

        return ResponseEntity.ok()
                // Token 값은 JSON 본문이 아닌 HttpOnly Cookie 헤더로만 전달
                .header(HttpHeaders.SET_COOKIE, jwtCookieProvider
                        .createAccessTokenCookie(loginResult.accessToken())
                        .toString())
                .header(HttpHeaders.SET_COOKIE, jwtCookieProvider
                        .createRefreshTokenCookie(loginResult.refreshToken())
                        .toString())
                .body(ApiResponse.success(loginResult.loginResponse()));
    }

    // Refresh Token Redis 저장값과 브라우저 Cookie를 함께 삭제해 현재 로그인 상태 종료
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = JwtCookieProvider.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken
    ) {
        userService.logout(refreshToken);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, jwtCookieProvider.expireAccessTokenCookie().toString())
                .header(HttpHeaders.SET_COOKIE, jwtCookieProvider.expireRefreshTokenCookie().toString())
                .build();
    }

    // Refresh Cookie 검증 뒤 새 Access·Refresh Cookie를 발급하는 Token 재발급 API 제공
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(value = JwtCookieProvider.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken
    ) {
        TokenReissueResult reissueResult = userService.reissueTokens(refreshToken);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, jwtCookieProvider
                        .createAccessTokenCookie(reissueResult.accessToken())
                        .toString())
                .header(HttpHeaders.SET_COOKIE, jwtCookieProvider
                        .createRefreshTokenCookie(reissueResult.refreshToken())
                        .toString())
                .build();
    }
}
