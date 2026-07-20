package com.cortexgate.orchestrator.service;

import com.cortexgate.orchestrator.dto.ChatRequest;
import com.cortexgate.orchestrator.entity.InteractionRecord;
import com.cortexgate.orchestrator.kafka.ResponseProducerService;
import com.cortexgate.orchestrator.repository.InteractionRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.Generation;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrchestratorServiceTest {

    @Mock private RedisSemanticCache semanticCache;
    @Mock private MongoVectorService vectorService;
    @Mock private ChatClient.Builder chatClientBuilder;
    @Mock private ChatClient chatClient;
    @Mock private ChatClient.ChatClientPromptRequest promptRequest;
    @Mock private ChatClient.ChatClientRequest.CallPromptResponseSpec callResponseSpec;
    @Mock private ResponseProducerService responseProducer;
    @Mock private InteractionRecordRepository interactionRepository;

    private OrchestratorService orchestratorService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        orchestratorService = new OrchestratorService(
                semanticCache, vectorService, chatClientBuilder,
                responseProducer, interactionRepository
        );
    }

    @Test
    void testProcessRequest_CacheHit() {
        ChatRequest request = new ChatRequest("req-123", "What is Cortex?");
        when(semanticCache.searchSimilar("What is Cortex?")).thenReturn(Optional.of("Cached Response"));

        orchestratorService.processRequest(request);

        verify(interactionRepository, times(1)).save(any(InteractionRecord.class));
        verify(responseProducer, times(1)).streamToken(any());
        verifyNoInteractions(vectorService);
        verifyNoInteractions(chatClient);
    }

    @Test
    void testProcessRequest_CacheMiss() {
        ChatRequest request = new ChatRequest("req-123", "What is Cortex?");
        when(semanticCache.searchSimilar("What is Cortex?")).thenReturn(Optional.empty());
        when(vectorService.retrieveContext("What is Cortex?")).thenReturn(List.of("Context 1"));
        
        when(chatClient.prompt(any(Prompt.class))).thenReturn(promptRequest);
        when(promptRequest.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("LLM Response");

        orchestratorService.processRequest(request);

        verify(semanticCache, times(1)).cacheResponse("What is Cortex?", "LLM Response");
        verify(interactionRepository, times(1)).save(any(InteractionRecord.class));
        verify(responseProducer, times(1)).streamToken(any());
    }
}
