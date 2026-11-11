package com.yisy.modules.doraneuron.security;

import com.yisy.modules.doraneuron.config.NeuronSecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implements token bucket rate limiting for API endpoints.
 *
 * This service tracks requests per IP address and enforces a maximum number
 * of requests per time window. It uses a sliding window algorithm to provide
 * smooth rate limiting without sudden drops.
 */
@Component
public class RateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);

    private final NeuronSecurityConfig securityConfig;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimiterService(NeuronSecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    /**
     * Check if a request from the given IP should be allowed.
     */
    public boolean isAllowed(String ipAddress) {
        if (!securityConfig.isRateLimitingEnabled()) {
            return true;
        }

        long now = System.currentTimeMillis();
        int maxRequests = securityConfig.getMaxRequestsPerWindow();
        long windowMs = securityConfig.getRateLimitWindowSeconds() * 1000L;

        Bucket bucket = buckets.computeIfAbsent(ipAddress, k -> new Bucket(now));

        synchronized (bucket) {
            // Remove expired timestamps
            bucket.removeExpired(now - windowMs);

            // Check if under limit
            if (bucket.getRequestCount() < maxRequests) {
                bucket.addRequest(now);
                return true;
            }

            log.warn("Rate limit exceeded for IP: {} ({} requests in {}s window)",
                    ipAddress, bucket.getRequestCount(), securityConfig.getRateLimitWindowSeconds());
            return false;
        }
    }

    /**
     * Get the current request count for an IP.
     */
    public int getRequestCount(String ipAddress) {
        Bucket bucket = buckets.get(ipAddress);
        return bucket != null ? bucket.getRequestCount() : 0;
    }

    /**
     * Get the number of unique IPs being tracked.
     */
    public int getTrackedIps() {
        return buckets.size();
    }

    /**
     * Clean up expired buckets periodically.
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        long windowMs = securityConfig.getRateLimitWindowSeconds() * 1000L;

        buckets.entrySet().removeIf(entry -> {
            synchronized (entry.getValue()) {
                entry.getValue().removeExpired(now - windowMs);
                return entry.getValue().getRequestCount() == 0;
            }
        });
    }

    /**
     * Token bucket for tracking requests from a single IP.
     */
    private static class Bucket {
        private final AtomicInteger requestCount = new AtomicInteger(0);
        private final long[] timestamps = new long[1000]; // Max timestamps to track
        private int timestampCount = 0;

        public Bucket(long initialTime) {
            // Initialize with current time
        }

        public void addRequest(long timestamp) {
            if (timestampCount < timestamps.length) {
                timestamps[timestampCount++] = timestamp;
            } else {
                // Shift array to make room
                System.arraycopy(timestamps, 1, timestamps, 0, timestamps.length - 1);
                timestamps[timestamps.length - 1] = timestamp;
            }
            requestCount.incrementAndGet();
        }

        public void removeExpired(long cutoff) {
            int validCount = 0;
            for (int i = 0; i < timestampCount; i++) {
                if (timestamps[i] >= cutoff) {
                    timestamps[validCount++] = timestamps[i];
                }
            }
            timestampCount = validCount;
            requestCount.set(timestampCount);
        }

        public int getRequestCount() {
            return requestCount.get();
        }
    }
}
