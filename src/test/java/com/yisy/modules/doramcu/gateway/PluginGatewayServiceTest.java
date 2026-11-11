package com.yisy.modules.doramcu.gateway;

import com.yisy.modules.doramcu.hex.GalaxyFactory;
import com.yisy.modules.doramcu.unified.GameType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PluginGatewayService Tests")
class PluginGatewayServiceTest {

    private PluginGatewayService pluginGatewayService;

    @BeforeEach
    void setUp() {
        pluginGatewayService = new PluginGatewayService();
    }

    // ---- registerPlugin ----

    @Nested
    @DisplayName("registerPlugin Tests")
    class RegisterPluginTests {

        @Test
        @DisplayName("should register a plugin")
        void registerPlugin_shouldRegisterPlugin() {
            // Arrange
            PluginDescriptor descriptor = new PluginDescriptor(
                    GameType.TRAVELLER,
                    "Traveller",
                    "Space RPG",
                    "com.yisy.modules.doramcu.traveller"
            );

            // Act
            pluginGatewayService.registerPlugin(descriptor);

            // Assert
            assertTrue(pluginGatewayService.getPlugin(GameType.TRAVELLER).isPresent());
        }

        @Test
        @DisplayName("should register multiple plugins")
        void registerPlugin_shouldRegisterMultiplePlugins() {
            // Arrange & Act
            pluginGatewayService.registerPlugin(new PluginDescriptor(
                    GameType.TRAVELLER, "Traveller", "Space RPG", "com.yisy.modules.doramcu.traveller"
            ));
            pluginGatewayService.registerPlugin(new PluginDescriptor(
                    GameType.CAPTAINS_LOG, "Captain's Log", "Star Trek RPG", "com.yisy.modules.doramcu.captainslog"
            ));

            // Assert
            assertTrue(pluginGatewayService.getPlugin(GameType.TRAVELLER).isPresent());
            assertTrue(pluginGatewayService.getPlugin(GameType.CAPTAINS_LOG).isPresent());
        }

        @Test
        @DisplayName("should overwrite existing plugin")
        void registerPlugin_shouldOverwriteExisting() {
            // Arrange
            PluginDescriptor descriptor1 = new PluginDescriptor(
                    GameType.TRAVELLER, "Traveller Old", "Old desc", "com.old"
            );
            pluginGatewayService.registerPlugin(descriptor1);

            // Act
            PluginDescriptor descriptor2 = new PluginDescriptor(
                    GameType.TRAVELLER, "Traveller New", "New desc", "com.new"
            );
            pluginGatewayService.registerPlugin(descriptor2);

            // Assert
            Optional<PluginDescriptor> plugin = pluginGatewayService.getPlugin(GameType.TRAVELLER);
            assertTrue(plugin.isPresent());
            assertEquals("Traveller New", plugin.get().getDisplayName());
        }
    }

    // ---- getPlugin ----

    @Nested
    @DisplayName("getPlugin Tests")
    class GetPluginTests {

        @Test
        @DisplayName("should return empty when plugin not registered")
        void getPlugin_shouldReturnEmptyWhenNotRegistered() {
            // Act
            Optional<PluginDescriptor> plugin = pluginGatewayService.getPlugin(GameType.TRAVELLER);

            // Assert
            assertTrue(plugin.isEmpty());
        }

        @Test
        @DisplayName("should return plugin when registered")
        void getPlugin_shouldReturnWhenRegistered() {
            // Arrange
            PluginDescriptor descriptor = new PluginDescriptor(
                    GameType.TRAVELLER, "Traveller", "Space RPG", "com.yisy.modules.doramcu.traveller"
            );
            pluginGatewayService.registerPlugin(descriptor);

            // Act
            Optional<PluginDescriptor> plugin = pluginGatewayService.getPlugin(GameType.TRAVELLER);

            // Assert
            assertTrue(plugin.isPresent());
            assertEquals("Traveller", plugin.get().getDisplayName());
        }
    }

    // ---- getAvailablePlugins ----

    @Nested
    @DisplayName("getAvailablePlugins Tests")
    class GetAvailablePluginsTests {

        @Test
        @DisplayName("should return empty list when no plugins registered")
        void getAvailablePlugins_shouldReturnEmptyList() {
            // Act
            List<PluginDescriptor> plugins = pluginGatewayService.getAvailablePlugins();

            // Assert
            assertTrue(plugins.isEmpty());
        }

