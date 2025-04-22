package com.b612.rose.utils;

import com.b612.rose.entity.domain.*;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.entity.enums.InteractiveObjectType;
import com.b612.rose.entity.enums.StarType;
import com.b612.rose.repository.*;
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
    private final GameProgressRepository gameProgressRepository;
    private final InteractiveObjectRepository interactiveObjectRepository;
    private final UserInteractionRepository userInteractionRepository;

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
        initUserInteractions(userId);
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

        if (starType == StarType.SAD) {
            activateRequestForm(userId);
        }
    }

    @Transactional
    protected void activateRequestForm(UUID userId) {
        InteractiveObject requestFormObject = interactiveObjectRepository
                .findByObjectType(InteractiveObjectType.REQUEST_FORM)
                .orElseThrow(() -> new IllegalArgumentException("Request form object not found"));

        UserInteraction interaction = userInteractionRepository
                .findByUserIdAndObjectId(userId, requestFormObject.getObjectId())
                .orElse(null);

        if (interaction == null) {
            interaction = UserInteraction.builder()
                    .userId(userId)
                    .objectId(requestFormObject.getObjectId())
                    .hasInteracted(false)
                    .isActive(true)  // 활성화
                    .build();
        } else {
            interaction = UserInteraction.builder()
                    .interactionId(interaction.getInteractionId())
                    .userId(interaction.getUserId())
                    .objectId(interaction.getObjectId())
                    .hasInteracted(interaction.isHasInteracted())
                    .isActive(true)
                    .interactedAt(interaction.getInteractedAt())
                    .build();
        }

        userInteractionRepository.save(interaction);
    }


    @Transactional
    public void completeGame(UUID userId, String email, String concern, String selectedNpc) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        User updatedUser = User.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(email)
                .concern(concern)
                .selectedNpc(selectedNpc)
                .isCompleted(true)
                .build();

        userRepository.save(updatedUser);

        GameProgress progress = gameProgressRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Game progress not found for user: " + userId));

        GameProgress updatedProgress = GameProgress.builder()
                .progressId(progress.getProgressId())
                .userId(userId)
                .currentStage(GameStage.GAME_COMPLETE)
                .updatedAt(LocalDateTime.now())
                .build();

        gameProgressRepository.save(updatedProgress);
    }

    public boolean areAllStarsCollectedAndDelivered(UUID userId) {
        List<CollectedStar> stars = collectedStarRepository.findAllByUserId(userId);

        if (stars.isEmpty()) {
            return false;
        }

        return stars.stream().allMatch(star -> star.isCollected() && star.isDelivered());
    }

    private void initUserInteractions(UUID userId) {
        List<InteractiveObject> objects = interactiveObjectRepository.findAll();

        for (InteractiveObject object : objects) {
            boolean isActive = object.getObjectType() != InteractiveObjectType.REQUEST_FORM;

            UserInteraction interaction = UserInteraction.builder()
                    .userId(userId)
                    .objectId(object.getObjectId())
                    .hasInteracted(false)
                    .isActive(isActive)
                    .build();

            userInteractionRepository.save(interaction);
        }
    }
}