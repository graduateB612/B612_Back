package com.b612.rose.dto.response;

import com.b612.rose.entity.enums.InteractiveObjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectStatusResponse {
    private InteractiveObjectType objectType;
    private boolean hasInteracted;
    private boolean isActive;
}
