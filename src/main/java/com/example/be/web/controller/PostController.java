package com.example.be.web.controller;

import com.example.be.apiPayload.ApiResponse;
import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.apiPayload.exception.handler.UserHandler;
import com.example.be.domain.User;
import com.example.be.repository.UserRepository;
import com.example.be.service.JwtUtilServiceImpl;
import com.example.be.service.PostLikeServiceImpl;
import com.example.be.service.PostServiceImpl;
import com.example.be.service.UserServiceImpl;
import com.example.be.web.dto.CommonDTO;
import com.example.be.web.dto.PostDTO;
import com.example.be.web.dto.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostServiceImpl postService;
    private final PostLikeServiceImpl postLikeService;

    @PostMapping("/write")
    @Operation(summary = "게시글 작성 API")
    public ApiResponse<CommonDTO.IsSuccessDTO> write(@RequestBody PostDTO.postRequestDTO request, HttpServletRequest req) {

        return ApiResponse.onSuccess(postService.write(request, req));
    }

    @GetMapping("/list")
    @Operation(summary = "게시글 목록 조회 API (페이지네이션)", description = "4개씩 페이지네이션하여 게시글 목록을 조회합니다.")
    public ApiResponse<PostDTO.PageResponseDTO> getPosts(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page) {
        return ApiResponse.onSuccess(postService.getPosts(page, 4)); // 4개씩 페이지네이션
    }

    @GetMapping("/{postId}")
    @Operation(summary = "게시글 상세 조회 API", description = "게시글 ID로 게시글 상세 정보와 댓글 목록을 조회합니다.")
    public ApiResponse<PostDTO.PostDetailResponseDTO> getPostDetail(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            HttpServletRequest request) {
        return ApiResponse.onSuccess(postService.getPostDetail(postId, request));
    }

    @PostMapping("/{postId}/like")
    @Operation(summary = "게시글 좋아요 토글 API", description = "게시글에 좋아요를 누르거나 취소합니다.")
    public ApiResponse<PostDTO.PostLikeResponseDTO> togglePostLike(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            HttpServletRequest request) {
        return ApiResponse.onSuccess(postLikeService.togglePostLike(postId, request));
    }

    @PostMapping("/{postId}/delete")
    @Operation(summary = "게시글 삭제 API", description = "게시글을 삭제합니다.")
    public ApiResponse<CommonDTO.IsSuccessDTO> delete(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            HttpServletRequest request) {
        return ApiResponse.onSuccess(postService.deletePost(postId, request));
    }

    @GetMapping("/mypost")
    @Operation(summary = "내가 작성한 게시글 조회 API", description = "내가 작성한 게시글들을 조회합니다.")
    public ApiResponse<PostDTO.PageResponseDTO> getMyPosts(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            HttpServletRequest request) {
        return ApiResponse.onSuccess(postService.getMyPosts(page, 4, request));
    }


}
