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

    public CommonDTO.IsSuccessDTO login(UserDTO.LoginRequestDto request, HttpServletResponse response) {
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


        // 쿠키에 액세스 토큰 추가
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);  // JavaScript에서 접근 불가능하게 설정
        accessTokenCookie.setSecure(false);    // HTTPS에서만 전송되도록 설정, https 적용 후 true로 설정 예정
        accessTokenCookie.setPath("/");       // 모든 경로에서 쿠키 접근 가능
        accessTokenCookie.setMaxAge((int) (ACCESS_TOKEN_EXPIRATION_TIME / 1000));  // 밀리초를 초로 변환
        response.addCookie(accessTokenCookie);

        // 쿠키에 리프레시 토큰 추가
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (REFRESH_TOKEN_EXPIRATION_TIME / 1000));
        response.addCookie(refreshTokenCookie);

        return CommonDTO.IsSuccessDTO.builder()
                .isSuccess(true)
                .build();
    }
}
