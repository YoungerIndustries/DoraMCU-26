package com.yisy.modules.doramcu.service.admin;
import com.yisy.modules.doramcu.model.players.Player;
import org.springframework.stereotype.Service;

import org.springframework.stereotype.Service;import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Plugin Marketplace service for community-created content distribution.
 * <p>
 * Builds on top of ModService to provide a community-facing marketplace where users can
 * browse, search, rate, and download plugins. Plugins are categorized, rated, and
 * featured to help users discover quality content.
 * </p>
 * <p>
 * Features:
 * <ul>
 *   <li>Plugin listing with pagination and filtering</li>
 *   <li>Search by name, description, tags, and category</li>
 *   <li>Rating system with average calculation</li>
 *   <li>Download tracking and popularity ranking</li>
 *   <li>Category-based organization</li>
 *   <li>Featured and trending plugins</li>
 *   <li>Plugin approval workflow</li>
 *   <li>Statistics and analytics</li>
 * </ul>
 * </p>
 */

@Service
public class PluginMarketplaceService {

    private static final Logger log = LoggerFactory.getLogger(PluginMarketplaceService.class);
    private static final int MAX_PLUGINS = 500;
    private static final int MAX_RATINGS_PER_PLUGIN = 1000;
    private static final int MAX_TAGS = 10;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;
    private static final int PAGE_SIZE = 20;

    /** Plugin categories for organization. */
    public enum Category {
        GAME_MODE("game_mode", "Complete game modes"),
        RULESET("ruleset", "Rules and mechanics modifications"),
        CONTENT_PACK("content_pack", "Additional content and assets"),
        UI_THEME("ui_theme", "Visual appearance changes"),
        AI_BEHAVIOR("ai_behavior", "AI and NPC behavior modifications"),
        GALAXY_TEMPLATE("galaxy_template", "Galaxy generation templates"),
        CHARACTER_CLASS("character_class", "New character classes and types"),
        EVENT_SYSTEM("event_system", "Event and quest systems"),
        UTILITY("utility", "Tools and utilities"),
        SOCIAL("social", "Social and multiplayer enhancements");

        private final String id;
        private final String description;

        Category(String id, String description) {
            this.id = id;
            this.description = description;
        }

        public String getId() { return id; }
        public String getDescription() { return description; }
    }

    /** Plugin approval status. */
    public enum ApprovalStatus {
        PENDING("pending", "Awaiting review"),
        APPROVED("approved", "Approved and available"),
        REJECTED("rejected", "Rejected by moderators"),
        FLAGGED("flagged", "Flagged for review");

        private final String id;
        private final String description;

        ApprovalStatus(String id, String description) {
            this.id = id;
            this.description = description;
        }

        public String getId() { return id; }
        public String getDescription() { return description; }
    }

    /**
     * Plugin listing in the marketplace.
     */
    public static class PluginListing {
        private final String id;
        private final String name;
        private final String description;
        private final String author;
        private final String version;
        private final Category category;
        private final List<String> tags;
        private final ApprovalStatus approvalStatus;
        private final Instant createdAt;
        private final Instant updatedAt;
        private final int downloadCount;
        private final double averageRating;
        private final int ratingCount;
        private final boolean isFeatured;
        private final Map<String, Object> metadata;

