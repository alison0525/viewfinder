package com.viewfinder.domain.user.controller;

import com.viewfinder.domain.user.dto.SignUpRequest;
import com.viewfinder.domain.user.dto.SignUpResponse;
import com.viewfinder.domain.user.dto.LoginRequest;
import com.viewfinder.domain.user.dto.LoginResponse;
import com.viewfinder.domain.user.service.UserService;
import com.viewfinder.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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
        LoginResponse response = userService.login(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
