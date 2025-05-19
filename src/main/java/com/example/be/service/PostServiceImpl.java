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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class PostServiceImpl {
    private final JwtUtilServiceImpl jwtUtilService;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

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
}
