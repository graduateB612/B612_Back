package com.b612.rose.service;

import com.b612.rose.entity.domain.GameProgress;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.entity.enums.StarType;
import com.b612.rose.repository.GameProgressRepository;
import com.b612.rose.service.impl.CacheServiceImpl;
import com.b612.rose.utils.GameCacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CacheService 통합 캐시 서비스 테스트")
class CacheServiceImplTest {

    @Mock
    private GameCacheManager gameCacheManager;
    @Mock
    private GameProgressRepository gameProgressRepository;

    @InjectMocks
    private CacheServiceImpl cacheService;

    private UUID testUserId;
    private GameProgress testGameProgress;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testGameProgress = GameProgress.builder()
                .progressId(1)
                .userId(testUserId)
                .currentStage(GameStage.COLLECT_PRIDE)
                .build();
    }

    @Test
    @DisplayName("사용자 캐시 초기화")
    void initializeUserCache_Success() {
        // When
        cacheService.initializeUserCache(testUserId, GameStage.INTRO);

        // Then
        verify(gameCacheManager).initializeUserCache(testUserId, GameStage.INTRO);
    }

    @Test
    @DisplayName("현재 스테이지 조회 - 캐시에서 조회 성공")
    void getCurrentStage_FromCache_Success() {
        // Given
        when(gameCacheManager.getCurrentStageFromCache(testUserId)).thenReturn(GameStage.COLLECT_PRIDE);

        // When
        GameStage result = cacheService.getCurrentStage(testUserId);

        // Then
        assertThat(result).isEqualTo(GameStage.COLLECT_PRIDE);
        verify(gameCacheManager).getCurrentStageFromCache(testUserId);
        verify(gameProgressRepository, never()).findByUserId(any());
    }

    @Test
    @DisplayName("현재 스테이지 조회 - 캐시 없어서 DB에서 조회")
    void getCurrentStage_FromDatabase_Success() {
        // Given
        when(gameCacheManager.getCurrentStageFromCache(testUserId)).thenReturn(null);
        when(gameProgressRepository.findByUserId(testUserId)).thenReturn(Optional.of(testGameProgress));

        // When
        GameStage result = cacheService.getCurrentStage(testUserId);

        // Then
        assertThat(result).isEqualTo(GameStage.COLLECT_PRIDE);
        verify(gameCacheManager).getCurrentStageFromCache(testUserId);
        verify(gameProgressRepository).findByUserId(testUserId);
        verify(gameCacheManager).updateStageInCache(testUserId, GameStage.COLLECT_PRIDE);
    }

    @Test
    @DisplayName("게임 스테이지 업데이트 - DB와 캐시 동시 업데이트")
    void updateGameStage_Success() {
        // Given
        when(gameProgressRepository.findByUserId(testUserId)).thenReturn(Optional.of(testGameProgress));

        // When
        cacheService.updateGameStage(testUserId, GameStage.COLLECT_ENVY);

        // Then
        verify(gameProgressRepository).findByUserId(testUserId);
        verify(gameProgressRepository).save(any(GameProgress.class));
        verify(gameCacheManager).updateStageInCache(testUserId, GameStage.COLLECT_ENVY);
    }

    @Test
    @DisplayName("별 상태 업데이트")
    void updateStarState_Success() {
        // When
        cacheService.updateStarState(testUserId, StarType.PRIDE, true, false);

        // Then
        verify(gameCacheManager).updateStarStateInCache(testUserId, StarType.PRIDE, true, false);
    }

    @Test
    @DisplayName("별 수집 상태 조회")
    void isStarCollected_Success() {
        // Given
        when(gameCacheManager.isStarCollectedInCache(testUserId, StarType.PRIDE)).thenReturn(true);

        // When
        Boolean result = cacheService.isStarCollected(testUserId, StarType.PRIDE);

        // Then
        assertThat(result).isTrue();
        verify(gameCacheManager).isStarCollectedInCache(testUserId, StarType.PRIDE);
    }

    @Test
    @DisplayName("별 전달 상태 조회")
    void isStarDelivered_Success() {
        // Given
        when(gameCacheManager.isStarDeliveredInCache(testUserId, StarType.PRIDE)).thenReturn(false);

        // When
        Boolean result = cacheService.isStarDelivered(testUserId, StarType.PRIDE);

        // Then
        assertThat(result).isFalse();
        verify(gameCacheManager).isStarDeliveredInCache(testUserId, StarType.PRIDE);
    }

    @Test
    @DisplayName("사용자 캐시 삭제")
    void clearUserCache_Success() {
        // When
        cacheService.clearUserCache(testUserId);

        // Then
        verify(gameCacheManager).clearUserCache(testUserId);
    }

    @Test
    @DisplayName("캐시 크기 조회")
    void getCacheSize_Success() {
        // Given
        when(gameCacheManager.getCacheSize()).thenReturn(5);

        // When
        int result = cacheService.getCacheSize();

        // Then
        assertThat(result).isEqualTo(5);
        verify(gameCacheManager).getCacheSize();
    }

    @Test
    @DisplayName("사용자 캐시 존재 여부 확인")
    void hasCacheForUser_Success() {
        // Given
        when(gameCacheManager.hasCacheForUser(testUserId)).thenReturn(true);

        // When
        boolean result = cacheService.hasCacheForUser(testUserId);

        // Then
        assertThat(result).isTrue();
        verify(gameCacheManager).hasCacheForUser(testUserId);
    }
} 