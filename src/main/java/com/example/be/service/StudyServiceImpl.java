package com.example.be.service;

import com.example.be.apiPayload.code.status.ErrorStatus;
import com.example.be.apiPayload.exception.handler.UserHandler;
import com.example.be.domain.User;
import com.example.be.repository.UserRepository;
import com.example.be.web.dto.CommonDTO;
import com.example.be.web.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudyServiceImpl {
    private final JwtUtilServiceImpl jwtUtilService;
    private final UserRepository userRepository;

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

    public CommonDTO.IsSuccessDTO addStudyTime(HttpServletRequest request, int time) {
        User user= getUserFromRequest(request);

        if (user != null) {
            user.setTodayStudyTime(user.getTodayStudyTime()+ time);
            user.setTotalStudyTime(user.getTotalStudyTime()+ time);
            userRepository.save(user);
        }

        return CommonDTO.IsSuccessDTO.builder().isSuccess(true).build();
    }

    public CommonDTO.IsSuccessDTO addStudyGoalTime(HttpServletRequest request, int time) {
        User user= getUserFromRequest(request);

        if (user != null) {
            user.setGoalStudyTime(time);
            userRepository.save(user);
        }

        return CommonDTO.IsSuccessDTO.builder().isSuccess(true).build();
    }

    public UserDTO.studyTimeResponseDto getStudyTime(HttpServletRequest request) {
        User user= getUserFromRequest(request);

        int today = user.getTodayStudyTime();
        int total = user.getTotalStudyTime();
        int goal = user.getGoalStudyTime();

        return UserDTO.studyTimeResponseDto.builder()
                .userId(user.getId())
                .todayStudyTime(today/60 +"시간 " + today%60+"분")
                .totalStudyTime(total/60 + "시간 " + total%60 + "분")
                .goalStudyTime(goal/60 + "시간 " + goal%60 + "분")
                .build();
    }

    //매일 자정에 오늘 공부 시간 초기화
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void resetTodayStudyTime() {
        userRepository.updateTodayStudyTime();
    }



}
