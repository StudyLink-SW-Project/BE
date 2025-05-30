package com.example.be.service;


import com.example.be.apiPayload.ApiResponse;
import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.apiPayload.exception.handler.PostHandler;
import com.example.be.apiPayload.exception.handler.UserHandler;
import com.example.be.domain.Comment;
import com.example.be.domain.Post;
import com.example.be.domain.User;
import com.example.be.repository.PostRepository;
import com.example.be.repository.UserRepository;
import com.example.be.web.dto.CommentDTO;
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
    private final CommentLikeServiceImpl commentLikeService;

    // 사용자 정보 가져오는 메서드
    private User getUserFromRequest(HttpServletRequest request) {
        try {
            String accessToken = jwtUtilService.extractTokenFromCookie(request, "accessToken");
            if (accessToken != null) {
                String userId = jwtUtilService.getUserIdFromToken(accessToken);
                return userRepository.findByUserId(UUID.fromString(userId)).orElse(null);
            }
        } catch (Exception e) {
            throw new UserHandler(ErrorStatus._NOT_FOUND_COOKIE);
        }
        return null;
    }

    //글 작성 메서드
    public CommonDTO.IsSuccessDTO write(PostDTO.postRequestDTO request, HttpServletRequest req) {
        User user= getUserFromRequest(req);

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


    private boolean isCommentLikedByUser(Comment comment, User user) {
        if (user == null) {
            return false;
        }
        return commentLikeService.isCommentLikedByUser(comment, user);
    }

    // 게시글 상세 조회 메서드
    public PostDTO.PostDetailResponseDTO getPostDetail(Long postId, HttpServletRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._NOT_FOUND_POST));

        // 현재 로그인한 사용자 정보 가져오기
        User currentUser = getUserFromRequest(request);
        boolean isPostLiked = currentUser != null && postLikeService.isPostLikedByUser(post, currentUser);

        // 댓글 목록 변환 (좋아요 정보 포함)
        List<CommentDTO.CommentResponseDTO> commentDtoList = post.getComments().stream()
                .map(comment -> {
                    // 현재 사용자가 이 댓글에 좋아요를 눌렀는지 확인
                    boolean isCommentLiked = isCommentLikedByUser(comment, currentUser);
                    long commentLikeCount = commentLikeService.getCommentLikeCount(comment);

                    return CommentDTO.CommentResponseDTO.builder()
                            .id(comment.getId())
                            .comment(comment.getComment())
                            .userName(comment.getUser().getName())
                            .createDate(comment.getCreateDate())
                            .topParentId(comment.getTopParent() != null ? comment.getTopParent().getId() : null)
                            .likeCount(commentLikeCount)
                            .liked(isCommentLiked)
                            .build();
                })
                .collect(Collectors.toList());

        // 게시글의 좋아요 수 조회
        long postLikeCount = postLikeService.getPostLikeCount(post);

        // 게시글 상세 정보 변환
        return PostDTO.PostDetailResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .userName(post.getUser().getName())
                .createDate(post.getCreateDate())
                .commentCount(commentDtoList.size())
                .comments(commentDtoList)
                .likeCount(postLikeCount)
                .liked(isPostLiked)
                .build();
    }

    public CommonDTO.IsSuccessDTO deletePost(Long postId, HttpServletRequest request) {
        User user= getUserFromRequest(request);

        postRepository.findById(postId).orElseThrow(() ->
                new PostHandler(ErrorStatus._NOT_FOUND_POST));

        Post post = postRepository.findByIdAndUserId(postId, user.getId()).orElseThrow(()->
                new PostHandler(ErrorStatus._NOT_USER_POST));

        postRepository.delete(post);

        return CommonDTO.IsSuccessDTO.builder().isSuccess(true).build();
    }
}
