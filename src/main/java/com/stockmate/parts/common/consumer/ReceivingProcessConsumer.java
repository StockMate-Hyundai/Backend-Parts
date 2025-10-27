package com.stockmate.parts.common.consumer;

import com.stockmate.parts.api.parts.dto.ReceivingProcessRequestEvent;
import com.stockmate.parts.api.parts.service.StoreService;
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
public class ReceivingProcessConsumer {

    private final StoreService storeService;

    @KafkaListener(
            topics = "${kafka.topics.receiving-process-request}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleReceivingProcessRequest(
            @Payload ReceivingProcessRequestEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        log.info("입고 처리 요청 이벤트 수신 - 토픽: {}, 파티션: {}, 오프셋: {}, Order ID: {}, Order Number: {}, Items: {}",
                topic, partition, offset, event.getOrderId(), event.getOrderNumber(), event.getItems().size());

        // 입고 처리
        storeService.processReceivingRequest(event);
        acknowledgment.acknowledge();
        
        log.info("입고 처리 요청 처리 완료 - Order ID: {}", event.getOrderId());
    }
}
