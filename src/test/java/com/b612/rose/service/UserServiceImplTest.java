package com.b612.rose.service;

import com.b612.rose.dto.request.UserCreateRequest;
import com.b612.rose.dto.response.UserResponse;
import com.b612.rose.entity.domain.GameProgress;
import com.b612.rose.entity.domain.User;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.exception.BusinessException;
import com.b612.rose.exception.ErrorCode;
import com.b612.rose.mapper.GameProgressMapper;
import com.b612.rose.mapper.UserMapper;
import com.b612.rose.repository.GameProgressRepository;
import com.b612.rose.repository.UserRepository;
import com.b612.rose.service.impl.UserServiceImpl;
import com.b612.rose.service.service.AsyncTaskService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private GameProgressRepository gameProgressRepository;
    @Mock
    private AsyncTaskService asyncTaskService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private GameProgressMapper gameProgressMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID testUserId;
    private User testUser;
    private UserCreateRequest createRequest;
    private GameProgress testGameProgress;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        
        testUser = User.builder()
                .userId(testUserId)
                .userName("TestUser")
                .email("test@example.com")
                .concern("Test concern")
                .selectedNpc("Fox")
                .isCompleted(false)
                .build();

        createRequest = UserCreateRequest.builder()
                .userName("NewUser")
                .build();

        testGameProgress = GameProgress.builder()
                .progressId(1)
                .userId(testUserId)
                .currentStage(GameStage.INTRO)
                .build();
    }

    @Test
    @DisplayName("사용자 생성 성공")
    void createUser_Success() {
        // Given
        User savedUser = User.builder()
                .userId(testUserId)
                .userName("NewUser")
                .isCompleted(false)
                .build();

        UserResponse expectedResponse = UserResponse.builder()
                .id(testUserId)
                .userName("NewUser")
                .currentStage(GameStage.INTRO)
                .build();

        when(userMapper.toEntity(createRequest)).thenReturn(savedUser);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(gameProgressMapper.createNew(testUserId, GameStage.INTRO)).thenReturn(testGameProgress);
        when(userMapper.toResponse(savedUser, GameStage.INTRO)).thenReturn(expectedResponse);

        // When
        UserResponse result = userService.createUser(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserName()).isEqualTo("NewUser");
        assertThat(result.getCurrentStage()).isEqualTo(GameStage.INTRO);
        
        verify(userRepository).save(any(User.class));
        verify(gameProgressRepository).save(any(GameProgress.class));
        verify(asyncTaskService).initializeGameStateAsync(any(UUID.class));
    }

    @Test
    @DisplayName("사용자 조회 성공")
    void getUserById_Success() {
        // Given
        UserResponse expectedResponse = UserResponse.builder()
                .id(testUserId)
                .userName("TestUser")
                .currentStage(GameStage.INTRO)
                .build();

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(gameProgressRepository.findByUserId(testUserId)).thenReturn(Optional.of(testGameProgress));
        when(userMapper.toResponse(testUser, testGameProgress)).thenReturn(expectedResponse);

        // When
        Optional<UserResponse> result = userService.getUserById(testUserId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testUserId);
        assertThat(result.get().getUserName()).isEqualTo("TestUser");
        
        verify(userRepository).findById(testUserId);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회")
    void getUserById_UserNotFound_ReturnsEmpty() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When
        Optional<UserResponse> result = userService.getUserById(testUserId);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findById(testUserId);
    }

    @Test
    @DisplayName("이메일로 사용자 존재 확인 - 존재함")
    void isUserExists_UserExists_ReturnsTrue() {
        // Given
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When
        boolean result = userService.isUserExists(email);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("이메일로 사용자 존재 확인 - 존재하지 않음")
    void isUserExists_UserNotExists_ReturnsFalse() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // When
        boolean result = userService.isUserExists(email);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("이메일로 사용자 조회 성공")
    void getUserByEmail_Success() {
        // Given
        String email = "test@example.com";
        UserResponse expectedResponse = UserResponse.builder()
                .id(testUserId)
                .userName("TestUser")
                .currentStage(GameStage.INTRO)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(gameProgressRepository.findByUserId(testUserId)).thenReturn(Optional.of(testGameProgress));
        when(userMapper.toResponse(testUser, testGameProgress)).thenReturn(expectedResponse);

        // When
        Optional<UserResponse> result = userService.getUserByEmail(email);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUserName()).isEqualTo("TestUser");
        verify(userRepository).findByEmail(email);
    }
} 