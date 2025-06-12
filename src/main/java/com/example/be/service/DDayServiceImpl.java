package com.example.be.service;

import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.apiPayload.exception.handler.DDayHandler;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public List<DDayDTO.DDayResponseDto> getDDays(HttpServletRequest request) {
        User user = getUserFromRequest(request);
        List<DDay> dDayList = dayRepository.findDDayByUser(user);

        return dDayList.stream()
                .map(day -> DDayDTO.DDayResponseDto.builder()
                        .id(day.getId())
                        .name(day.getName())
                        .day(day.getDay())
                        .build())
                .collect(Collectors.toList());
    }

    public CommonDTO.IsSuccessDTO deleteDDay (HttpServletRequest request, Long id) {
        User user = getUserFromRequest(request);

        dayRepository.findById(id).orElseThrow(() -> new DDayHandler(ErrorStatus._NOT_FOUND_DDAY));
        DDay dDay = dayRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new DDayHandler(ErrorStatus._NOT_USER_DDAY));

        dayRepository.delete(dDay);

        return CommonDTO.IsSuccessDTO.builder().isSuccess(true).build();
    }
}
