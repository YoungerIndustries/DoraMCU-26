package com.yisy.modules.doramcu.config;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Location;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026LocationRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026ScenarioRepository;
import com.yisy.modules.doramcu.gateway.PluginDescriptor;
import com.yisy.modules.doramcu.gateway.PluginGateway;
import com.yisy.modules.doramcu.unified.GameType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Earth2026Initializer Tests")
class Earth2026InitializerTest {

    private StartupProgressService startupProgress;
    private Earth2026LocationRepository locationRepository;
    private Earth2026ScenarioRepository scenarioRepository;
    private PluginGateway pluginGateway;
    private Earth2026Initializer initializer;

    @BeforeEach
    void setUp() {
        startupProgress = mock(StartupProgressService.class);
        locationRepository = mock(Earth2026LocationRepository.class);
        scenarioRepository = mock(Earth2026ScenarioRepository.class);
        pluginGateway = mock(PluginGateway.class);
        initializer = new Earth2026Initializer(startupProgress);
    }

    @Nested
    @DisplayName("initEarth2026Hub")
    class InitEarth2026HubTests {

        private CommandLineRunner runner;

        @BeforeEach
        void createRunner() {
            runner = initializer.initEarth2026Hub(locationRepository, scenarioRepository, pluginGateway);
            assertNotNull(runner);
        }

        @Test
        @DisplayName("should start and complete earth_2026_hub step")
        void shouldStartAndCompleteStep() throws Exception {
            when(locationRepository.count()).thenReturn(8L);
            when(locationRepository.countByHasPortalTrue()).thenReturn(3L);
            when(locationRepository.findByHasPortalTrue()).thenReturn(List.of());
            when(scenarioRepository.count()).thenReturn(6L);
            when(pluginGateway.getAvailablePlugins()).thenReturn(List.of());
            when(pluginGateway.getEnabledPlugins()).thenReturn(List.of());

            runner.run(new String[0]);

            verify(startupProgress).startStep(eq("earth_2026_hub"), anyString());
            verify(startupProgress).completeStep(eq("earth_2026_hub"), anyString());
        }

        @Test
        @DisplayName("should verify location count")
        void shouldVerifyLocationCount() throws Exception {
            when(locationRepository.count()).thenReturn(10L);
            when(locationRepository.countByHasPortalTrue()).thenReturn(2L);
            when(locationRepository.findByHasPortalTrue()).thenReturn(List.of());
            when(scenarioRepository.count()).thenReturn(5L);
            when(pluginGateway.getAvailablePlugins()).thenReturn(List.of());
            when(pluginGateway.getEnabledPlugins()).thenReturn(List.of());

            runner.run(new String[0]);

            verify(locationRepository).count();
        }

        @Test
        @DisplayName("should verify portal count")
        void shouldVerifyPortalCount() throws Exception {
            when(locationRepository.count()).thenReturn(8L);
            when(locationRepository.countByHasPortalTrue()).thenReturn(3L);
            when(locationRepository.findByHasPortalTrue()).thenReturn(List.of());
            when(scenarioRepository.count()).thenReturn(6L);
            when(pluginGateway.getAvailablePlugins()).thenReturn(List.of());
            when(pluginGateway.getEnabledPlugins()).thenReturn(List.of());

            runner.run(new String[0]);

            verify(locationRepository).countByHasPortalTrue();
        }

        @Test
        @DisplayName("should list portal locations")
        void shouldListPortalLocations() throws Exception {
            Earth2026Location portal1 = new Earth2026Location("The Portal Room", "San Diego", "USA", "Portal");
            portal1.setPortalGalaxy("Milky Way");

            Earth2026Location portal2 = new Earth2026Location("Area 51", "Rachel", "USA", "Portal");
            portal2.setPortalGalaxy("The Void");

            when(locationRepository.count()).thenReturn(8L);
            when(locationRepository.countByHasPortalTrue()).thenReturn(2L);
            when(locationRepository.findByHasPortalTrue()).thenReturn(List.of(portal1, portal2));
            when(scenarioRepository.count()).thenReturn(6L);
            when(pluginGateway.getAvailablePlugins()).thenReturn(List.of());
            when(pluginGateway.getEnabledPlugins()).thenReturn(List.of());

            runner.run(new String[0]);

            verify(locationRepository).findByHasPortalTrue();
        }

        @Test
        @DisplayName("should verify scenario count")
        void shouldVerifyScenarioCount() throws Exception {
            when(locationRepository.count()).thenReturn(8L);
            when(locationRepository.countByHasPortalTrue()).thenReturn(3L);
            when(locationRepository.findByHasPortalTrue()).thenReturn(List.of());
            when(scenarioRepository.count()).thenReturn(6L);
            when(pluginGateway.getAvailablePlugins()).thenReturn(List.of());
            when(pluginGateway.getEnabledPlugins()).thenReturn(List.of());

            runner.run(new String[0]);

            verify(scenarioRepository).count();
        }

