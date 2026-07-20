package com.cortexgate.orchestrator.service;

import com.cortexgate.orchestrator.dto.ChatRequest;
import com.cortexgate.orchestrator.dto.TokenMessage;
import com.cortexgate.orchestrator.entity.InteractionRecord;
import com.cortexgate.orchestrator.kafka.ResponseProducerService;
import com.cortexgate.orchestrator.repository.InteractionRecordRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrchestratorService {

    private final RedisSemanticCache semanticCache;
    private final RedisVectorService vectorService;
    private final ChatClient chatClient;
    private final ResponseProducerService responseProducer;
    private final InteractionRecordRepository interactionRepository;

    public OrchestratorService(RedisSemanticCache semanticCache,
                               RedisVectorService vectorService,
                               ChatClient.Builder chatClientBuilder,
                               ResponseProducerService responseProducer,
                               InteractionRecordRepository interactionRepository) {
        this.semanticCache = semanticCache;
        this.vectorService = vectorService;
        this.chatClient = chatClientBuilder.build();
        this.responseProducer = responseProducer;
        this.interactionRepository = interactionRepository;
    }

    public void processRequest(ChatRequest request) {
        long startTime = System.currentTimeMillis();
        InteractionRecord record = new InteractionRecord();
        record.setRequestId(request.getRequestId());
        record.setQuery(request.getQuery());

        // 1. Semantic Cache Check
        long cacheStart = System.currentTimeMillis();
        Optional<String> cachedResponse = semanticCache.searchSimilar(request.getQuery());
        long cacheEnd = System.currentTimeMillis();
        record.setCacheHitTimeMs(cacheEnd - cacheStart);

        if (cachedResponse.isPresent()) {
            record.setCacheHit(true);
            record.setResponse(cachedResponse.get());
            record.setTotalTimeMs(System.currentTimeMillis() - startTime);
            interactionRepository.save(record);

            sendFinalResponse(request.getRequestId(), cachedResponse.get(), record);
            return;
        }

        record.setCacheHit(false);

        // 2. Vector Retrieval (Cache Miss)
        long retrievalStart = System.currentTimeMillis();
        List<String> context = vectorService.retrieveContext(request.getQuery());
        long retrievalEnd = System.currentTimeMillis();
        record.setRetrievalTimeMs(retrievalEnd - retrievalStart);

        // 3. LLM Call
        String promptText = "Context: " + String.join("\n", context) + "\n\nQuery: " + request.getQuery();
        
        long llmStart = System.currentTimeMillis();
        String llmResponse = chatClient.prompt(new Prompt(promptText)).call().content();
        long llmEnd = System.currentTimeMillis();
        record.setLlmLatencyMs(llmEnd - llmStart);
        record.setResponse(llmResponse);

        // 4. Cache Write-back
        semanticCache.cacheResponse(request.getQuery(), llmResponse);

        record.setTotalTimeMs(System.currentTimeMillis() - startTime);
        interactionRepository.save(record);

        sendFinalResponse(request.getRequestId(), llmResponse, record);
    }

    private void sendFinalResponse(String requestId, String responseText, InteractionRecord record) {
        TokenMessage tokenMessage = new TokenMessage();
        tokenMessage.setRequestId(requestId);
        tokenMessage.setToken(responseText);
        tokenMessage.setFinal(true);
        tokenMessage.setCacheHit(record.isCacheHit());
        tokenMessage.setCacheHitTimeMs(record.getCacheHitTimeMs());
        tokenMessage.setRetrievalTimeMs(record.getRetrievalTimeMs());
        tokenMessage.setLlmLatencyMs(record.getLlmLatencyMs());
        tokenMessage.setTotalTimeMs(record.getTotalTimeMs());

        responseProducer.streamToken(tokenMessage);
    }
}
