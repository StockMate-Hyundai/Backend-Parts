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
public class StockRestoreRequestEvent {
    private Long orderId;
    private String orderNumber;
    private List<StockRestoreItem> items;
    private String reason;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StockRestoreItem {
        private Long partId;
        private int amount;
    }
}
