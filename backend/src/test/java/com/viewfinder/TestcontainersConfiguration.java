package com.viewfinder;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

// @SpringBootTest에서만 불러오는 테스트 전용 Spring 설정 클래스 지정
// 현재는 Bean 메서드끼리 호출하지 않으므로 프록시가 필요 없어 테스트 시작 비용 절감
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    // 반환한 PostgreSQLContainer 객체를 Spring Bean으로 등록
    @Bean
    // 컨테이너의 URL·임시 포트·DB 이름·계정을 Spring DataSource에 자동 연결하도록 Service Connection 지정
    @ServiceConnection
    PostgreSQLContainer postgisContainer() {
        return new PostgreSQLContainer(
                // 로컬 개발·CI 테스트에서 공통으로 사용하는 공식 PostGIS 이미지 지정
                DockerImageName.parse("postgis/postgis:16-3.4")
                        .asCompatibleSubstituteFor("postgres")
        );
    }
}
