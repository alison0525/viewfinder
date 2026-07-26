package com.viewfinder.domain.user.repository;

import com.viewfinder.TestcontainersConfiguration;
import com.viewfinder.domain.user.entity.User;
import com.viewfinder.domain.user.enums.Provider;
import com.viewfinder.domain.user.enums.Role;
import com.viewfinder.global.config.JpaAuditingConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

// Web 서버 없이 JPA Repository·EntityManager만 기동하는 저장소 슬라이스 테스트 지정
@DataJpaTest
// H2 등 내장 DB 교체를 막고 Testcontainers PostgreSQL DataSource 유지
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// PostgreSQL Testcontainers Bean을 이 테스트 컨텍스트에 추가
@Import({TestcontainersConfiguration.class, JpaAuditingConfig.class})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndFindsUserByProvider() {
        User user = User.create(
                Provider.KAKAO,
                "kakao-user-1",
                "user@example.com",
                null,
                "viewfinder"
        );

        // INSERT를 즉시 실행해 DB 제약·매핑 오류를 여기서 확인
        userRepository.saveAndFlush(user);
        // 영속성 컨텍스트를 비워 다음 조회가 메모리가 아닌 실제 DB에서 일어나도록 처리
        entityManager.clear();

        User foundUser = userRepository.findByProviderAndProviderId(Provider.KAKAO, "kakao-user-1")
                .orElseThrow();

        assertThat(foundUser.getId()).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("user@example.com");
        assertThat(foundUser.getNickname()).isEqualTo("viewfinder");
        assertThat(foundUser.getRole()).isEqualTo(Role.USER);
        assertThat(foundUser.getCreatedAt()).isNotNull();
        assertThat(foundUser.getUpdatedAt()).isNotNull();
    }
}
