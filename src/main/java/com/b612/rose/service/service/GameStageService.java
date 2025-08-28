package com.b612.rose.service.service;

import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.entity.enums.StarType;

import java.util.UUID;

public interface GameStageService {
    
    // 현재 스테이지 조회
    GameStage getCurrentStage(UUID userId);
    
    // 별 수집 스테이지 조회
    GameStage getCollectStageForStar(StarType starType);
    
    // 별 전달 스테이지 조회
    GameStage getDeliverStageForStar(StarType starType);
    
    // 데이터베이스 스테이지 업데이트
    void updateDatabaseGameStage(UUID userId, GameStage newStage);
} 