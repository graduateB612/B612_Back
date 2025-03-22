package com.b612.rose.service.impl;

import com.b612.rose.dto.request.GameStageUpdateRequest;
import com.b612.rose.dto.response.GameProgressResponse;
import com.b612.rose.entity.domain.GameProgress;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.repository.GameProgressRepository;
import com.b612.rose.repository.UserRepository;
import com.b612.rose.service.service.GameProgressService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameProgressServiceImpl implements GameProgressService {

    private final GameProgressRepository gameProgressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public GameProgressResponse initGameProgress(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        GameProgress newProgress = GameProgress.builder()
                .userId(userId)
                .currentStage(GameStage.INTRO)
                .build();

        GameProgress savedProgress = gameProgressRepository.save(newProgress);
        return convertToResponse(savedProgress);
    }

    @Override
    @Transactional
    public GameProgressResponse updateGameStage(UUID userId, GameStageUpdateRequest requestDto) {
        GameProgress currentProgress = gameProgressRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Game progress not found for user ID: " + userId));

        GameProgress updatedProgress = GameProgress.builder()
                .progressId(currentProgress.getProgressId())
                .userId(currentProgress.getUserId())
                .currentStage(requestDto.getNewStage())
                .build();

        GameProgress savedProgress = gameProgressRepository.save(updatedProgress);
        return convertToResponse(savedProgress);
    }

    @Override
    public GameStage getCurrentStage(UUID userId) {
        return gameProgressRepository.findByUserId(userId)
                .map(GameProgress::getCurrentStage)
                .orElse(null);
    }

    private GameProgressResponse convertToResponse(GameProgress gameProgress) {
        return GameProgressResponse.builder()
                .userId(gameProgress.getUserId())
                .currentStage(gameProgress.getCurrentStage())
                .build();
    }
}
