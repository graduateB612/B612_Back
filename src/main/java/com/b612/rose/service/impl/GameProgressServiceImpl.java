package com.b612.rose.service.impl;

import com.b612.rose.dto.request.GameStageUpdateRequest;
import com.b612.rose.dto.request.StarActionRequest;
import com.b612.rose.dto.response.DialogueResponse;
import com.b612.rose.dto.response.GameProgressResponse;
import com.b612.rose.dto.response.GameStateResponse;
import com.b612.rose.entity.domain.GameProgress;
import com.b612.rose.entity.domain.Star;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.repository.GameProgressRepository;
import com.b612.rose.repository.StarRepository;
import com.b612.rose.repository.UserRepository;
import com.b612.rose.service.service.DialogueService;
import com.b612.rose.service.service.GameProgressService;
import com.b612.rose.utils.GameStateManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameProgressServiceImpl implements GameProgressService {

    private final GameProgressRepository gameProgressRepository;
    private final UserRepository userRepository;
    private final StarRepository starRepository;
    private final DialogueService dialogueService;
    private final GameStateManager gameStateManager;

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
            gameStateManager.handleGameStart(userId); // 플레이어의 별 수집 현황 초기화
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

        GameStage newStage = gameStateManager.getCollectStageForStar(star.getStarType());
        return updateGameStage(userId, new GameStageUpdateRequest(newStage));
    }

    @Override
    @Transactional
    public GameStateResponse onStarDelivered(UUID userId, StarActionRequest request) {
        Star star = starRepository.findByStarType(request.getStarType())
                .orElseThrow(() -> new IllegalArgumentException("Star not found with ID: " + request.getStarType()));

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
            throw new IllegalArgumentException("Game progress not found for user ID: " + userId);
        }

        List<DialogueResponse> dialogues = dialogueService.getDialoguesForCurrentStage(userId, currentStage);

        return GameStateResponse.builder()
                .userId(userId)
                .currentStage(currentStage)
                .dialogues(dialogues)
                .build();
    }

    private GameProgressResponse convertToResponse(GameProgress gameProgress) {
        return GameProgressResponse.builder()
                .userId(gameProgress.getUserId())
                .currentStage(gameProgress.getCurrentStage())
                .build();
    }
}
