package com.stockmate.parts.api.parts.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategorySumProjection {
    private Long categoryId;    // big category id (nullable)
    private String categoryName;// big category name (nullable)
    private Long amount;        // 해당 카테고리 수량 합계
}
