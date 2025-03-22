package com.b612.rose.entity.enums;

import lombok.Getter;

@Getter
public enum PurifiedType {
    PATIENCE("인내"),
    LOVE("사랑"),
    ENLIGHTENMENT("깨달음"),
    PURITY("순수");

    private final String description;

    PurifiedType(String description) {
        this.description = description;
    }

}
