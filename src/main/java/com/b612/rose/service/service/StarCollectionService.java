package com.b612.rose.service.service;

import com.b612.rose.entity.enums.StarType;

import java.util.UUID;

public interface StarCollectionService {

    // 별 수집 처리
    void markStarAsCollected(UUID userId, StarType starType);

    // 별 전달 처리
    void markStarAsDelivered(UUID userId, StarType starType);

    // 모든 별이 수집되고 전달되었는지 확인
    boolean areAllStarsCollectedAndDelivered(UUID userId);
} 