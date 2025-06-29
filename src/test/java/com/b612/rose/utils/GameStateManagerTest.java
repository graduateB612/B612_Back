package com.b612.rose.utils;

import com.b612.rose.entity.domain.*;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.entity.enums.InteractiveObjectType;
import com.b612.rose.entity.enums.StarType;
import com.b612.rose.exception.BusinessException;
import com.b612.rose.exception.ErrorCode;
import com.b612.rose.repository.*;
import com.b612.rose.service.service.CacheService;
import com.b612.rose.service.service.GameStageService;
import com.b612.rose.service.service.StarCollectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GameStateManager Facade 테스트")
class GameStateManagerTest {

    @Mock
    private StarRepository starRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CollectedStarRepository collectedStarRepository;
    @Mock
    private InteractiveObjectRepository interactiveObjectRepository;
    @Mock
    private UserInteractionRepository userInteractionRepository;
    @Mock
    private CacheService cacheService;
    @Mock
    private GameStageService gameStageService;
    @Mock
    private StarCollectionService starCollectionService;

    @InjectMocks
    private GameStateManager gameStateManager;

    private UUID testUserId;
    private User testUser;
    private Star testStar;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        
        testUser = User.builder()
                .userId(testUserId)
                .userName("TestUser")
                .build();

        testStar = Star.builder()
                .starId(1)
                .starType(StarType.PRIDE)
                .npcId(1)
                .build();
    }

    @Test
    @DisplayName("게임 시작 시 사용자 별 컬렉션과 상호작용 초기화")
    void handleGameStart_Success() {
        // Given
        List<Star> allStars = Arrays.asList(
                Star.builder().starId(1).starType(StarType.PRIDE).build(),
                Star.builder().starId(2).starType(StarType.ENVY).build(),
                Star.builder().starId(3).starType(StarType.LONELY).build(),
                Star.builder().starId(4).starType(StarType.SAD).build()
        );
        
        List<InteractiveObject> allObjects = Arrays.asList(
                InteractiveObject.builder().objectId(1).objectType(InteractiveObjectType.STAR_GUIDE).build(),
                InteractiveObject.builder().objectId(2).objectType(InteractiveObjectType.CHARACTER_PROFILE).build()
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(starRepository.findAll()).thenReturn(allStars);
        when(interactiveObjectRepository.findAll()).thenReturn(allObjects);

        // When
        gameStateManager.handleGameStart(testUserId);

        // Then
        verify(collectedStarRepository, times(4)).save(any(CollectedStar.class));
        verify(userInteractionRepository, times(2)).save(any(UserInteraction.class));
        verify(cacheService).initializeUserCache(eq(testUserId), eq(GameStage.INTRO));
    }

    @Test
    @DisplayName("현재 스테이지 조회 - 서비스 위임")
    void getCurrentStage_DelegatesToService() {
        // Given
        when(gameStageService.getCurrentStage(testUserId)).thenReturn(GameStage.COLLECT_PRIDE);

        // When
        GameStage result = gameStateManager.getCurrentStage(testUserId);

        // Then
        assertThat(result).isEqualTo(GameStage.COLLECT_PRIDE);
        verify(gameStageService).getCurrentStage(testUserId);
    }

    @Test
    @DisplayName("별 수집 스테이지 조회 - 서비스 위임")
    void getCollectStageForStar_DelegatesToService() {
        // Given
        when(gameStageService.getCollectStageForStar(StarType.PRIDE)).thenReturn(GameStage.COLLECT_PRIDE);

        // When
        GameStage result = gameStateManager.getCollectStageForStar(StarType.PRIDE);

        // Then
        assertThat(result).isEqualTo(GameStage.COLLECT_PRIDE);
        verify(gameStageService).getCollectStageForStar(StarType.PRIDE);
    }

    @Test
    @DisplayName("별 전달 스테이지 조회 - 서비스 위임")
    void getDeliverStageForStar_DelegatesToService() {
        // Given
        when(gameStageService.getDeliverStageForStar(StarType.PRIDE)).thenReturn(GameStage.COLLECT_ENVY);

        // When
        GameStage result = gameStateManager.getDeliverStageForStar(StarType.PRIDE);

        // Then
        assertThat(result).isEqualTo(GameStage.COLLECT_ENVY);
        verify(gameStageService).getDeliverStageForStar(StarType.PRIDE);
    }

    @Test
    @DisplayName("별 수집 처리 - 서비스 위임")
    void markStarAsCollected_DelegatesToService() {
        // When
        gameStateManager.markStarAsCollected(testUserId, StarType.PRIDE);

        // Then
        verify(starCollectionService).markStarAsCollected(testUserId, StarType.PRIDE);
    }

    @Test
    @DisplayName("별 전달 처리 - 서비스 위임")
    void markStarAsDelivered_DelegatesToService() {
        // When
        gameStateManager.markStarAsDelivered(testUserId, StarType.PRIDE);

        // Then
        verify(starCollectionService).markStarAsDelivered(testUserId, StarType.PRIDE);
    }

    @Test
    @DisplayName("모든 별 수집/전달 여부 확인 - 서비스 위임")
    void areAllStarsCollectedAndDelivered_DelegatesToService() {
        // Given
        when(starCollectionService.areAllStarsCollectedAndDelivered(testUserId)).thenReturn(true);

        // When
        boolean result = gameStateManager.areAllStarsCollectedAndDelivered(testUserId);

        // Then
        assertThat(result).isTrue();
        verify(starCollectionService).areAllStarsCollectedAndDelivered(testUserId);
    }

    @Test
    @DisplayName("데이터베이스 스테이지 업데이트 - 서비스 위임")
    void updateDatabaseGameStage_DelegatesToService() {
        // When
        gameStateManager.updateDatabaseGameStage(testUserId, GameStage.COLLECT_PRIDE);

        // Then
        verify(gameStageService).updateDatabaseGameStage(testUserId, GameStage.COLLECT_PRIDE);
    }
} 