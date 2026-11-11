package com.yisy.modules.doramcu.gateway;

import com.yisy.modules.doramcu.unified.GameType;

import java.util.List;
import java.util.Optional;

/**
 * Interface for managing game plugins in the DoraMCU Plugin Gateway.
 * <p>
 * The Plugin Gateway allows game modules to be registered at startup but loaded
 * lazily when a character transitions from Earth 2026 to a game galaxy. This
 * reduces startup time and memory usage by only loading the plugins that are
 * actually needed.
 * </p>
 */
public interface PluginGateway {

    /**
     * Registers a plugin with the gateway.
     * <p>
     * Called during startup to register all available game plugins.
     * The plugin is not loaded until {@link #loadPlugin(GameType)} is called.
     * </p>
     *
     * @param descriptor the plugin descriptor containing metadata
     */
    void registerPlugin(PluginDescriptor descriptor);

    /**
     * Returns the descriptor for a specific game type.
     *
     * @param gameType the game type to look up
     * @return the plugin descriptor if registered, empty otherwise
     */
    Optional<PluginDescriptor> getPlugin(GameType gameType);

    /**
     * Returns all registered plugins.
     *
     * @return list of all plugin descriptors
     */
    List<PluginDescriptor> getAvailablePlugins();

    /**
     * Returns only the enabled plugins.
     *
     * @return list of enabled plugin descriptors
     */
    List<PluginDescriptor> getEnabledPlugins();

    /**
     * Loads a plugin, making its controllers and services available.
     * <p>
     * In the current implementation, this enables the plugin's configuration
     * properties so that Spring can activate the plugin's beans.
     * </p>
     *
     * @param gameType the game type to load
     * @return true if the plugin was loaded, false if it was already loaded
     */
    boolean loadPlugin(GameType gameType);

    /**
     * Unloads a plugin, disabling its controllers and services.
     *
     * @param gameType the game type to unload
     */
    void unloadPlugin(GameType gameType);

    /**
     * Checks if a plugin is currently loaded (enabled).
     *
     * @param gameType the game type to check
     * @return true if the plugin is enabled
     */
    boolean isPluginLoaded(GameType gameType);

    /**
     * Returns the plugin descriptor for a galaxy name.
     * <p>
     * Used during galaxy transition to determine which plugin to load.
     * </p>
     *
     * @param galaxyName the name of the galaxy
     * @return the plugin descriptor if a plugin operates in this galaxy, empty otherwise
     */
    Optional<PluginDescriptor> getPluginForGalaxy(String galaxyName);

    /**
     * Returns the game type for a galaxy name.
     *
     * @param galaxyName the name of the galaxy
     * @return the game type if known, empty otherwise
     */
    Optional<GameType> getGameTypeForGalaxy(String galaxyName);
}
