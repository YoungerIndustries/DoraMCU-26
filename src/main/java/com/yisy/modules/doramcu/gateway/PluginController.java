package com.yisy.modules.doramcu.gateway;

import com.yisy.modules.doramcu.unified.GameType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for the Plugin Gateway.
 * <p>
 * Provides endpoints for discovering available game plugins,
 * loading/unloading plugins, and checking plugin status.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/plugins")
public class PluginController {

    private final PluginGateway pluginGateway;

    public PluginController(PluginGateway pluginGateway) {
        this.pluginGateway = pluginGateway;
    }

    /**
     * Lists all available game plugins.
     *
     * @return list of plugin descriptors
     */
    @GetMapping
    public ResponseEntity<List<PluginDescriptor>> getAvailablePlugins() {
        return ResponseEntity.ok(pluginGateway.getAvailablePlugins());
    }

    /**
     * Lists only the enabled (loaded) plugins.
     *
     * @return list of enabled plugin descriptors
     */
    @GetMapping("/enabled")
    public ResponseEntity<List<PluginDescriptor>> getEnabledPlugins() {
        return ResponseEntity.ok(pluginGateway.getEnabledPlugins());
    }

    /**
     * Gets a specific plugin by game type.
     *
     * @param gameType the game type (e.g., "TRAVELLER", "CAPTAINS_LOG")
     * @return the plugin descriptor or 404
     */
    @GetMapping("/{gameType}")
    public ResponseEntity<PluginDescriptor> getPlugin(@PathVariable String gameType) {
        try {
            GameType type = GameType.valueOf(gameType.toUpperCase());
            return pluginGateway.getPlugin(type)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Loads a plugin, enabling its controllers and services.
     *
     * @param gameType the game type to load
     * @return status message
     */
    @PostMapping("/{gameType}/load")
    public ResponseEntity<Map<String, Object>> loadPlugin(@PathVariable String gameType) {
        try {
            GameType type = GameType.valueOf(gameType.toUpperCase());
            boolean loaded = pluginGateway.loadPlugin(type);
            if (loaded) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Plugin " + type + " loaded successfully",
                        "gameType", type.name()
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Plugin " + type + " was already loaded",
                        "gameType", type.name()
                ));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid game type: " + gameType
            ));
        }
    }

    /**
     * Unloads a plugin, disabling its controllers and services.
     *
     * @param gameType the game type to unload
     * @return status message
     */
    @PostMapping("/{gameType}/unload")
    public ResponseEntity<Map<String, Object>> unloadPlugin(@PathVariable String gameType) {
        try {
            GameType type = GameType.valueOf(gameType.toUpperCase());
            pluginGateway.unloadPlugin(type);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Plugin " + type + " unloaded",
                    "gameType", type.name()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid game type: " + gameType
            ));
        }
    }

    /**
     * Checks if a plugin is loaded.
     *
     * @param gameType the game type to check
     * @return status message
     */
    @GetMapping("/{gameType}/status")
    public ResponseEntity<Map<String, Object>> getPluginStatus(@PathVariable String gameType) {
        try {
            GameType type = GameType.valueOf(gameType.toUpperCase());
            boolean loaded = pluginGateway.isPluginLoaded(type);
            return ResponseEntity.ok(Map.of(
                    "gameType", type.name(),
                    "loaded", loaded
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid game type: " + gameType
            ));
        }
    }

    /**
     * Gets the plugin for a specific galaxy.
     *
     * @param galaxyName the name of the galaxy
     * @return the plugin descriptor or 404
     */
    @GetMapping("/galaxy/{galaxyName}")
    public ResponseEntity<PluginDescriptor> getPluginForGalaxy(@PathVariable String galaxyName) {
        return pluginGateway.getPluginForGalaxy(galaxyName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
