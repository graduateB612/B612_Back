package com.b612.rose.service.service;

import com.b612.rose.dto.request.EmailRequest;
import com.b612.rose.dto.response.GameStateResponse;

import java.util.UUID;

public interface GameCompletionService {
    GameStateResponse completeGameAndSendEmail(UUID userId, EmailRequest request);
} 