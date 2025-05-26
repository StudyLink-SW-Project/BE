package com.example.be.service;

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
                .sid(room.getSid())
                .name(room.getName())
                .creationTime(room.getCreationTime())
                .numParticipants(room.getNumParticipants())
                .maxParticipants(room.getMaxParticipants())
                .metadata(room.getMetadata())
                .build();
    }
}
