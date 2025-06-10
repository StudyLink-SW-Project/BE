package com.example.be.web.controller;


import com.example.be.apiPayload.ApiResponse;
import com.example.be.service.RoomServiceImpl;
import com.example.be.web.dto.RoomDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {
    private final RoomServiceImpl roomService;


    @GetMapping("/rooms")
    @Operation(summary = "방 목록 조회", description = "생성된 모든 방 목록을 조회합니다.")
    public ApiResponse<RoomDTO.RoomListResponseDto> getAllRooms() {

        return ApiResponse.onSuccess(roomService.getAllRooms());
    }

    @PostMapping("/set")
    @Operation(summary = "비밀번호 및 이미지 저장", description = "생성된 방의 이미지번호와 비밀번호를 설정합니다.")
    public ApiResponse<RoomDTO.RoomDto> setRoom(@RequestBody RoomDTO.RoomSetRequestDto request) {

        return ApiResponse.onSuccess(roomService.setRoom(request));
    }
}
