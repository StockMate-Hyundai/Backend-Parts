package com.stockmate.parts.common.consumer;

import com.stockmate.parts.api.parts.dto.StockRestoreRequestEvent;
import com.stockmate.parts.api.parts.service.PartsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockRestoreRequestConsumer {

    private final PartsService partsService;

    @KafkaListener(
            topics = "${kafka.topics.stock-restore-request}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleStockRestoreRequest(StockRestoreRequestEvent event) {
        log.info("=== 재고 복구 요청 이벤트 수신 === Order ID: {}, Order Number: {}, Reason: {}, Items: {}",
                event.getOrderId(), event.getOrderNumber(), event.getReason(), event.getItems().size());

        try {
            // 재고 복구 처리 (보상 트랜잭션)
            partsService.restoreStock(event);

            log.info("=== 재고 복구 요청 처리 완료 === Order ID: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("=== 재고 복구 요청 처리 실패 === Order ID: {}, 에러: {}", event.getOrderId(), e.getMessage(), e);
        }
    }
}