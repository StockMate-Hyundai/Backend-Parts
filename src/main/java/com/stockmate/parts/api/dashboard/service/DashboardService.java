package com.stockmate.parts.api.dashboard.service;

import com.stockmate.parts.api.dashboard.dto.WarehouseInventoryRatioResponseDTO;
import com.stockmate.parts.api.parts.repository.PartsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DashboardService {

    private final PartsRepository partsRepository;

    // 창고별 재고 비중 조회 location 첫 글자(A, B, C, D, E)로 창고를 구분하여 재고 수량과 비중을 계산
    public WarehouseInventoryRatioResponseDTO getWarehouseInventoryRatio() {
        log.info("[DashboardService] 🔍 창고별 재고 비중 조회 시작");

        List<Object[]> result = partsRepository.getWarehouseInventoryRatio();

        // 전체 재고 수량 계산
        long totalQuantity = result.stream()
                .mapToLong(row -> ((Number) row[1]).longValue())
                .sum();

        log.info("[DashboardService] 전체 재고 수량: {}", totalQuantity);

        // 비율 계산
        List<WarehouseInventoryRatioResponseDTO.WarehouseRatio> warehouseRatios = new ArrayList<>();
        
        for (Object[] row : result) {
            String warehouse = (String) row[0];
            Long quantity = ((Number) row[1]).longValue();
            
            double percentage = totalQuantity > 0 
                    ? (quantity * 100.0 / totalQuantity) 
                    : 0.0;
            
            warehouseRatios.add(WarehouseInventoryRatioResponseDTO.WarehouseRatio.builder()
                    .warehouse(warehouse)
                    .totalQuantity(quantity)
                    .percentage(Math.round(percentage * 100.0) / 100.0) // 소수점 둘째 자리까지
                    .build());
        }

        log.info("[DashboardService] 🏁 창고별 재고 비중 조회 완료 | 창고 수: {}", warehouseRatios.size());

        return WarehouseInventoryRatioResponseDTO.builder()
                .warehouses(warehouseRatios)
                .build();
    }
}

