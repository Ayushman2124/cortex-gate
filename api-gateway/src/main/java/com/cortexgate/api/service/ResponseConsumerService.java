package com.cortexgate.api.service;

import com.cortexgate.api.dto.TokenMessage;
import com.cortexgate.api.websocket.ChatWebSocketHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ResponseConsumerService {

    private static final Logger log = LoggerFactory.getLogger(ResponseConsumerService.class);
    private final ChatWebSocketHandler chatWebSocketHandler;
    private final ObjectMapper objectMapper;

    public ResponseConsumerService(ChatWebSocketHandler chatWebSocketHandler, ObjectMapper objectMapper) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "ai-responses", groupId = "api-gateway-group")
    public void consumeAiResponse(String messageStr) {
        try {
            TokenMessage message = objectMapper.readValue(messageStr, TokenMessage.class);
            chatWebSocketHandler.sendMessageToClient(message);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse TokenMessage", e);
        }
    }
}
