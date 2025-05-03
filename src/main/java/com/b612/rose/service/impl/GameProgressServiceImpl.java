package com.b612.rose.service.impl;

import com.b612.rose.dto.request.EmailRequest;
import com.b612.rose.dto.request.GameStageUpdateRequest;
import com.b612.rose.dto.request.StarActionRequest;
import com.b612.rose.dto.response.DialogueResponse;
import com.b612.rose.dto.response.GameProgressResponse;
import com.b612.rose.dto.response.GameStateResponse;
import com.b612.rose.entity.domain.GameProgress;
import com.b612.rose.entity.domain.InteractiveObject;
import com.b612.rose.entity.domain.Star;
import com.b612.rose.entity.domain.UserInteraction;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.entity.enums.InteractiveObjectType;
import com.b612.rose.entity.enums.StarType;
import com.b612.rose.exception.BusinessException;
import com.b612.rose.exception.ErrorCode;
import com.b612.rose.repository.*;
import com.b612.rose.service.service.DialogueService;
import com.b612.rose.service.service.EmailAsyncService;
import com.b612.rose.service.service.EmailService;
import com.b612.rose.service.service.GameProgressService;
import com.b612.rose.utils.GameStateManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameProgressServiceImpl implements GameProgressService {

    private final GameProgressRepository gameProgressRepository;
    private final UserRepository userRepository;
    private final StarRepository starRepository;
    private final DialogueService dialogueService;
    private final GameStateManager gameStateManager;
    private final EmailAsyncService emailAsyncService;

    // 게임 진척도 업데이트
    @Override
    @Transactional
    public GameStateResponse updateGameStage(UUID userId, GameStageUpdateRequest request) {
        GameProgress currentProgress = gameProgressRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GAME_PROGRESS_NOT_FOUND,
                        "게임 진척도를 찾을 수 없음. 사용자 ID: " + userId));

        GameStage newStage = request.getNewStage();
        gameStateManager.updateMemoryStage(userId, newStage);
        List<DialogueResponse> dialogues = dialogueService.getDialoguesForCurrentStage(userId, newStage);

        GameStateResponse response = GameStateResponse.builder()
                .userId(userId)
                .currentStage(newStage)
                .dialogues(dialogues)
                .build();

        updateGameStageAsync(userId, currentProgress.getProgressId(), newStage);
        return response;
    }

    // 별 수집 처리
    @Override
    @Transactional
    public GameStateResponse onStarCollected(UUID userId, StarActionRequest request) {
        StarType starType = request.getStarType();
        GameStage newStage = gameStateManager.getCollectStageForStar(starType);

        gameStateManager.updateMemoryStage(userId, newStage);
        gameStateManager.updateMemoryGameState(userId, starType, true, starType == StarType.PRIDE);

        List<DialogueResponse> dialogues = dialogueService.getDialoguesForCurrentStage(userId, newStage);

        GameStateResponse immediateResponse = GameStateResponse.builder()
                .userId(userId)
                .currentStage(newStage)
                .dialogues(dialogues)
                .build();

        processStarCollectionAsync(userId, request, newStage);

        return immediateResponse;
    }

    // 별 전달 처리
    @Override
    @Transactional
    public GameStateResponse onStarDelivered(UUID userId, StarActionRequest request) {
        StarType starType = request.getStarType();
        GameStage newStage = gameStateManager.getDeliverStageForStar(starType);

        gameStateManager.updateMemoryStage(userId, newStage);
        gameStateManager.updateMemoryGameState(userId, starType, true, true);

        List<DialogueResponse> dialogues = dialogueService.getDialoguesForCurrentStage(userId, newStage);

        GameStateResponse immediateResponse = GameStateResponse.builder()
                .userId(userId)
                .currentStage(newStage)
                .dialogues(dialogues)
                .build();

        processStarDeliveryAsync(userId, request, newStage);

        return immediateResponse;
    }

    @Override
    public GameStage getCurrentStage(UUID userId) {
        return gameStateManager.getCurrentStage(userId);
    }

    @Override
    public GameStateResponse getCurrentGameState(UUID userId) {
        GameStage currentStage = gameStateManager.getCurrentStage(userId);

        if (currentStage == null) {
            throw new BusinessException(ErrorCode.GAME_PROGRESS_NOT_FOUND,
                    "게임 진척도를 찾을 수 없음. userId: "+ userId);
        }

        List<DialogueResponse> dialogues = dialogueService.getDialoguesForCurrentStage(userId, currentStage);

        return GameStateResponse.builder()
                .userId(userId)
                .currentStage(currentStage)
                .dialogues(dialogues)
                .build();
    }

    // 게임 완료 처리, 이메일 전송 처리 -> 로직 꾸진 거 보니 고쳐야할듯
    @Override
    @Transactional
    public GameStateResponse completeGameAndSendEmail(UUID userId, EmailRequest request) {
        if (!gameStateManager.areAllStarsCollectedAndDelivered(userId)) {
            log.error("모든 별이 수집 및 전달되지 않았습니다. userId: {}", userId);
            throw new BusinessException(ErrorCode.STARS_NOT_COMPLETED,
                    "모든 별이 수집 및 전달되지 않았습니다.");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            log.error("이메일 주소가 비어있습니다. userId: {}", userId);
            throw new BusinessException(ErrorCode.EMAIL_REQUIRED,
                    "이메일 주소가 필요합니다.");
        }

        if (request.getSelectedNpc() == null || request.getSelectedNpc().isBlank()) {
            log.error("선택된 NPC가 없습니다. userId: {}", userId);
            throw new BusinessException(ErrorCode.NPC_SELECTION_REQUIRED,
                    "NPC를 선택해야 합니다.");
        }

        log.info("게임 완료 처리 - 사용자: {}, 이메일: {}, 선택한 NPC: {}",
                userId, request.getEmail(), request.getSelectedNpc());

        gameStateManager.updateMemoryStage(userId, GameStage.GAME_COMPLETE);
        gameStateManager.completeGame(userId, request.getEmail(), request.getConcern(), request.getSelectedNpc());
        GameStateResponse response = getCurrentGameState(userId);

        try {
            log.info("비동기 이메일 전송 요청 - 사용자: {}", userId);
            emailAsyncService.sendEmailAsync(userId, request);
        } catch (Exception e) {
            log.error("이메일 전송 요청 실패 - 사용자: {}, 오류: {}", userId, e.getMessage());
        }

        return response;
    }

    @Async("taskExecutor")
    protected void updateGameStageAsync(UUID userId, Integer progressId, GameStage newStage) {
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
    protected void processStarCollectionAsync(UUID userId, StarActionRequest request, GameStage newStage) {
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
    protected void processStarDeliveryAsync(UUID userId, StarActionRequest request, GameStage newStage) {
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
