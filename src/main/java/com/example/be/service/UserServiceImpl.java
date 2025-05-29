package com.example.be.service;

import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.apiPayload.exception.handler.UserHandler;
import com.example.be.domain.RefreshToken;
import com.example.be.domain.User;
import com.example.be.domain.enums.LoginType;
import com.example.be.repository.RefreshTokenRepository;
import com.example.be.repository.UserRepository;
import com.example.be.web.dto.CommonDTO;
import com.example.be.web.dto.UserDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final JwtUtilServiceImpl jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.access-token.expiration-time}")
    private long ACCESS_TOKEN_EXPIRATION_TIME; // 액세스 토큰 유효기간

    @Value("${jwt.refresh-token.expiration-time}")
    private long REFRESH_TOKEN_EXPIRATION_TIME; // 리프레쉬 토큰 유효기간

    public CommonDTO.IsSuccessDTO signUp(UserDTO.SingUpRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail()))
            throw new UserHandler(ErrorStatus._EXIST_EMAIL);

        User user = User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .name(request.getName())
                .userId(UUID.randomUUID())
                .provider("general")
                .providerId(null)
                .createDate(LocalDateTime.now())
                .build();

        userRepository.save(user);

        return CommonDTO.IsSuccessDTO.builder()
                .isSuccess(true)
                .build();

    }

    public CommonDTO.IsSuccessDTO login(UserDTO.LoginRequestDto request, HttpServletResponse response, HttpServletRequest httpRequest) {
        //db에 아이디랑 비밀번호가 일치하는지 조회
        // 일치한다면 토큰 발급 후 response

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(()
                -> new UserHandler(ErrorStatus._NOT_FOUND_USER));

        if (!user.getPassword().equals(request.getPassword()))
            throw new UserHandler(ErrorStatus._NOT_CORRECT_PASSWORD);

        refreshTokenRepository.deleteByUserId(user.getUserId());

        // RefreshToken 재발급
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId(), REFRESH_TOKEN_EXPIRATION_TIME);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .userId(user.getUserId())
                .token(refreshToken)
                .build();

        refreshTokenRepository.save(newRefreshToken);

        // AccessToken 발급
        String accessToken = jwtUtil.generateAccessToken(user.getUserId(), ACCESS_TOKEN_EXPIRATION_TIME);

        String origin = httpRequest.getHeader("Origin");
        boolean isSecure = origin == null || !origin.contains("localhost");

// 액세스 토큰
        if (isSecure) {
            // 배포 환경: Secure + SameSite=None
            response.addHeader("Set-Cookie",
                    String.format("accessToken=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=None",
                            accessToken, (int) (ACCESS_TOKEN_EXPIRATION_TIME / 1000)));
        } else {
            // 로컬 환경: SameSite=None (Secure 없음)
            response.addHeader("Set-Cookie",
                    String.format("accessToken=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=None",
                            accessToken, (int) (ACCESS_TOKEN_EXPIRATION_TIME / 1000)));
        }

// 리프레시 토큰
        if (isSecure) {
            response.addHeader("Set-Cookie",
                    String.format("refreshToken=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=None",
                            refreshToken, (int) (REFRESH_TOKEN_EXPIRATION_TIME / 1000)));
        } else {
            response.addHeader("Set-Cookie",
                    String.format("refreshToken=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=None",
                            refreshToken, (int) (REFRESH_TOKEN_EXPIRATION_TIME / 1000)));
        }

        return CommonDTO.IsSuccessDTO.builder().isSuccess(true).build();
    }

    public UserDTO.UserResponseDto getUserInfo(String accessToken) {
        // 토큰이 없는 경우 처리
        if(accessToken == null) {
            throw new UserHandler(ErrorStatus._NOT_FOUND_USER);
        }

        // 토큰에서 사용자 ID 추출
        String userId = jwtUtil.getUserIdFromToken(accessToken);

        // 사용자 정보 조회
        User user = userRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new UserHandler(ErrorStatus._NOT_FOUND_USER));

        // UserResponseDto로 변환하여 반환
        return UserDTO.UserResponseDto.builder()
                .userId(user.getId())
                .userName(user.getName())
                .email(user.getEmail())
                .loginType(user.getProvider())
                .build();
    }


    public CommonDTO.IsSuccessDTO logout(HttpServletResponse response, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if(cookies == null) {
            throw new UserHandler(ErrorStatus._NOT_FOUND_COOKIE);
        }

        // Origin 헤더로 환경 판단
        String origin = request.getHeader("Origin");
        boolean isSecure = origin == null || !origin.contains("localhost");

        // 쿠키 삭제 - addHeader 방식 사용
        if (isSecure) {
            // 배포 환경
            response.addHeader("Set-Cookie",
                    "accessToken=; Path=/; Max-Age=0; HttpOnly; Secure; SameSite=None");
            response.addHeader("Set-Cookie",
                    "refreshToken=; Path=/; Max-Age=0; HttpOnly; Secure; SameSite=None");
        } else {
            // 로컬 환경
            response.addHeader("Set-Cookie",
                    "accessToken=; Path=/; Max-Age=0; HttpOnly; SameSite=None");
            response.addHeader("Set-Cookie",
                    "refreshToken=; Path=/; Max-Age=0; HttpOnly; SameSite=None");
        }

        return CommonDTO.IsSuccessDTO.builder().isSuccess(true).build();
    }

}
