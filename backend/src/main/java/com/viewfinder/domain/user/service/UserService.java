package com.viewfinder.domain.user.service;

import com.viewfinder.domain.user.dto.SignUpRequest;
import com.viewfinder.domain.user.dto.SignUpResponse;
import com.viewfinder.domain.user.dto.LoginRequest;
import com.viewfinder.domain.user.dto.LoginResponse;
import com.viewfinder.domain.user.entity.User;
import com.viewfinder.domain.user.enums.Provider;
import com.viewfinder.domain.user.exception.UserErrorCode;
import com.viewfinder.domain.user.repository.UserRepository;
import com.viewfinder.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// User 도메인의 회원가입·로그인 규칙을 처리하는 Service 지정
@Service
@Transactional(readOnly = true)
// final 의존성을 받는 생성자를 Lombok이 자동 생성
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 로컬 회원가입의 중복 검증·비밀번호 암호화·User 저장 처리
    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        validateDuplicate(request.email(), request.nickname());

        // 평문 비밀번호를 DB 저장 전 BCrypt 해시로 변환
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.createLocal(request.email(), encodedPassword, request.nickname());
        User savedUser = userRepository.save(user);

        return new SignUpResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getNickname()
        );
    }

    // 로컬 계정 이메일·BCrypt 비밀번호를 검증하고 로그인 User 정보 반환
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                // 이메일 존재 여부를 노출하지 않는 공통 로그인 실패 오류 반환
                .orElseThrow(() -> new BusinessException(UserErrorCode.LOGIN_FAILED));

        // 소셜 계정이거나 입력 비밀번호가 저장된 BCrypt 해시와 다르면 동일 오류 반환
        if (user.getProvider() != Provider.LOCAL
                || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(UserErrorCode.LOGIN_FAILED);
        }

        return new LoginResponse(user.getId(), user.getEmail(), user.getNickname());
    }

    // 이메일·닉네임 유니크 제약 위반 전 사용자용 오류 반환
    private void validateDuplicate(String email, String nickname) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(UserErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }
}
