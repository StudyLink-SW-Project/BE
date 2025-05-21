package com.example.be.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class PostDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor

    public static class postRequestDTO{
        String title;
        String content;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class postResponseDTO {
        Long id;
        String title;
        String content;
        String userName;
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate createDate;
        int commentCount;
        boolean isDone;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "게시글 페이지네이션 응답 DTO")
    public static class PageResponseDTO {
        private List<postResponseDTO> posts;
        private int currentPage;
        private int totalPages;
        private long totalElements;
        private boolean first;
        private boolean last;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "댓글 응답 DTO")
    public static class CommentResponseDTO {
        private Long id;
        private String comment;
        private String userName;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createDate;

        private Long topParentId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "게시글 상세 조회 응답 DTO")
    public static class PostDetailResponseDTO {
        private Long id;
        private String title;
        private String content;
        private String userName;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createDate;

        private boolean isDone;
        private int commentCount;
        private List<CommentResponseDTO> comments;
    }
}
