package com.viewfinder.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

// 상속받는 Entity의 공통 컬럼을 정의하고 자체 테이블은 만들지 않는 JPA 부모 클래스 지정
@MappedSuperclass
// 생성·수정 시점에 AuditingEntityListener가 날짜 컬럼을 자동 설정하도록 연결
@EntityListeners(AuditingEntityListener.class)
// Setter 없이 공통 필드의 조회 메서드만 Lombok이 자동 생성
@Getter
public abstract class BaseEntity {

    // PostgreSQL IDENTITY 기본키 값을 DB가 생성하도록 지정
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // INSERT 시점에 Spring Data JPA Auditing이 현재 시각을 자동 설정
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // INSERT·UPDATE 시점에 Spring Data JPA Auditing이 현재 시각을 자동 설정
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
