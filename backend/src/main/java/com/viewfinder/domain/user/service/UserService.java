package com.viewfinder.domain.user.service;

import com.viewfinder.domain.user.dto.SignUpRequest;
import com.viewfinder.domain.user.dto.SignUpResponse;
import com.viewfinder.domain.user.dto.LoginRequest;
import com.viewfinder.domain.user.dto.LoginResult;
import com.viewfinder.domain.user.dto.LoginResponse;
import com.viewfinder.domain.user.dto.TokenReissueResult;
import com.viewfinder.domain.user.entity.User;
import com.viewfinder.domain.user.enums.Provider;
import com.viewfinder.domain.user.exception.UserErrorCode;
import com.viewfinder.domain.user.repository.UserRepository;
import com.viewfinder.global.exception.BusinessException;
import com.viewfinder.global.config.JwtProperties;
import com.viewfinder.global.jwt.TokenType;
import com.viewfinder.global.jwt.JwtTokenProvider;
import com.viewfinder.global.redis.RefreshTokenStore;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final JwtProperties jwtProperties;

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
    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                // 이메일 존재 여부를 노출하지 않는 공통 로그인 실패 오류 반환
                .orElseThrow(() -> new BusinessException(UserErrorCode.LOGIN_FAILED));

        // 소셜 계정이거나 입력 비밀번호가 저장된 BCrypt 해시와 다르면 동일 오류 반환
        if (user.getProvider() != Provider.LOCAL
                || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(UserErrorCode.LOGIN_FAILED);
        }

        LoginResponse loginResponse = new LoginResponse(user.getId(), user.getEmail(), user.getNickname());
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // 새 로그인으로 발급한 Refresh Token을 사용자별 Redis Key와 14일 TTL로 저장
        refreshTokenStore.save(user.getId(), refreshToken, jwtProperties.refreshTokenExpiration());

        return new LoginResult(loginResponse, accessToken, refreshToken);
    }

    // 현재 Refresh Token과 Redis 저장값이 일치할 때만 서버 측 로그인 상태 삭제
    public void logout(String refreshToken) {
        if (refreshToken == null || !jwtTokenProvider.isValidToken(refreshToken)) {
            return;
        }

        try {
            if (jwtTokenProvider.getTokenType(refreshToken) != TokenType.REFRESH) {
                return;
            }

            Long userId = jwtTokenProvider.getUserId(refreshToken);
            refreshTokenStore.findByUserId(userId)
                    // 사용자 ID만 보고 삭제하면 이전 기기 Token이 Redis의 최신 로그인 Token까지 삭제하므로 현재 저장값 일치 확인
                    .filter(savedRefreshToken -> savedRefreshToken.equals(refreshToken))
                    .ifPresent(savedRefreshToken -> refreshTokenStore.deleteByUserId(userId));
        } catch (IllegalArgumentException exception) {
            // 형식이 불완전한 Token도 로그아웃 요청 자체는 성공 처리
        }
    }

    // 현재 Redis Refresh Token을 검증하고 새 Access·Refresh Token을 발급하는 재발급 처리
    public TokenReissueResult reissueTokens(String refreshToken) {
        if (refreshToken == null || !jwtTokenProvider.isValidToken(refreshToken)) {
            throw new BusinessException(UserErrorCode.TOKEN_REISSUE_FAILED);
        }

        try {
            if (jwtTokenProvider.getTokenType(refreshToken) != TokenType.REFRESH) {
                throw new BusinessException(UserErrorCode.TOKEN_REISSUE_FAILED);
            }

            Long userId = jwtTokenProvider.getUserId(refreshToken);
            refreshTokenStore.findByUserId(userId)
                    // Redis에 없는 만료·로그아웃 Token과 이전 로그인 Token의 재발급 차단
                    .filter(refreshToken::equals)
                    .orElseThrow(() -> new BusinessException(UserErrorCode.TOKEN_REISSUE_FAILED));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(UserErrorCode.TOKEN_REISSUE_FAILED));

            String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());
            String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
            // 기존 Token을 새 Token으로 교체하고 14일 TTL을 새로 시작
            refreshTokenStore.save(user.getId(), newRefreshToken, jwtProperties.refreshTokenExpiration());

            return new TokenReissueResult(accessToken, newRefreshToken);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(UserErrorCode.TOKEN_REISSUE_FAILED);
        }
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
