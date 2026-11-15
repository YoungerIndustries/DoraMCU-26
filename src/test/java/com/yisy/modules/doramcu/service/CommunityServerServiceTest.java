package com.yisy.modules.doramcu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CommunityServerServiceTest {

    private CommunityServerService service;

    @BeforeEach
    void setUp() {
        service = new CommunityServerService();
        service.clearAll();
    }

    @Nested
    class Enums {
        @Test
        void getAllServerStatuses() {
            List<Map<String, String>> statuses = service.getAllServerStatuses();
            assertEquals(4, statuses.size());
        }

        @Test
        void getAllGameModes() {
            List<Map<String, String>> modes = service.getAllGameModes();
            assertEquals(11, modes.size());
        }

        @Test
        void getAllContentTypes() {
            List<Map<String, String>> types = service.getAllContentTypes();
            assertEquals(6, types.size());
        }

        @Test
        void enumHasNameAndLabel() {
            service.getAllServerStatuses().forEach(s -> {
                assertNotNull(s.get("name"));
                assertNotNull(s.get("label"));
                assertNotNull(s.get("description"));
            });
        }
    }

    @Nested
    class ServerRegistration {
        @Test
        void registerServer() {
            CommunityServerService.ServerInfo server = service.registerServer(
                    "srv1", "Test Server", "A test server", "localhost", 8080,
                    CommunityServerService.GameMode.TRAVELLER, 10, "owner1");
            assertNotNull(server);
            assertEquals("srv1", server.getId());
            assertEquals("Test Server", server.getName());
            assertEquals(CommunityServerService.GameMode.TRAVELLER, server.getGameMode());
        }

        @Test
        void registerServerBlankId() {
            CommunityServerService.ServerInfo server = service.registerServer(
                    "", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            assertNull(server);
        }

        @Test
        void registerServerBlankName() {
            CommunityServerService.ServerInfo server = service.registerServer(
                    "srv1", "", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            assertNull(server);
        }

        @Test
        void registerServerDuplicateId() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            CommunityServerService.ServerInfo server = service.registerServer(
                    "srv1", "Test2", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            assertNull(server);
        }

        @Test
        void registerServerInvalidMaxPlayers() {
            CommunityServerService.ServerInfo server = service.registerServer(
                    "srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 0, "owner1");
            assertNull(server);
        }

        @Test
        void registerServerMaxPlayersTooHigh() {
            CommunityServerService.ServerInfo server = service.registerServer(
                    "srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 101, "owner1");
            assertNull(server);
        }

        @Test
        void getServer() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            CommunityServerService.ServerInfo server = service.getServer("srv1");
            assertNotNull(server);
            assertEquals("srv1", server.getId());
        }

        @Test
        void getServerNotFound() {
            assertNull(service.getServer("nonexistent"));
        }

        @Test
        void listServers() {
            service.registerServer("srv1", "Test1", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            service.registerServer("srv2", "Test2", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            assertEquals(2, service.listServers().size());
        }

        @Test
        void listServersByGameMode() {
            service.registerServer("srv1", "Test1", "", "localhost", 8080,
                    CommunityServerService.GameMode.TRAVELLER, 10, "owner1");
            service.registerServer("srv2", "Test2", "", "localhost", 8080,
                    CommunityServerService.GameMode.SFB, 10, "owner1");
            assertEquals(1, service.listServersByGameMode(CommunityServerService.GameMode.TRAVELLER).size());
            assertEquals(1, service.listServersByGameMode(CommunityServerService.GameMode.SFB).size());
        }

        @Test
        void searchServersByTag() {
            service.registerServer("srv1", "Test1", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            service.addTags("srv1", Set.of("pvp", "competitive"));
            assertEquals(1, service.searchServersByTag("pvp").size());
            assertEquals(0, service.searchServersByTag("casual").size());
        }

        @Test
        void listServersWithSlots() {
            service.registerServer("srv1", "Test1", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            service.registerServer("srv2", "Test2", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            service.updatePlayerCount("srv2", 10);
            assertEquals(1, service.listServersWithSlots().size());
        }

        @Test
        void serverInfoConnectionUrl() {
            service.registerServer("srv1", "Test", "", "example.com", 9090,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            CommunityServerService.ServerInfo server = service.getServer("srv1");
            assertEquals("ws://example.com:9090/ws/v1/4x/session", server.getConnectionUrl());
        }

        @Test
        void serverInfoToMap() {
            service.registerServer("srv1", "Test", "Desc", "localhost", 8080,
                    CommunityServerService.GameMode.TRAVELLER, 10, "owner1");
            CommunityServerService.ServerInfo server = service.getServer("srv1");
            Map<String, Object> map = server.toMap();
            assertEquals("srv1", map.get("id"));
            assertEquals("Test", map.get("name"));
            assertEquals("TRAVELLER", map.get("gameMode"));
        }
    }

    @Nested
    class ServerLifecycle {
        @Test
        void activateServer() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            CommunityServerService.ServerInfo server = service.activateServer("srv1");
            assertNotNull(server);
            assertEquals(CommunityServerService.ServerStatus.ACTIVE, server.getStatus());
        }

        @Test
        void activateServerNotFound() {
            assertNull(service.activateServer("nonexistent"));
        }

        @Test
        void activateActiveServer() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            service.activateServer("srv1");
            assertNull(service.activateServer("srv1"));
        }

        @Test
        void pauseServer() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            service.activateServer("srv1");
            CommunityServerService.ServerInfo server = service.pauseServer("srv1");
            assertNotNull(server);
            assertEquals(CommunityServerService.ServerStatus.PAUSED, server.getStatus());
        }

        @Test
        void pauseNonActiveServer() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            assertNull(service.pauseServer("srv1"));
        }

        @Test
        void setOffline() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            service.activateServer("srv1");
            CommunityServerService.ServerInfo server = service.setOffline("srv1");
            assertNotNull(server);
            assertEquals(CommunityServerService.ServerStatus.OFFLINE, server.getStatus());
            assertEquals(0, server.getCurrentPlayers());
        }

        @Test
        void heartbeat() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            assertTrue(service.heartbeat("srv1"));
        }

        @Test
        void heartbeatNotFound() {
            assertFalse(service.heartbeat("nonexistent"));
        }

        @Test
        void updatePlayerCount() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            CommunityServerService.ServerInfo server = service.updatePlayerCount("srv1", 5);
            assertNotNull(server);
            assertEquals(5, server.getCurrentPlayers());
        }

        @Test
        void updatePlayerCountExceedsMax() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            assertNull(service.updatePlayerCount("srv1", 11));
        }

        @Test
        void updatePlayerCountNegative() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            assertNull(service.updatePlayerCount("srv1", -1));
        }

        @Test
        void listActiveServers() {
            service.registerServer("srv1", "Test1", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            service.registerServer("srv2", "Test2", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            service.activateServer("srv1");
            assertEquals(1, service.listActiveServers().size());
        }

        @Test
        void resumeFromPaused() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            service.activateServer("srv1");
            service.pauseServer("srv1");
            CommunityServerService.ServerInfo server = service.activateServer("srv1");
            assertNotNull(server);
            assertEquals(CommunityServerService.ServerStatus.ACTIVE, server.getStatus());
        }
    }

    @Nested
    class Tags {
        @Test
        void addTags() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            CommunityServerService.ServerInfo server = service.addTags("srv1", Set.of("pvp", "competitive"));
            assertNotNull(server);
            assertTrue(server.getTags().contains("pvp"));
            assertTrue(server.getTags().contains("competitive"));
        }

        @Test
        void addTagsNotFound() {
            assertNull(service.addTags("nonexistent", Set.of("tag")));
        }
    }

    @Nested
    class ContentSharing {
        @Test
        void shareContent() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            CommunityServerService.ContentItem item = service.shareContent(
                    "srv1", "My Mod", CommunityServerService.ContentType.MOD,
                    "A cool mod", "author1", Map.of("version", "1.0"));
            assertNotNull(item);
            assertEquals("My Mod", item.getName());
            assertEquals(CommunityServerService.ContentType.MOD, item.getType());
        }

        @Test
        void shareContentServerNotFound() {
            assertNull(service.shareContent("nonexistent", "Mod", CommunityServerService.ContentType.MOD,
                    "", "author", Map.of()));
        }

        @Test
        void getContent() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            service.shareContent("srv1", "Mod1", CommunityServerService.ContentType.MOD, "", "author1", Map.of());
            service.shareContent("srv1", "Mod2", CommunityServerService.ContentType.SCENARIO, "", "author1", Map.of());
            assertEquals(2, service.getContent("srv1").size());
        }

        @Test
        void getContentByType() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            service.shareContent("srv1", "Mod1", CommunityServerService.ContentType.MOD, "", "author1", Map.of());
            service.shareContent("srv1", "Scenario1", CommunityServerService.ContentType.SCENARIO, "", "author1", Map.of());
            assertEquals(1, service.getContentByType(CommunityServerService.ContentType.MOD).size());
            assertEquals(1, service.getContentByType(CommunityServerService.ContentType.SCENARIO).size());
        }

        @Test
        void searchContent() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            service.shareContent("srv1", "Space Mod", CommunityServerService.ContentType.MOD, "", "author1", Map.of());
            service.shareContent("srv1", "Land Mod", CommunityServerService.ContentType.MOD, "", "author1", Map.of());
            assertEquals(1, service.searchContent("space").size());
            assertEquals(2, service.searchContent("mod").size());
        }

        @Test
        void downloadContent() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            CommunityServerService.ContentItem item = service.shareContent(
                    "srv1", "Mod", CommunityServerService.ContentType.MOD, "", "author1", Map.of());
            CommunityServerService.ContentItem downloaded = service.downloadContent("srv1", item.getId());
            assertNotNull(downloaded);
            assertEquals(1, downloaded.getDownloadCount());
        }

        @Test
        void downloadContentNotFound() {
            assertNull(service.downloadContent("srv1", "nonexistent"));
        }

        @Test
        void contentItemToMap() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            CommunityServerService.ContentItem item = service.shareContent(
                    "srv1", "Mod", CommunityServerService.ContentType.MOD, "Desc", "author1", Map.of("key", "value"));
            Map<String, Object> map = item.toMap();
            assertEquals("Mod", map.get("name"));
            assertEquals("MOD", map.get("type"));
        }
    }

    @Nested
    class Reviews {
        @Test
        void addReview() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            CommunityServerService.Review review = service.addReview("srv1", "reviewer1", 5, "Great server!");
            assertNotNull(review);
            assertEquals(5, review.getRating());
            assertEquals("Great server!", review.getComment());
        }

        @Test
        void addReviewInvalidRating() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            assertNull(service.addReview("srv1", "reviewer1", 0, "Bad"));
            assertNull(service.addReview("srv1", "reviewer1", 6, "Bad"));
        }

        @Test
        void addReviewServerNotFound() {
            assertNull(service.addReview("nonexistent", "reviewer1", 5, "Good"));
        }

        @Test
        void getReviews() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            service.addReview("srv1", "r1", 5, "Great");
            service.addReview("srv1", "r2", 4, "Good");
            assertEquals(2, service.getReviews("srv1").size());
        }

        @Test
        void getAverageRating() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            service.addReview("srv1", "r1", 5, "Great");
            service.addReview("srv1", "r2", 3, "OK");
            assertEquals(4.0, service.getAverageRating("srv1"), 0.1);
        }

        @Test
        void getAverageRatingNoReviews() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            assertEquals(0.0, service.getAverageRating("srv1"), 0.1);
        }

        @Test
        void reviewToMap() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            CommunityServerService.Review review = service.addReview("srv1", "r1", 5, "Great");
            Map<String, Object> map = review.toMap();
            assertEquals(5, map.get("rating"));
            assertEquals("Great", map.get("comment"));
        }
    }

    @Nested
    class ConfigTemplates {
        @Test
        void registerTemplate() {
            CommunityServerService.ConfigTemplate template = service.registerTemplate(
                    "tpl1", "Traveller Quick Start", "Quick Traveller setup",
                    CommunityServerService.GameMode.TRAVELLER, 20,
                    Set.of("mod1"), Map.of("setting1", "value1"));
            assertNotNull(template);
            assertEquals("tpl1", template.getId());
            assertEquals(CommunityServerService.GameMode.TRAVELLER, template.getGameMode());
        }

        @Test
        void registerTemplateBlankId() {
            assertNull(service.registerTemplate("", "Name", "",
                    CommunityServerService.GameMode.GENERIC, 10, Set.of(), Map.of()));
        }

        @Test
        void registerTemplateBlankName() {
            assertNull(service.registerTemplate("tpl1", "", "",
                    CommunityServerService.GameMode.GENERIC, 10, Set.of(), Map.of()));
        }

        @Test
        void getTemplate() {
            service.registerTemplate("tpl1", "Name", "",
                    CommunityServerService.GameMode.GENERIC, 10, Set.of(), Map.of());
            assertNotNull(service.getTemplate("tpl1"));
        }

        @Test
        void getTemplateNotFound() {
            assertNull(service.getTemplate("nonexistent"));
        }

        @Test
        void listTemplates() {
            service.registerTemplate("tpl1", "Name1", "",
                    CommunityServerService.GameMode.GENERIC, 10, Set.of(), Map.of());
            service.registerTemplate("tpl2", "Name2", "",
                    CommunityServerService.GameMode.GENERIC, 10, Set.of(), Map.of());
            assertEquals(2, service.listTemplates().size());
        }

        @Test
        void listTemplatesByGameMode() {
            service.registerTemplate("tpl1", "Name1", "",
                    CommunityServerService.GameMode.TRAVELLER, 10, Set.of(), Map.of());
            service.registerTemplate("tpl2", "Name2", "",
                    CommunityServerService.GameMode.SFB, 10, Set.of(), Map.of());
            assertEquals(1, service.listTemplatesByGameMode(CommunityServerService.GameMode.TRAVELLER).size());
        }

        @Test
        void createServerFromTemplate() {
            service.registerTemplate("tpl1", "Traveller Quick", "Quick setup",
                    CommunityServerService.GameMode.TRAVELLER, 20, Set.of(), Map.of());
            CommunityServerService.ServerInfo server = service.createServerFromTemplate("tpl1", "srv1", "owner1");
            assertNotNull(server);
            assertEquals("Traveller Quick Instance", server.getName());
            assertEquals(CommunityServerService.GameMode.TRAVELLER, server.getGameMode());
            assertEquals(20, server.getMaxPlayers());
        }

        @Test
        void createServerFromTemplateNotFound() {
            assertNull(service.createServerFromTemplate("nonexistent", "srv1", "owner1"));
        }

        @Test
        void templateToMap() {
            service.registerTemplate("tpl1", "Name", "Desc",
                    CommunityServerService.GameMode.TRAVELLER, 20, Set.of("mod1"), Map.of("key", "val"));
            CommunityServerService.ConfigTemplate template = service.getTemplate("tpl1");
            Map<String, Object> map = template.toMap();
            assertEquals("Name", map.get("name"));
            assertEquals("TRAVELLER", map.get("gameMode"));
        }
    }

    @Nested
    class Statistics {
        @Test
        void getStatistics() {
            service.registerServer("srv1", "Test1", "", "localhost", 8080,
                    CommunityServerService.GameMode.TRAVELLER, 10, "owner1");
            service.registerServer("srv2", "Test2", "", "localhost", 8080,
                    CommunityServerService.GameMode.SFB, 10, "owner1");
            service.activateServer("srv1");
            service.addReview("srv1", "r1", 5, "Great");

            Map<String, Object> stats = service.getStatistics();
            assertEquals(2, stats.get("totalServers"));
            assertEquals(1, stats.get("activeServers"));
            assertEquals(1, stats.get("totalReviews"));
            assertEquals(5.0, stats.get("averageRating"));
        }

        @Test
        void getServerStatistics() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.TRAVELLER, 10, "owner1");
            service.activateServer("srv1");
            service.addReview("srv1", "r1", 5, "Great");

            Map<String, Object> stats = service.getServerStatistics("srv1");
            assertEquals("srv1", stats.get("serverId"));
            assertEquals("ACTIVE", stats.get("status"));
            assertEquals("TRAVELLER", stats.get("gameMode"));
        }

        @Test
        void getServerStatisticsNotFound() {
            assertTrue(service.getServerStatistics("nonexistent").isEmpty());
        }
    }

    @Nested
    class Management {
        @Test
        void removeServer() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            assertTrue(service.removeServer("srv1"));
            assertNull(service.getServer("srv1"));
        }

        @Test
        void removeActiveServer() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            service.activateServer("srv1");
            assertFalse(service.removeServer("srv1"));
        }

        @Test
        void removeServerNotFound() {
            assertFalse(service.removeServer("nonexistent"));
        }

        @Test
        void clearAll() {
            service.registerServer("srv1", "Test", "", "localhost", 8080,
                    CommunityServerService.GameMode.GENERIC, 10, "owner1");
            service.registerTemplate("tpl1", "Name", "",
                    CommunityServerService.GameMode.GENERIC, 10, Set.of(), Map.of());
            service.clearAll();
            assertEquals(0, service.listServers().size());
            assertEquals(0, service.listTemplates().size());
        }
    }

    @Nested
    class IntegrationScenarios {
        @Test
        void fullServerLifecycle() {
            // Register
            CommunityServerService.ServerInfo server = service.registerServer(
                    "srv1", "Galaxy Server", "Multiplayer galaxy", "example.com", 9090,
                    CommunityServerService.GameMode.TRAVELLER, 20, "owner1");
            assertNotNull(server);
            assertEquals(CommunityServerService.ServerStatus.REGISTERED, server.getStatus());

            // Activate
            server = service.activateServer("srv1");
            assertEquals(CommunityServerService.ServerStatus.ACTIVE, server.getStatus());

            // Add tags
            server = service.addTags("srv1", Set.of("pvp", "multiplayer"));
            assertTrue(server.getTags().contains("pvp"));

            // Update players
            server = service.updatePlayerCount("srv1", 5);
            assertEquals(5, server.getCurrentPlayers());

            // Share content
            CommunityServerService.ContentItem item = service.shareContent(
                    "srv1", "Custom Rules", CommunityServerService.ContentType.RULESET,
                    "Custom ruleset", "owner1", Map.of("version", "1.0"));
            assertNotNull(item);

            // Add review
            CommunityServerService.Review review = service.addReview("srv1", "player1", 5, "Amazing server!");
            assertNotNull(review);

            // Pause
            server = service.pauseServer("srv1");
            assertEquals(CommunityServerService.ServerStatus.PAUSED, server.getStatus());

            // Reactivate
            server = service.activateServer("srv1");
            assertEquals(CommunityServerService.ServerStatus.ACTIVE, server.getStatus());

            // Set offline
            server = service.setOffline("srv1");
            assertEquals(CommunityServerService.ServerStatus.OFFLINE, server.getStatus());
            assertEquals(0, server.getCurrentPlayers());

            // Remove
            assertTrue(service.removeServer("srv1"));
        }

        @Test
        void templateToServerWorkflow() {
            // Create template
            CommunityServerService.ConfigTemplate template = service.registerTemplate(
                    "traveller-quick", "Traveller Quick Start", "Quick Traveller setup",
                    CommunityServerService.GameMode.TRAVELLER, 20,
                    Set.of("mod1", "mod2"), Map.of("galaxySize", "medium"));
            assertNotNull(template);

            // Create server from template
            CommunityServerService.ServerInfo server = service.createServerFromTemplate(
                    "traveller-quick", "srv1", "owner1");
            assertNotNull(server);
            assertEquals("Traveller Quick Start Instance", server.getName());
            assertEquals(CommunityServerService.GameMode.TRAVELLER, server.getGameMode());
            assertEquals(20, server.getMaxPlayers());

            // Activate and share content
            service.activateServer("srv1");
            service.shareContent("srv1", "Galaxy Template",
                    CommunityServerService.ContentType.GALAXY_TEMPLATE, "Pre-built galaxy",
                    "owner1", Map.of("sectors", 100));

            assertEquals(1, service.getContent("srv1").size());
            assertEquals(1, service.listActiveServers().size());
        }

        @Test
        void multipleServersDiscovery() {
            // Register multiple servers
            service.registerServer("srv1", "Traveller Hub", "", "localhost", 8080,
                    CommunityServerService.GameMode.TRAVELLER, 10, "owner1");
            service.registerServer("srv2", "SFB Arena", "", "localhost", 8081,
                    CommunityServerService.GameMode.SFB, 5, "owner2");
            service.registerServer("srv3", "D&D Dungeon", "", "localhost", 8082,
                    CommunityServerService.GameMode.DND_5E, 8, "owner3");

            // Activate all
            service.activateServer("srv1");
            service.activateServer("srv2");
            service.activateServer("srv3");

            // Discovery
            assertEquals(3, service.listActiveServers().size());
            assertEquals(1, service.listServersByGameMode(CommunityServerService.GameMode.TRAVELLER).size());
            assertEquals(3, service.listServersWithSlots().size());

            // Fill one server
            service.updatePlayerCount("srv2", 5);
            assertEquals(2, service.listServersWithSlots().size());

            // Statistics
            Map<String, Object> stats = service.getStatistics();
            assertEquals(3, stats.get("totalServers"));
            assertEquals(3, stats.get("activeServers"));
        }
    }
}
