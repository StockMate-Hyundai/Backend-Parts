package com.stockmate.parts.api.parts.controller;

import com.stockmate.parts.api.parts.dto.common.PageResponseDto;
import com.stockmate.parts.api.parts.dto.common.CategoryAmountDto;
import com.stockmate.parts.api.parts.dto.store.StorePartsDto;
import com.stockmate.parts.api.parts.service.StoreService;
import com.stockmate.parts.common.config.swagger.security.SecurityUser;
import com.stockmate.parts.common.response.ApiResponse;
import com.stockmate.parts.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Store", description = "지점 재고 관련 API 입니다.")
@RestController
@RequestMapping("/api/v1/store")
@RequiredArgsConstructor
@Slf4j
public class StoreController {

    private final StoreService storeService;

    @Operation(summary = "지점 재고 조회")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponseDto<StorePartsDto>>> getInventories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) List<String> categoryName,
            @RequestParam(required = false) List<String> trim,
            @RequestParam(required = false) List<String> model,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        long userId = securityUser.getMemberId();
        var data = storeService.searchParts(userId, categoryName, trim, model, page, size);
        return ApiResponse.success(SuccessStatus.STORE_SEARCH_SUCCESS, data);
    }

    @Operation(summary = "카테고리별 부족 재고 조회")
    @GetMapping("/under-limit")
    public ResponseEntity<ApiResponse<PageResponseDto<StorePartsDto>>> getUnderLimitInventories(
            @RequestParam(required = false) String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        long userId = securityUser.getMemberId();
        var data = storeService.getUnderLimit(userId, categoryName, page, size);
        return ApiResponse.success(SuccessStatus.STORE_UNDER_LIMIT_SUCCESS, data);
    }

    @Operation(summary = "카테고리별 부족 재고 갯수 조회")
    @GetMapping("/lack-count")
    public ResponseEntity<ApiResponse<List<CategoryAmountDto>>> getCategoryLackCount(
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        long userId = securityUser.getMemberId();
        var data = storeService.getCategoryLackCount(userId);
        return ApiResponse.success(SuccessStatus.STORE_CATEGORY_LACK_COUNT_SUCCESS, data);
    }

    @Operation(summary = "부품명으로 검색")
    @GetMapping("/find-name")
    public ResponseEntity<ApiResponse<PageResponseDto<StorePartsDto>>> findByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        long userId = securityUser.getMemberId();
        var data = storeService.findByName(userId, name, page, size);
        return ApiResponse.success(SuccessStatus.STORE_FIND_NAME_SUCCESS, data);
    }

    @Operation(summary = "최소 필요 수량 변경")
    @PutMapping("/update-limit")
    public ResponseEntity<ApiResponse<Void>> updateLimitAmount(
            @RequestParam Long partId,
            @RequestParam Integer newLimit,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        long userId = securityUser.getMemberId();
        storeService.updateLimitAmount(userId, partId, newLimit);
        return ApiResponse.success(SuccessStatus.STORE_LIMIT_UPDATE_SUCCESS, null);
    }

    @Operation(summary = "가맹점 부품 재고 등록/수정 API", description = "가맹점의 부품 재고를 등록하거나 수정합니다.")
    @PostMapping("/inventory/update")
    public ResponseEntity<ApiResponse<com.stockmate.parts.api.parts.dto.StoreInventoryUpdateResponseDTO>> updateStoreInventory(
            @RequestBody com.stockmate.parts.api.parts.dto.StoreInventoryUpdateRequestDTO requestDTO) {

        log.info("가맹점 부품 재고 업데이트 요청 - 가맹점 ID: {}, 아이템 수: {}", 
                requestDTO.getMemberId(), requestDTO.getItems().size());

        storeService.updateStoreInventory(requestDTO.getMemberId(), requestDTO.getItems());

        com.stockmate.parts.api.parts.dto.StoreInventoryUpdateResponseDTO response = 
                com.stockmate.parts.api.parts.dto.StoreInventoryUpdateResponseDTO.builder()
                .memberId(requestDTO.getMemberId())
                .message("재고 업데이트 성공")
                .updatedItemCount(requestDTO.getItems().size())
                .build();

        log.info("가맹점 부품 재고 업데이트 완료 - 가맹점 ID: {}, 업데이트된 아이템 수: {}", 
                requestDTO.getMemberId(), requestDTO.getItems().size());

        return ApiResponse.success(SuccessStatus.UPDATE_STORE_INVENTORY_SUCCESS, response);
    }

    @Operation(summary = "가맹점 부품 출고 처리 API", description = "가맹점의 부품을 출고 처리합니다. 부품 코드로 조회하여 재고를 차감합니다.")
    @PostMapping("/release")
    public ResponseEntity<ApiResponse<Void>> releaseStock(
            @RequestBody com.stockmate.parts.api.parts.dto.store.StockReleaseRequestDTO requestDTO,
            @AuthenticationPrincipal SecurityUser securityUser) {

        log.info("가맹점 부품 출고 처리 요청 - 가맹점 ID: {}, 요청자 ID: {}, 출고 아이템 수: {}", 
                requestDTO.getMemberId(), securityUser.getMemberId(), requestDTO.getItems().size());

        storeService.releaseStock(requestDTO, securityUser.getMemberId());

        log.info("가맹점 부품 출고 처리 완료 - 가맹점 ID: {}", requestDTO.getMemberId());
        return ApiResponse.success_only(SuccessStatus.RELEASE_STOCK_SUCCESS);
    }

//
//    @Operation(summary = "부품 분포 요약-임시 API")
//    @GetMapping("/parts/{partId}")
//    public ResponseEntity<ApiResponse<PartsDistributionDto>> getPartDistribution(@PathVariable Long partId) {
//        var body = inventoryService.getPartDistribution(partId);
//        return ApiResponse.success(SuccessStatus.PART_DISTRIBUTION_SUCCESS, body);
//    }
//
//
//    @Operation(summary = "전사 재고 분석-임시 API")
//    @GetMapping("/analysis")
//    public ResponseEntity<ApiResponse<PageResponseDto<AnalysisRowDto>>> getAnalysis(
//            @RequestParam(required = false) String q,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size
//    ) {
//        var result = inventoryService.getAnalysis(q, page, size);
//        return ApiResponse.success(SuccessStatus.INVENTORY_ANALYSIS_SUCCESS, result);
//    }

//    @Operation(summary = "전사 재고 분석 export API")
//    @GetMapping("/analysis/export")
//    public ResponseEntity<Resource> exportAnalysis(
//            @RequestParam(required = false) String q,
//            @RequestParam(defaultValue = "csv") String format
//    ) throws IOException {
//
//        Path file;
//        String ct;
//        String downloadName;
//
//        if ("xlsx".equalsIgnoreCase(format)) {
//            // Path f = inventoryService.exportAnalysisXlsx(q);
//            // ct = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
//            // downloadName = "inventory_analysis.xlsx";
//            // file = f;
//            // (지금은 CSV 기본 제공. XLSX 주석 해제하면 됨)
//            throw new IllegalArgumentException("xlsx export not enabled yet");
//        } else {
//            file = inventoryService.exportAnalysisCsv(q);
//            ct = "text/csv";
//            downloadName = "inventory_analysis.csv";
//        }
//
//        FileSystemResource res = new FileSystemResource(file);
//        return ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType(ct))
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + downloadName)
//                .contentLength(res.contentLength())
//                .body(res);
//    }
}
