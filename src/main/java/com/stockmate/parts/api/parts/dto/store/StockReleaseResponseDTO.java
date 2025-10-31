package com.stockmate.parts.api.parts.dto.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReleaseResponseDTO {
    private String message; // 응답 메시지
    private boolean success; // 성공 여부
}
