package com.viewfinder.global.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 공통 예외 처리의 HTTP 상태와 JSON 응답 형식을 확인하는 MVC 슬라이스 테스트 지정
@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
// Security 정책 구현 전 예외 처리만 검증하도록 보안 필터 제외
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {
        GlobalExceptionHandlerTest.TestController.class,
        GlobalExceptionHandler.class
})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void convertsBusinessExceptionToCommonErrorResponse() throws Exception {
        mockMvc.perform(post("/test/business-error"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_001"))
                .andExpect(jsonPath("$.error.message").value("요청 값이 올바르지 않습니다."));
    }

    @Test
    void convertsValidationExceptionToCommonErrorResponse() throws Exception {
        mockMvc.perform(post("/test/validation-error")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_001"));
    }

    // 예외 처리 테스트 전용 요청 진입점 정의
    @RestController
    @RequestMapping("/test")
    static class TestController {

        // BusinessException 변환 확인용 예외 발생 API 정의
        @PostMapping("/business-error")
        void businessError() {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }

        // @Valid 변환 확인용 요청 DTO와 API 정의
        @PostMapping("/validation-error")
        void validationError(@Valid @RequestBody ValidationRequest request) {
        }
    }

    // record 컴포넌트에 Bean Validation 제약을 선언한 요청 DTO 정의
    record ValidationRequest(
            @NotBlank String name
    ) {
    }
}
