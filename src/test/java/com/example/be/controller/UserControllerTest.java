package com.example.be.controller;

import com.example.be.apiPayload.exception.handler.UserHandler;
import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.config.JwtAuthenticationFilter;
import com.example.be.service.JwtUtilServiceImpl;
import com.example.be.service.UserServiceImpl;
import com.example.be.web.controller.UserController;
import com.example.be.web.dto.CommonDTO;
import com.example.be.web.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserController 슬라이스 테스트 (Controller 계층만 테스트)
 */
@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)  // Security Filter 비활성화
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceImpl userService;

    @MockBean
    private JwtUtilServiceImpl jwtUtilService;

    // ==================== 회원가입 API 테스트 ====================
    @Nested
    @DisplayName("POST /user/signup - 회원가입 API")
    class SignUpApiTest {

        @Test
        @DisplayName("회원가입 성공 - 200 OK")
        void signUp_Success() throws Exception {
            // Given
            UserDTO.SingUpRequestDto request = UserDTO.SingUpRequestDto.builder()
                    .email("newuser@example.com")
                    .password("password123")
                    .name("새로운유저")
                    .build();

            given(userService.signUp(any(UserDTO.SingUpRequestDto.class)))
                    .willReturn(CommonDTO.IsSuccessDTO.builder().isSuccess(true).build());

            // When & Then
            mockMvc.perform(post("/user/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.isSuccess").value(true));
        }

        @Test
        @DisplayName("회원가입 실패 - 이메일 중복 시 에러 응답")
        void signUp_Fail_DuplicateEmail() throws Exception {
            // Given
            UserDTO.SingUpRequestDto request = UserDTO.SingUpRequestDto.builder()
                    .email("existing@example.com")
                    .password("password123")
                    .name("중복유저")
                    .build();

            given(userService.signUp(any(UserDTO.SingUpRequestDto.class)))
                    .willThrow(new UserHandler(ErrorStatus._EXIST_EMAIL));

            // When & Then
            mockMvc.perform(post("/user/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false));
        }
    }

    // ==================== 로그인 API 테스트 ====================
    @Nested
    @DisplayName("POST /user/login - 로그인 API")
    class LoginApiTest {

        @Test
        @DisplayName("로그인 성공 - 사용자 정보 반환")
        void login_Success() throws Exception {
            // Given
            UserDTO.LoginRequestDto request = UserDTO.LoginRequestDto.builder()
                    .email("test@example.com")
                    .password("password123")
                    .build();

            UserDTO.UserResponseDto responseDto = UserDTO.UserResponseDto.builder()
                    .userId(1L)
                    .userName("테스트유저")
                    .email("test@example.com")
                    .loginType("general")
                    .build();

            given(userService.login(any(), any(), any())).willReturn(responseDto);

            // When & Then
            mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.userId").value(1))
                    .andExpect(jsonPath("$.result.userName").value("테스트유저"))
                    .andExpect(jsonPath("$.result.email").value("test@example.com"))
                    .andExpect(jsonPath("$.result.loginType").value("general"));
        }

        @Test
        @DisplayName("로그인 실패 - 사용자 없음")
        void login_Fail_UserNotFound() throws Exception {
            // Given
            UserDTO.LoginRequestDto request = UserDTO.LoginRequestDto.builder()
                    .email("notexist@example.com")
                    .password("password123")
                    .build();

            given(userService.login(any(), any(), any()))
                    .willThrow(new UserHandler(ErrorStatus._NOT_FOUND_USER));

            // When & Then
            mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.isSuccess").value(false));
        }
    }

    // ==================== 사용자 정보 조회 API 테스트 ====================
    @Nested
    @DisplayName("GET /user/info - 사용자 정보 조회 API")
    class GetUserInfoApiTest {

        @Test
        @DisplayName("사용자 정보 조회 성공")
        void getUserInfo_Success() throws Exception {
            // Given
            UserDTO.UserResponseDto responseDto = UserDTO.UserResponseDto.builder()
                    .userId(1L)
                    .userName("테스트유저")
                    .email("test@example.com")
                    .loginType("general")
                    .build();

            given(jwtUtilService.extractTokenFromCookie(any(), any())).willReturn("validToken");
            given(userService.getUserInfo(any())).willReturn(responseDto);

            // When & Then
            mockMvc.perform(get("/user/info")
                            .cookie(new jakarta.servlet.http.Cookie("accessToken", "validToken")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.userName").value("테스트유저"));
        }
    }

    // ==================== 로그아웃 API 테스트 ====================
    @Nested
    @DisplayName("POST /user/logout - 로그아웃 API")
    class LogoutApiTest {

        @Test
        @DisplayName("로그아웃 성공")
        void logout_Success() throws Exception {
            // Given
            given(userService.logout(any(), any()))
                    .willReturn(CommonDTO.IsSuccessDTO.builder().isSuccess(true).build());

            // When & Then
            mockMvc.perform(post("/user/logout")
                            .cookie(new jakarta.servlet.http.Cookie("accessToken", "token")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.isSuccess").value(true));
        }
    }
}
