package com.b612.rose.service.impl;

import com.b612.rose.dto.request.UserCreateRequest;
import com.b612.rose.dto.response.UserResponse;
import com.b612.rose.entity.domain.GameProgress;
import com.b612.rose.entity.domain.User;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.exception.BusinessException;
import com.b612.rose.exception.ErrorCode;
import com.b612.rose.exception.ExceptionUtils;
import com.b612.rose.mapper.GameProgressMapper;
import com.b612.rose.mapper.UserMapper;
import com.b612.rose.repository.GameProgressRepository;
import com.b612.rose.repository.UserRepository;
import com.b612.rose.service.service.AsyncTaskService;
import com.b612.rose.service.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final GameProgressRepository gameProgressRepository;
    private final AsyncTaskService asyncTaskService;
    private final UserMapper userMapper;
    private final GameProgressMapper gameProgressMapper;

    // 사용자 생성
    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        // 1. 사용자 엔티티 생성 및 저장
        User newUser = userMapper.toEntity(request);

        User savedUser = userRepository.save(newUser);

        // 2. 동일 트랜잭션 내에서 GameProgress 처리하여 외래 키 위반 방지
        GameProgress newProgress = gameProgressMapper.createNew(savedUser.getUserId(), GameStage.INTRO);
        gameProgressRepository.save(newProgress);

        // 3. 트랜잭션 커밋 후 비동기 작업 시작을 보장
        final UUID userId = savedUser.getUserId();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                    asyncTaskService.initializeGameStateAsync(userId);
            }
        });
        } else {
            // 테스트 환경에서는 즉시 실행
            asyncTaskService.initializeGameStateAsync(userId);
        }

        return userMapper.toResponse(savedUser, GameStage.INTRO);
    }

    // 사용자 id로 사용자 정보 조회
    @Override
    public Optional<UserResponse> getUserById(UUID id) {
        return userRepository.findById(id)
                .map(user -> {
                    GameProgress gameProgress = ExceptionUtils.getGameProgressOrThrow(
                            gameProgressRepository.findByUserId(user.getUserId()), user.getUserId());
                    return userMapper.toResponse(user, gameProgress);
                });
    }

    // 이메일로 사용자 조회
    @Override
    public Optional<UserResponse> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    GameProgress gameProgress = ExceptionUtils.getGameProgressOrThrow(
                            gameProgressRepository.findByUserId(user.getUserId()), user.getUserId());
                    return userMapper.toResponse(user, gameProgress);
                });
    }

    // 사용자가 존재하나요? (이메일로)
    @Override
    public boolean isUserExists(String email) {
        return userRepository.existsByEmail(email);
    }


}
