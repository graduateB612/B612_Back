package com.b612.rose.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StarGuideEntryResponse {
    private Integer entryId;
    private String starName;
    private String starSource;
    private String description;
}
