package com.example.be.web.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
public class LoginController {

    @GetMapping("/login")
    public ResponseEntity<?> handleLoginRedirect(
            @RequestParam String name,
            @RequestParam String access_token,
            @RequestParam String refresh_token) {

        return ResponseEntity.ok("로그인 성공");
    }

}
