package com.stockmate.parts.api.parts.dto.store;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class StockCheckResponseDto {
    private Long partId;
    private Integer stock;
    private Boolean canOrder;
}