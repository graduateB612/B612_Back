package com.b612.rose.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StarGuideResponse {
    private List<DialogueResponse> dialogues;
    private List<StarGuideEntryResponse> starEntries;
    private int totalPages;
    private int currentPage;
}
