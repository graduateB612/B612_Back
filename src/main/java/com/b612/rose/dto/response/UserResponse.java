package com.b612.rose.dto.response;

import com.b612.rose.entity.enums.GameStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String userName;
    private String selectedNpc;
    private String concern;
    private GameStage currentStage;
}
