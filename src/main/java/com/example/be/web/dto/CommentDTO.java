package com.example.be.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "댓글 좋아요 응답 DTO")
    public static class CommentLikeResponseDTO {
        private Long commentId;
        private boolean liked;
        private long likeCount;
    }

    // 기존 CommentResponseDTO에 좋아요 관련 필드 추가
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

        private Long topParentId; // 상위 댓글 ID (답글인 경우)

        // 좋아요 관련 필드 추가
        private long likeCount;
        private boolean liked; // 현재 사용자가 좋아요를 눌렀는지 여부
    }
}
