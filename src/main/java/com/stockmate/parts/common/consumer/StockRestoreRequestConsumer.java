package com.stockmate.parts.common.consumer;

import com.stockmate.parts.api.parts.dto.StockRestoreRequestEvent;
import com.stockmate.parts.api.parts.service.PartsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
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
    public void handleStockRestoreRequest(
            @Payload StockRestoreRequestEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        log.info("재고 복구 요청 이벤트 수신 - 토픽: {}, 파티션: {}, 오프셋: {}, Order ID: {}, Order Number: {}, Reason: {}, Items: {}",
                topic, partition, offset, event.getOrderId(), event.getOrderNumber(), event.getReason(), event.getItems().size());

        // 재고 복구 처리 (보상 트랜잭션)
        partsService.restoreStock(event);
        acknowledgment.acknowledge();
        
        log.info("재고 복구 요청 처리 완료 - Order ID: {}", event.getOrderId());
    }
}
