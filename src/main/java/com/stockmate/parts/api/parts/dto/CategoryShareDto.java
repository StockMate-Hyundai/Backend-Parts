package com.stockmate.parts.api.parts.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryShareDto {
    private Long categoryId;      // 대분류 ID
    private String categoryName;  // 대분류 명 (전기/램프 등)
    private Long amount;          // 수량 합계
    private double ratio;         // 전체 대비 %
}
