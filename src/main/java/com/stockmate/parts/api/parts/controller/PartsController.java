package com.stockmate.parts.api.parts.controller;

import com.stockmate.parts.api.parts.dto.common.PageResponseDto;
import com.stockmate.parts.api.parts.dto.parts.OrderCheckReqDto;
import com.stockmate.parts.api.parts.dto.parts.OrderCheckResponseDto;
import com.stockmate.parts.api.parts.dto.parts.PartsDto;
import com.stockmate.parts.api.parts.service.PartsService;
import com.stockmate.parts.common.response.ApiResponse;
import com.stockmate.parts.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Parts", description = "부품 관련 API")
@RestController
@RequestMapping("/api/v1/parts")
@RequiredArgsConstructor
public class PartsController {
    private final PartsService partsService;

    @Operation(summary = "부품 상세 조회")
    @GetMapping("/detail/{partId}")
    public ResponseEntity<ApiResponse<PartsDto>> getPartDetail(
            @PathVariable Long partId
    ) {
        var data = partsService.getPartDetail(partId);
        return ApiResponse.success(SuccessStatus.PARTS_DETAIL_SUCCESS, data);
    }

    @Operation(summary = "부품 전체 조회")
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<PageResponseDto<PartsDto>>> getPartsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var data = partsService.getAllParts(page, size);
        return ApiResponse.success(SuccessStatus.PARTS_LIST_SUCCESS, data);
    }

    @Operation(summary = "부품 검색")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponseDto<PartsDto>>> getSearchList(
            @RequestParam(required = false) List<String> categoryName,
            @RequestParam(required = false) List<String> trim,
            @RequestParam(required = false) List<String> model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var data = partsService.getModelCategory(categoryName, trim, model, page, size);
        return ApiResponse.success(SuccessStatus.PARTS_MODEL_CATEGORY_SUCCESS, data);
    }

    @Operation(summary = "부족 재고 조회")
    @GetMapping("/lack")
    public ResponseEntity<ApiResponse<PageResponseDto<PartsDto>>> getLackStock(
            @RequestParam(required = false, defaultValue = "5") int amount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var data = partsService.getLackStock(amount, page, size);
        return ApiResponse.success(SuccessStatus.PARTS_LACK_STOCK, data);
    }

    @Operation(summary = "주문 가능 여부 확인")
    @PostMapping("/check")
    public ResponseEntity<ApiResponse<OrderCheckResponseDto>> checkStock(
            @RequestBody List<OrderCheckReqDto> requests
    ) {
        var data = partsService.checkStock(requests);
        return ApiResponse.success(SuccessStatus.PARTS_STOCK_CHECK_SUCCESS, data);
    }
}
