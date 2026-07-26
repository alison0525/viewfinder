package com.viewfinder.domain.user.service;

import com.viewfinder.TestcontainersConfiguration;
import com.viewfinder.domain.user.dto.SignUpRequest;
import com.viewfinder.domain.user.dto.SignUpResponse;
import com.viewfinder.domain.user.dto.LoginRequest;
import com.viewfinder.domain.user.dto.LoginResponse;
import com.viewfinder.domain.user.entity.User;
import com.viewfinder.domain.user.exception.UserErrorCode;
import com.viewfinder.domain.user.repository.UserRepository;
import com.viewfinder.global.config.JpaAuditingConfig;
import com.viewfinder.global.config.PasswordEncoderConfig;
import com.viewfinder.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 실제 PostgreSQL에서 회원가입 Service 규칙을 확인하는 JPA 통합 테스트 지정
@DataJpaTest
// H2 등 내장 DB 교체를 막고 Testcontainers PostgreSQL DataSource 유지
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// Service·비밀번호 암호화·Auditing·PostgreSQL Testcontainers Bean 추가
@Import({
        UserService.class,
        PasswordEncoderConfig.class,
        JpaAuditingConfig.class,
        TestcontainersConfiguration.class
})
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void signsUpLocalUserWithEncodedPassword() {
        SignUpResponse response = userService.signUp(
                new SignUpRequest("user@example.com", "password1234", "viewfinder")
        );

        User savedUser = userRepository.findById(response.id()).orElseThrow();

        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.nickname()).isEqualTo("viewfinder");
        assertThat(savedUser.getPassword()).isNotEqualTo("password1234");
        assertThat(passwordEncoder.matches("password1234", savedUser.getPassword())).isTrue();
    }

    @Test
    void rejectsDuplicateEmail() {
        userRepository.saveAndFlush(User.createLocal("user@example.com", "encoded-password", "first"));

        assertThatThrownBy(() -> userService.signUp(
                new SignUpRequest("user@example.com", "password1234", "second")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(UserErrorCode.EMAIL_ALREADY_EXISTS);
    }

    @Test
    void rejectsDuplicateNickname() {
        userRepository.saveAndFlush(User.createLocal("first@example.com", "encoded-password", "viewfinder"));

        assertThatThrownBy(() -> userService.signUp(
                new SignUpRequest("second@example.com", "password1234", "viewfinder")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(UserErrorCode.NICKNAME_ALREADY_EXISTS);
    }

    @Test
    void logsInWithMatchingLocalAccountPassword() {
        String encodedPassword = passwordEncoder.encode("password1234");
        userRepository.saveAndFlush(User.createLocal("user@example.com", encodedPassword, "viewfinder"));

        LoginResponse response = userService.login(
                new LoginRequest("user@example.com", "password1234")
        );

        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.nickname()).isEqualTo("viewfinder");
    }

    @Test
    void rejectsUnknownEmailWithGenericLoginError() {
        assertThatThrownBy(() -> userService.login(
                new LoginRequest("unknown@example.com", "password1234")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(UserErrorCode.LOGIN_FAILED);
    }

    @Test
    void rejectsWrongPasswordWithGenericLoginError() {
        String encodedPassword = passwordEncoder.encode("password1234");
        userRepository.saveAndFlush(User.createLocal("user@example.com", encodedPassword, "viewfinder"));

        assertThatThrownBy(() -> userService.login(
                new LoginRequest("user@example.com", "wrong-password")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(UserErrorCode.LOGIN_FAILED);
    }
}
