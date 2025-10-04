package com.stockmate.parts.api.parts.dto;

import com.stockmate.parts.api.parts.entity.Parts;
import com.stockmate.parts.api.parts.entity.StoreInventory;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemDto {
    private Long partId;
    private String partName;
    private String image;
    private Long price;
    private Long amount;
    private Long limitAmount;
    private Boolean underLimit;
    private LocalDateTime updatedAt;

    public static InventoryItemDto of(StoreInventory si) {
        Parts p = si.getPart();
        boolean under = si.getLimitAmount() != null && si.getAmount() < si.getLimitAmount();
        return InventoryItemDto.builder()
                .partId(p.getId())
                .partName(p.getName())
                .image(p.getImage())
                .price(p.getPrice())
                .amount(si.getAmount())
                .limitAmount(si.getLimitAmount())
                .underLimit(under)
                .updatedAt(si.getUpdatedAt())
                .build();
    }
}
