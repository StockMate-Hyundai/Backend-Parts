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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockRestoreRequestConsumer {

    private final PartsService partsService;
    
    // 재시도 횟수를 추적하기 위한 맵 (Order ID -> 재시도 횟수)
    private final ConcurrentHashMap<Long, AtomicInteger> retryCountMap = new ConcurrentHashMap<>();
    private static final int MAX_RETRY_COUNT = 3; // 최대 재시도 횟수

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

        try {
            // 재고 복구 처리 (보상 트랜잭션)
            partsService.restoreStock(event);
            acknowledgment.acknowledge();
            
            // 처리 성공 시 재시도 카운트 제거
            retryCountMap.remove(event.getOrderId());
            
            log.info("재고 복구 요청 처리 완료 - Order ID: {}", event.getOrderId());

        } catch (Exception e) {
            // 재시도 횟수 증가
            AtomicInteger retryCount = retryCountMap.computeIfAbsent(event.getOrderId(), k -> new AtomicInteger(0));
            int currentRetryCount = retryCount.incrementAndGet();
            
            log.error("재고 복구 요청 처리 실패 - Order ID: {}, 재시도 횟수: {}/{}, 에러: {}",
                    event.getOrderId(), currentRetryCount, MAX_RETRY_COUNT, e.getMessage(), e);
            
            // 최대 재시도 횟수 초과 시 acknowledge 처리 (재고 복구 실패는 심각한 문제)
            if (currentRetryCount >= MAX_RETRY_COUNT) {
                log.error("최대 재시도 횟수 초과 - Order ID: {}를 acknowledge 처리하여 더 이상 재시도하지 않음. ⚠️ 재고 복구 실패는 수동 처리 필요!", 
                        event.getOrderId());
                acknowledgment.acknowledge();
                retryCountMap.remove(event.getOrderId());
                // TODO: 재고 복구 실패 시 알림 또는 별도 처리 필요
            }
            // 최대 재시도 횟수 미달 시 acknowledge 하지 않아 재시도
        }
    }
}
