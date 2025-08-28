package com.b612.rose.service.service;

import com.b612.rose.dto.request.StarActionRequest;
import com.b612.rose.dto.response.GameStateResponse;

import java.util.UUID;

public interface StarActionService {
    GameStateResponse onStarCollected(UUID userId, StarActionRequest request);
    GameStateResponse onStarDelivered(UUID userId, StarActionRequest request);
} 