package com.stockmate.parts.api.parts.controller;

import com.stockmate.parts.api.parts.dto.AnalysisRowDto;
import com.stockmate.parts.api.parts.dto.InventoryItemDto;
import com.stockmate.parts.api.parts.dto.PageResponseDto;
import com.stockmate.parts.api.parts.dto.PartsDistributionDto;
import com.stockmate.parts.api.parts.service.InventoryService;
import com.stockmate.parts.common.response.ApiResponse;
import com.stockmate.parts.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;

@Tag(name = "Inventory", description = "재고 관련 API 입니다.")
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "재고조회 API")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<InventoryItemDto>>> getInventories(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categoryId
    ) {
        var data = inventoryService.getInventories(userId, page, size, categoryId);
        return ApiResponse.success(SuccessStatus.INVENTORY_FETCH_SUCCESS, data);
    }

    @Operation(summary = "재고검색 API")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponseDto<InventoryItemDto>>> searchInventories(
            @RequestParam Long userId,
            @RequestParam String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var data = inventoryService.searchInventories(userId, keyword, categoryId, page, size);
        return ApiResponse.success(SuccessStatus.INVENTORY_SEARCH_SUCCESS, data);
    }

    @Operation(summary = "부족재고 조회 API")
    @GetMapping("/under-limit")
    public ResponseEntity<ApiResponse<PageResponseDto<InventoryItemDto>>> getUnderLimitInventories(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var data = inventoryService.getUnderLimit(userId, page, size);
        return ApiResponse.success(SuccessStatus.INVENTORY_UNDER_LIMIT_SUCCESS, data);
    }

    @Operation(summary = "부품 분포 요약-임시 API")
    @GetMapping("/parts/{partId}")
    public ResponseEntity<ApiResponse<PartsDistributionDto>> getPartDistribution(@PathVariable Long partId) {
        var body = inventoryService.getPartDistribution(partId);
        return ApiResponse.success(SuccessStatus.PART_DISTRIBUTION_SUCCESS, body);
    }

    @Operation(summary = "전사 재고 분석-임시 API")
    @GetMapping("/analysis")
    public ResponseEntity<ApiResponse<PageResponseDto<AnalysisRowDto>>> getAnalysis(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var result = inventoryService.getAnalysis(q, page, size);
        return ApiResponse.success(SuccessStatus.INVENTORY_ANALYSIS_SUCCESS, result);
    }

    @Operation(summary = "전사 재고 분석 export API")
    @GetMapping("/analysis/export")
    public ResponseEntity<Resource> exportAnalysis(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "csv") String format
    ) throws IOException {

        Path file;
        String ct;
        String downloadName;

        if ("xlsx".equalsIgnoreCase(format)) {
            // Path f = inventoryService.exportAnalysisXlsx(q);
            // ct = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            // downloadName = "inventory_analysis.xlsx";
            // file = f;
            // (지금은 CSV 기본 제공. XLSX 주석 해제하면 됨)
            throw new IllegalArgumentException("xlsx export not enabled yet");
        } else {
            file = inventoryService.exportAnalysisCsv(q);
            ct = "text/csv";
            downloadName = "inventory_analysis.csv";
        }

        FileSystemResource res = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(ct))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + downloadName)
                .contentLength(res.contentLength())
                .body(res);
    }
}
