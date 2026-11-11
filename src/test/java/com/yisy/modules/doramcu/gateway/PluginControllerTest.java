package com.yisy.modules.doramcu.gateway;

import com.yisy.modules.doramcu.unified.GameType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("PluginController Tests")
class PluginControllerTest {

    private PluginGateway pluginGateway;
    private PluginController pluginController;

    @BeforeEach
    void setUp() {
        pluginGateway = mock(PluginGateway.class);
        pluginController = new PluginController(pluginGateway);
    }

    // ---- getAvailablePlugins ----

    @Nested
    @DisplayName("GET /api/v1/plugins Tests")
    class GetAllPluginsTests {

        @Test
        @DisplayName("should return all available plugins")
        void getAvailablePlugins_shouldReturnAll() {
            // Arrange
            when(pluginGateway.getAvailablePlugins()).thenReturn(List.of(
                    new PluginDescriptor(GameType.TRAVELLER, "Traveller", "Space RPG", "com.traveller"),
                    new PluginDescriptor(GameType.EARTH_2026, "Earth 2026", "Hub", "com.earth2026")
            ));

            // Act
            ResponseEntity<List<PluginDescriptor>> response = pluginController.getAvailablePlugins();

            // Assert
            assertEquals(200, response.getStatusCode().value());
            assertEquals(2, response.getBody().size());
        }

