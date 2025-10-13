package com.stockmate.parts.api.parts.service;

import com.stockmate.parts.api.parts.dto.PageResponseDto;
import com.stockmate.parts.api.parts.dto.PartsDto;
import com.stockmate.parts.api.parts.entity.Parts;
import com.stockmate.parts.api.parts.repository.PartsRepository;
import com.stockmate.parts.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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

    // 모델명, 카테고리명 부품 조회
    public PageResponseDto<PartsDto> getModelCategory(
            String categoryName, String model, int page, int size
    ) {
        if (page < 0 || size <= 0)
            throw new BadRequestException("페이지 번호나 사이즈가 유효하지 않습니다.");
        Pageable pageable = PageRequest.of(page, size);
        Page<Parts> result = partsRepository.findByCategoryAndModel(categoryName, model, pageable);
        Page<PartsDto> mapped = result.map(PartsDto::of);
        return PageResponseDto.from(mapped);
    }
}
