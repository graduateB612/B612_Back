package com.b612.rose.service.impl;

import com.b612.rose.entity.domain.GameProgress;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.entity.enums.StarType;
import com.b612.rose.exception.BusinessException;
import com.b612.rose.exception.ErrorCode;
import com.b612.rose.exception.ExceptionUtils;
import com.b612.rose.repository.GameProgressRepository;
import com.b612.rose.service.service.GameStageService;
import com.b612.rose.utils.GameCacheManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameStageServiceImpl implements GameStageService {

    private final GameProgressRepository gameProgressRepository;
    private final GameCacheManager gameCacheManager;

    // 현재 스테이지 조회 (캐시 우선, 없으면 DB 조회)
    @Override
    public GameStage getCurrentStage(UUID userId) {
        GameStage cachedStage = gameCacheManager.getCurrentStageFromCache(userId);
        if (cachedStage != null) {
            return cachedStage;
        }

        GameProgress progress = ExceptionUtils.getGameProgressOrThrow(
                gameProgressRepository.findByUserId(userId), userId);
        
        GameStage currentStage = progress.getCurrentStage();
        gameCacheManager.updateStageInCache(userId, currentStage);
        
        return currentStage;
    }

    // 별 수집 스테이지 조회
    @Override
    public GameStage getCollectStageForStar(StarType starType) {
        return switch (starType) {
            case PRIDE -> GameStage.COLLECT_PRIDE;
            case ENVY -> GameStage.COLLECT_ENVY;
            case LONELY -> GameStage.COLLECT_LONELY;
            case SAD -> GameStage.COLLECT_SAD;
            default -> throw new BusinessException(ErrorCode.STAR_NOT_FOUND,
                    "지원되지 않는 별 타입입니다. starType: " + starType);
        };
    }

    // 별 전달 스테이지 조회
    @Override
    public GameStage getDeliverStageForStar(StarType starType) {
        if (starType == StarType.PRIDE) {
            return GameStage.COLLECT_ENVY;
        }

        return switch (starType) {
            case ENVY -> GameStage.DELIVER_ENVY;
            case LONELY -> GameStage.DELIVER_LONELY;
            case SAD -> GameStage.DELIVER_SAD;
            default -> throw new BusinessException(ErrorCode.STAR_NOT_FOUND,
                    "지원되지 않는 별 타입입니다. starType: " + starType);
        };
    }

    // 데이터베이스 스테이지 업데이트
    @Override
    @Transactional
    public void updateDatabaseGameStage(UUID userId, GameStage newStage) {
        GameProgress currentProgress = ExceptionUtils.getGameProgressOrThrow(
                gameProgressRepository.findByUserId(userId), userId);

        GameProgress updatedProgress = GameProgress.builder()
                .progressId(currentProgress.getProgressId())
                .userId(userId)
                .currentStage(newStage)
                .build();

        gameProgressRepository.save(updatedProgress);
        gameCacheManager.updateStageInCache(userId, newStage);
    }
} 