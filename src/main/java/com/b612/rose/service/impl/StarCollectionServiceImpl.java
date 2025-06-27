package com.b612.rose.service.impl;

import com.b612.rose.entity.domain.CollectedStar;
import com.b612.rose.entity.domain.InteractiveObject;
import com.b612.rose.entity.domain.Star;
import com.b612.rose.entity.domain.UserInteraction;
import com.b612.rose.entity.enums.InteractiveObjectType;
import com.b612.rose.entity.enums.StarType;
import com.b612.rose.exception.BusinessException;
import com.b612.rose.exception.ErrorCode;
import com.b612.rose.exception.ExceptionUtils;
import com.b612.rose.repository.CollectedStarRepository;
import com.b612.rose.repository.InteractiveObjectRepository;
import com.b612.rose.repository.StarRepository;
import com.b612.rose.repository.UserInteractionRepository;
import com.b612.rose.service.service.StarCollectionService;
import com.b612.rose.utils.GameCacheManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StarCollectionServiceImpl implements StarCollectionService {

    private final StarRepository starRepository;
    private final CollectedStarRepository collectedStarRepository;
    private final InteractiveObjectRepository interactiveObjectRepository;
    private final UserInteractionRepository userInteractionRepository;
    private final GameCacheManager gameCacheManager;

    // 별 수집 처리
    @Override
    @Transactional
    public void markStarAsCollected(UUID userId, StarType starType) {
        Star star = ExceptionUtils.getStarOrThrow(starRepository.findByStarType(starType), starType);

        CollectedStar oldCollectedStar = collectedStarRepository.findByUserIdAndStarStarType(userId, starType)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "수집된 별을 찾을 수 없습니다. userId : " + userId + " 별 종류: " + starType));

        // PRIDE 별은 수집과 동시에 전달까지 처리되므로 검증에서 제외
        if (starType != StarType.PRIDE) {
            ExceptionUtils.validateStarNotCollected(oldCollectedStar.isCollected(), starType);
        }

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
        gameCacheManager.updateStarStateInCache(userId, starType, true, oldCollectedStar.isDelivered());
    }

    // 별 전달 처리
    @Override
    @Transactional
    public void markStarAsDelivered(UUID userId, StarType starType) {
        Star star = ExceptionUtils.getStarOrThrow(starRepository.findByStarType(starType), starType);

        CollectedStar oldCollectedStar = collectedStarRepository.findByUserIdAndStarStarType(userId, starType)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "사용자에게서 수집된 별을 찾을 수 없습니다. : " + userId + " 별의 종류: " + starType));

        ExceptionUtils.validateStarCollected(oldCollectedStar.isCollected(), starType);

        // PRIDE 별은 수집과 동시에 전달까지 처리되므로 검증에서 제외
        if (starType != StarType.PRIDE) {
            ExceptionUtils.validateStarNotDelivered(oldCollectedStar.isDelivered(), starType);
        }
        
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
        gameCacheManager.updateStarStateInCache(userId, starType, oldCollectedStar.isCollected(), true);

        if (starType == StarType.SAD) {
            activateRequestForm(userId);
        }
    }

    // 모든 별이 수집되고 전달되었는지 확인
    @Override
    public boolean areAllStarsCollectedAndDelivered(UUID userId) {
        List<CollectedStar> stars = collectedStarRepository.findAllByUserId(userId);

        if (stars.isEmpty()) {
            return false;
        }

        return stars.stream().allMatch(star -> star.isCollected() && star.isDelivered());
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
} 