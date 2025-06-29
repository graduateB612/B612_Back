package com.b612.rose.utils;

import com.b612.rose.entity.domain.*;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.entity.enums.InteractiveObjectType;
import com.b612.rose.entity.enums.StarType;
import com.b612.rose.exception.ExceptionUtils;
import com.b612.rose.repository.*;
import com.b612.rose.service.service.CacheService;
import com.b612.rose.service.service.GameStageService;
import com.b612.rose.service.service.StarCollectionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GameStateManager {

    private final StarRepository starRepository;
    private final UserRepository userRepository;
    private final CollectedStarRepository collectedStarRepository;
    private final InteractiveObjectRepository interactiveObjectRepository;
    private final UserInteractionRepository userInteractionRepository;

    private final CacheService cacheService;
    private final GameStageService gameStageService;
    private final StarCollectionService starCollectionService;

    // 게임 시작때 필요한 로직
    @Transactional
    public void handleGameStart(UUID userId) {
        ExceptionUtils.getUserOrThrow(userRepository.findById(userId), userId);

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
        cacheService.initializeUserCache(userId, com.b612.rose.entity.enums.GameStage.INTRO);
    }

    // 현재 스테이지 조회 (서비스 위임)
    public GameStage getCurrentStage(UUID userId) {
        return gameStageService.getCurrentStage(userId);
    }

    // 별 수집 스테이지 조회 (서비스 위임)
    public GameStage getCollectStageForStar(StarType starType) {
        return gameStageService.getCollectStageForStar(starType);
    }

    // 별 전달 스테이지 조회 (서비스 위임)
    public GameStage getDeliverStageForStar(StarType starType) {
        return gameStageService.getDeliverStageForStar(starType);
    }

    // 별 수집 처리 (서비스 위임)
    public void markStarAsCollected(UUID userId, StarType starType) {
        starCollectionService.markStarAsCollected(userId, starType);
    }

    // 별 전달 처리 (서비스 위임)
    public void markStarAsDelivered(UUID userId, StarType starType) {
        starCollectionService.markStarAsDelivered(userId, starType);
        }

    // 데이터베이스 스테이지 업데이트 (서비스 위임)
    public void updateDatabaseGameStage(UUID userId, GameStage newStage) {
        gameStageService.updateDatabaseGameStage(userId, newStage);
    }

    // 의뢰서 활성화
    @Transactional
    protected void activateRequestForm(UUID userId) {
        InteractiveObject requestFormObject = ExceptionUtils.getInteractiveObjectOrThrow(
                interactiveObjectRepository.findByObjectType(InteractiveObjectType.REQUEST_FORM),
                "REQUEST_FORM");

        UserInteraction interaction = userInteractionRepository
                .findByUserIdAndObjectId(userId, requestFormObject.getObjectId())
                .orElse(null);

        if (interaction == null) {
            interaction = UserInteraction.builder()
                    .userId(userId)
                    .objectId(requestFormObject.getObjectId())
                    .hasInteracted(false)
                    .isActive(true)
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

    // 게임 완료 처리
    @Transactional
    public void completeGame(UUID userId, String email, String concern, String selectedNpc) {
        User user = ExceptionUtils.getUserOrThrow(userRepository.findById(userId), userId);

        User updatedUser = User.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(email)
                .concern(concern)
                .selectedNpc(selectedNpc)
                .isCompleted(true)
                .build();

        userRepository.save(updatedUser);
        updateDatabaseGameStage(userId, GameStage.GAME_COMPLETE);
    }


    // 모든 별 수집/전달 여부 확인 (서비스 위임)
    public boolean areAllStarsCollectedAndDelivered(UUID userId) {
        return starCollectionService.areAllStarsCollectedAndDelivered(userId);
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