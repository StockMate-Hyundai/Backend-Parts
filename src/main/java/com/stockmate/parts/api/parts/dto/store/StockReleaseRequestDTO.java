package com.stockmate.parts.api.parts.dto.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReleaseRequestDTO {
    private List<StockReleaseItem> items; // 출고할 부품 목록

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StockReleaseItem {
        private String partCode; // 부품 코드
        private int quantity;    // 출고 수량
    }
}