        private PluginListing(Builder builder) {
            this.id = builder.id;
            this.name = builder.name;
            this.description = builder.description;
            this.author = builder.author;
            this.version = builder.version;
            this.category = builder.category;
            this.tags = new ArrayList<>(builder.tags);
            this.approvalStatus = builder.approvalStatus;
            this.createdAt = builder.createdAt;
            this.updatedAt = builder.updatedAt;
            this.downloadCount = builder.downloadCount;
            this.averageRating = builder.averageRating;
            this.ratingCount = builder.ratingCount;
            this.isFeatured = builder.isFeatured;
            this.metadata = new HashMap<>(builder.metadata);
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getAuthor() { return author; }
        public String getVersion() { return version; }
        public Category getCategory() { return category; }
        public List<String> getTags() { return tags; }
        public ApprovalStatus getApprovalStatus() { return approvalStatus; }
        public Instant getCreatedAt() { return createdAt; }
        public Instant getUpdatedAt() { return updatedAt; }
        public int getDownloadCount() { return downloadCount; }
        public double getAverageRating() { return averageRating; }
        public int getRatingCount() { return ratingCount; }
        public boolean isFeatured() { return isFeatured; }
        public Map<String, Object> getMetadata() { return metadata; }

        public static Builder builder(String id) {
            return new Builder(id);
        }

        public static class Builder {
            private final String id;
            private String name = "";
            private String description = "";
            private String author = "";
            private String version = "1.0.0";
            private Category category = Category.UTILITY;
            private final List<String> tags = new ArrayList<>();
            private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;
            private Instant createdAt = Instant.now();
            private Instant updatedAt = Instant.now();
            private int downloadCount = 0;
            private double averageRating = 0.0;
            private int ratingCount = 0;
            private boolean isFeatured = false;
            private final Map<String, Object> metadata = new HashMap<>();

            public Builder(String id) {
                this.id = id;
            }

            public Builder name(String name) { this.name = name; return this; }
            public Builder description(String description) { this.description = description; return this; }
            public Builder author(String author) { this.author = author; return this; }
            public Builder version(String version) { this.version = version; return this; }
            public Builder category(Category category) { this.category = category; return this; }
            public Builder tags(List<String> tags) { this.tags.addAll(tags); return this; }
            public Builder tag(String tag) { this.tags.add(tag); return this; }
            public Builder approvalStatus(ApprovalStatus status) { this.approvalStatus = status; return this; }
            public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
            public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
            public Builder downloadCount(int count) { this.downloadCount = count; return this; }
            public Builder averageRating(double rating) { this.averageRating = rating; return this; }
            public Builder ratingCount(int count) { this.ratingCount = count; return this; }
            public Builder featured(boolean featured) { this.isFeatured = featured; return this; }
            public Builder metadata(String key, Object value) { this.metadata.put(key, value); return this; }
            public PluginListing build() { return new PluginListing(this); }
        }
    }

    /**
     * Rating for a plugin.
     */
    public static class PluginRating {
        private final String pluginId;
        private final String userId;
        private final int rating;
        private final String comment;
        private final Instant createdAt;

        private PluginRating(Builder builder) {
            this.pluginId = builder.pluginId;
            this.userId = builder.userId;
            this.rating = builder.rating;
            this.comment = builder.comment;
            this.createdAt = builder.createdAt;
        }

        public String getPluginId() { return pluginId; }
        public String getUserId() { return userId; }
        public int getRating() { return rating; }
        public String getComment() { return comment; }
        public Instant getCreatedAt() { return createdAt; }

        public static Builder builder(String pluginId, String userId) {
            return new Builder(pluginId, userId);
        }

        public static class Builder {
            private final String pluginId;
            private final String userId;
            private int rating = 5;
            private String comment = "";
            private Instant createdAt = Instant.now();

            public Builder(String pluginId, String userId) {
                this.pluginId = pluginId;
                this.userId = userId;
            }

            public Builder rating(int rating) {
                if (rating < 1 || rating > 5) throw new IllegalArgumentException("Rating must be 1-5");
                this.rating = rating;
                return this;
            }

            public Builder comment(String comment) { this.comment = comment; return this; }
            public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
            public PluginRating build() { return new PluginRating(this); }
        }
    }

    /**
     * Paginated plugin list result.
     */
    public static class PaginatedPlugins {
        private final List<PluginListing> plugins;
        private final int page;
        private final int pageSize;
        private final int totalPages;
        private final int totalPlugins;

        public PaginatedPlugins(List<PluginListing> plugins, int page, int pageSize, int totalPlugins) {
            this.plugins = plugins;
            this.page = page;
            this.pageSize = pageSize;
            this.totalPlugins = totalPlugins;
            this.totalPages = (int) Math.ceil((double) totalPlugins / pageSize);
        }

        public List<PluginListing> getPlugins() { return plugins; }
        public int getPage() { return page; }
        public int getPageSize() { return pageSize; }
        public int getTotalPages() { return totalPages; }
        public int getTotalPlugins() { return totalPlugins; }
    }

