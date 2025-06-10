package com.example.be.web.controller;


import com.example.be.apiPayload.ApiResponse;
import com.example.be.service.RoomServiceImpl;
import com.example.be.web.dto.RoomDTO;
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomList;
import io.livekit.server.VideoGrant;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

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
}
