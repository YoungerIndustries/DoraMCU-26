package com.yisy.modules.doramcu.captainslog.config;

import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Captain's Log tech level adapter.
 * <p>
 * Maps generic tech-level modules to Star Trek: Captain's Log universe lore-appropriate names and stats.
 * Captain's Log supports TL6–TL10 (Information Age through Post-Human).
 * </p>
 */
@Component
public class CaptainsLogTechLevelAdapter implements TechLevelAdapter {

    private static final Map<String, String> LORE_NAMES = new HashMap<>();
    private static final Map<String, Map<String, Object>> LORE_STATS = new HashMap<>();

    static {
        // === TL6: Information Age (Early Federation era) ===
        putLore(TechLevel.TL6, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST", "Federation Outpost",
                Map.of("pop", 5000, "security", 8, "tradeBonus", 10));
        putLore(TechLevel.TL6, ModuleCategory.SETTLEMENT, "ORBITAL_HABITAT", "Orbital Colony",
                Map.of("pop", 20000, "security", 9));
        putLore(TechLevel.TL6, ModuleCategory.INDUSTRY, "FUSION_REACTOR", "Fusion Power Plant",
                Map.of("output", 800, "energy", 800));
        putLore(TechLevel.TL6, ModuleCategory.INDUSTRY, "ASTEROID_MINE", "Asteroid Mining Operation",
                Map.of("output", 400, "minerals", 300));
        putLore(TechLevel.TL6, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Sublight Transport",
                Map.of("speed", 800, "cargo", 150));
        putLore(TechLevel.TL6, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Lunar Facility",
                Map.of("pop", 500, "security", 7));
        putLore(TechLevel.TL6, ModuleCategory.DEFENSE, "WALL", "Perimeter Fence",
                Map.of("defense", 5, "health", 100));
        putLore(TechLevel.TL6, ModuleCategory.WEAPON, "RIFLE", "Pulse Pistol",
                Map.of("damage", 8, "range", 50));
        putLore(TechLevel.TL6, ModuleCategory.ARMOR, "LIGHT_ARMOR", "Environmental Suit",
                Map.of("defense", 3, "speedPenalty", 0));
        putLore(TechLevel.TL6, ModuleCategory.NPC, "MERCHANT", "Federation Trader",
                Map.of("credits", 500, "reputation", 50));
        putLore(TechLevel.TL6, ModuleCategory.MONSTER, "BANDIT", "Space Pirate",
                Map.of("hp", 30, "attack", 8, "defense", 3));
        putLore(TechLevel.TL6, ModuleCategory.ANIMAL, "COW", "Terran Bovine",
                Map.of("milkYield", 20, "meatYield", 200, "type", "Livestock", "size", "Large"));

        // === TL7: Interplanetary (Federation expansion) ===
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST", "Starbase Outpost",
                Map.of("pop", 15000, "security", 10, "tradeBonus", 20));
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "ORBITAL_HABITAT", "Orbital Station",
                Map.of("pop", 80000, "security", 12));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "FUSION_REACTOR", "Warp Core Facility",
                Map.of("output", 1500, "energy", 1500));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "ASTEROID_MINE", "Deep Space Mining",
                Map.of("output", 800, "minerals", 600));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Warp-capable Shuttle",
                Map.of("speed", 2000, "cargo", 300));
        putLore(TechLevel.TL7, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Deep Space Facility",
                Map.of("pop", 2000, "security", 10));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "WALL", "Deflector Shield",
                Map.of("defense", 10, "health", 500));
        putLore(TechLevel.TL7, ModuleCategory.WEAPON, "RIFLE", "Type-2 Phaser",
                Map.of("damage", 15, "range", 100));
        putLore(TechLevel.TL7, ModuleCategory.ARMOR, "LIGHT_ARMOR", "Phase Damper Vest",
                Map.of("defense", 6, "speedPenalty", 0));
        putLore(TechLevel.TL7, ModuleCategory.NPC, "MERCHANT", "Stellar Cartographer",
                Map.of("credits", 2000, "reputation", 75));
        putLore(TechLevel.TL7, ModuleCategory.MONSTER, "BANDIT", "Romulan Raider",
                Map.of("hp", 60, "attack", 15, "defense", 8));
        putLore(TechLevel.TL7, ModuleCategory.ANIMAL, "COW", "Genetically Modified Bovine",
                Map.of("milkYield", 35, "meatYield", 300, "type", "Bio-Engineered", "size", "Large"));

        // === TL8: Interstellar (Advanced Federation) ===
        putLore(TechLevel.TL8, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST", "Federation Colony",
                Map.of("pop", 50000, "security", 12, "tradeBonus", 30));
        putLore(TechLevel.TL8, ModuleCategory.SETTLEMENT, "ORBITAL_HABITAT", "Hab Ring",
                Map.of("pop", 200000, "security", 15));
        putLore(TechLevel.TL8, ModuleCategory.INDUSTRY, "FUSION_REACTOR", "Antimatter Reactor",
                Map.of("output", 3000, "energy", 3000));
        putLore(TechLevel.TL8, ModuleCategory.INDUSTRY, "ASTEROID_MINE", "Dyson Swarm Node",
                Map.of("output", 2000, "minerals", 1500));
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Warp 6 Cruiser",
                Map.of("speed", 5000, "cargo", 1000));
        putLore(TechLevel.TL8, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Starbase",
                Map.of("pop", 10000, "security", 15));
        putLore(TechLevel.TL8, ModuleCategory.DEFENSE, "WALL", "Planetary Shield Generator",
                Map.of("defense", 20, "health", 2000));
        putLore(TechLevel.TL8, ModuleCategory.WEAPON, "RIFLE", "Type-3 Phaser Rifle",
                Map.of("damage", 25, "range", 200));
        putLore(TechLevel.TL8, ModuleCategory.ARMOR, "LIGHT_ARMOR", "Deflector Armor",
                Map.of("defense", 10, "speedPenalty", 0));
        putLore(TechLevel.TL8, ModuleCategory.NPC, "MERCHANT", "Federation Commodore",
                Map.of("credits", 10000, "reputation", 90));
        putLore(TechLevel.TL8, ModuleCategory.MONSTER, "BANDIT", "Klingon Bird of Prey",
                Map.of("hp", 150, "attack", 30, "defense", 15));
        putLore(TechLevel.TL8, ModuleCategory.ANIMAL, "COW", "Synthetic Livestock",
                Map.of("milkYield", 60, "meatYield", 500, "type", "Synthetic", "size", "Large"));

        // === TL9: Galactic (Empire-scale) ===
        putLore(TechLevel.TL9, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST", "Federation World",
                Map.of("pop", 500000, "security", 15, "tradeBonus", 50));
        putLore(TechLevel.TL9, ModuleCategory.SETTLEMENT, "ORBITAL_HABITAT", "Megastructure",
                Map.of("pop", 1000000, "security", 20));
        putLore(TechLevel.TL9, ModuleCategory.INDUSTRY, "FUSION_REACTOR", "Singularity Engine",
                Map.of("output", 10000, "energy", 10000));
        putLore(TechLevel.TL9, ModuleCategory.INDUSTRY, "ASTEROID_MINE", "Planet Cracker",
                Map.of("output", 10000, "minerals", 8000));
        putLore(TechLevel.TL9, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Warp 9 Dreadnought",
                Map.of("speed", 10000, "cargo", 5000));
        putLore(TechLevel.TL9, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Galactic Hub",
                Map.of("pop", 100000, "security", 20));
        putLore(TechLevel.TL9, ModuleCategory.DEFENSE, "WALL", "Dimensional Barrier",
                Map.of("defense", 40, "health", 10000));
        putLore(TechLevel.TL9, ModuleCategory.WEAPON, "RIFLE", "Quantum Disruptor",
                Map.of("damage", 50, "range", 500));
        putLore(TechLevel.TL9, ModuleCategory.ARMOR, "LIGHT_ARMOR", "Temporal Shield",
                Map.of("defense", 20, "speedPenalty", 0));
        putLore(TechLevel.TL9, ModuleCategory.NPC, "MERCHANT", "Federation Admiral",
                Map.of("credits", 50000, "reputation", 100));
        putLore(TechLevel.TL9, ModuleCategory.MONSTER, "BANDIT", "Dominion Warship",
                Map.of("hp", 500, "attack", 60, "defense", 30));
        putLore(TechLevel.TL9, ModuleCategory.ANIMAL, "COW", "Holographic Livestock",
                Map.of("milkYield", 100, "meatYield", 1000, "type", "Holographic", "size", "Large"));

        // === TL10: Post-Human (Transcendent Federation) ===
        putLore(TechLevel.TL10, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST", "Transcendent World",
                Map.of("pop", 5000000, "security", 25, "tradeBonus", 100));
        putLore(TechLevel.TL10, ModuleCategory.SETTLEMENT, "ORBITAL_HABITAT", "Dyson Sphere",
                Map.of("pop", 10000000, "security", 30));
        putLore(TechLevel.TL10, ModuleCategory.INDUSTRY, "FUSION_REACTOR", "Vacuum Energy Tap",
                Map.of("output", 100000, "energy", 100000));
        putLore(TechLevel.TL10, ModuleCategory.INDUSTRY, "ASTEROID_MINE", "Matter Replicator",
                Map.of("output", 100000, "minerals", 100000));
        putLore(TechLevel.TL10, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Transwarp Cruiser",
                Map.of("speed", 50000, "cargo", 50000));
        putLore(TechLevel.TL10, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Reality Anchor",
                Map.of("pop", 1000000, "security", 30));
        putLore(TechLevel.TL10, ModuleCategory.DEFENSE, "WALL", "Reality Firewall",
                Map.of("defense", 100, "health", 100000));
        putLore(TechLevel.TL10, ModuleCategory.WEAPON, "RIFLE", "Reality Eraser",
                Map.of("damage", 200, "range", 10000));
        putLore(TechLevel.TL10, ModuleCategory.ARMOR, "LIGHT_ARMOR", "Existence Shield",
                Map.of("defense", 50, "speedPenalty", 0));
        putLore(TechLevel.TL10, ModuleCategory.NPC, "MERCHANT", "Transcendent Being",
                Map.of("credits", 1000000, "reputation", 100));
        putLore(TechLevel.TL10, ModuleCategory.MONSTER, "BANDIT", "Borg Cube",
                Map.of("hp", 5000, "attack", 200, "defense", 100));
        putLore(TechLevel.TL10, ModuleCategory.ANIMAL, "COW", "Pure Energy Lifeform",
                Map.of("milkYield", 1000, "meatYield", 10000, "type", "Energy", "size", "Variable"));
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
        return TechLevel.TL10;
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
