package com.stockmate.parts.api.parts.controller;

import com.stockmate.parts.api.parts.dto.CategoryShareDto;
import com.stockmate.parts.api.parts.service.DashboardService;
import com.stockmate.parts.common.response.ApiResponse;
import com.stockmate.parts.common.response.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventory/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/category-share")
    public ResponseEntity<ApiResponse<List<CategoryShareDto>>> getCategoryShare(
            @RequestParam Long userId
    ) {
        var data = dashboardService.getCategoryShare(userId);
        return ApiResponse.success(SuccessStatus.INVENTORY_DASHBOARD_CATEGORY_SUCCESS, data);
    }
}
