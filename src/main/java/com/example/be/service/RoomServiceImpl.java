package com.example.be.service;

import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.apiPayload.exception.handler.RoomHandler;
import com.example.be.domain.Room;
import com.example.be.repository.RoomRepository;
import com.example.be.web.dto.RoomDTO;
import io.livekit.server.RoomService;
import io.livekit.server.RoomServiceClient;
import livekit.LivekitModels;
import livekit.LivekitRoom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomServiceImpl {
    private final RoomServiceClient roomServiceClient;
    private final RoomRepository roomRepository;

    public RoomDTO.RoomListResponseDto getAllRooms() {
        try {
            Call<List<LivekitModels.Room>> call = roomServiceClient.listRooms();
            Response<List<LivekitModels.Room>> response = call.execute();

            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to fetch rooms: " + response.errorBody().string());
            }

            List<LivekitModels.Room> rooms = response.body();
            List<RoomDTO.RoomDto> roomDtos = rooms.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return RoomDTO.RoomListResponseDto.builder()
                    .rooms(roomDtos)
                    .totalCount(roomDtos.size())
                    .build();
        } catch (IOException e) {
            log.error("Failed to fetch rooms from LiveKit", e);
            throw new RuntimeException("Failed to fetch rooms from LiveKit", e);
        }
    }


    private RoomDTO.RoomDto convertToDto(LivekitModels.Room room) {
        return RoomDTO.RoomDto.builder()
                .roomName(room.getName())
                .creationTime(room.getCreationTime())
                .participantsCounts(room.getNumParticipants())
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
