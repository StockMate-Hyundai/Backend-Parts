package com.stockmate.parts.api.parts.dto.parts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockDeductionResponseDto {
    private Long orderId;
    private String orderNumber;
    private String message;
    private boolean success;
}

