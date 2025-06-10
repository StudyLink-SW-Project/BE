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
            Room room = roomRepository.findByTitle(roomName);
            long createAt = event.getRoom().getCreationTime();
            System.out.println("LiveKit Webhook: " + event.toString());

            switch (event.toString()) {
                case "room_started" -> {
                    room = Room.builder()
                            .title(roomName)
                            .createDate(createAt)
                            .build();
                    roomRepository.save(room);
                }
                case "room_finished" -> roomRepository.delete(room);
                case "participant_joined" -> {
                    room.setParticipantCount(room.getParticipantCount() + 1);
                    roomRepository.save(room);
                }
                case "participant_left" -> {
                    room.setParticipantCount(room.getParticipantCount() - 1);
                    roomRepository.save(room);
                }
            }
        } catch (Exception e) {
            System.err.println("Error validating webhook event: " + e.getMessage());
        }
        return ResponseEntity.ok("ok");
    }
}
