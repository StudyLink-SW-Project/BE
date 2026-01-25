package com.example.be.service;

import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.apiPayload.exception.handler.DDayHandler;
import com.example.be.domain.DDay;
import com.example.be.domain.User;
import com.example.be.repository.DDayRepository;
import com.example.be.repository.UserRepository;
import com.example.be.web.dto.CommonDTO;
import com.example.be.web.dto.DDayDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DDayServiceImplTest {

    @InjectMocks
    private DDayServiceImpl dDayService;

    @Mock
    private JwtUtilServiceImpl jwtUtilService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DDayRepository dayRepository;

    @Mock
    private HttpServletRequest request;

    private User testUser;
    private DDay testDDay;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(1L)
                .userId(testUserId)
                .email("test@example.com")
                .name("테스트유저")
                .build();

        testDDay = DDay.builder()
                .id(1L)
                .name("수능")
                .day(LocalDate.of(2025, 11, 14))
                .user(testUser)
                .build();
    }

    private void mockUserAuthentication() {
        given(jwtUtilService.extractTokenFromCookie(any(), anyString())).willReturn("validToken");
        given(jwtUtilService.getUserIdFromToken("validToken")).willReturn(testUserId.toString());
        given(userRepository.findByUserId(testUserId)).willReturn(Optional.of(testUser));
    }

    // ==================== D-Day 생성 테스트 ====================
    @Nested
    @DisplayName("D-Day 생성 테스트")
    class CreateDDayTest {

        @Test
        @DisplayName("D-Day 생성 성공")
        void createDDay_Success() {
            // Given
            mockUserAuthentication();
            DDayDTO.DDayRequestDto requestDto = DDayDTO.DDayRequestDto.builder()
                    .name("정보처리기사")
                    .day(LocalDate.of(2025, 5, 10))
                    .build();

            given(dayRepository.save(any(DDay.class))).willReturn(testDDay);

            // When
            CommonDTO.IsSuccessDTO result = dDayService.createDDay(request, requestDto);

            // Then
            assertThat(result.getIsSuccess()).isTrue();
            verify(dayRepository, times(1)).save(any(DDay.class));
        }
    }

    // ==================== D-Day 목록 조회 테스트 ====================
    @Nested
    @DisplayName("D-Day 목록 조회 테스트")
    class GetDDaysTest {

        @Test
        @DisplayName("D-Day 목록 조회 성공")
        void getDDays_Success() {
            // Given
            mockUserAuthentication();
            DDay dDay2 = DDay.builder()
                    .id(2L)
                    .name("토익")
                    .day(LocalDate.of(2025, 3, 15))
                    .user(testUser)
                    .build();

            given(dayRepository.findDDayByUser(testUser)).willReturn(List.of(testDDay, dDay2));

            // When
            List<DDayDTO.DDayResponseDto> result = dDayService.getDDays(request);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("수능");
            assertThat(result.get(1).getName()).isEqualTo("토익");
        }

        @Test
        @DisplayName("빈 D-Day 목록 조회")
        void getDDays_Empty() {
            // Given
            mockUserAuthentication();
            given(dayRepository.findDDayByUser(testUser)).willReturn(List.of());

            // When
            List<DDayDTO.DDayResponseDto> result = dDayService.getDDays(request);

            // Then
            assertThat(result).isEmpty();
        }
    }

    // ==================== D-Day 삭제 테스트 ====================
    @Nested
    @DisplayName("D-Day 삭제 테스트")
    class DeleteDDayTest {

        @Test
        @DisplayName("D-Day 삭제 성공")
        void deleteDDay_Success() {
            // Given
            mockUserAuthentication();
            given(dayRepository.findById(1L)).willReturn(Optional.of(testDDay));
            given(dayRepository.findByIdAndUser(1L, testUser)).willReturn(Optional.of(testDDay));

            // When
            CommonDTO.IsSuccessDTO result = dDayService.deleteDDay(request, 1L);

            // Then
            assertThat(result.getIsSuccess()).isTrue();
            verify(dayRepository, times(1)).delete(testDDay);
        }

        @Test
        @DisplayName("존재하지 않는 D-Day 삭제 실패")
        void deleteDDay_NotFound() {
            // Given
            mockUserAuthentication();
            given(dayRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> dDayService.deleteDDay(request, 999L))
                    .isInstanceOf(DDayHandler.class);
        }

        @Test
        @DisplayName("다른 사용자의 D-Day 삭제 실패")
        void deleteDDay_NotOwner() {
            // Given
            mockUserAuthentication();
            given(dayRepository.findById(1L)).willReturn(Optional.of(testDDay));
            given(dayRepository.findByIdAndUser(1L, testUser)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> dDayService.deleteDDay(request, 1L))
                    .isInstanceOf(DDayHandler.class);
        }
    }

    // ==================== D-Day 수정 테스트 ====================
    @Nested
    @DisplayName("D-Day 수정 테스트")
    class EditDDayTest {

        @Test
        @DisplayName("D-Day 수정 성공")
        void editDDay_Success() {
            // Given
            mockUserAuthentication();
            DDayDTO.DDayEditRequestDto requestDto = DDayDTO.DDayEditRequestDto.builder()
                    .id(1L)
                    .name("수정된 이름")
                    .day(LocalDate.of(2025, 12, 25))
                    .build();

            given(dayRepository.findById(1L)).willReturn(Optional.of(testDDay));
            given(dayRepository.findByIdAndUser(1L, testUser)).willReturn(Optional.of(testDDay));

            // When
            CommonDTO.IsSuccessDTO result = dDayService.editDDay(request, requestDto);

            // Then
            assertThat(result.getIsSuccess()).isTrue();
            assertThat(testDDay.getName()).isEqualTo("수정된 이름");
            assertThat(testDDay.getDay()).isEqualTo(LocalDate.of(2025, 12, 25));
            verify(dayRepository, times(1)).save(testDDay);
        }

        @Test
        @DisplayName("존재하지 않는 D-Day 수정 실패")
        void editDDay_NotFound() {
            // Given
            mockUserAuthentication();
            DDayDTO.DDayEditRequestDto requestDto = DDayDTO.DDayEditRequestDto.builder()
                    .id(999L)
                    .name("수정")
                    .day(LocalDate.now())
                    .build();

            given(dayRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> dDayService.editDDay(request, requestDto))
                    .isInstanceOf(DDayHandler.class);
        }

        @Test
        @DisplayName("다른 사용자의 D-Day 수정 실패")
        void editDDay_NotOwner() {
            // Given
            mockUserAuthentication();
            DDayDTO.DDayEditRequestDto requestDto = DDayDTO.DDayEditRequestDto.builder()
                    .id(1L)
                    .name("수정")
                    .day(LocalDate.now())
                    .build();

            given(dayRepository.findById(1L)).willReturn(Optional.of(testDDay));
            given(dayRepository.findByIdAndUser(1L, testUser)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> dDayService.editDDay(request, requestDto))
                    .isInstanceOf(DDayHandler.class);
        }
    }
}
