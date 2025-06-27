package com.b612.rose.service;

import com.b612.rose.entity.domain.GameProgress;
import com.b612.rose.repository.GameProgressRepository;
import com.b612.rose.service.impl.UserAsyncServiceImpl;
import com.b612.rose.utils.GameStateManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserAsyncServiceImplTest {

    @Mock
    private GameProgressRepository gameProgressRepository;

    @Mock
    private GameStateManager gameStateManager;

    @InjectMocks
    private UserAsyncServiceImpl userAsyncService;

    @Test
    void initializeGameStateAsync_ShouldCreateGameProgressAndInitializeState() {
        // Given
        UUID userId = UUID.randomUUID();

        // When
        when(gameProgressRepository.save(any(GameProgress.class))).thenAnswer(invocation -> {
            GameProgress progress = invocation.getArgument(0);
            return progress;
        });
        doNothing().when(gameStateManager).handleGameStart(userId);

        userAsyncService.initializeGameStateAsync(userId);

        // Then
        verify(gameProgressRepository, times(1)).save(any(GameProgress.class));
        verify(gameStateManager, times(1)).handleGameStart(userId);
    }
}