package com.yisy.modules.doramcu.gateway;

import com.yisy.modules.doramcu.hex.GalaxyFactory;
import com.yisy.modules.doramcu.unified.GameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of the Plugin Gateway that manages game plugin lifecycle.
 * <p>
 * At startup, all available game plugins are registered but not loaded.
 * When a character transitions from Earth 2026 to a game galaxy, the
 * corresponding plugin is loaded, enabling its controllers and services.
 * </p>
 * <p>
 * The Earth 2026 plugin is always loaded as the core module.
 * </p>
 */
@Service
public class PluginGatewayService implements PluginGateway {

    private static final Logger log = LoggerFactory.getLogger(PluginGatewayService.class);

    private final Map<GameType, PluginDescriptor> plugins = new ConcurrentHashMap<>();

    /**
     * Maps galaxy names to game types for plugin discovery.
     * @deprecated Plugins are now loaded dynamically based on galaxy metadata, not hardcoded names.
     */
    @Deprecated
    private static final Map<String, GameType> GALAXY_TO_GAME_TYPE = Map.of(
            GalaxyFactory.GAME_GALAXY_NAME, GameType.DORAMCU_4X,
            GalaxyFactory.TEST_GALAXY_NAME, GameType.DORAMCU_4X
    );

    @Override
    public void registerPlugin(PluginDescriptor descriptor) {
        plugins.put(descriptor.getGameType(), descriptor);
        log.info("Registered plugin: {} ({})", descriptor.getDisplayName(), descriptor.getPackageName());
    }

    @Override
    public Optional<PluginDescriptor> getPlugin(GameType gameType) {
        return Optional.ofNullable(plugins.get(gameType));
    }

    @Override
    public List<PluginDescriptor> getAvailablePlugins() {
        return List.copyOf(plugins.values());
    }

    @Override
    public List<PluginDescriptor> getEnabledPlugins() {
        return plugins.values().stream()
                .filter(PluginDescriptor::isEnabled)
                .collect(Collectors.toList());
    }

    @Override
    public boolean loadPlugin(GameType gameType) {
        PluginDescriptor descriptor = plugins.get(gameType);
        if (descriptor == null) {
            log.warn("Plugin not found for game type: {}", gameType);
            return false;
        }
        if (descriptor.isEnabled()) {
            log.debug("Plugin already loaded: {}", descriptor.getDisplayName());
            return false;
        }
        descriptor.setEnabled(true);
        log.info("Loaded plugin: {} ({})", descriptor.getDisplayName(), descriptor.getPackageName());
        return true;
    }

    @Override
    public void unloadPlugin(GameType gameType) {
        PluginDescriptor descriptor = plugins.get(gameType);
        if (descriptor != null) {
            descriptor.setEnabled(false);
            log.info("Unloaded plugin: {}", descriptor.getDisplayName());
        }
    }

    @Override
    public boolean isPluginLoaded(GameType gameType) {
        PluginDescriptor descriptor = plugins.get(gameType);
        return descriptor != null && descriptor.isEnabled();
    }

    @Override
    public Optional<PluginDescriptor> getPluginForGalaxy(String galaxyName) {
        if (galaxyName == null || galaxyName.trim().isEmpty()) {
            return Optional.empty();
        }
        GameType gameType = GALAXY_TO_GAME_TYPE.get(galaxyName);
        if (gameType == null) {
            return Optional.empty();
        }
        return getPlugin(gameType);
    }

    @Override
    public Optional<GameType> getGameTypeForGalaxy(String galaxyName) {
        if (galaxyName == null || galaxyName.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(GALAXY_TO_GAME_TYPE.get(galaxyName));
    }

    /**
     * Registers all default game plugins at application startup.
     * <p>
     * Earth 2026 is always enabled as the core module.
     * All other plugins are registered but not loaded until a character
     * transitions to their target galaxy.
     * </p>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void registerDefaultPlugins() {
        log.info("Registering default game plugins...");

        // Earth 2026 - Core module, always enabled
        PluginDescriptor earth2026 = new PluginDescriptor(
                GameType.EARTH_2026,
                "Earth 2026",
                "Modern-day RPG hub — starting point for all characters",
                "com.yisy.modules.doramcu.earth2026"
        );
        earth2026.setRequiresGalaxyTransition(false);
        earth2026.setEnabled(true);
        registerPlugin(earth2026);

        // DoraMCU 4X - Core galaxy management
        PluginDescriptor dora4x = new PluginDescriptor(
                GameType.DORAMCU_4X,
                "DoraMCU 4X",
                "4X empire strategy — build and manage your galactic empire",
                "com.yisy.modules.doramcu"
        );
        dora4x.setTargetGalaxy(GalaxyFactory.GAME_GALAXY_NAME);
        registerPlugin(dora4x);

        // Traveller - Space-faring RPG
        PluginDescriptor traveller = new PluginDescriptor(
                GameType.TRAVELLER,
                "Traveller",
                "Classic space-faring RPG with careers, skills, and exploration",
                "com.yisy.modules.doramcu.traveller"
        );
        registerPlugin(traveller);

        // Captain's Log - Star Trek RPG
        PluginDescriptor captainsLog = new PluginDescriptor(
                GameType.CAPTAINS_LOG,
                "Captain's Log",
                "Solo Star Trek Adventures RPG with missions and character development",
                "com.yisy.modules.doramcu.captainslog"
        );
        registerPlugin(captainsLog);

        // D&D 5e - Fantasy RPG
        PluginDescriptor dnd5e = new PluginDescriptor(
                GameType.DND_5E,
                "Dungeons & Dragons 5e",
                "Fantasy RPG with character creation, combat, spells, and adventures",
                "com.yisy.modules.doramcu.dnd5e"
        );
        registerPlugin(dnd5e);

        // Third Reich - WWII combat
        PluginDescriptor thirdReich = new PluginDescriptor(
                GameType.THIRD_REICH,
                "Third Reich 3R4",
                "WWII ground combat engine",
                "com.yisy.modules.doramcu.thirdreich"
        );
        registerPlugin(thirdReich);

        log.info("Registered {} game plugins (Earth 2026 enabled as core module)", plugins.size());
    }
}
