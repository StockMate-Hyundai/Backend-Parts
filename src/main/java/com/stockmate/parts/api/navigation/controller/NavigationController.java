package com.stockmate.parts.api.navigation.controller;

import com.stockmate.parts.api.navigation.dto.AlgorithmComparisonDTO;
import com.stockmate.parts.api.navigation.dto.NavigationRequestDTO;
import com.stockmate.parts.api.navigation.dto.NavigationResponseDTO;
import com.stockmate.parts.api.navigation.service.NavigationService;
import com.stockmate.parts.common.response.ApiResponse;
import com.stockmate.parts.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Navigation", description = "창고 네비게이션 관련 API입니다.")
@RestController
@RequestMapping("/api/v1/order/navigation")
@RequiredArgsConstructor
@Slf4j
public class NavigationController {
    
    private final NavigationService navigationService;
    
    @Operation(summary = "최적 경로 계산 API", description = "주문 번호를 기반으로 최적의 피킹 경로를 계산합니다.")
    @PostMapping("/optimal")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<NavigationResponseDTO>> calculateOptimalRoute(
            @RequestBody NavigationRequestDTO requestDTO) {
        
        log.info("최적 경로 계산 요청 - 주문 번호 수: {}", requestDTO.getOrderNumbers().size());
        
        NavigationResponseDTO response = navigationService.calculateOptimalRoute(requestDTO);
        
        log.info("최적 경로 계산 완료 - 알고리즘: {}, 총 거리: {}, 실행 시간: {}ms",
                response.getAlgorithmType(), response.getTotalDistance(), response.getExecutionTimeMs());
        
        return ApiResponse.success(SuccessStatus.NAVIGATION_OPTIMAL_ROUTE_SUCCESS, response);
    }
    
    @Operation(summary = "알고리즘 비교 API", description = "모든 알고리즘을 실행하여 성능을 비교합니다.")
    @PostMapping("/compare")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AlgorithmComparisonDTO>> compareAllAlgorithms(
            @RequestBody NavigationRequestDTO requestDTO) {
        
        log.info("알고리즘 비교 요청 - 주문 번호 수: {}", requestDTO.getOrderNumbers().size());
        
        AlgorithmComparisonDTO response = navigationService.compareAllAlgorithms(requestDTO);
        
        log.info("알고리즘 비교 완료 - 부품 수: {}, 추천 알고리즘: {}",
                response.getPartCount(), response.getRecommendedAlgorithm());
        
        return ApiResponse.success(SuccessStatus.NAVIGATION_ALGORITHM_COMPARISON_SUCCESS, response);
    }
}

