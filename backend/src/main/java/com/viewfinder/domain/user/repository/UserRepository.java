package com.viewfinder.domain.user.repository;

import com.viewfinder.domain.user.entity.User;
import com.viewfinder.domain.user.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 메서드 이름을 해석해 provider와 providerId 조건의 SELECT 쿼리를 자동 생성
    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    // 메서드 이름을 해석해 email 조건의 SELECT 쿼리를 자동 생성
    Optional<User> findByEmail(String email);
}
