package com.b612.rose.mapper;

import com.b612.rose.entity.domain.CollectedStar;
import com.b612.rose.entity.domain.UserInteraction;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class EntityMapper {

    // CollectedStar 초기 생성
    public CollectedStar createCollectedStar(UUID userId, Integer starId) {
        return CollectedStar.builder()
                .userId(userId)
                .starId(starId)
                .collected(false)
                .delivered(false)
                .build();
    }

    // CollectedStar 수집 상태 업데이트
    public CollectedStar updateCollectedStar(CollectedStar existing, boolean collected, boolean delivered, LocalDateTime collectedAt, LocalDateTime deliveredAt) {
        return CollectedStar.builder()
                .collectionId(existing.getCollectionId())
                .userId(existing.getUserId())
                .starId(existing.getStarId())
                .collected(collected)
                .delivered(delivered)
                .collectedAt(collectedAt)
                .deliveredAt(deliveredAt)
                .build();
    }

    // UserInteraction 초기 생성
    public UserInteraction createUserInteraction(UUID userId, Integer objectId, boolean isActive) {
        return UserInteraction.builder()
                .userId(userId)
                .objectId(objectId)
                .hasInteracted(false)
                .isActive(isActive)
                .build();
    }

    // UserInteraction 업데이트
    public UserInteraction updateUserInteraction(UserInteraction existing, boolean hasInteracted, boolean isActive, LocalDateTime interactedAt) {
        return UserInteraction.builder()
                .interactionId(existing.getInteractionId())
                .userId(existing.getUserId())
                .objectId(existing.getObjectId())
                .hasInteracted(hasInteracted)
                .isActive(isActive)
                .interactedAt(interactedAt)
                .build();
    }


} 