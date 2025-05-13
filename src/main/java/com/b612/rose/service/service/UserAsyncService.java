package com.b612.rose.service.service;

import java.util.UUID;

public interface UserAsyncService {
    void initializeGameStateAsync(UUID userId);
}