package com.cortexgate.orchestrator.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interaction_records")
public class InteractionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String requestId;
    
    @Column(columnDefinition = "TEXT")
    private String query;
    
    @Column(columnDefinition = "TEXT")
    private String response;
    
    private boolean cacheHit;
    private long cacheHitTimeMs;
    private long retrievalTimeMs;
    private long llmLatencyMs;
    private long totalTimeMs;
    private LocalDateTime timestamp;

    public InteractionRecord() {
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters omitted for brevity, adding standard ones
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    public boolean isCacheHit() { return cacheHit; }
    public void setCacheHit(boolean cacheHit) { this.cacheHit = cacheHit; }
    public long getCacheHitTimeMs() { return cacheHitTimeMs; }
    public void setCacheHitTimeMs(long cacheHitTimeMs) { this.cacheHitTimeMs = cacheHitTimeMs; }
    public long getRetrievalTimeMs() { return retrievalTimeMs; }
    public void setRetrievalTimeMs(long retrievalTimeMs) { this.retrievalTimeMs = retrievalTimeMs; }
    public long getLlmLatencyMs() { return llmLatencyMs; }
    public void setLlmLatencyMs(long llmLatencyMs) { this.llmLatencyMs = llmLatencyMs; }
    public long getTotalTimeMs() { return totalTimeMs; }
    public void setTotalTimeMs(long totalTimeMs) { this.totalTimeMs = totalTimeMs; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
