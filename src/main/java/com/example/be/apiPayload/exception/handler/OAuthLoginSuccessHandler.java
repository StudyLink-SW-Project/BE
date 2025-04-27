package com.example.be.apiPayload.exception.handler;

import com.example.be.config.provider.GoogleUserInfo;
import com.example.be.config.provider.KakaoUserInfo;
import com.example.be.config.provider.NaverUserInfo;
import com.example.be.config.provider.OAuth2UserInfo;
import com.example.be.domain.RefreshToken;
import com.example.be.domain.User;
import com.example.be.repository.RefreshTokenRepository;
import com.example.be.repository.UserRepository;
import com.example.be.service.JwtUtilServiceImpl;
import jakarta.servlet.http.Cookie;
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
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.Map;
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
    private final RefreshTokenRepository refreshTokenRepository;

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

        User existUser = userRepository.findByProviderId(providerId);
        User user;

        if (existUser == null) {
            // 신규 유저인 경우
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
        } else {
            // 기존 유저인 경우
            log.info("기존 유저입니다.");
            refreshTokenRepository.deleteByUserId(existUser.getUserId());
            user = existUser;
        }

        log.info("유저 이름 : {}", name);
        log.info("PROVIDER : {}", provider);
        log.info("PROVIDER_ID : {}", providerId);

        // 리프레쉬 토큰 발급 후 저장
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId(), REFRESH_TOKEN_EXPIRATION_TIME);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .userId(user.getUserId())
                .token(refreshToken)
                .build();

        refreshTokenRepository.save(newRefreshToken);

        // 액세스 토큰 발급
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

//        // 이름, 액세스 토큰, 리프레쉬 토큰을 담아 리다이렉트
//        String encodedName = URLEncoder.encode(name, "UTF-8");
//        String redirectUri = String.format(REDIRECT_URI, encodedName, accessToken, refreshToken);
//        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }
}