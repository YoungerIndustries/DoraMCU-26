package com.yisy.modules.doramcu.service.galaxy;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;

import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.gamecore.GameModule;
import com.yisy.modules.doramcu.gamecore.GameModuleRegistry;
import com.yisy.modules.doramcu.gamecore.MechanicType;
import com.yisy.modules.doramcu.gamecore.RulesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that populates a new galaxy with data from rules databases.
 * <p>
 * This service reads from active rules databases (filtered by selected mechanics)
 * and populates the new galaxy with:
 * <ul>
 *   <li>Factions/empires from rules database</li>
 *   <li>Species/races from rules database</li>
 *   <li>Technology levels from rules database</li>
 *   <li>Trade goods and resources from rules database</li>
 * </ul>
 * </p>
 * <p>
 * The population process is configurable per galaxy creation request. When no
 * specific mechanics are selected, all available rules database entries are used.
 * </p>
 */
@Service
public class RulesDatabaseGalaxyPopulator {

    private static final Logger log = LoggerFactory.getLogger(RulesDatabaseGalaxyPopulator.class);

    private final GameModuleRegistry gameModuleRegistry;

    public RulesDatabaseGalaxyPopulator(GameModuleRegistry gameModuleRegistry) {
        this.gameModuleRegistry = gameModuleRegistry;
    }

    /**
     * Populate a galaxy with data from rules databases based on selected mechanics.
     *
     * @param galaxyName the name of the galaxy to populate
     * @param mechanics  the set of mechanics to use for filtering (null for all)
     * @return the population result with summary of populated data
     */
    public PopulationResult populateGalaxy(String galaxyName, Set<MechanicType> mechanics) {
        log.info("Populating galaxy '{}' with rules database data, mechanics: {}", galaxyName, mechanics);

        PopulationResult result = new PopulationResult(galaxyName);

        // Get all rules services from registered modules
        List<RulesService> rulesServices = getActiveRulesServices();

        if (rulesServices.isEmpty()) {
            log.warn("No active rules services found for galaxy {}", galaxyName);
            return result;
        }

        // Collect all rules entries, filtered by mechanics if specified
        for (RulesService rulesService : rulesServices) {
            try {
                List<com.yisy.modules.doramcu.shared.model.RulesEntry> entries = rulesService.listRules();
                if (entries != null) {
                    // Filter entries by category/mechanic mapping
                    List<com.yisy.modules.doramcu.shared.model.RulesEntry> filtered = filterEntriesByMechanic(entries, mechanics);
                    result.addEntries(filtered);
                    log.debug("Added {} entries from rules service for galaxy {}", filtered.size(), galaxyName);
                }
            } catch (Exception e) {
                log.error("Error reading rules service for galaxy {}: {}", galaxyName, e.getMessage(), e);
            }
        }

        log.info("Galaxy '{}' populated with {} total entries from {} rules services",
                galaxyName, result.getTotalEntries(), rulesServices.size());

        return result;
    }

