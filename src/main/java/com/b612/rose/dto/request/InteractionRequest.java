package com.b612.rose.dto.request;

import com.b612.rose.entity.enums.InteractiveObjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteractionRequest {
    private InteractiveObjectType objectType;
}
