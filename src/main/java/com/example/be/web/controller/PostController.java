package com.example.be.web.controller;

import com.example.be.apiPayload.ApiResponse;
import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.apiPayload.exception.handler.UserHandler;
import com.example.be.domain.User;
import com.example.be.repository.UserRepository;
import com.example.be.service.JwtUtilServiceImpl;
import com.example.be.service.PostServiceImpl;
import com.example.be.web.dto.CommonDTO;
import com.example.be.web.dto.PostDTO;
import com.example.be.web.dto.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostServiceImpl postService;

    @PostMapping("/write")
    @Operation(summary = "게시글 작성 API")
    public ApiResponse<CommonDTO.IsSuccessDTO> write(@RequestBody PostDTO.postRequestDTO request, HttpServletRequest req) {

        return ApiResponse.onSuccess(postService.write(request, req));
    }

}
