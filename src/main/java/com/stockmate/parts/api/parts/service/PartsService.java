package com.stockmate.parts.api.parts.service;

import com.stockmate.parts.api.parts.dto.common.CategoryAmountDto;
import com.stockmate.parts.api.parts.dto.common.PageResponseDto;
import com.stockmate.parts.api.parts.dto.parts.OrderCheckDto;
import com.stockmate.parts.api.parts.dto.parts.OrderCheckReqDto;
import com.stockmate.parts.api.parts.dto.parts.OrderCheckResponseDto;
import com.stockmate.parts.api.parts.dto.parts.PartsDto;
import com.stockmate.parts.api.parts.entity.Parts;
import com.stockmate.parts.api.parts.repository.PartsRepository;
import com.stockmate.parts.common.exception.BadRequestException;
import com.stockmate.parts.common.producer.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PartsService {
    private final PartsRepository partsRepository;
    private final KafkaProducerService kafkaProducerService;

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

            // TODO: 코드 리팩토링
            orders.add(OrderCheckDto.builder()
                    .partId(req.getPartId())
                    .requestedAmount(req.getAmount())
                    .availableStock(stock != null ? stock : 0)
                    .canOrder(canOrder)
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

    // 재고 차감 (Saga Pattern)
    @Transactional
    public void deductStock(com.stockmate.parts.api.parts.dto.StockDeductionRequestEvent event) {
        log.info("재고 차감 시작 - Order ID: {}, Order Number: {}", event.getOrderId(), event.getOrderNumber());

        try {
            for (com.stockmate.parts.api.parts.dto.StockDeductionRequestEvent.StockDeductionItem item : event.getItems()) {
                Parts part = partsRepository.findById(item.getPartId())
                        .orElseThrow(() -> {
                            log.error("부품을 찾을 수 없음 - Part ID: {}", item.getPartId());
                            return new RuntimeException("부품을 찾을 수 없습니다. Part ID: " + item.getPartId());
                        });

                // 재고 확인
                if (part.getAmount() < item.getAmount()) {
                    log.warn("재고 부족 - Part ID: {}, 현재 재고: {}, 요청 수량: {}",
                            item.getPartId(), part.getAmount(), item.getAmount());
                    throw new RuntimeException(String.format(
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

            // 모든 재고 차감 성공 → 성공 이벤트 발행
            com.stockmate.parts.api.parts.dto.StockDeductionSuccessEvent successEvent = 
                    com.stockmate.parts.api.parts.dto.StockDeductionSuccessEvent.builder()
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .build();

            kafkaProducerService.sendStockDeductionSuccess(successEvent);

            log.info("재고 차감 완료 및 성공 이벤트 발행 - Order ID: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("재고 차감 실패 - Order ID: {}, 에러: {}", event.getOrderId(), e.getMessage(), e);

            // 실패 이벤트 발행
            com.stockmate.parts.api.parts.dto.StockDeductionFailedEvent failedEvent = 
                    com.stockmate.parts.api.parts.dto.StockDeductionFailedEvent.builder()
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .reason(e.getMessage())
                    .build();

            kafkaProducerService.sendStockDeductionFailed(failedEvent);

            log.info("재고 차감 실패 이벤트 발행 완료 - Order ID: {}", event.getOrderId());
        }
    }

    // 재고 복구 (보상 트랜잭션)
    @Transactional
    public void restoreStock(com.stockmate.parts.api.parts.dto.StockRestoreRequestEvent event) {
        log.info("재고 복구 시작 - Order ID: {}, Order Number: {}, Reason: {}",
                event.getOrderId(), event.getOrderNumber(), event.getReason());

        try {
            for (com.stockmate.parts.api.parts.dto.StockRestoreRequestEvent.StockRestoreItem item : event.getItems()) {
                Parts part = partsRepository.findById(item.getPartId())
                        .orElseThrow(() -> {
                            log.error("부품을 찾을 수 없음 - Part ID: {}", item.getPartId());
                            return new RuntimeException("부품을 찾을 수 없습니다. Part ID: " + item.getPartId());
                        });

                // 재고 복구
                int newAmount = part.getAmount() + item.getAmount();
                part.setAmount(newAmount);
                partsRepository.save(part);

                log.info("재고 복구 성공 - Part ID: {}, 복구 수량: {}, 현재 재고: {}",
                        item.getPartId(), item.getAmount(), newAmount);
            }

            log.info("재고 복구 완료 - Order ID: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("재고 복구 실패 - Order ID: {}, 에러: {}", event.getOrderId(), e.getMessage(), e);
            // 재고 복구 실패는 심각한 문제이므로 별도 알림/처리 필요
        }
    }
}
