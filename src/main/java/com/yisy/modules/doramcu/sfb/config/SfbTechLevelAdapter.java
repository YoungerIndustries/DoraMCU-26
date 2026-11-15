package com.yisy.modules.doramcu.sfb.config;

import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Star Fleet Bridge tech level adapter.
 * <p>
 * Maps generic tech-level modules to Star Fleet Bridge universe lore-appropriate names and stats.
 * SFB supports TL6–TL12 (Information Age through Post-Singularity).
 * </p>
 */
@Component
public class SfbTechLevelAdapter implements TechLevelAdapter {

    private static final Map<String, String> LORE_NAMES = new HashMap<>();
    private static final Map<String, Map<String, Object>> LORE_STATS = new HashMap<>();

    static {
        // === TL6: Information Age (Early Starfleet) ===
        putLore(TechLevel.TL6, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST", "Starfleet Outpost",
                Map.of("pop", 5000, "security", 8, "tradeBonus", 10));
        putLore(TechLevel.TL6, ModuleCategory.SETTLEMENT, "ORBITAL_HABITAT", "Orbital Station",
                Map.of("pop", 20000, "security", 10));
        putLore(TechLevel.TL6, ModuleCategory.INDUSTRY, "FUSION_REACTOR", "Fusion Reactor",
                Map.of("output", 800, "energy", 800));
        putLore(TechLevel.TL6, ModuleCategory.INDUSTRY, "ASTEROID_MINE", "Mining Station",
                Map.of("output", 400, "minerals", 300));
        putLore(TechLevel.TL6, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Shuttlecraft",
                Map.of("speed", 800, "cargo", 150));
        putLore(TechLevel.TL6, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Lunar Outpost",
                Map.of("pop", 500, "security", 7));
        putLore(TechLevel.TL6, ModuleCategory.DEFENSE, "WALL", "Perimeter Shield",
                Map.of("defense", 5, "health", 100));
        putLore(TechLevel.TL6, ModuleCategory.WEAPON, "RIFLE", "Type-1 Phaser",
                Map.of("damage", 8, "range", 50));
        putLore(TechLevel.TL6, ModuleCategory.ARMOR, "LIGHT_ARMOR", "Environmental Suit",
                Map.of("defense", 3, "speedPenalty", 0));
        putLore(TechLevel.TL6, ModuleCategory.NPC, "MERCHANT", "Starfleet Ensign",
                Map.of("credits", 500, "reputation", 50));
        putLore(TechLevel.TL6, ModuleCategory.MONSTER, "BANDIT", "Space Pirate",
                Map.of("hp", 30, "attack", 8, "defense", 3));
        putLore(TechLevel.TL6, ModuleCategory.ANIMAL, "COW", "Terran Bovine",
                Map.of("milkYield", 20, "meatYield", 200, "type", "Livestock", "size", "Large"));

        // === TL7: Interplanetary (Federation expansion) ===
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST", "Starbase",
                Map.of("pop", 15000, "security", 10, "tradeBonus", 20));
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "ORBITAL_HABITAT", "Deep Space Station",
                Map.of("pop", 80000, "security", 12));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "FUSION_REACTOR", "Warp Core",
                Map.of("output", 1500, "energy", 1500));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "ASTEROID_MINE", "Deep Space Mining",
                Map.of("output", 800, "minerals", 600));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Constitution-class",
                Map.of("speed", 2000, "cargo", 300));
        putLore(TechLevel.TL7, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Deep Space Facility",
                Map.of("pop", 2000, "security", 10));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "WALL", "Deflector Shield",
                Map.of("defense", 10, "health", 500));
        putLore(TechLevel.TL7, ModuleCategory.WEAPON, "RIFLE", "Type-2 Phaser",
                Map.of("damage", 15, "range", 100));
        putLore(TechLevel.TL7, ModuleCategory.ARMOR, "LIGHT_ARMOR", "Phase Damper Vest",
                Map.of("defense", 6, "speedPenalty", 0));
        putLore(TechLevel.TL7, ModuleCategory.NPC, "MERCHANT", "Starfleet Lieutenant",
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
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Galaxy-class",
                Map.of("speed", 5000, "cargo", 1000));
        putLore(TechLevel.TL8, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Starbase",
                Map.of("pop", 10000, "security", 15));
        putLore(TechLevel.TL8, ModuleCategory.DEFENSE, "WALL", "Planetary Shield",
                Map.of("defense", 20, "health", 2000));
        putLore(TechLevel.TL8, ModuleCategory.WEAPON, "RIFLE", "Type-3 Phaser Rifle",
                Map.of("damage", 25, "range", 200));
        putLore(TechLevel.TL8, ModuleCategory.ARMOR, "LIGHT_ARMOR", "Deflector Armor",
                Map.of("defense", 10, "speedPenalty", 0));
        putLore(TechLevel.TL8, ModuleCategory.NPC, "MERCHANT", "Starfleet Commander",
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
        putLore(TechLevel.TL9, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Sovereign-class",
                Map.of("speed", 10000, "cargo", 5000));
        putLore(TechLevel.TL9, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Galactic Hub",
                Map.of("pop", 100000, "security", 20));
        putLore(TechLevel.TL9, ModuleCategory.DEFENSE, "WALL", "Dimensional Barrier",
                Map.of("defense", 40, "health", 10000));
        putLore(TechLevel.TL9, ModuleCategory.WEAPON, "RIFLE", "Quantum Disruptor",
                Map.of("damage", 50, "range", 500));
        putLore(TechLevel.TL9, ModuleCategory.ARMOR, "LIGHT_ARMOR", "Temporal Shield",
                Map.of("defense", 20, "speedPenalty", 0));
        putLore(TechLevel.TL9, ModuleCategory.NPC, "MERCHANT", "Starfleet Captain",
                Map.of("credits", 50000, "reputation", 100));
        putLore(TechLevel.TL9, ModuleCategory.MONSTER, "BANDIT", "Dominion Warship",
                Map.of("hp", 500, "attack", 60, "defense", 30));
        putLore(TechLevel.TL9, ModuleCategory.ANIMAL, "COW", "Holographic Livestock",
                Map.of("milkYield", 100, "meatYield", 1000, "type", "Holographic", "size", "Large"));

        // === TL10: Post-Human ===
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
        putLore(TechLevel.TL10, ModuleCategory.NPC, "MERCHANT", "Starfleet Admiral",
                Map.of("credits", 1000000, "reputation", 100));
        putLore(TechLevel.TL10, ModuleCategory.MONSTER, "BANDIT", "Borg Cube",
                Map.of("hp", 5000, "attack", 200, "defense", 100));
        putLore(TechLevel.TL10, ModuleCategory.ANIMAL, "COW", "Pure Energy Lifeform",
                Map.of("milkYield", 1000, "meatYield", 10000, "type", "Energy", "size", "Variable"));

        // === TL11: Transcendent ===
        putLore(TechLevel.TL11, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST", "Dimensional Hub",
                Map.of("pop", 50000000, "security", 40, "tradeBonus", 200));
        putLore(TechLevel.TL11, ModuleCategory.SETTLEMENT, "ORBITAL_HABITAT", "Universe Seed",
                Map.of("pop", 100000000, "security", 50));
        putLore(TechLevel.TL11, ModuleCategory.INDUSTRY, "FUSION_REACTOR", "Dimensional Tap",
                Map.of("output", 1000000, "energy", 1000000));
        putLore(TechLevel.TL11, ModuleCategory.INDUSTRY, "ASTEROID_MINE", "Reality Forger",
                Map.of("output", 1000000, "minerals", 1000000));
        putLore(TechLevel.TL11, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Dimensional Ship",
                Map.of("speed", 100000, "cargo", 100000));
        putLore(TechLevel.TL11, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Dimensional Gateway",
                Map.of("pop", 10000000, "security", 50));
        putLore(TechLevel.TL11, ModuleCategory.DEFENSE, "WALL", "Dimensional Lock",
                Map.of("defense", 200, "health", 1000000));
        putLore(TechLevel.TL11, ModuleCategory.WEAPON, "RIFLE", "Dimensional Weapon",
                Map.of("damage", 1000, "range", 100000));
        putLore(TechLevel.TL11, ModuleCategory.ARMOR, "LIGHT_ARMOR", "Dimensional Armor",
                Map.of("defense", 100, "speedPenalty", 0));
        putLore(TechLevel.TL11, ModuleCategory.NPC, "MERCHANT", "Q-Continuum Being",
                Map.of("credits", 10000000, "reputation", 100));
        putLore(TechLevel.TL11, ModuleCategory.MONSTER, "BANDIT", "Dimensional Predator",
                Map.of("hp", 50000, "attack", 1000, "defense", 500));
        putLore(TechLevel.TL11, ModuleCategory.ANIMAL, "COW", "Dimensional Creature",
                Map.of("milkYield", 10000, "meatYield", 100000, "type", "Dimensional", "size", "Variable"));

        // === TL12: Post-Singularity ===
        putLore(TechLevel.TL12, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST", "Multiverse Nexus",
                Map.of("pop", 500000000, "security", 50, "tradeBonus", 500));
        putLore(TechLevel.TL12, ModuleCategory.SETTLEMENT, "ORBITAL_HABITAT", "Multiverse Hub",
                Map.of("pop", 1000000000, "security", 60));
        putLore(TechLevel.TL12, ModuleCategory.INDUSTRY, "FUSION_REACTOR", "Multiverse Engine",
                Map.of("output", 10000000, "energy", 10000000));
        putLore(TechLevel.TL12, ModuleCategory.INDUSTRY, "ASTEROID_MINE", "Universe Harvester",
                Map.of("output", 10000000, "minerals", 10000000));
        putLore(TechLevel.TL12, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Multiverse Voyager",
                Map.of("speed", 1000000, "cargo", 1000000));
        putLore(TechLevel.TL12, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Multiverse Anchor",
                Map.of("pop", 100000000, "security", 60));
        putLore(TechLevel.TL12, ModuleCategory.DEFENSE, "WALL", "Multiverse Barrier",
                Map.of("defense", 500, "health", 10000000));
        putLore(TechLevel.TL12, ModuleCategory.WEAPON, "RIFLE", "Multiverse Eraser",
                Map.of("damage", 10000, "range", 1000000));
        putLore(TechLevel.TL12, ModuleCategory.ARMOR, "LIGHT_ARMOR", "Multiverse Shield",
                Map.of("defense", 200, "speedPenalty", 0));
        putLore(TechLevel.TL12, ModuleCategory.NPC, "MERCHANT", "Transcendent Entity",
                Map.of("credits", 100000000, "reputation", 100));
        putLore(TechLevel.TL12, ModuleCategory.MONSTER, "BANDIT", "Multiverse Threat",
                Map.of("hp", 500000, "attack", 10000, "defense", 5000));
        putLore(TechLevel.TL12, ModuleCategory.ANIMAL, "COW", "Multiverse Lifeform",
                Map.of("milkYield", 100000, "meatYield", 1000000, "type", "Multiversal", "size", "Infinite"));
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
