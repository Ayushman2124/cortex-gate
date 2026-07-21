package com.cortexgate.orchestrator.service;

import com.cortexgate.orchestrator.dto.ChatRequest;
import com.cortexgate.orchestrator.dto.TokenMessage;
import com.cortexgate.orchestrator.entity.InteractionRecord;
import com.cortexgate.orchestrator.kafka.ResponseProducerService;
import com.cortexgate.orchestrator.repository.InteractionRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrchestratorServiceTest {

    @Mock private RedisSemanticCache semanticCache;
    @Mock private RedisVectorService vectorService;
    @Mock private ChatClient.Builder chatClientBuilder;
    @Mock private ChatClient chatClient;
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
        ChatRequest request = new ChatRequest();
        request.setRequestId("req-123");
        request.setQuery("What is CortexGate?");

        when(semanticCache.searchSimilar("What is CortexGate?")).thenReturn(Optional.of("It is an orchestrator."));

        orchestratorService.processRequest(request);

        // Verify it didn't call Vector DB or LLM
        verify(vectorService, never()).retrieveContext(any());
        verify(chatClient, never()).prompt(any(Prompt.class));

        // Verify response producer
        ArgumentCaptor<TokenMessage> tokenCaptor = ArgumentCaptor.forClass(TokenMessage.class);
        verify(responseProducer).streamToken(tokenCaptor.capture());

        TokenMessage emitted = tokenCaptor.getValue();
        assertEquals("req-123", emitted.getRequestId());
        assertEquals("It is an orchestrator.", emitted.getToken());
        assertTrue(emitted.isFinal());
        assertTrue(emitted.isCacheHit());

        // Verify DB Save
        verify(interactionRepository).save(any(InteractionRecord.class));
    }

    @Test
    void testProcessRequest_CacheMiss() {
        ChatRequest request = new ChatRequest();
        request.setRequestId("req-456");
        request.setQuery("How does it scale?");

        when(semanticCache.searchSimilar("How does it scale?")).thenReturn(Optional.empty());
        when(vectorService.retrieveContext("How does it scale?")).thenReturn(List.of("Context document 1"));
        
        ChatClient.ChatClientPromptRequest requestSpec = mock(ChatClient.ChatClientPromptRequest.class);
        when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("It scales horizontally.");

        orchestratorService.processRequest(request);

        // Verify Vector DB and LLM were called
        verify(vectorService).retrieveContext("How does it scale?");
        verify(chatClient).prompt(any(Prompt.class));

        // Verify Cache Writeback
        verify(semanticCache).cacheResponse("How does it scale?", "It scales horizontally.");

        // Verify response producer
        ArgumentCaptor<TokenMessage> tokenCaptor = ArgumentCaptor.forClass(TokenMessage.class);
        verify(responseProducer).streamToken(tokenCaptor.capture());

        TokenMessage emitted = tokenCaptor.getValue();
        assertEquals("req-456", emitted.getRequestId());
        assertEquals("It scales horizontally.", emitted.getToken());
        assertTrue(emitted.isFinal());
        assertFalse(emitted.isCacheHit());

        // Verify DB Save
        verify(interactionRepository).save(any(InteractionRecord.class));
    }
}
