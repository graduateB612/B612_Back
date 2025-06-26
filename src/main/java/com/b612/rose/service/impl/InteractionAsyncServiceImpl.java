package com.b612.rose.service.impl;

import com.b612.rose.entity.domain.InteractiveObject;
import com.b612.rose.entity.domain.UserInteraction;
import com.b612.rose.entity.enums.InteractiveObjectType;
import com.b612.rose.exception.BusinessException;
import com.b612.rose.exception.ErrorCode;
import com.b612.rose.repository.InteractiveObjectRepository;
import com.b612.rose.repository.UserInteractionRepository;
import com.b612.rose.service.service.InteractionAsyncService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InteractionAsyncServiceImpl implements InteractionAsyncService {

    private final InteractiveObjectRepository interactiveObjectRepository;
    private final UserInteractionRepository userInteractionRepository;

    @Async("taskExecutor")
    @Transactional
    @Override
    public void updateInteractionAsync(UUID userId, InteractiveObjectType objectType) {
        try {
            log.info("비동기 사용자 상호작용 기록 시작: userId={}, objectType={}", userId, objectType);

            InteractiveObject object = interactiveObjectRepository.findByObjectType(objectType)
                    .orElseThrow(() -> new BusinessException(ErrorCode.OBJECT_NOT_FOUND,
                            "오브젝트를 찾을 수 없습니다: " + objectType));

            UserInteraction interaction = userInteractionRepository
                    .findByUserIdAndObjectId(userId, object.getObjectId())
                    .orElse(null);

            if (interaction == null) {
                interaction = UserInteraction.builder()
                        .userId(userId)
                        .objectId(object.getObjectId())
                        .hasInteracted(true)
                        .isActive(true)
                        .interactedAt(LocalDateTime.now())
                        .build();
            } else {
                interaction = UserInteraction.builder()
                        .interactionId(interaction.getInteractionId())
                        .userId(interaction.getUserId())
                        .objectId(interaction.getObjectId())
                        .hasInteracted(true)
                        .isActive(interaction.isActive())
                        .interactedAt(LocalDateTime.now())
                        .build();
            }

            userInteractionRepository.save(interaction);
            log.info("비동기 사용자 상호작용 기록 완료: userId={}, objectType={}", userId, objectType);
        } catch (Exception e) {
            log.error("비동기 사용자 상호작용 기록 실패: userId={}, objectType={}, error={}",
                    userId, objectType, e.getMessage(), e);
        }
    }
}
