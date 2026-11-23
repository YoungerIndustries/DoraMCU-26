package com.yisy.modules.doranews.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Controller that proxies news data from the main DoraMCU server
 * and provides system stats including active player count.
 */
@RestController
public class NewsProxyController {

    private static final Logger log = LoggerFactory.getLogger(NewsProxyController.class);

    @Value("${doramcu.server.url:${DORA_MCU_SERVER_URL:http://localhost:8080}}")
    private String doraMcuServerUrl;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Cache for player count and system stats — atomic reference prevents partial updates
    private final AtomicReference<Map<String, Object>> statsCache =
            new AtomicReference<>(Collections.emptyMap());
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // Shared executor pool for SSE client polling — prevents per-client thread leaks
    private final ScheduledExecutorService sseWorkerPool = Executors.newScheduledThreadPool(
            2, r -> { Thread t = new Thread(r, "dnn-sse-worker"); t.setDaemon(true); return t; });

    public NewsProxyController(HttpClient httpClient) {
        this.httpClient = httpClient;
        // Refresh stats every 10 seconds
        scheduler.scheduleAtFixedRate(this::refreshStats, 5, 10, TimeUnit.SECONDS);
        refreshStats();
    }

    /**
     * Gracefully shut down the scheduled executor services on application stop.
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down NewsProxyController schedulers");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        sseWorkerPool.shutdown();
        try {
            if (!sseWorkerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                sseWorkerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            sseWorkerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * HttpClient configuration — shared bean for proper lifecycle management.
     */
    @Configuration
    static class HttpClientConfig {
        @Bean
        public HttpClient httpClient() {
            return HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(5))
                    .build();
        }
    }

    /**
     * Proxy the news feed from the main server.
     * Parses the JSON response to ensure proper content-type handling.
     */
    @GetMapping("/api/news/feed")
    public Object getFeed(@RequestParam(defaultValue = "50") int limit) {
        try {
            String url = doraMcuServerUrl + "/api/v1/news/feed?limit=" + limit;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return parseJsonResponse(response.body());
            }
            log.warn("News feed from main server returned status {}", response.statusCode());
            return proxyError("Main server returned status " + response.statusCode(), "/api/news/feed");
        } catch (Exception e) {
            log.warn("Failed to fetch news feed from main server: {}", e.getMessage());
            return proxyError(e.getMessage(), "/api/news/feed");
        }
    }

    /**
     * Proxy the live news SSE stream from the main server.
     * Uses SSE subscription to the main server instead of polling for real-time updates.
     * Falls back to polling if SSE is unavailable.
     */
    @GetMapping(value = "/api/news/live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeLive() {
        String clientId = "news-" + UUID.randomUUID().toString();
        log.info("Dora News Network client connected: {}", clientId);

        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        // Send welcome event immediately using cached stats (non-blocking)
        try {
            Map<String, Object> cached = statsCache.get();
            int cachedPlayers = cached.containsKey("activePlayers")
                    ? (int) cached.get("activePlayers") : 0;
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of(
                            "title", "Dora News Network",
                            "tagline", "Live updates from the DoraMCU Universe",
                            "subscriberCount", 1,
                            "activePlayers", cachedPlayers
                    )));
        } catch (IOException e) {
            log.warn("Failed to send welcome event: {}", e.getMessage());
        }

        // Set up cleanup handlers to prevent thread leaks
        emitter.onCompletion(() -> {
            log.info("SSE client disconnected: {}", clientId);
        });
        emitter.onTimeout(() -> {
            log.warn("SSE client timeout: {}", clientId);
        });
        emitter.onError((e) -> {
            log.warn("SSE client error: {}: {}", clientId, e.getMessage());
        });

        // Attempt SSE forwarding first; fall back to polling if SSE fails
        // Uses shared executor pool to prevent per-client thread leaks
        sseWorkerPool.submit(() -> {
            try {
                forwardSseFromMainServer(emitter, clientId);
            } catch (Exception e) {
                log.info("SSE forwarding failed, falling back to polling: {}", e.getMessage());
                fallbackPolling(emitter, clientId);
            } finally {
                emitter.complete();
            }
        });

        return emitter;
    }

    /**
     * Forward SSE events from the main server's /api/v1/news/live endpoint.
     * Uses a read timeout to prevent blocking indefinitely on idle connections.
     */
    void forwardSseFromMainServer(SseEmitter emitter, String clientId) throws IOException, InterruptedException {
        String sseUrl = doraMcuServerUrl + "/api/v1/news/live";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(sseUrl))
                .header("Accept", MediaType.TEXT_EVENT_STREAM_VALUE)
                .GET()
                .timeout(java.time.Duration.ofSeconds(30))
                .build();

        // Use a buffered reader to read SSE events line by line
        HttpResponse<java.io.InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new IOException("Main server SSE returned status " + response.statusCode());
        }

        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(response.body()))) {
            String line;
            StringBuilder eventBuilder = new StringBuilder();
            String eventName = "message";
            long lastActivity = System.currentTimeMillis();
            final long idleTimeoutMs = 60_000; // 60-second idle timeout

            while ((line = reader.readLine()) != null) {
                // Check for idle timeout to prevent blocking on dead connections
                if (System.currentTimeMillis() - lastActivity > idleTimeoutMs) {
                    log.debug("SSE idle timeout for {}: no data for 60s", clientId);
                    break;
                }
                lastActivity = System.currentTimeMillis();
                if (line.isEmpty()) {
                    // Empty line signals end of SSE event
                    if (eventBuilder.length() > 0) {
                        String data = eventBuilder.toString();
                        emitter.send(SseEmitter.event()
                                .name(eventName)
                                .data(data));
                        eventBuilder.setLength(0);
                        eventName = "message";
                    }
                } else if (line.startsWith("event:")) {
                    eventName = line.substring(6).trim();
                } else if (line.startsWith("data:")) {
                    eventBuilder.append(line.substring(5).trim());
                }
                // Ignore other fields (id, retry, etc.)
            }
        }
    }

    /**
     * Fallback: poll the main server's REST feed every 10 seconds.
     */
    void fallbackPolling(SseEmitter emitter, String clientId) {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String feedUrl = doraMcuServerUrl + "/api/v1/news/feed?limit=5";
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(feedUrl))
                            .GET()
                            .timeout(java.time.Duration.ofSeconds(5))
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) {
                        String data = response.body();
                        if (!data.isEmpty() && !data.equals("[]")) {
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("feed-update")
                                        .data(data));
                            } catch (IOException e) {
                                break; // Client disconnected
                            }
                        }
                    }
                } catch (Exception e) {
                    log.debug("Poll failed for {}: {}", clientId, e.getMessage());
                }
                Thread.sleep(10000); // Poll every 10 seconds
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Get current system stats including active player count.
     */
    @GetMapping("/api/stats")
    public Map<String, Object> getStats() {
        return getCurrentStats();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getCurrentStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("timestamp", System.currentTimeMillis());
        stats.put("serverUrl", doraMcuServerUrl);

        // Try to get player count from the main server
        int activePlayers = 0;
        boolean serverReachable = false;
        try {
            // Use the news feed endpoint instead of /actuator/health, which may return
            // 503 when Redis/Ollama are unavailable (e.g. on Pi deployments).
            String checkUrl = doraMcuServerUrl + "/api/v1/news/feed?limit=1";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(checkUrl))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(5))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            serverReachable = (response.statusCode() == 200);
        } catch (Exception e) {
            log.debug("Main server reachability check failed: {}", e.getMessage());
        }
        stats.put("serverReachable", serverReachable);

        // Use cached stats — atomic snapshot prevents partial updates
        Map<String, Object> cached = statsCache.get();
        if (cached.containsKey("activePlayers")) {
            activePlayers = (int) cached.get("activePlayers");
        }
        stats.put("activePlayers", activePlayers);

        if (cached.containsKey("eventCount")) {
            stats.put("eventCount", cached.get("eventCount"));
        }

        // JVM stats
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;
        stats.put("jvmUsedMemoryMB", usedMemory);
        stats.put("jvmTotalMemoryMB", totalMemory);
        stats.put("jvmCpuCores", Runtime.getRuntime().availableProcessors());

        return stats;
    }

    private void refreshStats() {
        Map<String, Object> newStats = new HashMap<>();
        try {
            // Try to get presence data from the main server
            String presenceUrl = doraMcuServerUrl + "/api/v1/news/presence";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(presenceUrl))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(5))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                // Parse the presence response to get real subscriber count
                try {
                    JsonNode node = objectMapper.readTree(response.body());
                    if (node.has("activeSubscribers")) {
                        newStats.put("activePlayers", node.get("activeSubscribers").asInt());
                    }
                    if (node.has("eventHistorySize")) {
                        newStats.put("eventCount", node.get("eventHistorySize").asInt());
                    }
                } catch (JsonProcessingException e) {
                    log.debug("Failed to parse presence response: {}", e.getMessage());
                    newStats.put("activePlayers", 0);
                }
            } else {
                newStats.put("activePlayers", 0);
            }
        } catch (Exception e) {
            log.debug("Failed to refresh stats from main server: {}", e.getMessage());
            newStats.put("activePlayers", 0);
        }
        // Atomically replace the entire cache to prevent partial updates
        statsCache.set(newStats);
    }

    // ==========================================
    // Galaxy Health Proxy Endpoint
    // ==========================================

    /**
     * Proxy the galaxy health summary from the main server.
     * Returns cached health data for all galaxies in the DoraMCU universe.
     * Uses the lightweight cached endpoint which returns instantly from
     * in-memory event history instead of running expensive assessments.
     */
    @GetMapping("/api/galaxies/health")
    public Object getGalaxyHealth() {
        return proxyGet("/api/v1/news/galaxy-health-summary", 5);
    }

    /**
     * Proxy the galaxy dashboard for a single galaxy by name.
     * Returns comprehensive data: health, build, workforce, work orders, events, players.
     */
    @GetMapping("/api/galaxies/{name}/dashboard")
    public Object getGalaxyDashboard(@PathVariable String name) {
        try {
            // URLEncoder.encode uses '+' for spaces, but URL paths need '%20'
            String encodedName = java.net.URLEncoder.encode(name, java.nio.charset.StandardCharsets.UTF_8)
                    .replace("+", "%20");
            return proxyGet("/api/v1/admin/galaxies/" + encodedName + "/dashboard", 30);
        } catch (Exception e) {
            log.warn("Failed to encode galaxy name '{}': {}", name, e.getMessage());
            return Collections.emptyList();
        }
    }

    // ==========================================
    // Babylon 5 Universe Data Proxy Endpoints
    // ==========================================

    /**
     * Generic proxy for Babylon 5 universe data endpoints.
     * Supports: races, characters, storylines, dialogues, locations.
     */
    @GetMapping("/api/b5/{resource}")
    public Object getB5Resource(@PathVariable String resource) {
        return proxyGet("/api/v1/babylon5/" + resource);
    }

    /**
     * Generic proxy GET helper.
     */
    private Object proxyGet(String path) {
        return proxyGet(path, 10);
    }

    /**
     * Generic proxy GET helper with configurable timeout seconds.
     * Returns structured error response instead of silent empty list.
     */
    private Object proxyGet(String path, int timeoutSeconds) {
        try {
            String url = doraMcuServerUrl + path;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(timeoutSeconds))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return parseJsonResponse(response.body());
            }
            log.warn("Proxy request to {} returned status {}", path, response.statusCode());
            return proxyError("Main server returned status " + response.statusCode(), path);
        } catch (java.net.http.HttpTimeoutException e) {
            log.warn("Proxy request to {} timed out after {}s: {}", path, timeoutSeconds, e.getMessage());
            return proxyError("Request timed out after " + timeoutSeconds + "s", path);
        } catch (Exception e) {
            log.warn("Failed to proxy request {}: {}", path, e.getMessage());
            return proxyError(e.getMessage(), path);
        }
    }

    /**
     * Create a structured error response for proxy failures.
     */
    private Map<String, Object> proxyError(String message, String path) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        error.put("path", path);
        return error;
    }

    /**
     * Parse a JSON string response into a List or Map for correct serialization.
     * JsonNode direct return produces metadata properties in Spring Boot 4.x,
     * so we convert to standard Java collections instead.
     * Falls back to returning the raw string if parsing fails.
     */
    private Object parseJsonResponse(String body) {
        if (body == null || body.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.isArray()) {
                return objectMapper.convertValue(node, List.class);
            } else if (node.isObject()) {
                return objectMapper.convertValue(node, Map.class);
            }
            return body;
        } catch (JsonProcessingException e) {
            log.debug("Failed to parse JSON response, returning raw: {}", e.getMessage());
            return body;
        }
    }
}
