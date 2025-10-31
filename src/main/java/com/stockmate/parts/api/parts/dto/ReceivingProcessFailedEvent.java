package com.stockmate.parts.api.parts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceivingProcessFailedEvent {
    private Long orderId;
    private String orderNumber;
    private String approvalAttemptId;
    private String errorMessage;
    private Object data;
}
