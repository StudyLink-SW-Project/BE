package com.example.be.domain.redis;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.UUID;

@RedisHash(value = "refreshToken")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class RedisRefreshToken {

    @Id
    private String userId; // UUID를 String으로 저장

    private String token;

    @TimeToLive
    private Long expiration; // TTL (초 단위)

    public static RedisRefreshToken of(UUID userId, String token, long expirationMillis) {
        return RedisRefreshToken.builder()
                .userId(userId.toString())
                .token(token)
                .expiration(expirationMillis / 1000) // 밀리초를 초로 변환
                .build();
    }
}
