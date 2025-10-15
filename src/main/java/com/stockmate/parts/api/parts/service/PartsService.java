package com.stockmate.parts.api.parts.service;

import com.stockmate.parts.api.parts.dto.common.PageResponseDto;
import com.stockmate.parts.api.parts.dto.parts.OrderCheckDto;
import com.stockmate.parts.api.parts.dto.parts.OrderCheckReqDto;
import com.stockmate.parts.api.parts.dto.parts.OrderCheckResponseDto;
import com.stockmate.parts.api.parts.dto.parts.PartsDto;
import com.stockmate.parts.api.parts.entity.Parts;
import com.stockmate.parts.api.parts.repository.PartsRepository;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PartsService {
    private final PartsRepository partsRepository;

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

    // id로 조회
//    public PartsDto getParts(Long id) {}

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
                    .build());
        }

        log.info("<== [checkStock] 발주 가능 여부 확인 완료 | 결과 개수: {}", orders.size());

        OrderCheckResponseDto responses = OrderCheckResponseDto.builder()
                .orderList(orders)
                .totalPrice(totalAmount)
                .build();

        return responses;
    }
}
