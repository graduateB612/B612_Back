package com.b612.rose.utils;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailContentResult {
    private final String content;
    private final String purifiedTypeName;
    private final String starImagePath;
}


