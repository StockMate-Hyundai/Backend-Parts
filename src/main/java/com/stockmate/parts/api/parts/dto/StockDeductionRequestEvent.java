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
public class StockDeductionRequestEvent {
    private Long orderId;
    private String orderNumber;
    private String approvalAttemptId; // Saga 시도 식별자
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