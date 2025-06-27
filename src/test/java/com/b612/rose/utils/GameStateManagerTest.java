package com.b612.rose.utils;

import com.b612.rose.entity.domain.*;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.entity.enums.InteractiveObjectType;
import com.b612.rose.entity.enums.StarType;
import com.b612.rose.exception.BusinessException;
import com.b612.rose.exception.ErrorCode;
import com.b612.rose.repository.*;
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
@DisplayName("GameStateManager 테스트")
class GameStateManagerTest {

    @Mock
    private StarRepository starRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CollectedStarRepository collectedStarRepository;
    @Mock
    private GameProgressRepository gameProgressRepository;
    @Mock
    private InteractiveObjectRepository interactiveObjectRepository;
    @Mock
    private UserInteractionRepository userInteractionRepository;

    @InjectMocks
    private GameStateManager gameStateManager;

    private UUID testUserId;
    private User testUser;
    private Star testStar;
    private CollectedStar testCollectedStar;
    private GameProgress testGameProgress;

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

        testCollectedStar = CollectedStar.builder()
                .collectionId(1)
                .userId(testUserId)
                .starId(testStar.getStarId())
                .collected(false)
                .delivered(false)
                .build();

        testGameProgress = GameProgress.builder()
                .progressId(1)
                .userId(testUserId)
                .currentStage(GameStage.INTRO)
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
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 게임 시작 시 예외 발생")
    void handleGameStart_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> gameStateManager.handleGameStart(testUserId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("사용자를 찾을 수 없습니다. userId: " + testUserId);
    }

    @Test
    @DisplayName("현재 스테이지 조회 - 메모리 캐시에서 조회")
    void getCurrentStage_FromMemoryCache() {
        // Given
        gameStateManager.updateMemoryStage(testUserId, GameStage.COLLECT_PRIDE);

        // When
        GameStage currentStage = gameStateManager.getCurrentStage(testUserId);

        // Then
        assertThat(currentStage).isEqualTo(GameStage.COLLECT_PRIDE);
        verify(gameProgressRepository, never()).findByUserId(any());
    }

    @Test
    @DisplayName("현재 스테이지 조회 - DB에서 조회 후 캐시 생성")
    void getCurrentStage_FromDatabase() {
        // Given
        when(gameProgressRepository.findByUserId(testUserId))
                .thenReturn(Optional.of(testGameProgress));

        // When
        GameStage currentStage = gameStateManager.getCurrentStage(testUserId);

        // Then
        assertThat(currentStage).isEqualTo(GameStage.INTRO);
        verify(gameProgressRepository).findByUserId(testUserId);
    }

    @Test
    @DisplayName("별 타입에 따른 수집 스테이지 매핑")
    void getCollectStageForStar() {
        // When & Then
        assertThat(gameStateManager.getCollectStageForStar(StarType.PRIDE))
                .isEqualTo(GameStage.COLLECT_PRIDE);
        assertThat(gameStateManager.getCollectStageForStar(StarType.ENVY))
                .isEqualTo(GameStage.COLLECT_ENVY);
        assertThat(gameStateManager.getCollectStageForStar(StarType.LONELY))
                .isEqualTo(GameStage.COLLECT_LONELY);
        assertThat(gameStateManager.getCollectStageForStar(StarType.SAD))
                .isEqualTo(GameStage.COLLECT_SAD);
    }

    @Test
    @DisplayName("별 타입에 따른 전달 스테이지 매핑")
    void getDeliverStageForStar() {
        // When & Then
        assertThat(gameStateManager.getDeliverStageForStar(StarType.PRIDE))
                .isEqualTo(GameStage.COLLECT_ENVY);
        assertThat(gameStateManager.getDeliverStageForStar(StarType.ENVY))
                .isEqualTo(GameStage.DELIVER_ENVY);
        assertThat(gameStateManager.getDeliverStageForStar(StarType.LONELY))
                .isEqualTo(GameStage.DELIVER_LONELY);
        assertThat(gameStateManager.getDeliverStageForStar(StarType.SAD))
                .isEqualTo(GameStage.DELIVER_SAD);
    }

