package com.stockmate.parts.api.parts.service;

import com.stockmate.parts.api.parts.dto.common.CategoryAmountDto;
import com.stockmate.parts.api.parts.dto.common.PageResponseDto;
import com.stockmate.parts.api.parts.dto.parts.*;
import com.stockmate.parts.api.parts.entity.Parts;
import com.stockmate.parts.api.parts.entity.StoreInventory;
import com.stockmate.parts.api.parts.repository.PartsRepository;
import com.stockmate.parts.api.parts.repository.StoreRepository;
import com.stockmate.parts.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PartsService {
    private final PartsRepository partsRepository;
    private final StoreRepository storeRepository;
    private final com.stockmate.parts.api.parts.service.UserService userService;

    // 상세 부품 조회
    public List<PartsDto> getPartDetail(List<Long> partIds) {
        log.info("[부품 상세 조회 요청] partId = {}", partIds);

        List<Parts> parts = partsRepository.findAllById(partIds);

        if (parts.size() != partIds.size()) {
            List<Long> foundIds = parts.stream()
                    .map(Parts::getId)
                    .toList();
            List<Long> missingIds = partIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            log.warn("[부품 조회 실패] 존재하지 않는 ID: {}", missingIds);
            throw new BadRequestException("존재하지 않는 부품 ID: " + missingIds);
        }

        List<PartsDto> response = parts.stream()
                .map(PartsDto::of)
                .toList();

        log.info("[부품 조회 성공] response size : {}", response.size());
        return response;
    }

    // 전체 부품 조회
    public PageResponseDto<PartsDto> getAllParts(int page, int size) {
        if (page < 0 || size <= 0)
            throw new BadRequestException("페이지 번호나 사이즈가 유효하지 않습니다.");
        Pageable pageable = PageRequest.of(page, size);
        Page<Parts> result = partsRepository.findAll(pageable);
        Page<PartsDto> mapped = result.map(PartsDto::of);
        return PageResponseDto.from(mapped);
    }

    // 본사 -> 지점 부품 조회
    public PageResponseDto<StoreStockResponseDto> getStoreParts(Long storeId, int page, int size) {
        if (page < 0 || size <= 0)
            throw new BadRequestException("페이지 번호나 사이즈가 유효하지 않습니다.");
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> result = storeRepository.findByUserId(storeId, pageable);

        Page<StoreStockResponseDto> mapped = result.map(row -> {
            Parts p = (Parts) row[0];
            StoreInventory si = (StoreInventory) row[1];
            return StoreStockResponseDto.of(p, si);
        });
        return PageResponseDto.from(mapped);
    }

    // 차 분류, 모델명, 카테고리명 부품 조회
    public PageResponseDto<PartsDto> getModelCategory(
            List<String> categoryName, List<String> trim, List<String> model, int page, int size
    ) {
        if (page < 0 || size <= 0)
            throw new BadRequestException("페이지 번호나 사이즈가 유효하지 않습니다.");
        Pageable pageable = PageRequest.of(page, size);
        Page<Parts> result = partsRepository.findByCategoryAndModel(categoryName, trim, model, pageable);
        Page<PartsDto> mapped = result.map(PartsDto::of);
        return PageResponseDto.from(mapped);
    }

    // 부족 재고 조회
    public PageResponseDto<PartsDto> getLackStock(
            int amount, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Parts> result = partsRepository.findByAmountLessThanEqual(amount, pageable);
        Page<PartsDto> mapped = result.map(PartsDto::of);
        return PageResponseDto.from(mapped);
    }

    // 발주 가능 여부
    public OrderCheckResponseDto checkStock(List<OrderCheckReqDto> requests) {
        log.info("==> [checkStock] 발주 가능 여부 확인 요청 시작 | 요청 수: {}", requests.size());
        List<OrderCheckDto> orders = new ArrayList<>();
        int totalAmount = 0;

        for (OrderCheckReqDto req : requests) {
            log.debug(">> 요청 데이터: partId={}, amount={}", req.getPartId(), req.getAmount());

            // 유효성 검사
            if (req.getPartId() == null || req.getPartId() <= 0) {
                log.error("[checkStock] 잘못된 부품 ID 입력: {}", req.getPartId());
                throw new BadRequestException("유효하지 않은 부품 ID입니다.");
            }
            if (req.getAmount() == null || req.getAmount() <= 0) {
                log.error("[checkStock] 요청 수량이 0 이하임: {}", req.getAmount());
                throw new BadRequestException("요청 수량은 0보다 커야 합니다.");
            }

            // 재고 조회
            Parts part = partsRepository.findById(req.getPartId())
                    .orElseThrow(() -> {
                        log.error("[checkStock] 존재하지 않는 부품 ID: {}", req.getPartId());
                        return new BadRequestException("존재하지 않는 부품 ID입니다.");
                    });

            Integer stock = part.getAmount();
            boolean canOrder = stock != null && stock >= req.getAmount();
            totalAmount += req.getAmount() * Integer.parseInt(String.valueOf(part.getPrice()));
            log.info("[checkStock] partId={}, stock={}, requested={}, canOrder={}",
                    req.getPartId(), stock, req.getAmount(), canOrder);

            orders.add(OrderCheckDto.builder()
                    .partId(req.getPartId())
                    .requestedAmount(req.getAmount())
                    .availableStock(stock != null ? stock : 0)
                    .canOrder(canOrder)
                    .categoryName(part.getCategoryName())
                    .name(part.getName())
                    .build());
        }

        log.info("<== [checkStock] 발주 가능 여부 확인 완료 | 결과 개수: {}", orders.size());

        return OrderCheckResponseDto.builder()
                .orderList(orders)
                .totalPrice(totalAmount)
                .build();
    }

    // 카테고리별 재고 갯수
    public List<CategoryAmountDto> categoryAmount() {
        log.info("[PartsService] 🔍 카테고리별 부품 수 조회 시작");

        List<Object[]> result = partsRepository.categoryAmount();

        List<CategoryAmountDto> mapped = result.stream()
                .map(row -> new CategoryAmountDto((String) row[0], ((Long) row[1]).intValue()))
                .toList();

        log.info("[PartsService] 🏁 카테고리별 부품 수 조회 완료 | totalMapped={}", mapped.size());
        return mapped;
    }

    // 창고 구역별 부품 조회
    public List<LocationResponseDto> getLocationParts(String location) {
        List<LocationResponseDto> response = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            List<Parts> parts = partsRepository.getLocationParts(location, i);
            List<PartsDto> mapped = parts.stream()
                    .map(PartsDto::of)
                    .toList();

            LocationResponseDto dto = LocationResponseDto.builder()
                    .floor(i)
                    .parts(mapped)
                    .build();

            response.add(dto);
        }
        return response;
    }

    // API용 재고 차감
    @Transactional
    public void deductStockApi(com.stockmate.parts.api.parts.dto.parts.StockDeductionRequestDto requestDto) {
        log.info("API 재고 차감 시작 - Order ID: {}, Order Number: {}", requestDto.getOrderId(), requestDto.getOrderNumber());

        for (com.stockmate.parts.api.parts.dto.parts.StockDeductionRequestDto.StockDeductionItem item : requestDto.getItems()) {
            Parts part = partsRepository.findById(item.getPartId())
                    .orElseThrow(() -> {
                        log.error("부품을 찾을 수 없음 - Part ID: {}", item.getPartId());
                        return new BadRequestException("부품을 찾을 수 없습니다. Part ID: " + item.getPartId());
                    });

            // 재고 확인
            if (part.getAmount() < item.getAmount()) {
                log.warn("재고 부족 - Part ID: {}, 현재 재고: {}, 요청 수량: {}",
                        item.getPartId(), part.getAmount(), item.getAmount());
                throw new BadRequestException(String.format(
                        "재고가 부족합니다. Part ID: %d, 현재 재고: %d, 요청 수량: %d",
                        item.getPartId(), part.getAmount(), item.getAmount()));
            }

            // 재고 차감
            int newAmount = part.getAmount() - item.getAmount();
            part.setAmount(newAmount);
            partsRepository.save(part);

            log.info("재고 차감 성공 - Part ID: {}, 차감 수량: {}, 남은 재고: {}",
                    item.getPartId(), item.getAmount(), newAmount);
        }

        log.info("API 재고 차감 완료 - Order ID: {}", requestDto.getOrderId());
    }

    // 부품 ID로 본사 및 가맹점별 재고 조회
    public PartDistributionResponseDTO getPartDistribution(Long partId, int page, int size) {
        log.info("[PartsService] 🔍 부품 분포 조회 시작 - Part ID: {}, Page: {}, Size: {}", partId, page, size);

        // 1. 부품 조회 (본사 보유 수량)
        Parts part = partsRepository.findById(partId)
                .orElseThrow(() -> {
                    log.error("[PartsService] ❌ 부품을 찾을 수 없음 - Part ID: {}", partId);
                    return new BadRequestException(com.stockmate.parts.common.response.ErrorStatus.PART_NOT_FOUND_EXCEPTION.getMessage());
                });

        Integer headquartersQuantity = part.getAmount() != null ? part.getAmount() : 0;
        log.info("[PartsService] 본사 보유 수량 - Part ID: {}, Quantity: {}", partId, headquartersQuantity);

        // 2. 가맹점별 재고 조회 (페이지네이션)
        if (page < 0 || size <= 0) {
            throw new BadRequestException("페이지 번호나 사이즈가 유효하지 않습니다.");
        }
        Pageable pageable = PageRequest.of(page, size);

        Page<StoreInventory> storeInventoryPage = storeRepository.findByPartId(partId, pageable);

        log.info("[PartsService] 가맹점 재고 조회 완료 - 총 개수: {}, 현재 페이지: {}", 
                storeInventoryPage.getTotalElements(), storeInventoryPage.getContent().size());

        // 3. User 서버에서 가맹점 정보 조회
        List<Long> userIds = storeInventoryPage.getContent().stream()
                .map(StoreInventory::getUserId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());

        final Map<Long, com.stockmate.parts.api.parts.dto.parts.UserBatchResponseDTO> userMap;
        if (!userIds.isEmpty()) {
            userMap = userService.getUsersByMemberIds(userIds);
            log.info("[PartsService] 사용자 정보 조회 완료 - 조회된 사용자 수: {}", userMap.size());
        } else {
            userMap = new java.util.HashMap<>();
        }

        // 4. DTO 변환
        final Map<Long, com.stockmate.parts.api.parts.dto.parts.UserBatchResponseDTO> finalUserMap = userMap;
        Page<PartDistributionResponseDTO.StoreDistributionItem> storeItems = storeInventoryPage.map(storeInventory -> {
            com.stockmate.parts.api.parts.dto.parts.UserBatchResponseDTO userInfo = finalUserMap.get(storeInventory.getUserId());
            return PartDistributionResponseDTO.StoreDistributionItem.builder()
                    .userId(storeInventory.getUserId())
                    .quantity(storeInventory.getAmount() != null ? storeInventory.getAmount() : 0)
                    .storeInfo(userInfo)
                    .build();
        });

        PartDistributionResponseDTO response = PartDistributionResponseDTO.builder()
                .partId(partId)
                .partName(part.getKorName() != null ? part.getKorName() : part.getName())
                .headquartersQuantity(headquartersQuantity)
                .stores(PageResponseDto.from(storeItems))
                .build();

        log.info("[PartsService] 🏁 부품 분포 조회 완료 - Part ID: {}", partId);
        return response;
    }
}
