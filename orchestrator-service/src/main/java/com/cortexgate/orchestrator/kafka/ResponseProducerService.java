package com.cortexgate.orchestrator.kafka;

import com.cortexgate.orchestrator.dto.TokenMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ResponseProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ResponseProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void streamToken(TokenMessage message) {
        kafkaTemplate.send("ai-responses", message.getRequestId(), message);
    }
}
