package com.b612.rose.utils;

import com.b612.rose.entity.domain.*;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.entity.enums.InteractiveObjectType;
import com.b612.rose.entity.enums.StarType;
import com.b612.rose.exception.BusinessException;
import com.b612.rose.exception.ErrorCode;
import com.b612.rose.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;

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

    // 게임 시작때 필요한 로직
    @Transactional
    public void handleGameStart(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND,
                        "사용자를 찾을 수 없습니다. userId: " + userId));

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

    // 별 줍는 거에 따라 어떤 스테이지로 업데이트할지
    public GameStage getCollectStageForStar(StarType starType) {
        return switch (starType) {
            case PRIDE -> GameStage.COLLECT_PRIDE;
            case ENVY -> GameStage.COLLECT_ENVY;
            case LONELY -> GameStage.COLLECT_LONELY;
            case SAD -> GameStage.COLLECT_SAD;
        };
    }

    // 별 주는 거에 따라 어떤 스테이지로 업데이트할지
    public GameStage getDeliverStageForStar(StarType starType) {
        if (starType == StarType.PRIDE) {
            return GameStage.COLLECT_ENVY;
        }

        return switch (starType) {
            case ENVY -> GameStage.DELIVER_ENVY;
            case LONELY -> GameStage.DELIVER_LONELY;
            case SAD -> GameStage.DELIVER_SAD;
            default -> throw new BusinessException(ErrorCode.STAR_NOT_FOUND,
                    "해당 별을 찾을 수 없습니다. " + starType);
        };
    }

    // 별 주웠다고 업데이트
    @Transactional
    public void markStarAsCollected(UUID userId, StarType starType) {
        Star star = starRepository.findByStarType(starType)
                .orElseThrow(() -> new BusinessException(ErrorCode.STAR_NOT_FOUND,
                        "해당 별을 찾을 수 없습니다. " + starType));

        CollectedStar oldCollectedStar = collectedStarRepository.findByUserIdAndStarStarType(userId, starType)
                .orElseThrow(() ->new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "수집된 별을 찾을 수 없습니다. userId : " + userId + " 별 종류: " + starType));

        if (oldCollectedStar.isCollected() && starType != StarType.PRIDE) {
            throw new BusinessException(ErrorCode.STAR_ALREADY_COLLECTED,
                    "이 별은 이미 수집되었습니다. : " + starType);
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
    }

    // 별 줬다고 업데이트
    @Transactional
    public void markStarAsDelivered(UUID userId, StarType starType) {
        Star star = starRepository.findByStarType(starType)
                .orElseThrow(() -> new BusinessException(ErrorCode.STAR_NOT_FOUND,
                        "해당 타입의 별을 찾을 수 없습니다. :  " + starType));

        CollectedStar oldCollectedStar = collectedStarRepository.findByUserIdAndStarStarType(userId, starType)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "사용자에게서 수집된 별을 찾을 수 없습니다. : " + userId + " 별의 종류: " + starType));

        if (!oldCollectedStar.isCollected()) {
            throw new BusinessException(ErrorCode.STAR_NOT_COLLECTED,
                    "별 전달을 위해선 먼저 수집해야합니다. " + starType);
        }

        if (oldCollectedStar.isDelivered() && starType != StarType.PRIDE) {
            throw new BusinessException(ErrorCode.STAR_ALREADY_COLLECTED,
                    "이미 전달된 별입니다. " + starType);
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

        if (starType == StarType.SAD) {
            activateRequestForm(userId);
        }
    }

    // 의뢰서 활성화
    @Transactional
    protected void activateRequestForm(UUID userId) {
        InteractiveObject requestFormObject = interactiveObjectRepository
                .findByObjectType(InteractiveObjectType.REQUEST_FORM)
                .orElseThrow(() -> new BusinessException(ErrorCode.OBJECT_NOT_FOUND,
                        "요청 오브젝트를 찾을 수 없습니다."));

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND,
                        "사용자를 찾을 수 없습니다: " + userId));

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
                .orElseThrow(() -> new BusinessException(ErrorCode.GAME_PROGRESS_NOT_FOUND,
                        "사용자의 게임 진척도를 찾을 수 없습니다: " + userId));

        GameProgress updatedProgress = GameProgress.builder()
                .progressId(progress.getProgressId())
                .userId(userId)
                .currentStage(GameStage.GAME_COMPLETE)
                .updatedAt(LocalDateTime.now())
                .build();

        gameProgressRepository.save(updatedProgress);
    }

    // 별 다 줍고 전달했는지 검증
    public boolean areAllStarsCollectedAndDelivered(UUID userId) {
        List<CollectedStar> stars = collectedStarRepository.findAllByUserId(userId);

        if (stars.isEmpty()) {
            return false;
        }

        return stars.stream().allMatch(star -> star.isCollected() && star.isDelivered());
    }

    // 상호작용 요소 초기화
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