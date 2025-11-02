package com.stockmate.parts.api.navigation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "알고리즘 비교 결과 DTO")
public class AlgorithmComparisonDTO {
    
    @Schema(description = "각 알고리즘별 결과")
    private Map<String, AlgorithmResult> results;
    
    @Schema(description = "추천 알고리즘", example = "NEAREST_NEIGHBOR_2OPT")
    private String recommendedAlgorithm;
    
    @Schema(description = "부품 개수", example = "10")
    private int partCount;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "알고리즘 실행 결과")
    public static class AlgorithmResult {
        @Schema(description = "알고리즘 이름", example = "Nearest Neighbor")
        private String algorithmName;
        
        @Schema(description = "총 거리", example = "150")
        private int totalDistance;
        
        @Schema(description = "실행 시간 (ms)", example = "5")
        private long executionTimeMs;
        
        @Schema(description = "최적 경로")
        private List<String> route;
        
        @Schema(description = "알고리즘 시간 복잡도", example = "O(n²)")
        private String timeComplexity;
        
        @Schema(description = "알고리즘 정확도", example = "60~80%")
        private String accuracy;
    }
}

