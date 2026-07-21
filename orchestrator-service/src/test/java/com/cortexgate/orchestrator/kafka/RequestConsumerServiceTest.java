package com.cortexgate.orchestrator.kafka;

import com.cortexgate.orchestrator.dto.ChatRequest;
import com.cortexgate.orchestrator.service.OrchestratorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestConsumerServiceTest {

    @Mock
    private OrchestratorService orchestratorService;

    private RequestConsumerService requestConsumerService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        requestConsumerService = new RequestConsumerService(orchestratorService, objectMapper);
    }

    @Test
    void testConsumeRequest_Success() throws Exception {
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setRequestId("req-789");
        chatRequest.setQuery("Test query");
        
        String jsonPayload = objectMapper.writeValueAsString(chatRequest);

        requestConsumerService.consumeChatRequest(jsonPayload);

        verify(orchestratorService).processRequest(argThat(req -> 
            "req-789".equals(req.getRequestId()) && "Test query".equals(req.getQuery())
        ));
    }

    @Test
    void testConsumeRequest_InvalidJson_DoesNotThrow() {
        String invalidJson = "{ invalid_json }";

        requestConsumerService.consumeChatRequest(invalidJson);

        // Verify that parsing failure gets caught and logged, but doesn't crash consumer
        verify(orchestratorService, never()).processRequest(any());
    }
}
