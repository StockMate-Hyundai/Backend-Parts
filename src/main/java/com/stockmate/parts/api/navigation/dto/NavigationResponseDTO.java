package com.stockmate.parts.api.navigation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "네비게이션 응답 DTO")
public class NavigationResponseDTO {
    
    @Schema(description = "알고리즘 타입", example = "NEAREST_NEIGHBOR")
    private String algorithmType;
    
    @Schema(description = "최적 경로")
    private List<RouteStep> optimizedRoute;
    
    @Schema(description = "총 이동 거리 (Manhattan Distance)", example = "150")
    private int totalDistance;
    
    @Schema(description = "예상 소요 시간 (초) - 총계", example = "180")
    private int estimatedTimeSeconds;
    
    @Schema(description = "걷기 소요 시간 (초)", example = "120")
    private int walkingTimeSeconds;
    
    @Schema(description = "부품 피킹 소요 시간 (초)", example = "48")
    private int pickingTimeSeconds;
    
    @Schema(description = "버퍼 시간 (초)", example = "10")
    private int bufferTimeSeconds;
    
    @Schema(description = "알고리즘 실행 시간 (ms)", example = "5")
    private long executionTimeMs;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "경로 단계")
    public static class RouteStep {
        @Schema(description = "위치 (A5-2 형식 또는 '문', '포장대')", example = "A5-2")
        private String location;
        
        @Schema(description = "위치 설명", example = "브레이크 패드")
        private String description;
        
        @Schema(description = "순서", example = "1")
        private int sequence;
        
        @Schema(description = "이 단계까지의 누적 거리", example = "25")
        private int cumulativeDistance;
        
        @Schema(description = "이전 위치로부터의 거리", example = "25")
        private int distanceFromPrevious;
        
        @Schema(description = "주문 번호", example = "SMO-1")
        private String orderNumber;
        
        @Schema(description = "부품 ID", example = "1")
        private Long partId;
    }
}

