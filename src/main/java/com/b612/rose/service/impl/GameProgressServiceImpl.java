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


    @Override
    @Transactional
    public GameStateResponse updateGameStage(UUID userId, GameStageUpdateRequest request) {
        GameProgress currentProgress = gameProgressRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GAME_PROGRESS_NOT_FOUND,
                        "게임 진척도를 찾을 수 없음. 사용자 ID: "+userId));

        GameStage newStage = request.getNewStage();

        GameProgress updatedProgress = GameProgress.builder()
                .progressId(currentProgress.getProgressId())
                .userId(currentProgress.getUserId())
                .currentStage(newStage)
                .build();

        GameProgress savedProgress = gameProgressRepository.save(updatedProgress);

        if (newStage == GameStage.GAME_START) {
            gameStateManager.handleGameStart(userId);
        }

        List<DialogueResponse> dialogues = dialogueService.getDialoguesForCurrentStage(userId, newStage);

        return GameStateResponse.builder()
                .userId(savedProgress.getUserId())
                .currentStage(savedProgress.getCurrentStage())
                .dialogues(dialogues)
                .build();
    }

    @Override
    @Transactional
    public GameStateResponse onStarCollected(UUID userId, StarActionRequest request) {
        Star star = starRepository.findByStarType(request.getStarType())
                .orElseThrow(() -> new BusinessException(ErrorCode.STAR_NOT_FOUND,
                        "해당 별을 찾을 수 없음. ID: "+ request.getStarType()));

        gameStateManager.markStarAsCollected(userId, request.getStarType());
        
        if (request.getStarType() == StarType.PRIDE) {
            gameStateManager.markStarAsDelivered(userId, request.getStarType());
        }

        GameStage newStage = gameStateManager.getCollectStageForStar(star.getStarType());
        return updateGameStage(userId, new GameStageUpdateRequest(newStage));
    }

    @Override
    @Transactional
    public GameStateResponse onStarDelivered(UUID userId, StarActionRequest request) {
        Star star = starRepository.findByStarType(request.getStarType())
                .orElseThrow(() -> new BusinessException(ErrorCode.STAR_NOT_FOUND,
                        "해당 별을 찾을 수 없음. ID: "+ request.getStarType()));

        gameStateManager.markStarAsDelivered(userId, request.getStarType());

        GameStage newStage = gameStateManager.getDeliverStageForStar(star.getStarType());
        return updateGameStage(userId, new GameStageUpdateRequest(newStage));
    }

    @Override
    public GameStage getCurrentStage(UUID userId) {
        return gameProgressRepository.findByUserId(userId)
                .map(GameProgress::getCurrentStage)
                .orElse(null);
    }

    @Override
    public GameStateResponse getCurrentGameState(UUID userId) {
        GameStage currentStage = getCurrentStage(userId);

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
}
