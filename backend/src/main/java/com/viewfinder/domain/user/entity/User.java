package com.viewfinder.domain.user.entity;

import com.viewfinder.domain.user.enums.Provider;
import com.viewfinder.domain.user.enums.Role;
import com.viewfinder.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;

// users 테이블의 한 행과 연결되는 JPA Entity 지정
@Entity
@Table(name = "users")
// Setter 없이 User 필드의 조회 메서드만 Lombok이 자동 생성
@Getter
public class User extends BaseEntity {

    @Column(length = 255)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(name = "profile_image_url", length = 2048)
    private String profileImageUrl;

    // enum 이름(USER, ADMIN)을 문자열로 저장해 DB 값과 Java enum 일치
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // OAuth 제공자 이름을 문자열로 저장해 DB 값과 Java enum 일치
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @Column(name = "provider_id", nullable = false, length = 255)
    private String providerId;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected User() {
    }

    private User(Provider provider, String providerId, String email, String password, String nickname) {
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = Role.USER;
    }

    public static User create(Provider provider, String providerId, String email, String password, String nickname) {
        return new User(provider, providerId, email, password, nickname);
    }

}
