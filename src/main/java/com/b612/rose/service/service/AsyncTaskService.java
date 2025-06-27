package com.b612.rose.service.service;

import com.b612.rose.dto.request.EmailRequest;
import com.b612.rose.dto.request.StarActionRequest;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.entity.enums.InteractiveObjectType;

import java.util.UUID;

public interface AsyncTaskService {
    
    // 사용자 관련 비동기 작업
    void initializeGameStateAsync(UUID userId);
    
    // 게임 진행 관련 비동기 작업
    void updateGameStageAsync(UUID userId, Integer progressId, GameStage newStage);
    void processStarCollectionAsync(UUID userId, StarActionRequest request, GameStage newStage);
    void processStarDeliveryAsync(UUID userId, StarActionRequest request, GameStage newStage);
    
    // 상호작용 관련 비동기 작업
    void updateInteractionAsync(UUID userId, InteractiveObjectType objectType);
    
    // 이메일 관련 비동기 작업
    void sendEmailAsync(UUID userId, EmailRequest request);
} 