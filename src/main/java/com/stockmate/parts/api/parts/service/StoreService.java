package com.stockmate.parts.api.parts.service;

import com.stockmate.parts.api.parts.dto.common.PageResponseDto;
import com.stockmate.parts.api.parts.dto.common.CategoryAmountDto;
import com.stockmate.parts.api.parts.dto.store.ReleasedItemDTO;
import com.stockmate.parts.api.parts.dto.store.StockReleaseRequestDTO;
import com.stockmate.parts.api.parts.dto.store.StorePartsDto;
import com.stockmate.parts.api.parts.entity.Parts;
import com.stockmate.parts.api.parts.entity.StoreInventory;
import com.stockmate.parts.api.parts.repository.StoreRepository;
import com.stockmate.parts.api.parts.repository.PartsRepository;
import com.stockmate.parts.common.exception.BadRequestException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final PartsRepository partsRepository;
    private final WebClient webClient;

    @Value("${information.server.url}")
    private String informationServerUrl;

    public PageResponseDto<StorePartsDto> searchParts(
            Long userId, List<String> categoryName, List<String> trim, List<String> model,
            int page, int size
    ) {
        log.info("[StoreService] 🔍 지점 부품 조회 시작 | userId={}, page={}, size={}, categoryName={}, trim={}, model={}",
                userId, page, size, categoryName, trim, model);

        if (userId == null || userId <= 0) {
            log.error("[StoreService] ❌ 잘못된 사용자 ID: {}", userId);
            throw new BadRequestException("잘못된 사용자 ID입니다.");
        }
        if (page < 0 || size <= 0) {
            log.error("[StoreService] ❌ 잘못된 페이지 요청 | page={}, size={}", page, size);
            throw new BadRequestException("페이지 번호나 사이즈가 유효하지 않습니다.");
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> result = storeRepository.searchParts(userId, categoryName, trim, model, pageable);
        log.info("[StoreService] ✅ 검색 결과 조회 완료 | totalElements={}, totalPages={}",
                result.getTotalElements(), result.getTotalPages());

        Page<StorePartsDto> mapped = result.map(row -> {
            Parts part = (Parts) row[0];
            StoreInventory storeInventory = (StoreInventory) row[1];
            Boolean isLack = row[2] == null ? false : (Boolean) row[2];
            return StorePartsDto.of(part, storeInventory, isLack);
        });

        log.info("[StoreService] 🏁 searchParts() 종료");
        return PageResponseDto.from(mapped);
    }

    // 카테고리별 부족 재고 조회
    public PageResponseDto<StorePartsDto> getUnderLimit(Long userId, String categoryName, int page, int size) {
        log.info("[StoreService] 🔍 부족 재고 조회 시작 | userId={}, categoryName={}, page={}, size={}",
                userId, categoryName, page, size);

        if (userId == null || userId <= 0) {
            log.error("[StoreService] ❌ 잘못된 사용자 ID: {}", userId);
            throw new BadRequestException("잘못된 사용자 ID입니다.");
        }
        if (page < 0 || size <= 0) {
            log.error("[StoreService] ❌ 잘못된 페이지 요청 | page={}, size={}", page, size);
            throw new BadRequestException("페이지 번호나 사이즈가 유효하지 않습니다.");
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> result = storeRepository.findUnderLimitByCategory(userId, categoryName, pageable);
        Page<StorePartsDto> mapped = result.map(row -> {
            Parts part = (Parts) row[0];
            StoreInventory storeInventory = (StoreInventory) row[1];
            Boolean isLack = row[2] != null && (Boolean) row[2];
            return StorePartsDto.of(part, storeInventory, isLack);
        });

        log.info("[StoreService] 🏁 getUnderLimit() 종료");
        return PageResponseDto.from(mapped);
    }

    // 카테고리별 부족 제품 갯수
    public List<CategoryAmountDto> getCategoryLackCount(Long userId) {
        log.info("[StoreService] 🔍 카테고리별 부족 재고 수 조회 시작 | userId={}", userId);

        if (userId == null || userId <= 0) {
            log.error("[StoreService] ❌ 잘못된 사용자 ID: {}", userId);
            throw new BadRequestException("잘못된 사용자 ID입니다.");
        }

        List<Object[]> result = storeRepository.countLackPartsByCategory(userId);

        log.info("[StoreService] ✅ 카테고리별 부족 재고 수 조회 완료 | totalCategories={}", result.size());
        return result.stream()
                .map(row -> new CategoryAmountDto((String) row[0], ((Long) row[1]).intValue()))
                .toList();
    }

    // 부품명으로 검색
    @Transactional
    public PageResponseDto<StorePartsDto> findByName(Long userId, String name, int page, int size) {
        log.info("[StoreService] 🔍 부품명 검색 시작 | userId={}, name='{}', page={}, size={}", userId, name, page, size);

        if (userId == null || userId <= 0) {
            log.error("[StoreService] ❌ 잘못된 사용자 ID: {}", userId);
            throw new BadRequestException("잘못된 사용자 ID입니다.");
        }
        if (page < 0 || size <= 0) {
            log.error("[StoreService] ❌ 잘못된 페이지 요청 | page={}, size={}", page, size);
            throw new BadRequestException("페이지 번호나 사이즈가 유효하지 않습니다.");
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> result = storeRepository.findByName(userId, name, pageable);
        log.info("[StoreService] ✅ JPQL 조회 완료 | totalElements={}, totalPages={}",
                result.getTotalElements(), result.getTotalPages());

        Page<StorePartsDto> mapped = result.map(row -> {
            Parts part = (Parts) row[0];
            StoreInventory storeInventory = (StoreInventory) row[1];
            Boolean isLack = row[2] != null && (Boolean) row[2];
            return StorePartsDto.of(part, storeInventory, isLack);
        });

        log.info("[StoreService] 🏁 findByName() 종료 | mappedSize={}", mapped.getContent().size());

        return PageResponseDto.from(mapped);
    }

    // 최소 필요 수량 변경
    @Transactional
    public void updateLimitAmount(Long userId, Long partId, Integer newLimit) {
        log.info("[StoreService] 🔧 최소 수량 변경 요청 | userId={}, partId={}, newLimitAmount={}", userId, partId, newLimit);

        if (userId == null || userId <= 0 || partId == null || partId <= 0) {
            log.error("[StoreService] ❌ 잘못된 ID 값 | userId={}, partId={}", userId, partId);
            throw new BadRequestException("잘못된 사용자 또는 부품 ID입니다.");
        }
        if (newLimit == null || newLimit < 0) {
            log.error("[StoreService] ❌ 잘못된 최소 수량 | newLimitAmount={}", newLimit);
            throw new BadRequestException("최소 수량은 0 이상이어야 합니다.");
        }

        StoreInventory storeInventory = storeRepository.findStoreInventoryByUserIdAndPartId(userId, partId)
                .orElseThrow(() -> {
                    log.error("[StoreService] ❌ 해당 부품이 존재하지 않음 | userId={}, partId={}", userId, partId);
                    return new BadRequestException("해당 부품이 존재하지 않습니다.");
                });

        storeInventory.setLimitAmount(newLimit);
        storeRepository.save(storeInventory);

        log.info("[StoreService] ✅ 최소 수량 변경 완료 | userId={}, partId={}, newLimitAmount={}", userId, partId, newLimit);
    }

//    @Value("${stockmate.export.tmp-dir:/tmp/stockmate}")
//    private String exportTmpDir = "/tmp/stockmate";

//    public PartsDistributionDto getPartDistribution(Long partId) {
//        Long hqAmount = storeInventoryRepository.sumAmountByPartAndStore(partId, hqUserId);
//        long underLimitCnt = storeInventoryRepository.countStoresUnderLimit(partId);
//        return PartsDistributionDto.builder()
//                .partId(partId)
//                .hqAmount(hqAmount == null ? 0L : hqAmount)
//                .storeUnderLimitCount(underLimitCnt)
//                .build();
//    }
//
//    public PageResponseDto<AnalysisRowDto> getAnalysis(String q, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        Page<AnalysisRowDto> rows = storeInventoryRepository.analyzeAll(pageable, blankToNull(q));
//        return PageResponseDto.from(rows);
//    }
//
//    /** CSV 내보내기 */
//    public Path exportAnalysisCsv(String q) throws IOException {
//        Files.createDirectories(Path.of(exportTmpDir));
//        Path file = Path.of(exportTmpDir, "inventory_analysis.csv");
//
//        // 데이터
//        List<AnalysisRowDto> rows = storeInventoryRepository
//                .analyzeAll(Pageable.unpaged(), blankToNull(q)).getContent();
//
//        try (BufferedWriter writer = Files.newBufferedWriter(file);
//             CSVPrinter csv = new CSVPrinter(writer, CSVFormat.DEFAULT
//                     .withHeader("partId","partName","price","totalAmount","shortageStores"))) {
//            for (AnalysisRowDto r : rows) {
//                csv.printRecord(r.getPartId(), r.getPartName(), r.getPrice(),
//                        nullToZero(r.getTotalAmount()), nullToZero(r.getShortageStores()));
//            }
//        }
//        return file;
//    }
//
//    private String blankToNull(String s) { return (s == null || s.isBlank()) ? null : s; }
//    private long nullToZero(Long v) { return v == null ? 0L : v; }

    // 가맹점 부품 재고 등록/수정 API
    @Transactional
    public void updateStoreInventory(Long memberId, List<com.stockmate.parts.api.parts.dto.StoreInventoryItemDTO> items) {
        log.info("[StoreService] 가맹점 부품 재고 업데이트 시작 - 가맹점 ID: {}, 아이템 수: {}", memberId, items.size());

        for (com.stockmate.parts.api.parts.dto.StoreInventoryItemDTO item : items) {
            Long partId = item.getPartId();
            int quantity = item.getQuantity();

            log.info("[StoreService] 부품 재고 추가 - Part ID: {}, Quantity: {}", partId, quantity);

            Parts part = partsRepository.findById(partId)
                    .orElseThrow(() -> {
                        log.error("[StoreService] ❌ 부품을 찾을 수 없음 - Part ID: {}", partId);
                        return new BadRequestException("부품을 찾을 수 없습니다: " + partId);
                    });

            StoreInventory storeInventory = storeRepository.findStoreInventoryByUserIdAndPartId(memberId, partId)
                    .orElse(StoreInventory.builder()
                            .userId(memberId)
                            .part(part)
                            .amount(0)
                            .limitAmount(0)
                            .build());

            storeInventory.setAmount((storeInventory.getAmount() != null ? storeInventory.getAmount() : 0) + quantity);
            storeRepository.save(storeInventory);

            log.info("[StoreService] ✅ 부품 재고 추가 완료 - Part ID: {}, 추가 수량: {}, 현재 재고: {}",
                    partId, quantity, storeInventory.getAmount());
        }

        log.info("[StoreService] ✅ 가맹점 부품 재고 업데이트 완료 - 가맹점 ID: {}", memberId);
    }

    // 가맹점 부품 출고 처리 API
    @Transactional
    public void releaseStock(StockReleaseRequestDTO requestDTO, Long requesterMemberId) {
        log.info("[StoreService] 🚚 가맹점 부품 출고 처리 시작 - 가맹점 ID: {}, 출고 아이템 수: {}", 
                requesterMemberId, requestDTO.getItems().size());

        Long memberId = requesterMemberId;
        List<ReleasedItemDTO> releasedItems = new java.util.ArrayList<>();

        for (com.stockmate.parts.api.parts.dto.store.StockReleaseRequestDTO.StockReleaseItem item : requestDTO.getItems()) {
            String partCode = item.getPartCode();
            int quantity = item.getQuantity();

            log.info("[StoreService] 부품 출고 처리 - Part Code: {}, Quantity: {}", partCode, quantity);

            // 1. 가맹점 ID + 부품 코드로 직접 재고 조회
            StoreInventory storeInventory = storeRepository.findByUserIdAndPartCode(memberId, partCode)
                    .orElseThrow(() -> {
                        log.error("[StoreService] ❌ 가맹점에 해당 부품 재고가 없음 - Member ID: {}, Part Code: {}", 
                                memberId, partCode);
                        return new BadRequestException(String.format(
                                "가맹점에 해당 부품 재고가 없습니다. Part Code: %s", partCode));
                    });

            // 2. 재고 확인
            int currentAmount = storeInventory.getAmount() != null ? storeInventory.getAmount() : 0;
            if (currentAmount < quantity) {
                log.error("[StoreService] ❌ 재고 부족 - Part Code: {}, 현재 재고: {}, 요청 수량: {}", 
                        partCode, currentAmount, quantity);
                throw new BadRequestException(String.format(
                        "재고가 부족합니다. Part Code: %s, 현재 재고: %d, 요청 수량: %d", 
                        partCode, currentAmount, quantity));
            }

            // 3. 재고 차감
            int newAmount = currentAmount - quantity;
            storeInventory.setAmount(newAmount);
            storeRepository.save(storeInventory);

            log.info("[StoreService] ✅ 부품 출고 완료 - Part Code: {}, 출고 수량: {}, 남은 재고: {}", 
                    partCode, quantity, newAmount);

            // 4. 출고 결과 추가
            releasedItems.add(com.stockmate.parts.api.parts.dto.store.ReleasedItemDTO.builder()
                    .partId(storeInventory.getPart().getId())
                    .partCode(partCode)
                    .partName(storeInventory.getPart().getKorName())
                    .releasedQuantity(quantity)
                    .remainingQuantity(newAmount)
                    .build());
        }

        log.info("[StoreService] 🏁 가맹점 부품 출고 처리 완료 - 가맹점 ID: {}, 출고 부품 종류 수: {}", 
                memberId, releasedItems.size());

        // Information 서버에 출고 히스토리 등록
        registerReleaseHistory(memberId, releasedItems);
    }

    // Information 서버에 출고 히스토리 등록
    private void registerReleaseHistory(Long memberId, java.util.List<com.stockmate.parts.api.parts.dto.store.ReleasedItemDTO> releasedItems) {
        log.info("[StoreService] Information 서버 출고 히스토리 등록 시작 - 가맹점 ID: {}", memberId);

        // 출고 메시지 생성
        String message = String.format("부품 출고: %d개 품목 출고 완료", releasedItems.size());
        
        // 부품 간단 정보를 items 리스트로 변환 (ID, 수량만)
        java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
        for (com.stockmate.parts.api.parts.dto.store.ReleasedItemDTO item : releasedItems) {
            java.util.Map<String, Object> itemMap = new java.util.HashMap<>();
            itemMap.put("partId", item.getPartId());
            itemMap.put("quantity", item.getReleasedQuantity());
            items.add(itemMap);
        }
        
        java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("memberId", memberId);
        requestBody.put("orderNumber", null); // 출고는 주문 번호 없음
        requestBody.put("message", message);
        requestBody.put("status", "RELEASED");
        requestBody.put("type", "RELEASE");
        requestBody.put("items", items); // 부품 상세 정보 추가

        try {
            String response = webClient.post()
                    .uri(informationServerUrl + "/api/v1/information/order-history")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("[StoreService] Information 서버 출고 히스토리 등록 성공 - 응답: {}", response);
        } catch (Exception e) {
            log.error("[StoreService] Information 서버 출고 히스토리 등록 실패 - 에러: {}", e.getMessage(), e);
            // 출고 히스토리 등록 실패는 전체 트랜잭션을 롤백하지 않음 (출고는 이미 완료됨)
        }
    }
}
