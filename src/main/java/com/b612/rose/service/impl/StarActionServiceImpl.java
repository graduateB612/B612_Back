package com.b612.rose.service.impl;

import com.b612.rose.dto.request.StarActionRequest;
import com.b612.rose.dto.response.DialogueResponse;
import com.b612.rose.dto.response.GameStateResponse;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.entity.enums.StarType;
import com.b612.rose.service.service.AsyncTaskService;
import com.b612.rose.service.service.CacheService;
import com.b612.rose.service.service.DialogueService;
import com.b612.rose.service.service.StarActionService;
import com.b612.rose.utils.GameStateManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StarActionServiceImpl implements StarActionService {

    private final GameStateManager gameStateManager;
    private final CacheService cacheService;
    private final DialogueService dialogueService;
    private final AsyncTaskService asyncTaskService;

    // 별 수집 처리
    @Override
    @Transactional
    public GameStateResponse onStarCollected(UUID userId, StarActionRequest request) {
        StarType starType = request.getStarType();
        GameStage newStage = gameStateManager.getCollectStageForStar(starType);

        // 캐시 즉시 업데이트 (사용자 경험 향상)
        cacheService.updateStarState(userId, starType, true, starType == StarType.PRIDE);

        // 응답용 대화 조회
        List<DialogueResponse> dialogues = dialogueService.getDialoguesForCurrentStage(userId, newStage);

        GameStateResponse immediateResponse = GameStateResponse.builder()
                .userId(userId)
                .currentStage(newStage)
                .dialogues(dialogues)
                .build();

        // 비동기로 실제 DB 처리
        asyncTaskService.processStarCollectionAsync(userId, request, newStage);

        return immediateResponse;
    }

    // 별 전달 처리
    @Override
    @Transactional
    public GameStateResponse onStarDelivered(UUID userId, StarActionRequest request) {
        StarType starType = request.getStarType();
        GameStage newStage = gameStateManager.getDeliverStageForStar(starType);

        // 캐시 즉시 업데이트 (사용자 경험 향상)
        cacheService.updateStarState(userId, starType, true, true);

        // 응답용 대화 조회
        List<DialogueResponse> dialogues = dialogueService.getDialoguesForCurrentStage(userId, newStage);

        GameStateResponse immediateResponse = GameStateResponse.builder()
                .userId(userId)
                .currentStage(newStage)
                .dialogues(dialogues)
                .build();

        // 비동기로 실제 DB 처리
        asyncTaskService.processStarDeliveryAsync(userId, request, newStage);

        return immediateResponse;
    }
} 