package com.example.be.web.controller;

import com.example.be.apiPayload.ApiResponse;
import com.example.be.apiPayload.code.status.SuccessStatus;
import com.example.be.service.UserServiceImpl;
import com.example.be.web.dto.CommonDTO;
import com.example.be.web.dto.UserDTO;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserServiceImpl userService;

    @Hidden
    @GetMapping("/login")
    public ResponseEntity<?> handleLoginRedirect(
            @RequestParam String name,
            @RequestParam String access_token,
            @RequestParam String refresh_token) {

        return ResponseEntity.ok("로그인 성공");
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입 API")
    public ApiResponse<CommonDTO.IsSuccessDTO> signup(@RequestBody UserDTO.SingUpRequestDto requestDto) {

        return ApiResponse.onSuccess(userService.signUp(requestDto));
    }
}
