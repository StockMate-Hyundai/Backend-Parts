package com.stockmate.parts.api.parts.dto.parts;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCheckResponseDto {
    private List<OrderCheckDto> orderList;
    private Integer totalPrice;
}