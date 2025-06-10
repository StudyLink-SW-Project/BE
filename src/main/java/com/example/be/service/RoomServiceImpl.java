package com.example.be.service;

import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.apiPayload.exception.handler.RoomHandler;
import com.example.be.domain.Room;
import com.example.be.repository.RoomRepository;
import com.example.be.web.dto.RoomDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomServiceImpl {
    private final RoomRepository roomRepository;

    public RoomDTO.RoomListResponseDto getAllRooms() {

        List<Room> rooms = roomRepository.findAll();
        List<RoomDTO.RoomDto> roomDtos = rooms.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return RoomDTO.RoomListResponseDto.builder()
                .rooms(roomDtos)
                .totalCount(roomDtos.size())
                .build();
    }

    private RoomDTO.RoomDto convertToDto(Room room) {
        return RoomDTO.RoomDto.builder()
                .roomName(room.getTitle())
                .password(room.getPassword())
                .roomImage(room.getRoomImage())
                .creationTime(room.getCreateDate())
                .participantsCounts(room.getParticipantCount())
                .maxParticipants(room.getMaxParticipants())
                .build();
    }

    public RoomDTO.RoomDto setRoom(RoomDTO.RoomSetRequestDto request) {
        Room room = roomRepository.findByTitle(request.getRoomName());
        if (room == null) {
            throw new RoomHandler(ErrorStatus._NOT_FOUND_ROOM);
        }
        room.setPassword(request.getPassword());
        room.setRoomImage(request.getRoomImage());
        roomRepository.save(room);

        return RoomDTO.RoomDto.builder()
                .roomName(room.getTitle())
                .roomImage(room.getRoomImage())
                .password(room.getPassword())
                .participantsCounts(room.getParticipantCount())
                .creationTime(room.getCreateDate())
                .maxParticipants(room.getMaxParticipants())
                .build();
    }
}
