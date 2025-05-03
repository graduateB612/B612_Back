package com.b612.rose.service.impl;

import com.b612.rose.dto.request.StarActionRequest;
import com.b612.rose.entity.domain.GameProgress;
import com.b612.rose.entity.domain.Star;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.entity.enums.StarType;
import com.b612.rose.exception.BusinessException;
import com.b612.rose.exception.ErrorCode;
import com.b612.rose.repository.GameProgressRepository;
import com.b612.rose.repository.StarRepository;
import com.b612.rose.service.service.GameProgressAsyncService;
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
public class GameProgressAsyncServiceImpl implements GameProgressAsyncService {
    private final GameProgressRepository gameProgressRepository;
    private final StarRepository starRepository;
    private final GameStateManager gameStateManager;

    @Async("taskExecutor")
    @Transactional
    @Override
    public void updateGameStageAsync(UUID userId, Integer progressId, GameStage newStage) {
        try {
            GameProgress updatedProgress = GameProgress.builder()
                    .progressId(progressId)
                    .userId(userId)
                    .currentStage(newStage)
                    .build();

            gameProgressRepository.save(updatedProgress);

            if (newStage == GameStage.GAME_START) {
                gameStateManager.handleGameStart(userId);
            }

            log.info("비동기 게임 스테이지 업데이트 완료: userId={}, stage={}", userId, newStage);
        } catch (Exception e) {
            log.error("비동기 게임 스테이지 업데이트 실패: userId={}, stage={}, error={}",
                    userId, newStage, e.getMessage(), e);
        }
    }

    @Async("taskExecutor")
    @Transactional
    @Override
    public void processStarCollectionAsync(UUID userId, StarActionRequest request, GameStage newStage) {
        try {
            log.info("비동기 별 수집 처리 시작: userId={}, starType={}", userId, request.getStarType());

            Star star = starRepository.findByStarType(request.getStarType())
                    .orElseThrow(() -> new BusinessException(ErrorCode.STAR_NOT_FOUND,
                            "해당 별을 찾을 수 없음: " + request.getStarType()));

            gameStateManager.markStarAsCollected(userId, request.getStarType());

            if (request.getStarType() == StarType.PRIDE) {
                gameStateManager.markStarAsDelivered(userId, request.getStarType());
            }

            GameProgress currentProgress = gameProgressRepository.findByUserId(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.GAME_PROGRESS_NOT_FOUND,
                            "게임 진척도를 찾을 수 없음. 사용자 ID: "+userId));

            GameProgress updatedProgress = GameProgress.builder()
                    .progressId(currentProgress.getProgressId())
                    .userId(userId)
                    .currentStage(newStage)
                    .build();

            gameProgressRepository.save(updatedProgress);

            log.info("비동기 별 수집 처리 완료: userId={}, starType={}", userId, request.getStarType());
        } catch (Exception e) {
            log.error("비동기 별 수집 처리 실패: userId={}, starType={}, error={}",
                    userId, request.getStarType(), e.getMessage(), e);
        }
    }

    @Async("taskExecutor")
    @Transactional
    @Override
    public void processStarDeliveryAsync(UUID userId, StarActionRequest request, GameStage newStage) {
        try {
            log.info("비동기 별 전달 처리 시작: userId={}, starType={}", userId, request.getStarType());

            Star star = starRepository.findByStarType(request.getStarType())
                    .orElseThrow(() -> new BusinessException(ErrorCode.STAR_NOT_FOUND,
                            "해당 별을 찾을 수 없음: " + request.getStarType()));

            gameStateManager.markStarAsDelivered(userId, request.getStarType());

            GameProgress currentProgress = gameProgressRepository.findByUserId(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.GAME_PROGRESS_NOT_FOUND,
                            "게임 진척도를 찾을 수 없음. 사용자 ID: "+userId));

            GameProgress updatedProgress = GameProgress.builder()
                    .progressId(currentProgress.getProgressId())
                    .userId(userId)
                    .currentStage(newStage)
                    .build();

            gameProgressRepository.save(updatedProgress);

            log.info("비동기 별 전달 처리 완료: userId={}, starType={}", userId, request.getStarType());
        } catch (Exception e) {
            log.error("비동기 별 전달 처리 실패: userId={}, starType={}, error={}",
                    userId, request.getStarType(), e.getMessage(), e);
        }
    }
}
