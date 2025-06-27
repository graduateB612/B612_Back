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
import com.b612.rose.exception.ExceptionUtils;
import com.b612.rose.repository.*;
import com.b612.rose.service.service.*;
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
    private final GameProgressAsyncService gameProgressAsyncService;

    // 게임 진척도 업데이트
    @Override
    @Transactional
    public GameStateResponse updateGameStage(UUID userId, GameStageUpdateRequest request) {
        GameProgress currentProgress = ExceptionUtils.getGameProgressOrThrow(
                gameProgressRepository.findByUserId(userId), userId);

        GameStage newStage = request.getNewStage();
        gameStateManager.updateMemoryStage(userId, newStage);
        List<DialogueResponse> dialogues = dialogueService.getDialoguesForCurrentStage(userId, newStage);

        GameStateResponse response = GameStateResponse.builder()
                .userId(userId)
                .currentStage(newStage)
                .dialogues(dialogues)
                .build();

        gameProgressAsyncService.updateGameStageAsync(userId, currentProgress.getProgressId(), newStage);
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

        gameProgressAsyncService.processStarCollectionAsync(userId, request, newStage);

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

        gameProgressAsyncService.processStarDeliveryAsync(userId, request, newStage);

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
        ExceptionUtils.validateAllStarsCompleted(
                gameStateManager.areAllStarsCollectedAndDelivered(userId));
        ExceptionUtils.validateEmailProvided(request.getEmail());
        ExceptionUtils.validateNpcSelected(request.getSelectedNpc());

        log.info("게임 완료 처리 - 사용자: {}, 이메일: {}, 선택한 NPC: {}",
                userId, request.getEmail(), request.getSelectedNpc());

        gameStateManager.updateMemoryStage(userId, GameStage.GAME_COMPLETE);
        gameStateManager.completeGame(userId, request.getEmail(), request.getConcern(), request.getSelectedNpc());
        GameStateResponse response = getCurrentGameState(userId);

        emailAsyncService.sendEmailAsync(userId, request);

        return response;
    }
}
