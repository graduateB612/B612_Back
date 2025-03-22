package com.b612.rose.service.impl;

import com.b612.rose.dto.request.UserCreateRequest;
import com.b612.rose.dto.response.UserResponse;
import com.b612.rose.entity.domain.GameProgress;
import com.b612.rose.entity.domain.User;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.repository.GameProgressRepository;
import com.b612.rose.repository.UserRepository;
import com.b612.rose.service.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final GameProgressRepository gameProgressRepository;

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest requestDto) {
        User newUser = User.builder()
                .userName(requestDto.getUserName())
                .isCompleted(false)
                .build();

        User savedUser = userRepository.save(newUser);

        GameProgress newProgress = GameProgress.builder()
                .userId(savedUser.getUserId())
                .currentStage(GameStage.INTRO)
                .build();
        gameProgressRepository.save(newProgress);

        return UserResponse.builder()
                .id(savedUser.getUserId())
                .userName(savedUser.getUserName())
                .build();
    }

    @Override
    public Optional<UserResponse> getUserById(UUID id) {
        return userRepository.findById(id)
                .map(this::convertToResponse);
    }

    @Override
    public Optional<UserResponse> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToResponse);
    }

    @Override
    public boolean isUserExists(String email) {
        return userRepository.existsByEmail(email);
    }

    private UserResponse convertToResponse(User user) {
        GameStage currentStage = gameProgressRepository.findByUserId(user.getUserId())
                .map(GameProgress::getCurrentStage)
                .orElse(null);

        return UserResponse.builder()
                .id(user.getUserId())
                .userName(user.getUserName())
                .selectedNpc(user.getSelectedNpc())
                .concern(user.getConcern())
                .currentStage(currentStage)
                .build();
    }
}
