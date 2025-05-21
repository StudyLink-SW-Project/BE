package com.example.be.service;


import com.example.be.apiPayload.ApiResponse;
import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.apiPayload.exception.handler.UserHandler;
import com.example.be.domain.Post;
import com.example.be.domain.User;
import com.example.be.repository.PostRepository;
import com.example.be.repository.UserRepository;
import com.example.be.web.dto.CommonDTO;
import com.example.be.web.dto.PostDTO;
import com.example.be.web.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PostServiceImpl {
    private final JwtUtilServiceImpl jwtUtilService;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostLikeServiceImpl postLikeService;


    //글 작성 메서드
    public CommonDTO.IsSuccessDTO write(PostDTO.postRequestDTO request, HttpServletRequest req) {
        String accessToken = jwtUtilService.extractTokenFromCookie(req, "accessToken");

        // 토큰이 없는 경우 처리
        if(accessToken == null) {
            throw new UserHandler(ErrorStatus._NOT_FOUND_USER);
        }

        // 토큰에서 사용자 ID 추출
        String userId = jwtUtilService.getUserIdFromToken(accessToken);

        // 사용자 정보 조회
        User user = userRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new UserHandler(ErrorStatus._NOT_FOUND_USER));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .createDate(LocalDateTime.now())
                .build();

        postRepository.save(post);


        return CommonDTO.IsSuccessDTO.builder().isSuccess(true).build();
    }

    public PostDTO.PageResponseDTO getPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findAllByOrderByCreateDateDesc(pageable);

        List<PostDTO.postResponseDTO> postDtoList = postPage.getContent().stream()
                .map(post -> PostDTO.postResponseDTO.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .userName(post.getUser().getName())
                        .createDate(post.getCreateDate().toLocalDate())
                        .isDone(post.isDone())
                        .commentCount(post.getComments() != null ? post.getComments().size() : 0)
                        .likeCount(postLikeService.getPostLikeCount(post))
                        .build())
                .collect(Collectors.toList());

        return PostDTO.PageResponseDTO.builder()
                .posts(postDtoList)
                .currentPage(postPage.getNumber())
                .totalPages(postPage.getTotalPages())
                .totalElements(postPage.getTotalElements())
                .first(postPage.isFirst())
                .last(postPage.isLast())
                .build();
    }

    // 게시글 상세 조회 메서드
    public PostDTO.PostDetailResponseDTO getPostDetail(Long postId, HttpServletRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new UserHandler(ErrorStatus._NOT_FOUND_POST));

        // 현재 로그인한 사용자 정보 가져오기 (좋아요 여부 확인용)
        User currentUser = null;
        boolean isLiked = false;

        try {
            String accessToken = jwtUtilService.extractTokenFromCookie(request, "accessToken");
            if (accessToken != null) {
                String userId = jwtUtilService.getUserIdFromToken(accessToken);
                currentUser = userRepository.findByUserId(UUID.fromString(userId)).orElse(null);

                // 현재 사용자가 이 게시글에 좋아요를 눌렀는지 확인
                if (currentUser != null) {
                    isLiked = postLikeService.isPostLikedByUser(post, currentUser);
                }
            }
        } catch (Exception e) {
            // 비로그인 사용자도 게시글을 볼 수 있도록 예외를 무시하고 진행
        }

        // 댓글 목록 변환
        List<PostDTO.CommentResponseDTO> commentDtoList = post.getComments().stream()
                .map(comment -> PostDTO.CommentResponseDTO.builder()
                        .id(comment.getId())
                        .comment(comment.getComment())
                        .userName(comment.getUser().getName())
                        .createDate(comment.getCreateDate())
                        .topParentId(comment.getTopParent() != null ? comment.getTopParent().getId() : null)
                        .build())
                .collect(Collectors.toList());

        // 좋아요 수 조회
        long likeCount = postLikeService.getPostLikeCount(post);

        // 게시글 상세 정보 변환
        return PostDTO.PostDetailResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .userName(post.getUser().getName())
                .createDate(post.getCreateDate())
                .isDone(post.isDone())
                .commentCount(commentDtoList.size())
                .comments(commentDtoList)
                // 좋아요 정보 추가
                .likeCount(likeCount)
                .liked(isLiked)
                .build();
    }
}
