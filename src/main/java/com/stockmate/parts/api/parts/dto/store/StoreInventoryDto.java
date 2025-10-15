package com.stockmate.parts.api.parts.dto.store;

import com.stockmate.parts.api.parts.entity.Parts;
import com.stockmate.parts.api.parts.entity.StoreInventory;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreInventoryDto {
    private Long partId;
    private String partName;
    private String image;
    private Long price;
    private Integer amount;
    private Integer limitAmount;
    private Boolean underLimit;
    private LocalDateTime updatedAt;

    public static StoreInventoryDto of(StoreInventory si) {
        Parts p = si.getPart();
        boolean under = si.getLimitAmount() != null && si.getAmount() < si.getLimitAmount();
        return StoreInventoryDto.builder()
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
