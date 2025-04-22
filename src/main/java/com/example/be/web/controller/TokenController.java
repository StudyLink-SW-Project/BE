package com.example.be.web.controller;


import com.example.be.apiPayload.ApiResponse;
import com.example.be.apiPayload.code.status.SuccessStatus;
import com.example.be.service.TokenServiceImpl;
import com.example.be.web.dto.TokenResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.token.TokenService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TokenController {

    private final TokenServiceImpl authService;


    // 액세스 토큰을 재발행하는 API
    @GetMapping("/reissue/access-token")
    @Operation(summary = "액세스 토큰 재발행 API")
    public ResponseEntity<ApiResponse<Object>> reissueAccessToken(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader) {

        TokenResponseDTO accessToken = authService.reissueAccessToken(authorizationHeader);
        return ApiResponse.onSuccess(SuccessStatus._CREATED_ACCESS_TOKEN, accessToken);
    }
}