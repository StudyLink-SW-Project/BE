package com.example.be.config;

import io.livekit.server.RoomServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiveKitConfig {

    @Value("${livekit.api.key}")
    private String LIVEKIT_API_KEY;

    @Value("${livekit.api.secret}")
    private String LIVEKIT_API_SECRET;

    @Value("${livekit.api.host}")
    private String LIVEKIT_API_HOST;

    @Bean
    public RoomServiceClient roomServiceClient() {
        return RoomServiceClient.create(
                LIVEKIT_API_HOST,
                LIVEKIT_API_KEY,
                LIVEKIT_API_SECRET
        );
    }
}
