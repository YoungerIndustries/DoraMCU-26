package com.yisy.modules.doranews.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Controller that proxies news data from the main DoraMCU server
 * and provides system stats including active player count.
 */
@RestController
public class NewsProxyController {

    private static final Logger log = LoggerFactory.getLogger(NewsProxyController.class);

    @Value("${doramcu.server.url:http://localhost:8080}")
    private String doraMcuServerUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(5))
            .build();

    // Cache for player count and system stats
    private final Map<String, Object> statsCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public NewsProxyController() {
        // Refresh stats every 10 seconds
        scheduler.scheduleAtFixedRate(this::refreshStats, 5, 10, TimeUnit.SECONDS);
        refreshStats();
    }

    /**
     * Proxy the news feed from the main server.
     */
    @GetMapping("/api/news/feed")
    public Object getFeed(@RequestParam(defaultValue = "50") int limit) {
        try {
            String url = doraMcuServerUrl + "/api/v1/news/feed?limit=" + limit;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch news feed from main server: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Proxy the live news SSE stream from the main server.
     */
    @GetMapping(value = "/api/news/live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeLive() {
        String clientId = "news-" + UUID.randomUUID().toString();
        log.info("Dora News Network client connected: {}", clientId);

        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        // Send welcome event immediately using cached stats (non-blocking)
        try {
            int cachedPlayers = statsCache.containsKey("activePlayers")
                    ? (int) statsCache.get("activePlayers") : 0;
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

        // Periodically poll news feed and push updates via SSE
        final boolean[] running = {true};
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                while (running[0]) {
                    try {
                        String feedUrl = doraMcuServerUrl + "/api/v1/news/feed?limit=5";
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(feedUrl))
                                .GET()
                                .timeout(java.time.Duration.ofSeconds(5))
                                .build();
                        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                        if (response.statusCode() == 200) {
                            // Send latest events as SSE messages
                            String data = response.body();
                            if (!data.isEmpty() && !data.equals("[]")) {
                                try {
                                    emitter.send(SseEmitter.event()
                                            .name("feed-update")
                                            .data(data));
                                } catch (IOException e) {
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Poll failed: {}", e.getMessage());
                    }
                    Thread.sleep(10000); // Poll every 10 seconds
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                emitter.complete();
            }
        });

        return emitter;
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
            String healthUrl = doraMcuServerUrl + "/actuator/health";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(healthUrl))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(5))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            serverReachable = (response.statusCode() == 200);
        } catch (Exception e) {
            log.debug("Main server health check failed: {}", e.getMessage());
        }
        stats.put("serverReachable", serverReachable);

        // Use cached stats
        if (statsCache.containsKey("activePlayers")) {
            activePlayers = (int) statsCache.get("activePlayers");
        }
        stats.put("activePlayers", activePlayers);

        if (statsCache.containsKey("eventCount")) {
            stats.put("eventCount", statsCache.get("eventCount"));
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
        try {
            // Try to get news stats from the main server
            String statsUrl = doraMcuServerUrl + "/api/v1/news/stats";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(statsUrl))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(5))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                // Parse the response to get event count
                statsCache.put("eventCount", "available");
            }
        } catch (Exception e) {
            log.debug("Failed to refresh stats from main server: {}", e.getMessage());
        }

        // Simulate active player count based on server reachability
        // In a real scenario, this would come from a presence API
        try {
            String healthUrl = doraMcuServerUrl + "/actuator/health";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(healthUrl))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(5))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                // Server is up - estimate active players
                // This is a placeholder; in production, use a real presence API
                statsCache.put("activePlayers", estimateActivePlayers());
            } else {
                statsCache.put("activePlayers", 0);
            }
        } catch (Exception e) {
            statsCache.put("activePlayers", 0);
        }
    }

    private int estimateActivePlayers() {
        // Placeholder: returns a simulated count
        // In production, this would call a real presence API endpoint
        return (int) (Math.random() * 5) + 1;
    }

    // ==========================================
    // Galaxy Health Proxy Endpoint
    // ==========================================

    /**
     * Proxy the galaxy health summary from the main server.
     * Returns health data for all galaxies in the DoraMCU universe.
     * Uses a longer timeout since health assessment can be slow for many galaxies.
     */
    @GetMapping("/api/galaxies/health")
    public Object getGalaxyHealth() {
        return proxyGet("/api/v1/admin/galaxies/health-summary", 30);
    }

    /**
     * Proxy the galaxy dashboard for a single galaxy by name.
     * Returns comprehensive data: health, build, workforce, work orders, events, players.
     */
    @GetMapping("/api/galaxies/{name}/dashboard")
    public Object getGalaxyDashboard(@PathVariable String name) {
        return proxyGet("/api/v1/admin/galaxies/" + name + "/dashboard", 30);
    }

    // ==========================================
    // Babylon 5 Universe Data Proxy Endpoints
    // ==========================================

    /**
     * Proxy the B5 races list from the main server.
     */
    @GetMapping("/api/b5/races")
    public Object getB5Races() {
        return proxyGet("/api/v1/babylon5/races");
    }

    /**
     * Proxy the B5 characters list from the main server.
     */
    @GetMapping("/api/b5/characters")
    public Object getB5Characters() {
        return proxyGet("/api/v1/babylon5/characters");
    }

    /**
     * Proxy the B5 storylines list from the main server.
     */
    @GetMapping("/api/b5/storylines")
    public Object getB5Storylines() {
        return proxyGet("/api/v1/babylon5/storylines");
    }

    /**
     * Proxy the B5 dialogues list from the main server.
     */
    @GetMapping("/api/b5/dialogues")
    public Object getB5Dialogues() {
        return proxyGet("/api/v1/babylon5/dialogues");
    }

    /**
     * Proxy the B5 locations list from the main server.
     */
    @GetMapping("/api/b5/locations")
    public Object getB5Locations() {
        return proxyGet("/api/v1/babylon5/locations");
    }

    /**
     * Generic proxy GET helper for B5 endpoints.
     */
    private Object proxyGet(String path) {
        return proxyGet(path, 10);
    }

    /**
     * Generic proxy GET helper with configurable timeout seconds.
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
                return response.body();
            }
        } catch (java.net.http.HttpTimeoutException e) {
            log.warn("Proxy request to {} timed out after {}s: {}", path, timeoutSeconds, e.getMessage());
        } catch (Exception e) {
            log.warn("Failed to proxy request {}: {}", path, e.getMessage());
        }
        return Collections.emptyList();
    }
}
