package com.example.be.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ParticipantDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantDto {
        private String sid;
        private String identity;
        private String name;
        private String metadata;
        private long joinedAt;
    }
}
