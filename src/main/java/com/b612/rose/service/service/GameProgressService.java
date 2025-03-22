package com.b612.rose.service.service;

import com.b612.rose.dto.request.GameStageUpdateRequest;
import com.b612.rose.dto.response.GameProgressResponse;
import com.b612.rose.entity.enums.GameStage;

import java.util.UUID;

public interface GameProgressService {
    GameProgressResponse initGameProgress(UUID userId);
    GameProgressResponse updateGameStage(UUID userId, GameStageUpdateRequest requestDto);
    GameStage getCurrentStage(UUID userId);
}
