package com.b612.rose.service.impl;

import com.b612.rose.dto.request.EmailRequest;
import com.b612.rose.dto.response.DialogueResponse;
import com.b612.rose.dto.response.GameStateResponse;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.exception.ExceptionUtils;
import com.b612.rose.service.service.AsyncTaskService;
import com.b612.rose.service.service.CacheService;
import com.b612.rose.service.service.DialogueService;
import com.b612.rose.service.service.GameCompletionService;
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
public class GameCompletionServiceImpl implements GameCompletionService {

    private final GameStateManager gameStateManager;
    private final CacheService cacheService;
    private final DialogueService dialogueService;
    private final AsyncTaskService asyncTaskService;

    // 게임 완료 처리, 이메일 전송 처리
    @Override
    @Transactional
    public GameStateResponse completeGameAndSendEmail(UUID userId, EmailRequest request) {
        // 게임 완료 조건 검증
        ExceptionUtils.validateAllStarsCompleted(
                gameStateManager.areAllStarsCollectedAndDelivered(userId));
        ExceptionUtils.validateEmailProvided(request.getEmail());
        ExceptionUtils.validateNpcSelected(request.getSelectedNpc());

        log.info("게임 완료 처리 - 사용자: {}, 이메일: {}, 선택한 NPC: {}",
                userId, request.getEmail(), request.getSelectedNpc());

        // 게임 완료 상태로 업데이트
        cacheService.updateGameStage(userId, GameStage.GAME_COMPLETE);
        gameStateManager.completeGame(userId, request.getEmail(), request.getConcern(), request.getSelectedNpc());
        
        // 현재 게임 상태 응답 생성
        GameStage currentStage = gameStateManager.getCurrentStage(userId);
        List<DialogueResponse> dialogues = dialogueService.getDialoguesForCurrentStage(userId, currentStage);
        
        GameStateResponse response = GameStateResponse.builder()
                .userId(userId)
                .currentStage(currentStage)
                .dialogues(dialogues)
                .build();

        // 비동기 이메일 전송
        asyncTaskService.sendEmailAsync(userId, request);

        return response;
    }
} 