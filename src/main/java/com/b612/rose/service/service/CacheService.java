package com.b612.rose.service.service;

import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.entity.enums.StarType;

import java.util.UUID;

public interface CacheService {
    
    // 사용자 게임 캐시 초기화
    void initializeUserCache(UUID userId, GameStage initialStage);
    
    // 현재 스테이지 조회 (캐시 우선, 없으면 DB 조회 후 캐시 저장)
    GameStage getCurrentStage(UUID userId);
    
    // 게임 스테이지 업데이트 (DB + 캐시 동시 업데이트)
    void updateGameStage(UUID userId, GameStage newStage);
    
    // 별 상태 업데이트 (DB + 캐시 동시 업데이트)
    void updateStarState(UUID userId, StarType starType, boolean collected, boolean delivered);
    
    // 별 수집 상태 조회 (캐시 우선)
    Boolean isStarCollected(UUID userId, StarType starType);
    
    // 별 전달 상태 조회 (캐시 우선)
    Boolean isStarDelivered(UUID userId, StarType starType);
    
    // 사용자 캐시 완전 삭제
    void clearUserCache(UUID userId);
    
    // 캐시 상태 정보 조회
    int getCacheSize();
    
    // 특정 사용자의 캐시 존재 여부 확인
    boolean hasCacheForUser(UUID userId);
} 