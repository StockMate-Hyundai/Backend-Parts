package com.stockmate.parts.common.consumer;

import com.stockmate.parts.api.parts.dto.StockDeductionRequestEvent;
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
public class StockDeductionRequestConsumer {

    private final PartsService partsService;

    @KafkaListener(
            topics = "${kafka.topics.stock-deduction-request}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleStockDeductionRequest(
            @Payload StockDeductionRequestEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("재고 차감 요청 이벤트 수신 - 토픽: {}, 파티션: {}, 오프셋: {}, Order ID: {}, Order Number: {}, Items: {}",
                    topic, partition, offset, event.getOrderId(), event.getOrderNumber(), event.getItems().size());

            // 재고 차감 처리
            partsService.deductStock(event);

            acknowledgment.acknowledge();
            log.info("재고 차감 요청 처리 완료 - Order ID: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("재고 차감 요청 처리 실패 - Order ID: {}, 에러: {}", event.getOrderId(), e.getMessage(), e);
            // 실패 시에도 acknowledge (DLQ나 별도 에러 처리 필요 시 제거)
            acknowledgment.acknowledge();
        }
    }
}