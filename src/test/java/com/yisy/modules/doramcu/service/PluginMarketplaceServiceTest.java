package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.service.PluginMarketplaceService.*;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PluginMarketplaceServiceTest {

    private PluginMarketplaceService service;

    @BeforeEach
    void setUp() {
        service = new PluginMarketplaceService();
    }

    @AfterEach
    void tearDown() {
        service.clearAll();
    }

    @Nested
    class RegisterPlugin {
        @Test
        void registersPluginSuccessfully() {
            Optional<PluginListing> result = service.registerPlugin(
                    "Test Plugin", "A test plugin", "Author1", Category.GAME_MODE);

            assertTrue(result.isPresent());
            assertEquals("Test Plugin", result.get().getName());
            assertEquals("Author1", result.get().getAuthor());
            assertEquals(Category.GAME_MODE, result.get().getCategory());
            assertEquals(ApprovalStatus.PENDING, result.get().getApprovalStatus());
        }

        @Test
        void rejectsBlankName() {
            Optional<PluginListing> result = service.registerPlugin("", "Desc", "Author", Category.UTILITY);
            assertFalse(result.isPresent());
        }

        @Test
        void rejectsNullName() {
            Optional<PluginListing> result = service.registerPlugin(null, "Desc", "Author", Category.UTILITY);
            assertFalse(result.isPresent());
        }

        @Test
        void rejectsBlankAuthor() {
            Optional<PluginListing> result = service.registerPlugin("Name", "Desc", "", Category.UTILITY);
            assertFalse(result.isPresent());
        }

        @Test
        void rejectsNullAuthor() {
            Optional<PluginListing> result = service.registerPlugin("Name", "Desc", null, Category.UTILITY);
            assertFalse(result.isPresent());
        }

        @Test
        void rejectsDescriptionTooLong() {
            String longDesc = "x".repeat(2001);
            Optional<PluginListing> result = service.registerPlugin("Name", longDesc, "Author", Category.UTILITY);
            assertFalse(result.isPresent());
        }

        @Test
        void defaultsToUtilityCategory() {
            Optional<PluginListing> result = service.registerPlugin("Name", "Desc", "Author", null);
            assertTrue(result.isPresent());
            assertEquals(Category.UTILITY, result.get().getCategory());
        }

        @Test
        void assignsUniqueIds() {
            Optional<PluginListing> p1 = service.registerPlugin("Plugin1", "Desc", "Author", Category.UTILITY);
            Optional<PluginListing> p2 = service.registerPlugin("Plugin2", "Desc", "Author", Category.UTILITY);
            assertTrue(p1.isPresent());
            assertTrue(p2.isPresent());
            assertNotEquals(p1.get().getId(), p2.get().getId());
        }

        @Test
        void emptyDescriptionIsAllowed() {
            Optional<PluginListing> result = service.registerPlugin("Name", null, "Author", Category.UTILITY);
            assertTrue(result.isPresent());
            assertEquals("", result.get().getDescription());
        }
    }

    @Nested
    class GetPlugin {
        @Test
        void returnsPluginById() {
            Optional<PluginListing> created = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            String id = created.get().getId();

            Optional<PluginListing> found = service.getPlugin(id);
            assertTrue(found.isPresent());
            assertEquals("P1", found.get().getName());
        }

        @Test
        void returnsEmptyForUnknownId() {
            Optional<PluginListing> found = service.getPlugin("nonexistent");
            assertFalse(found.isPresent());
        }
    }

    @Nested
    class ListPlugins {
        @Test
        void returnsEmptyListWhenNoPlugins() {
            PaginatedPlugins result = service.listPlugins(0, 20, null, null);
            assertTrue(result.getPlugins().isEmpty());
            assertEquals(0, result.getTotalPlugins());
            assertEquals(0, result.getTotalPages());
        }

        @Test
        void paginatesCorrectly() {
            for (int i = 0; i < 25; i++) {
                service.registerPlugin("Plugin" + i, "Desc", "Author", Category.UTILITY);
            }

            PaginatedPlugins page1 = service.listPlugins(0, 10, null, null);
            assertEquals(10, page1.getPlugins().size());
            assertEquals(25, page1.getTotalPlugins());
            assertEquals(3, page1.getTotalPages());

            PaginatedPlugins page2 = service.listPlugins(1, 10, null, null);
            assertEquals(10, page2.getPlugins().size());

            PaginatedPlugins page3 = service.listPlugins(2, 10, null, null);
            assertEquals(5, page3.getPlugins().size());
        }

        @Test
        void filtersByCategory() {
            service.registerPlugin("Game1", "Desc", "Author", Category.GAME_MODE);
            service.registerPlugin("Theme1", "Desc", "Author", Category.UI_THEME);
            service.registerPlugin("Game2", "Desc", "Author", Category.GAME_MODE);

            PaginatedPlugins result = service.listPlugins(0, 20, Category.GAME_MODE, null);
            assertEquals(2, result.getPlugins().size());
        }

        @Test
        void filtersByApprovalStatus() {
            Optional<PluginListing> p1 = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            service.approvePlugin(p1.get().getId());

            Optional<PluginListing> p2 = service.registerPlugin("P2", "Desc", "Author", Category.UTILITY);

            PaginatedPlugins approved = service.listPlugins(0, 20, null, ApprovalStatus.APPROVED);
            assertEquals(1, approved.getPlugins().size());

            PaginatedPlugins pending = service.listPlugins(0, 20, null, ApprovalStatus.PENDING);
            assertEquals(1, pending.getPlugins().size());
        }

        @Test
        void sortsByCreatedAtDescending() {
            service.registerPlugin("Old", "Desc", "Author", Category.UTILITY);
            service.registerPlugin("New", "Desc", "Author", Category.UTILITY);

            PaginatedPlugins result = service.listPlugins(0, 20, null, null);
            assertEquals("New", result.getPlugins().get(0).getName());
        }
    }

    @Nested
    class SearchPlugins {
        @Test
        void searchesByName() {
            service.registerPlugin("Space Explorer", "Desc", "Author", Category.GAME_MODE);
            service.registerPlugin("Trade Helper", "Desc", "Author", Category.UTILITY);

            List<PluginListing> results = service.searchPlugins("Space");
            assertEquals(1, results.size());
            assertEquals("Space Explorer", results.get(0).getName());
        }

        @Test
        void searchesByDescription() {
            service.registerPlugin("P1", "Galaxy builder tool", "Author", Category.UTILITY);
            service.registerPlugin("P2", "Combat simulator", "Author", Category.GAME_MODE);

            List<PluginListing> results = service.searchPlugins("Galaxy");
            assertEquals(1, results.size());
        }

        @Test
        void searchesByAuthor() {
            service.registerPlugin("P1", "Desc", "JohnDoe", Category.UTILITY);
            service.registerPlugin("P2", "Desc", "JaneDoe", Category.UTILITY);

            List<PluginListing> results = service.searchPlugins("John");
            assertEquals(1, results.size());
        }

        @Test
        void searchesByTag() {
            Optional<PluginListing> p1 = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            service.addTags(p1.get().getId(), List.of("combat", "sfb"));

            List<PluginListing> results = service.searchPlugins("combat");
            assertEquals(1, results.size());
        }

        @Test
        void returnsAllForEmptyQuery() {
            service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            service.registerPlugin("P2", "Desc", "Author", Category.UTILITY);

            List<PluginListing> results = service.searchPlugins("");
            assertEquals(2, results.size());
        }

        @Test
        void returnsAllForNullQuery() {
            service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);

            List<PluginListing> results = service.searchPlugins(null);
            assertEquals(1, results.size());
        }

        @Test
        void caseInsensitiveSearch() {
            service.registerPlugin("Space Explorer", "Desc", "Author", Category.GAME_MODE);

            List<PluginListing> results = service.searchPlugins("space");
            assertEquals(1, results.size());
        }
    }

    @Nested
    class CategoryFiltering {
        @Test
        void getPluginsByCategory() {
            service.registerPlugin("G1", "Desc", "Author", Category.GAME_MODE);
            service.registerPlugin("T1", "Desc", "Author", Category.UI_THEME);
            service.registerPlugin("G2", "Desc", "Author", Category.GAME_MODE);

            List<PluginListing> gameModes = service.getPluginsByCategory(Category.GAME_MODE);
            assertEquals(2, gameModes.size());
        }

        @Test
        void getCategories() {
            List<Category> categories = service.getCategories();
            assertEquals(Category.values().length, categories.size());
        }

        @Test
        void getApprovalStatuses() {
            List<ApprovalStatus> statuses = service.getApprovalStatuses();
            assertEquals(ApprovalStatus.values().length, statuses.size());
        }
    }

    @Nested
    class RatingSystem {
        @Test
        void ratePluginSuccessfully() {
            Optional<PluginListing> p = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            String id = p.get().getId();

            Optional<PluginRating> rating = service.ratePlugin(id, "user1", 5, "Great!");
            assertTrue(rating.isPresent());
            assertEquals(5, rating.get().getRating());
            assertEquals("Great!", rating.get().getComment());
        }

        @Test
        void updatesAverageRating() {
            Optional<PluginListing> p = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            String id = p.get().getId();

            service.ratePlugin(id, "user1", 4, "Good");
            service.ratePlugin(id, "user2", 5, "Excellent");

            Optional<PluginListing> updated = service.getPlugin(id);
            assertEquals(4.5, updated.get().getAverageRating(), 0.01);
            assertEquals(2, updated.get().getRatingCount());
        }

        @Test
        void userCanUpdateRating() {
            Optional<PluginListing> p = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            String id = p.get().getId();

            service.ratePlugin(id, "user1", 3, "OK");
            service.ratePlugin(id, "user1", 5, "Changed mind");

            Optional<PluginListing> updated = service.getPlugin(id);
            assertEquals(5.0, updated.get().getAverageRating(), 0.01);
            assertEquals(1, updated.get().getRatingCount());
        }

        @Test
        void rejectsInvalidRating() {
            Optional<PluginListing> p = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            Optional<PluginRating> result = service.ratePlugin(p.get().getId(), "user1", 6, "Too high");
            assertFalse(result.isPresent());
        }

        @Test
        void rejectsNegativeRating() {
            Optional<PluginListing> p = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            Optional<PluginRating> result = service.ratePlugin(p.get().getId(), "user1", 0, "Too low");
            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyForUnknownPlugin() {
            Optional<PluginRating> result = service.ratePlugin("nonexistent", "user1", 5, "Comment");
            assertFalse(result.isPresent());
        }

        @Test
        void getRatings() {
            Optional<PluginListing> p = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            String id = p.get().getId();

            service.ratePlugin(id, "user1", 4, "Good");
            service.ratePlugin(id, "user2", 5, "Great");

            List<PluginRating> ratings = service.getRatings(id);
            assertEquals(2, ratings.size());
        }

        @Test
        void getRatingsEmptyForUnknownPlugin() {
            List<PluginRating> ratings = service.getRatings("nonexistent");
            assertTrue(ratings.isEmpty());
        }
    }

    @Nested
    class DownloadTracking {
        @Test
        void recordDownload() {
            Optional<PluginListing> p = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            String id = p.get().getId();

            boolean recorded = service.recordDownload(id);
            assertTrue(recorded);

            Optional<PluginListing> updated = service.getPlugin(id);
            assertEquals(1, updated.get().getDownloadCount());
        }

        @Test
        void incrementsDownloadCount() {
            Optional<PluginListing> p = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            String id = p.get().getId();

            service.recordDownload(id);
            service.recordDownload(id);
            service.recordDownload(id);

            Optional<PluginListing> updated = service.getPlugin(id);
            assertEquals(3, updated.get().getDownloadCount());
        }

        @Test
        void returnsFalseForUnknownPlugin() {
            boolean recorded = service.recordDownload("nonexistent");
            assertFalse(recorded);
        }
    }

    @Nested
    class ApprovalWorkflow {
        @Test
        void approvePlugin() {
            Optional<PluginListing> p = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            boolean approved = service.approvePlugin(p.get().getId());
            assertTrue(approved);

            Optional<PluginListing> updated = service.getPlugin(p.get().getId());
            assertEquals(ApprovalStatus.APPROVED, updated.get().getApprovalStatus());
        }

        @Test
        void rejectPlugin() {
            Optional<PluginListing> p = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            boolean rejected = service.rejectPlugin(p.get().getId());
            assertTrue(rejected);

            Optional<PluginListing> updated = service.getPlugin(p.get().getId());
            assertEquals(ApprovalStatus.REJECTED, updated.get().getApprovalStatus());
        }

        @Test
        void flagPlugin() {
            Optional<PluginListing> p = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            boolean flagged = service.flagPlugin(p.get().getId());
            assertTrue(flagged);

            Optional<PluginListing> updated = service.getPlugin(p.get().getId());
            assertEquals(ApprovalStatus.FLAGGED, updated.get().getApprovalStatus());
        }

        @Test
        void returnsFalseForUnknownPlugin() {
            assertFalse(service.approvePlugin("nonexistent"));
            assertFalse(service.rejectPlugin("nonexistent"));
            assertFalse(service.flagPlugin("nonexistent"));
        }

        @Test
        void getApprovedPlugins() {
            Optional<PluginListing> p1 = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            service.approvePlugin(p1.get().getId());

            Optional<PluginListing> p2 = service.registerPlugin("P2", "Desc", "Author", Category.UTILITY);

            List<PluginListing> approved = service.getApprovedPlugins();
            assertEquals(1, approved.size());
            assertEquals(p1.get().getId(), approved.get(0).getId());
        }
    }

    @Nested
    class FeaturedPlugins {
        @Test
        void featurePlugin() {
            Optional<PluginListing> p = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            boolean featured = service.featurePlugin(p.get().getId());
            assertTrue(featured);

            Optional<PluginListing> updated = service.getPlugin(p.get().getId());
            assertTrue(updated.get().isFeatured());
        }

        @Test
        void unfeaturePlugin() {
            Optional<PluginListing> p = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            service.featurePlugin(p.get().getId());
            boolean unfeatured = service.unfeaturePlugin(p.get().getId());
            assertTrue(unfeatured);

            Optional<PluginListing> updated = service.getPlugin(p.get().getId());
            assertFalse(updated.get().isFeatured());
        }

        @Test
        void getFeaturedPlugins() {
            Optional<PluginListing> p1 = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            Optional<PluginListing> p2 = service.registerPlugin("P2", "Desc", "Author", Category.UTILITY);
            service.featurePlugin(p1.get().getId());

            List<PluginListing> featured = service.getFeaturedPlugins();
            assertEquals(1, featured.size());
            assertEquals(p1.get().getId(), featured.get(0).getId());
        }

        @Test
        void featureUnknownPluginReturnsFalse() {
            assertFalse(service.featurePlugin("nonexistent"));
        }
    }

    @Nested
    class TrendingPlugins {
        @Test
        void returnsMostDownloadedFirst() {
            Optional<PluginListing> p1 = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            Optional<PluginListing> p2 = service.registerPlugin("P2", "Desc", "Author", Category.UTILITY);
            Optional<PluginListing> p3 = service.registerPlugin("P3", "Desc", "Author", Category.UTILITY);

            service.recordDownload(p1.get().getId());
            service.recordDownload(p1.get().getId());
            service.recordDownload(p1.get().getId());
            service.recordDownload(p2.get().getId());
            service.recordDownload(p2.get().getId());

            List<PluginListing> trending = service.getTrendingPlugins(2);
            assertEquals(2, trending.size());
            assertEquals(p1.get().getId(), trending.get(0).getId());
            assertEquals(p2.get().getId(), trending.get(1).getId());
        }

        @Test
        void limitsResults() {
            for (int i = 0; i < 10; i++) {
                service.registerPlugin("P" + i, "Desc", "Author", Category.UTILITY);
            }

            List<PluginListing> trending = service.getTrendingPlugins(3);
            assertEquals(3, trending.size());
        }
    }

    @Nested
    class Tags {
        @Test
        void addTags() {
            Optional<PluginListing> p = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            boolean added = service.addTags(p.get().getId(), List.of("combat", "sfb", "tactical"));
            assertTrue(added);

            Optional<PluginListing> updated = service.getPlugin(p.get().getId());
            assertEquals(3, updated.get().getTags().size());
            assertTrue(updated.get().getTags().contains("combat"));
        }

        @Test
        void limitsTagsToMax() {
            Optional<PluginListing> p = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            List<String> manyTags = List.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l");
            service.addTags(p.get().getId(), manyTags);

            Optional<PluginListing> updated = service.getPlugin(p.get().getId());
            assertEquals(10, updated.get().getTags().size());
        }

        @Test
        void doesNotDuplicateTags() {
            Optional<PluginListing> p = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            service.addTags(p.get().getId(), List.of("combat"));
            service.addTags(p.get().getId(), List.of("combat", "sfb"));

            Optional<PluginListing> updated = service.getPlugin(p.get().getId());
            assertEquals(2, updated.get().getTags().size());
        }

        @Test
        void returnsFalseForUnknownPlugin() {
            assertFalse(service.addTags("nonexistent", List.of("tag")));
        }
    }

    @Nested
    class RemovePlugin {
        @Test
        void removesPlugin() {
            Optional<PluginListing> p = service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            String id = p.get().getId();

            service.removePlugin(id);
            Optional<PluginListing> found = service.getPlugin(id);
            assertFalse(found.isPresent());
        }
    }

    @Nested
    class Statistics {
        @Test
        void emptyStatistics() {
            MarketplaceStats stats = service.getStatistics();
            assertEquals(0, stats.getTotalPlugins());
            assertEquals(0, stats.getApprovedPlugins());
            assertEquals(0, stats.getPendingPlugins());
            assertEquals(0, stats.getTotalDownloads());
            assertEquals(0.0, stats.getAverageRating(), 0.01);
        }

        @Test
        void calculatesStatistics() {
            Optional<PluginListing> p1 = service.registerPlugin("P1", "Desc", "Author1", Category.GAME_MODE);
            Optional<PluginListing> p2 = service.registerPlugin("P2", "Desc", "Author1", Category.UI_THEME);
            Optional<PluginListing> p3 = service.registerPlugin("P3", "Desc", "Author2", Category.GAME_MODE);

            service.approvePlugin(p1.get().getId());
            service.approvePlugin(p2.get().getId());

            service.recordDownload(p1.get().getId());
            service.recordDownload(p1.get().getId());

            service.ratePlugin(p1.get().getId(), "user1", 5, "Great");

            MarketplaceStats stats = service.getStatistics();
            assertEquals(3, stats.getTotalPlugins());
            assertEquals(2, stats.getApprovedPlugins());
            assertEquals(1, stats.getPendingPlugins());
            assertEquals(2, stats.getTotalDownloads());
            assertEquals(5.0, stats.getAverageRating(), 0.01);
            assertEquals(2, stats.getPluginsByCategory().get(Category.GAME_MODE));
            assertEquals(1, stats.getPluginsByCategory().get(Category.UI_THEME));
            assertEquals(2, stats.getPluginsByAuthor().get("Author1"));
            assertEquals(1, stats.getPluginsByAuthor().get("Author2"));
        }
    }

    @Nested
    class BuilderTests {
        @Test
        void pluginListingBuilder() {
            PluginListing plugin = PluginListing.builder("test-id")
                    .name("Test")
                    .description("A test")
                    .author("Author")
                    .version("2.0.0")
                    .category(Category.GAME_MODE)
                    .tags(List.of("test", "demo"))
                    .approvalStatus(ApprovalStatus.APPROVED)
                    .downloadCount(100)
                    .averageRating(4.5)
                    .ratingCount(10)
                    .featured(true)
                    .metadata("key", "value")
                    .build();

            assertEquals("test-id", plugin.getId());
            assertEquals("Test", plugin.getName());
            assertEquals("A test", plugin.getDescription());
            assertEquals("Author", plugin.getAuthor());
            assertEquals("2.0.0", plugin.getVersion());
            assertEquals(Category.GAME_MODE, plugin.getCategory());
            assertEquals(2, plugin.getTags().size());
            assertEquals(ApprovalStatus.APPROVED, plugin.getApprovalStatus());
            assertEquals(100, plugin.getDownloadCount());
            assertEquals(4.5, plugin.getAverageRating(), 0.01);
            assertEquals(10, plugin.getRatingCount());
            assertTrue(plugin.isFeatured());
            assertEquals("value", plugin.getMetadata().get("key"));
        }

        @Test
        void pluginRatingBuilder() {
            PluginRating rating = PluginRating.builder("plugin-1", "user-1")
                    .rating(5)
                    .comment("Excellent!")
                    .build();

            assertEquals("plugin-1", rating.getPluginId());
            assertEquals("user-1", rating.getUserId());
            assertEquals(5, rating.getRating());
            assertEquals("Excellent!", rating.getComment());
        }

        @Test
        void pluginRatingBuilderThrowsForInvalidRating() {
            assertThrows(IllegalArgumentException.class, () ->
                    PluginRating.builder("p", "u").rating(6).build());
        }

        @Test
        void marketplaceStatsBuilder() {
            MarketplaceStats stats = MarketplaceStats.builder()
                    .totalPlugins(10)
                    .approvedPlugins(8)
                    .pendingPlugins(2)
                    .totalDownloads(100)
                    .averageRating(4.5)
                    .build();

            assertEquals(10, stats.getTotalPlugins());
            assertEquals(8, stats.getApprovedPlugins());
            assertEquals(2, stats.getPendingPlugins());
            assertEquals(100, stats.getTotalDownloads());
            assertEquals(4.5, stats.getAverageRating(), 0.01);
        }

        @Test
        void paginatedPlugins() {
            PaginatedPlugins paginated = new PaginatedPlugins(List.of(), 0, 20, 0);
            assertTrue(paginated.getPlugins().isEmpty());
            assertEquals(0, paginated.getPage());
            assertEquals(20, paginated.getPageSize());
            assertEquals(0, paginated.getTotalPages());
            assertEquals(0, paginated.getTotalPlugins());
        }

        @Test
        void paginatedPluginsCalculatesTotalPages() {
            PaginatedPlugins paginated = new PaginatedPlugins(List.of(), 0, 10, 25);
            assertEquals(3, paginated.getTotalPages());
        }
    }

    @Nested
    class IntegrationScenarios {
        @Test
        void fullPluginLifecycle() {
            // Register
            Optional<PluginListing> p = service.registerPlugin("Space Explorer", "Explore the galaxy", "Dev1", Category.GAME_MODE);
            assertTrue(p.isPresent());
            assertEquals(ApprovalStatus.PENDING, p.get().getApprovalStatus());

            // Add tags
            service.addTags(p.get().getId(), List.of("exploration", "galaxy"));

            // Approve
            service.approvePlugin(p.get().getId());
            assertEquals(ApprovalStatus.APPROVED, service.getPlugin(p.get().getId()).get().getApprovalStatus());

            // Feature
            service.featurePlugin(p.get().getId());
            assertTrue(service.getPlugin(p.get().getId()).get().isFeatured());

            // Rate
            service.ratePlugin(p.get().getId(), "user1", 5, "Amazing!");
            service.ratePlugin(p.get().getId(), "user2", 4, "Great game!");

            // Download
            service.recordDownload(p.get().getId());
            service.recordDownload(p.get().getId());

            // Verify final state
            Optional<PluginListing> finalState = service.getPlugin(p.get().getId());
            assertEquals(4.5, finalState.get().getAverageRating(), 0.01);
            assertEquals(2, finalState.get().getRatingCount());
            assertEquals(2, finalState.get().getDownloadCount());
            assertTrue(finalState.get().isFeatured());
            assertEquals(2, finalState.get().getTags().size());
        }

        @Test
        void marketplaceWithMultiplePlugins() {
            // Register multiple plugins
            for (int i = 0; i < 5; i++) {
                service.registerPlugin("Plugin" + i, "Description " + i, "Author" + (i % 2),
                        i % 2 == 0 ? Category.GAME_MODE : Category.UI_THEME);
            }

            // Approve some
            List<PluginListing> all = service.searchPlugins("");
            for (int i = 0; i < 3; i++) {
                service.approvePlugin(all.get(i).getId());
            }

            // Statistics
            MarketplaceStats stats = service.getStatistics();
            assertEquals(5, stats.getTotalPlugins());
            assertEquals(3, stats.getApprovedPlugins());
            assertEquals(2, stats.getPendingPlugins());

            // Approved only
            List<PluginListing> approved = service.getApprovedPlugins();
            assertEquals(3, approved.size());
        }

        @Test
        void clearAllResetsMarketplace() {
            service.registerPlugin("P1", "Desc", "Author", Category.UTILITY);
            service.registerPlugin("P2", "Desc", "Author", Category.UTILITY);

            service.clearAll();

            MarketplaceStats stats = service.getStatistics();
            assertEquals(0, stats.getTotalPlugins());
            assertEquals(0, stats.getTotalDownloads());
        }
    }
}
