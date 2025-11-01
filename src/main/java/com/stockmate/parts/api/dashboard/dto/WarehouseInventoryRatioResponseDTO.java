package com.stockmate.parts.api.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseInventoryRatioResponseDTO {
    private List<WarehouseRatio> warehouses;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WarehouseRatio {
        private String warehouse;     // 창고 코드 (A, B, C, D, E)
        private Long totalQuantity;   // 총 재고 수량
        private Double percentage;     // 비중 (%)
    }
}

