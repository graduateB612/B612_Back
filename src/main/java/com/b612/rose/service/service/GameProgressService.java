package com.b612.rose.service.service;

import com.b612.rose.dto.request.EmailRequest;
import com.b612.rose.dto.request.GameStageUpdateRequest;
import com.b612.rose.dto.request.StarActionRequest;
import com.b612.rose.dto.response.GameProgressResponse;
import com.b612.rose.dto.response.GameStateResponse;
import com.b612.rose.entity.enums.GameStage;

import java.util.UUID;

public interface GameProgressService {
    GameProgressResponse initGameProgress(UUID userId);
    GameStateResponse updateGameStage(UUID userId, GameStageUpdateRequest requestDto);
    GameStateResponse onStarCollected(UUID userId, StarActionRequest request);
    GameStateResponse onStarDelivered(UUID userId, StarActionRequest request);
    GameStage getCurrentStage(UUID userId);
    GameStateResponse getCurrentGameState(UUID userId);
    GameStateResponse completeGameAndSendEmail(UUID userId, EmailRequest request);
}