        @Test
        @DisplayName("should return empty list when no plugins")
        void getAvailablePlugins_shouldReturnEmpty() {
            // Arrange
            when(pluginGateway.getAvailablePlugins()).thenReturn(List.of());

            // Act
            ResponseEntity<List<PluginDescriptor>> response = pluginController.getAvailablePlugins();

            // Assert
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isEmpty());
        }
    }

    // ---- getEnabledPlugins ----

    @Nested
    @DisplayName("GET /api/v1/plugins/enabled Tests")
    class GetEnabledPluginsTests {

        @Test
        @DisplayName("should return only enabled plugins")
        void getEnabledPlugins_shouldReturnEnabled() {
            // Arrange
            PluginDescriptor enabled = new PluginDescriptor(
                    GameType.EARTH_2026, "Earth 2026", "Hub", "com.earth2026"
            );
            enabled.setEnabled(true);
            when(pluginGateway.getEnabledPlugins()).thenReturn(List.of(enabled));

            // Act
            ResponseEntity<List<PluginDescriptor>> response = pluginController.getEnabledPlugins();

            // Assert
            assertEquals(200, response.getStatusCode().value());
            assertEquals(1, response.getBody().size());
        }
    }

    // ---- getPlugin ----

    @Nested
    @DisplayName("GET /api/v1/plugins/{gameType} Tests")
    class GetPluginTests {

        @Test
        @DisplayName("should return plugin by game type")
        void getPlugin_shouldReturnPlugin() {
            // Arrange
            PluginDescriptor descriptor = new PluginDescriptor(
                    GameType.TRAVELLER, "Traveller", "Space RPG", "com.traveller"
            );
            when(pluginGateway.getPlugin(GameType.TRAVELLER)).thenReturn(Optional.of(descriptor));

            // Act
            ResponseEntity<PluginDescriptor> response = pluginController.getPlugin("TRAVELLER");

            // Assert
            assertEquals(200, response.getStatusCode().value());
            assertEquals("Traveller", response.getBody().getDisplayName());
        }

        @Test
        @DisplayName("should return 404 when plugin not found")
        void getPlugin_shouldReturn404WhenNotFound() {
            // Arrange
            when(pluginGateway.getPlugin(GameType.TRAVELLER)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<PluginDescriptor> response = pluginController.getPlugin("TRAVELLER");

            // Assert
            assertEquals(404, response.getStatusCode().value());
        }

        @Test
        @DisplayName("should return 400 for invalid game type")
        void getPlugin_shouldReturn400ForInvalidGameType() {
            // Act
            ResponseEntity<PluginDescriptor> response = pluginController.getPlugin("INVALID");

            // Assert
            assertEquals(400, response.getStatusCode().value());
        }

        @Test
        @DisplayName("should accept lowercase game type")
        void getPlugin_shouldAcceptLowercase() {
            // Arrange
            PluginDescriptor descriptor = new PluginDescriptor(
                    GameType.TRAVELLER, "Traveller", "Space RPG", "com.traveller"
            );
            when(pluginGateway.getPlugin(GameType.TRAVELLER)).thenReturn(Optional.of(descriptor));

            // Act
            ResponseEntity<PluginDescriptor> response = pluginController.getPlugin("traveller");

            // Assert
            assertEquals(200, response.getStatusCode().value());
        }
    }

    // ---- loadPlugin ----

    @Nested
    @DisplayName("POST /api/v1/plugins/{gameType}/load Tests")
    class LoadPluginTests {

        @Test
        @DisplayName("should load plugin and return success")
        void loadPlugin_shouldLoadAndReturnSuccess() {
            // Arrange
            when(pluginGateway.loadPlugin(GameType.TRAVELLER)).thenReturn(true);

            // Act
            ResponseEntity<Map<String, Object>> response = pluginController.loadPlugin("TRAVELLER");

            // Assert
            assertEquals(200, response.getStatusCode().value());
            assertTrue((Boolean) response.getBody().get("success"));
            verify(pluginGateway).loadPlugin(GameType.TRAVELLER);
        }

        @Test
        @DisplayName("should return success when plugin already loaded")
        void loadPlugin_shouldReturnSuccessWhenAlreadyLoaded() {
            // Arrange
            when(pluginGateway.loadPlugin(GameType.TRAVELLER)).thenReturn(false);

            // Act
            ResponseEntity<Map<String, Object>> response = pluginController.loadPlugin("TRAVELLER");

            // Assert
            assertEquals(200, response.getStatusCode().value());
            assertTrue((Boolean) response.getBody().get("success"));
        }

        @Test
        @DisplayName("should return 400 for invalid game type")
        void loadPlugin_shouldReturn400ForInvalidGameType() {
            // Act
            ResponseEntity<Map<String, Object>> response = pluginController.loadPlugin("INVALID");

            // Assert
            assertEquals(400, response.getStatusCode().value());
            assertFalse((Boolean) response.getBody().get("success"));
        }
    }

    // ---- unloadPlugin ----

    @Nested
    @DisplayName("POST /api/v1/plugins/{gameType}/unload Tests")
    class UnloadPluginTests {

        @Test
        @DisplayName("should unload plugin and return success")
        void unloadPlugin_shouldUnloadAndReturnSuccess() {
            // Arrange
            doNothing().when(pluginGateway).unloadPlugin(GameType.TRAVELLER);

            // Act
            ResponseEntity<Map<String, Object>> response = pluginController.unloadPlugin("TRAVELLER");

            // Assert
            assertEquals(200, response.getStatusCode().value());
            assertTrue((Boolean) response.getBody().get("success"));
            verify(pluginGateway).unloadPlugin(GameType.TRAVELLER);
        }

        @Test
        @DisplayName("should return 400 for invalid game type")
        void unloadPlugin_shouldReturn400ForInvalidGameType() {
            // Act
            ResponseEntity<Map<String, Object>> response = pluginController.unloadPlugin("INVALID");

            // Assert
            assertEquals(400, response.getStatusCode().value());
            assertFalse((Boolean) response.getBody().get("success"));
        }
    }

    // ---- getPluginStatus ----

    @Nested
    @DisplayName("GET /api/v1/plugins/{gameType}/status Tests")
    class GetPluginStatusTests {

        @Test
        @DisplayName("should return loaded status")
        void getPluginStatus_shouldReturnLoaded() {
            // Arrange
            when(pluginGateway.isPluginLoaded(GameType.TRAVELLER)).thenReturn(true);

            // Act
            ResponseEntity<Map<String, Object>> response = pluginController.getPluginStatus("TRAVELLER");

            // Assert
            assertEquals(200, response.getStatusCode().value());
            assertTrue((Boolean) response.getBody().get("loaded"));
        }

        @Test
        @DisplayName("should return not loaded status")
        void getPluginStatus_shouldReturnNotLoaded() {
            // Arrange
            when(pluginGateway.isPluginLoaded(GameType.TRAVELLER)).thenReturn(false);

            // Act
            ResponseEntity<Map<String, Object>> response = pluginController.getPluginStatus("TRAVELLER");

            // Assert
            assertEquals(200, response.getStatusCode().value());
            assertFalse((Boolean) response.getBody().get("loaded"));
        }

        @Test
        @DisplayName("should return 400 for invalid game type")
        void getPluginStatus_shouldReturn400ForInvalidGameType() {
            // Act
            ResponseEntity<Map<String, Object>> response = pluginController.getPluginStatus("INVALID");

            // Assert
            assertEquals(400, response.getStatusCode().value());
        }
    }

    // ---- getPluginForGalaxy ----

    @Nested
    @DisplayName("GET /api/v1/plugins/galaxy/{galaxyName} Tests")
    class GetPluginForGalaxyTests {

        @Test
        @DisplayName("should return plugin for galaxy")
        void getPluginForGalaxy_shouldReturnPlugin() {
            // Arrange
            PluginDescriptor descriptor = new PluginDescriptor(
                    GameType.TRAVELLER, "Traveller", "Space RPG", "com.traveller"
            );
            when(pluginGateway.getPluginForGalaxy("The Void")).thenReturn(Optional.of(descriptor));

            // Act
            ResponseEntity<PluginDescriptor> response = pluginController.getPluginForGalaxy("The Void");

            // Assert
            assertEquals(200, response.getStatusCode().value());
            assertEquals("Traveller", response.getBody().getDisplayName());
        }

        @Test
        @DisplayName("should return 404 when no plugin for galaxy")
        void getPluginForGalaxy_shouldReturn404() {
            // Arrange
            when(pluginGateway.getPluginForGalaxy("Unknown Galaxy")).thenReturn(Optional.empty());

            // Act
            ResponseEntity<PluginDescriptor> response = pluginController.getPluginForGalaxy("Unknown Galaxy");

            // Assert
            assertEquals(404, response.getStatusCode().value());
        }
    }
}