    /**
     * Get all active rules services from registered modules.
     *
     * @return list of active rules services
     */
    public List<RulesService> getActiveRulesServices() {
        return gameModuleRegistry.getAllModules().stream()
                .map(GameModule::getRulesService)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Filter rules entries by mechanic type.
     * Maps rule categories to mechanic types.
     *
     * @param entries   the rules entries to filter
     * @param mechanics the mechanics to filter by (null for all)
     * @return filtered list of entries
     */
    public List<com.yisy.modules.doramcu.shared.model.RulesEntry> filterEntriesByMechanic(
            List<com.yisy.modules.doramcu.shared.model.RulesEntry> entries,
            Set<MechanicType> mechanics) {

        if (mechanics == null || mechanics.isEmpty()) {
            return entries;
        }

        return entries.stream()
                .filter(entry -> {
                    if (entry.getCategory() == null) {
                        return true; // Include entries without category
                    }
                    // Map category to mechanic type
                    MechanicType mechanic = mapCategoryToMechanic(entry.getCategory());
                    return mechanic == null || mechanics.contains(mechanic);
                })
                .collect(Collectors.toList());
    }

    /**
     * Map a rules entry category to a mechanic type.
     *
     * @param category the category string
     * @return the matching mechanic type, or null if no match
     */
    public MechanicType mapCategoryToMechanic(String category) {
        if (category == null) {
            return null;
        }
        String lower = category.toLowerCase();
        if (lower.contains("combat") || lower.contains("weapon") || lower.contains("attack")) {
            return MechanicType.COMBAT;
        } else if (lower.contains("trade") || lower.contains("economy") || lower.contains("currency")) {
            return MechanicType.ECONOMY;
        } else if (lower.contains("diplomacy") || lower.contains("alliance") || lower.contains("faction")) {
            return MechanicType.DIPLOMACY;
        } else if (lower.contains("craft") || lower.contains("manufacture") || lower.contains("recipe")) {
            return MechanicType.CRAFTING;
        } else if (lower.contains("explore") || lower.contains("discovery") || lower.contains("frontier")) {
            return MechanicType.EXPLORATION;
        } else if (lower.contains("survival") || lower.contains("hazard") || lower.contains("environment")) {
            return MechanicType.SURVIVAL;
        } else if (lower.contains("character") || lower.contains("skill") || lower.contains("ability")) {
            return MechanicType.RPG;
        } else if (lower.contains("tactical") || lower.contains("tactic") || lower.contains("grid")) {
            return MechanicType.TACTICAL;
        } else if (lower.contains("species") || lower.contains("race") || lower.contains("alien")) {
            return MechanicType.CHARACTER;
        } else if (lower.contains("tech") || lower.contains("technology") || lower.contains("research")) {
            return MechanicType.TECHNOLOGY;
        } else if (lower.contains("spell") || lower.contains("psionic") || lower.contains("supernatural")) {
            return MechanicType.SUPERNATURAL;
        } else if (lower.contains("encounter") || lower.contains("npc") || lower.contains("event")) {
            return MechanicType.ENCOUNTER;
        } else if (lower.contains("campaign") || lower.contains("quest") || lower.contains("adventure")) {
            return MechanicType.CAMPAIGN;
        } else if (lower.contains("inventory") || lower.contains("item") || lower.contains("equipment")) {
            return MechanicType.INVENTORY;
        } else if (lower.contains("empire") || lower.contains("4x") || lower.contains("expand")) {
            return MechanicType.FOUR_X;
        } else if (lower.contains("tycoon") || lower.contains("business") || lower.contains("property")) {
            return MechanicType.TYCOON;
        } else if (lower.contains("estate") || lower.contains("real") || lower.contains("property")) {
            return MechanicType.REAL_ESTATE;
        }
        return null; // Include by default if no match
    }

    /**
     * Result of a galaxy population operation.
     */
    public static class PopulationResult {
        private final String galaxyName;
        private final List<com.yisy.modules.doramcu.shared.model.RulesEntry> allEntries = new ArrayList<>();
        private final Map<String, Integer> categoryCounts = new HashMap<>();

        public PopulationResult(String galaxyName) {
            this.galaxyName = galaxyName;
        }

        public void addEntries(List<com.yisy.modules.doramcu.shared.model.RulesEntry> entries) {
            if (entries != null) {
                allEntries.addAll(entries);
                // Count by category
                for (com.yisy.modules.doramcu.shared.model.RulesEntry entry : entries) {
                    String category = entry.getCategory() != null ? entry.getCategory() : "uncategorized";
                    categoryCounts.merge(category, 1, Integer::sum);
                }
            }
        }

        public String getGalaxyName() {
            return galaxyName;
        }

        public List<com.yisy.modules.doramcu.shared.model.RulesEntry> getAllEntries() {
            return Collections.unmodifiableList(allEntries);
        }

        public int getTotalEntries() {
            return allEntries.size();
        }

        public Map<String, Integer> getCategoryCounts() {
            return Collections.unmodifiableMap(categoryCounts);
        }
    }
}
