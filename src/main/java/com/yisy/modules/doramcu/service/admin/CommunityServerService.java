package com.yisy.modules.doramcu.service.admin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Community Server Hosting service for user-hosted game instances with shared content.
 * <p>
 * Enables community members to host their own game server instances, share content
 * (mods, scenarios, galaxy templates), and discover other servers.
 * </p>
 * <p>
 * Features:
 * <ul>
 *   <li>Server registration with metadata (name, description, game mode, capacity)</li>
 *   <li>Server lifecycle: registered → active → paused → offline</li>
 *   <li>Content sharing between servers (mods, scenarios, galaxy templates)</li>
 *   <li>Server discovery and filtering by game mode, capacity, tags</li>
 *   <li>Health monitoring with heartbeat tracking</li>
 *   <li>Server rating and review system</li>
 *   <li>Configuration templates for quick setup</li>
 * </ul>
 * </p>
 */
@Service
public class CommunityServerService {

    private static final Logger log = LoggerFactory.getLogger(CommunityServerService.class);
    private static final int MAX_SERVERS = 500;
    private static final int MAX_CONTENT_ITEMS = 100;
    private static final int MAX_REVIEWS = 50;
    private static final long HEARTBEAT_TIMEOUT_MS = 300_000; // 5 minutes
    private static final long PAUSE_TIMEOUT_MS = 3_600_000; // 1 hour

    /** Server ID → ServerInfo mapping. */
    private final ConcurrentHashMap<String, ServerInfo> servers = new ConcurrentHashMap<>();

    /** Active server IDs for quick lookup. */
    private final Set<String> activeServers = ConcurrentHashMap.newKeySet();

    /** Server ID → shared content items. */
    private final ConcurrentHashMap<String, List<ContentItem>> serverContent = new ConcurrentHashMap<>();

    /** Server ID → reviews. */
    private final ConcurrentHashMap<String, List<Review>> serverReviews = new ConcurrentHashMap<>();

    /** Configuration templates. */
    private final ConcurrentHashMap<String, ConfigTemplate> configTemplates = new ConcurrentHashMap<>();

    /** Counter for server registration order. */
    private final AtomicLong registrationOrder = new AtomicLong(0);

    // --- Enums ---

    /**
     * Server lifecycle status.
     */
    public enum ServerStatus {
        REGISTERED("Registered", "Server registered but not yet active"),
        ACTIVE("Active", "Server is running and accepting connections"),
        PAUSED("Paused", "Server is temporarily paused"),
        OFFLINE("Offline", "Server is offline");

        private final String label;
        private final String description;

        ServerStatus(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() { return label; }
        public String getDescription() { return description; }
    }

    /**
     * Game mode types for server filtering.
     */
    public enum GameMode {
        TRAVELLER("Traveller", "Space exploration and trade"),
        STAR_TREK_4X("Star Trek 4X", "Star Trek grand strategy"),
        BABYLON_5("Babylon 5", "Babylon 5 universe"),
        SFB("Star Fleet Battles", "Tactical starship combat"),
        DND_5E("D&D 5E", "Fantasy role-playing"),
        MECH_WARRIOR("MechWarrior", "Mech combat"),
        STAR_WARS("Star Wars", "Star Wars universe"),
        STARSHIP_TROOPERS("Starship Troopers", "Infantry vs bugs"),
        THIRD_REICH("Third Reich", "Alternate history ground combat"),
        ZERO_ZERO_SEVEN("007", "Spy operations"),
        GENERIC("Generic", "Generic game mode");

        private final String label;
        private final String description;

        GameMode(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() { return label; }
        public String getDescription() { return description; }
    }

    /**
     * Content type for shared items.
     */
    public enum ContentType {
        MOD("Mod", "Game modification"),
        SCENARIO("Scenario", "Pre-defined game scenario"),
        GALAXY_TEMPLATE("Galaxy Template", "Galaxy generation template"),
        RULESET("Ruleset", "Custom rules configuration"),
        CAMPAIGN("Campaign", "Multi-session campaign"),
        UI_THEME("UI Theme", "Visual customization");

        private final String label;
        private final String description;

        ContentType(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() { return label; }
        public String getDescription() { return description; }
    }

    // --- ServerInfo Model ---

