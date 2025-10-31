package com.stockmate.parts.api.parts.dto.parts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockDeductionRequestDto {
    private Long orderId;
    private String orderNumber;
    private List<StockDeductionItem> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StockDeductionItem {
        private Long partId;
        private int amount;
    }
}

