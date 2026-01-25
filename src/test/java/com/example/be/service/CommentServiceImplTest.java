package com.example.be.service;

import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.apiPayload.exception.handler.CommentHandler;
import com.example.be.domain.Comment;
import com.example.be.domain.Post;
import com.example.be.domain.User;
import com.example.be.repository.CommentRepository;
import com.example.be.repository.PostRepository;
import com.example.be.repository.UserRepository;
import com.example.be.web.dto.CommentDTO;
import com.example.be.web.dto.CommonDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @InjectMocks
    private CommentServiceImpl commentService;

    @Mock
    private JwtUtilServiceImpl jwtUtilService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private HttpServletRequest request;

    private User testUser;
    private Post testPost;
    private Comment testComment;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(1L)
                .userId(testUserId)
                .email("test@example.com")
                .name("테스트유저")
                .build();

        testPost = Post.builder()
                .id(1L)
                .title("테스트 게시글")
                .content("테스트 내용")
                .user(testUser)
                .createDate(LocalDateTime.now())
                .build();

        testComment = Comment.builder()
                .id(1L)
                .comment("테스트 댓글")
                .user(testUser)
                .post(testPost)
                .createDate(LocalDateTime.now())
                .build();
    }

    private void mockUserAuthentication() {
        given(jwtUtilService.extractTokenFromCookie(any(), anyString())).willReturn("validToken");
        given(jwtUtilService.getUserIdFromToken("validToken")).willReturn(testUserId.toString());
        given(userRepository.findByUserId(testUserId)).willReturn(Optional.of(testUser));
    }

    // ==================== 댓글 작성 테스트 ====================
    @Nested
    @DisplayName("댓글 작성 테스트")
    class CreateCommentTest {

        @Test
        @DisplayName("댓글 작성 성공")
        void createComment_Success() {
            // Given
            mockUserAuthentication();
            CommentDTO.CommentRequestDTO requestDTO = CommentDTO.CommentRequestDTO.builder()
                    .comment("새 댓글")
                    .postId(1L)
                    .parentCommentId(null)
                    .build();

            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));
            given(commentRepository.save(any(Comment.class))).willReturn(testComment);

            // When
            CommonDTO.IsSuccessDTO result = commentService.createComment(requestDTO, request);

            // Then
            assertThat(result.getIsSuccess()).isTrue();
            verify(commentRepository, times(1)).save(any(Comment.class));
        }

        @Test
        @DisplayName("대댓글 작성 성공")
        void createReply_Success() {
            // Given
            mockUserAuthentication();
            CommentDTO.CommentRequestDTO requestDTO = CommentDTO.CommentRequestDTO.builder()
                    .comment("대댓글입니다")
                    .postId(1L)
                    .parentCommentId(1L)  // 부모 댓글 ID
                    .build();

            Comment parentComment = Comment.builder()
                    .id(1L)
                    .comment("부모 댓글")
                    .user(testUser)
                    .post(testPost)
                    .topParent(null)  // 최상위 댓글
                    .build();

            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));
            given(commentRepository.findById(1L)).willReturn(Optional.of(parentComment));
            given(commentRepository.save(any(Comment.class))).willReturn(testComment);

            // When
            CommonDTO.IsSuccessDTO result = commentService.createComment(requestDTO, request);

            // Then
            assertThat(result.getIsSuccess()).isTrue();
            verify(commentRepository, times(1)).save(any(Comment.class));
        }

        @Test
        @DisplayName("존재하지 않는 게시글에 댓글 작성 실패")
        void createComment_PostNotFound() {
            // Given
            mockUserAuthentication();
            CommentDTO.CommentRequestDTO requestDTO = CommentDTO.CommentRequestDTO.builder()
                    .comment("댓글")
                    .postId(999L)
                    .build();

            given(postRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> commentService.createComment(requestDTO, request))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("존재하지 않는 부모 댓글에 대댓글 작성 실패")
        void createReply_ParentNotFound() {
            // Given
            mockUserAuthentication();
            CommentDTO.CommentRequestDTO requestDTO = CommentDTO.CommentRequestDTO.builder()
                    .comment("대댓글")
                    .postId(1L)
                    .parentCommentId(999L)
                    .build();

            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));
            given(commentRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> commentService.createComment(requestDTO, request))
                    .isInstanceOf(CommentHandler.class);
        }
    }

    // ==================== 댓글 삭제 테스트 ====================
    @Nested
    @DisplayName("댓글 삭제 테스트")
    class DeleteCommentTest {

        @Test
        @DisplayName("댓글 삭제 성공")
        void deleteComment_Success() {
            // Given
            mockUserAuthentication();
            given(commentRepository.findById(1L)).willReturn(Optional.of(testComment));
            given(commentRepository.findByIdAndUserId(1L, testUser.getId())).willReturn(Optional.of(testComment));

            // When
            CommonDTO.IsSuccessDTO result = commentService.deleteComment(1L, request);

            // Then
            assertThat(result.getIsSuccess()).isTrue();
            verify(commentRepository, times(1)).delete(testComment);
        }

        @Test
        @DisplayName("존재하지 않는 댓글 삭제 실패")
        void deleteComment_NotFound() {
            // Given
            mockUserAuthentication();
            given(commentRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> commentService.deleteComment(999L, request))
                    .isInstanceOf(CommentHandler.class);
        }

        @Test
        @DisplayName("다른 사용자의 댓글 삭제 실패")
        void deleteComment_NotOwner() {
            // Given
            mockUserAuthentication();
            given(commentRepository.findById(1L)).willReturn(Optional.of(testComment));
            given(commentRepository.findByIdAndUserId(1L, testUser.getId())).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> commentService.deleteComment(1L, request))
                    .isInstanceOf(CommentHandler.class);
        }
    }
}
