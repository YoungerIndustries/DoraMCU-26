package com.yisy.modules.doramcu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Provides message batching and rate limiting for WebSocket handlers.
 * <p>
 * Collects individual messages over a short window, then sends them as a
 * single batched message to reduce network overhead and frontend processing
 * load. Also enforces per-session rate limits to prevent message flooding.
 * </p>
 */
@Service
public class WebSocketMessageBatcher {

    private static final Logger log = LoggerFactory.getLogger(WebSocketMessageBatcher.class);

    /** Default batch window in milliseconds */
    private static final long DEFAULT_BATCH_WINDOW_MS = 200;

    /** Default max messages per batch */
    private static final int DEFAULT_MAX_BATCH_SIZE = 50;

    /** Default max messages per second per session */
    private static final int DEFAULT_MAX_MESSAGES_PER_SECOND = 100;

    private final ObjectMapper objectMapper;
    private final long batchWindowMs;
    private final int maxBatchSize;
    private final int maxMessagesPerSecond;

    /** Per-session message counters for rate limiting */
    private final ConcurrentHashMap<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();

    public WebSocketMessageBatcher() {
        this(DEFAULT_BATCH_WINDOW_MS, DEFAULT_MAX_BATCH_SIZE, DEFAULT_MAX_MESSAGES_PER_SECOND);
    }

    public WebSocketMessageBatcher(long batchWindowMs, int maxBatchSize, int maxMessagesPerSecond) {
        this.objectMapper = new ObjectMapper();
        this.batchWindowMs = batchWindowMs;
        this.maxBatchSize = maxBatchSize;
        this.maxMessagesPerSecond = maxMessagesPerSecond;
    }

    /**
     * Check if a message should be allowed for the given session (rate limiting).
     *
     * @param sessionId the WebSocket session ID
     * @return true if the message is allowed, false if rate limited
     */
    public boolean isMessageAllowed(String sessionId) {
        RateLimiter limiter = rateLimiters.computeIfAbsent(sessionId, k -> new RateLimiter());
        return limiter.allow();
    }

    /**
     * Create a batched message from multiple individual messages.
     *
     * @param messages the individual JSON messages to batch
     * @return a single TextMessage containing all messages in a batch array
     */
    public TextMessage createBatch(List<ObjectNode> messages) {
        if (messages.isEmpty()) {
            return null;
        }

        ObjectNode batch = objectMapper.createObjectNode();
        batch.put("type", "_batch");
        batch.put("count", messages.size());
        batch.put("timestamp", System.currentTimeMillis());

        ArrayNode items = batch.putArray("messages");
        int limit = Math.min(messages.size(), maxBatchSize);
        for (int i = 0; i < limit; i++) {
            items.add(messages.get(i));
        }

        try {
            return new TextMessage(objectMapper.writeValueAsString(batch));
        } catch (Exception e) {
            log.error("Failed to create batch message", e);
            return null;
        }
    }

    /**
     * Send a batched message to a session, with graceful error handling.
     */
    public void sendBatch(WebSocketSession session, TextMessage batch) {
        if (batch == null || session == null || !session.isOpen()) {
            return;
        }
        try {
            session.sendMessage(batch);
        } catch (IOException e) {
            log.warn("Failed to send batch to session {}: {}", session.getId(), e.getMessage());
        }
    }

    /**
     * Clean up rate limiter for a disconnected session.
     */
    public void cleanupSession(String sessionId) {
        rateLimiters.remove(sessionId);
    }

    /**
     * Get current rate limiter stats.
     */
    public ObjectNode getStats() {
        ObjectNode stats = objectMapper.createObjectNode();
        stats.put("activeSessions", rateLimiters.size());
        stats.put("batchWindowMs", batchWindowMs);
        stats.put("maxBatchSize", maxBatchSize);
        stats.put("maxMessagesPerSecond", maxMessagesPerSecond);
        return stats;
    }

    /**
     * Simple token bucket rate limiter.
     */
    static class RateLimiter {
        private final AtomicLong tokens = new AtomicLong(DEFAULT_MAX_MESSAGES_PER_SECOND);
        private final AtomicLong lastRefill = new AtomicLong(System.currentTimeMillis());
        private final int maxTokens;

        RateLimiter() {
            this.maxTokens = DEFAULT_MAX_MESSAGES_PER_SECOND;
        }

        RateLimiter(int maxTokens) {
            this.maxTokens = maxTokens;
            this.tokens.set(maxTokens);
        }

        synchronized boolean allow() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefill.get();

            // Refill tokens based on elapsed time
            if (elapsed >= 1000) {
                long tokensToAdd = (elapsed / 1000) * maxTokens;
                tokens.set(Math.min(maxTokens, tokens.get() + tokensToAdd));
                lastRefill.set(now);
            }

            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }
    }
}
