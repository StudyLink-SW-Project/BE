package com.example.be.web.controller;

import com.example.be.apiPayload.ApiResponse;
import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.apiPayload.exception.handler.UserHandler;
import com.example.be.service.JwtUtilServiceImpl;
import com.example.be.service.UserServiceImpl;
import com.example.be.web.dto.CommonDTO;
import com.example.be.web.dto.UserDTO;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserServiceImpl userService;
    private final JwtUtilServiceImpl jwtUtilService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입 API")
    public ApiResponse<CommonDTO.IsSuccessDTO> signup(@RequestBody UserDTO.SingUpRequestDto request) {
        return ApiResponse.onSuccess(userService.signUp(request));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인 API")
    public ApiResponse<CommonDTO.IsSuccessDTO> login(@RequestBody UserDTO.LoginRequestDto dtoRequest, HttpServletResponse response, HttpServletRequest request) {
        return ApiResponse.onSuccess(userService.login(dtoRequest, response, request));
    }

    @GetMapping("/info")
    @Operation(summary = "유저 정보 반환 API")
    public ApiResponse<UserDTO.UserResponseDto> userInfo(HttpServletRequest request) {
        // 쿠키에서 액세스 토큰 추출은 서비스로 이동
        String accessToken = jwtUtilService.extractTokenFromCookie(request, "accessToken");

        // 사용자 정보 조회 로직도 서비스로 이동
        return ApiResponse.onSuccess(userService.getUserInfo(accessToken));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 API")
    public ApiResponse<CommonDTO.IsSuccessDTO> logout(HttpServletResponse response, HttpServletRequest request) {
        return ApiResponse.onSuccess(userService.logout(response, request));
    }
}