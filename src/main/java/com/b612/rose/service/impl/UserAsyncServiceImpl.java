package com.b612.rose.service.impl;

import com.b612.rose.entity.domain.GameProgress;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.repository.GameProgressRepository;
import com.b612.rose.service.service.UserAsyncService;
import com.b612.rose.utils.GameStateManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAsyncServiceImpl implements UserAsyncService {

    private final GameProgressRepository gameProgressRepository;
    private final GameStateManager gameStateManager;

    @Async("taskExecutor")
    @Transactional
    @Override
    public void initializeGameStateAsync(UUID userId) {
        try {
            log.info("비동기 게임 상태 초기화 시작: userId={}", userId);

            // 게임 진행 상태 생성
            GameProgress newProgress = GameProgress.builder()
                    .userId(userId)
                    .currentStage(GameStage.INTRO)
                    .build();
            gameProgressRepository.save(newProgress);

            // 게임 시작 시 필요한 별, 상호작용 등 초기화
            gameStateManager.handleGameStart(userId);

            log.info("비동기 게임 상태 초기화 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("비동기 게임 상태 초기화 실패: userId={}, error={}",
                    userId, e.getMessage(), e);
        }
    }
}
