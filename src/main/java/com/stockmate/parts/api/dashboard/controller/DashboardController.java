package com.stockmate.parts.api.dashboard.controller;

import com.stockmate.parts.api.dashboard.dto.WarehouseInventoryRatioResponseDTO;
import com.stockmate.parts.api.dashboard.service.DashboardService;
import com.stockmate.parts.common.response.ApiResponse;
import com.stockmate.parts.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard", description = "대시보드 관련 API 입니다.")
@RestController
@RequestMapping("/api/v1/parts/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(
            summary = "창고별 재고 비중 조회",
            description = "A, B, C, D, E 창고별 재고 수량과 비중(%)을 조회합니다."
    )
    @GetMapping("/warehouse-ratio")
    public ResponseEntity<ApiResponse<WarehouseInventoryRatioResponseDTO>> getWarehouseInventoryRatio() {
        log.info("창고별 재고 비중 조회 요청");
        WarehouseInventoryRatioResponseDTO response = dashboardService.getWarehouseInventoryRatio();
        log.info("창고별 재고 비중 조회 완료 - 창고 수: {}", response.getWarehouses().size());
        return ApiResponse.success(SuccessStatus.PARTS_WAREHOUSE_RATIO_SUCCESS, response);
    }
}

