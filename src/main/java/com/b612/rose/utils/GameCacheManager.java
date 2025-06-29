package com.b612.rose.utils;

import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.entity.enums.StarType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameCacheManager {

    private final Map<UUID, GameStateCache> userGameStates = new ConcurrentHashMap<>();

    // 메모리에서 현재 스테이지 조회, 없으면 null 반환
    public GameStage getCurrentStageFromCache(UUID userId) {
        GameStateCache cache = userGameStates.get(userId);
        return cache != null ? cache.getCurrentStage() : null;
    }

    // 사용자 게임 캐시 초기화
    public void initializeUserCache(UUID userId, GameStage initialStage) {
        GameStateCache cache = GameStateCache.builder()
                .currentStage(initialStage)
                .collectedStars(new EnumMap<>(StarType.class))
                .deliveredStars(new EnumMap<>(StarType.class))
                .build();
        
        userGameStates.put(userId, cache);
    }

    // 메모리에서 게임 스테이지 업데이트
    public void updateStageInCache(UUID userId, GameStage newStage) {
        userGameStates.compute(userId, (key, existingCache) -> {
            GameStateCache cache = existingCache != null ? existingCache : GameStateCache.createInitial();

            return GameStateCache.builder()
                    .currentStage(newStage)
                    .collectedStars(cache.getCollectedStars())
                    .deliveredStars(cache.getDeliveredStars())
                    .build();
        });
    }

    // 메모리에서 별 상태 업데이트 (수집/전달)
    public void updateStarStateInCache(UUID userId, StarType starType, boolean collected, boolean delivered) {
        if (starType == null) {
            throw new IllegalArgumentException("StarType cannot be null");
        }
        
        userGameStates.compute(userId, (key, existingCache) -> {
            GameStateCache cache = existingCache != null ? existingCache : GameStateCache.createInitial();

            Map<StarType, Boolean> collectedMap = new EnumMap<>(cache.getCollectedStars());
            Map<StarType, Boolean> deliveredMap = new EnumMap<>(cache.getDeliveredStars());

            collectedMap.put(starType, collected);
            deliveredMap.put(starType, delivered);

            return GameStateCache.builder()
                    .currentStage(cache.getCurrentStage())
                    .collectedStars(collectedMap)
                    .deliveredStars(deliveredMap)
                    .build();
        });
    }

    // 메모리에서 별 수집 상태 조회
    public Boolean isStarCollectedInCache(UUID userId, StarType starType) {
        if (starType == null) {
            return null;
        }
        GameStateCache cache = userGameStates.get(userId);
        if (cache == null) {
            return null;
        }
        return cache.getCollectedStars().get(starType);
    }

    // 메모리에서 별 전달 상태 조회
    public Boolean isStarDeliveredInCache(UUID userId, StarType starType) {
        if (starType == null) {
            return null;
        }
        GameStateCache cache = userGameStates.get(userId);
        if (cache == null) {
            return null;
        }
        return cache.getDeliveredStars().get(starType);
    }

    // 사용자 캐시 완전 삭제
    public void clearUserCache(UUID userId) {
        userGameStates.remove(userId);
    }

    // 전체 캐시 상태 조회
    public int getCacheSize() {
        return userGameStates.size();
    }

    // 특정 사용자의 캐시 존재 여부 확인
    public boolean hasCacheForUser(UUID userId) {
        return userGameStates.containsKey(userId);
    }
} 