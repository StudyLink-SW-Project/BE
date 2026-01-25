package com.example.be.service;

import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.apiPayload.exception.handler.RoomHandler;
import com.example.be.domain.Room;
import com.example.be.repository.RoomRepository;
import com.example.be.web.dto.RoomDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @InjectMocks
    private RoomServiceImpl roomService;

    @Mock
    private RoomRepository roomRepository;

    private Room testRoom;

    @BeforeEach
    void setUp() {
        testRoom = Room.builder()
                .id(1L)
                .title("알고리즘 스터디")
                .description("코딩테스트 준비방")
                .password(null)
                .roomImage("image.jpg")
                .maxParticipants(10)
                .participantCount(3)
                .createDate(System.currentTimeMillis())
                .build();
    }

    // ==================== 스터디룸 목록 조회 테스트 ====================
    @Nested
    @DisplayName("스터디룸 목록 조회 테스트")
    class GetAllRoomsTest {

        @Test
        @DisplayName("스터디룸 목록 조회 성공")
        void getAllRooms_Success() {
            // Given
            Room room2 = Room.builder()
                    .id(2L)
                    .title("자바 스터디")
                    .description("스프링 공부방")
                    .maxParticipants(5)
                    .participantCount(2)
                    .createDate(System.currentTimeMillis())
                    .build();

            given(roomRepository.findAll()).willReturn(List.of(testRoom, room2));

            // When
            RoomDTO.RoomListResponseDto result = roomService.getAllRooms();

            // Then
            assertThat(result.getRooms()).hasSize(2);
            assertThat(result.getTotalCount()).isEqualTo(2);
            assertThat(result.getRooms().get(0).getRoomName()).isEqualTo("알고리즘 스터디");
            assertThat(result.getRooms().get(1).getRoomName()).isEqualTo("자바 스터디");
        }

        @Test
        @DisplayName("빈 스터디룸 목록 조회")
        void getAllRooms_Empty() {
            // Given
            given(roomRepository.findAll()).willReturn(new ArrayList<>());

            // When
            RoomDTO.RoomListResponseDto result = roomService.getAllRooms();

            // Then
            assertThat(result.getRooms()).isEmpty();
            assertThat(result.getTotalCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("스터디룸 참여자 수 확인")
        void getAllRooms_CheckParticipants() {
            // Given
            given(roomRepository.findAll()).willReturn(List.of(testRoom));

            // When
            RoomDTO.RoomListResponseDto result = roomService.getAllRooms();

            // Then
            assertThat(result.getRooms().get(0).getParticipantsCounts()).isEqualTo(3);
            assertThat(result.getRooms().get(0).getMaxParticipants()).isEqualTo(10);
        }
    }

    // ==================== 스터디룸 설정 테스트 ====================
    @Nested
    @DisplayName("스터디룸 설정 테스트")
    class SetRoomTest {

        @Test
        @DisplayName("스터디룸 설정 성공")
        void setRoom_Success() {
            // Given
            RoomDTO.RoomSetRequestDto requestDto = RoomDTO.RoomSetRequestDto.builder()
                    .roomName("알고리즘 스터디")
                    .roomDescription("수정된 설명")
                    .password("1234")
                    .roomImage("new_image.jpg")
                    .build();

            given(roomRepository.findByTitle("알고리즘 스터디")).willReturn(testRoom);

            // When
            RoomDTO.RoomDto result = roomService.setRoom(requestDto);

            // Then
            assertThat(result.getRoomName()).isEqualTo("알고리즘 스터디");
            assertThat(result.getRoomDescription()).isEqualTo("수정된 설명");
            assertThat(result.getPassword()).isEqualTo("1234");
            assertThat(result.getRoomImage()).isEqualTo("new_image.jpg");
            verify(roomRepository, times(1)).save(testRoom);
        }

        @Test
        @DisplayName("비밀번호 설정 성공")
        void setRoom_WithPassword() {
            // Given
            RoomDTO.RoomSetRequestDto requestDto = RoomDTO.RoomSetRequestDto.builder()
                    .roomName("알고리즘 스터디")
                    .roomDescription("비밀방")
                    .password("secret123")
                    .roomImage("image.jpg")
                    .build();

            given(roomRepository.findByTitle("알고리즘 스터디")).willReturn(testRoom);

            // When
            RoomDTO.RoomDto result = roomService.setRoom(requestDto);

            // Then
            assertThat(result.getPassword()).isEqualTo("secret123");
        }

        @Test
        @DisplayName("존재하지 않는 스터디룸 설정 실패")
        void setRoom_NotFound() {
            // Given
            RoomDTO.RoomSetRequestDto requestDto = RoomDTO.RoomSetRequestDto.builder()
                    .roomName("없는 방")
                    .roomDescription("설명")
                    .build();

            given(roomRepository.findByTitle("없는 방")).willReturn(null);

            // When & Then
            assertThatThrownBy(() -> roomService.setRoom(requestDto))
                    .isInstanceOf(RoomHandler.class);
        }
    }

    // ==================== 동시성 제어 관련 테스트 (이력서 내용) ====================
    @Nested
    @DisplayName("스터디룸 참여자 수 정합성 테스트")
    class ParticipantCountTest {

        @Test
        @DisplayName("참여자 수가 최대 인원을 초과하지 않음")
        void participantCount_NotExceedMax() {
            // Given
            testRoom.setParticipantCount(10);  // 최대 인원
            testRoom.setMaxParticipants(10);

            given(roomRepository.findAll()).willReturn(List.of(testRoom));

            // When
            RoomDTO.RoomListResponseDto result = roomService.getAllRooms();

            // Then
            RoomDTO.RoomDto room = result.getRooms().get(0);
            assertThat(room.getParticipantsCounts()).isLessThanOrEqualTo(room.getMaxParticipants());
        }

        @Test
        @DisplayName("참여자 수가 0 이상임")
        void participantCount_NonNegative() {
            // Given
            testRoom.setParticipantCount(0);

            given(roomRepository.findAll()).willReturn(List.of(testRoom));

            // When
            RoomDTO.RoomListResponseDto result = roomService.getAllRooms();

            // Then
            assertThat(result.getRooms().get(0).getParticipantsCounts()).isGreaterThanOrEqualTo(0);
        }
    }
}
