package com.example.be.service;

import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.apiPayload.exception.handler.UserHandler;
import com.example.be.domain.Comment;
import com.example.be.domain.CommentLike;
import com.example.be.domain.User;
import com.example.be.repository.CommentLikeRepository;
import com.example.be.repository.CommentRepository;
import com.example.be.repository.UserRepository;
import com.example.be.web.dto.CommentDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentLikeServiceImpl {
    private final JwtUtilServiceImpl jwtUtilService;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

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

    @Transactional
    public CommentDTO.CommentLikeResponseDTO toggleCommentLike(Long commentId, HttpServletRequest request) {

        User user= getUserFromRequest(request);

        // 댓글 정보 가져오기
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new UserHandler(ErrorStatus._NOT_FOUND_COMMENT));

        // 이미 좋아요를 눌렀는지 확인
        Optional<CommentLike> existingLike = commentLikeRepository.findByUserAndComment(user, comment);

        boolean isLiked;
        if (existingLike.isPresent()) {
            // 좋아요가 이미 있으면 삭제 (좋아요 취소)
            commentLikeRepository.delete(existingLike.get());
            isLiked = false;
        } else {
            // 좋아요가 없으면 추가
            CommentLike commentLike = CommentLike.builder()
                    .user(user)
                    .comment(comment)
                    .createDate(ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime())
                    .build();
            commentLikeRepository.save(commentLike);
            isLiked = true;
        }

        // 좋아요 수 조회
        long likeCount = commentLikeRepository.countByComment(comment);

        return CommentDTO.CommentLikeResponseDTO.builder()
                .commentId(commentId)
                .liked(isLiked)
                .likeCount(likeCount)
                .build();
    }

    // 특정 댓글에 대한 사용자의 좋아요 여부 확인
    public boolean isCommentLikedByUser(Comment comment, User user) {
        return commentLikeRepository.existsByUserAndComment(user, comment);
    }

    // 특정 댓글의 좋아요 수 조회
    public long getCommentLikeCount(Comment comment) {
        return commentLikeRepository.countByComment(comment);
    }

    // 특정 댓글 ID의 좋아요 수 조회
    public long getCommentLikeCount(Long commentId) {
        return commentLikeRepository.countByCommentId(commentId);
    }
}
