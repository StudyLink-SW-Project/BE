package com.example.be.web.controller;

import com.example.be.apiPayload.ApiResponse;
import com.example.be.service.CommentServiceImpl;
import com.example.be.service.PostServiceImpl;
import com.example.be.web.dto.CommentDTO;
import com.example.be.web.dto.CommonDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {
    private final PostServiceImpl postService;
    private final CommentServiceImpl commentService;

    @PostMapping("")
    @Operation(summary = "댓글 작성 API", description = "게시글에 댓글을 작성하거나 다른 댓글에 대한 답글을 작성합니다.")
    public ApiResponse<CommonDTO.IsSuccessDTO> createComment(
            @RequestBody CommentDTO.CommentRequestDTO requestDTO,
            HttpServletRequest request) {
        return ApiResponse.onSuccess(commentService.createComment(requestDTO, request));
    }
}
