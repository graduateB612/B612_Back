package com.b612.rose.service.impl;

import com.b612.rose.dto.request.UserCreateRequest;
import com.b612.rose.dto.response.UserResponse;
import com.b612.rose.entity.domain.GameProgress;
import com.b612.rose.entity.domain.User;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.exception.BusinessException;
import com.b612.rose.exception.ErrorCode;
import com.b612.rose.repository.GameProgressRepository;
import com.b612.rose.repository.UserRepository;
import com.b612.rose.service.service.UserAsyncService;
import com.b612.rose.service.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final GameProgressRepository gameProgressRepository;
    private final UserAsyncService userAsyncService;

    // 사용자 생성
    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        User newUser = User.builder()
                .userName(request.getUserName())
                .isCompleted(false)
                .build();

        User savedUser = userRepository.save(newUser);
        userAsyncService.initializeGameStateAsync(savedUser.getUserId());

        return UserResponse.builder()
                .id(savedUser.getUserId())
                .userName(savedUser.getUserName())
                .currentStage(GameStage.INTRO)
                .build();
    }

    // 사용자 id로 사용자 정보 조회
    @Override
    public Optional<UserResponse> getUserById(UUID id) {
        return userRepository.findById(id)
                .map(this::convertToResponse);
    }

    // 이메일로 사용자 조회
    @Override
    public Optional<UserResponse> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToResponse);
    }

    // 사용자가 존재하나요? (이메일로)
    @Override
    public boolean isUserExists(String email) {
        return userRepository.existsByEmail(email);
    }

    private UserResponse convertToResponse(User user) {
        GameStage currentStage = gameProgressRepository.findByUserId(user.getUserId())
                .map(GameProgress::getCurrentStage)
                .orElseThrow(() -> new BusinessException(ErrorCode.GAME_PROGRESS_NOT_FOUND,
                        "게임 진척도를 찾을 수 없습니다. userId: " + user.getUserId()));

        return UserResponse.builder()
                .id(user.getUserId())
                .userName(user.getUserName())
                .selectedNpc(user.getSelectedNpc())
                .concern(user.getConcern())
                .currentStage(currentStage)
                .build();
    }
}