    /**
     * Marketplace statistics.
     */
    public static class MarketplaceStats {
        private final int totalPlugins;
        private final int approvedPlugins;
        private final int pendingPlugins;
        private final int totalDownloads;
        private final double averageRating;
        private final Map<Category, Integer> pluginsByCategory;
        private final Map<String, Integer> pluginsByAuthor;

        private MarketplaceStats(Builder builder) {
            this.totalPlugins = builder.totalPlugins;
            this.approvedPlugins = builder.approvedPlugins;
            this.pendingPlugins = builder.pendingPlugins;
            this.totalDownloads = builder.totalDownloads;
            this.averageRating = builder.averageRating;
            this.pluginsByCategory = new EnumMap<>(builder.pluginsByCategory);
            this.pluginsByAuthor = new HashMap<>(builder.pluginsByAuthor);
        }

        public int getTotalPlugins() { return totalPlugins; }
        public int getApprovedPlugins() { return approvedPlugins; }
        public int getPendingPlugins() { return pendingPlugins; }
        public int getTotalDownloads() { return totalDownloads; }
        public double getAverageRating() { return averageRating; }
        public Map<Category, Integer> getPluginsByCategory() { return pluginsByCategory; }
        public Map<String, Integer> getPluginsByAuthor() { return pluginsByAuthor; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private int totalPlugins;
            private int approvedPlugins;
            private int pendingPlugins;
            private int totalDownloads;
            private double averageRating;
            private final Map<Category, Integer> pluginsByCategory = new EnumMap<>(Category.class);
            private final Map<String, Integer> pluginsByAuthor = new HashMap<>();

            public Builder totalPlugins(int count) { this.totalPlugins = count; return this; }
            public Builder approvedPlugins(int count) { this.approvedPlugins = count; return this; }
            public Builder pendingPlugins(int count) { this.pendingPlugins = count; return this; }
            public Builder totalDownloads(int count) { this.totalDownloads = count; return this; }
            public Builder averageRating(double rating) { this.averageRating = rating; return this; }
            public Builder pluginsByCategory(Map<Category, Integer> byCategory) {
                this.pluginsByCategory.putAll(byCategory);
                return this;
            }
            public Builder pluginsByAuthor(Map<String, Integer> byAuthor) {
                this.pluginsByAuthor.putAll(byAuthor);
                return this;
            }
            public MarketplaceStats build() { return new MarketplaceStats(this); }
        }
    }

    // ---- Service state ----

    private final ConcurrentHashMap<String, PluginListing> plugins = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<PluginRating>> ratings = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> downloadCounts = new ConcurrentHashMap<>();
    private final Set<String> featuredPlugins = ConcurrentHashMap.newKeySet();
    private final AtomicLong pluginIdCounter = new AtomicLong(0);

    /**
     * Register a new plugin listing.
     *
     * @param name plugin name
     * @param description plugin description
     * @param author plugin author
     * @param category plugin category
     * @return the created plugin listing
     */
    public Optional<PluginListing> registerPlugin(String name, String description, String author, Category category) {
        if (name == null || name.isBlank()) {
            log.warn("Plugin name cannot be blank");
            return Optional.empty();
        }
        if (author == null || author.isBlank()) {
            log.warn("Plugin author cannot be blank");
            return Optional.empty();
        }
        if (plugins.size() >= MAX_PLUGINS) {
            log.warn("Marketplace full: {} plugins", MAX_PLUGINS);
            return Optional.empty();
        }
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            log.warn("Plugin description too long: {} chars", description.length());
            return Optional.empty();
        }

        String id = "plugin-" + pluginIdCounter.incrementAndGet();
        PluginListing plugin = PluginListing.builder(id)
                .name(name)
                .description(description != null ? description : "")
                .author(author)
                .category(category != null ? category : Category.UTILITY)
                .approvalStatus(ApprovalStatus.PENDING)
                .build();

        plugins.put(id, plugin);
        ratings.put(id, new ArrayList<>());
        downloadCounts.put(id, 0);
        log.info("Plugin registered: {} ({}) by {}", name, id, author);
        return Optional.of(plugin);
    }

    /**
     * Get a plugin by ID.
     */
    public Optional<PluginListing> getPlugin(String pluginId) {
        return Optional.ofNullable(plugins.get(pluginId));
    }

