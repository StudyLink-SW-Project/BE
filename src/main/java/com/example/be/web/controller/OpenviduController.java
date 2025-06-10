package com.example.be.web.controller;

import com.example.be.domain.Room;
import com.example.be.repository.RoomRepository;
import com.example.be.web.dto.OpenviduDTO;
import io.livekit.server.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import livekit.LivekitWebhook.WebhookEvent;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/video")
@RequiredArgsConstructor
public class OpenviduController {

    @Value("${livekit.api.key}")
    private String LIVEKIT_API_KEY;

    @Value("${livekit.api.secret}")
    private String LIVEKIT_API_SECRET;

    private final RoomRepository roomRepository;


    @PostMapping(value = "/token")
    public ResponseEntity<Map<String, String>> createToken(@RequestBody OpenviduDTO.TokenRequestDto request) {
        String roomName = request.getRoomName();
        String participantName = request.getParticipantName();

        if (roomName == null || participantName == null) {
            return ResponseEntity.badRequest().body(Map.of("errorMessage", "roomName and participantName are required"));
        }

        AccessToken token = new AccessToken(LIVEKIT_API_KEY, LIVEKIT_API_SECRET);
        token.setName(participantName);
        token.setIdentity(participantName);
        token.addGrants(new RoomJoin(true), new RoomName(roomName));

        return ResponseEntity.ok(Map.of("token", token.toJwt()));
    }

        @PostMapping(value = "/livekit/webhook", consumes = "application/webhook+json")
        public ResponseEntity<String> receiveWebhook(@RequestHeader("Authorization") String authHeader, @RequestBody String body) {
            WebhookReceiver webhookReceiver = new WebhookReceiver(LIVEKIT_API_KEY, LIVEKIT_API_SECRET);
            try {
                WebhookEvent event = webhookReceiver.receive(body, authHeader);
                String roomName = event.getRoom().getName();
                long createAt = event.getRoom().getCreationTime();

                System.out.println("LiveKit Webhook Event: " + event.getEvent());
                System.out.println("Room Name: " + roomName);

                // 이벤트 타입에 따른 처리 - event.getEvent() 사용
                String eventType = event.getEvent();

                switch (eventType) {
                    case "room_started" -> {
                        // 기존 방이 있는지 확인 후 생성
                        Room existingRoom = roomRepository.findByTitle(roomName);
                        if (existingRoom == null) {
                            Room newRoom = Room.builder()
                                    .title(roomName)
                                    .createDate(createAt)
                                    .participantCount(0)
                                    .maxParticipants("10") // 기본값 설정
                                    .build();
                            roomRepository.save(newRoom);
                            System.out.println("Room created: " + roomName);
                        }
                    }
                    case "room_finished" -> {
                        Room room = roomRepository.findByTitle(roomName);
                        if (room != null) {
                            roomRepository.delete(room);
                            System.out.println("Room deleted: " + roomName);
                        }
                    }
                    case "participant_joined" -> {
                        Room room = roomRepository.findByTitle(roomName);
                        if (room != null) {
                            room.setParticipantCount(room.getParticipantCount() + 1);
                            roomRepository.save(room);
                            System.out.println("Participant joined. Current count: " + room.getParticipantCount());
                        }
                    }
                    case "participant_left" -> {
                        Room room = roomRepository.findByTitle(roomName);
                        if (room != null && room.getParticipantCount() > 0) {
                            room.setParticipantCount(room.getParticipantCount() - 1);
                            roomRepository.save(room);
                            System.out.println("Participant left. Current count: " + room.getParticipantCount());
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error validating webhook event: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.badRequest().body("Invalid webhook");
            }

            return ResponseEntity.ok("ok");
        }
    }