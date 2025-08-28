package com.b612.rose.service.impl;

import com.b612.rose.entity.domain.GameProgress;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.entity.enums.StarType;
import com.b612.rose.exception.ExceptionUtils;
import com.b612.rose.repository.GameProgressRepository;
import com.b612.rose.service.service.CacheService;
import com.b612.rose.utils.GameCacheManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheServiceImpl implements CacheService {

    private final GameCacheManager gameCacheManager;
    private final GameProgressRepository gameProgressRepository;

    // 사용자 게임 캐시 초기화
    @Override
    public void initializeUserCache(UUID userId, GameStage initialStage) {
        gameCacheManager.initializeUserCache(userId, initialStage);
    }

    // 현재 스테이지 조회 (캐시 우선, 없으면 DB 조회 후 캐시 저장)
    @Override
    @Cacheable(value = "starCache", key = "'currentStage:' + #userId")
    public GameStage getCurrentStage(UUID userId) {
        // 메모리 캐시에서 조회
        GameStage cachedStage = gameCacheManager.getCurrentStageFromCache(userId);
        if (cachedStage != null) {
            return cachedStage;
        }

        // DB에서 조회 후 캐시 저장
        GameProgress progress = ExceptionUtils.getGameProgressOrThrow(
                gameProgressRepository.findByUserId(userId), userId);
        
        GameStage currentStage = progress.getCurrentStage();
        gameCacheManager.updateStageInCache(userId, currentStage);
        
        return currentStage;
    }

    // 게임 스테이지 업데이트 (DB + 캐시 동시 업데이트)
    @Override
    @Transactional
    @CacheEvict(value = "starCache", key = "'currentStage:' + #userId")
    public void updateGameStage(UUID userId, GameStage newStage) {
        // DB 업데이트
        GameProgress currentProgress = ExceptionUtils.getGameProgressOrThrow(
                gameProgressRepository.findByUserId(userId), userId);

        GameProgress updatedProgress = GameProgress.builder()
                .progressId(currentProgress.getProgressId())
                .userId(userId)
                .currentStage(newStage)
                .build();

        gameProgressRepository.save(updatedProgress);
        
        // 메모리 캐시 업데이트
        gameCacheManager.updateStageInCache(userId, newStage);
    }

    // 별 상태 업데이트 (메모리 캐시만)
    @Override
    public void updateStarState(UUID userId, StarType starType, boolean collected, boolean delivered) {
        gameCacheManager.updateStarStateInCache(userId, starType, collected, delivered);
    }

    // 별 수집 상태 조회 (캐시 우선)
    @Override
    public Boolean isStarCollected(UUID userId, StarType starType) {
        return gameCacheManager.isStarCollectedInCache(userId, starType);
    }

    // 별 전달 상태 조회 (캐시 우선)
    @Override
    public Boolean isStarDelivered(UUID userId, StarType starType) {
        return gameCacheManager.isStarDeliveredInCache(userId, starType);
    }

    // 사용자 캐시 완전 삭제
    @Override
    @CacheEvict(value = "starCache", key = "'currentStage:' + #userId")
    public void clearUserCache(UUID userId) {
        gameCacheManager.clearUserCache(userId);
    }

    // 캐시 상태 정보 조회
    @Override
    public int getCacheSize() {
        return gameCacheManager.getCacheSize();
    }

    // 특정 사용자의 캐시 존재 여부 확인
    @Override
    public boolean hasCacheForUser(UUID userId) {
        return gameCacheManager.hasCacheForUser(userId);
    }
} 