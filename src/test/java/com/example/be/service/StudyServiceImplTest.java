package com.example.be.service;

import com.example.be.domain.User;
import com.example.be.repository.UserRepository;
import com.example.be.web.dto.CommonDTO;
import com.example.be.web.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyServiceImplTest {

    @InjectMocks
    private StudyServiceImpl studyService;

    @Mock
    private JwtUtilServiceImpl jwtUtilService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(1L)
                .userId(testUserId)
                .email("test@example.com")
                .name("테스트유저")
                .todayStudyTime(60)   // 1시간
                .totalStudyTime(300)  // 5시간
                .goalStudyTime(480)   // 8시간
                .build();
    }

    private void mockUserAuthentication() {
        given(jwtUtilService.extractTokenFromCookie(any(), anyString())).willReturn("validToken");
        given(jwtUtilService.getUserIdFromToken("validToken")).willReturn(testUserId.toString());
        given(userRepository.findByUserId(testUserId)).willReturn(Optional.of(testUser));
    }

    // ==================== 공부 시간 추가 테스트 ====================
    @Nested
    @DisplayName("공부 시간 추가 테스트")
    class AddStudyTimeTest {

        @Test
        @DisplayName("공부 시간 추가 성공")
        void addStudyTime_Success() {
            // Given
            mockUserAuthentication();
            int additionalTime = 30;  // 30분 추가

            // When
            CommonDTO.IsSuccessDTO result = studyService.addStudyTime(request, additionalTime);

            // Then
            assertThat(result.getIsSuccess()).isTrue();
            assertThat(testUser.getTodayStudyTime()).isEqualTo(90);   // 60 + 30
            assertThat(testUser.getTotalStudyTime()).isEqualTo(330);  // 300 + 30
            verify(userRepository, times(1)).save(testUser);
        }

        @Test
        @DisplayName("0분 추가 시에도 성공")
        void addStudyTime_ZeroMinutes() {
            // Given
            mockUserAuthentication();

            // When
            CommonDTO.IsSuccessDTO result = studyService.addStudyTime(request, 0);

            // Then
            assertThat(result.getIsSuccess()).isTrue();
            assertThat(testUser.getTodayStudyTime()).isEqualTo(60);  // 변화 없음
        }
    }

    // ==================== 목표 시간 설정 테스트 ====================
    @Nested
    @DisplayName("목표 시간 설정 테스트")
    class AddStudyGoalTimeTest {

        @Test
        @DisplayName("목표 시간 설정 성공")
        void addStudyGoalTime_Success() {
            // Given
            mockUserAuthentication();
            int newGoalTime = 600;  // 10시간

            // When
            CommonDTO.IsSuccessDTO result = studyService.addStudyGoalTime(request, newGoalTime);

            // Then
            assertThat(result.getIsSuccess()).isTrue();
            assertThat(testUser.getGoalStudyTime()).isEqualTo(600);
            verify(userRepository, times(1)).save(testUser);
        }
    }

    // ==================== 공부 시간 조회 테스트 ====================
    @Nested
    @DisplayName("공부 시간 조회 테스트")
    class GetStudyTimeTest {

        @Test
        @DisplayName("공부 시간 조회 성공")
        void getStudyTime_Success() {
            // Given
            mockUserAuthentication();

            // When
            UserDTO.studyTimeResponseDto result = studyService.getStudyTime(request);

            // Then
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getTodayStudyTime()).isEqualTo("1시간 0분");   // 60분
            assertThat(result.getTotalStudyTime()).isEqualTo("5시간 0분");   // 300분
            assertThat(result.getGoalStudyTime()).isEqualTo("8시간 0분");    // 480분
        }

        @Test
        @DisplayName("분 단위 포함된 공부 시간 조회")
        void getStudyTime_WithMinutes() {
            // Given
            mockUserAuthentication();
            testUser.setTodayStudyTime(90);   // 1시간 30분
            testUser.setTotalStudyTime(150);  // 2시간 30분

            // When
            UserDTO.studyTimeResponseDto result = studyService.getStudyTime(request);

            // Then
            assertThat(result.getTodayStudyTime()).isEqualTo("1시간 30분");
            assertThat(result.getTotalStudyTime()).isEqualTo("2시간 30분");
        }

        @Test
        @DisplayName("0분인 경우 조회")
        void getStudyTime_ZeroTime() {
            // Given
            mockUserAuthentication();
            testUser.setTodayStudyTime(0);
            testUser.setTotalStudyTime(0);
            testUser.setGoalStudyTime(0);

            // When
            UserDTO.studyTimeResponseDto result = studyService.getStudyTime(request);

            // Then
            assertThat(result.getTodayStudyTime()).isEqualTo("0시간 0분");
            assertThat(result.getTotalStudyTime()).isEqualTo("0시간 0분");
            assertThat(result.getGoalStudyTime()).isEqualTo("0시간 0분");
        }
    }
}
