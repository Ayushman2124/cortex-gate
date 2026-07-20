package com.cortexgate.api.service;

import com.cortexgate.api.dto.ChatRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendChatRequest(ChatRequest request) {
        kafkaTemplate.send("chat-requests", request.getRequestId(), request);
    }
}