        @Test
        @DisplayName("should return all registered plugins")
        void getAvailablePlugins_shouldReturnAllPlugins() {
            // Arrange
            pluginGatewayService.registerPlugin(new PluginDescriptor(
                    GameType.TRAVELLER, "Traveller", "Space RPG", "com.yisy.modules.doramcu.traveller"
            ));
            pluginGatewayService.registerPlugin(new PluginDescriptor(
                    GameType.CAPTAINS_LOG, "Captain's Log", "Star Trek RPG", "com.yisy.modules.doramcu.captainslog"
            ));

            // Act
            List<PluginDescriptor> plugins = pluginGatewayService.getAvailablePlugins();

            // Assert
            assertEquals(2, plugins.size());
        }
    }

    // ---- getEnabledPlugins ----

    @Nested
    @DisplayName("getEnabledPlugins Tests")
    class GetEnabledPluginsTests {

        @Test
        @DisplayName("should return only enabled plugins")
        void getEnabledPlugins_shouldReturnOnlyEnabled() {
            // Arrange
            PluginDescriptor enabled = new PluginDescriptor(
                    GameType.EARTH_2026, "Earth 2026", "Hub", "com.yisy.modules.doramcu.earth2026"
            );
            enabled.setEnabled(true);
            pluginGatewayService.registerPlugin(enabled);

            PluginDescriptor disabled = new PluginDescriptor(
                    GameType.TRAVELLER, "Traveller", "Space RPG", "com.yisy.modules.doramcu.traveller"
            );
            pluginGatewayService.registerPlugin(disabled);

            // Act
            List<PluginDescriptor> enabledPlugins = pluginGatewayService.getEnabledPlugins();

            // Assert
            assertEquals(1, enabledPlugins.size());
            assertEquals(GameType.EARTH_2026, enabledPlugins.get(0).getGameType());
        }
    }

    // ---- loadPlugin ----

    @Nested
    @DisplayName("loadPlugin Tests")
    class LoadPluginTests {

        @Test
        @DisplayName("should load plugin and return true")
        void loadPlugin_shouldLoadAndReturnTrue() {
            // Arrange
            PluginDescriptor descriptor = new PluginDescriptor(
                    GameType.TRAVELLER, "Traveller", "Space RPG", "com.yisy.modules.doramcu.traveller"
            );
            pluginGatewayService.registerPlugin(descriptor);
            assertFalse(descriptor.isEnabled());

            // Act
            boolean result = pluginGatewayService.loadPlugin(GameType.TRAVELLER);

            // Assert
            assertTrue(result);
            assertTrue(descriptor.isEnabled());
        }

        @Test
        @DisplayName("should return false when plugin already loaded")
        void loadPlugin_shouldReturnFalseWhenAlreadyLoaded() {
            // Arrange
            PluginDescriptor descriptor = new PluginDescriptor(
                    GameType.TRAVELLER, "Traveller", "Space RPG", "com.yisy.modules.doramcu.traveller"
            );
            descriptor.setEnabled(true);
            pluginGatewayService.registerPlugin(descriptor);

            // Act
            boolean result = pluginGatewayService.loadPlugin(GameType.TRAVELLER);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("should return false when plugin not registered")
        void loadPlugin_shouldReturnFalseWhenNotRegistered() {
            // Act
            boolean result = pluginGatewayService.loadPlugin(GameType.TRAVELLER);

            // Assert
            assertFalse(result);
        }
    }

    // ---- unloadPlugin ----

    @Nested
    @DisplayName("unloadPlugin Tests")
    class UnloadPluginTests {

        @Test
        @DisplayName("should unload plugin")
        void unloadPlugin_shouldUnloadPlugin() {
            // Arrange
            PluginDescriptor descriptor = new PluginDescriptor(
                    GameType.TRAVELLER, "Traveller", "Space RPG", "com.yisy.modules.doramcu.traveller"
            );
            descriptor.setEnabled(true);
            pluginGatewayService.registerPlugin(descriptor);

            // Act
            pluginGatewayService.unloadPlugin(GameType.TRAVELLER);

            // Assert
            assertFalse(descriptor.isEnabled());
        }

        @Test
        @DisplayName("should do nothing when plugin not registered")
        void unloadPlugin_shouldDoNothingWhenNotRegistered() {
            // Act & Assert - should not throw
            pluginGatewayService.unloadPlugin(GameType.TRAVELLER);
        }
    }

    // ---- isPluginLoaded ----

    @Nested
    @DisplayName("isPluginLoaded Tests")
    class IsPluginLoadedTests {

