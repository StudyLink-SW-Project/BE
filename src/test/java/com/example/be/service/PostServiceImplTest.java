package com.example.be.service;

import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.apiPayload.exception.handler.PostHandler;
import com.example.be.domain.Comment;
import com.example.be.domain.Post;
import com.example.be.domain.User;
import com.example.be.repository.PostRepository;
import com.example.be.repository.UserRepository;
import com.example.be.web.dto.CommonDTO;
import com.example.be.web.dto.PostDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @InjectMocks
    private PostServiceImpl postService;

    @Mock
    private JwtUtilServiceImpl jwtUtilService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeServiceImpl postLikeService;

    @Mock
    private CommentLikeServiceImpl commentLikeService;

    @Mock
    private HttpServletRequest request;

    private User testUser;
    private Post testPost;
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
                .title("테스트 제목")
                .content("테스트 내용")
                .user(testUser)
                .createDate(LocalDateTime.now())
                .comments(new ArrayList<>())
                .likes(new ArrayList<>())
                .build();
    }

    private void mockUserAuthentication() {
        given(jwtUtilService.extractTokenFromCookie(any(), anyString())).willReturn("validToken");
        given(jwtUtilService.getUserIdFromToken("validToken")).willReturn(testUserId.toString());
        given(userRepository.findByUserId(testUserId)).willReturn(Optional.of(testUser));
    }

    // ==================== 글 작성 테스트 ====================
    @Nested
    @DisplayName("글 작성 테스트")
    class WritePostTest {

        @Test
        @DisplayName("글 작성 성공")
        void write_Success() {
            // Given
            mockUserAuthentication();
            PostDTO.postRequestDTO requestDTO = PostDTO.postRequestDTO.builder()
                    .title("새 글 제목")
                    .content("새 글 내용")
                    .build();

            given(postRepository.save(any(Post.class))).willReturn(testPost);

            // When
            CommonDTO.IsSuccessDTO result = postService.write(requestDTO, request);

            // Then
            assertThat(result.getIsSuccess()).isTrue();
            verify(postRepository, times(1)).save(any(Post.class));
        }
    }

    // ==================== 게시글 목록 조회 테스트 ====================
    @Nested
    @DisplayName("게시글 목록 조회 테스트")
    class GetPostsTest {

        @Test
        @DisplayName("게시글 목록 조회 성공")
        void getPosts_Success() {
            // Given
            List<Post> posts = List.of(testPost);
            Page<Post> postPage = new PageImpl<>(posts, PageRequest.of(0, 4), 1);

            given(postRepository.findAllByOrderByCreateDateDesc(any(Pageable.class))).willReturn(postPage);
            given(postLikeService.getPostLikeCount(any(Post.class))).willReturn(5L);

            // When
            PostDTO.PageResponseDTO result = postService.getPosts(0, 4);

            // Then
            assertThat(result.getPosts()).hasSize(1);
            assertThat(result.getCurrentPage()).isEqualTo(0);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getPosts().get(0).getTitle()).isEqualTo("테스트 제목");
        }

        @Test
        @DisplayName("빈 게시글 목록 조회")
        void getPosts_Empty() {
            // Given
            Page<Post> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 4), 0);
            given(postRepository.findAllByOrderByCreateDateDesc(any(Pageable.class))).willReturn(emptyPage);

            // When
            PostDTO.PageResponseDTO result = postService.getPosts(0, 4);

            // Then
            assertThat(result.getPosts()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }

    // ==================== 게시글 상세 조회 테스트 ====================
    @Nested
    @DisplayName("게시글 상세 조회 테스트")
    class GetPostDetailTest {

        @Test
        @DisplayName("게시글 상세 조회 성공")
        void getPostDetail_Success() {
            // Given
            mockUserAuthentication();
            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));
            given(postLikeService.isPostLikedByUser(any(Post.class), any(User.class))).willReturn(true);
            given(postLikeService.getPostLikeCount(any(Post.class))).willReturn(10L);

            // When
            PostDTO.PostDetailResponseDTO result = postService.getPostDetail(1L, request);

            // Then
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("테스트 제목");
            assertThat(result.getContent()).isEqualTo("테스트 내용");
            assertThat(result.getUserName()).isEqualTo("테스트유저");
            assertThat(result.isLiked()).isTrue();
            assertThat(result.getLikeCount()).isEqualTo(10L);
        }

        @Test
        @DisplayName("존재하지 않는 게시글 조회 실패")
        void getPostDetail_NotFound() {
            // Given
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> postService.getPostDetail(999L, request))
                    .isInstanceOf(PostHandler.class);
        }
    }

    // ==================== 게시글 삭제 테스트 ====================
    @Nested
    @DisplayName("게시글 삭제 테스트")
    class DeletePostTest {

        @Test
        @DisplayName("게시글 삭제 성공")
        void deletePost_Success() {
            // Given
            mockUserAuthentication();
            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));
            given(postRepository.findByIdAndUserId(1L, testUser.getId())).willReturn(Optional.of(testPost));

            // When
            CommonDTO.IsSuccessDTO result = postService.deletePost(1L, request);

            // Then
            assertThat(result.getIsSuccess()).isTrue();
            verify(postRepository, times(1)).delete(testPost);
        }

        @Test
        @DisplayName("존재하지 않는 게시글 삭제 실패")
        void deletePost_NotFound() {
            // Given
            mockUserAuthentication();
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> postService.deletePost(999L, request))
                    .isInstanceOf(PostHandler.class);
        }

        @Test
        @DisplayName("다른 사용자의 게시글 삭제 실패")
        void deletePost_NotOwner() {
            // Given
            mockUserAuthentication();
            given(postRepository.findById(1L)).willReturn(Optional.of(testPost));
            given(postRepository.findByIdAndUserId(1L, testUser.getId())).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> postService.deletePost(1L, request))
                    .isInstanceOf(PostHandler.class);
        }
    }

    // ==================== 내 게시글 조회 테스트 ====================
    @Nested
    @DisplayName("내 게시글 조회 테스트")
    class GetMyPostsTest {

        @Test
        @DisplayName("내 게시글 목록 조회 성공")
        void getMyPosts_Success() {
            // Given
            mockUserAuthentication();
            List<Post> myPosts = List.of(testPost);
            Page<Post> postPage = new PageImpl<>(myPosts, PageRequest.of(0, 4), 1);

            given(postRepository.findPostsByUserOrderByCreateDateDesc(eq(testUser), any(Pageable.class)))
                    .willReturn(postPage);
            given(postLikeService.getPostLikeCount(any(Post.class))).willReturn(3L);

            // When
            PostDTO.PageResponseDTO result = postService.getMyPosts(0, 4, request);

            // Then
            assertThat(result.getPosts()).hasSize(1);
            assertThat(result.getPosts().get(0).getUserName()).isEqualTo("테스트유저");
        }
    }
}
