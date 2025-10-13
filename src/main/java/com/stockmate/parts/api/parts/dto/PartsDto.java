package com.stockmate.parts.api.parts.dto;

import com.stockmate.parts.api.parts.entity.Parts;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@Builder
public class PartsDto {
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
    private Integer amount;

    public static PartsDto of(Parts p) {
        return PartsDto.builder()
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
                .amount(p.getAmount())
                .build();
    }
}
