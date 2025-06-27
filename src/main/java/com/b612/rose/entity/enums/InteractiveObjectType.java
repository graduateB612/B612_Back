package com.b612.rose.entity.enums;

import lombok.Getter;

@Getter
public enum InteractiveObjectType {
    STAR_GUIDE("별 도감"),
    CHARACTER_PROFILE("캐릭터 프로필"),
    REQUEST_FORM("의뢰 작성");

    private final String description;

    InteractiveObjectType(String description) {
        this.description = description;
    }
}