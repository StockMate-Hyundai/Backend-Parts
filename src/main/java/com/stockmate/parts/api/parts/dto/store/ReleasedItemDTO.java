package com.stockmate.parts.api.parts.dto.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleasedItemDTO {
    private Long partId;            // 부품 ID
    private String partCode;        // 부품 코드
    private String partName;        // 부품 이름
    private int releasedQuantity;   // 출고된 수량
    private int remainingQuantity;  // 남은 재고 수량
}