    /**
     * Community server information.
     */
    public static class ServerInfo {
        private final String id;
        private final String name;
        private final String description;
        private final String host;
        private final int port;
        private final GameMode gameMode;
        private final int maxPlayers;
        private final int currentPlayers;
        private final ServerStatus status;
        private final Set<String> tags;
        private final Set<String> enabledMods;
        private final Instant registeredAt;
        private final Instant lastHeartbeat;
        private final long registrationOrder;
        private final String ownerId;

        private ServerInfo(Builder builder) {
            this.id = builder.id;
            this.name = builder.name;
            this.description = builder.description;
            this.host = builder.host;
            this.port = builder.port;
            this.gameMode = builder.gameMode;
            this.maxPlayers = builder.maxPlayers;
            this.currentPlayers = builder.currentPlayers;
            this.status = builder.status;
            this.tags = Collections.unmodifiableSet(new HashSet<>(builder.tags));
            this.enabledMods = Collections.unmodifiableSet(new HashSet<>(builder.enabledMods));
            this.registeredAt = builder.registeredAt;
            this.lastHeartbeat = builder.lastHeartbeat;
            this.registrationOrder = builder.registrationOrder;
            this.ownerId = builder.ownerId;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getHost() { return host; }
        public int getPort() { return port; }
        public GameMode getGameMode() { return gameMode; }
        public int getMaxPlayers() { return maxPlayers; }
        public int getCurrentPlayers() { return currentPlayers; }
        public ServerStatus getStatus() { return status; }
        public Set<String> getTags() { return tags; }
        public Set<String> getEnabledMods() { return enabledMods; }
        public Instant getRegisteredAt() { return registeredAt; }
        public Instant getLastHeartbeat() { return lastHeartbeat; }
        public long getRegistrationOrder() { return registrationOrder; }
        public String getOwnerId() { return ownerId; }

        public boolean hasAvailableSlots() {
            return currentPlayers < maxPlayers;
        }

        public boolean isOnline() {
            return status == ServerStatus.ACTIVE;
        }

        public boolean isStale() {
            return Instant.now().minusMillis(HEARTBEAT_TIMEOUT_MS).isAfter(lastHeartbeat);
        }

        public String getConnectionUrl() {
            return String.format("ws://%s:%d/ws/v1/4x/session", host, port);
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", id);
            map.put("name", name);
            map.put("description", description);
            map.put("host", host);
            map.put("port", port);
            map.put("gameMode", gameMode.name());
            map.put("maxPlayers", maxPlayers);
            map.put("currentPlayers", currentPlayers);
            map.put("status", status.name());
            map.put("tags", tags);
            map.put("enabledMods", enabledMods);
            map.put("registeredAt", registeredAt);
            map.put("lastHeartbeat", lastHeartbeat);
            map.put("ownerId", ownerId);
            map.put("connectionUrl", getConnectionUrl());
            return map;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String id;
            private String name;
            private String description = "";
            private String host = "localhost";
            private int port = 8080;
            private GameMode gameMode = GameMode.GENERIC;
            private int maxPlayers = 10;
            private int currentPlayers = 0;
            private ServerStatus status = ServerStatus.REGISTERED;
            private final Set<String> tags = new HashSet<>();
            private final Set<String> enabledMods = new HashSet<>();
            private Instant registeredAt = Instant.now();
            private Instant lastHeartbeat = Instant.now();
            private long registrationOrder;
            private String ownerId;

            public Builder id(String id) { this.id = id; return this; }
            public Builder name(String name) { this.name = name; return this; }
            public Builder description(String description) { this.description = description; return this; }
            public Builder host(String host) { this.host = host; return this; }
            public Builder port(int port) { this.port = port; return this; }
            public Builder gameMode(GameMode gameMode) { this.gameMode = gameMode; return this; }
            public Builder maxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; return this; }
            public Builder currentPlayers(int currentPlayers) { this.currentPlayers = currentPlayers; return this; }
            public Builder status(ServerStatus status) { this.status = status; return this; }
            public Builder tags(Set<String> tags) { this.tags.addAll(tags); return this; }
            public Builder enabledMods(Set<String> mods) { this.enabledMods.addAll(mods); return this; }
            public Builder registeredAt(Instant registeredAt) { this.registeredAt = registeredAt; return this; }
            public Builder lastHeartbeat(Instant lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; return this; }
            public Builder registrationOrder(long order) { this.registrationOrder = order; return this; }
            public Builder ownerId(String ownerId) { this.ownerId = ownerId; return this; }

            public ServerInfo build() {
                return new ServerInfo(this);
            }
        }
    }

