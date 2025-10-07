package com.stockmate.parts.api.parts.service;

import com.stockmate.parts.api.parts.dto.CategoryShareDto;
import com.stockmate.parts.api.parts.dto.CategorySumProjection;
import com.stockmate.parts.api.parts.repository.StoreInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.stockmate.parts.common.exception.BadRequestException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final StoreInventoryRepository storeInventoryRepository;

    public List<CategoryShareDto> getCategoryShare(Long userId) {

        if (userId == null || userId <= 0) {
            throw new BadRequestException("잘못된 사용자 ID입니다.");
        }

        List<CategorySumProjection> rows = storeInventoryRepository.sumByBigCategory(userId);
        long total = rows.stream().mapToLong(r -> r.getAmount() == null ? 0L : r.getAmount()).sum();

        return rows.stream().map(r -> {
            long amt = r.getAmount() == null ? 0L : r.getAmount();
            double ratio = (total == 0) ? 0.0 : ((double) amt / (double) total) * 100.0;
            return CategoryShareDto.builder()
                    .categoryId(r.getCategoryId())
                    .categoryName((r.getCategoryName() == null || r.getCategoryName().isBlank())
                            ? "미지정" : r.getCategoryName())
                    .amount(amt)
                    .ratio(Math.round(ratio * 10.0) / 10.0)   // 소수 1자리
                    .build();
        }).toList();
    }
}
