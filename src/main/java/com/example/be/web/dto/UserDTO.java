package com.example.be.web.dto;

import com.example.be.domain.enums.LoginType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponseDto {
        private Long userId;
        private String userName;
        private LoginType loginType;
        private String email;
    }
}