    /**
     * List plugins with pagination and filtering.
     *
     * @param page page number (0-based)
     * @param pageSize page size
     * @param category filter by category (null for all)
     * @param approvalStatus filter by approval status (null for all)
     * @return paginated plugin list
     */
    public PaginatedPlugins listPlugins(int page, int pageSize, Category category, ApprovalStatus approvalStatus) {
        List<PluginListing> filtered = plugins.values().stream()
                .filter(p -> category == null || p.getCategory() == category)
                .filter(p -> approvalStatus == null || p.getApprovalStatus() == approvalStatus)
                .sorted(Comparator.comparing(PluginListing::getCreatedAt).reversed())
                .collect(Collectors.toList());

        int total = filtered.size();
        int from = page * pageSize;
        int to = Math.min(from + pageSize, total);

        List<PluginListing> pagePlugins = filtered.subList(from, to);
        return new PaginatedPlugins(pagePlugins, page, pageSize, total);
    }

    /**
     * Search plugins by query string.
     *
     * @param query search query (matches name, description, tags, author)
     * @return list of matching plugins
     */
    public List<PluginListing> searchPlugins(String query) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>(plugins.values());
        }

        String lowerQuery = query.toLowerCase();
        return plugins.values().stream()
                .filter(p -> p.getName().toLowerCase().contains(lowerQuery) ||
                        p.getDescription().toLowerCase().contains(lowerQuery) ||
                        p.getAuthor().toLowerCase().contains(lowerQuery) ||
                        p.getTags().stream().anyMatch(t -> t.toLowerCase().contains(lowerQuery)))
                .sorted(Comparator.comparing(PluginListing::getAverageRating).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get plugins by category.
     */
    public List<PluginListing> getPluginsByCategory(Category category) {
        return plugins.values().stream()
                .filter(p -> p.getCategory() == category)
                .sorted(Comparator.comparing(PluginListing::getAverageRating).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get featured plugins.
     */
    public List<PluginListing> getFeaturedPlugins() {
        return featuredPlugins.stream()
                .map(plugins::get)
                .filter(p -> p != null)
                .sorted(Comparator.comparing(PluginListing::getAverageRating).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get trending plugins (most downloaded in recent period).
     *
     * @param limit number of plugins to return
     */
    public List<PluginListing> getTrendingPlugins(int limit) {
        return plugins.values().stream()
                .sorted(Comparator.comparing(PluginListing::getDownloadCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get approved plugins only.
     */
    public List<PluginListing> getApprovedPlugins() {
        return plugins.values().stream()
                .filter(p -> p.getApprovalStatus() == ApprovalStatus.APPROVED)
                .sorted(Comparator.comparing(PluginListing::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Rate a plugin.
     *
     * @param pluginId plugin ID
     * @param userId user ID
     * @param rating rating (1-5)
     * @param comment optional comment
     * @return the created rating, or empty if plugin not found
     */
    public Optional<PluginRating> ratePlugin(String pluginId, String userId, int rating, String comment) {
        PluginListing plugin = plugins.get(pluginId);
        if (plugin == null) {
            return Optional.empty();
        }
        if (rating < 1 || rating > 5) {
            log.warn("Invalid rating: {} for plugin {}", rating, pluginId);
            return Optional.empty();
        }

        List<PluginRating> pluginRatings = ratings.computeIfAbsent(pluginId, k -> new ArrayList<>());

        // Remove existing rating from this user
        pluginRatings.removeIf(r -> r.getUserId().equals(userId));

        PluginRating newRating = PluginRating.builder(pluginId, userId)
                .rating(rating)
                .comment(comment != null ? comment : "")
                .build();

        pluginRatings.add(newRating);

        // Update average rating
        double avg = pluginRatings.stream().mapToInt(PluginRating::getRating).average().orElse(0.0);
        int count = pluginRatings.size();

        plugins.put(pluginId, PluginListing.builder(pluginId)
                .name(plugin.getName())
                .description(plugin.getDescription())
                .author(plugin.getAuthor())
                .version(plugin.getVersion())
                .category(plugin.getCategory())
                .tags(plugin.getTags())
                .approvalStatus(plugin.getApprovalStatus())
                .createdAt(plugin.getCreatedAt())
                .updatedAt(Instant.now())
                .downloadCount(plugin.getDownloadCount())
                .averageRating(avg)
                .ratingCount(count)
                .featured(plugin.isFeatured())
                .build());

        log.info("Plugin {} rated {} by {}", pluginId, rating, userId);
        return Optional.of(newRating);
    }

    /**
     * Get ratings for a plugin.
     */
    public List<PluginRating> getRatings(String pluginId) {
        return ratings.getOrDefault(pluginId, Collections.emptyList());
    }

    /**
     * Record a download for a plugin.
     *
     * @param pluginId plugin ID
     * @return true if download recorded
     */
    public boolean recordDownload(String pluginId) {
        PluginListing plugin = plugins.get(pluginId);
        if (plugin == null) {
            return false;
        }

        int count = downloadCounts.merge(pluginId, 1, Integer::sum);
        plugins.put(pluginId, PluginListing.builder(pluginId)
                .name(plugin.getName())
                .description(plugin.getDescription())
                .author(plugin.getAuthor())
                .version(plugin.getVersion())
                .category(plugin.getCategory())
                .tags(plugin.getTags())
                .approvalStatus(plugin.getApprovalStatus())
                .createdAt(plugin.getCreatedAt())
                .updatedAt(Instant.now())
                .downloadCount(count)
                .averageRating(plugin.getAverageRating())
                .ratingCount(plugin.getRatingCount())
                .featured(plugin.isFeatured())
                .build());

        log.info("Download recorded for {}: {} total", pluginId, count);
        return true;
    }

    /**
     * Approve a plugin.
     */
    public boolean approvePlugin(String pluginId) {
        return updateApprovalStatus(pluginId, ApprovalStatus.APPROVED);
    }

    /**
     * Reject a plugin.
     */
    public boolean rejectPlugin(String pluginId) {
        return updateApprovalStatus(pluginId, ApprovalStatus.REJECTED);
    }

    /**
     * Flag a plugin for review.
     */
    public boolean flagPlugin(String pluginId) {
        return updateApprovalStatus(pluginId, ApprovalStatus.FLAGGED);
    }

    /**
     * Update approval status.
     */
    private boolean updateApprovalStatus(String pluginId, ApprovalStatus status) {
        PluginListing plugin = plugins.get(pluginId);
        if (plugin == null) {
            return false;
        }

        plugins.put(pluginId, PluginListing.builder(pluginId)
                .name(plugin.getName())
                .description(plugin.getDescription())
                .author(plugin.getAuthor())
                .version(plugin.getVersion())
                .category(plugin.getCategory())
                .tags(plugin.getTags())
                .approvalStatus(status)
                .createdAt(plugin.getCreatedAt())
                .updatedAt(Instant.now())
                .downloadCount(plugin.getDownloadCount())
                .averageRating(plugin.getAverageRating())
                .ratingCount(plugin.getRatingCount())
                .featured(plugin.isFeatured())
                .build());

        log.info("Plugin {} approval status: {}", pluginId, status);
        return true;
    }

    /**
     * Feature a plugin.
     */
    public boolean featurePlugin(String pluginId) {
        if (!plugins.containsKey(pluginId)) {
            return false;
        }
        featuredPlugins.add(pluginId);
        PluginListing plugin = plugins.get(pluginId);
        plugins.put(pluginId, PluginListing.builder(pluginId)
                .name(plugin.getName())
                .description(plugin.getDescription())
                .author(plugin.getAuthor())
                .version(plugin.getVersion())
                .category(plugin.getCategory())
                .tags(plugin.getTags())
                .approvalStatus(plugin.getApprovalStatus())
                .createdAt(plugin.getCreatedAt())
                .updatedAt(Instant.now())
                .downloadCount(plugin.getDownloadCount())
                .averageRating(plugin.getAverageRating())
                .ratingCount(plugin.getRatingCount())
                .featured(true)
                .build());
        log.info("Plugin {} featured", pluginId);
        return true;
    }

    /**
     * Unfeature a plugin.
     */
    public boolean unfeaturePlugin(String pluginId) {
        featuredPlugins.remove(pluginId);
        PluginListing plugin = plugins.get(pluginId);
        if (plugin == null) {
            return false;
        }
        plugins.put(pluginId, PluginListing.builder(pluginId)
                .name(plugin.getName())
                .description(plugin.getDescription())
                .author(plugin.getAuthor())
                .version(plugin.getVersion())
                .category(plugin.getCategory())
                .tags(plugin.getTags())
                .approvalStatus(plugin.getApprovalStatus())
                .createdAt(plugin.getCreatedAt())
                .updatedAt(plugin.getUpdatedAt())
                .downloadCount(plugin.getDownloadCount())
                .averageRating(plugin.getAverageRating())
                .ratingCount(plugin.getRatingCount())
                .featured(false)
                .build());
        return true;
    }

    /**
     * Add tags to a plugin.
     */
    public boolean addTags(String pluginId, List<String> tags) {
        PluginListing plugin = plugins.get(pluginId);
        if (plugin == null) {
            return false;
        }
        List<String> allTags = new ArrayList<>(plugin.getTags());
        for (String tag : tags) {
            if (allTags.size() < MAX_TAGS && !allTags.contains(tag)) {
                allTags.add(tag);
            }
        }
        plugins.put(pluginId, PluginListing.builder(pluginId)
                .name(plugin.getName())
                .description(plugin.getDescription())
                .author(plugin.getAuthor())
                .version(plugin.getVersion())
                .category(plugin.getCategory())
                .tags(allTags)
                .approvalStatus(plugin.getApprovalStatus())
                .createdAt(plugin.getCreatedAt())
                .updatedAt(Instant.now())
                .downloadCount(plugin.getDownloadCount())
                .averageRating(plugin.getAverageRating())
                .ratingCount(plugin.getRatingCount())
                .featured(plugin.isFeatured())
                .build());
        return true;
    }

    /**
     * Remove a plugin from the marketplace.
     */
    public boolean removePlugin(String pluginId) {
        plugins.remove(pluginId);
        ratings.remove(pluginId);
        downloadCounts.remove(pluginId);
        featuredPlugins.remove(pluginId);
        log.info("Plugin removed: {}", pluginId);
        return true;
    }

    /**
     * Get marketplace statistics.
     */
    public MarketplaceStats getStatistics() {
        int total = plugins.size();
        int approved = (int) plugins.values().stream()
                .filter(p -> p.getApprovalStatus() == ApprovalStatus.APPROVED)
                .count();
        int pending = (int) plugins.values().stream()
                .filter(p -> p.getApprovalStatus() == ApprovalStatus.PENDING)
                .count();
        int downloads = downloadCounts.values().stream().mapToInt(Integer::intValue).sum();

        double avgRating = plugins.values().stream()
                .filter(p -> p.getRatingCount() > 0)
                .mapToDouble(PluginListing::getAverageRating)
                .average().orElse(0.0);

        Map<Category, Integer> byCategory = new EnumMap<>(Category.class);
        for (Category c : Category.values()) {
            byCategory.put(c, 0);
        }
        plugins.values().forEach(p -> byCategory.merge(p.getCategory(), 1, Integer::sum));

        Map<String, Integer> byAuthor = new HashMap<>();
        plugins.values().forEach(p -> byAuthor.merge(p.getAuthor(), 1, Integer::sum));

        return MarketplaceStats.builder()
                .totalPlugins(total)
                .approvedPlugins(approved)
                .pendingPlugins(pending)
                .totalDownloads(downloads)
                .averageRating(avgRating)
                .pluginsByCategory(byCategory)
                .pluginsByAuthor(byAuthor)
                .build();
    }

    /**
     * Get all available categories.
     */
    public List<Category> getCategories() {
        return Arrays.asList(Category.values());
    }

    /**
     * Get all available approval statuses.
     */
    public List<ApprovalStatus> getApprovalStatuses() {
        return Arrays.asList(ApprovalStatus.values());
    }

    /**
     * Clear all marketplace data.
     */
    public void clearAll() {
        plugins.clear();
        ratings.clear();
        downloadCounts.clear();
        featuredPlugins.clear();
        pluginIdCounter.set(0);
        log.info("Marketplace cleared");
    }
}
