package com.b612.rose.entity.enums;

import lombok.Getter;

@Getter
public enum StarType {
    LONELY("외로움"),
    ENVY("질투"),
    SAD("슬픔"),
    PRIDE("교만");

    private final String description;

    StarType(String description) {
        this.description = description;
    }

}
