package com.cortexgate.orchestrator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DocumentLoaderService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DocumentLoaderService.class);
    private final VectorStore vectorStore;

    public DocumentLoaderService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking if sample documents exist in Redis Vector Store...");
        
        // We do a simple search to see if we've already loaded data
        List<Document> existing = vectorStore.similaritySearch("CortexGate");
        
        if (existing.isEmpty()) {
            log.info("No documents found. Loading sample company data into Redis Vector Store...");
            
            List<Document> sampleDocs = List.of(
                new Document("CortexGate is a next-generation API Gateway designed to handle high-throughput AI requests, load balancing, and intelligent semantic caching to reduce LLM costs.", Map.of("source", "architecture-doc")),
                new Document("The Orchestrator Service in CortexGate is responsible for taking requests from Kafka, checking the Redis Semantic Cache, retrieving relevant context from Redis Vector Search, and calling the Gemini AI models.", Map.of("source", "orchestrator-readme")),
                new Document("If you ever encounter a 404 error with gemini-1.5-pro, it means the model is deprecated on the main endpoint. You should upgrade to gemini-2.5-flash or gemini-3.5-flash.", Map.of("source", "troubleshooting-guide")),
                new Document("CortexGate was developed by the Advanced AI team in 2026 to solve the massive latency issues caused by naive LLM integrations.", Map.of("source", "history-wiki"))
            );
            
            vectorStore.add(sampleDocs);
            log.info("Successfully loaded {} sample documents into Vector Store and generated embeddings!", sampleDocs.size());
        } else {
            log.info("Sample documents already exist. Skipping data load.");
        }
    }
}
