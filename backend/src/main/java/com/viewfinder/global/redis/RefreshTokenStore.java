package com.viewfinder.global.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Optional;

// Redis에서 사용자별 Refresh Token을 저장·조회·삭제하는 컴포넌트 지정
@Component
// final Redis 의존성을 받는 생성자를 Lombok이 자동 생성
@RequiredArgsConstructor
public class RefreshTokenStore {

    private static final String KEY_PREFIX = "auth:refresh:";

    private final StringRedisTemplate stringRedisTemplate;

    // 사용자별 Refresh Token을 만료 시간과 함께 Redis에 저장
    public void save(Long userId, String refreshToken, Duration expiration) {
        stringRedisTemplate.opsForValue().set(key(userId), refreshToken, expiration);
    }

    // 사용자 ID로 현재 Redis Refresh Token을 조회하고 없으면 빈 Optional 반환
    public Optional<String> findByUserId(Long userId) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(key(userId)));
    }

    // 로그아웃·재발급 시 기존 사용자 Refresh Token을 Redis에서 삭제
    public void deleteByUserId(Long userId) {
        stringRedisTemplate.delete(key(userId));
    }

    // Redis Key 충돌을 막기 위한 인증 도메인 접두사와 사용자 ID 조합 생성
    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }
}
