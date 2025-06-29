package com.b612.rose.mapper;

import com.b612.rose.dto.request.UserCreateRequest;
import com.b612.rose.dto.response.UserResponse;
import com.b612.rose.entity.domain.GameProgress;
import com.b612.rose.entity.domain.User;
import com.b612.rose.entity.enums.GameStage;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    // UserCreateRequest → User Entity 변환
    public User toEntity(UserCreateRequest request) {
        return User.builder()
                .userName(request.getUserName())
                .isCompleted(false)
                .build();
    }

    // User Entity → UserResponse 변환 (GameProgress 포함)
    public UserResponse toResponse(User user, GameProgress gameProgress) {
        return UserResponse.builder()
                .id(user.getUserId())
                .userName(user.getUserName())
                .selectedNpc(user.getSelectedNpc())
                .concern(user.getConcern())
                .currentStage(gameProgress.getCurrentStage())
                .build();
    }

    // User Entity → UserResponse 변환 (기본 스테이지)
    public UserResponse toResponse(User user, GameStage currentStage) {
        return UserResponse.builder()
                .id(user.getUserId())
                .userName(user.getUserName())
                .selectedNpc(user.getSelectedNpc())
                .concern(user.getConcern())
                .currentStage(currentStage)
                .build();
    }

    // 게임 완료 시 User Entity 업데이트
    public User updateForGameCompletion(User user, String email, String concern, String selectedNpc) {
        return User.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(email)
                .concern(concern)
                .selectedNpc(selectedNpc)
                .isCompleted(true)
                .build();
    }
} 