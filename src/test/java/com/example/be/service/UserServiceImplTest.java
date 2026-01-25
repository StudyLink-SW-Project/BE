package com.example.be.service;

import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.apiPayload.exception.handler.UserHandler;
import com.example.be.domain.User;
import com.example.be.domain.redis.RedisRefreshToken;
import com.example.be.repository.UserRepository;
import com.example.be.repository.redis.RedisRefreshTokenRepository;
import com.example.be.web.dto.CommonDTO;
import com.example.be.web.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl 단위 테스트
 *
 * 단위 테스트란?
 * - Service 로직만 테스트 (DB 연결 X)
 * - Mock 객체로 의존성 대체
 * - 빠르고 독립적으로 실행 가능
 */
@ExtendWith(MockitoExtension.class)  // Mockito 사용 설정
class UserServiceImplTest {

    @InjectMocks  // 테스트 대상 클래스
    private UserServiceImpl userService;

    @Mock  // 가짜 객체 (실제 DB 연결 X)
    private UserRepository userRepository;

    @Mock
    private JwtUtilServiceImpl jwtUtilService;

    @Mock
    private RedisRefreshTokenRepository redisRefreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    // 테스트에서 공통으로 사용할 데이터
    private User testUser;
    private UUID testUserId;

    @BeforeEach  // 각 테스트 전에 실행
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(1L)
                .userId(testUserId)
                .email("test@example.com")
                .password("encodedPassword")
                .name("테스트유저")
                .provider("general")
                .createDate(LocalDateTime.now())
                .build();
    }

    // ==================== 회원가입 테스트 ====================
    @Nested  // 관련 테스트를 그룹화
    @DisplayName("회원가입 테스트")
    class SignUpTest {

        @Test
        @DisplayName("회원가입 성공")
        void signUp_Success() {
            // Given (준비): 테스트 데이터와 Mock 동작 설정
            UserDTO.SingUpRequestDto request = UserDTO.SingUpRequestDto.builder()
                    .email("newuser@example.com")
                    .password("password123")
                    .name("새로운유저")
                    .build();

            // Mock 동작 정의: 이메일 중복 체크 → false (중복 아님)
            given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
            // Mock 동작 정의: 비밀번호 인코딩
            given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");
            // Mock 동작 정의: 저장 시 유저 반환
            given(userRepository.save(any(User.class))).willReturn(testUser);

            // When (실행): 실제 테스트할 메서드 호출
            CommonDTO.IsSuccessDTO result = userService.signUp(request);

            // Then (검증): 결과 확인
            assertThat(result.getIsSuccess()).isTrue();

            // save 메서드가 1번 호출되었는지 검증
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("회원가입 실패 - 이메일 중복")
        void signUp_Fail_DuplicateEmail() {
            // Given
            UserDTO.SingUpRequestDto request = UserDTO.SingUpRequestDto.builder()
                    .email("existing@example.com")
                    .password("password123")
                    .name("중복유저")
                    .build();

            // 이메일이 이미 존재하는 상황 설정
            given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

            // When & Then: 예외가 발생하는지 검증
            assertThatThrownBy(() -> userService.signUp(request))
                    .isInstanceOf(UserHandler.class);

            // save가 호출되지 않아야 함
            verify(userRepository, never()).save(any(User.class));
        }
    }

    // ==================== 로그인 테스트 ====================
    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("로그인 성공")
        void login_Success() {
            // Given
            UserDTO.LoginRequestDto loginRequest = UserDTO.LoginRequestDto.builder()
                    .email("test@example.com")
                    .password("password123")
                    .build();

            given(userRepository.findByEmail(loginRequest.getEmail()))
                    .willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword()))
                    .willReturn(true);  // 비밀번호 일치
            given(jwtUtilService.generateAccessToken(any(UUID.class), anyLong()))
                    .willReturn("accessToken123");
            given(jwtUtilService.generateRefreshToken(any(UUID.class), anyLong()))
                    .willReturn("refreshToken123");

            // When
            UserDTO.UserResponseDto result = userService.login(loginRequest, response, request);

            // Then
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            assertThat(result.getUserName()).isEqualTo("테스트유저");

            // 토큰이 쿠키에 설정되었는지 검증
            verify(response, times(2)).addHeader(eq("Set-Cookie"), anyString());
            // 리프레시 토큰이 Redis에 저장되었는지 검증
            verify(redisRefreshTokenRepository).save(any(RedisRefreshToken.class));
        }

        @Test
        @DisplayName("로그인 실패 - 존재하지 않는 사용자")
        void login_Fail_UserNotFound() {
            // Given
            UserDTO.LoginRequestDto loginRequest = UserDTO.LoginRequestDto.builder()
                    .email("notexist@example.com")
                    .password("password123")
                    .build();

            given(userRepository.findByEmail(loginRequest.getEmail()))
                    .willReturn(Optional.empty());  // 사용자 없음

            // When & Then
            assertThatThrownBy(() -> userService.login(loginRequest, response, request))
                    .isInstanceOf(UserHandler.class);
        }

        @Test
        @DisplayName("로그인 실패 - 비밀번호 불일치")
        void login_Fail_WrongPassword() {
            // Given
            UserDTO.LoginRequestDto loginRequest = UserDTO.LoginRequestDto.builder()
                    .email("test@example.com")
                    .password("wrongPassword")
                    .build();

            given(userRepository.findByEmail(loginRequest.getEmail()))
                    .willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword()))
                    .willReturn(false);  // 비밀번호 불일치

            // When & Then
            assertThatThrownBy(() -> userService.login(loginRequest, response, request))
                    .isInstanceOf(UserHandler.class);
        }
    }

    // ==================== 사용자 정보 조회 테스트 ====================
    @Nested
    @DisplayName("사용자 정보 조회 테스트")
    class GetUserInfoTest {

        @Test
        @DisplayName("사용자 정보 조회 성공")
        void getUserInfo_Success() {
            // Given
            String accessToken = "validAccessToken";

            given(jwtUtilService.getUserIdFromToken(accessToken))
                    .willReturn(testUserId.toString());
            given(userRepository.findByUserId(testUserId))
                    .willReturn(Optional.of(testUser));

            // When
            UserDTO.UserResponseDto result = userService.getUserInfo(accessToken);

            // Then
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            assertThat(result.getUserName()).isEqualTo("테스트유저");
            assertThat(result.getLoginType()).isEqualTo("general");
        }

        @Test
        @DisplayName("사용자 정보 조회 실패 - 토큰 없음")
        void getUserInfo_Fail_NoToken() {
            // Given
            String accessToken = null;

            // When & Then
            assertThatThrownBy(() -> userService.getUserInfo(accessToken))
                    .isInstanceOf(UserHandler.class);
        }
    }
}
