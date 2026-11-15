package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.service.CommunityServerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Community Server Hosting.
 * <p>
 * Provides endpoints for server registration, lifecycle management, content sharing,
 * reviews, configuration templates, and server discovery.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/community-servers")
public class CommunityServerController {

    private final CommunityServerService communityServerService;

    public CommunityServerController(CommunityServerService communityServerService) {
        this.communityServerService = communityServerService;
    }

    // --- Enum Lists ---

    /**
     * Get all server statuses.
     */
    @GetMapping("/statuses")
    public List<Map<String, String>> getServerStatuses() {
        return communityServerService.getAllServerStatuses();
    }

    /**
     * Get all game modes.
     */
    @GetMapping("/game-modes")
    public List<Map<String, String>> getGameModes() {
        return communityServerService.getAllGameModes();
    }

    /**
     * Get all content types.
     */
    @GetMapping("/content-types")
    public List<Map<String, String>> getContentTypes() {
        return communityServerService.getAllContentTypes();
    }

    // --- Server Registration ---

    /**
     * Register a new community server.
     */
    @PostMapping("/register")
    public ResponseEntity<CommunityServerService.ServerInfo> registerServer(@RequestBody Map<String, Object> request) {
        String id = (String) request.get("id");
        String name = (String) request.get("name");
        String description = (String) request.getOrDefault("description", "");
        String host = (String) request.getOrDefault("host", "localhost");
        int port = request.containsKey("port") ? ((Number) request.get("port")).intValue() : 8080;
        String gameMode = (String) request.getOrDefault("gameMode", "GENERIC");
        int maxPlayers = request.containsKey("maxPlayers") ? ((Number) request.get("maxPlayers")).intValue() : 10;
        String ownerId = (String) request.get("ownerId");

        CommunityServerService.GameMode mode;
        try {
            mode = CommunityServerService.GameMode.valueOf(gameMode);
        } catch (IllegalArgumentException e) {
            mode = CommunityServerService.GameMode.GENERIC;
        }

        CommunityServerService.ServerInfo server = communityServerService.registerServer(
                id, name, description, host, port, mode, maxPlayers, ownerId);
        if (server == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(server);
    }

    /**
     * Get server by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CommunityServerService.ServerInfo> getServer(@PathVariable String id) {
        CommunityServerService.ServerInfo server = communityServerService.getServer(id);
        if (server == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(server);
    }

    /**
     * List all servers.
     */
    @GetMapping
    public List<CommunityServerService.ServerInfo> listServers() {
        return communityServerService.listServers();
    }

    /**
     * List active servers.
     */
    @GetMapping("/active")
    public List<CommunityServerService.ServerInfo> listActiveServers() {
        return communityServerService.listActiveServers();
    }

    /**
     * List servers by game mode.
     */
    @GetMapping("/game-mode/{gameMode}")
    public List<CommunityServerService.ServerInfo> listServersByGameMode(@PathVariable String gameMode) {
        CommunityServerService.GameMode mode;
        try {
            mode = CommunityServerService.GameMode.valueOf(gameMode);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
        return communityServerService.listServersByGameMode(mode);
    }

    /**
     * Search servers by tag.
     */
    @GetMapping("/tag/{tag}")
    public List<CommunityServerService.ServerInfo> searchServersByTag(@PathVariable String tag) {
        return communityServerService.searchServersByTag(tag);
    }

    /**
     * List servers with available slots.
     */
    @GetMapping("/available")
    public List<CommunityServerService.ServerInfo> listServersWithSlots() {
        return communityServerService.listServersWithSlots();
    }

    // --- Server Lifecycle ---

    /**
     * Activate a server.
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<CommunityServerService.ServerInfo> activateServer(@PathVariable String id) {
        CommunityServerService.ServerInfo server = communityServerService.activateServer(id);
        if (server == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(server);
    }

    /**
     * Pause a server.
     */
    @PostMapping("/{id}/pause")
    public ResponseEntity<CommunityServerService.ServerInfo> pauseServer(@PathVariable String id) {
        CommunityServerService.ServerInfo server = communityServerService.pauseServer(id);
        if (server == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(server);
    }

    /**
     * Set server offline.
     */
    @PostMapping("/{id}/offline")
    public ResponseEntity<CommunityServerService.ServerInfo> setOffline(@PathVariable String id) {
        CommunityServerService.ServerInfo server = communityServerService.setOffline(id);
        if (server == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(server);
    }

    /**
     * Update server heartbeat.
     */
    @PostMapping("/{id}/heartbeat")
    public ResponseEntity<Map<String, Boolean>> heartbeat(@PathVariable String id) {
        boolean ok = communityServerService.heartbeat(id);
        return ResponseEntity.ok(Map.of("success", ok));
    }

    /**
     * Update player count.
     */
    @PostMapping("/{id}/players")
    public ResponseEntity<CommunityServerService.ServerInfo> updatePlayerCount(
            @PathVariable String id, @RequestBody Map<String, Integer> request) {
        int count = request.getOrDefault("count", 0);
        CommunityServerService.ServerInfo server = communityServerService.updatePlayerCount(id, count);
        if (server == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(server);
    }

    /**
     * Check and mark stale servers as offline.
     */
    @PostMapping("/check-stale")
    public Map<String, Object> checkStaleServers() {
        List<String> stale = communityServerService.checkStaleServers();
        return Map.of("staleServers", stale, "count", stale.size());
    }

    // --- Tags ---

    /**
     * Add tags to a server.
     */
    @PostMapping("/{id}/tags")
    public ResponseEntity<CommunityServerService.ServerInfo> addTags(
            @PathVariable String id, @RequestBody Map<String, List<String>> request) {
        java.util.Set<String> tags = new java.util.HashSet<>(request.getOrDefault("tags", List.of()));
        CommunityServerService.ServerInfo server = communityServerService.addTags(id, tags);
        if (server == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(server);
    }

    // --- Content Sharing ---

    /**
     * Share content from a server.
     */
    @PostMapping("/{serverId}/content")
    public ResponseEntity<CommunityServerService.ContentItem> shareContent(
            @PathVariable String serverId, @RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String type = (String) request.getOrDefault("type", "MOD");
        String description = (String) request.getOrDefault("description", "");
        String author = (String) request.get("author");
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) request.getOrDefault("data", Map.of());

        CommunityServerService.ContentType contentType;
        try {
            contentType = CommunityServerService.ContentType.valueOf(type);
        } catch (IllegalArgumentException e) {
            contentType = CommunityServerService.ContentType.MOD;
        }

        CommunityServerService.ContentItem item = communityServerService.shareContent(
                serverId, name, contentType, description, author, data);
        if (item == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(item);
    }

    /**
     * Get content items for a server.
     */
    @GetMapping("/{serverId}/content")
    public List<CommunityServerService.ContentItem> getContent(@PathVariable String serverId) {
        return communityServerService.getContent(serverId);
    }

    /**
     * Get content by type across all servers.
     */
    @GetMapping("/content/type/{type}")
    public List<CommunityServerService.ContentItem> getContentByType(@PathVariable String type) {
        CommunityServerService.ContentType contentType;
        try {
            contentType = CommunityServerService.ContentType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
        return communityServerService.getContentByType(contentType);
    }

    /**
     * Search content by name.
     */
    @GetMapping("/content/search/{query}")
    public List<CommunityServerService.ContentItem> searchContent(@PathVariable String query) {
        return communityServerService.searchContent(query);
    }

    /**
     * Download content (increment download count).
     */
    @PostMapping("/{serverId}/content/{contentId}/download")
    public ResponseEntity<CommunityServerService.ContentItem> downloadContent(
            @PathVariable String serverId, @PathVariable String contentId) {
        CommunityServerService.ContentItem item = communityServerService.downloadContent(serverId, contentId);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    // --- Reviews ---

    /**
     * Add a review for a server.
     */
    @PostMapping("/{serverId}/reviews")
    public ResponseEntity<CommunityServerService.Review> addReview(
            @PathVariable String serverId, @RequestBody Map<String, Object> request) {
        String reviewerId = (String) request.get("reviewerId");
        int rating = request.containsKey("rating") ? ((Number) request.get("rating")).intValue() : 3;
        String comment = (String) request.getOrDefault("comment", "");

        CommunityServerService.Review review = communityServerService.addReview(
                serverId, reviewerId, rating, comment);
        if (review == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(review);
    }

    /**
     * Get reviews for a server.
     */
    @GetMapping("/{serverId}/reviews")
    public List<CommunityServerService.Review> getReviews(@PathVariable String serverId) {
        return communityServerService.getReviews(serverId);
    }

    /**
     * Get average rating for a server.
     */
    @GetMapping("/{serverId}/rating")
    public Map<String, Object> getAverageRating(@PathVariable String serverId) {
        double rating = communityServerService.getAverageRating(serverId);
        return Map.of("serverId", serverId, "averageRating", rating);
    }

    // --- Config Templates ---

    /**
     * Register a configuration template.
     */
    @PostMapping("/templates")
    public ResponseEntity<CommunityServerService.ConfigTemplate> registerTemplate(
            @RequestBody Map<String, Object> request) {
        String id = (String) request.get("id");
        String name = (String) request.get("name");
        String description = (String) request.getOrDefault("description", "");
        String gameMode = (String) request.getOrDefault("gameMode", "GENERIC");
        int maxPlayers = request.containsKey("maxPlayers") ? ((Number) request.get("maxPlayers")).intValue() : 10;
        @SuppressWarnings("unchecked")
        List<String> recommendedMods = (List<String>) request.getOrDefault("recommendedMods", List.of());
        @SuppressWarnings("unchecked")
        Map<String, Object> settings = (Map<String, Object>) request.getOrDefault("settings", Map.of());

        CommunityServerService.GameMode mode;
        try {
            mode = CommunityServerService.GameMode.valueOf(gameMode);
        } catch (IllegalArgumentException e) {
            mode = CommunityServerService.GameMode.GENERIC;
        }

        CommunityServerService.ConfigTemplate template = communityServerService.registerTemplate(
                id, name, description, mode, maxPlayers,
                new java.util.HashSet<>(recommendedMods), settings);
        if (template == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(template);
    }

    /**
     * Get template by ID.
     */
    @GetMapping("/templates/{id}")
    public ResponseEntity<CommunityServerService.ConfigTemplate> getTemplate(@PathVariable String id) {
        CommunityServerService.ConfigTemplate template = communityServerService.getTemplate(id);
        if (template == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(template);
    }

    /**
     * List all templates.
     */
    @GetMapping("/templates")
    public List<CommunityServerService.ConfigTemplate> listTemplates() {
        return communityServerService.listTemplates();
    }

    /**
     * List templates by game mode.
     */
    @GetMapping("/templates/game-mode/{gameMode}")
    public List<CommunityServerService.ConfigTemplate> listTemplatesByGameMode(@PathVariable String gameMode) {
        CommunityServerService.GameMode mode;
        try {
            mode = CommunityServerService.GameMode.valueOf(gameMode);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
        return communityServerService.listTemplatesByGameMode(mode);
    }

    /**
     * Create a server from a template.
     */
    @PostMapping("/templates/{templateId}/create-server")
    public ResponseEntity<CommunityServerService.ServerInfo> createServerFromTemplate(
            @PathVariable String templateId, @RequestBody Map<String, String> request) {
        String serverId = request.get("serverId");
        String ownerId = request.get("ownerId");

        CommunityServerService.ServerInfo server = communityServerService.createServerFromTemplate(
                templateId, serverId, ownerId);
        if (server == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(server);
    }

    // --- Statistics ---

    /**
     * Get overall server statistics.
     */
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        return communityServerService.getStatistics();
    }

    /**
     * Get server-specific statistics.
     */
    @GetMapping("/{id}/statistics")
    public Map<String, Object> getServerStatistics(@PathVariable String id) {
        return communityServerService.getServerStatistics(id);
    }

    // --- Management ---

    /**
     * Remove a server.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> removeServer(@PathVariable String id) {
        boolean removed = communityServerService.removeServer(id);
        return ResponseEntity.ok(Map.of("removed", removed));
    }

    /**
     * Clear all data.
     */
    @DeleteMapping("/clear-all")
    public void clearAll() {
        communityServerService.clearAll();
    }
}
