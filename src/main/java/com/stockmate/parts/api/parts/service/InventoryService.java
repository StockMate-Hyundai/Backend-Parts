package com.stockmate.parts.api.parts.service;

import com.stockmate.parts.api.parts.dto.*;
import com.stockmate.parts.api.parts.entity.Parts;
import com.stockmate.parts.api.parts.repository.StoreInventoryRepository;
import com.stockmate.parts.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final StoreInventoryRepository storeInventoryRepository;

    public PageResponseDto<PartsDto> searchParts(Long userId, List<String> categoryName, List<String> trim, List<String> model, int page, int size) {
        if (userId == null || userId <= 0)
            throw new BadRequestException("잘못된 사용자 ID입니다.");
        if (page < 0 || size <= 0)
            throw new BadRequestException("페이지 번호나 사이즈가 유효하지 않습니다.");
        Pageable pageable = PageRequest.of(page, size);
        Page<Parts> result = storeInventoryRepository.searchParts(userId, categoryName, trim, model, pageable);
        Page<PartsDto> mapped = result.map(PartsDto::of);
        return PageResponseDto.from(mapped);
    }

    // 발주 가능 여부
//    public StockCheckResponseDto checkStock(Long partId, Integer amount) {
//        Integer stock = storeInventoryRepository.findAmountByPartId(partId);
//        return StockCheckResponseDto.builder()
//                .partId(partId)
//                .stock(stock)
//                .canOrder(stock < amount ? false : true)
//                .build();
//    }

    //    @Value("${stockmate.hq-user-id:1}")
//    private Long hqUserId  = 1L;

//    @Value("${stockmate.export.tmp-dir:/tmp/stockmate}")
//    private String exportTmpDir = "/tmp/stockmate";

//    public PageResponseDto<InventoryItemDto> getInventories(Long userId, int page, int size, Long categoryId) {
//        if (userId == null || userId <= 0)
//            throw new BadRequestException("잘못된 사용자 ID입니다.");
//        if (page < 0 || size <= 0)
//            throw new BadRequestException("페이지 번호나 사이즈가 유효하지 않습니다.");
//        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
//        Page<StoreInventory> result = storeInventoryRepository.findByUserAndCategory(userId, categoryId, pageable);
//        Page<InventoryItemDto> mapped = result.map(InventoryItemDto::of);
//        return PageResponseDto.from(mapped);
//    }

//    public PageResponseDto<InventoryItemDto> searchInventories(Long userId, String keyword, Long categoryId, int page, int size) {
//        if (userId == null || userId <= 0)
//            throw new BadRequestException("잘못된 사용자 ID입니다.");
//        if (page < 0 || size <= 0)
//            throw new BadRequestException("페이지 번호나 사이즈가 유효하지 않습니다.");
//        // 검색어가 없으면 전체 재고 조회로 대체
//        if (keyword == null || keyword.isBlank()) {
//            return getInventories(userId, page, size, categoryId);
//        }
//        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
//        Page<StoreInventory> result = storeInventoryRepository
//                .findByUserIdAndPart_NameContainingIgnoreCaseAndCategory(userId, keyword, categoryId, pageable);
//
//        Page<InventoryItemDto> mapped = result.map(InventoryItemDto::of);
//        return PageResponseDto.from(mapped);
//    }

//    public PageResponseDto<InventoryItemDto> getUnderLimit(Long userId, int page, int size) {
//        if (userId == null || userId <= 0)
//            throw new BadRequestException("잘못된 사용자 ID입니다.");
//        if (page < 0 || size <= 0)
//            throw new BadRequestException("페이지 번호나 사이즈가 유효하지 않습니다.");
//        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
//        Page<StoreInventory> result = storeInventoryRepository.findUnderLimitByUser(userId, pageable);
//        Page<InventoryItemDto> mapped = result.map(InventoryItemDto::of);
//        return PageResponseDto.from(mapped);
//    }
//
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
