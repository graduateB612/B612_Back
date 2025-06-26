package com.b612.rose.service.service;

import com.b612.rose.dto.request.StarActionRequest;
import com.b612.rose.entity.enums.GameStage;

import java.util.UUID;

public interface GameProgressAsyncService {
    void updateGameStageAsync(UUID userId, Integer progressId, GameStage newStage);
    void processStarCollectionAsync(UUID userId, StarActionRequest request, GameStage newStage);
    void processStarDeliveryAsync(UUID userId, StarActionRequest request, GameStage newStage);
}
