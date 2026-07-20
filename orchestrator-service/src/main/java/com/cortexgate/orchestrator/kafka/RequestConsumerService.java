package com.cortexgate.orchestrator.kafka;

import com.cortexgate.orchestrator.dto.ChatRequest;
import com.cortexgate.orchestrator.service.OrchestratorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class RequestConsumerService {

    private static final Logger log = LoggerFactory.getLogger(RequestConsumerService.class);
    private final OrchestratorService orchestratorService;
    private final ObjectMapper objectMapper;

    public RequestConsumerService(OrchestratorService orchestratorService, ObjectMapper objectMapper) {
        this.orchestratorService = orchestratorService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "chat-requests", groupId = "orchestrator-group")
    public void consumeChatRequest(String messageStr) {
        try {
            ChatRequest request = objectMapper.readValue(messageStr, ChatRequest.class);
            orchestratorService.processRequest(request);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse ChatRequest", e);
        }
    }
}
