package com.stockmate.parts.api.parts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockDeductionFailedEvent {
    private Long orderId;
    private String orderNumber;
    private String reason;
}