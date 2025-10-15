package com.stockmate.parts.api.parts.dto.parts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCheckReqDto {
    private Long partId;
    private Integer amount;
}
