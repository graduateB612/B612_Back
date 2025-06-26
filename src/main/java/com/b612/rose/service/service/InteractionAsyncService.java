package com.b612.rose.service.service;

import com.b612.rose.entity.enums.InteractiveObjectType;

import java.util.UUID;

public interface InteractionAsyncService {
    void updateInteractionAsync(UUID userId, InteractiveObjectType objectType);
}
