package com.stockmate.parts.api.parts.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRowDto {
    private Long partId;
    private String partName;
    private Long price;
    private Long totalAmount;     // 전사 총수량
    private Long shortageStores;  // 부족재고 지점 수
}
