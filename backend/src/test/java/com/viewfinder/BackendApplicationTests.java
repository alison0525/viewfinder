package com.viewfinder;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

// 실제 Spring Boot 애플리케이션 컨텍스트 전체 기동 테스트 지정
@SpringBootTest
// TestcontainersConfiguration의 컨테이너 Bean을 현재 테스트 컨텍스트에 추가
@Import(TestcontainersConfiguration.class)
class BackendApplicationTests {

    // Spring이 Testcontainers PostgreSQL 연결 정보로 생성한 JdbcTemplate 주입
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
        // Spring Bean 생성과 PostgreSQL 연결 실패 여부 확인
    }

    @Test
    void postgisIsAvailable() {
        // Flyway V1 마이그레이션으로 생성한 PostGIS 확장 함수의 실제 실행 여부 확인
        String version = jdbcTemplate.queryForObject("SELECT PostGIS_Version()", String.class);

        assertThat(version).isNotBlank();
    }

    @Test
    void flywayMigratesInitialSchema() {
        Integer migrationCount = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM flyway_schema_history
                WHERE success = TRUE
                  AND version IN ('1', '2')
                """, Integer.class);
        Integer tableCount = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name IN ('users', 'spots', 'posts')
                """, Integer.class);

        assertThat(migrationCount).isEqualTo(2);
        assertThat(tableCount).isEqualTo(3);
    }

}
