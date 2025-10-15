package com.stockmate.parts.api.parts.dto.parts;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCheckResponseDto {
    private Long partId;
    private Integer requestedAmount;
    private Integer availableStock;
    private Boolean canOrder;
}