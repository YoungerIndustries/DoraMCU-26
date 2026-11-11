package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.unified.GameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller that provides game module metadata for the unified UI launcher.
 */
@RestController
@RequestMapping("/api/v1/games")
public class GameModuleController {

    private static final Logger log = LoggerFactory.getLogger(GameModuleController.class);

    // Map of game module IDs to their HTML pages
    private static final Map<String, String> MODULE_PAGES = new HashMap<>();

    static {
        MODULE_PAGES.put("doramcu-4x", "game.html");
        MODULE_PAGES.put("traveller", "traveller-rules.html");
        MODULE_PAGES.put("captains-log", null);
        MODULE_PAGES.put("dnd-5e", null);
        MODULE_PAGES.put("third-reich", null);
        MODULE_PAGES.put("earth-2026", null);
    }

    /**
     * Returns a list of all available game modules with metadata.
     * Used by the unified UI launcher to populate the game cards dashboard.
     *
     * @return List of game module info maps
     */
    @GetMapping("/list")
    public List<Map<String, Object>> listGameModules() {
        log.debug("Game module list requested");
        List<Map<String, Object>> modules = new ArrayList<>();

        for (GameType gameType : GameType.values()) {
            String id = gameType.name().toLowerCase().replace('_', '-');
            String page = MODULE_PAGES.getOrDefault(id, null);
            boolean available = page != null;

            Map<String, Object> module = new HashMap<>();
            module.put("id", id);
            module.put("name", gameType.getDisplayName());
            module.put("description", gameType.getDescription());
            module.put("page", page);
            module.put("available", available);
            modules.add(module);
        }

        return modules;
    }
}
