package com.yisy.modules.doramcu.daoc.config;

import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Dark Age of Camelot tech level adapter.
 * <p>
 * Maps generic tech-level modules to Dark Age of Camelot lore-appropriate names and stats.
 * DaOC spans TL3–TL7 (Medieval fantasy through divine magic).
 * </p>
 * <p>
 * Tech level mapping:
 * <ul>
 *   <li>TL3: Medieval — swords, shields, castles</li>
 *   <li>TL4: Renaissance — crossbows, trebuchets</li>
 *   <li>TL5: High Magic — spell towers, enchantments</li>
 *   <li>TL6: Divine — god blessings, holy artifacts</li>
 *   <li>TL7: Mythical — legendary weapons, realm gates</li>
 * </ul>
 * </p>
 */
@Component
public class DaocTechLevelAdapter implements TechLevelAdapter {

    private static final Map<String, String> LORE_NAMES = new HashMap<>();
    private static final Map<String, Map<String, Object>> LORE_STATS = new HashMap<>();

    static {
        // === TL3: Medieval Era ===
        putLore(TechLevel.TL3, ModuleCategory.SETTLEMENT, "COLONY", "Village",
                Map.of("hp", 180, "capacity", 400, "garrison", 40));
        putLore(TechLevel.TL3, ModuleCategory.SETTLEMENT, "BARRACKS", "Barracks",
                Map.of("hp", 220, "capacity", 120, "garrison", 60));
        putLore(TechLevel.TL3, ModuleCategory.INDUSTRY, "WEARONS_FACTORY", "Blacksmith",
                Map.of("output", 22, "weapons", 12));
        putLore(TechLevel.TL3, ModuleCategory.VEHICLE, "ARMORED_TRUCK", "Supply Wagon",
                Map.of("speed", 4, "capacity", 12, "armor", 6));
        putLore(TechLevel.TL3, ModuleCategory.DEFENSE, "PERIMETER_FORTIFICATION", "Stone Wall",
                Map.of("ac", 7, "hp", 250));
        putLore(TechLevel.TL3, ModuleCategory.DEFENSE, "WATCHTOWER", "Watchtower",
                Map.of("ac", 5, "hp", 180, "visionRange", 5));

        // === TL4: Renaissance ===
        putLore(TechLevel.TL4, ModuleCategory.SETTLEMENT, "PLANETARY_BASE", "Fortress",
                Map.of("hp", 600, "capacity", 1800, "garrison", 250));
        putLore(TechLevel.TL4, ModuleCategory.INDUSTRY, "VEHICLE_PLANT", "Armorer",
                Map.of("output", 35, "vehicles", 6));
        putLore(TechLevel.TL4, ModuleCategory.VEHICLE, "TANK", "Trebuchet",
                Map.of("speed", 2, "capacity", 4, "armor", 20, "cannonDamage", "5d6"));
        putLore(TechLevel.TL4, ModuleCategory.DEFENSE, "ANTI_TANK_BATTERY", "Crossbow Emplacement",
                Map.of("ac", 11, "hp", 280, "attackDamage", "4d6", "range", 9));

        // === TL5: High Magic ===
        putLore(TechLevel.TL5, ModuleCategory.SETTLEMENT, "PLANETARY_BASE", "Magical Citadel",
                Map.of("hp", 1200, "capacity", 3500, "garrison", 600));
        putLore(TechLevel.TL5, ModuleCategory.SETTLEMENT, "CIVILIAN_HABITAT", "Mage Tower",
                Map.of("hp", 550, "capacity", 250));
        putLore(TechLevel.TL5, ModuleCategory.INDUSTRY, "WEARONS_FACTORY", "Enchanted Forge",
                Map.of("output", 55, "weapons", 35));
        putLore(TechLevel.TL5, ModuleCategory.DEFENSE, "PLANETARY_BATTERY", "Spell Tower",
                Map.of("ac", 19, "hp", 650, "attackDamage", "6d6", "range", 12));
        putLore(TechLevel.TL5, ModuleCategory.COMMUNICATION, "RADAR_STATION", "Scrying Pool",
                Map.of("range", 120, "detectionRange", 90));

        // === TL6: Divine ===
        putLore(TechLevel.TL6, ModuleCategory.SETTLEMENT, "MOBILE_INFANTRY_BASE", "Temple of the Gods",
                Map.of("hp", 3500, "capacity", 12000, "garrison", 2500));
        putLore(TechLevel.TL6, ModuleCategory.VEHICLE, "WARSHIP", "Flying Fortress",
                Map.of("speed", 7, "capacity", 6000, "armor", 55, "weaponDamage", "9d6"));
        putLore(TechLevel.TL6, ModuleCategory.DEFENSE, "PLANETARY_SHIELD", "Divine Shield",
                Map.of("ac", 30, "hp", 3000, "shieldHP", 6000));

        // === TL7: Mythical ===
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "FEDERATED_CAPITAL", "Realm Gate",
                Map.of("hp", 10000, "capacity", 60000, "tradeBonus", 10));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "WARSHIP", "Dragon Mount",
                Map.of("speed", 10, "capacity", 25000, "armor", 90, "weaponDamage", "18d6"));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "PLANETARY_SHIELD", "Realm Barrier",
                Map.of("ac", 42, "hp", 8000, "shieldHP", 16000));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "MECH_FORTRESS", "Legendary Fortress",
                Map.of("ac", 36, "hp", 5000, "garrison", 1500, "turretDamage", "14d6"));
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
        return TechLevel.TL3;
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