        @Test
        @DisplayName("should verify plugin gateway")
        void shouldVerifyPluginGateway() throws Exception {
            PluginDescriptor earth2026 = new PluginDescriptor(
                    GameType.EARTH_2026, "Earth 2026", "Core module", "com.yisy.modules.doramcu.earth2026"
            );
            earth2026.setEnabled(true);

            when(locationRepository.count()).thenReturn(8L);
            when(locationRepository.countByHasPortalTrue()).thenReturn(3L);
            when(locationRepository.findByHasPortalTrue()).thenReturn(List.of());
            when(scenarioRepository.count()).thenReturn(6L);
            when(pluginGateway.getAvailablePlugins()).thenReturn(List.of(earth2026));
            when(pluginGateway.getEnabledPlugins()).thenReturn(List.of(earth2026));

            runner.run(new String[0]);

            verify(pluginGateway).getAvailablePlugins();
            verify(pluginGateway).getEnabledPlugins();
        }

        @Test
        @DisplayName("should include correct counts in completion message")
        void shouldIncludeCountsInCompletionMessage() throws Exception {
            when(locationRepository.count()).thenReturn(8L);
            when(locationRepository.countByHasPortalTrue()).thenReturn(3L);
            when(locationRepository.findByHasPortalTrue()).thenReturn(List.of());
            when(scenarioRepository.count()).thenReturn(6L);
            when(pluginGateway.getAvailablePlugins()).thenReturn(List.of());
            when(pluginGateway.getEnabledPlugins()).thenReturn(List.of());

            runner.run(new String[0]);

            verify(startupProgress).completeStep(eq("earth_2026_hub"),
                    eq("Earth 2026 Hub initialized with 8 locations, 6 scenarios, 3 portals"));
        }

        @Test
        @DisplayName("should handle zero locations")
        void shouldHandleZeroLocations() throws Exception {
            when(locationRepository.count()).thenReturn(0L);
            when(locationRepository.countByHasPortalTrue()).thenReturn(0L);
            when(locationRepository.findByHasPortalTrue()).thenReturn(List.of());
            when(scenarioRepository.count()).thenReturn(0L);
            when(pluginGateway.getAvailablePlugins()).thenReturn(List.of());
            when(pluginGateway.getEnabledPlugins()).thenReturn(List.of());

            runner.run(new String[0]);

            verify(startupProgress).completeStep(eq("earth_2026_hub"),
                    eq("Earth 2026 Hub initialized with 0 locations, 0 scenarios, 0 portals"));
        }

        @Test
        @DisplayName("should handle no portals")
        void shouldHandleNoPortals() throws Exception {
            when(locationRepository.count()).thenReturn(8L);
            when(locationRepository.countByHasPortalTrue()).thenReturn(0L);
            when(locationRepository.findByHasPortalTrue()).thenReturn(List.of());
            when(scenarioRepository.count()).thenReturn(6L);
            when(pluginGateway.getAvailablePlugins()).thenReturn(List.of());
            when(pluginGateway.getEnabledPlugins()).thenReturn(List.of());

            runner.run(new String[0]);

            verify(startupProgress).completeStep(eq("earth_2026_hub"),
                    eq("Earth 2026 Hub initialized with 8 locations, 6 scenarios, 0 portals"));
        }

        @Test
        @DisplayName("should handle multiple plugins")
        void shouldHandleMultiplePlugins() throws Exception {
            PluginDescriptor earth2026 = new PluginDescriptor(
                    GameType.EARTH_2026, "Earth 2026", "Core module", "com.yisy.modules.doramcu.earth2026"
            );
            earth2026.setEnabled(true);

            PluginDescriptor traveller = new PluginDescriptor(
                    GameType.TRAVELLER, "Traveller", "Space RPG", "com.yisy.modules.doramcu.traveller"
            );

            when(locationRepository.count()).thenReturn(8L);
            when(locationRepository.countByHasPortalTrue()).thenReturn(3L);
            when(locationRepository.findByHasPortalTrue()).thenReturn(List.of());
            when(scenarioRepository.count()).thenReturn(6L);
            when(pluginGateway.getAvailablePlugins()).thenReturn(List.of(earth2026, traveller));
            when(pluginGateway.getEnabledPlugins()).thenReturn(List.of(earth2026));

            runner.run(new String[0]);

            verify(pluginGateway).getAvailablePlugins();
            verify(pluginGateway).getEnabledPlugins();
        }
    }
}
