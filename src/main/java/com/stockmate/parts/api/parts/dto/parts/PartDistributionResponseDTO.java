package com.stockmate.parts.api.parts.dto.parts;

import com.stockmate.parts.api.parts.dto.common.PageResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartDistributionResponseDTO {
    private Long partId;
    private String partName;
    private Integer headquartersQuantity;  // 본사 보유 수량
    private PageResponseDto<StoreDistributionItem> stores;  // 가맹점별 보유 수량 (페이지네이션)

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StoreDistributionItem {
        private Long userId;           // 가맹점 ID
        private Integer quantity;      // 가맹점 보유 수량
        private UserBatchResponseDTO storeInfo;  // 가맹점 정보 (User 서버에서 조회)
    }
}

