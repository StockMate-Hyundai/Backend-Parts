package com.stockmate.parts.api.parts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreInventoryUpdateRequestDTO {
    private Long memberId; // 가맹점 ID
    private List<StoreInventoryItemDTO> items;
}

