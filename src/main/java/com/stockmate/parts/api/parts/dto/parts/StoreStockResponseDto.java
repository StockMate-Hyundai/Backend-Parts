package com.stockmate.parts.api.parts.dto.parts;

import com.stockmate.parts.api.parts.entity.Parts;
import com.stockmate.parts.api.parts.entity.StoreInventory;
import lombok.*;

import java.util.List;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class StoreStockResponseDto {
    private Long id;
    private String name;
    private Long price;
    private String image;
    private String trim;
    private String model;
    private Integer category;
    private String korName;
    private String engName;
    private String categoryName;
    private String code;
    private String location;
    private Long cost;
    private Integer amount;
    private Integer limitAmount;

    public static StoreStockResponseDto of(Parts p, StoreInventory si) {
        return StoreStockResponseDto.builder()
                .id(p.getId())
                .name(p.getName())
                .price(p.getPrice())
                .image(p.getImage())
                .trim(p.getTrim())
                .model(p.getModel())
                .category(p.getCategory())
                .korName(p.getKorName())
                .engName(p.getEngName())
                .categoryName(p.getCategoryName())
                .code(p.getCode())
                .location(p.getLocation())
                .cost(p.getCost())
                .amount(si != null ? si.getAmount() : null)
                .limitAmount(si != null ? si.getLimitAmount() : null)
                .build();
    }
}
