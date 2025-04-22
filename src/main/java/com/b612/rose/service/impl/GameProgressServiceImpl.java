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
import com.b612.rose.repository.*;
import com.b612.rose.service.service.DialogueService;
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
    private final EmailService emailService;
    private InteractiveObjectRepository interactiveObjectRepository;
    private UserInteractionRepository userInteractionRepository;

    @Override
    @Transactional
    public GameProgressResponse initGameProgress(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        GameProgress newProgress = GameProgress.builder()
                .userId(userId)
                .currentStage(GameStage.INTRO)
                .build();

        GameProgress savedProgress = gameProgressRepository.save(newProgress);
        initUserInteractions(userId);

        return convertToResponse(savedProgress);
    }

    @Override
    @Transactional
    public GameStateResponse updateGameStage(UUID userId, GameStageUpdateRequest request) {
        GameProgress currentProgress = gameProgressRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Game progress not found for user ID: " + userId));

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
                .orElseThrow(() -> new IllegalArgumentException("Star not found with ID: " + request.getStarType()));

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
                .orElseThrow(() -> new IllegalArgumentException("Star not found with ID: " + request.getStarType()));

        gameStateManager.markStarAsDelivered(userId, request.getStarType());

        // 마지막 별(SAD)을 전달했다면 의뢰 작성 오브젝트 활성화
        if (request.getStarType() == StarType.SAD) {
            InteractiveObject requestFormObject = interactiveObjectRepository
                    .findByObjectType(InteractiveObjectType.REQUEST_FORM)
                    .orElseThrow(() -> new IllegalArgumentException("Request form object not found"));

            UserInteraction interaction = userInteractionRepository
                    .findByUserIdAndObjectId(userId, requestFormObject.getObjectId())
                    .orElse(null);

            if (interaction == null) {
                interaction = UserInteraction.builder()
                        .userId(userId)
                        .objectId(requestFormObject.getObjectId())
                        .hasInteracted(false)
                        .isActive(true)  // 활성화
                        .build();
            } else {
                interaction = UserInteraction.builder()
                        .interactionId(interaction.getInteractionId())
                        .userId(interaction.getUserId())
                        .objectId(interaction.getObjectId())
                        .hasInteracted(interaction.isHasInteracted())
                        .isActive(true)  // 활성화
                        .interactedAt(interaction.getInteractedAt())
                        .build();
            }

            userInteractionRepository.save(interaction);
        }

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
            throw new IllegalArgumentException("Game progress not found for user ID: " + userId);
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
            throw new IllegalStateException("모든 별이 수집 및 전달되지 않았습니다");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            log.error("이메일 주소가 비어있습니다. userId: {}", userId);
            throw new IllegalArgumentException("이메일 주소가 필요합니다");
        }

        if (request.getSelectedNpc() == null || request.getSelectedNpc().isBlank()) {
            log.error("선택된 NPC가 없습니다. userId: {}", userId);
            throw new IllegalArgumentException("NPC를 선택해야 합니다");
        }

        log.info("이메일 전송 시도. userId: {}, email: {}, selectedNpc: {}",
                userId, request.getEmail(), request.getSelectedNpc());
        boolean isEmailSent = emailService.sendEmail(userId, request);

        if (!isEmailSent) {
            log.error("이메일 전송에 실패했습니다. userId: {}, email: {}", userId, request.getEmail());
            throw new RuntimeException("이메일 전송에 실패했습니다");
        }

        log.info("이메일 전송 성공, 게임 완료 처리. userId: {}", userId);
        gameStateManager.completeGame(userId, request.getEmail(), request.getConcern(), request.getSelectedNpc());

        return getCurrentGameState(userId);
    }

    private GameProgressResponse convertToResponse(GameProgress gameProgress) {
        return GameProgressResponse.builder()
                .userId(gameProgress.getUserId())
                .currentStage(gameProgress.getCurrentStage())
                .build();
    }

    private void initUserInteractions(UUID userId) {
        List<InteractiveObject> objects = interactiveObjectRepository.findAll();

        for (InteractiveObject object : objects) {
            boolean isActive = object.getObjectType() != InteractiveObjectType.REQUEST_FORM;

            UserInteraction interaction = UserInteraction.builder()
                    .userId(userId)
                    .objectId(object.getObjectId())
                    .hasInteracted(false)
                    .isActive(isActive)
                    .build();

            userInteractionRepository.save(interaction);
        }
    }
}
