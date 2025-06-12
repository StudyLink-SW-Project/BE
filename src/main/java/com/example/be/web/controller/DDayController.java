package com.example.be.web.controller;

import com.example.be.apiPayload.ApiResponse;
import com.example.be.service.DDayServiceImpl;
import com.example.be.web.dto.CommentDTO;
import com.example.be.web.dto.CommonDTO;
import com.example.be.web.dto.DDayDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/day")
@RequiredArgsConstructor
public class DDayController {
    private final DDayServiceImpl dayService;

    @PostMapping("")
    @Operation(summary = "디데이 추가 API", description = "디데이를 추가합니다.")
    public ApiResponse<CommonDTO.IsSuccessDTO> createDDay(
            HttpServletRequest request,
            @RequestBody DDayDTO.DDayRequestDto requestDto) {
        return ApiResponse.onSuccess(dayService.createDDay(request, requestDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "디데이 삭제 API", description = "디데이를 삭제합니다.")
    public ApiResponse<CommonDTO.IsSuccessDTO> deleteDDay(
            HttpServletRequest request, @PathVariable Long id) {
        return ApiResponse.onSuccess(dayService.deleteDDay(request, id));
    }
//
//    @PostMapping("")
//    @Operation(summary = "디데이 수정 API", description = "디데이를 수정합니다.")
//    public ApiResponse<CommonDTO.IsSuccessDTO> createComment(
//            @RequestBody CommentDTO.CommentRequestDTO requestDTO,
//            HttpServletRequest request) {
//        return ApiResponse.onSuccess(commentService.createComment(requestDTO, request));
//    }
//
    @GetMapping("")
    @Operation(summary = "디데이 조회 API", description = "디데이를 조회합니다.")
    public ApiResponse<List<DDayDTO.DDayResponseDto>> getDDay(
            HttpServletRequest request) {
        return ApiResponse.onSuccess(dayService.getDDays(request));
    }
}
