package com.yisy.modules.doramcu.warhammer40k.config;

import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Warhammer 40K tech level adapter.
 * <p>
 * Maps generic tech-level modules to Warhammer 40K lore-appropriate names and stats.
 * Warhammer 40K spans TL6–TL12 (Gothic future through C'tan shards).
 * </p>
 * <p>
 * Tech level mapping:
 * <ul>
 *   <li>TL6: Imperial Guard — lasguns, tanks, artillery</li>
 *   <li>TL7: Adeptus Mechanicus — advanced wargear, dreadnoughts</li>
 *   <li>TL8: Adeptus Astartes — power armor, bolters, terminators</li>
 *   <li>TL9: Necron — gauss weapons, living metal, obelisks</li>
 *   <li>TL10: Eldar — wraithbone, shield domes, webway</li>
 *   <li>TL11: Tyranid — bio-ships, hive mind, synapse</li>
 *   <li>TL12: Chaos/C'tan — warp tech, god machines</li>
 * </ul>
 * </p>
 */
@Component
public class WarhammerTechLevelAdapter implements TechLevelAdapter {

    private static final Map<String, String> LORE_NAMES = new HashMap<>();
    private static final Map<String, Map<String, Object>> LORE_STATS = new HashMap<>();

    static {
        // === TL6: Imperial Guard Era ===
        putLore(TechLevel.TL6, ModuleCategory.SETTLEMENT, "PLANETARY_BASE", "Imperial Fortress Monastery",
                Map.of("hp", 800, "capacity", 3000, "garrison", 1000));
        putLore(TechLevel.TL6, ModuleCategory.SETTLEMENT, "BARRACKS", "Guard Barracks",
                Map.of("hp", 300, "capacity", 500, "garrison", 300));
        putLore(TechLevel.TL6, ModuleCategory.INDUSTRY, "WEARONS_FACTORY", "Lasgun Factory",
                Map.of("output", 50, "weapons", 30));
        putLore(TechLevel.TL6, ModuleCategory.VEHICLE, "TANK", "Leman Russ Battle Tank",
                Map.of("speed", 6, "capacity", 5, "armor", 35, "cannonDamage", "6d6"));
        putLore(TechLevel.TL6, ModuleCategory.DEFENSE, "PERIMETER_FORTIFICATION", "Fortified Bunker",
                Map.of("ac", 15, "hp", 400));
        putLore(TechLevel.TL6, ModuleCategory.DEFENSE, "ANTI_TANK_BATTERY", "Lascannon Battery",
                Map.of("ac", 14, "hp", 200, "attackDamage", "5d6", "range", 10));
        putLore(TechLevel.TL6, ModuleCategory.COMMUNICATION, "RADIO_NETWORK", "Imperial Vox Network",
                Map.of("range", 100, "encryptionLevel", 3));

        // === TL7: Adeptus Mechanicus ===
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "PLANETARY_BASE", "Forge World Outpost",
                Map.of("hp", 1200, "capacity", 5000, "garrison", 2000));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "POWERED_ARMOR_FACTORY", "Artificer Armor Forge",
                Map.of("output", 30, "armorUnits", 15));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "ARMORED_TRUCK", "Troop Transport",
                Map.of("speed", 8, "capacity", 20, "armor", 15));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "INFANTRY_FIGHTING_VEHICLE", "Chimera APC",
                Map.of("speed", 8, "capacity", 10, "armor", 20, "cannonDamage", "3d6"));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "WATCHTOWER", "Sensor Tower",
                Map.of("ac", 12, "hp", 200, "visionRange", 8));
        putLore(TechLevel.TL7, ModuleCategory.INFRASTRUCTURE, "AIRFIELD", "Orbital Drop Platform",
                Map.of("hp", 600, "aircraftCapacity", 10));

        // === TL8: Adeptus Astartes ===
        putLore(TechLevel.TL8, ModuleCategory.SETTLEMENT, "PLANETARY_BASE", "Space Marine Chapter Fortress",
                Map.of("hp", 2000, "capacity", 10000, "garrison", 1000));
        putLore(TechLevel.TL8, ModuleCategory.SETTLEMENT, "MOBILE_INFANTRY_QUARTERS", "Space Marine Barracks",
                Map.of("hp", 800, "capacity", 200, "garrison", 100));
        putLore(TechLevel.TL8, ModuleCategory.INDUSTRY, "WEARONS_FACTORY", "Bolter Weapons Factory",
                Map.of("output", 40, "weapons", 25));
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "POWERED_ARMOR_PROTOTYPE", "Space Marine Power Armor",
                Map.of("speed", 12, "capacity", 1, "armor", 50, "weaponDamage", "6d6"));
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "TANK", "Baneblade Super Heavy Tank",
                Map.of("speed", 4, "capacity", 15, "armor", 60, "cannonDamage", "10d6"));
        putLore(TechLevel.TL8, ModuleCategory.DEFENSE, "PLANETARY_BATTERY", "Heavy Bolter Emplacement",
                Map.of("ac", 20, "hp", 600, "attackDamage", "6d6", "range", 12));
        putLore(TechLevel.TL8, ModuleCategory.DEFENSE, "MISSILE_SILEO", "Missile Launcher Battery",
                Map.of("ac", 18, "hp", 500, "missiles", 12, "range", 50));
        putLore(TechLevel.TL8, ModuleCategory.COMMUNICATION, "ORBITAL_COMMS", "Astropathic Relay",
                Map.of("range", 500, "encryptionLevel", 6));

        // === TL9: Necron ===
        putLore(TechLevel.TL9, ModuleCategory.SETTLEMENT, "PLANETARY_BASE", "Necron Tomb World",
                Map.of("hp", 5000, "capacity", 50000, "garrison", 5000));
        putLore(TechLevel.TL9, ModuleCategory.INDUSTRY, "WARSHIP_YARD", "Necron Construct Forge",
                Map.of("output", 50, "ships", 5));
        putLore(TechLevel.TL9, ModuleCategory.VEHICLE, "MOBILE_INFANTRY_ARMOR", "Necron Immortals",
                Map.of("speed", 10, "capacity", 5, "armor", 60, "weaponDamage", "5d6", "regeneration", true));
        putLore(TechLevel.TL9, ModuleCategory.DEFENSE, "PLANETARY_SHIELD", "Necron Shield Dome",
                Map.of("ac", 30, "hp", 3000, "shieldHP", 6000));
        putLore(TechLevel.TL9, ModuleCategory.DEFENSE, "ORBITAL_BATTERY", "Gauss Flayer Battery",
                Map.of("ac", 25, "hp", 1000, "attackDamage", "10d6", "range", 20));

        // === TL10: Eldar ===
        putLore(TechLevel.TL10, ModuleCategory.SETTLEMENT, "FEDERATED_CAPITAL", "Eldar Webway Portal",
                Map.of("hp", 3000, "capacity", 10000, "tradeBonus", 5));
        putLore(TechLevel.TL10, ModuleCategory.VEHICLE, "WARSHIP", "Eldar Craftworld Ship",
                Map.of("speed", 8, "capacity", 5000, "armor", 70, "weaponDamage", "12d6"));
        putLore(TechLevel.TL10, ModuleCategory.DEFENSE, "PLANETARY_SHIELD", "Eldar Shield Dome",
                Map.of("ac", 35, "hp", 4000, "shieldHP", 8000));
        putLore(TechLevel.TL10, ModuleCategory.COMMUNICATION, "FEDERATION_NETWORK", "Webway Network",
                Map.of("range", 2000, "encryptionLevel", 8, "interplanetary", true));

        // === TL11: Tyranid ===
        putLore(TechLevel.TL11, ModuleCategory.SETTLEMENT, "MOBILE_INFANTRY_BASE", "Hive Fleet Nexus",
                Map.of("hp", 8000, "capacity", 100000, "garrison", 10000));
        putLore(TechLevel.TL11, ModuleCategory.VEHICLE, "WARSHIP", "Bio-ship",
                Map.of("speed", 6, "capacity", 20000, "armor", 80, "weaponDamage", "15d6", "regeneration", true));
        putLore(TechLevel.TL11, ModuleCategory.DEFENSE, "MECH_FORTRESS", "Tyranid Spore Mine",
                Map.of("ac", 28, "hp", 5000, "garrison", 1000, "turretDamage", "12d6"));

        // === TL12: Chaos/C'tan ===
        putLore(TechLevel.TL12, ModuleCategory.SETTLEMENT, "FEDERATED_CAPITAL", "Chaos Fortress",
                Map.of("hp", 20000, "capacity", 500000, "tradeBonus", 10));
        putLore(TechLevel.TL12, ModuleCategory.VEHICLE, "WARSHIP", "Chaos Daemon Prince Ship",
                Map.of("speed", 10, "capacity", 50000, "armor", 150, "weaponDamage", "30d6"));
        putLore(TechLevel.TL12, ModuleCategory.DEFENSE, "PLANETARY_SHIELD", "Warp Shield",
                Map.of("ac", 50, "hp", 10000, "shieldHP", 20000));
        putLore(TechLevel.TL12, ModuleCategory.DEFENSE, "MECH_FORTRESS", "C'tan Shard Fortress",
                Map.of("ac", 45, "hp", 15000, "garrison", 5000, "turretDamage", "20d6"));
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
        return TechLevel.TL12;
    }

    @Override
    public TechLevel getMinTechLevel() {
        return TechLevel.TL6;
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
