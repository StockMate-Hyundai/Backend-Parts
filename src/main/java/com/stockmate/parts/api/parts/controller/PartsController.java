package com.stockmate.parts.api.parts.controller;

import com.stockmate.parts.api.parts.dto.PageResponseDto;
import com.stockmate.parts.api.parts.dto.PartsDto;
import com.stockmate.parts.api.parts.entity.Parts;
import com.stockmate.parts.api.parts.service.PartsService;
import com.stockmate.parts.common.response.ApiResponse;
import com.stockmate.parts.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "Parts", description = "부품 관련 API")
@RestController
@RequestMapping("/api/v1/parts")
@RequiredArgsConstructor
public class PartsController {
    private final PartsService partsService;

//    @Operation(summary = "부품 전체 조회")
//    @GetMapping("/list")
//    public ResponseEntity<ApiResponse<PageResponseDto<PartsDto>>> getPartsList(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size
//    ) {
//        var data = partsService.getAllParts(page, size);
//        return ApiResponse.success(SuccessStatus.PARTS_LIST_SUCCESS, data);
//    }

    @Operation(summary = "모델명, 카테고리명으로 부품 조회")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponseDto<PartsDto>>> getSearchList(
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var data = partsService.getModelCategory(categoryName, model, page, size);
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

//    @GetMapping("/parts")
//    public List<Parts> getParts() {
//
//        ArrayList<Parts> parts = new ArrayList<>();
//        parts.add(new Parts(1L,"자동차바퀴",23));
//        parts.add(new Parts(2L,"자동차 엔진",50));
//        parts.add(new Parts(3L,"자동차 쇼바",40));
//
//        return parts;
//    }
}
