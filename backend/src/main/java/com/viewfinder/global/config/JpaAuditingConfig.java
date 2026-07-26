package com.viewfinder.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// @CreatedDate와 @LastModifiedDate 자동 설정 기능 활성화
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
