package com.example.be.web.controller;

import com.example.be.apiPayload.ApiResponse;
import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.apiPayload.exception.handler.UserHandler;
import com.example.be.domain.User;
import com.example.be.repository.UserRepository;
import com.example.be.service.JwtUtilServiceImpl;
import com.example.be.service.UserServiceImpl;
import com.example.be.web.dto.CommonDTO;
import com.example.be.web.dto.UserDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.metrics.SystemMetricsAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserServiceImpl userService;
    private final SystemMetricsAutoConfiguration systemMetricsAutoConfiguration;
    private final JwtUtilServiceImpl jwtUtilServiceImpl;
    private final UserRepository userRepository;

    @Hidden
    @GetMapping("/socialLogin")
    public ResponseEntity<?> handleLoginRedirect(
            @RequestParam String name,
            @RequestParam String access_token,
            @RequestParam String refresh_token) {

        return ResponseEntity.ok("로그인 성공");
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입 API")
    public ApiResponse<CommonDTO.IsSuccessDTO> signup(@RequestBody UserDTO.SingUpRequestDto request) {

        return ApiResponse.onSuccess(userService.signUp(request));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인 API")
    public ApiResponse<CommonDTO.IsSuccessDTO> login(@RequestBody UserDTO.LoginRequestDto request, HttpServletResponse response) {

        return ApiResponse.onSuccess(userService.login(request, response));
    }

    @PostMapping("/info")
    @Operation(summary = "유저 정보 반환 API")
    public ApiResponse<UserDTO.UserResponseDto> userInfo(HttpServletRequest request) {
        String accessToken = null;
        Cookie[] cookies = request.getCookies();

        if(cookies != null) {
            for(Cookie cookie : cookies) {
                if(cookie.getName().equals("accessToken")) {
                    accessToken = cookie.getValue();
                }
            }
        }
        System.out.println(accessToken);

        // 토큰이 없는 경우 처리
        if(accessToken == null) {
            throw new UserHandler(ErrorStatus._NOT_FOUND_USER);
        }

        // 토큰에서 사용자 ID 추출
        String userId = jwtUtilServiceImpl.getUserIdFromToken(accessToken);

        // 사용자 정보 조회
        User user = userRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new UserHandler(ErrorStatus._NOT_FOUND_USER));

        // UserResponseDto로 변환하여 반환
        UserDTO.UserResponseDto userResponseDto = UserDTO.UserResponseDto.builder()
                .userId(user.getId())
                .userName(user.getName())
                .email(user.getEmail())
                .loginType(user.getProvider())
                .build();

        return ApiResponse.onSuccess(userResponseDto); }

}
