package com.stockmate.parts.common.producer;

import com.stockmate.parts.api.parts.dto.StockDeductionFailedEvent;
import com.stockmate.parts.api.parts.dto.StockDeductionSuccessEvent;
import com.stockmate.parts.api.parts.dto.ReceivingProcessSuccessEvent;
import com.stockmate.parts.api.parts.dto.ReceivingProcessFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.stock-deduction-success}")
    private String stockDeductionSuccessTopic;

    @Value("${kafka.topics.stock-deduction-failed}")
    private String stockDeductionFailedTopic;

    @Value("${kafka.topics.receiving-process-success}")
    private String receivingProcessSuccessTopic;

    @Value("${kafka.topics.receiving-process-failed}")
    private String receivingProcessFailedTopic;

    public void sendStockDeductionSuccess(StockDeductionSuccessEvent event) {
        log.info("재고 차감 성공 이벤트 발송 시작 - Order ID: {}", event.getOrderId());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                stockDeductionSuccessTopic,
                event.getOrderId().toString(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("재고 차감 성공 이벤트 발송 성공 - 토픽: {}, Order ID: {}",
                        result.getRecordMetadata().topic(), event.getOrderId());
            } else {
                log.error("재고 차감 성공 이벤트 발송 실패 - Order ID: {}, 에러: {}",
                        event.getOrderId(), ex.getMessage(), ex);
            }
        });
    }

    public void sendStockDeductionFailed(StockDeductionFailedEvent event) {
        log.info("재고 차감 실패 이벤트 발송 시작 - Order ID: {}, Reason: {}", 
                event.getOrderId(), event.getReason());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                stockDeductionFailedTopic,
                event.getOrderId().toString(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("재고 차감 실패 이벤트 발송 성공 - 토픽: {}, Order ID: {}",
                        result.getRecordMetadata().topic(), event.getOrderId());
            } else {
                log.error("재고 차감 실패 이벤트 발송 실패 - Order ID: {}, 에러: {}",
                        event.getOrderId(), ex.getMessage(), ex);
            }
        });
    }

    public void sendReceivingProcessSuccess(ReceivingProcessSuccessEvent event) {
        log.info("입고 처리 성공 이벤트 발송 시작 - Order ID: {}", event.getOrderId());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                receivingProcessSuccessTopic,
                event.getOrderId().toString(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("입고 처리 성공 이벤트 발송 성공 - 토픽: {}, Order ID: {}",
                        result.getRecordMetadata().topic(), event.getOrderId());
            } else {
                log.error("입고 처리 성공 이벤트 발송 실패 - Order ID: {}, 에러: {}",
                        event.getOrderId(), ex.getMessage(), ex);
            }
        });
    }

    public void sendReceivingProcessFailed(ReceivingProcessFailedEvent event) {
        log.info("입고 처리 실패 이벤트 발송 시작 - Order ID: {}, Reason: {}", 
                event.getOrderId(), event.getErrorMessage());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                receivingProcessFailedTopic,
                event.getOrderId().toString(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("입고 처리 실패 이벤트 발송 성공 - 토픽: {}, Order ID: {}",
                        result.getRecordMetadata().topic(), event.getOrderId());
            } else {
                log.error("입고 처리 실패 이벤트 발송 실패 - Order ID: {}, 에러: {}",
                        event.getOrderId(), ex.getMessage(), ex);
            }
        });
    }
}

