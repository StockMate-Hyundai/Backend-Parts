package com.stockmate.parts.api.parts.service;

import com.stockmate.parts.api.parts.dto.common.PageResponseDto;
import com.stockmate.parts.api.parts.dto.common.CategoryAmountDto;
import com.stockmate.parts.api.parts.dto.store.StorePartsDto;
import com.stockmate.parts.api.parts.dto.ReceivingProcessRequestEvent;
import com.stockmate.parts.api.parts.dto.ReceivingProcessSuccessEvent;
import com.stockmate.parts.api.parts.dto.ReceivingProcessFailedEvent;
import com.stockmate.parts.api.parts.entity.Parts;
import com.stockmate.parts.api.parts.entity.StoreInventory;
import com.stockmate.parts.api.parts.repository.StoreRepository;
import com.stockmate.parts.api.parts.repository.PartsRepository;
import com.stockmate.parts.common.exception.BadRequestException;
import com.stockmate.parts.common.producer.KafkaProducerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final PartsRepository partsRepository;
    private final KafkaProducerService kafkaProducerService;

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

    // 입고 처리 (Kafka Consumer에서 호출)
    @Transactional
    public void processReceivingRequest(ReceivingProcessRequestEvent event) {
        log.info("[StoreService] 📦 입고 처리 시작 - Order ID: {}, Order Number: {}, 가맹점 ID: {}", 
                event.getOrderId(), event.getOrderNumber(), event.getMemberId());

        try {
            // 각 부품별로 재고 추가
            for (ReceivingProcessRequestEvent.ReceivingItemDTO item : event.getItems()) {
                Long partId = item.getPartId();
                int quantity = item.getQuantity();

                log.info("[StoreService] 부품 재고 추가 - Part ID: {}, Quantity: {}", partId, quantity);

                // 부품 존재 확인
                Parts part = partsRepository.findById(partId)
                        .orElseThrow(() -> {
                            log.error("[StoreService] ❌ 부품을 찾을 수 없음 - Part ID: {}", partId);
                            return new BadRequestException("부품을 찾을 수 없습니다: " + partId);
                        });

                // 기존 재고 조회 또는 새로 생성
                StoreInventory storeInventory = storeRepository.findStoreInventoryByUserIdAndPartId(event.getMemberId(), partId)
                        .orElse(StoreInventory.builder()
                                .userId(event.getMemberId())
                                .part(part)
                                .amount(0)
                                .limitAmount(0)
                                .build());

                // 재고 추가 (기존 amount 필드 사용)
                storeInventory.setAmount((storeInventory.getAmount() != null ? storeInventory.getAmount() : 0) + quantity);
                storeRepository.save(storeInventory);

                log.info("[StoreService] ✅ 부품 재고 추가 완료 - Part ID: {}, 추가 수량: {}, 현재 재고: {}", 
                        partId, quantity, storeInventory.getAmount());
            }

            // 성공 이벤트 발송
            ReceivingProcessSuccessEvent successEvent = ReceivingProcessSuccessEvent.builder()
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .approvalAttemptId(event.getApprovalAttemptId())
                    .message("입고 처리 성공")
                    .build();

            kafkaProducerService.sendReceivingProcessSuccess(successEvent);
            log.info("[StoreService] ✅ 입고 처리 성공 이벤트 발송 완료 - Order ID: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("[StoreService] ❌ 입고 처리 실패 - Order ID: {}, Error: {}", event.getOrderId(), e.getMessage(), e);

            // 실패 이벤트 발송
            ReceivingProcessFailedEvent failedEvent = ReceivingProcessFailedEvent.builder()
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .approvalAttemptId(event.getApprovalAttemptId())
                    .errorMessage("입고 처리 실패: " + e.getMessage())
                    .data(e)
                    .build();

            kafkaProducerService.sendReceivingProcessFailed(failedEvent);
            log.info("[StoreService] ❌ 입고 처리 실패 이벤트 발송 완료 - Order ID: {}", event.getOrderId());

            throw e;
        }
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
}
