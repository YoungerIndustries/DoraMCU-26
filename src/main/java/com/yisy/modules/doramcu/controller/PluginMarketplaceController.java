package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.service.PluginMarketplaceService;
import com.yisy.modules.doramcu.service.PluginMarketplaceService.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for the Plugin Marketplace.
 * <p>
 * Provides endpoints for browsing, searching, rating, and managing plugins
 * in the community marketplace.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/marketplace")
public class PluginMarketplaceController {

    private final PluginMarketplaceService marketplaceService;

    public PluginMarketplaceController(PluginMarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    /**
     * Get all available categories.
     */
    @GetMapping("/categories")
    public List<PluginMarketplaceService.Category> getCategories() {
        return marketplaceService.getCategories();
    }

    /**
     * Get all available approval statuses.
     */
    @GetMapping("/approval-statuses")
    public List<PluginMarketplaceService.ApprovalStatus> getApprovalStatuses() {
        return marketplaceService.getApprovalStatuses();
    }

    /**
     * Register a new plugin.
     */
    @PostMapping("/plugins")
    public ResponseEntity<?> registerPlugin(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String description = (String) request.get("description");
        String author = (String) request.get("author");
        String categoryStr = (String) request.get("category");

        PluginMarketplaceService.Category category = null;
        if (categoryStr != null) {
            try {
                category = PluginMarketplaceService.Category.valueOf(categoryStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid category: " + categoryStr));
            }
        }

        Optional<PluginListing> plugin = marketplaceService.registerPlugin(name, description, author, category);
        if (plugin.isPresent()) {
            return ResponseEntity.ok(plugin.get());
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Failed to register plugin"));
    }

    /**
     * Get a plugin by ID.
     */
    @GetMapping("/plugins/{id}")
    public ResponseEntity<?> getPlugin(@PathVariable String id) {
        Optional<PluginListing> plugin = marketplaceService.getPlugin(id);
        if (plugin.isPresent()) {
            return ResponseEntity.ok(plugin.get());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * List plugins with pagination and filtering.
     */
    @GetMapping("/plugins")
    public PaginatedPlugins listPlugins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String approvalStatus) {

        PluginMarketplaceService.Category cat = null;
        if (category != null) {
            try {
                cat = PluginMarketplaceService.Category.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid category
            }
        }

        PluginMarketplaceService.ApprovalStatus status = null;
        if (approvalStatus != null) {
            try {
                status = PluginMarketplaceService.ApprovalStatus.valueOf(approvalStatus.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid status
            }
        }

        return marketplaceService.listPlugins(page, pageSize, cat, status);
    }

    /**
     * Search plugins by query.
     */
    @GetMapping("/search")
    public List<PluginListing> searchPlugins(@RequestParam String q) {
        return marketplaceService.searchPlugins(q);
    }

    /**
     * Get plugins by category.
     */
    @GetMapping("/category/{category}")
    public List<PluginListing> getPluginsByCategory(@PathVariable String category) {
        try {
            PluginMarketplaceService.Category cat = PluginMarketplaceService.Category.valueOf(category.toUpperCase());
            return marketplaceService.getPluginsByCategory(cat);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    /**
     * Get featured plugins.
     */
    @GetMapping("/featured")
    public List<PluginListing> getFeaturedPlugins() {
        return marketplaceService.getFeaturedPlugins();
    }

    /**
     * Get trending plugins.
     */
    @GetMapping("/trending")
    public List<PluginListing> getTrendingPlugins(@RequestParam(defaultValue = "10") int limit) {
        return marketplaceService.getTrendingPlugins(limit);
    }

    /**
     * Get approved plugins only.
     */
    @GetMapping("/approved")
    public List<PluginListing> getApprovedPlugins() {
        return marketplaceService.getApprovedPlugins();
    }

    /**
     * Rate a plugin.
     */
    @PostMapping("/plugins/{id}/rate")
    public ResponseEntity<?> ratePlugin(@PathVariable String id, @RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        Integer rating = (Integer) request.get("rating");
        String comment = (String) request.get("comment");

        if (userId == null || rating == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId and rating are required"));
        }

        Optional<PluginRating> ratingResult = marketplaceService.ratePlugin(id, userId, rating, comment);
        if (ratingResult.isPresent()) {
            return ResponseEntity.ok(ratingResult.get());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get ratings for a plugin.
     */
    @GetMapping("/plugins/{id}/ratings")
    public List<PluginRating> getRatings(@PathVariable String id) {
        return marketplaceService.getRatings(id);
    }

    /**
     * Record a download for a plugin.
     */
    @PostMapping("/plugins/{id}/download")
    public ResponseEntity<?> recordDownload(@PathVariable String id) {
        boolean recorded = marketplaceService.recordDownload(id);
        if (recorded) {
            return ResponseEntity.ok(Map.of("status", "download recorded"));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Approve a plugin.
     */
    @PostMapping("/plugins/{id}/approve")
    public ResponseEntity<?> approvePlugin(@PathVariable String id) {
        boolean approved = marketplaceService.approvePlugin(id);
        if (approved) {
            return ResponseEntity.ok(Map.of("status", "approved"));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Reject a plugin.
     */
    @PostMapping("/plugins/{id}/reject")
    public ResponseEntity<?> rejectPlugin(@PathVariable String id) {
        boolean rejected = marketplaceService.rejectPlugin(id);
        if (rejected) {
            return ResponseEntity.ok(Map.of("status", "rejected"));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Flag a plugin for review.
     */
    @PostMapping("/plugins/{id}/flag")
    public ResponseEntity<?> flagPlugin(@PathVariable String id) {
        boolean flagged = marketplaceService.flagPlugin(id);
        if (flagged) {
            return ResponseEntity.ok(Map.of("status", "flagged"));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Feature a plugin.
     */
    @PostMapping("/plugins/{id}/feature")
    public ResponseEntity<?> featurePlugin(@PathVariable String id) {
        boolean featured = marketplaceService.featurePlugin(id);
        if (featured) {
            return ResponseEntity.ok(Map.of("status", "featured"));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Unfeature a plugin.
     */
    @PostMapping("/plugins/{id}/unfeature")
    public ResponseEntity<?> unfeaturePlugin(@PathVariable String id) {
        boolean unfeatured = marketplaceService.unfeaturePlugin(id);
        if (unfeatured) {
            return ResponseEntity.ok(Map.of("status", "unfeatured"));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Add tags to a plugin.
     */
    @PostMapping("/plugins/{id}/tags")
    public ResponseEntity<?> addTags(@PathVariable String id, @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) request.get("tags");
        if (tags == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "tags are required"));
        }
        boolean added = marketplaceService.addTags(id, tags);
        if (added) {
            return ResponseEntity.ok(Map.of("status", "tags added"));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Remove a plugin.
     */
    @DeleteMapping("/plugins/{id}")
    public ResponseEntity<?> removePlugin(@PathVariable String id) {
        marketplaceService.removePlugin(id);
        return ResponseEntity.ok(Map.of("status", "removed"));
    }

    /**
     * Get marketplace statistics.
     */
    @GetMapping("/statistics")
    public MarketplaceStats getStatistics() {
        return marketplaceService.getStatistics();
    }
}
