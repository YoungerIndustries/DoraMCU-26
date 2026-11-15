package com.yisy.modules.doramcu.warcraft.config;

import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Warcraft tech level adapter.
 * <p>
 * Maps generic tech-level modules to Warcraft lore-appropriate names and stats.
 * Warcraft spans TL3–TL8 (Medieval fantasy through magical superweapons).
 * </p>
 * <p>
 * Tech level mapping:
 * <ul>
 *   <li>TL3: Medieval — swords, bows, basic fortifications</li>
 *   <li>TL4: Gnomish Tech — cannons, mechanical devices</li>
 *   <li>TL5: Elven Magic — arcane towers, druid circles</li>
 *   <li>TL6: Undead Scourge — death knights, necromancy</li>
 *   <li>TL7: Demon Tech — fel energy, portal magic</li>
 *   <li>TL8: Titan/Deity — world destroyers, ancient magic</li>
 * </ul>
 * </p>
 */
@Component
public class WarcraftTechLevelAdapter implements TechLevelAdapter {

    private static final Map<String, String> LORE_NAMES = new HashMap<>();
    private static final Map<String, Map<String, Object>> LORE_STATS = new HashMap<>();

    static {
        // === TL3: Medieval Era ===
        putLore(TechLevel.TL3, ModuleCategory.SETTLEMENT, "COLONY", "Human Village",
                Map.of("hp", 150, "capacity", 300, "garrison", 30));
        putLore(TechLevel.TL3, ModuleCategory.SETTLEMENT, "BARRACKS", "Barracks",
                Map.of("hp", 200, "capacity", 100, "garrison", 50));
        putLore(TechLevel.TL3, ModuleCategory.INDUSTRY, "WEARONS_FACTORY", "Blacksmith",
                Map.of("output", 20, "weapons", 10));
        putLore(TechLevel.TL3, ModuleCategory.VEHICLE, "ARMORED_TRUCK", "Supply Wagon",
                Map.of("speed", 4, "capacity", 10, "armor", 5));
        putLore(TechLevel.TL3, ModuleCategory.DEFENSE, "PERIMETER_FORTIFICATION", "Wooden Wall",
                Map.of("ac", 6, "hp", 200));
        putLore(TechLevel.TL3, ModuleCategory.DEFENSE, "WATCHTOWER", "Lookout Tower",
                Map.of("ac", 5, "hp", 150, "visionRange", 5));
        putLore(TechLevel.TL3, ModuleCategory.COMMUNICATION, "RADIO_NETWORK", "Horn Signal Network",
                Map.of("range", 20, "encryptionLevel", 1));

        // === TL4: Gnomish Technology ===
        putLore(TechLevel.TL4, ModuleCategory.SETTLEMENT, "PLANETARY_BASE", "Gnomish Workshop",
                Map.of("hp", 400, "capacity", 800, "garrison", 100));
        putLore(TechLevel.TL4, ModuleCategory.INDUSTRY, "VEHICLE_PLANT", "Mechanical Workshop",
                Map.of("output", 30, "vehicles", 5));
        putLore(TechLevel.TL4, ModuleCategory.VEHICLE, "TANK", "Siege Engine",
                Map.of("speed", 3, "capacity", 5, "armor", 20, "cannonDamage", "4d6"));
        putLore(TechLevel.TL4, ModuleCategory.DEFENSE, "ANTI_TANK_BATTERY", "Cannon Emplacement",
                Map.of("ac", 10, "hp", 250, "attackDamage", "4d6", "range", 8));
        putLore(TechLevel.TL4, ModuleCategory.INFRASTRUCTURE, "AIRFIELD", "Hot Air Balloon Platform",
                Map.of("hp", 200, "aircraftCapacity", 3));

        // === TL5: Elven Magic ===
        putLore(TechLevel.TL5, ModuleCategory.SETTLEMENT, "PLANETARY_BASE", "Elven Citadel",
                Map.of("hp", 800, "capacity", 2000, "garrison", 300));
        putLore(TechLevel.TL5, ModuleCategory.SETTLEMENT, "CIVILIAN_HABITAT", "Druid Circle",
                Map.of("hp", 400, "capacity", 500));
        putLore(TechLevel.TL5, ModuleCategory.INDUSTRY, "WEARONS_FACTORY", "Arcane Forge",
                Map.of("output", 40, "weapons", 20));
        putLore(TechLevel.TL5, ModuleCategory.VEHICLE, "INFANTRY_FIGHTING_VEHICLE", "Dreadroar",
                Map.of("speed", 8, "capacity", 8, "armor", 25, "cannonDamage", "4d6"));
        putLore(TechLevel.TL5, ModuleCategory.DEFENSE, "PLANETARY_BATTERY", "Arcane Tower",
                Map.of("ac", 15, "hp", 500, "attackDamage", "5d6", "range", 10));
        putLore(TechLevel.TL5, ModuleCategory.COMMUNICATION, "RADAR_STATION", "Scrying Pool",
                Map.of("range", 100, "detectionRange", 80));

        // === TL6: Undead Scourge ===
        putLore(TechLevel.TL6, ModuleCategory.SETTLEMENT, "PLANETARY_BASE", "Necropolis",
                Map.of("hp", 1200, "capacity", 5000, "garrison", 1000));
        putLore(TechLevel.TL6, ModuleCategory.SETTLEMENT, "MOBILE_INFANTRY_QUARTERS", "Crypt",
                Map.of("hp", 600, "capacity", 500, "garrison", 200));
        putLore(TechLevel.TL6, ModuleCategory.INDUSTRY, "POWERED_ARMOR_FACTORY", "Death Knight Forge",
                Map.of("output", 30, "armorUnits", 15));
        putLore(TechLevel.TL6, ModuleCategory.VEHICLE, "POWERED_ARMOR_PROTOTYPE", "Death Knight Armor",
                Map.of("speed", 10, "capacity", 1, "armor", 40, "weaponDamage", "5d6"));
        putLore(TechLevel.TL6, ModuleCategory.DEFENSE, "MISSILE_SILEO", "Death Coil Battery",
                Map.of("ac", 16, "hp", 400, "missiles", 10, "range", 30));

        // === TL7: Demon Technology ===
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "MOBILE_INFANTRY_BASE", "Demon Portal",
                Map.of("hp", 3000, "capacity", 20000, "garrison", 3000));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "WARSHIP", "Doom Voyager",
                Map.of("speed", 6, "capacity", 5000, "armor", 60, "weaponDamage", "10d6"));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "PLANETARY_SHIELD", "Fel Shield",
                Map.of("ac", 25, "hp", 2000, "shieldHP", 4000));
        putLore(TechLevel.TL7, ModuleCategory.COMMUNICATION, "FEDERATION_NETWORK", "Rift Network",
                Map.of("range", 500, "encryptionLevel", 6, "interplanetary", true));

        // === TL8: Titan/Deity ===
        putLore(TechLevel.TL8, ModuleCategory.SETTLEMENT, "FEDERATED_CAPITAL", "Titan Vault",
                Map.of("hp", 10000, "capacity", 100000, "tradeBonus", 10));
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "WARSHIP", "Titan Core",
                Map.of("speed", 8, "capacity", 20000, "armor", 100, "weaponDamage", "20d6"));
        putLore(TechLevel.TL8, ModuleCategory.DEFENSE, "PLANETARY_SHIELD", "Titan Shield",
                Map.of("ac", 40, "hp", 8000, "shieldHP", 15000));
        putLore(TechLevel.TL8, ModuleCategory.DEFENSE, "MECH_FORTRESS", "Ancient Fortress",
                Map.of("ac", 35, "hp", 6000, "garrison", 2000, "turretDamage", "15d6"));
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
        return TechLevel.TL8;
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
