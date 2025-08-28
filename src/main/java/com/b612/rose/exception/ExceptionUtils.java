package com.b612.rose.exception;

import com.b612.rose.entity.domain.User;
import com.b612.rose.entity.domain.Star;
import com.b612.rose.entity.domain.GameProgress;
import com.b612.rose.entity.domain.InteractiveObject;
import com.b612.rose.entity.domain.Dialogue;
import com.b612.rose.entity.enums.StarType;

import java.util.Optional;
import java.util.UUID;

public class ExceptionUtils {

    private ExceptionUtils() {
        
    }

    // 사용자 관련 예외
    
    public static User getUserOrThrow(Optional<User> userOptional, UUID userId) {
        return userOptional.orElseThrow(() -> 
            new BusinessException(ErrorCode.USER_NOT_FOUND, 
                "사용자를 찾을 수 없습니다. userId: " + userId));
    }

    public static User getUserOrThrow(Optional<User> userOptional, String email) {
        return userOptional.orElseThrow(() -> 
            new BusinessException(ErrorCode.USER_NOT_FOUND, 
                "사용자를 찾을 수 없습니다. email: " + email));
    }

    // 게임 진행 관련 예외
    
    public static GameProgress getGameProgressOrThrow(Optional<GameProgress> progressOptional, UUID userId) {
        return progressOptional.orElseThrow(() -> 
            new BusinessException(ErrorCode.GAME_PROGRESS_NOT_FOUND, 
                "게임 진척도를 찾을 수 없습니다. userId: " + userId));
    }

    public static GameProgress getGameProgressOrThrow(Optional<GameProgress> progressOptional) {
        return progressOptional.orElseThrow(() -> 
            new BusinessException(ErrorCode.GAME_PROGRESS_NOT_FOUND, 
                "게임 진척도를 찾을 수 없습니다."));
    }

    // 별 관련 예외
    
    public static Star getStarOrThrow(Optional<Star> starOptional, StarType starType) {
        return starOptional.orElseThrow(() -> 
            new BusinessException(ErrorCode.STAR_NOT_FOUND, 
                "해당 별을 찾을 수 없습니다. starType: " + starType));
    }

    public static Star getStarOrThrow(Optional<Star> starOptional, int starId) {
        return starOptional.orElseThrow(() -> 
            new BusinessException(ErrorCode.STAR_NOT_FOUND, 
                "해당 별을 찾을 수 없습니다. starId: " + starId));
    }

    // 상호작용 오브젝트 관련 예외
    
    public static InteractiveObject getInteractiveObjectOrThrow(Optional<InteractiveObject> objectOptional, String objectType) {
        return objectOptional.orElseThrow(() -> 
            new BusinessException(ErrorCode.OBJECT_NOT_FOUND, 
                "해당 오브젝트를 찾을 수 없습니다. objectType: " + objectType));
    }

    public static InteractiveObject getInteractiveObjectOrThrow(Optional<InteractiveObject> objectOptional, int objectId) {
        return objectOptional.orElseThrow(() -> 
            new BusinessException(ErrorCode.OBJECT_NOT_FOUND, 
                "해당 오브젝트를 찾을 수 없습니다. objectId: " + objectId));
    }

    // 대화 관련 예외
    
    public static Dialogue getDialogueOrThrow(Optional<Dialogue> dialogueOptional, String context) {
        return dialogueOptional.orElseThrow(() -> 
            new BusinessException(ErrorCode.DIALOGUE_NOT_FOUND, 
                "해당 대화를 찾을 수 없습니다. " + context));
    }

    // 비즈니스 로직 검증
    
    public static void validateStarNotCollected(boolean isCollected, StarType starType) {
        if (isCollected) {
            throw new BusinessException(ErrorCode.STAR_ALREADY_COLLECTED, 
                "이미 수집된 별입니다. starType: " + starType);
        }
    }

    public static void validateStarCollected(boolean isCollected, StarType starType) {
        if (!isCollected) {
            throw new BusinessException(ErrorCode.STAR_NOT_COLLECTED, 
                "별을 먼저 수집해야 합니다. starType: " + starType);
        }
    }

    public static void validateStarNotDelivered(boolean isDelivered, StarType starType) {
        if (isDelivered) {
            throw new BusinessException(ErrorCode.STAR_ALREADY_COLLECTED, 
                "이미 전달된 별입니다. starType: " + starType);
        }
    }

    public static void validateObjectActive(boolean isActive, int objectId) {
        if (!isActive) {
            throw new BusinessException(ErrorCode.OBJECT_NOT_ACTIVE, 
                "비활성화된 오브젝트입니다. objectId: " + objectId);
        }
    }

    public static void validateAllStarsCompleted(boolean allCompleted) {
        if (!allCompleted) {
            throw new BusinessException(ErrorCode.STARS_NOT_COMPLETED, 
                "아직 모든 별을 수집, 전달하지 않았습니다.");
        }
    }

    public static void validateEmailProvided(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.EMAIL_REQUIRED, 
                "이메일 주소를 입력해주세요.");
        }
    }

    public static void validateNpcSelected(String selectedNpc) {
        if (selectedNpc == null || selectedNpc.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.NPC_SELECTION_REQUIRED, 
                "NPC를 선택해주세요.");
        }
    }
} 