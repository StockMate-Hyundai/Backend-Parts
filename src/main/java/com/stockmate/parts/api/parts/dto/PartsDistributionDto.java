package com.stockmate.parts.api.parts.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartsDistributionDto {
    private Long partId;
    private Long hqAmount;             // 본사 보유 수량
    private Long storeUnderLimitCount; // 부족재고 지점 수
}
