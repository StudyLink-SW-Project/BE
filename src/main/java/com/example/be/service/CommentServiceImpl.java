package com.example.be.service;

import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.apiPayload.exception.handler.CommentHandler;
import com.example.be.apiPayload.exception.handler.UserHandler;
import com.example.be.domain.Comment;
import com.example.be.domain.Post;
import com.example.be.domain.User;
import com.example.be.repository.CommentRepository;
import com.example.be.repository.PostRepository;
import com.example.be.repository.UserRepository;
import com.example.be.web.dto.CommentDTO;
import com.example.be.web.dto.CommonDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl {

    private final JwtUtilServiceImpl jwtUtilService;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public CommonDTO.IsSuccessDTO createComment(CommentDTO.CommentRequestDTO requestDTO, HttpServletRequest request) {
        String accessToken = jwtUtilService.extractTokenFromCookie(request, "accessToken");
        if (accessToken == null) {
            throw new UserHandler(ErrorStatus._NOT_FOUND_USER);
        }

        String userId = jwtUtilService.getUserIdFromToken(accessToken);
        User user = userRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new UserHandler(ErrorStatus._NOT_FOUND_USER));

        Post post = postRepository.findById(requestDTO.getPostId())
                .orElseThrow(() -> new UserHandler(ErrorStatus._NOT_FOUND_POST));

        Comment comment = Comment.builder()
                .comment(requestDTO.getComment())
                .createDate(LocalDateTime.now())
                .user(user)
                .post(post)
                .build();

        // 대댓글인 경우 상위 댓글 설정
        if (requestDTO.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(requestDTO.getParentCommentId())
                    .orElseThrow(() -> new CommentHandler(ErrorStatus._NOT_FOUND_COMMENT));

            if (parentComment.getTopParent() != null) {
                comment.setTopParent(parentComment.getTopParent());
            } else {
                comment.setTopParent(parentComment);
            }
        }

        commentRepository.save(comment);
        return CommonDTO.IsSuccessDTO.builder().isSuccess(true).build();
    }
}
