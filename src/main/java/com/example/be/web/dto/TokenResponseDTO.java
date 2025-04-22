package com.example.be.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TokenResponseDTO {
    @JsonProperty("access_token")
    private String accessToken;
}