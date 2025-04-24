package com.b612.rose.service.impl;

import com.b612.rose.dto.request.EmailRequest;
import com.b612.rose.service.service.EmailAsyncService;
import com.b612.rose.service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailAsyncServiceImpl implements EmailAsyncService {
    private final EmailService emailService;

    @Async("taskExecutor")
    @Override
    public void sendEmailAsync(UUID userId, EmailRequest request){
        try {
            log.info("비동기 이메일 전송 시작 - 사용자: {}, 이메일: {}", userId, request.getEmail());
            boolean result = emailService.sendEmail(userId, request);
            log.info("비동기 이메일 전송 완료 - 사용자: {}, 이메일: {}, 결과: {}",
                    userId, request.getEmail(), result ? "성공" : "실패");
        } catch (Exception e) {
            log.error("비동기 이메일 전송 처리 중 예외 발생 - 사용자: {}, 이메일: {}, 오류: {}",
                    userId, request.getEmail(), e.getMessage(), e);
        }
    }
}
