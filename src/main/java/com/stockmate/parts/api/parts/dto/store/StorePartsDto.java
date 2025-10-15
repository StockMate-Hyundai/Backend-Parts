package com.stockmate.parts.api.parts.dto.store;

import com.stockmate.parts.api.parts.entity.Parts;
import com.stockmate.parts.api.parts.entity.StoreInventory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class StorePartsDto {
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
    private Integer stock;
    private Integer amount;
    private Integer limitAmount;
    private Boolean isLack;

    public static StorePartsDto of(Parts p, StoreInventory si, Boolean isLack) {
        return StorePartsDto.builder()
                .id(p.getId())
                .name(p.getName())
                .price(p.getPrice())
                .image(p.getImage())
                .image(p.getImage())
                .trim(p.getTrim())
                .model(p.getModel())
                .category(p.getCategory())
                .korName(p.getKorName())
                .engName(p.getEngName())
                .categoryName(p.getCategoryName())
                .stock(p.getAmount())
                .amount(si.getAmount())
                .limitAmount(si.getLimitAmount())
                .isLack(isLack)
                .build();
    }
}
