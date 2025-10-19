package com.stockmate.parts.api.parts.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryAmountDto {
    private String categoryName;
    private Integer count;
}