    // --- ContentItem Model ---

    /**
     * Shared content item between servers.
     */
    public static class ContentItem {
        private final String id;
        private final String name;
        private final ContentType type;
        private final String description;
        private final String serverId;
        private final String author;
        private final Map<String, Object> data;
        private final Set<String> tags;
        private final Instant createdAt;
        private final int downloadCount;

        private ContentItem(Builder builder) {
            this.id = builder.id;
            this.name = builder.name;
            this.type = builder.type;
            this.description = builder.description;
            this.serverId = builder.serverId;
            this.author = builder.author;
            this.data = Collections.unmodifiableMap(new LinkedHashMap<>(builder.data));
            this.tags = Collections.unmodifiableSet(new HashSet<>(builder.tags));
            this.createdAt = builder.createdAt;
            this.downloadCount = builder.downloadCount;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public ContentType getType() { return type; }
        public String getDescription() { return description; }
        public String getServerId() { return serverId; }
        public String getAuthor() { return author; }
        public Map<String, Object> getData() { return data; }
        public Set<String> getTags() { return tags; }
        public Instant getCreatedAt() { return createdAt; }
        public int getDownloadCount() { return downloadCount; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", id);
            map.put("name", name);
            map.put("type", type.name());
            map.put("description", description);
            map.put("serverId", serverId);
            map.put("author", author);
            map.put("data", data);
            map.put("tags", tags);
            map.put("createdAt", createdAt);
            map.put("downloadCount", downloadCount);
            return map;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String id;
            private String name;
            private ContentType type = ContentType.MOD;
            private String description = "";
            private String serverId;
            private String author;
            private final Map<String, Object> data = new LinkedHashMap<>();
            private final Set<String> tags = new HashSet<>();
            private Instant createdAt = Instant.now();
            private int downloadCount = 0;

            public Builder id(String id) { this.id = id; return this; }
            public Builder name(String name) { this.name = name; return this; }
            public Builder type(ContentType type) { this.type = type; return this; }
            public Builder description(String description) { this.description = description; return this; }
            public Builder serverId(String serverId) { this.serverId = serverId; return this; }
            public Builder author(String author) { this.author = author; return this; }
            public Builder data(Map<String, Object> data) { this.data.putAll(data); return this; }
            public Builder tags(Set<String> tags) { this.tags.addAll(tags); return this; }
            public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
            public Builder downloadCount(int count) { this.downloadCount = count; return this; }

            public ContentItem build() {
                return new ContentItem(this);
            }
        }
    }

    // --- Review Model ---

    /**
     * Server review and rating.
     */
    public static class Review {
        private final String id;
        private final String serverId;
        private final String reviewerId;
        private final int rating;
        private final String comment;
        private final Instant createdAt;

        private Review(Builder builder) {
            this.id = builder.id;
            this.serverId = builder.serverId;
            this.reviewerId = builder.reviewerId;
            this.rating = builder.rating;
            this.comment = builder.comment;
            this.createdAt = builder.createdAt;
        }

        public String getId() { return id; }
        public String getServerId() { return serverId; }
        public String getReviewerId() { return reviewerId; }
        public int getRating() { return rating; }
        public String getComment() { return comment; }
        public Instant getCreatedAt() { return createdAt; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", id);
            map.put("serverId", serverId);
            map.put("reviewerId", reviewerId);
            map.put("rating", rating);
            map.put("comment", comment);
            map.put("createdAt", createdAt);
            return map;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String id;
            private String serverId;
            private String reviewerId;
            private int rating;
            private String comment = "";
            private Instant createdAt = Instant.now();

            public Builder id(String id) { this.id = id; return this; }
            public Builder serverId(String serverId) { this.serverId = serverId; return this; }
            public Builder reviewerId(String reviewerId) { this.reviewerId = reviewerId; return this; }
            public Builder rating(int rating) { this.rating = rating; return this; }
            public Builder comment(String comment) { this.comment = comment; return this; }
            public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

            public Review build() {
                return new Review(this);
            }
        }
    }

