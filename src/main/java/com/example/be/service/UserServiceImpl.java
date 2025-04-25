package com.example.be.service;

import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.apiPayload.exception.handler.UserHandler;
import com.example.be.domain.User;
import com.example.be.repository.UserRepository;
import com.example.be.web.dto.CommonDTO;
import com.example.be.web.dto.UserDTO;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl {
    private final UserRepository userRepository;

    public CommonDTO.IsSuccessDTO signUp(UserDTO.SingUpRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail()))
            throw new UserHandler(ErrorStatus._EXIST_EMAIL);

        User user = User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .name(request.getName())
                .userId(UUID.randomUUID())
                .provider("general")
                .providerId(null)
                .createDate(LocalDateTime.now())
                .build();

        userRepository.save(user);

        return CommonDTO.IsSuccessDTO.builder()
                .isSuccess(true)
                .build();

    }
}
