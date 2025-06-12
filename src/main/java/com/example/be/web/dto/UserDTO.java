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
        private String loginType;
        private String email;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SingUpRequestDto {
        private String email;
        private String name;
        private String password;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequestDto {
        private String email;
        private String password;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class studyTimeResponseDto {
        private Long userId;
        private String todayStudyTime;
        private String totalStudyTime;
        private String goalStudyTime;
    }
}
