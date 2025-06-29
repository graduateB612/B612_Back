package com.b612.rose.service.impl;


import com.b612.rose.dto.request.GameStageUpdateRequest;

import com.b612.rose.dto.response.DialogueResponse;
import com.b612.rose.dto.response.GameProgressResponse;
import com.b612.rose.dto.response.GameStateResponse;
import com.b612.rose.entity.domain.GameProgress;
import com.b612.rose.entity.domain.InteractiveObject;
import com.b612.rose.entity.domain.Star;
import com.b612.rose.entity.domain.UserInteraction;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.entity.enums.InteractiveObjectType;

import com.b612.rose.exception.BusinessException;
import com.b612.rose.exception.ErrorCode;
import com.b612.rose.exception.ExceptionUtils;
import com.b612.rose.repository.*;
import com.b612.rose.service.service.*;
import com.b612.rose.service.service.CacheService;
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
    private final CacheService cacheService;
    private final AsyncTaskService asyncTaskService;

    // 게임 진척도 업데이트
    @Override
    @Transactional
    public GameStateResponse updateGameStage(UUID userId, GameStageUpdateRequest request) {
        GameProgress currentProgress = ExceptionUtils.getGameProgressOrThrow(
                gameProgressRepository.findByUserId(userId), userId);

        GameStage newStage = request.getNewStage();
        cacheService.updateStarState(userId, null, false, false); // 즉시 응답용 임시 캐시 업데이트
        List<DialogueResponse> dialogues = dialogueService.getDialoguesForCurrentStage(userId, newStage);

        GameStateResponse response = GameStateResponse.builder()
                .userId(userId)
                .currentStage(newStage)
                .dialogues(dialogues)
                .build();

        asyncTaskService.updateGameStageAsync(userId, currentProgress.getProgressId(), newStage);
        return response;
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


}
