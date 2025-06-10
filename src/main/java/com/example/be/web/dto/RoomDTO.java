package com.example.be.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class RoomDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomDto {
        private String roomName;
        private String password;
        private String roomImage;
        private long creationTime;
        private int participantsCounts;
        private int maxParticipants;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRoomRequestDto {
        private String roomName;
        private Integer maxParticipants;
        private String metadata;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomListResponseDto {
        private List<RoomDto> rooms;
        private int totalCount;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomSetRequestDto {
        private String roomName;
        private String password;
        private String roomImage;
    }
}
