package com.stockmate.parts.api.parts.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResponseDto {
    private List<AnalysisRowDto> rows;
    private long totalParts;
    private long totalQuantity;
    private long totalShortageStores;

    public static AnalysisResponseDto of(List<AnalysisRowDto> rows) {
        long tp = rows.size();
        long tq = rows.stream().mapToLong(r -> r.getTotalAmount() == null ? 0 : r.getTotalAmount()).sum();
        long ts = rows.stream().mapToLong(r -> r.getShortageStores() == null ? 0 : r.getShortageStores()).sum();
        return AnalysisResponseDto.builder()
                .rows(rows)
                .totalParts(tp)
                .totalQuantity(tq)
                .totalShortageStores(ts)
                .build();
    }
}
