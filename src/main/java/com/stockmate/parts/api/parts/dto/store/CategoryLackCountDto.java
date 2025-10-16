package com.stockmate.parts.api.parts.dto.store;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryLackCountDto {
    private String categoryName;
    private Integer count;
}
