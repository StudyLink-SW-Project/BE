package com.example.be.service;

import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.apiPayload.exception.handler.UserHandler;
import com.example.be.domain.redis.RedisRefreshToken;
import com.example.be.domain.User;
import com.example.be.repository.redis.RedisRefreshTokenRepository;
import com.example.be.repository.UserRepository;
import com.example.be.web.dto.CommonDTO;
import com.example.be.web.dto.UserDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final JwtUtilServiceImpl jwtUtilService;
    private final RedisRefreshTokenRepository redisRefreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.access-token.expiration-time}")
    private long ACCESS_TOKEN_EXPIRATION_TIME; // 액세스 토큰 유효기간

    @Value("${jwt.refresh-token.expiration-time}")
    private long REFRESH_TOKEN_EXPIRATION_TIME; // 리프레쉬 토큰 유효기간


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

    public CommonDTO.IsSuccessDTO signUp(UserDTO.SingUpRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail()))
            throw new UserHandler(ErrorStatus._EXIST_EMAIL);

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .userId(UUID.randomUUID())
                .provider("general")
                .providerId(null)
                .createDate(ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime())
                .build();

        userRepository.save(user);

        return CommonDTO.IsSuccessDTO.builder()
                .isSuccess(true)
                .build();

    }

    public UserDTO.UserResponseDto login(UserDTO.LoginRequestDto request, HttpServletResponse response, HttpServletRequest httpRequest) {
        //db에 아이디랑 비밀번호가 일치하는지 조회
        // 일치한다면 토큰 발급 후 response

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(()
                -> new UserHandler(ErrorStatus._NOT_FOUND_USER));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new UserHandler(ErrorStatus._NOT_CORRECT_PASSWORD);

        redisRefreshTokenRepository.deleteById(user.getUserId().toString());

        // RefreshToken 재발급
        String refreshToken = jwtUtilService.generateRefreshToken(user.getUserId(), REFRESH_TOKEN_EXPIRATION_TIME);

        RedisRefreshToken newRefreshToken = RedisRefreshToken.of(
                user.getUserId(),
                refreshToken,
                REFRESH_TOKEN_EXPIRATION_TIME
        );

        redisRefreshTokenRepository.save(newRefreshToken);

        // AccessToken 발급
        String accessToken = jwtUtilService.generateAccessToken(user.getUserId(), ACCESS_TOKEN_EXPIRATION_TIME);

            response.addHeader("Set-Cookie",
                    String.format("accessToken=%s; Path=/; Domain=.studylink.store; Max-Age=%d; HttpOnly; Secure; SameSite=None",
                            accessToken, (int) (ACCESS_TOKEN_EXPIRATION_TIME / 1000)));
            response.addHeader("Set-Cookie",
                    String.format("refreshToken=%s; Path=/; Domain=.studylink.store; Max-Age=%d; HttpOnly; Secure; SameSite=None",
                            refreshToken, (int) (REFRESH_TOKEN_EXPIRATION_TIME / 1000)));

        // 사용자 정보 반환
        return UserDTO.UserResponseDto.builder()
                .userId(user.getId())
                .userName(user.getName())
                .email(user.getEmail())
                .loginType(user.getProvider())
                .build();
    }

    public UserDTO.UserResponseDto getUserInfo(String accessToken) {
        // 토큰이 없는 경우 처리
        if(accessToken == null) {
            throw new UserHandler(ErrorStatus._NOT_FOUND_USER);
        }

        // 토큰에서 사용자 ID 추출
        String userId = jwtUtilService.getUserIdFromToken(accessToken);

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
            response.addHeader("Set-Cookie",
                    "accessToken=; Path=/; Domain=.studylink.store; Max-Age=0; HttpOnly; Secure; SameSite=None");
            response.addHeader("Set-Cookie",
                    "refreshToken=; Path=/; Domain=.studylink.store; Max-Age=0; HttpOnly; Secure; SameSite=None");

        return CommonDTO.IsSuccessDTO.builder().isSuccess(true).build();
    }

    public CommonDTO.IsSuccessDTO createResolve(HttpServletRequest request, UserDTO.resolveDto resolve) {
        User user = getUserFromRequest(request);

        user.setResolve(resolve.getResolve());
        userRepository.save(user);

        return CommonDTO.IsSuccessDTO.builder().isSuccess(true).build();
    }

    public UserDTO.resolveDto getResolve(HttpServletRequest request) {
        User user = getUserFromRequest(request);

        String resolve = userRepository.findResolveByUserId(user.getId());
        if(resolve == null) {
            resolve = "";
        }

        return UserDTO.resolveDto.builder().resolve(resolve).build();
    }
}