    // --- ConfigTemplate Model ---

    /**
     * Configuration template for quick server setup.
     */
    public static class ConfigTemplate {
        private final String id;
        private final String name;
        private final String description;
        private final GameMode gameMode;
        private final int maxPlayers;
        private final Set<String> recommendedMods;
        private final Map<String, Object> settings;
        private final Instant createdAt;

        private ConfigTemplate(Builder builder) {
            this.id = builder.id;
            this.name = builder.name;
            this.description = builder.description;
            this.gameMode = builder.gameMode;
            this.maxPlayers = builder.maxPlayers;
            this.recommendedMods = Collections.unmodifiableSet(new HashSet<>(builder.recommendedMods));
            this.settings = Collections.unmodifiableMap(new LinkedHashMap<>(builder.settings));
            this.createdAt = builder.createdAt;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public GameMode getGameMode() { return gameMode; }
        public int getMaxPlayers() { return maxPlayers; }
        public Set<String> getRecommendedMods() { return recommendedMods; }
        public Map<String, Object> getSettings() { return settings; }
        public Instant getCreatedAt() { return createdAt; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", id);
            map.put("name", name);
            map.put("description", description);
            map.put("gameMode", gameMode.name());
            map.put("maxPlayers", maxPlayers);
            map.put("recommendedMods", recommendedMods);
            map.put("settings", settings);
            map.put("createdAt", createdAt);
            return map;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String id;
            private String name;
            private String description = "";
            private GameMode gameMode = GameMode.GENERIC;
            private int maxPlayers = 10;
            private final Set<String> recommendedMods = new HashSet<>();
            private final Map<String, Object> settings = new LinkedHashMap<>();
            private Instant createdAt = Instant.now();

            public Builder id(String id) { this.id = id; return this; }
            public Builder name(String name) { this.name = name; return this; }
            public Builder description(String description) { this.description = description; return this; }
            public Builder gameMode(GameMode gameMode) { this.gameMode = gameMode; return this; }
            public Builder maxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; return this; }
            public Builder recommendedMods(Set<String> mods) { this.recommendedMods.addAll(mods); return this; }
            public Builder settings(Map<String, Object> settings) { this.settings.putAll(settings); return this; }
            public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

            public ConfigTemplate build() {
                return new ConfigTemplate(this);
            }
        }
    }

    // --- Server Registration ---

    /**
     * Register a new community server.
     */
    public ServerInfo registerServer(String id, String name, String description, String host, int port,
                                      GameMode gameMode, int maxPlayers, String ownerId) {
        if (servers.size() >= MAX_SERVERS) {
            log.warn("Cannot register server: max servers limit reached");
            return null;
        }
        if (id == null || id.isBlank()) {
            log.warn("Server ID cannot be blank");
            return null;
        }
        if (name == null || name.isBlank()) {
            log.warn("Server name cannot be blank");
            return null;
        }
        if (servers.containsKey(id)) {
            log.warn("Server already registered: {}", id);
            return null;
        }
        if (maxPlayers <= 0 || maxPlayers > 100) {
            log.warn("Max players must be between 1 and 100");
            return null;
        }

        ServerInfo server = ServerInfo.builder()
                .id(id)
                .name(name)
                .description(description)
                .host(host)
                .port(port)
                .gameMode(gameMode)
                .maxPlayers(maxPlayers)
                .ownerId(ownerId)
                .registrationOrder(registrationOrder.incrementAndGet())
                .build();

        servers.put(id, server);
        serverContent.putIfAbsent(id, new ArrayList<>());
        serverReviews.putIfAbsent(id, new ArrayList<>());
        log.info("Server registered: {} ({})", name, id);
        return server;
    }

    /**
     * Get server by ID.
     */
    public ServerInfo getServer(String id) {
        return servers.get(id);
    }

    /**
     * List all servers.
     */
    public List<ServerInfo> listServers() {
        return new ArrayList<>(servers.values());
    }

