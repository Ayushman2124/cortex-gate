package com.cortexgate.api.controller;

import com.cortexgate.api.dto.ChatRequest;
import com.cortexgate.api.service.KafkaProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*") // Allows Vite frontend to hit this endpoint
public class ChatController {

    private final KafkaProducerService kafkaProducerService;

    public ChatController(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @PostMapping
    public ResponseEntity<ChatRequest> submitChat(@RequestBody ChatRequest request) {
        if (request.getRequestId() == null || request.getRequestId().isEmpty()) {
            request.setRequestId(UUID.randomUUID().toString());
        }
        
        kafkaProducerService.sendChatRequest(request);
        
        return ResponseEntity.ok(request);
    }
}
