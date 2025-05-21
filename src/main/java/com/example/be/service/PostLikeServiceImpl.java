package com.example.be.service;

import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.apiPayload.exception.handler.UserHandler;
import com.example.be.domain.Post;
import com.example.be.domain.PostLike;
import com.example.be.domain.User;
import com.example.be.repository.PostLikeRepository;
import com.example.be.repository.PostRepository;
import com.example.be.repository.UserRepository;
import com.example.be.web.dto.PostDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl {

    private final JwtUtilServiceImpl jwtUtilService;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    @Transactional
    public PostDTO.PostLikeResponseDTO togglePostLike(Long postId, HttpServletRequest request) {
        // 토큰에서 사용자 정보 가져오기
        String accessToken = jwtUtilService.extractTokenFromCookie(request, "accessToken");
        if (accessToken == null) {
            throw new UserHandler(ErrorStatus._NOT_FOUND_USER);
        }

        String userId = jwtUtilService.getUserIdFromToken(accessToken);
        User user = userRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new UserHandler(ErrorStatus._NOT_FOUND_USER));

        // 게시글 정보 가져오기
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new UserHandler(ErrorStatus._NOT_FOUND_POST));

        // 이미 좋아요를 눌렀는지 확인
        Optional<PostLike> existingLike = postLikeRepository.findByUserAndPost(user, post);

        boolean isLiked;
        if (existingLike.isPresent()) {
            // 좋아요가 이미 있으면 삭제 (좋아요 취소)
            postLikeRepository.delete(existingLike.get());
            isLiked = false;
        } else {
            // 좋아요가 없으면 추가
            PostLike postLike = PostLike.builder()
                    .user(user)
                    .post(post)
                    .createDate(LocalDateTime.now())
                    .build();
            postLikeRepository.save(postLike);
            isLiked = true;
        }

        // 좋아요 수 조회
        long likeCount = postLikeRepository.countByPost(post);

        return PostDTO.PostLikeResponseDTO.builder()
                .postId(postId)
                .liked(isLiked)
                .likeCount(likeCount)
                .build();
    }

    // 특정 게시글에 대한 사용자의 좋아요 여부 확인
    public boolean isPostLikedByUser(Post post, User user) {
        return postLikeRepository.existsByUserAndPost(user, post);
    }

    // 특정 게시글의 좋아요 수 조회
    public long getPostLikeCount(Post post) {
        return postLikeRepository.countByPost(post);
    }

    // 특정 게시글 ID의 좋아요 수 조회
    public long getPostLikeCount(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }
}