        @Test
        @DisplayName("should return false when plugin not registered")
        void isPluginLoaded_shouldReturnFalseWhenNotRegistered() {
            // Act & Assert
            assertFalse(pluginGatewayService.isPluginLoaded(GameType.TRAVELLER));
        }

        @Test
        @DisplayName("should return false when plugin registered but not enabled")
        void isPluginLoaded_shouldReturnFalseWhenNotEnabled() {
            // Arrange
            pluginGatewayService.registerPlugin(new PluginDescriptor(
                    GameType.TRAVELLER, "Traveller", "Space RPG", "com.yisy.modules.doramcu.traveller"
            ));

            // Act & Assert
            assertFalse(pluginGatewayService.isPluginLoaded(GameType.TRAVELLER));
        }

        @Test
        @DisplayName("should return true when plugin is enabled")
        void isPluginLoaded_shouldReturnTrueWhenEnabled() {
            // Arrange
            PluginDescriptor descriptor = new PluginDescriptor(
                    GameType.TRAVELLER, "Traveller", "Space RPG", "com.yisy.modules.doramcu.traveller"
            );
            descriptor.setEnabled(true);
            pluginGatewayService.registerPlugin(descriptor);

            // Act & Assert
            assertTrue(pluginGatewayService.isPluginLoaded(GameType.TRAVELLER));
        }
    }

    // ---- getPluginForGalaxy ----

    @Nested
    @DisplayName("getPluginForGalaxy Tests")
    class GetPluginForGalaxyTests {

        @Test
        @DisplayName("should return empty for null galaxy")
        void getPluginForGalaxy_shouldReturnEmptyForNull() {
            // Act & Assert
            assertTrue(pluginGatewayService.getPluginForGalaxy(null).isEmpty());
        }

        @Test
        @DisplayName("should return empty for blank galaxy")
        void getPluginForGalaxy_shouldReturnEmptyForBlank() {
            // Act & Assert
            assertTrue(pluginGatewayService.getPluginForGalaxy("   ").isEmpty());
        }

        @Test
        @DisplayName("should return empty for unknown galaxy")
        void getPluginForGalaxy_shouldReturnEmptyForUnknown() {
            // Act & Assert
            assertTrue(pluginGatewayService.getPluginForGalaxy("Unknown Galaxy").isEmpty());
        }

        @Test
        @DisplayName("should return plugin for Milky Way")
        void getPluginForGalaxy_shouldReturnForMilkyWay() {
            // Arrange
            pluginGatewayService.registerPlugin(new PluginDescriptor(
                    GameType.DORAMCU_4X, "DoraMCU 4X", "4X game", "com.yisy.modules.doramcu"
            ));

            // Act
            Optional<PluginDescriptor> plugin = pluginGatewayService.getPluginForGalaxy(GalaxyFactory.GAME_GALAXY_NAME);

            // Assert
            assertTrue(plugin.isPresent());
            assertEquals(GameType.DORAMCU_4X, plugin.get().getGameType());
        }

        @Test
        @DisplayName("should return plugin for The Void")
        void getPluginForGalaxy_shouldReturnForTheVoid() {
            // Arrange
            pluginGatewayService.registerPlugin(new PluginDescriptor(
                    GameType.TRAVELLER, "Traveller", "Space RPG", "com.yisy.modules.doramcu.traveller"
            ));

            // Act
            Optional<PluginDescriptor> plugin = pluginGatewayService.getPluginForGalaxy(GalaxyFactory.VOID_GALAXY_NAME);

            // Assert
            assertTrue(plugin.isPresent());
            assertEquals(GameType.TRAVELLER, plugin.get().getGameType());
        }
    }

    // ---- getGameTypeForGalaxy ----

    @Nested
    @DisplayName("getGameTypeForGalaxy Tests")
    class GetGameTypeForGalaxyTests {

        @Test
        @DisplayName("should return empty for null galaxy")
        void getGameTypeForGalaxy_shouldReturnEmptyForNull() {
            // Act & Assert
            assertTrue(pluginGatewayService.getGameTypeForGalaxy(null).isEmpty());
        }

        @Test
        @DisplayName("should return empty for blank galaxy")
        void getGameTypeForGalaxy_shouldReturnEmptyForBlank() {
            // Act & Assert
            assertTrue(pluginGatewayService.getGameTypeForGalaxy("   ").isEmpty());
        }

