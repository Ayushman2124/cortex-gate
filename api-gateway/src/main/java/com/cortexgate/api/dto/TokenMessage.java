package com.cortexgate.api.dto;

public class TokenMessage {
    private String requestId;
    private String token;
    private boolean isFinal;
    private long cacheHitTimeMs;
    private long retrievalTimeMs;
    private long llmLatencyMs;
    private long totalTimeMs;
    private boolean cacheHit;

    public TokenMessage() {}

    // Getters and Setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public boolean isFinal() { return isFinal; }
    public void setFinal(boolean aFinal) { isFinal = aFinal; }
    public long getCacheHitTimeMs() { return cacheHitTimeMs; }
    public void setCacheHitTimeMs(long cacheHitTimeMs) { this.cacheHitTimeMs = cacheHitTimeMs; }
    public long getRetrievalTimeMs() { return retrievalTimeMs; }
    public void setRetrievalTimeMs(long retrievalTimeMs) { this.retrievalTimeMs = retrievalTimeMs; }
    public long getLlmLatencyMs() { return llmLatencyMs; }
    public void setLlmLatencyMs(long llmLatencyMs) { this.llmLatencyMs = llmLatencyMs; }
    public long getTotalTimeMs() { return totalTimeMs; }
    public void setTotalTimeMs(long totalTimeMs) { this.totalTimeMs = totalTimeMs; }
    public boolean isCacheHit() { return cacheHit; }
    public void setCacheHit(boolean cacheHit) { this.cacheHit = cacheHit; }
}
