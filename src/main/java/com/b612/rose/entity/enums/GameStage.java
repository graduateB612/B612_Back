package com.b612.rose.entity.enums;

import lombok.Getter;

@Getter
public enum GameStage {
    INTRO("첫 화면"),
    GAME_START("게임 화면 진입"),
    COLLECT_PRIDE("별 수집 1단계"),
    DELIVER_PRIDE("별 전달 1단계"),
    COLLECT_ENVY("별 수집 2단계"),
    DELIVER_ENVY("별 전달 2단계"),
    COLLECT_LONELY("별 수집 3단계"),
    DELIVER_LONELY("별 전달 3단계"),
    COLLECT_SAD("별 수집 4단계"),
    DELIVER_SAD("별 전달 4단계"),
    REQUEST_INPUT("고민 작성"),
    NPC_SELECTION("npc 선택"),
    GAME_COMPLETE("게임 클리어");

    private final String description;

    GameStage(String description) {
        this.description = description;
    }
}
