package com.b612.rose.entity.enums;

import lombok.Getter;

@Getter
public enum GameStage {
    INTRO("첫 화면"),
    GAME_START("게임 화면 진입"),
    COLLECT_PRIDE("첫번째 별 수집 및 전달 완료"),
    COLLECT_ENVY("두번째 별 수집 완료"),
    DELIVER_ENVY("두번째 별 전달 완료"),
    COLLECT_LONELY("세번째 별 수집 완료"),
    DELIVER_LONELY("세번째 별 전달 완료"),
    COLLECT_SAD("네번째 별 수집 완료"),
    DELIVER_SAD("네번째 별 전달 완료"),
    REQUEST_INPUT("고민 작성 완료"),
    NPC_SELECTION("npc 선택 완료"),
    GAME_COMPLETE("이메일 전송 완료 및 게임 클리어");

    private final String description;

    GameStage(String description) {
        this.description = description;
    }
}
