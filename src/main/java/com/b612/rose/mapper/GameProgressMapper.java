package com.b612.rose.mapper;

import com.b612.rose.entity.domain.GameProgress;
import com.b612.rose.entity.enums.GameStage;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GameProgressMapper {

    // 새로운 GameProgress Entity 생성
    public GameProgress createNew(UUID userId, GameStage initialStage) {
        return GameProgress.builder()
                .userId(userId)
                .currentStage(initialStage)
                .build();
    }

    // GameProgress Entity 업데이트
    public GameProgress updateStage(GameProgress existing, GameStage newStage) {
        return GameProgress.builder()
                .progressId(existing.getProgressId())
                .userId(existing.getUserId())
                .currentStage(newStage)
                .build();
    }
} 