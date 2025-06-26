package com.b612.rose.service;

import com.b612.rose.entity.domain.InteractiveObject;
import com.b612.rose.entity.domain.UserInteraction;
import com.b612.rose.entity.enums.InteractiveObjectType;
import com.b612.rose.repository.InteractiveObjectRepository;
import com.b612.rose.repository.UserInteractionRepository;
import com.b612.rose.service.impl.InteractionAsyncServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InteractionAsyncServiceImplTest {

    @Mock
    private InteractiveObjectRepository interactiveObjectRepository;

    @Mock
    private UserInteractionRepository userInteractionRepository;

    @InjectMocks
    private InteractionAsyncServiceImpl interactionAsyncService;

    @Test
    void updateInteractionAsync_WithExistingInteraction_ShouldUpdateInteraction() {
        // Given
        UUID userId = UUID.randomUUID();
        InteractiveObjectType objectType = InteractiveObjectType.STAR_GUIDE;

        InteractiveObject object = InteractiveObject.builder()
                .objectId(1)
                .objectType(objectType)
                .build();

        UserInteraction existingInteraction = UserInteraction.builder()
                .interactionId(1)
                .userId(userId)
                .objectId(object.getObjectId())
                .hasInteracted(false)
                .isActive(true)
                .build();

        // When
        when(interactiveObjectRepository.findByObjectType(objectType)).thenReturn(Optional.of(object));
        when(userInteractionRepository.findByUserIdAndObjectId(userId, object.getObjectId()))
                .thenReturn(Optional.of(existingInteraction));
        when(userInteractionRepository.save(any(UserInteraction.class))).thenReturn(existingInteraction);

        interactionAsyncService.updateInteractionAsync(userId, objectType);

        // Then
        verify(userInteractionRepository, times(1)).save(any(UserInteraction.class));
    }

    @Test
    void updateInteractionAsync_WithNewInteraction_ShouldCreateInteraction() {
        // Given
        UUID userId = UUID.randomUUID();
        InteractiveObjectType objectType = InteractiveObjectType.CHARACTER_PROFILE;

        InteractiveObject object = InteractiveObject.builder()
                .objectId(2)
                .objectType(objectType)
                .build();

        // When
        when(interactiveObjectRepository.findByObjectType(objectType)).thenReturn(Optional.of(object));
        when(userInteractionRepository.findByUserIdAndObjectId(userId, object.getObjectId()))
                .thenReturn(Optional.empty());

        interactionAsyncService.updateInteractionAsync(userId, objectType);

        // Then
        verify(userInteractionRepository, times(1)).save(any(UserInteraction.class));
    }
}