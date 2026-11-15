package com.yisy.modules.doramcu.everquest.config;

import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * EverQuest tech level adapter.
 * <p>
 * Maps generic tech-level modules to EverQuest lore-appropriate names and stats.
 * EverQuest spans TL2–TL7 (Primitive through high fantasy magic).
 * </p>
 * <p>
 * Tech level mapping:
 * <ul>
 *   <li>TL2: Primitive — clubs, stones, basic shelters</li>
 *   <li>TL3: Medieval — swords, bows, castles</li>
 *   <li>TL4: Renaissance — crossbows, cannons, advanced fortifications</li>
 *   <li>TL5: High Magic — spell towers, magical artifacts</li>
 *   <li>TL6: God Magic — divine weapons, planar gates</li>
 *   <li>TL7: Primordial — ancient powers, world-shaping magic</li>
 * </ul>
 * </p>
 */
@Component
public class EverQuestTechLevelAdapter implements TechLevelAdapter {

    private static final Map<String, String> LORE_NAMES = new HashMap<>();
    private static final Map<String, Map<String, Object>> LORE_STATS = new HashMap<>();

    static {
        // === TL2: Primitive Era ===
        putLore(TechLevel.TL2, ModuleCategory.SETTLEMENT, "COLONY", "Primitive Camp",
                Map.of("hp", 80, "capacity", 100, "garrison", 10));
        putLore(TechLevel.TL2, ModuleCategory.DEFENSE, "PERIMETER_FORTIFICATION", "Stone Wall",
                Map.of("ac", 4, "hp", 100));
        putLore(TechLevel.TL2, ModuleCategory.COMMUNICATION, "RADIO_NETWORK", "Drum Signal",
                Map.of("range", 10, "encryptionLevel", 1));

        // === TL3: Medieval Era ===
        putLore(TechLevel.TL3, ModuleCategory.SETTLEMENT, "COLONY", "Human Town",
                Map.of("hp", 200, "capacity", 500, "garrison", 50));
        putLore(TechLevel.TL3, ModuleCategory.SETTLEMENT, "BARRACKS", "Guard Barracks",
                Map.of("hp", 250, "capacity", 150, "garrison", 80));
        putLore(TechLevel.TL3, ModuleCategory.INDUSTRY, "WEARONS_FACTORY", "Blacksmith",
                Map.of("output", 25, "weapons", 15));
        putLore(TechLevel.TL3, ModuleCategory.VEHICLE, "ARMORED_TRUCK", "Supply Cart",
                Map.of("speed", 4, "capacity", 15, "armor", 8));
        putLore(TechLevel.TL3, ModuleCategory.DEFENSE, "PERIMETER_FORTIFICATION", "Stone Wall",
                Map.of("ac", 8, "hp", 300));
        putLore(TechLevel.TL3, ModuleCategory.DEFENSE, "WATCHTOWER", "Watchtower",
                Map.of("ac", 6, "hp", 200, "visionRange", 6));

        // === TL4: Renaissance ===
        putLore(TechLevel.TL4, ModuleCategory.SETTLEMENT, "PLANETARY_BASE", "City Keep",
                Map.of("hp", 500, "capacity", 1500, "garrison", 200));
        putLore(TechLevel.TL4, ModuleCategory.INDUSTRY, "VEHICLE_PLANT", "Armorer Workshop",
                Map.of("output", 35, "vehicles", 8));
        putLore(TechLevel.TL4, ModuleCategory.VEHICLE, "TANK", "Siege Ram",
                Map.of("speed", 3, "capacity", 8, "armor", 25, "cannonDamage", "4d6"));
        putLore(TechLevel.TL4, ModuleCategory.DEFENSE, "ANTI_TANK_BATTERY", "Ballista",
                Map.of("ac", 12, "hp", 300, "attackDamage", "5d6", "range", 10));

        // === TL5: High Magic ===
        putLore(TechLevel.TL5, ModuleCategory.SETTLEMENT, "PLANETARY_BASE", "Magical Citadel",
                Map.of("hp", 1000, "capacity", 3000, "garrison", 500));
        putLore(TechLevel.TL5, ModuleCategory.SETTLEMENT, "CIVILIAN_HABITAT", "Mage Tower",
                Map.of("hp", 500, "capacity", 200));
        putLore(TechLevel.TL5, ModuleCategory.INDUSTRY, "WEARONS_FACTORY", "Enchanted Forge",
                Map.of("output", 50, "weapons", 30));
        putLore(TechLevel.TL5, ModuleCategory.DEFENSE, "PLANETARY_BATTERY", "Spell Tower",
                Map.of("ac", 18, "hp", 600, "attackDamage", "6d6", "range", 12));
        putLore(TechLevel.TL5, ModuleCategory.COMMUNICATION, "RADAR_STATION", "Scrying Crystal",
                Map.of("range", 150, "detectionRange", 100));

        // === TL6: God Magic ===
        putLore(TechLevel.TL6, ModuleCategory.SETTLEMENT, "MOBILE_INFANTRY_BASE", "Planar Gate",
                Map.of("hp", 3000, "capacity", 10000, "garrison", 2000));
        putLore(TechLevel.TL6, ModuleCategory.VEHICLE, "WARSHIP", "Flying Fortress",
                Map.of("speed", 8, "capacity", 5000, "armor", 50, "weaponDamage", "8d6"));
        putLore(TechLevel.TL6, ModuleCategory.DEFENSE, "PLANETARY_SHIELD", "Divine Shield",
                Map.of("ac", 28, "hp", 2500, "shieldHP", 5000));

        // === TL7: Primordial ===
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "FEDERATED_CAPITAL", "Primordial Sanctum",
                Map.of("hp", 8000, "capacity", 50000, "tradeBonus", 8));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "WARSHIP", "World Tree",
                Map.of("speed", 6, "capacity", 20000, "armor", 80, "weaponDamage", "15d6"));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "PLANETARY_SHIELD", "Primordial Barrier",
                Map.of("ac", 38, "hp", 6000, "shieldHP", 12000));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "MECH_FORTRESS", "Ancient Ruin",
                Map.of("ac", 32, "hp", 4000, "garrison", 1000, "turretDamage", "12d6"));
    }

    private static void putLore(TechLevel tl, ModuleCategory cat, String genericType,
                                String loreName, Map<String, Object> stats) {
        String key = key(tl, cat, genericType);
        LORE_NAMES.put(key, loreName);
        LORE_STATS.put(key, stats);
    }

    private static String key(TechLevel tl, ModuleCategory cat, String genericType) {
        return tl.name() + "|" + cat.name() + "|" + genericType;
    }

    @Override
    public String getLoreName(TechLevel techLevel, ModuleCategory category, String genericType) {
        if (!supportsTechLevel(techLevel)) return null;
        return LORE_NAMES.getOrDefault(key(techLevel, category, genericType), null);
    }

    @Override
    public Map<String, Object> getLoreStats(TechLevel techLevel, ModuleCategory category, String genericType) {
        if (!supportsTechLevel(techLevel)) return Collections.emptyMap();
        return LORE_STATS.getOrDefault(key(techLevel, category, genericType), Collections.emptyMap());
    }

    @Override
    public TechLevel getMaxTechLevel() {
        return TechLevel.TL7;
    }

    @Override
    public TechLevel getMinTechLevel() {
        return TechLevel.TL2;
    }

    @Override
    public List<String> getAvailableModules(TechLevel techLevel) {
        if (!supportsTechLevel(techLevel)) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (String key : LORE_NAMES.keySet()) {
            if (key.startsWith(techLevel.name() + "|")) {
                String[] parts = key.split("\\|");
                if (parts.length >= 3) {
                    result.add(parts[2]);
                }
            }
        }
        return result;
    }

    @Override
    public List<String> getAvailableModules(TechLevel techLevel, ModuleCategory category) {
        if (!supportsTechLevel(techLevel)) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        String prefix = techLevel.name() + "|" + category.name() + "|";
        for (String key : LORE_NAMES.keySet()) {
            if (key.startsWith(prefix)) {
                result.add(key.substring(prefix.length()));
            }
        }
        return result;
    }
}
