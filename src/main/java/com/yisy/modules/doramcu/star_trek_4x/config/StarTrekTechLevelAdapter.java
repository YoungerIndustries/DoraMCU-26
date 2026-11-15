package com.yisy.modules.doramcu.star_trek_4x.config;

import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Star Trek 4X tech level adapter.
 * <p>
 * Maps generic tech-level modules to Star Trek universe lore-appropriate names and stats.
 * Star Trek 4X supports TL6–TL10 (Information Age through Post-Human).
 * </p>
 */
@Component
public class StarTrekTechLevelAdapter implements TechLevelAdapter {

    private static final Map<String, String> LORE_NAMES = new HashMap<>();
    private static final Map<String, Map<String, Object>> LORE_STATS = new HashMap<>();

    static {
        // === TL6: Information Age (Early Star Trek — pre-warp colonies) ===
        putLore(TechLevel.TL6, ModuleCategory.SETTLEMENT, "SPACEPORT_CITY", "Starfleet Academy Campus",
                Map.of("pop", 50000, "security", 8, "scienceBonus", 10));
        putLore(TechLevel.TL6, ModuleCategory.SETTLEMENT, "RESEARCH_CAMPUS", "Scientific Research Outpost",
                Map.of("pop", 5000, "security", 7, "scienceBonus", 8));
        putLore(TechLevel.TL6, ModuleCategory.INDUSTRY, "SEMICONDUCTOR_FAB", "Computer Core Fabrication",
                Map.of("output", 300));
        putLore(TechLevel.TL6, ModuleCategory.INDUSTRY, "NUCLEAR_PLANT", "Fusion Power Plant",
                Map.of("output", 500, "energy", 500));
        putLore(TechLevel.TL6, ModuleCategory.INDUSTRY, "ROBOTICS_FACTORY", "Automated Assembly Plant",
                Map.of("output", 200));
        putLore(TechLevel.TL6, ModuleCategory.VEHICLE, "HOVER_TRANSPORT", "Hover Transport",
                Map.of("speed", 100, "cargo", 20));
        putLore(TechLevel.TL6, ModuleCategory.VEHICLE, "ORBITAL_ROCKET", "Orbital Shuttle",
                Map.of("speed", 500, "cargo", 100));
        putLore(TechLevel.TL6, ModuleCategory.VEHICLE, "SUBORBITAL_SHUTTLE", "Atmospheric Shuttle",
                Map.of("speed", 300, "cargo", 50));
        putLore(TechLevel.TL6, ModuleCategory.INFRASTRUCTURE, "ORBITAL_ELEVATOR", "Orbital Elevator",
                Map.of("tradeBonus", 15));
        putLore(TechLevel.TL6, ModuleCategory.INFRASTRUCTURE, "DATA_CENTER", "Data Center",
                Map.of("computePower", 100));
        putLore(TechLevel.TL6, ModuleCategory.INFRASTRUCTURE, "SOLAR_FARM", "Solar Array",
                Map.of("energy", 200));
        putLore(TechLevel.TL6, ModuleCategory.DEFENSE, "MISSILE_SILEO", "Missile Defense Battery",
                Map.of("defense", 12, "weapons", "20d6"));
        putLore(TechLevel.TL6, ModuleCategory.DEFENSE, "SATELLITE_DEFENSE", "Orbital Defense Network",
                Map.of("defense", 10, "range", 10000));
        putLore(TechLevel.TL6, ModuleCategory.COMMUNICATION, "SATELLITE_UPLINK", "Subspace Relay Station",
                Map.of("range", 50000));
        putLore(TechLevel.TL6, ModuleCategory.COMMUNICATION, "FIBER_OPTIC_HUB", "Fiber Optic Hub",
                Map.of("range", 1000));

        // === TL7: Interplanetary (Standard Federation colony era) ===
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST", "Colonial Outpost",
                Map.of("pop", 10000, "security", 9, "tradeBonus", 15));
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "ORBITAL_HABITAT", "Orbital Station",
                Map.of("pop", 50000, "security", 10));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "FUSION_REACTOR", "Fusion Reactor",
                Map.of("output", 1000, "energy", 1000));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "ASTEROID_MINE", "Asteroid Mining Operation",
                Map.of("output", 500, "minerals", 400));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "BIOTECH_LAB", "Bio-Lab",
                Map.of("output", 200, "medicalBonus", 5));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Interplanetary Transport",
                Map.of("speed", 1000, "cargo", 200));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "MARS_ROVER", "Planetary Rover",
                Map.of("speed", 20, "cargo", 10));
        putLore(TechLevel.TL7, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Lunar Outpost",
                Map.of("pop", 1000, "security", 8));
        putLore(TechLevel.TL7, ModuleCategory.INFRASTRUCTURE, "SPACE_ELEVATOR", "Space Elevator",
                Map.of("tradeBonus", 20));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "ORBITAL_GUN", "Orbital Defense Platform",
                Map.of("defense", 15, "weapons", "50d6"));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "PLANETARY_SHIELD", "Planetary Shield Generator",
                Map.of("defense", 20));
        putLore(TechLevel.TL7, ModuleCategory.COMMUNICATION, "DEEP_SPACE_ARRAY", "Deep Space Array",
                Map.of("range", 100000));

        // === TL8: Interstellar (Standard Federation era) ===
        putLore(TechLevel.TL8, ModuleCategory.SETTLEMENT, "SPACE_STATION", "Deep Space Station",
                Map.of("pop", 100000, "security", 12));
        putLore(TechLevel.TL8, ModuleCategory.SETTLEMENT, "DYSON_RING", "Stellar Energy Collector",
                Map.of("pop", 1000000, "security", 15));
        putLore(TechLevel.TL8, ModuleCategory.INDUSTRY, "ANTIMATTER_PLANT", "Antimatter Refinery",
                Map.of("output", 5000, "fuel", 5000));
        putLore(TechLevel.TL8, ModuleCategory.INDUSTRY, "NANOFAB", "Molecular Assembler Plant",
                Map.of("output", 2000));
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "FTL_CRUISER", "Constitution-Class Cruiser",
                Map.of("speed", 5000, "cargo", 500, "weapons", "100d6", "shieldHP", 500));
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "TRANSPORT_SHUTTLE", "Transport Shuttle",
                Map.of("speed", 2000, "cargo", 100));
        putLore(TechLevel.TL8, ModuleCategory.INFRASTRUCTURE, "JUMP_GATE_STATION", "Warp Gate Station",
                Map.of("tradeBonus", 50));
        putLore(TechLevel.TL8, ModuleCategory.INFRASTRUCTURE, "STARLIFT", "Starlift",
                Map.of("tradeBonus", 30));
        putLore(TechLevel.TL8, ModuleCategory.DEFENSE, "ENERGY_WEAPON", "Phaser Array",
                Map.of("defense", 25, "weapons", "200d6"));
        putLore(TechLevel.TL8, ModuleCategory.DEFENSE, "SHIELD_GENERATOR", "Deflector Shield Array",
                Map.of("defense", 30, "shieldHP", 1000));
        putLore(TechLevel.TL8, ModuleCategory.COMMUNICATION, "HYPERNET_RELAY", "Subspace Relay",
                Map.of("range", 1000000));

        // === TL9: Galactic (Advanced Federation / Borg era) ===
        putLore(TechLevel.TL9, ModuleCategory.SETTLEMENT, "STARBASE", "Starbase",
                Map.of("pop", 5000000, "security", 20, "shieldHP", 10000));
        putLore(TechLevel.TL9, ModuleCategory.SETTLEMENT, "WORLD_CITY", "Ecumenopolis",
                Map.of("pop", 50000000, "security", 18, "tradeBonus", 30));
        putLore(TechLevel.TL9, ModuleCategory.INDUSTRY, "STAR_FORGE", "Starfleet Shipyard",
                Map.of("output", 100000));
        putLore(TechLevel.TL9, ModuleCategory.INDUSTRY, "AI_CORE", "Ship Computer Core",
                Map.of("output", 50000, "aiBonus", 10));
        putLore(TechLevel.TL9, ModuleCategory.VEHICLE, "DREADNOUGHT", "Dreadnought-Class Warship",
                Map.of("speed", 10000, "cargo", 5000, "weapons", "500d6", "shieldHP", 5000));
        putLore(TechLevel.TL9, ModuleCategory.VEHICLE, "WORMHOLE_DRIVE", "Wormhole Explorer",
                Map.of("speed", 50000, "cargo", 1000));
        putLore(TechLevel.TL9, ModuleCategory.INFRASTRUCTURE, "WORMHOLE_NETWORK", "Wormhole Network Node",
                Map.of("tradeBonus", 100));
        putLore(TechLevel.TL9, ModuleCategory.INFRASTRUCTURE, "PLANETARY_COMPUTER", "Planetary Computer",
                Map.of("computePower", 1000000));
        putLore(TechLevel.TL9, ModuleCategory.DEFENSE, "PLANET_CRUSHER", "Planetary Defense Grid",
                Map.of("defense", 50, "shieldHP", 20000));
        putLore(TechLevel.TL9, ModuleCategory.COMMUNICATION, "QUANTUM_NET", "Quantum Entanglement Network",
                Map.of("range", 10000000));

        // === TL10: Post-Human (Voyager-era advanced tech) ===
        putLore(TechLevel.TL10, ModuleCategory.SETTLEMENT, "MEGASTRUCTURE_HUB", "Delta Quadrant Hub",
                Map.of("pop", 100000000, "security", 30));
        putLore(TechLevel.TL10, ModuleCategory.INDUSTRY, "STAR_LIFTER", "Stellar Matter Harvester",
                Map.of("output", 500000));
        putLore(TechLevel.TL10, ModuleCategory.INDUSTRY, "REALITY_ENGINE", "Transwarp Core",
                Map.of("output", 1000000));
        putLore(TechLevel.TL10, ModuleCategory.VEHICLE, "WARP_GATE", "Transwarp Gateway",
                Map.of("speed", 100000, "cargo", 100000));
        putLore(TechLevel.TL10, ModuleCategory.INFRASTRUCTURE, "DYSON_SPHERE", "Stellar Collector Sphere",
                Map.of("energy", 1000000));
        putLore(TechLevel.TL10, ModuleCategory.INFRASTRUCTURE, "MATRIOSKA_BRAIN", "Ship Computer Matrix",
                Map.of("computePower", 100000000));
        putLore(TechLevel.TL10, ModuleCategory.DEFENSE, "VOID_SHIELD", "Transwarp Shield",
                Map.of("defense", 100, "shieldHP", 100000));
        putLore(TechLevel.TL10, ModuleCategory.COMMUNICATION, "THOUGHT_NET", "Neural Interface Network",
                Map.of("range", 100000000));

        // ==========================================
        // CROSS-GALAXY CONVERSION CATEGORIES
        // ==========================================

        // --- TREASURE ---
        putLore(TechLevel.TL6, ModuleCategory.TREASURE, "GOLD_COINS", "Stellar Credits",
                Map.of("gpValue", 100, "rarity", "Common", "type", "Currency"));
        putLore(TechLevel.TL6, ModuleCategory.TREASURE, "GEMSTONE", "Dilithium Crystal",
                Map.of("gpValue", 500, "rarity", "Uncommon", "type", "Crystal"));
        putLore(TechLevel.TL7, ModuleCategory.TREASURE, "GOLD_COINS", "Stellar Credits",
                Map.of("gpValue", 1000, "rarity", "Common", "type", "Currency"));
        putLore(TechLevel.TL7, ModuleCategory.TREASURE, "GEMSTONE", "Refined Dilithium",
                Map.of("gpValue", 5000, "rarity", "Rare", "type", "Crystal"));
        putLore(TechLevel.TL8, ModuleCategory.TREASURE, "GOLD_COINS", "Latinum",
                Map.of("gpValue", 10000, "rarity", "Common", "type", "Currency"));
        putLore(TechLevel.TL8, ModuleCategory.TREASURE, "GEMSTONE", "Antimatter Crystal",
                Map.of("gpValue", 50000, "rarity", "Very Rare", "type", "Exotic Matter"));
        putLore(TechLevel.TL9, ModuleCategory.TREASURE, "GOLD_COINS", "Galactic Credits",
                Map.of("gpValue", 100000, "rarity", "Common", "type", "Currency"));
        putLore(TechLevel.TL9, ModuleCategory.TREASURE, "ANCIENT_ARTIFACT", "Ancient Artifact",
                Map.of("gpValue", 500000, "rarity", "Legendary", "type", "Artifact"));
        putLore(TechLevel.TL10, ModuleCategory.TREASURE, "GOLD_COINS", "Universal Credits",
                Map.of("gpValue", 1000000, "rarity", "Common", "type", "Currency"));
        putLore(TechLevel.TL10, ModuleCategory.TREASURE, "ANCIENT_ARTIFACT", "Ancient Alien Relic",
                Map.of("gpValue", 5000000, "rarity", "Artifact", "type", "Relic"));

        // --- WEAPON ---
        putLore(TechLevel.TL6, ModuleCategory.WEAPON, "RIFLE", "Pulse Rifle",
                Map.of("damage", "3d6", "damageType", "Energy", "weight", 4));
        putLore(TechLevel.TL6, ModuleCategory.WEAPON, "HANDGUN", "Pulse Pistol",
                Map.of("damage", "2d6", "damageType", "Energy", "weight", 2));
        putLore(TechLevel.TL7, ModuleCategory.WEAPON, "RIFLE", "Phaser Rifle",
                Map.of("damage", "4d6", "damageType", "Energy", "weight", 3));
        putLore(TechLevel.TL7, ModuleCategory.WEAPON, "HANDGUN", "Phaser Pistol",
                Map.of("damage", "3d6", "damageType", "Energy", "weight", 1));
        putLore(TechLevel.TL8, ModuleCategory.WEAPON, "RIFLE", "Type-2 Phaser",
                Map.of("damage", "5d6", "damageType", "Energy", "weight", 2));
        putLore(TechLevel.TL8, ModuleCategory.WEAPON, "HANDGUN", "Type-3 Phaser",
                Map.of("damage", "4d6", "damageType", "Energy", "weight", 1));
        putLore(TechLevel.TL8, ModuleCategory.WEAPON, "MAGIC_LONGSWORD", "Disruptor Blade",
                Map.of("damage", "3d6", "damageType", "Energy", "weight", 2));
        putLore(TechLevel.TL9, ModuleCategory.WEAPON, "RIFLE", "Photon Torpedo Launcher",
                Map.of("damage", "8d6", "damageType", "Energy", "weight", 10));
        putLore(TechLevel.TL9, ModuleCategory.WEAPON, "HANDGUN", "Quantum Torpedo",
                Map.of("damage", "6d6", "damageType", "Energy", "weight", 5));
        putLore(TechLevel.TL10, ModuleCategory.WEAPON, "RIFLE", "Transwarp Disruptor",
                Map.of("damage", "10d6", "damageType", "Energy", "weight", 1));

        // --- ARMOR ---
        putLore(TechLevel.TL6, ModuleCategory.ARMOR, "MAGIC_PLATE", "Environmental Suit",
                Map.of("ac", 17, "weight", 20, "type", "Environmental"));
        putLore(TechLevel.TL7, ModuleCategory.ARMOR, "MAGIC_PLATE", "Phase Armor",
                Map.of("ac", 18, "weight", 15, "type", "Phase"));
        putLore(TechLevel.TL8, ModuleCategory.ARMOR, "MAGIC_PLATE", "Deflector Shield Vest",
                Map.of("ac", 20, "weight", 10, "type", "Energy Shield", "shieldHP", 100));
        putLore(TechLevel.TL9, ModuleCategory.ARMOR, "MAGIC_PLATE", "Personal Shield Generator",
                Map.of("ac", 22, "weight", 5, "type", "Force Field", "shieldHP", 500));
        putLore(TechLevel.TL10, ModuleCategory.ARMOR, "MAGIC_PLATE", "Transwarp Shield",
                Map.of("ac", 25, "weight", 1, "type", "Transwarp", "shieldHP", 1000));

        // --- POTION ---
        putLore(TechLevel.TL6, ModuleCategory.POTION, "ANTIDOTE", "Hypospray Antidote",
                Map.of("rarity", "Common", "gpValue", 500, "type", "Medical"));
        putLore(TechLevel.TL6, ModuleCategory.POTION, "GREATER_HEALING_POTION", "Medigel",
                Map.of("rarity", "Common", "gpValue", 1000, "healing", "10d4+10"));
        putLore(TechLevel.TL7, ModuleCategory.POTION, "GREATER_HEALING_POTION", "Regeneration Serum",
                Map.of("rarity", "Uncommon", "gpValue", 2000, "healing", "12d4+12"));
        putLore(TechLevel.TL8, ModuleCategory.POTION, "GREATER_HEALING_POTION", "Tricordrazine",
                Map.of("rarity", "Rare", "gpValue", 5000, "healing", "15d4+15"));
        putLore(TechLevel.TL9, ModuleCategory.POTION, "GREATER_HEALING_POTION", "Synthetic Blood",
                Map.of("rarity", "Very Rare", "gpValue", 10000, "healing", "20d4+20"));
        putLore(TechLevel.TL10, ModuleCategory.POTION, "GREATER_HEALING_POTION", "Cellular Regenerator",
                Map.of("rarity", "Legendary", "gpValue", 50000, "healing", "30d4+30"));

        // --- MAGIC_ITEM → Tech Items ---
        putLore(TechLevel.TL6, ModuleCategory.MAGIC_ITEM, "STAFF_OF_POWER", "Tricorder",
                Map.of("rarity", "Common", "type", "Scanner", "scanRange", 1000));
        putLore(TechLevel.TL7, ModuleCategory.MAGIC_ITEM, "STAFF_OF_POWER", "Transporter Emitter",
                Map.of("rarity", "Rare", "type", "Transporter", "range", 10000));
        putLore(TechLevel.TL8, ModuleCategory.MAGIC_ITEM, "STAFF_OF_POWER", "Replicator",
                Map.of("rarity", "Rare", "type", "Replicator", "output", "Unlimited"));
        putLore(TechLevel.TL8, ModuleCategory.MAGIC_ITEM, "WAND_OF_WISDOM", "Ship Computer Interface",
                Map.of("rarity", "Uncommon", "type", "Computer", "intelligenceBonus", 3));
        putLore(TechLevel.TL9, ModuleCategory.MAGIC_ITEM, "STAFF_OF_POWER", "Holodeck Emitter",
                Map.of("rarity", "Very Rare", "type", "Holodeck", "simulations", "Unlimited"));
        putLore(TechLevel.TL10, ModuleCategory.MAGIC_ITEM, "STAFF_OF_POWER", "Transwarp Beacon",
                Map.of("rarity", "Legendary", "type", "Transwarp", "range", "Unlimited"));

        // --- NPC ---
        putLore(TechLevel.TL6, ModuleCategory.NPC, "MERCHANT", "Stellar Trader",
                Map.of("role", "Trade", "alignment", "Neutral", "level", 10));
        putLore(TechLevel.TL6, ModuleCategory.NPC, "GUARD_CAPTAIN", "Security Chief",
                Map.of("role", "Military", "alignment", "Lawful Neutral", "level", 12));
        putLore(TechLevel.TL7, ModuleCategory.NPC, "MERCHANT", "Colonial Trader",
                Map.of("role", "Trade", "alignment", "Neutral", "level", 12));
        putLore(TechLevel.TL7, ModuleCategory.NPC, "WIZARD", "Science Officer",
                Map.of("role", "Scientist", "alignment", "Neutral", "level", 15));
        putLore(TechLevel.TL8, ModuleCategory.NPC, "KNIGHT", "Starfleet Commander",
                Map.of("role", "Military", "alignment", "Lawful Good", "level", 18));
        putLore(TechLevel.TL8, ModuleCategory.NPC, "MERCHANT", "Klingon Merchant",
                Map.of("role", "Trade", "alignment", "Lawful Neutral", "level", 15));
        putLore(TechLevel.TL9, ModuleCategory.NPC, "MERCHANT", "Federation Ambassador",
                Map.of("role", "Diplomat", "alignment", "Lawful Good", "level", 20));
        putLore(TechLevel.TL9, ModuleCategory.NPC, "WIZARD", "Ship AI",
                Map.of("role", "AI", "alignment", "Neutral", "level", 20));
        putLore(TechLevel.TL10, ModuleCategory.NPC, "ARCHMAGE", "Ancient Entity",
                Map.of("role", "Entity", "alignment", "Neutral", "level", 20));

        // --- MONSTER ---
        putLore(TechLevel.TL6, ModuleCategory.MONSTER, "MIND_FLAYER", "Alien Hive Mind",
                Map.of("cr", 15, "hp", 300, "ac", 18, "type", "Alien", "size", "Medium"));
        putLore(TechLevel.TL7, ModuleCategory.MONSTER, "MIND_FLAYER", "Hirogen Hunter",
                Map.of("cr", 18, "hp", 500, "ac", 20, "type", "Alien", "size", "Large"));
        putLore(TechLevel.TL8, ModuleCategory.MONSTER, "ANCIENT_DRAGON", "Sphere Builder",
                Map.of("cr", 25, "hp", 1000, "ac", 25, "type", "Construct", "size", "Gargantuan"));
        putLore(TechLevel.TL9, ModuleCategory.MONSTER, "ANCIENT_DRAGON", "Borg Cube",
                Map.of("cr", 30, "hp", 5000, "ac", 30, "type", "Construct", "size", "Gargantuan", "shieldHP", 5000));
        putLore(TechLevel.TL9, ModuleCategory.MONSTER, "LICH", "Borg Queen",
                Map.of("cr", 28, "hp", 2000, "ac", 28, "type", "Cyborg", "size", "Medium", "shieldHP", 2000));
        putLore(TechLevel.TL10, ModuleCategory.MONSTER, "ANCIENT_DRAGON", "Transwarp Entity",
                Map.of("cr", 40, "hp", 100000, "ac", 40, "type", "Energy", "size", "Colossal"));

        // --- ADVENTURE_GEAR ---
        putLore(TechLevel.TL6, ModuleCategory.ADVENTURE_GEAR, "TORCH", "Photon Emitter",
                Map.of("cost", 50, "weight", 0.5, "duration", "100 hours"));
        putLore(TechLevel.TL6, ModuleCategory.ADVENTURE_GEAR, "RATIONS", "Nutrient Paste",
                Map.of("cost", 10, "weight", 0.5, "duration", "1 day"));
        putLore(TechLevel.TL6, ModuleCategory.ADVENTURE_GEAR, "BACKPACK", "Survival Kit",
                Map.of("cost", 100, "weight", 10, "capacity", "Emergency supplies"));
        putLore(TechLevel.TL7, ModuleCategory.ADVENTURE_GEAR, "BACKPACK", "Environmental Suit",
                Map.of("cost", 5000, "weight", 20, "duration", "72 hours"));
        putLore(TechLevel.TL8, ModuleCategory.ADVENTURE_GEAR, "BACKPACK", "Void Suit",
                Map.of("cost", 10000, "weight", 15, "duration", "168 hours"));
        putLore(TechLevel.TL9, ModuleCategory.ADVENTURE_GEAR, "BACKPACK", "Personal Force Field",
                Map.of("cost", 50000, "weight", 1, "duration", "Unlimited"));

        // --- DUNGEON_ROOM → Ship Rooms ---
        putLore(TechLevel.TL6, ModuleCategory.DUNGEON_ROOM, "CASTLE_HALL", "Command Center",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL6, ModuleCategory.DUNGEON_ROOM, "ARCANE_LAB", "Research Lab",
                Map.of("size", "Large", "light", "Bright"));
        putLore(TechLevel.TL7, ModuleCategory.DUNGEON_ROOM, "CASTLE_HALL", "Ship Bridge",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL7, ModuleCategory.DUNGEON_ROOM, "DUNGEON_CELL", "Airlock",
                Map.of("size", "Small", "light", "Bright"));
        putLore(TechLevel.TL8, ModuleCategory.DUNGEON_ROOM, "CASTLE_HALL", "Starship Bridge",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL8, ModuleCategory.DUNGEON_ROOM, "ARCANE_LAB", "Science Lab",
                Map.of("size", "Large", "light", "Bright"));
        putLore(TechLevel.TL8, ModuleCategory.DUNGEON_ROOM, "DUNGEON_CELL", "Sickbay",
                Map.of("size", "Medium", "light", "Bright"));
        putLore(TechLevel.TL9, ModuleCategory.DUNGEON_ROOM, "CASTLE_HALL", "Starbase Command",
                Map.of("size", "Colossal", "light", "Bright"));
        putLore(TechLevel.TL10, ModuleCategory.DUNGEON_ROOM, "CASTLE_HALL", "Transwarp Hub",
                Map.of("size", "Colossal", "light", "Bright"));

        // --- New sci-fi DUNGEON_ROOM types (TL6-TL10) ---
        putLore(TechLevel.TL6, ModuleCategory.DUNGEON_ROOM, "COMMAND_CENTER", "Mission Control",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL6, ModuleCategory.DUNGEON_ROOM, "RESEARCH_LAB", "Science Laboratory",
                Map.of("size", "Large", "light", "Bright"));
        putLore(TechLevel.TL6, ModuleCategory.DUNGEON_ROOM, "HOVER_DEPOT", "Hover Transport Bay",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL6, ModuleCategory.DUNGEON_ROOM, "ORBITAL_CONTROL", "Orbital Launch Control",
                Map.of("size", "Large", "light", "Bright"));
        putLore(TechLevel.TL6, ModuleCategory.DUNGEON_ROOM, "MEDBAY", "Sickbay",
                Map.of("size", "Medium", "light", "Bright"));
        putLore(TechLevel.TL7, ModuleCategory.DUNGEON_ROOM, "SHIP_BRIDGE", "Starship Bridge",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL7, ModuleCategory.DUNGEON_ROOM, "FUSION_CORE", "Fusion Reactor Room",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL7, ModuleCategory.DUNGEON_ROOM, "DOCKING_BAY", "Docking Bay",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL8, ModuleCategory.DUNGEON_ROOM, "STARSHIP_BRIDGE", "Constitution-Class Bridge",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL8, ModuleCategory.DUNGEON_ROOM, "ANTIMATTER_CORE", "Antimatter Containment",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL8, ModuleCategory.DUNGEON_ROOM, "ENGINEERING", "Main Engineering",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL8, ModuleCategory.DUNGEON_ROOM, "TRANSPORTER_ROOM", "Transporter Room",
                Map.of("size", "Medium", "light", "Bright"));
        putLore(TechLevel.TL8, ModuleCategory.DUNGEON_ROOM, "OBSERVATION_DECK", "Observation Lounge",
                Map.of("size", "Large", "light", "Bright"));
        putLore(TechLevel.TL9, ModuleCategory.DUNGEON_ROOM, "STARBASE_COMMAND", "Starbase Command",
                Map.of("size", "Colossal", "light", "Bright"));
        putLore(TechLevel.TL9, ModuleCategory.DUNGEON_ROOM, "DREADNOUGHT_BRIDGE", "Dreadnought Bridge",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL10, ModuleCategory.DUNGEON_ROOM, "MEGASTRUCTURE_HUB", "Transwarp Hub",
                Map.of("size", "Colossal", "light", "Bright"));

        // --- FURNITURE → Ship Furniture ---
        putLore(TechLevel.TL6, ModuleCategory.FURNITURE, "STONE_THRONE", "Captain's Chair",
                Map.of("hp", 50, "ac", 15, "weight", 100));
        putLore(TechLevel.TL6, ModuleCategory.FURNITURE, "WOODEN_TABLE", "Holo-Table",
                Map.of("hp", 20, "ac", 10, "weight", 50));
        putLore(TechLevel.TL7, ModuleCategory.FURNITURE, "STONE_THRONE", "Command Console",
                Map.of("hp", 100, "ac", 20, "weight", 200));
        putLore(TechLevel.TL7, ModuleCategory.FURNITURE, "WOODEN_TABLE", "Data Console",
                Map.of("hp", 50, "ac", 15, "weight", 100));
        putLore(TechLevel.TL8, ModuleCategory.FURNITURE, "STONE_THRONE", "Captain's Chair",
                Map.of("hp", 200, "ac", 25, "weight", 500));
        putLore(TechLevel.TL8, ModuleCategory.FURNITURE, "WOODEN_TABLE", "Replicator",
                Map.of("hp", 100, "ac", 20, "weight", 200));
        putLore(TechLevel.TL9, ModuleCategory.FURNITURE, "STONE_THRONE", "Admiral's Chair",
                Map.of("hp", 500, "ac", 30, "weight", 1000));

        // --- New sci-fi FURNITURE types (TL6-TL10) ---
        putLore(TechLevel.TL6, ModuleCategory.FURNITURE, "HOLO_TABLE", "Holographic Table",
                Map.of("hp", 20, "ac", 10, "weight", 200));
        putLore(TechLevel.TL6, ModuleCategory.FURNITURE, "CRYO_POD", "Cryogenic Pod",
                Map.of("hp", 100, "ac", 20, "weight", 1000));
        putLore(TechLevel.TL6, ModuleCategory.FURNITURE, "DIAGNOSTIC_BED", "Bio-Bed",
                Map.of("hp", 50, "ac", 15, "weight", 300));
        putLore(TechLevel.TL7, ModuleCategory.FURNITURE, "COMMAND_CHAIR", "Captain's Chair",
                Map.of("hp", 100, "ac", 20, "weight", 400));
        putLore(TechLevel.TL7, ModuleCategory.FURNITURE, "NAVIGATION_CONSOLE", "Navigation Console",
                Map.of("hp", 40, "ac", 18, "weight", 250));
        putLore(TechLevel.TL8, ModuleCategory.FURNITURE, "HOLODECK_EMITTER", "Holodeck Emitter",
                Map.of("hp", 100, "ac", 20, "weight", 1000));
        putLore(TechLevel.TL8, ModuleCategory.FURNITURE, "TRICORDER", "Type-4 Tricorder",
                Map.of("hp", 10, "ac", 15, "weight", 2));
        putLore(TechLevel.TL8, ModuleCategory.FURNITURE, "WARP_CONSOLE", "Warp Drive Console",
                Map.of("hp", 60, "ac", 22, "weight", 300));
        putLore(TechLevel.TL8, ModuleCategory.FURNITURE, "SHIELD_GENERATOR", "Deflector Shield Control",
                Map.of("hp", 200, "ac", 25, "weight", 500));
        putLore(TechLevel.TL9, ModuleCategory.FURNITURE, "AI_INTERFACE", "Ship Computer Interface",
                Map.of("hp", 150, "ac", 28, "weight", 100));
        putLore(TechLevel.TL9, ModuleCategory.FURNITURE, "GALACTIC_MAP", "Star Chart Display",
                Map.of("hp", 50, "ac", 20, "weight", 2000));
        putLore(TechLevel.TL10, ModuleCategory.FURNITURE, "REALITY_CONSOLE", "Transwarp Console",
                Map.of("hp", 500, "ac", 30, "weight", 0));

        // --- TRAP → Security ---
        putLore(TechLevel.TL6, ModuleCategory.TRAP, "PRESSURE_PLATE", "Laser Grid",
                Map.of("dc", 16, "damage", "4d6", "damageType", "Energy"));
        putLore(TechLevel.TL7, ModuleCategory.TRAP, "PRESSURE_PLATE", "Photon Torpedo Trap",
                Map.of("dc", 17, "damage", "5d6", "damageType", "Energy"));
        putLore(TechLevel.TL8, ModuleCategory.TRAP, "PRESSURE_PLATE", "Containment Field",
                Map.of("dc", 18, "damage", "6d6", "damageType", "Energy"));
        putLore(TechLevel.TL9, ModuleCategory.TRAP, "PRESSURE_PLATE", "Shield Disruption",
                Map.of("dc", 20, "damage", "10d6", "damageType", "Energy"));
        putLore(TechLevel.TL10, ModuleCategory.TRAP, "PRESSURE_PLATE", "Transwarp Anomaly",
                Map.of("dc", 25, "damage", "15d6", "damageType", "Energy"));

        // --- New sci-fi TRAP types (TL6-TL10) ---
        putLore(TechLevel.TL6, ModuleCategory.TRAP, "LASER_GRID", "Laser Security Grid",
                Map.of("dc", 16, "damage", "4d6", "damageType", "Energy"));
        putLore(TechLevel.TL6, ModuleCategory.TRAP, "EMP_PULSE", "EMP Disruptor",
                Map.of("dc", 14, "damage", "3d6", "damageType", "Energy"));
        putLore(TechLevel.TL7, ModuleCategory.TRAP, "CONTAINMENT_FIELD", "Containment Field",
                Map.of("dc", 18, "damage", "6d6", "damageType", "Energy"));
        putLore(TechLevel.TL7, ModuleCategory.TRAP, "AUTO_TURRET", "Auto-Turret",
                Map.of("dc", 16, "damage", "4d6", "damageType", "Energy"));
        putLore(TechLevel.TL8, ModuleCategory.TRAP, "PHOTON_TORPEDO", "Photon Torpedo Trap",
                Map.of("dc", 20, "damage", "8d6", "damageType", "Energy"));
        putLore(TechLevel.TL8, ModuleCategory.TRAP, "SHIELD_DISRUPTOR", "Shield Disruptor",
                Map.of("dc", 18, "damage", "6d6", "damageType", "Energy"));
        putLore(TechLevel.TL9, ModuleCategory.TRAP, "ANTIMATTER_BOMB", "Antimatter Detonator",
                Map.of("dc", 22, "damage", "12d6", "damageType", "Energy"));
        putLore(TechLevel.TL10, ModuleCategory.TRAP, "REALITY_COLLAPSE", "Transwarp Collapse",
                Map.of("dc", 25, "damage", "20d6", "damageType", "Energy"));

        // --- DOOR → Ship Doors ---
        putLore(TechLevel.TL6, ModuleCategory.DOOR, "PORTCULLIS", "Blast Door",
                Map.of("hp", 200, "ac", 25, "classDC", 25));
        putLore(TechLevel.TL7, ModuleCategory.DOOR, "PORTCULLIS", "Airlock Door",
                Map.of("hp", 300, "ac", 28, "classDC", 28));
        putLore(TechLevel.TL8, ModuleCategory.DOOR, "PORTCULLIS", "Force Field Door",
                Map.of("hp", 500, "ac", 30, "classDC", 30));
        putLore(TechLevel.TL9, ModuleCategory.DOOR, "PORTCULLIS", "Energy Barrier",
                Map.of("hp", 1000, "ac", 35, "classDC", 35));
        putLore(TechLevel.TL10, ModuleCategory.DOOR, "PORTCULLIS", "Transwarp Barrier",
                Map.of("hp", 5000, "ac", 40, "classDC", 40));

        // --- New sci-fi DOOR types (TL6-TL10) ---
        putLore(TechLevel.TL6, ModuleCategory.DOOR, "BLAST_DOOR", "Blast Door",
                Map.of("hp", 200, "ac", 25, "classDC", 15));
        putLore(TechLevel.TL6, ModuleCategory.DOOR, "AIRLOCK", "Airlock",
                Map.of("hp", 150, "ac", 22, "classDC", 14));
        putLore(TechLevel.TL7, ModuleCategory.DOOR, "FORCE_FIELD_DOOR", "Force Field Door",
                Map.of("hp", 100, "ac", 28, "classDC", 16));
        putLore(TechLevel.TL8, ModuleCategory.DOOR, "ENERGY_DOOR", "Energy Door",
                Map.of("hp", 150, "ac", 30, "classDC", 18));
        putLore(TechLevel.TL8, ModuleCategory.DOOR, "TRANSPORTER_LOCK", "Transporter Lock",
                Map.of("hp", 100, "ac", 28, "classDC", 17));
        putLore(TechLevel.TL9, ModuleCategory.DOOR, "DIMENSIONAL_DOOR", "Dimensional Door",
                Map.of("hp", 200, "ac", 35, "classDC", 20));
        putLore(TechLevel.TL10, ModuleCategory.DOOR, "TRANSWARP_GATE", "Transwarp Gate",
                Map.of("hp", 500, "ac", 40, "classDC", 25));

        // --- CHEST → Containers ---
        putLore(TechLevel.TL6, ModuleCategory.CHEST, "TREASURE_CHEST", "Cargo Container",
                Map.of("hp", 100, "ac", 20, "capacity", "Huge"));
        putLore(TechLevel.TL7, ModuleCategory.CHEST, "TREASURE_CHEST", "Cargo Pod",
                Map.of("hp", 200, "ac", 22, "capacity", "Huge"));
        putLore(TechLevel.TL8, ModuleCategory.CHEST, "TREASURE_CHEST", "Secure Locker",
                Map.of("hp", 500, "ac", 25, "capacity", "Large"));
        putLore(TechLevel.TL9, ModuleCategory.CHEST, "TREASURE_CHEST", "Quantum Storage",
                Map.of("hp", 1000, "ac", 30, "capacity", "Dimensional"));

        // --- SPELL → Tech Abilities ---
        putLore(TechLevel.TL6, ModuleCategory.SPELL, "TELEPORT", "Transporter Beam-Out",
                Map.of("level", 3, "school", "Transport", "range", "10000ft"));
        putLore(TechLevel.TL7, ModuleCategory.SPELL, "TELEPORT", "Shield Modulation",
                Map.of("level", 5, "school", "Shield", "range", "Self", "shieldHP", 500));
        putLore(TechLevel.TL8, ModuleCategory.SPELL, "TELEPORT", "Computer Override",
                Map.of("level", 5, "school", "Hacking", "range", "100ft"));
        putLore(TechLevel.TL9, ModuleCategory.SPELL, "TELEPORT", "Warp Core Breach",
                Map.of("level", 7, "school", "Energy", "range", "1000ft", "damage", "20d6", "damageType", "Energy"));
        putLore(TechLevel.TL10, ModuleCategory.SPELL, "TELEPORT", "Transwarp Manipulation",
                Map.of("level", 9, "school", "Transwarp", "range", "Unlimited"));

        // === ANIMAL mappings (TL6-TL10, Star Trek style) ===
        // Livestock
        putLore(TechLevel.TL6, ModuleCategory.ANIMAL, "COW", "Vulcan Cow",
                Map.of("milkYield", 25, "meatYield", 220, "type", "Beast", "size", "Large"));
        putLore(TechLevel.TL6, ModuleCategory.ANIMAL, "PIG", "Beta Lyrae Pig",
                Map.of("meatYield", 90, "type", "Beast", "size", "Medium"));
        putLore(TechLevel.TL6, ModuleCategory.ANIMAL, "SHEEP", "Vulcan Sheep",
                Map.of("milkYield", 3, "woolYield", 4, "meatYield", 35, "type", "Beast", "size", "Medium"));
        putLore(TechLevel.TL6, ModuleCategory.ANIMAL, "CHICKEN", "Vulcan Chicken",
                Map.of("eggRate", 6, "meatYield", 3, "type", "Beast", "size", "Tiny"));
        putLore(TechLevel.TL6, ModuleCategory.ANIMAL, "HORSE", "Vulcan Horse",
                Map.of("carryingCapacity", 120, "meatYield", 170, "type", "Beast", "size", "Large"));
        putLore(TechLevel.TL6, ModuleCategory.ANIMAL, "DOG", "Vulcan Hound",
                Map.of("meatYield", 18, "type", "Beast", "size", "Medium"));
        putLore(TechLevel.TL7, ModuleCategory.ANIMAL, "COW", "Colonial Bovine",
                Map.of("milkYield", 30, "meatYield", 250, "type", "Beast", "size", "Large"));
        putLore(TechLevel.TL7, ModuleCategory.ANIMAL, "HORSE", "Hover Horse",
                Map.of("carryingCapacity", 150, "meatYield", 180, "type", "Beast", "size", "Large"));
        putLore(TechLevel.TL8, ModuleCategory.ANIMAL, "COW", "Replicated Bovine",
                Map.of("milkYield", 40, "meatYield", 300, "type", "Bio-Engineered", "size", "Large"));
        putLore(TechLevel.TL8, ModuleCategory.ANIMAL, "HORSE", "Anti-Gravity Steed",
                Map.of("carryingCapacity", 200, "type", "Bio-Engineered", "size", "Large"));
        putLore(TechLevel.TL8, ModuleCategory.ANIMAL, "DOG", "Security K-9",
                Map.of("type", "Bio-Engineered", "size", "Medium"));
        putLore(TechLevel.TL9, ModuleCategory.ANIMAL, "COW", "Genetic Bovine",
                Map.of("milkYield", 50, "meatYield", 400, "type", "Bio-Engineered", "size", "Large"));
        putLore(TechLevel.TL9, ModuleCategory.ANIMAL, "DOG", "Combat Drone Hound",
                Map.of("type", "Construct", "size", "Medium"));
        putLore(TechLevel.TL10, ModuleCategory.ANIMAL, "COW", "Transwarp Bovine",
                Map.of("milkYield", 100, "meatYield", 500, "type", "Bio-Engineered", "size", "Large"));
        putLore(TechLevel.TL10, ModuleCategory.ANIMAL, "HORSE", "Transwarp Mount",
                Map.of("carryingCapacity", 500, "type", "Bio-Engineered", "size", "Large"));
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