        @Test
        @DisplayName("should return DORAMCU_4X for Milky Way")
        void getGameTypeForGalaxy_shouldReturnDora4xForMilkyWay() {
            // Act
            Optional<GameType> gameType = pluginGatewayService.getGameTypeForGalaxy(GalaxyFactory.GAME_GALAXY_NAME);

            // Assert
            assertTrue(gameType.isPresent());
            assertEquals(GameType.DORAMCU_4X, gameType.get());
        }

        @Test
        @DisplayName("should return TRAVELLER for The Void")
        void getGameTypeForGalaxy_shouldReturnTravellerForTheVoid() {
            // Act
            Optional<GameType> gameType = pluginGatewayService.getGameTypeForGalaxy(GalaxyFactory.VOID_GALAXY_NAME);

            // Assert
            assertTrue(gameType.isPresent());
            assertEquals(GameType.TRAVELLER, gameType.get());
        }

        @Test
        @DisplayName("should return DORAMCU_4X for Test Galaxy")
        void getGameTypeForGalaxy_shouldReturnDora4xForTestGalaxy() {
            // Act
            Optional<GameType> gameType = pluginGatewayService.getGameTypeForGalaxy(GalaxyFactory.TEST_GALAXY_NAME);

            // Assert
            assertTrue(gameType.isPresent());
            assertEquals(GameType.DORAMCU_4X, gameType.get());
        }

        @Test
        @DisplayName("should return empty for unknown galaxy")
        void getGameTypeForGalaxy_shouldReturnEmptyForUnknown() {
            // Act & Assert
            assertTrue(pluginGatewayService.getGameTypeForGalaxy("Unknown Galaxy").isEmpty());
        }
    }

    // ---- registerDefaultPlugins ----

    @Nested
    @DisplayName("registerDefaultPlugins Tests")
    class RegisterDefaultPluginsTests {

        @Test
        @DisplayName("should register all default plugins")
        void registerDefaultPlugins_shouldRegisterAll() {
            // Act
            pluginGatewayService.registerDefaultPlugins();

            // Assert
            List<PluginDescriptor> plugins = pluginGatewayService.getAvailablePlugins();
            assertEquals(6, plugins.size());
        }

        @Test
        @DisplayName("should enable Earth 2026 as core module")
        void registerDefaultPlugins_shouldEnableEarth2026() {
            // Act
            pluginGatewayService.registerDefaultPlugins();

            // Assert
            assertTrue(pluginGatewayService.isPluginLoaded(GameType.EARTH_2026));
        }

        @Test
        @DisplayName("should not enable other plugins by default")
        void registerDefaultPlugins_shouldNotEnableOthers() {
            // Act
            pluginGatewayService.registerDefaultPlugins();

            // Assert
            assertFalse(pluginGatewayService.isPluginLoaded(GameType.TRAVELLER));
            assertFalse(pluginGatewayService.isPluginLoaded(GameType.CAPTAINS_LOG));
            assertFalse(pluginGatewayService.isPluginLoaded(GameType.DND_5E));
            assertFalse(pluginGatewayService.isPluginLoaded(GameType.THIRD_REICH));
            assertFalse(pluginGatewayService.isPluginLoaded(GameType.DORAMCU_4X));
        }

        @Test
        @DisplayName("should set correct target galaxies")
        void registerDefaultPlugins_shouldSetTargetGalaxies() {
            // Act
            pluginGatewayService.registerDefaultPlugins();

            // Assert
            Optional<PluginDescriptor> dora4x = pluginGatewayService.getPlugin(GameType.DORAMCU_4X);
            assertTrue(dora4x.isPresent());
            assertEquals(GalaxyFactory.GAME_GALAXY_NAME, dora4x.get().getTargetGalaxy());

            Optional<PluginDescriptor> traveller = pluginGatewayService.getPlugin(GameType.TRAVELLER);
            assertTrue(traveller.isPresent());
            assertEquals(GalaxyFactory.VOID_GALAXY_NAME, traveller.get().getTargetGalaxy());
        }

        @Test
        @DisplayName("should set Earth 2026 as not requiring galaxy transition")
        void registerDefaultPlugins_shouldSetEarth2026NoTransition() {
            // Act
            pluginGatewayService.registerDefaultPlugins();

            // Assert
            Optional<PluginDescriptor> earth2026 = pluginGatewayService.getPlugin(GameType.EARTH_2026);
            assertTrue(earth2026.isPresent());
            assertFalse(earth2026.get().isRequiresGalaxyTransition());
        }
    }
}
