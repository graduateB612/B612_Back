package com.b612.rose.utils;

import com.b612.rose.entity.domain.CollectedStar;
import com.b612.rose.entity.domain.Star;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.entity.enums.StarType;
import com.b612.rose.repository.CollectedStarRepository;
import com.b612.rose.repository.StarRepository;
import com.b612.rose.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GameStateManager {

    private final StarRepository starRepository;
    private final UserRepository userRepository;
    private final CollectedStarRepository collectedStarRepository;

    @Transactional
    public void handleGameStart(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        List<Star> allStars = starRepository.findAll();

        for (Star star : allStars) {
            CollectedStar collectedStar = CollectedStar.builder()
                    .userId(userId)
                    .starId(star.getStarId())
                    .collected(false)
                    .delivered(false)
                    .build();

            collectedStarRepository.save(collectedStar);
        }
    }

    public GameStage getCollectStageForStar(StarType starType) {
        return switch (starType) {
            case PRIDE -> GameStage.COLLECT_PRIDE;
            case ENVY -> GameStage.COLLECT_ENVY;
            case LONELY -> GameStage.COLLECT_LONELY;
            case SAD -> GameStage.COLLECT_SAD;
        };
    }

    public GameStage getDeliverStageForStar(StarType starType) {
        if (starType == StarType.PRIDE) {
            return GameStage.COLLECT_ENVY;
        }

        return switch (starType) {
            case PRIDE -> GameStage.COLLECT_ENVY;
            case ENVY -> GameStage.DELIVER_ENVY;
            case LONELY -> GameStage.DELIVER_LONELY;
            case SAD -> GameStage.DELIVER_SAD;
        };
    }

    @Transactional
    public void markStarAsCollected(UUID userId, StarType starType) {
        Star star = starRepository.findByStarType(starType)
                .orElseThrow(() -> new IllegalArgumentException("Star not found with type: " + starType));

        CollectedStar oldCollectedStar = collectedStarRepository.findByUserIdAndStarStarType(userId, starType)
                .orElseThrow(() -> new IllegalArgumentException("Collected star not found for user: " + userId + " and type: " + starType));

        CollectedStar updatedCollectedStar = CollectedStar.builder()
                .collectionId(oldCollectedStar.getCollectionId())
                .userId(userId)
                .starId(star.getStarId())
                .collected(true)
                .delivered(oldCollectedStar.isDelivered())
                .collectedAt(LocalDateTime.now())
                .deliveredAt(oldCollectedStar.getDeliveredAt())
                .build();

        collectedStarRepository.save(updatedCollectedStar);
    }

    @Transactional
    public void markStarAsDelivered(UUID userId, StarType starType) {
        Star star = starRepository.findByStarType(starType)
                .orElseThrow(() -> new IllegalArgumentException("Star not found with type: " + starType));

        CollectedStar oldCollectedStar = collectedStarRepository.findByUserIdAndStarStarType(userId, starType)
                .orElseThrow(() -> new IllegalArgumentException("Collected star not found for user: " + userId + " and type: " + starType));

        CollectedStar updatedCollectedStar = CollectedStar.builder()
                .collectionId(oldCollectedStar.getCollectionId())
                .userId(userId)
                .starId(star.getStarId())
                .collected(oldCollectedStar.isCollected())
                .delivered(true)
                .collectedAt(oldCollectedStar.getCollectedAt())
                .deliveredAt(LocalDateTime.now())
                .build();

        collectedStarRepository.save(updatedCollectedStar);
    }
}