package com.stockmate.parts.api.parts.controller;

import com.stockmate.parts.api.parts.dto.common.CategoryAmountDto;
import com.stockmate.parts.api.parts.dto.common.PageResponseDto;
import com.stockmate.parts.api.parts.dto.parts.*;
import com.stockmate.parts.api.parts.service.PartsService;
import com.stockmate.parts.common.response.ApiResponse;
import com.stockmate.parts.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
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
    @PostMapping("/detail")
    public ResponseEntity<ApiResponse<List<PartsDto>>> getPartDetail(
            @RequestBody List<Long> partIds
    ) {
        var data = partsService.getPartDetail(partIds);
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

    @Operation(summary = "지점 부품 전체 조회", description = "본사에서 지점 부품을 조회합니다.")
    @GetMapping("/list/{storeId}")
    public ResponseEntity<ApiResponse<PageResponseDto<PartsDto>>> getStorePartsList(
            @PathVariable("storeId") Long storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var data = partsService.getStoreParts(storeId, page, size);
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

    @Operation(summary = "카테고리별 부품 갯수 조회")
    @GetMapping("/category-amount")
    public ResponseEntity<ApiResponse<List<CategoryAmountDto>>> categoryAmount() {
        var data = partsService.categoryAmount();
        return ApiResponse.success(SuccessStatus.PARTS_CATEGORY_AMOUNT, data);
    }

    @Operation(summary = "재고 차감 API (주문 승인용)")
    @PostMapping("/deduct-stock")
    public ResponseEntity<ApiResponse<StockDeductionResponseDto>> deductStock(
            @RequestBody StockDeductionRequestDto requestDto
    ) {
        partsService.deductStockApi(requestDto);
        
        StockDeductionResponseDto response = StockDeductionResponseDto.builder()
                .orderId(requestDto.getOrderId())
                .orderNumber(requestDto.getOrderNumber())
                .message("재고 차감 성공")
                .success(true)
                .build();
        
        return ApiResponse.success(SuccessStatus.PARTS_STOCK_DEDUCTION_SUCCESS, response);
    }
}
