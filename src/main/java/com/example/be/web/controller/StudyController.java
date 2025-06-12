package com.example.be.web.controller;

import com.example.be.apiPayload.ApiResponse;
import com.example.be.service.StudyServiceImpl;
import com.example.be.service.UserServiceImpl;
import com.example.be.web.dto.CommonDTO;
import com.example.be.web.dto.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyServiceImpl studyService;

    @PostMapping("/{time}")
    @Operation(summary = "오늘, 누적 공부 시간 추가 API")
    public ApiResponse<CommonDTO.IsSuccessDTO> addStudyTime(HttpServletRequest request, @PathVariable("time")int time) {
        return ApiResponse.onSuccess(studyService.addStudyTime(request, time));
    }

    @PostMapping("/goal/{time}")
    @Operation(summary = "목표 공부 시간 수정 API")
    public ApiResponse<CommonDTO.IsSuccessDTO> addStudyGoalTime(HttpServletRequest request, @PathVariable("time")int time) {
        return ApiResponse.onSuccess(studyService.addStudyGoalTime(request, time));
    }

    @GetMapping("/time")
    @Operation(summary = "오늘, 누적, 목표 공부 시간 조회 API")
    public ApiResponse<UserDTO.studyTimeResponseDto> getStudyTime(HttpServletRequest request) {
        return ApiResponse.onSuccess(studyService.getStudyTime(request));
    }


}
