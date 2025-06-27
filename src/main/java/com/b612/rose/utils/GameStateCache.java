package com.b612.rose.utils;

import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.entity.enums.StarType;
import lombok.Builder;
import lombok.Getter;

import java.util.EnumMap;
import java.util.Map;

@Builder
@Getter
public class GameStateCache {
    private GameStage currentStage;
    private Map<StarType, Boolean> collectedStars;
    private Map<StarType, Boolean> deliveredStars;

    // 캐시 초기화
    public static GameStateCache createInitial() {
        Map<StarType, Boolean> collected = new EnumMap<>(StarType.class);
        Map<StarType, Boolean> delivered = new EnumMap<>(StarType.class);

        for (StarType type : StarType.values()) {
            collected.put(type, false);
            delivered.put(type, false);
        }

        return GameStateCache.builder()
                .currentStage(GameStage.INTRO)
                .collectedStars(collected)
                .deliveredStars(delivered)
                .build();
    }
}
