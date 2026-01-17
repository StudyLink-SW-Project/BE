package com.example.be.apiPayload.exception.handler;

import com.example.be.config.provider.GoogleUserInfo;
import com.example.be.config.provider.KakaoUserInfo;
import com.example.be.config.provider.NaverUserInfo;
import com.example.be.config.provider.OAuth2UserInfo;
import com.example.be.domain.redis.RedisRefreshToken;
import com.example.be.domain.User;
import com.example.be.repository.redis.RedisRefreshTokenRepository;
import com.example.be.repository.UserRepository;
import com.example.be.service.JwtUtilServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Value("${jwt.redirect}")
    private String REDIRECT_URI; // 프론트엔드로 Jwt 토큰을 리다이렉트할 URI

    @Value("${jwt.access-token.expiration-time}")
    private long ACCESS_TOKEN_EXPIRATION_TIME; // 액세스 토큰 유효기간

    @Value("${jwt.refresh-token.expiration-time}")
    private long REFRESH_TOKEN_EXPIRATION_TIME; // 리프레쉬 토큰 유효기간


    private OAuth2UserInfo oAuth2UserInfo = null;

    private final JwtUtilServiceImpl jwtUtil;
    private final UserRepository userRepository;
    private final RedisRefreshTokenRepository redisRefreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication; // 토큰
        final String provider = token.getAuthorizedClientRegistrationId(); // provider 추출

        // 구글 || 카카오 || 네이버 로그인 요청
        switch (provider) {
            case "google" -> {
                log.info("구글 로그인 요청");
                oAuth2UserInfo = new GoogleUserInfo(token.getPrincipal().getAttributes());
            }
            case "kakao" -> {
                log.info("카카오 로그인 요청");
                oAuth2UserInfo = new KakaoUserInfo(token.getPrincipal().getAttributes());
            }
            case "naver" -> {
                log.info("네이버 로그인 요청");
                oAuth2UserInfo = new NaverUserInfo((Map<String, Object>) token.getPrincipal().getAttributes().get("response"));
            }
        }

        // 정보 추출
        String providerId = oAuth2UserInfo.getProviderId();
        String name = oAuth2UserInfo.getName();
        String email = oAuth2UserInfo.getEmail();

        // 1. providerId로 기존 유저 찾기
        User existUser = userRepository.findByProviderId(providerId);
        User user;

        if (existUser != null) {
            // providerId로 찾은 기존 유저
            log.info("기존 유저입니다. (providerId 일치)");
            redisRefreshTokenRepository.deleteById(existUser.getUserId().toString());
            user = existUser;
        } else {
            // 2. providerId로 못 찾으면 email로 찾기
            Optional<User> userByEmail = userRepository.findByEmail(email);

            if (userByEmail.isPresent()) {
                // 같은 이메일로 다른 방식으로 가입한 유저 - 소셜 계정 연동
                log.info("기존 유저입니다. (email 일치 - 소셜 계정 연동)");
                user = userByEmail.get();
                user.setProvider(provider);
                user.setProviderId(providerId);
                userRepository.save(user);
                redisRefreshTokenRepository.deleteById(user.getUserId().toString());
            } else {
                // 신규 유저
                log.info("신규 유저입니다. 등록을 진행합니다.");
                user = User.builder()
                        .userId(UUID.randomUUID())
                        .email(email)
                        .name(name)
                        .createDate(LocalDateTime.now())
                        .provider(provider)
                        .providerId(providerId)
                        .build();
                userRepository.save(user);
            }
        }

        log.info("유저 이름 : {}", name);
        log.info("PROVIDER : {}", provider);
        log.info("PROVIDER_ID : {}", providerId);

        // 리프레쉬 토큰 발급 후 저장
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId(), REFRESH_TOKEN_EXPIRATION_TIME);

        RedisRefreshToken newRefreshToken = RedisRefreshToken.of(
                user.getUserId(),
                refreshToken,
                REFRESH_TOKEN_EXPIRATION_TIME
        );

        redisRefreshTokenRepository.save(newRefreshToken);

        // 액세스 토큰 발급
        String accessToken = jwtUtil.generateAccessToken(user.getUserId(), ACCESS_TOKEN_EXPIRATION_TIME);

        // Set-Cookie 헤더로 쿠키 설정 (Domain, SameSite 포함)
        response.addHeader("Set-Cookie",
                String.format("accessToken=%s; Path=/; Domain=.studylink.store; Max-Age=%d; HttpOnly; Secure; SameSite=None",
                        accessToken, (int) (ACCESS_TOKEN_EXPIRATION_TIME / 1000)));
        response.addHeader("Set-Cookie",
                String.format("refreshToken=%s; Path=/; Domain=.studylink.store; Max-Age=%d; HttpOnly; Secure; SameSite=None",
                        refreshToken, (int) (REFRESH_TOKEN_EXPIRATION_TIME / 1000)));

        response.sendRedirect(REDIRECT_URI + provider);

//        // 이름, 액세스 토큰, 리프레쉬 토큰을 담아 리다이렉트
//        String encodedName = URLEncoder.encode(name, "UTF-8");
//        String redirectUri = String.format(REDIRECT_URI, encodedName, accessToken, refreshToken);
//        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }
}