package com.example.be.web.controller;

import com.example.be.apiPayload.ApiResponse;
import com.example.be.service.CommentLikeServiceImpl;
import com.example.be.service.CommentServiceImpl;
import com.example.be.service.PostServiceImpl;
import com.example.be.web.dto.CommentDTO;
import com.example.be.web.dto.CommonDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {
    private final PostServiceImpl postService;
    private final CommentServiceImpl commentService;
    private final CommentLikeServiceImpl commentLikeService;

    @PostMapping("")
    @Operation(summary = "댓글 작성 API", description = "게시글에 댓글을 작성하거나 다른 댓글에 대한 답글을 작성합니다.")
    public ApiResponse<CommonDTO.IsSuccessDTO> createComment(
            @RequestBody CommentDTO.CommentRequestDTO requestDTO,
            HttpServletRequest request) {
        return ApiResponse.onSuccess(commentService.createComment(requestDTO, request));
    }

    @PostMapping("/{commentId}/like")
    @Operation(summary = "댓글 좋아요 토글 API", description = "댓글에 좋아요를 누르거나 취소합니다.")
    public ApiResponse<CommentDTO.CommentLikeResponseDTO> toggleCommentLike(
            @Parameter(description = "댓글 ID") @PathVariable Long commentId,
            HttpServletRequest request) {
        return ApiResponse.onSuccess(commentLikeService.toggleCommentLike(commentId, request));
    }

    @PostMapping("/{commentId}/delete")
    @Operation(summary = "댓글 삭제 API", description = "댓글을 삭제합니다.")
    public ApiResponse<CommonDTO.IsSuccessDTO> deleteComment(
            @Parameter(description = "댓글 ID") @PathVariable Long commentId,
            HttpServletRequest request) {
        return ApiResponse.onSuccess(commentService.deleteComment(commentId, request));
    }
}