    /**
     * List active servers.
     */
    public List<ServerInfo> listActiveServers() {
        return servers.values().stream()
                .filter(s -> s.getStatus() == ServerStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    /**
     * List servers by game mode.
     */
    public List<ServerInfo> listServersByGameMode(GameMode gameMode) {
        return servers.values().stream()
                .filter(s -> s.getGameMode() == gameMode)
                .collect(Collectors.toList());
    }

    /**
     * Search servers by tag.
     */
    public List<ServerInfo> searchServersByTag(String tag) {
        return servers.values().stream()
                .filter(s -> s.getTags().contains(tag))
                .collect(Collectors.toList());
    }

    /**
     * List servers with available slots.
     */
    public List<ServerInfo> listServersWithSlots() {
        return servers.values().stream()
                .filter(ServerInfo::hasAvailableSlots)
                .collect(Collectors.toList());
    }

    // --- Server Lifecycle ---

    /**
     * Activate a server.
     */
    public ServerInfo activateServer(String id) {
        ServerInfo server = servers.get(id);
        if (server == null) {
            log.warn("Server not found: {}", id);
            return null;
        }
        if (server.getStatus() != ServerStatus.REGISTERED && server.getStatus() != ServerStatus.PAUSED) {
            log.warn("Server cannot be activated: {}", server.getStatus());
            return null;
        }
        ServerInfo updated = ServerInfo.builder()
                .id(server.getId())
                .name(server.getName())
                .description(server.getDescription())
                .host(server.getHost())
                .port(server.getPort())
                .gameMode(server.getGameMode())
                .maxPlayers(server.getMaxPlayers())
                .currentPlayers(server.getCurrentPlayers())
                .status(ServerStatus.ACTIVE)
                .tags(server.getTags())
                .enabledMods(server.getEnabledMods())
                .registeredAt(server.getRegisteredAt())
                .lastHeartbeat(Instant.now())
                .registrationOrder(server.getRegistrationOrder())
                .ownerId(server.getOwnerId())
                .build();
        servers.put(id, updated);
        activeServers.add(id);
        log.info("Server activated: {}", id);
        return updated;
    }

    /**
     * Pause a server.
     */
    public ServerInfo pauseServer(String id) {
        ServerInfo server = servers.get(id);
        if (server == null) {
            log.warn("Server not found: {}", id);
            return null;
        }
        if (server.getStatus() != ServerStatus.ACTIVE) {
            log.warn("Server cannot be paused: {}", server.getStatus());
            return null;
        }
        ServerInfo updated = ServerInfo.builder()
                .id(server.getId())
                .name(server.getName())
                .description(server.getDescription())
                .host(server.getHost())
                .port(server.getPort())
                .gameMode(server.getGameMode())
                .maxPlayers(server.getMaxPlayers())
                .currentPlayers(server.getCurrentPlayers())
                .status(ServerStatus.PAUSED)
                .tags(server.getTags())
                .enabledMods(server.getEnabledMods())
                .registeredAt(server.getRegisteredAt())
                .lastHeartbeat(Instant.now())
                .registrationOrder(server.getRegistrationOrder())
                .ownerId(server.getOwnerId())
                .build();
        servers.put(id, updated);
        activeServers.remove(id);
        log.info("Server paused: {}", id);
        return updated;
    }

    /**
     * Set server offline.
     */
    public ServerInfo setOffline(String id) {
        ServerInfo server = servers.get(id);
        if (server == null) {
            log.warn("Server not found: {}", id);
            return null;
        }
        ServerInfo updated = ServerInfo.builder()
                .id(server.getId())
                .name(server.getName())
                .description(server.getDescription())
                .host(server.getHost())
                .port(server.getPort())
                .gameMode(server.getGameMode())
                .maxPlayers(server.getMaxPlayers())
                .currentPlayers(0)
                .status(ServerStatus.OFFLINE)
                .tags(server.getTags())
                .enabledMods(server.getEnabledMods())
                .registeredAt(server.getRegisteredAt())
                .lastHeartbeat(Instant.now())
                .registrationOrder(server.getRegistrationOrder())
                .ownerId(server.getOwnerId())
                .build();
        servers.put(id, updated);
        activeServers.remove(id);
        log.info("Server offline: {}", id);
        return updated;
    }

    /**
     * Update server heartbeat.
     */
    public boolean heartbeat(String id) {
        ServerInfo server = servers.get(id);
        if (server == null) {
            return false;
        }
        ServerInfo updated = ServerInfo.builder()
                .id(server.getId())
                .name(server.getName())
                .description(server.getDescription())
                .host(server.getHost())
                .port(server.getPort())
                .gameMode(server.getGameMode())
                .maxPlayers(server.getMaxPlayers())
                .currentPlayers(server.getCurrentPlayers())
                .status(server.getStatus())
                .tags(server.getTags())
                .enabledMods(server.getEnabledMods())
                .registeredAt(server.getRegisteredAt())
                .lastHeartbeat(Instant.now())
                .registrationOrder(server.getRegistrationOrder())
                .ownerId(server.getOwnerId())
                .build();
        servers.put(id, updated);
        return true;
    }

    /**
     * Update player count.
     */
    public ServerInfo updatePlayerCount(String id, int count) {
        ServerInfo server = servers.get(id);
        if (server == null) {
            log.warn("Server not found: {}", id);
            return null;
        }
        if (count < 0 || count > server.getMaxPlayers()) {
            log.warn("Invalid player count: {} for server {}", count, id);
            return null;
        }
        ServerInfo updated = ServerInfo.builder()
                .id(server.getId())
                .name(server.getName())
                .description(server.getDescription())
                .host(server.getHost())
                .port(server.getPort())
                .gameMode(server.getGameMode())
                .maxPlayers(server.getMaxPlayers())
                .currentPlayers(count)
                .status(server.getStatus())
                .tags(server.getTags())
                .enabledMods(server.getEnabledMods())
                .registeredAt(server.getRegisteredAt())
                .lastHeartbeat(server.getLastHeartbeat())
                .registrationOrder(server.getRegistrationOrder())
                .ownerId(server.getOwnerId())
                .build();
        servers.put(id, updated);
        return updated;
    }

    /**
     * Check and mark stale servers as offline.
     */
    public List<String> checkStaleServers() {
        List<String> stale = new ArrayList<>();
        for (Map.Entry<String, ServerInfo> entry : servers.entrySet()) {
            if (entry.getValue().isStale() && entry.getValue().getStatus() == ServerStatus.ACTIVE) {
                setOffline(entry.getKey());
                stale.add(entry.getKey());
                log.warn("Server marked offline due to stale heartbeat: {}", entry.getKey());
            }
        }
        return stale;
    }

    // --- Tags ---

    /**
     * Add tags to a server.
     */
    public ServerInfo addTags(String id, Set<String> tags) {
        ServerInfo server = servers.get(id);
        if (server == null) {
            return null;
        }
        Set<String> updatedTags = new HashSet<>(server.getTags());
        updatedTags.addAll(tags);
        ServerInfo updated = ServerInfo.builder()
                .id(server.getId())
                .name(server.getName())
                .description(server.getDescription())
                .host(server.getHost())
                .port(server.getPort())
                .gameMode(server.getGameMode())
                .maxPlayers(server.getMaxPlayers())
                .currentPlayers(server.getCurrentPlayers())
                .status(server.getStatus())
                .tags(updatedTags)
                .enabledMods(server.getEnabledMods())
                .registeredAt(server.getRegisteredAt())
                .lastHeartbeat(server.getLastHeartbeat())
                .registrationOrder(server.getRegistrationOrder())
                .ownerId(server.getOwnerId())
                .build();
        servers.put(id, updated);
        return updated;
    }

    // --- Content Sharing ---

    /**
     * Share content from a server.
     */
    public ContentItem shareContent(String serverId, String name, ContentType type,
                                     String description, String author, Map<String, Object> data) {
        if (!servers.containsKey(serverId)) {
            log.warn("Server not found: {}", serverId);
            return null;
        }
        List<ContentItem> content = serverContent.get(serverId);
        if (content.size() >= MAX_CONTENT_ITEMS) {
            log.warn("Max content items reached for server: {}", serverId);
            return null;
        }
        String id = UUID.randomUUID().toString().substring(0, 8);
        ContentItem item = ContentItem.builder()
                .id(id)
                .name(name)
                .type(type)
                .description(description)
                .serverId(serverId)
                .author(author)
                .data(data)
                .build();
        content.add(item);
        log.info("Content shared: {} from {}", name, serverId);
        return item;
    }

    /**
     * Get content items for a server.
     */
    public List<ContentItem> getContent(String serverId) {
        return new ArrayList<>(serverContent.getOrDefault(serverId, Collections.emptyList()));
    }

    /**
     * Get content by type across all servers.
     */
    public List<ContentItem> getContentByType(ContentType type) {
        List<ContentItem> result = new ArrayList<>();
        for (List<ContentItem> content : serverContent.values()) {
            result.addAll(content.stream()
                    .filter(item -> item.getType() == type)
                    .collect(Collectors.toList()));
        }
        return result;
    }

    /**
     * Search content by name.
     */
    public List<ContentItem> searchContent(String query) {
        List<ContentItem> result = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (List<ContentItem> content : serverContent.values()) {
            result.addAll(content.stream()
                    .filter(item -> item.getName().toLowerCase().contains(lowerQuery))
                    .collect(Collectors.toList()));
        }
        return result;
    }

    /**
     * Increment download count for content.
     */
    public ContentItem downloadContent(String serverId, String contentId) {
        List<ContentItem> content = serverContent.get(serverId);
        if (content == null) {
            return null;
        }
        for (int i = 0; i < content.size(); i++) {
            ContentItem item = content.get(i);
            if (item.getId().equals(contentId)) {
                ContentItem updated = ContentItem.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .type(item.getType())
                        .description(item.getDescription())
                        .serverId(item.getServerId())
                        .author(item.getAuthor())
                        .data(item.getData())
                        .tags(item.getTags())
                        .createdAt(item.getCreatedAt())
                        .downloadCount(item.getDownloadCount() + 1)
                        .build();
                content.set(i, updated);
                return updated;
            }
        }
        return null;
    }

    // --- Reviews ---

    /**
     * Add a review for a server.
     */
    public Review addReview(String serverId, String reviewerId, int rating, String comment) {
        if (!servers.containsKey(serverId)) {
            log.warn("Server not found: {}", serverId);
            return null;
        }
        if (rating < 1 || rating > 5) {
            log.warn("Rating must be between 1 and 5");
            return null;
        }
        List<Review> reviews = serverReviews.get(serverId);
        if (reviews.size() >= MAX_REVIEWS) {
            log.warn("Max reviews reached for server: {}", serverId);
            return null;
        }
        String id = UUID.randomUUID().toString().substring(0, 8);
        Review review = Review.builder()
                .id(id)
                .serverId(serverId)
                .reviewerId(reviewerId)
                .rating(rating)
                .comment(comment)
                .build();
        reviews.add(review);
        log.info("Review added for {}: {} stars", serverId, rating);
        return review;
    }

    /**
     * Get reviews for a server.
     */
    public List<Review> getReviews(String serverId) {
        return new ArrayList<>(serverReviews.getOrDefault(serverId, Collections.emptyList()));
    }

    /**
     * Get average rating for a server.
     */
    public double getAverageRating(String serverId) {
        List<Review> reviews = serverReviews.get(serverId);
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    // --- Config Templates ---

    /**
     * Register a configuration template.
     */
    public ConfigTemplate registerTemplate(String id, String name, String description,
                                            GameMode gameMode, int maxPlayers,
                                            Set<String> recommendedMods, Map<String, Object> settings) {
        if (id == null || id.isBlank()) {
            return null;
        }
        if (name == null || name.isBlank()) {
            return null;
        }
        ConfigTemplate template = ConfigTemplate.builder()
                .id(id)
                .name(name)
                .description(description)
                .gameMode(gameMode)
                .maxPlayers(maxPlayers)
                .recommendedMods(recommendedMods)
                .settings(settings)
                .build();
        configTemplates.put(id, template);
        log.info("Config template registered: {}", id);
        return template;
    }

    /**
     * Get template by ID.
     */
    public ConfigTemplate getTemplate(String id) {
        return configTemplates.get(id);
    }

    /**
     * List all templates.
     */
    public List<ConfigTemplate> listTemplates() {
        return new ArrayList<>(configTemplates.values());
    }

    /**
     * List templates by game mode.
     */
    public List<ConfigTemplate> listTemplatesByGameMode(GameMode gameMode) {
        return configTemplates.values().stream()
                .filter(t -> t.getGameMode() == gameMode)
                .collect(Collectors.toList());
    }

    /**
     * Create a server from a template.
     */
    public ServerInfo createServerFromTemplate(String templateId, String serverId, String ownerId) {
        ConfigTemplate template = configTemplates.get(templateId);
        if (template == null) {
            log.warn("Template not found: {}", templateId);
            return null;
        }
        return registerServer(
                serverId,
                template.getName() + " Instance",
                template.getDescription(),
                "localhost",
                8080,
                template.getGameMode(),
                template.getMaxPlayers(),
                ownerId
        );
    }

    // --- Statistics ---

    /**
     * Get server statistics.
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalServers", servers.size());
        stats.put("activeServers", activeServers.size());
        stats.put("totalContent", serverContent.values().stream().mapToInt(List::size).sum());
        stats.put("totalReviews", serverReviews.values().stream().mapToInt(List::size).sum());
        stats.put("templates", configTemplates.size());

        // By game mode
        Map<String, Long> byGameMode = servers.values().stream()
                .collect(Collectors.groupingBy(s -> s.getGameMode().name(), Collectors.counting()));
        stats.put("byGameMode", byGameMode);

        // By status
        Map<String, Long> byStatus = servers.values().stream()
                .collect(Collectors.groupingBy(s -> s.getStatus().name(), Collectors.counting()));
        stats.put("byStatus", byStatus);

        // Average rating
        double avgRating = serverReviews.values().stream()
                .flatMap(List::stream)
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        stats.put("averageRating", Math.round(avgRating * 100.0) / 100.0);

        return stats;
    }

    /**
     * Get server-specific statistics.
     */
    public Map<String, Object> getServerStatistics(String serverId) {
        ServerInfo server = servers.get(serverId);
        if (server == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("serverId", serverId);
        stats.put("status", server.getStatus().name());
        stats.put("gameMode", server.getGameMode().name());
        stats.put("currentPlayers", server.getCurrentPlayers());
        stats.put("maxPlayers", server.getMaxPlayers());
        stats.put("contentItems", serverContent.getOrDefault(serverId, Collections.emptyList()).size());
        stats.put("reviews", serverReviews.getOrDefault(serverId, Collections.emptyList()).size());
        stats.put("averageRating", Math.round(getAverageRating(serverId) * 100.0) / 100.0);
        stats.put("uptime", server.getRegisteredAt());
        return stats;
    }

    // --- Management ---

    /**
     * Remove a server.
     */
    public boolean removeServer(String id) {
        ServerInfo server = servers.get(id);
        if (server == null) {
            return false;
        }
        if (server.getStatus() == ServerStatus.ACTIVE) {
            log.warn("Cannot remove active server: {}", id);
            return false;
        }
        servers.remove(id);
        activeServers.remove(id);
        serverContent.remove(id);
        serverReviews.remove(id);
        log.info("Server removed: {}", id);
        return true;
    }

    /**
     * Clear all data.
     */
    public void clearAll() {
        servers.clear();
        activeServers.clear();
        serverContent.clear();
        serverReviews.clear();
        configTemplates.clear();
        registrationOrder.set(0);
        log.info("All community server data cleared");
    }

    // --- Enum Lists ---

    /**
     * Get all server statuses.
     */
    public List<Map<String, String>> getAllServerStatuses() {
        List<Map<String, String>> result = new ArrayList<>();
        for (ServerStatus status : ServerStatus.values()) {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("name", status.name());
            map.put("label", status.getLabel());
            map.put("description", status.getDescription());
            result.add(map);
        }
        return result;
    }

    /**
     * Get all game modes.
     */
    public List<Map<String, String>> getAllGameModes() {
        List<Map<String, String>> result = new ArrayList<>();
        for (GameMode mode : GameMode.values()) {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("name", mode.name());
            map.put("label", mode.getLabel());
            map.put("description", mode.getDescription());
            result.add(map);
        }
        return result;
    }

    /**
     * Get all content types.
     */
    public List<Map<String, String>> getAllContentTypes() {
        List<Map<String, String>> result = new ArrayList<>();
        for (ContentType type : ContentType.values()) {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("name", type.name());
            map.put("label", type.getLabel());
            map.put("description", type.getDescription());
            result.add(map);
        }
        return result;
    }
}
