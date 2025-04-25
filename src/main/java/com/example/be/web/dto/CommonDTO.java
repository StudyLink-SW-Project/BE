package com.example.be.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CommonDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "COMMON_RES_06 : API 실행 성공 여부 응답 DTO")
    public static class IsSuccessDTO{
        Boolean isSuccess;
    }
}
