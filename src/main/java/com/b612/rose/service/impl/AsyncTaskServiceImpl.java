package com.b612.rose.service.impl;

import com.b612.rose.dto.request.EmailRequest;
import com.b612.rose.dto.request.StarActionRequest;
import com.b612.rose.entity.domain.GameProgress;
import com.b612.rose.entity.domain.InteractiveObject;
import com.b612.rose.entity.domain.Star;
import com.b612.rose.entity.domain.UserInteraction;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.entity.enums.InteractiveObjectType;
import com.b612.rose.entity.enums.StarType;
import com.b612.rose.exception.BusinessException;
import com.b612.rose.exception.ErrorCode;
import com.b612.rose.repository.GameProgressRepository;
import com.b612.rose.repository.InteractiveObjectRepository;
import com.b612.rose.repository.StarRepository;
import com.b612.rose.repository.UserInteractionRepository;
import com.b612.rose.service.service.AsyncTaskService;
import com.b612.rose.service.service.EmailService;
import com.b612.rose.utils.GameStateManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncTaskServiceImpl implements AsyncTaskService {

    private final GameProgressRepository gameProgressRepository;
    private final StarRepository starRepository;
    private final InteractiveObjectRepository interactiveObjectRepository;
    private final UserInteractionRepository userInteractionRepository;
    private final GameStateManager gameStateManager;
    private final EmailService emailService;

    // 사용자 게임 상태 초기화
    @Async("taskExecutor")
    @Transactional
    @Override
    public void initializeGameStateAsync(UUID userId) {
        executeAsyncTask("게임 상태 초기화", userId, () -> {
            gameStateManager.handleGameStart(userId);
        });
    }

    // 게임 스테이지 업데이트
    @Async("taskExecutor")
    @Transactional
    @Override
    public void updateGameStageAsync(UUID userId, Integer progressId, GameStage newStage) {
        executeAsyncTask("게임 스테이지 업데이트", userId, () -> {
            GameProgress updatedProgress = GameProgress.builder()
                    .progressId(progressId)
                    .userId(userId)
                    .currentStage(newStage)
                    .build();
            gameProgressRepository.save(updatedProgress);
        });
    }

    // 별 수집 처리
    @Async("taskExecutor")
    @Transactional
    @Override
    public void processStarCollectionAsync(UUID userId, StarActionRequest request, GameStage newStage) {
        executeAsyncTask("별 수집 처리", userId, () -> {
            Star star = starRepository.findByStarType(request.getStarType())
                    .orElseThrow(() -> new BusinessException(ErrorCode.STAR_NOT_FOUND,
                            "해당 별을 찾을 수 없음: " + request.getStarType()));

            gameStateManager.markStarAsCollected(userId, request.getStarType());

            // PRIDE 별은 수집과 동시에 전달 처리
            if (request.getStarType() == StarType.PRIDE) {
                gameStateManager.markStarAsDelivered(userId, request.getStarType());
            }

            updateGameStage(userId, newStage);
        });
    }

    // 별 전달 처리
    @Async("taskExecutor")
    @Transactional
    @Override
    public void processStarDeliveryAsync(UUID userId, StarActionRequest request, GameStage newStage) {
        executeAsyncTask("별 전달 처리", userId, () -> {
            Star star = starRepository.findByStarType(request.getStarType())
                    .orElseThrow(() -> new BusinessException(ErrorCode.STAR_NOT_FOUND,
                            "해당 별을 찾을 수 없음: " + request.getStarType()));

            gameStateManager.markStarAsDelivered(userId, request.getStarType());
            updateGameStage(userId, newStage);
        });
    }

    // 상호작용 기록 업데이트
    @Async("taskExecutor")
    @Transactional
    @Override
    public void updateInteractionAsync(UUID userId, InteractiveObjectType objectType) {
        executeAsyncTask("사용자 상호작용 기록", userId, () -> {
            InteractiveObject object = interactiveObjectRepository.findByObjectType(objectType)
                    .orElseThrow(() -> new BusinessException(ErrorCode.OBJECT_NOT_FOUND,
                            "오브젝트를 찾을 수 없습니다: " + objectType));

            UserInteraction interaction = userInteractionRepository
                    .findByUserIdAndObjectId(userId, object.getObjectId())
                    .orElse(null);

            if (interaction == null) {
                interaction = UserInteraction.builder()
                        .userId(userId)
                        .objectId(object.getObjectId())
                        .hasInteracted(true)
                        .isActive(true)
                        .interactedAt(LocalDateTime.now())
                        .build();
            } else {
                interaction = UserInteraction.builder()
                        .interactionId(interaction.getInteractionId())
                        .userId(interaction.getUserId())
                        .objectId(interaction.getObjectId())
                        .hasInteracted(true)
                        .isActive(interaction.isActive())
                        .interactedAt(LocalDateTime.now())
                        .build();
            }

            userInteractionRepository.save(interaction);
        });
    }

    // 이메일 전송
    @Async("taskExecutor")
    @Override
    public void sendEmailAsync(UUID userId, EmailRequest request) {
        executeAsyncTask("이메일 전송", userId, () -> {
            boolean result = emailService.sendEmail(userId, request);
            log.info("이메일 전송 결과: {}", result ? "성공" : "실패");
        });
    }

    // 공통 비동기 작업 실행 템플릿
    private void executeAsyncTask(String taskName, UUID userId, Runnable task) {
        try {
            log.info("비동기 {} 시작: userId={}", taskName, userId);
            task.run();
            log.info("비동기 {} 완료: userId={}", taskName, userId);
        } catch (Exception e) {
            log.error("비동기 {} 실패: userId={}, error={}", taskName, userId, e.getMessage(), e);
        }
    }

    // 게임 스테이지 업데이트 헬퍼 메서드
    private void updateGameStage(UUID userId, GameStage newStage) {
        GameProgress currentProgress = gameProgressRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GAME_PROGRESS_NOT_FOUND,
                        "게임 진척도를 찾을 수 없음. 사용자 ID: " + userId));

        GameProgress updatedProgress = GameProgress.builder()
                .progressId(currentProgress.getProgressId())
                .userId(userId)
                .currentStage(newStage)
                .build();

        gameProgressRepository.save(updatedProgress);
    }
} 