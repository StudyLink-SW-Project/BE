package com.example.be.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CommentDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "댓글 작성 요청 DTO")
    public static class CommentRequestDTO {
        private String comment;
        private Long postId;
        private Long parentCommentId;
    }
}
