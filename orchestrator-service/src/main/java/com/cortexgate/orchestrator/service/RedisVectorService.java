package com.cortexgate.orchestrator.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RedisVectorService {

    private final VectorStore vectorStore;

    public RedisVectorService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<String> retrieveContext(String query) {
        List<Document> results = this.vectorStore.similaritySearch(SearchRequest.query(query).withTopK(3));
        return results.stream()
                .map(Document::getContent)
                .collect(Collectors.toList());
    }
}