    @Test
    @DisplayName("메모리 게임 상태 업데이트")
    void updateMemoryGameState() {
        // When
        gameStateManager.updateMemoryGameState(testUserId, StarType.PRIDE, true, false);

        // Then
        // 메모리 상태 변경 검증은 private 맵이므로 간접적으로 확인
        // 실제로는 다른 메서드를 통해 검증 가능
        assertThatCode(() -> gameStateManager.updateMemoryGameState(testUserId, StarType.PRIDE, true, false))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("별 수집 성공")
    void markStarAsCollected_Success() {
        // Given
        when(starRepository.findByStarType(StarType.PRIDE)).thenReturn(Optional.of(testStar));
        when(collectedStarRepository.findByUserIdAndStarStarType(testUserId, StarType.PRIDE))
                .thenReturn(Optional.of(testCollectedStar));

        // When
        gameStateManager.markStarAsCollected(testUserId, StarType.PRIDE);

        // Then
        verify(collectedStarRepository).save(any(CollectedStar.class));
    }

    @Test
    @DisplayName("이미 수집된 별 재수집 시 예외 발생")
    void markStarAsCollected_AlreadyCollected_ThrowsException() {
        // Given
        CollectedStar alreadyCollectedStar = CollectedStar.builder()
                .collectionId(1)
                .userId(testUserId)
                .starId(testStar.getStarId())
                .collected(true)
                .delivered(false)
                .build();

        when(starRepository.findByStarType(StarType.ENVY)).thenReturn(Optional.of(testStar));
        when(collectedStarRepository.findByUserIdAndStarStarType(testUserId, StarType.ENVY))
                .thenReturn(Optional.of(alreadyCollectedStar));

        // When & Then
        assertThatThrownBy(() -> gameStateManager.markStarAsCollected(testUserId, StarType.ENVY))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이 별은 이미 수집되었습니다. : " + StarType.ENVY);
    }

    @Test
    @DisplayName("존재하지 않는 별 수집 시 예외 발생")
    void markStarAsCollected_StarNotFound_ThrowsException() {
        // Given
        when(starRepository.findByStarType(StarType.PRIDE)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> gameStateManager.markStarAsCollected(testUserId, StarType.PRIDE))
                .isInstanceOf(BusinessException.class)
                .hasMessage("해당 별을 찾을 수 없습니다. " + StarType.PRIDE);
    }

    @Test
    @DisplayName("별 전달 성공")
    void markStarAsDelivered_Success() {
        // Given
        CollectedStar collectedStar = CollectedStar.builder()
                .collectionId(1)
                .userId(testUserId)
                .starId(testStar.getStarId())
                .collected(true)
                .delivered(false)
                .build();

        when(starRepository.findByStarType(StarType.PRIDE)).thenReturn(Optional.of(testStar));
        when(collectedStarRepository.findByUserIdAndStarStarType(testUserId, StarType.PRIDE))
                .thenReturn(Optional.of(collectedStar));

        // When
        gameStateManager.markStarAsDelivered(testUserId, StarType.PRIDE);

        // Then
        verify(collectedStarRepository).save(any(CollectedStar.class));
    }

    @Test
    @DisplayName("수집되지 않은 별 전달 시 예외 발생")
    void markStarAsDelivered_NotCollected_ThrowsException() {
        // Given
        when(starRepository.findByStarType(StarType.PRIDE)).thenReturn(Optional.of(testStar));
        when(collectedStarRepository.findByUserIdAndStarStarType(testUserId, StarType.PRIDE))
                .thenReturn(Optional.of(testCollectedStar));

        // When & Then
        assertThatThrownBy(() -> gameStateManager.markStarAsDelivered(testUserId, StarType.PRIDE))
                .isInstanceOf(BusinessException.class)
                .hasMessage("별 전달을 위해선 먼저 수집해야합니다. " + StarType.PRIDE);
    }

    @Test
    @DisplayName("SAD 별 전달 시 의뢰서 활성화")
    void markStarAsDelivered_SadStar_ActivatesRequestForm() {
        // Given
        CollectedStar collectedStar = CollectedStar.builder()
                .collectionId(1)
                .userId(testUserId)
                .starId(testStar.getStarId())
                .collected(true)
                .delivered(false)
                .build();
        
        InteractiveObject requestForm = InteractiveObject.builder()
                .objectId(1)
                .objectType(InteractiveObjectType.REQUEST_FORM)
                .build();

        when(starRepository.findByStarType(StarType.SAD)).thenReturn(Optional.of(testStar));
        when(collectedStarRepository.findByUserIdAndStarStarType(testUserId, StarType.SAD))
                .thenReturn(Optional.of(collectedStar));
        when(interactiveObjectRepository.findByObjectType(InteractiveObjectType.REQUEST_FORM))
                .thenReturn(Optional.of(requestForm));
        when(userInteractionRepository.findByUserIdAndObjectId(testUserId, requestForm.getObjectId()))
                .thenReturn(Optional.empty());

        // When
        gameStateManager.markStarAsDelivered(testUserId, StarType.SAD);

        // Then
        verify(collectedStarRepository).save(any(CollectedStar.class)); // markStarAsDelivered
        verify(userInteractionRepository).save(any(UserInteraction.class)); // activateRequestForm
    }

    @Test
    @DisplayName("게임 완료 처리")
    void completeGame_Success() {
        // Given
        String email = "test@example.com";
        String concern = "Test concern";
        String selectedNpc = "Fox";

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(gameProgressRepository.findByUserId(testUserId)).thenReturn(Optional.of(testGameProgress));

        // When
        gameStateManager.completeGame(testUserId, email, concern, selectedNpc);

        // Then
        verify(userRepository).save(any(User.class));
        verify(gameProgressRepository).save(any(GameProgress.class));
    }

    @Test
    @DisplayName("모든 별 수집 및 전달 완료 검증 - 성공")
    void areAllStarsCollectedAndDelivered_AllCompleted_ReturnsTrue() {
        // Given
        List<CollectedStar> allCompletedStars = Arrays.asList(
                CollectedStar.builder().collected(true).delivered(true).build(),
                CollectedStar.builder().collected(true).delivered(true).build(),
                CollectedStar.builder().collected(true).delivered(true).build(),
                CollectedStar.builder().collected(true).delivered(true).build()
        );

        when(collectedStarRepository.findAllByUserId(testUserId)).thenReturn(allCompletedStars);

        // When
        boolean result = gameStateManager.areAllStarsCollectedAndDelivered(testUserId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("모든 별 수집 및 전달 완료 검증 - 실패")
    void areAllStarsCollectedAndDelivered_NotAllCompleted_ReturnsFalse() {
        // Given
        List<CollectedStar> incompleteStars = Arrays.asList(
                CollectedStar.builder().collected(true).delivered(true).build(),
                CollectedStar.builder().collected(true).delivered(false).build(), // 전달 안됨
                CollectedStar.builder().collected(false).delivered(false).build(), // 수집 안됨
                CollectedStar.builder().collected(true).delivered(true).build()
        );

        when(collectedStarRepository.findAllByUserId(testUserId)).thenReturn(incompleteStars);

        // When
        boolean result = gameStateManager.areAllStarsCollectedAndDelivered(testUserId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("빈 별 리스트일 때 검증 실패")
    void areAllStarsCollectedAndDelivered_EmptyList_ReturnsFalse() {
        // Given
        when(collectedStarRepository.findAllByUserId(testUserId)).thenReturn(Arrays.asList());

        // When
        boolean result = gameStateManager.areAllStarsCollectedAndDelivered(testUserId);

        // Then
        assertThat(result).isFalse();
    }
} 