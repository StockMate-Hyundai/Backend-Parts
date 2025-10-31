package com.stockmate.parts.api.parts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceivingProcessRequestEvent {
    private Long orderId;
    private String orderNumber;
    private String approvalAttemptId;
    private Long memberId; // 가맹점 ID
    private List<ReceivingItemDTO> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReceivingItemDTO {
        private Long partId;
        private int quantity;
    }
}
