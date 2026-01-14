package com.example.be.service;


import com.example.be.apiPayload.exception.tokenException.TokenErrorResult;
import com.example.be.apiPayload.exception.tokenException.TokenException;
import com.example.be.domain.redis.RedisRefreshToken;
import com.example.be.repository.redis.RedisRefreshTokenRepository;
import com.example.be.web.dto.TokenResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.token.TokenService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl{
    @Value("${jwt.access-token.expiration-time}")
    private long ACCESS_TOKEN_EXPIRATION_TIME; // 액세스 토큰 유효기간

    private final RedisRefreshTokenRepository redisRefreshTokenRepository;
    private final JwtUtilServiceImpl jwtUtil;


    //액세스 토큰 재발급
    public TokenResponseDTO reissueAccessToken(String authorizationHeader) {
        String refreshToken = jwtUtil.getTokenFromHeader(authorizationHeader);
        String userId = jwtUtil.getUserIdFromToken(refreshToken);

        // Redis에서 RefreshToken 조회
        RedisRefreshToken existRefreshToken = redisRefreshTokenRepository.findById(userId)
                .orElseThrow(() -> new TokenException(TokenErrorResult.INVALID_REFRESH_TOKEN));

        String accessToken = null;

        if (!existRefreshToken.getToken().equals(refreshToken) || jwtUtil.isTokenExpired(refreshToken)) {
            // 리프레쉬 토큰이 다르거나, 만료된 경우
            throw new TokenException(TokenErrorResult.INVALID_REFRESH_TOKEN); // 401 에러를 던져 재로그인을 요청
        } else {
            // 액세스 토큰 재발급
            accessToken = jwtUtil.generateAccessToken(UUID.fromString(userId), ACCESS_TOKEN_EXPIRATION_TIME);
        }

        return TokenResponseDTO.builder()
                .accessToken(accessToken)
                .build();
    }
}