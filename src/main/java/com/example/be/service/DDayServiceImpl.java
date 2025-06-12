package com.example.be.service;

import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.apiPayload.exception.handler.UserHandler;
import com.example.be.domain.DDay;
import com.example.be.domain.User;
import com.example.be.repository.DDayRepository;
import com.example.be.repository.UserRepository;
import com.example.be.web.dto.CommonDTO;
import com.example.be.web.dto.DDayDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DDayServiceImpl {
    private final UserRepository userRepository;
    private final JwtUtilServiceImpl jwtUtilService;
    private final DDayRepository dayRepository;

    private User getUserFromRequest(HttpServletRequest request) {
        try {
            String accessToken = jwtUtilService.extractTokenFromCookie(request, "accessToken");
            if (accessToken != null) {
                String userId = jwtUtilService.getUserIdFromToken(accessToken);
                return userRepository.findByUserId(UUID.fromString(userId)).orElse(null);
            }
        } catch (Exception e) {
            throw new UserHandler(ErrorStatus._NOT_FOUND_COOKIE);
        }
        return null;
    }

    public CommonDTO.IsSuccessDTO createDDay(HttpServletRequest request, DDayDTO.DDayRequestDto requestDto) {
        User user = getUserFromRequest(request);

        DDay dDay = DDay.builder()
                .name(requestDto.getName())
                .day(requestDto.getDay())
                .user(user)
                .build();

        dayRepository.save(dDay);

        return CommonDTO.IsSuccessDTO.builder().isSuccess(true).build();
    }

}
