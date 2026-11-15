package com.yisy.modules.doramcu.traveller.config;

import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Traveller tech level adapter.
 * <p>
 * Maps generic tech-level modules to Traveller universe lore-appropriate names and stats.
 * Traveller supports the full range TL0–TL12.
 * </p>
 */
@Component
public class TravellerTechLevelAdapter implements TechLevelAdapter {

    private static final Map<String, String> LORE_NAMES = new HashMap<>();
    private static final Map<String, Map<String, Object>> LORE_STATS = new HashMap<>();

    static {
        // === TL0: Prehistoric ===
        putLore(TechLevel.TL0, ModuleCategory.SETTLEMENT, "CAVE_DWELLING", "Cave Dwelling",
                Map.of("pop", 5, "security", 1));
        putLore(TechLevel.TL0, ModuleCategory.SETTLEMENT, "HUT_VILLAGE", "Hut Village",
                Map.of("pop", 20, "security", 1));
        putLore(TechLevel.TL0, ModuleCategory.INDUSTRY, "HUNTING_GROUND", "Hunting Ground",
                Map.of("output", 10, "food", 5));
        putLore(TechLevel.TL0, ModuleCategory.INDUSTRY, "GATHERING_SITE", "Gathering Site",
                Map.of("output", 8, "herbs", 3));
        putLore(TechLevel.TL0, ModuleCategory.VEHICLE, "RAFT", "Wooden Raft",
                Map.of("speed", 1, "cargo", 3));
        putLore(TechLevel.TL0, ModuleCategory.INFRASTRUCTURE, "FIRE_PIT", "Campfire",
                Map.of("light", 1));
        putLore(TechLevel.TL0, ModuleCategory.DEFENSE, "PALISADE", "Wooden Palisade",
                Map.of("defense", 1, "hp", 30));

        // === TL1: Ancient ===
        putLore(TechLevel.TL1, ModuleCategory.SETTLEMENT, "STONE_HUT_VILLAGE", "Stone Village",
                Map.of("pop", 50, "security", 2));
        putLore(TechLevel.TL1, ModuleCategory.SETTLEMENT, "TEMPLE_COMPOUND", "Temple",
                Map.of("pop", 30, "security", 2, "influence", 3));
        putLore(TechLevel.TL1, ModuleCategory.INDUSTRY, "FARM", "Basic Farm",
                Map.of("output", 25, "food", 20));
        putLore(TechLevel.TL1, ModuleCategory.INDUSTRY, "BRONZE_SMITHY", "Bronze Smithy",
                Map.of("output", 15, "weapons", 5));
        putLore(TechLevel.TL1, ModuleCategory.INDUSTRY, "CLAY_POTTERY", "Clay Pottery",
                Map.of("output", 10));
        putLore(TechLevel.TL1, ModuleCategory.VEHICLE, "OX_CART", "Ox Cart",
                Map.of("speed", 3, "cargo", 5));
        putLore(TechLevel.TL1, ModuleCategory.VEHICLE, "ROWBOAT", "Rowboat",
                Map.of("speed", 5, "cargo", 4));
        putLore(TechLevel.TL1, ModuleCategory.INFRASTRUCTURE, "STONE_ROAD", "Stone Road",
                Map.of("tradeBonus", 1));
        putLore(TechLevel.TL1, ModuleCategory.INFRASTRUCTURE, "WELL", "Stone Well",
                Map.of("water", 1));
        putLore(TechLevel.TL1, ModuleCategory.DEFENSE, "STONE_WALL", "Stone Wall",
                Map.of("defense", 3, "hp", 100));
        putLore(TechLevel.TL1, ModuleCategory.DEFENSE, "WATCH_TOWER", "Watch Tower",
                Map.of("defense", 2, "visionRange", 2));
        putLore(TechLevel.TL1, ModuleCategory.COMMUNICATION, "SMOKE_SIGNAL", "Smoke Signal Tower",
                Map.of("range", 5));

        // === TL2: Medieval ===
        putLore(TechLevel.TL2, ModuleCategory.SETTLEMENT, "STONE_CASTLE", "Feudal Hold",
                Map.of("pop", 100, "security", 4, "garrison", 20));
        putLore(TechLevel.TL2, ModuleCategory.SETTLEMENT, "MARKET_TOWN", "Market Town",
                Map.of("pop", 200, "security", 3, "tradeBonus", 3));
        putLore(TechLevel.TL2, ModuleCategory.SETTLEMENT, "MONASTERY", "Monastery",
                Map.of("pop", 50, "security", 3, "influence", 4));
        putLore(TechLevel.TL2, ModuleCategory.INDUSTRY, "BLACKSMITH_FORGE", "Blacksmith Forge",
                Map.of("output", 20, "weapons", 10));
        putLore(TechLevel.TL2, ModuleCategory.INDUSTRY, "WATER_MILL", "Water Mill",
                Map.of("output", 30));
        putLore(TechLevel.TL2, ModuleCategory.INDUSTRY, "WOOL_WORKSHOP", "Wool Workshop",
                Map.of("output", 15));
        putLore(TechLevel.TL2, ModuleCategory.INDUSTRY, "QUARRY", "Stone Quarry",
                Map.of("output", 25));
        putLore(TechLevel.TL2, ModuleCategory.VEHICLE, "HORSE_DRAWN_CART", "Horse Cart",
                Map.of("speed", 5, "cargo", 8));
        putLore(TechLevel.TL2, ModuleCategory.VEHICLE, "CARGO_SHIP", "Sailing Ship",
                Map.of("speed", 8, "cargo", 20));
        putLore(TechLevel.TL2, ModuleCategory.INFRASTRUCTURE, "STONE_BRIDGE", "Stone Bridge",
                Map.of("tradeBonus", 2));
        putLore(TechLevel.TL2, ModuleCategory.INFRASTRUCTURE, "CATHEDRAL", "Cathedral",
                Map.of("influence", 5));
        putLore(TechLevel.TL2, ModuleCategory.DEFENSE, "CASTLE_WALL", "Castle Wall",
                Map.of("defense", 5, "hp", 250));
        putLore(TechLevel.TL2, ModuleCategory.DEFENSE, "BARRACKS", "Barracks",
                Map.of("garrison", 30));
        putLore(TechLevel.TL2, ModuleCategory.COMMUNICATION, "MOUNTED_MESSENGER", "Messenger Station",
                Map.of("range", 20));

        // === TL3: Renaissance ===
        putLore(TechLevel.TL3, ModuleCategory.SETTLEMENT, "HARBOR_CITY", "Harbor City",
                Map.of("pop", 500, "security", 5, "tradeBonus", 5));
        putLore(TechLevel.TL3, ModuleCategory.SETTLEMENT, "UNIVERSITY", "University",
                Map.of("pop", 100, "security", 4, "scienceBonus", 3));
        putLore(TechLevel.TL3, ModuleCategory.INDUSTRY, "GUNPOWDER_WORKS", "Gunpowder Works",
                Map.of("output", 20, "weapons", 15));
        putLore(TechLevel.TL3, ModuleCategory.INDUSTRY, "PRINTING_PRESS", "Printing Press",
                Map.of("output", 30));
        putLore(TechLevel.TL3, ModuleCategory.INDUSTRY, "SHIPYARD", "Shipyard",
                Map.of("output", 40));
        putLore(TechLevel.TL3, ModuleCategory.VEHICLE, "GALLEON", "Galleon",
                Map.of("speed", 12, "cargo", 50, "weapons", "2d6"));
        putLore(TechLevel.TL3, ModuleCategory.VEHICLE, "STAGE_COACH", "Stage Coach",
                Map.of("speed", 6, "cargo", 10));
        putLore(TechLevel.TL3, ModuleCategory.INFRASTRUCTURE, "CANAL", "Canal",
                Map.of("tradeBonus", 3));
        putLore(TechLevel.TL3, ModuleCategory.INFRASTRUCTURE, "LANTERN_POST", "Lantern Post Network",
                Map.of("light", 3));
        putLore(TechLevel.TL3, ModuleCategory.DEFENSE, "ARTILLERY_FORT", "Artillery Fort",
                Map.of("defense", 7, "hp", 300, "weapons", "4d6"));
        putLore(TechLevel.TL3, ModuleCategory.DEFENSE, "MOAT", "Defensive Moat",
                Map.of("defense", 3));
        putLore(TechLevel.TL3, ModuleCategory.COMMUNICATION, "POST_OFFICE", "Post Office",
                Map.of("range", 50));

        // === TL4: Industrial ===
        putLore(TechLevel.TL4, ModuleCategory.SETTLEMENT, "INDUSTRIAL_CITY", "Industrial City",
                Map.of("pop", 2000, "security", 6, "tradeBonus", 8));
        putLore(TechLevel.TL4, ModuleCategory.SETTLEMENT, "RAILWAY_STATION", "Railway Station",
                Map.of("pop", 500, "security", 5));
        putLore(TechLevel.TL4, ModuleCategory.INDUSTRY, "STEAM_FACTORY", "Steam Factory",
                Map.of("output", 100));
        putLore(TechLevel.TL4, ModuleCategory.INDUSTRY, "COAL_MINE", "Coal Mine",
                Map.of("output", 80, "fuel", 60));
        putLore(TechLevel.TL4, ModuleCategory.INDUSTRY, "STEEL_MILL", "Steel Mill",
                Map.of("output", 120));
        putLore(TechLevel.TL4, ModuleCategory.INDUSTRY, "TEXTILE_MILL", "Textile Mill",
                Map.of("output", 60));
        putLore(TechLevel.TL4, ModuleCategory.VEHICLE, "STEAM_LOCOMOTIVE", "Steam Locomotive",
                Map.of("speed", 20, "cargo", 50));
        putLore(TechLevel.TL4, ModuleCategory.VEHICLE, "STEAMSHIP", "Steamship",
                Map.of("speed", 15, "cargo", 100));
        putLore(TechLevel.TL4, ModuleCategory.VEHICLE, "BUS_DEPOT", "Bus Depot",
                Map.of("speed", 10, "cargo", 30));
        putLore(TechLevel.TL4, ModuleCategory.INFRASTRUCTURE, "RAILROAD", "Railroad",
                Map.of("tradeBonus", 5));
        putLore(TechLevel.TL4, ModuleCategory.INFRASTRUCTURE, "TELEGRAPH_STATION", "Telegraph Station",
                Map.of("range", 100));
        putLore(TechLevel.TL4, ModuleCategory.INFRASTRUCTURE, "GAS_LAMP_GRID", "Gas Lamp Grid",
                Map.of("light", 5));
        putLore(TechLevel.TL4, ModuleCategory.DEFENSE, "IRONCLAD_FORT", "Ironclad Fort",
                Map.of("defense", 8, "hp", 400));
        putLore(TechLevel.TL4, ModuleCategory.COMMUNICATION, "TELEGRAPH_NETWORK", "Telegraph Network",
                Map.of("range", 500));

        // === TL5: Modern ===
        putLore(TechLevel.TL5, ModuleCategory.SETTLEMENT, "METROPOLIS", "Metropolis",
                Map.of("pop", 10000, "security", 7, "tradeBonus", 10));
        putLore(TechLevel.TL5, ModuleCategory.SETTLEMENT, "AIRPORT", "Airport",
                Map.of("pop", 2000, "security", 6));
        putLore(TechLevel.TL5, ModuleCategory.INDUSTRY, "ASSEMBLY_PLANT", "Assembly Plant",
                Map.of("output", 200));
        putLore(TechLevel.TL5, ModuleCategory.INDUSTRY, "OIL_REFINERY", "Oil Refinery",
                Map.of("output", 150, "fuel", 120));
        putLore(TechLevel.TL5, ModuleCategory.INDUSTRY, "CHEMICAL_PLANT", "Chemical Plant",
                Map.of("output", 100));
        putLore(TechLevel.TL5, ModuleCategory.VEHICLE, "AUTOMOBILE", "Automobile",
                Map.of("speed", 50, "cargo", 5));
        putLore(TechLevel.TL5, ModuleCategory.VEHICLE, "COMMERCIAL_AIRLINER", "Commercial Airliner",
                Map.of("speed", 200, "cargo", 200));
        putLore(TechLevel.TL5, ModuleCategory.VEHICLE, "TANK", "Tank",
                Map.of("speed", 30, "weapons", "8d6", "armor", 15));
        putLore(TechLevel.TL5, ModuleCategory.INFRASTRUCTURE, "HIGHWAY", "Highway",
                Map.of("tradeBonus", 8));
        putLore(TechLevel.TL5, ModuleCategory.INFRASTRUCTURE, "POWER_PLANT", "Power Plant",
                Map.of("energy", 100));
        putLore(TechLevel.TL5, ModuleCategory.INFRASTRUCTURE, "SEWER_SYSTEM", "Sewer System",
                Map.of("healthBonus", 3));
        putLore(TechLevel.TL5, ModuleCategory.DEFENSE, "MILITARY_BASE", "Military Base",
                Map.of("defense", 10, "garrison", 100));
        putLore(TechLevel.TL5, ModuleCategory.DEFENSE, "ANTI_AIR_BATTERY", "Anti-Air Battery",
                Map.of("defense", 8, "weapons", "6d6"));
        putLore(TechLevel.TL5, ModuleCategory.COMMUNICATION, "RADIO_TOWER", "Radio Tower",
                Map.of("range", 1000));
        putLore(TechLevel.TL5, ModuleCategory.COMMUNICATION, "CELL_TOWER", "Cell Tower",
                Map.of("range", 50));

        // === TL6: Information Age ===
        putLore(TechLevel.TL6, ModuleCategory.SETTLEMENT, "SPACEPORT_CITY", "Spaceport City",
                Map.of("pop", 50000, "security", 8, "tradeBonus", 12));
        putLore(TechLevel.TL6, ModuleCategory.SETTLEMENT, "RESEARCH_CAMPUS", "Research Campus",
                Map.of("pop", 5000, "security", 7, "scienceBonus", 8));
        putLore(TechLevel.TL6, ModuleCategory.INDUSTRY, "SEMICONDUCTOR_FAB", "Semiconductor Fab",
                Map.of("output", 300));
        putLore(TechLevel.TL6, ModuleCategory.INDUSTRY, "NUCLEAR_PLANT", "Nuclear Power Plant",
                Map.of("output", 500, "energy", 500));
        putLore(TechLevel.TL6, ModuleCategory.INDUSTRY, "ROBOTICS_FACTORY", "Robotics Factory",
                Map.of("output", 200));
        putLore(TechLevel.TL6, ModuleCategory.VEHICLE, "HOVER_TRANSPORT", "Hover Transport",
                Map.of("speed", 100, "cargo", 20));
        putLore(TechLevel.TL6, ModuleCategory.VEHICLE, "ORBITAL_ROCKET", "Orbital Rocket",
                Map.of("speed", 500, "cargo", 100));
        putLore(TechLevel.TL6, ModuleCategory.VEHICLE, "SUBORBITAL_SHUTTLE", "Suborbital Shuttle",
                Map.of("speed", 300, "cargo", 50));
        putLore(TechLevel.TL6, ModuleCategory.INFRASTRUCTURE, "ORBITAL_ELEVATOR", "Orbital Elevator",
                Map.of("tradeBonus", 15));
        putLore(TechLevel.TL6, ModuleCategory.INFRASTRUCTURE, "DATA_CENTER", "Data Center",
                Map.of("computePower", 100));
        putLore(TechLevel.TL6, ModuleCategory.INFRASTRUCTURE, "SOLAR_FARM", "Solar Farm",
                Map.of("energy", 200));
        putLore(TechLevel.TL6, ModuleCategory.DEFENSE, "MISSILE_SILEO", "Missile Silo",
                Map.of("defense", 12, "weapons", "20d6"));
        putLore(TechLevel.TL6, ModuleCategory.DEFENSE, "SATELLITE_DEFENSE", "Defense Satellite Network",
                Map.of("defense", 10, "range", 10000));
        putLore(TechLevel.TL6, ModuleCategory.COMMUNICATION, "SATELLITE_UPLINK", "Satellite Uplink Station",
                Map.of("range", 50000));
        putLore(TechLevel.TL6, ModuleCategory.COMMUNICATION, "FIBER_OPTIC_HUB", "Fiber Optic Hub",
                Map.of("range", 1000));

        // === TL7: Interplanetary ===
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST", "Colonial Outpost",
                Map.of("pop", 10000, "security", 9, "tradeBonus", 15));
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "ORBITAL_HABITAT", "Orbital Habitat",
                Map.of("pop", 50000, "security", 10));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "FUSION_REACTOR", "Fusion Reactor",
                Map.of("output", 1000, "energy", 1000));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "ASTEROID_MINE", "Asteroid Mine",
                Map.of("output", 500, "minerals", 400));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "BIOTECH_LAB", "Biotech Lab",
                Map.of("output", 200, "medicalBonus", 5));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Interplanetary Ship",
                Map.of("speed", 1000, "cargo", 200));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "MARS_ROVER", "Planetary Rover",
                Map.of("speed", 20, "cargo", 10));
        putLore(TechLevel.TL7, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Lunar Base",
                Map.of("pop", 1000, "security", 8));
        putLore(TechLevel.TL7, ModuleCategory.INFRASTRUCTURE, "SPACE_ELEVATOR", "Space Elevator",
                Map.of("tradeBonus", 20));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "ORBITAL_GUN", "Orbital Defense Gun",
                Map.of("defense", 15, "weapons", "50d6"));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "PLANETARY_SHIELD", "Planetary Shield Generator",
                Map.of("defense", 20));
        putLore(TechLevel.TL7, ModuleCategory.COMMUNICATION, "DEEP_SPACE_ARRAY", "Deep Space Array",
                Map.of("range", 100000));

        // === TL8: Interstellar ===
        putLore(TechLevel.TL8, ModuleCategory.SETTLEMENT, "SPACE_STATION", "Space Station",
                Map.of("pop", 100000, "security", 12));
        putLore(TechLevel.TL8, ModuleCategory.SETTLEMENT, "DYSON_RING", "Dyson Ring Segment",
                Map.of("pop", 1000000, "security", 15));
        putLore(TechLevel.TL8, ModuleCategory.INDUSTRY, "ANTIMATTER_PLANT", "Antimatter Plant",
                Map.of("output", 5000, "fuel", 5000));
        putLore(TechLevel.TL8, ModuleCategory.INDUSTRY, "NANOFAB", "Nanofabrication Plant",
                Map.of("output", 2000));
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "FTL_CRUISER", "FTL Cruiser",
                Map.of("speed", 5000, "cargo", 500, "weapons", "100d6"));
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "TRANSPORT_SHUTTLE", "Transport Shuttle",
                Map.of("speed", 2000, "cargo", 100));
        putLore(TechLevel.TL8, ModuleCategory.INFRASTRUCTURE, "JUMP_GATE_STATION", "Jump Gate Station",
                Map.of("tradeBonus", 50));
        putLore(TechLevel.TL8, ModuleCategory.INFRASTRUCTURE, "STARLIFT", "Starlift",
                Map.of("tradeBonus", 30));
        putLore(TechLevel.TL8, ModuleCategory.DEFENSE, "ENERGY_WEAPON", "Energy Weapon Platform",
                Map.of("defense", 25, "weapons", "200d6"));
        putLore(TechLevel.TL8, ModuleCategory.DEFENSE, "SHIELD_GENERATOR", "Shield Generator Array",
                Map.of("defense", 30));
        putLore(TechLevel.TL8, ModuleCategory.COMMUNICATION, "HYPERNET_RELAY", "Hypernet Relay",
                Map.of("range", 1000000));

        // === TL9: Galactic ===
        putLore(TechLevel.TL9, ModuleCategory.SETTLEMENT, "STARBASE", "Starbase",
                Map.of("pop", 5000000, "security", 20));
        putLore(TechLevel.TL9, ModuleCategory.SETTLEMENT, "WORLD_CITY", "World City",
                Map.of("pop", 50000000, "security", 18, "tradeBonus", 30));
        putLore(TechLevel.TL9, ModuleCategory.INDUSTRY, "STAR_FORGE", "Star Forge",
                Map.of("output", 100000));
        putLore(TechLevel.TL9, ModuleCategory.INDUSTRY, "AI_CORE", "AI Core Facility",
                Map.of("output", 50000, "aiBonus", 10));
        putLore(TechLevel.TL9, ModuleCategory.VEHICLE, "DREADNOUGHT", "Dreadnought",
                Map.of("speed", 10000, "cargo", 5000, "weapons", "500d6"));
        putLore(TechLevel.TL9, ModuleCategory.VEHICLE, "WORMHOLE_DRIVE", "Wormhole Drive Vessel",
                Map.of("speed", 50000, "cargo", 1000));
        putLore(TechLevel.TL9, ModuleCategory.INFRASTRUCTURE, "WORMHOLE_NETWORK", "Wormhole Network Node",
                Map.of("tradeBonus", 100));
        putLore(TechLevel.TL9, ModuleCategory.INFRASTRUCTURE, "PLANETARY_COMPUTER", "Planetary Computer",
                Map.of("computePower", 1000000));
        putLore(TechLevel.TL9, ModuleCategory.DEFENSE, "PLANET_CRUSHER", "Planet Defense Grid",
                Map.of("defense", 50));
        putLore(TechLevel.TL9, ModuleCategory.COMMUNICATION, "QUANTUM_NET", "Quantum Communication Network",
                Map.of("range", 10000000));

        // === TL10: Post-Human ===
        putLore(TechLevel.TL10, ModuleCategory.SETTLEMENT, "MEGASTRUCTURE_HUB", "Megastructure Hub",
                Map.of("pop", 100000000, "security", 30));
        putLore(TechLevel.TL10, ModuleCategory.INDUSTRY, "STAR_LIFTER", "Star Lifter",
                Map.of("output", 500000));
        putLore(TechLevel.TL10, ModuleCategory.INDUSTRY, "REALITY_ENGINE", "Reality Engine",
                Map.of("output", 1000000));
        putLore(TechLevel.TL10, ModuleCategory.VEHICLE, "WARP_GATE", "Warp Gate",
                Map.of("speed", 100000, "cargo", 100000));
        putLore(TechLevel.TL10, ModuleCategory.INFRASTRUCTURE, "DYSON_SPHERE", "Dyson Sphere",
                Map.of("energy", 1000000));
        putLore(TechLevel.TL10, ModuleCategory.INFRASTRUCTURE, "MATRIOSKA_BRAIN", "Matrioshka Brain",
                Map.of("computePower", 100000000));
        putLore(TechLevel.TL10, ModuleCategory.DEFENSE, "VOID_SHIELD", "Void Shield",
                Map.of("defense", 100));
        putLore(TechLevel.TL10, ModuleCategory.COMMUNICATION, "THOUGHT_NET", "Thought Network",
                Map.of("range", 100000000));

        // === TL11: Transcendent ===
        putLore(TechLevel.TL11, ModuleCategory.SETTLEMENT, "DIMENSIONAL_HUB", "Dimensional Hub",
                Map.of("pop", 1000000000, "security", 50));
        putLore(TechLevel.TL11, ModuleCategory.INDUSTRY, "UNIVERSE_SEEDER", "Universe Seeder",
                Map.of("output", 10000000));
        putLore(TechLevel.TL11, ModuleCategory.VEHICLE, "TIME_VESSEL", "Time Vessel",
                Map.of("speed", 1000000, "cargo", 10000));
        putLore(TechLevel.TL11, ModuleCategory.INFRASTRUCTURE, "DIMENSIONAL_BRIDGE", "Dimensional Bridge",
                Map.of("tradeBonus", 1000));
        putLore(TechLevel.TL11, ModuleCategory.DEFENSE, "TIMELINE_ANCHOR", "Timeline Anchor",
                Map.of("defense", 200));
        putLore(TechLevel.TL11, ModuleCategory.COMMUNICATION, "OMNI_CHANNEL", "Omni-Channel",
                Map.of("range", 1000000000));

        // === TL12: Post-Singularity ===
        putLore(TechLevel.TL12, ModuleCategory.SETTLEMENT, "OMNIVERSAL_THRONE", "Omniversal Throne",
                Map.of("pop", 1000000000L, "security", 100));
        putLore(TechLevel.TL12, ModuleCategory.INDUSTRY, "COSMIC_LOOM", "Cosmic Loom",
                Map.of("output", 100000000));
        putLore(TechLevel.TL12, ModuleCategory.VEHICLE, "OMNIVERSAL_TRAVELER", "Omniversal Traveler",
                Map.of("speed", 10000000, "cargo", 100000));
        putLore(TechLevel.TL12, ModuleCategory.INFRASTRUCTURE, "COSMIC_AXIS", "Cosmic Axis",
                Map.of("tradeBonus", 10000));
        putLore(TechLevel.TL12, ModuleCategory.DEFENSE, "EXISTENCE_SHIELD", "Existence Shield",
                Map.of("defense", 1000));
        putLore(TechLevel.TL12, ModuleCategory.COMMUNICATION, "ABSOLUTE_KNOWLEDGE", "Absolute Knowledge Network",
                Map.of("range", 1000000000L));

        // ==========================================
        // CROSS-GALAXY CONVERSION CATEGORIES
        // ==========================================

        // --- TREASURE (all TL) ---
        putLore(TechLevel.TL0, ModuleCategory.TREASURE, "BONE_NECKLACE", "Bone Talisman",
                Map.of("gpValue", 1, "rarity", "Common", "type", "Relic"));
        putLore(TechLevel.TL0, ModuleCategory.TREASURE, "STONE_AMULET", "Stone Charm",
                Map.of("gpValue", 2, "rarity", "Common", "type", "Relic"));
        putLore(TechLevel.TL1, ModuleCategory.TREASURE, "GOLD_COINS", "Gold Coins",
                Map.of("gpValue", 100, "rarity", "Common", "type", "Currency"));
        putLore(TechLevel.TL1, ModuleCategory.TREASURE, "GEMSTONE", "Gemstone",
                Map.of("gpValue", 50, "rarity", "Uncommon", "type", "Gem"));
        putLore(TechLevel.TL1, ModuleCategory.TREASURE, "JEWELRY", "Jewelry",
                Map.of("gpValue", 75, "rarity", "Uncommon", "type", "Wondrous Item"));
        putLore(TechLevel.TL2, ModuleCategory.TREASURE, "GOLD_HOARD", "Gold Hoard",
                Map.of("gpValue", 500, "rarity", "Rare", "type", "Currency"));
        putLore(TechLevel.TL2, ModuleCategory.TREASURE, "DIAMOND", "Diamond",
                Map.of("gpValue", 200, "rarity", "Rare", "type", "Gem"));
        putLore(TechLevel.TL2, ModuleCategory.TREASURE, "CROWN_JEWELS", "Crown Jewels",
                Map.of("gpValue", 1000, "rarity", "Very Rare", "type", "Regalia"));
        putLore(TechLevel.TL3, ModuleCategory.TREASURE, "ANCIENT_ARTIFACT", "Ancient Artifact",
                Map.of("gpValue", 500, "rarity", "Very Rare", "type", "Relic"));
        putLore(TechLevel.TL3, ModuleCategory.TREASURE, "SPELL_SCROLL", "Data Scroll",
                Map.of("gpValue", 100, "rarity", "Uncommon", "type", "Document"));
        putLore(TechLevel.TL4, ModuleCategory.TREASURE, "DRAGON_HOARD", "Dragon's Hoard",
                Map.of("gpValue", 50000, "rarity", "Legendary", "type", "Currency"));
        putLore(TechLevel.TL5, ModuleCategory.TREASURE, "GOLD_COINS", "Stellar Credits",
                Map.of("gpValue", 100, "rarity", "Common", "type", "Currency"));
        putLore(TechLevel.TL5, ModuleCategory.TREASURE, "GEMSTONE", "Synthetic Gem",
                Map.of("gpValue", 50, "rarity", "Uncommon", "type", "Gem"));
        putLore(TechLevel.TL6, ModuleCategory.TREASURE, "GOLD_COINS", "Stellar Credits",
                Map.of("gpValue", 100, "rarity", "Common", "type", "Currency"));
        putLore(TechLevel.TL6, ModuleCategory.TREASURE, "GEMSTONE", "Rare Crystal",
                Map.of("gpValue", 500, "rarity", "Uncommon", "type", "Crystal"));
        putLore(TechLevel.TL7, ModuleCategory.TREASURE, "GOLD_COINS", "Stellar Credits",
                Map.of("gpValue", 1000, "rarity", "Common", "type", "Currency"));
        putLore(TechLevel.TL7, ModuleCategory.TREASURE, "GEMSTONE", "Asteroid Crystal",
                Map.of("gpValue", 5000, "rarity", "Rare", "type", "Crystal"));
        putLore(TechLevel.TL8, ModuleCategory.TREASURE, "GOLD_COINS", "Stellar Credits",
                Map.of("gpValue", 10000, "rarity", "Common", "type", "Currency"));
        putLore(TechLevel.TL8, ModuleCategory.TREASURE, "GEMSTONE", "Exotic Matter Crystal",
                Map.of("gpValue", 50000, "rarity", "Very Rare", "type", "Exotic Matter"));
        putLore(TechLevel.TL9, ModuleCategory.TREASURE, "GOLD_COINS", "Galactic Credits",
                Map.of("gpValue", 100000, "rarity", "Common", "type", "Currency"));
        putLore(TechLevel.TL9, ModuleCategory.TREASURE, "ANCIENT_ARTIFACT", "Ancient Alien Artifact",
                Map.of("gpValue", 500000, "rarity", "Legendary", "type", "Artifact"));
        putLore(TechLevel.TL10, ModuleCategory.TREASURE, "GOLD_COINS", "Universal Credits",
                Map.of("gpValue", 1000000, "rarity", "Common", "type", "Currency"));
        putLore(TechLevel.TL10, ModuleCategory.TREASURE, "ANCIENT_ARTIFACT", "Pre-Singularity Relic",
                Map.of("gpValue", 5000000, "rarity", "Artifact", "type", "Relic"));
        putLore(TechLevel.TL11, ModuleCategory.TREASURE, "ANCIENT_ARTIFACT", "Dimensional Artifact",
                Map.of("gpValue", 10000000, "rarity", "Artifact", "type", "Relic"));
        putLore(TechLevel.TL12, ModuleCategory.TREASURE, "ANCIENT_ARTIFACT", "Omniversal Relic",
                Map.of("gpValue", 100000000L, "rarity", "Artifact", "type", "Relic"));

        // --- WEAPON (all TL) ---
        putLore(TechLevel.TL0, ModuleCategory.WEAPON, "STONE_SPEAR", "Stone Spear",
                Map.of("damage", "1d6", "damageType", "Piercing", "weight", 6));
        putLore(TechLevel.TL0, ModuleCategory.WEAPON, "STONE_AXE", "Stone Axe",
                Map.of("damage", "1d6", "damageType", "Slashing", "weight", 4));
        putLore(TechLevel.TL0, ModuleCategory.WEAPON, "BOW", "Shortbow",
                Map.of("damage", "1d8", "damageType", "Piercing", "weight", 2));
        putLore(TechLevel.TL1, ModuleCategory.WEAPON, "SHORTSWORD", "Shortsword",
                Map.of("damage", "1d6", "damageType", "Piercing", "weight", 2));
        putLore(TechLevel.TL1, ModuleCategory.WEAPON, "LONGSWORD", "Longsword",
                Map.of("damage", "1d8", "damageType", "Slashing", "weight", 3));
        putLore(TechLevel.TL1, ModuleCategory.WEAPON, "LONGBOW", "Longbow",
                Map.of("damage", "1d8", "damageType", "Piercing", "weight", 2));
        putLore(TechLevel.TL2, ModuleCategory.WEAPON, "WARHAMMER", "Warhammer",
                Map.of("damage", "1d8", "damageType", "Bludgeoning", "weight", 2));
        putLore(TechLevel.TL2, ModuleCategory.WEAPON, "HALBERD", "Halberd",
                Map.of("damage", "1d10", "damageType", "Slashing", "weight", 6));
        putLore(TechLevel.TL2, ModuleCategory.WEAPON, "CROSSBOW", "Heavy Crossbow",
                Map.of("damage", "1d10", "damageType", "Piercing", "weight", 18));
        putLore(TechLevel.TL3, ModuleCategory.WEAPON, "HANDGUN", "Flintlock Pistol",
                Map.of("damage", "1d10", "damageType", "Piercing", "weight", 2));
        putLore(TechLevel.TL4, ModuleCategory.WEAPON, "RIFLE", "Breech-Loading Rifle",
                Map.of("damage", "1d10", "damageType", "Piercing", "weight", 5));
        putLore(TechLevel.TL4, ModuleCategory.WEAPON, "MAGIC_LONGSWORD", "Plasma Sword",
                Map.of("damage", "1d8+1", "damageType", "Energy", "weight", 3));
        putLore(TechLevel.TL5, ModuleCategory.WEAPON, "RIFLE", "Assault Rifle",
                Map.of("damage", "2d6", "damageType", "Piercing", "weight", 5));
        putLore(TechLevel.TL5, ModuleCategory.WEAPON, "HANDGUN", "Sidearm Pistol",
                Map.of("damage", "1d8", "damageType", "Piercing", "weight", 2));
        putLore(TechLevel.TL6, ModuleCategory.WEAPON, "RIFLE", "Pulse Rifle",
                Map.of("damage", "3d6", "damageType", "Energy", "weight", 4));
        putLore(TechLevel.TL6, ModuleCategory.WEAPON, "HANDGUN", "Pulse Pistol",
                Map.of("damage", "2d6", "damageType", "Energy", "weight", 2));
        putLore(TechLevel.TL7, ModuleCategory.WEAPON, "RIFLE", "Plasma Rifle",
                Map.of("damage", "4d6", "damageType", "Energy", "weight", 4));
        putLore(TechLevel.TL7, ModuleCategory.WEAPON, "HANDGUN", "Plasma Pistol",
                Map.of("damage", "3d6", "damageType", "Energy", "weight", 2));
        putLore(TechLevel.TL8, ModuleCategory.WEAPON, "RIFLE", "Particle Beam Rifle",
                Map.of("damage", "5d6", "damageType", "Energy", "weight", 3));
        putLore(TechLevel.TL8, ModuleCategory.WEAPON, "HANDGUN", "Particle Beam Pistol",
                Map.of("damage", "4d6", "damageType", "Energy", "weight", 1));
        putLore(TechLevel.TL9, ModuleCategory.WEAPON, "RIFLE", "Antimatter Rifle",
                Map.of("damage", "8d6", "damageType", "Energy", "weight", 2));
        putLore(TechLevel.TL9, ModuleCategory.WEAPON, "HANDGUN", "Antimatter Pistol",
                Map.of("damage", "6d6", "damageType", "Energy", "weight", 1));
        putLore(TechLevel.TL10, ModuleCategory.WEAPON, "RIFLE", "Reality Disruptor",
                Map.of("damage", "10d6", "damageType", "Force", "weight", 1));
        putLore(TechLevel.TL11, ModuleCategory.WEAPON, "RIFLE", "Dimensional Blade",
                Map.of("damage", "15d6", "damageType", "Force", "weight", 0));
        putLore(TechLevel.TL12, ModuleCategory.WEAPON, "RIFLE", "Omniversal Annihilator",
                Map.of("damage", "20d6", "damageType", "Force", "weight", 0));

        // --- ARMOR (all TL) ---
        putLore(TechLevel.TL0, ModuleCategory.ARMOR, "LEATHER_ARMOR", "Leather Armor",
                Map.of("ac", 11, "weight", 10, "type", "Light"));
        putLore(TechLevel.TL0, ModuleCategory.ARMOR, "HIDE_ARMOR", "Hide Armor",
                Map.of("ac", 12, "weight", 12, "type", "Light"));
        putLore(TechLevel.TL1, ModuleCategory.ARMOR, "CHAIN_MAIL", "Chain Mail",
                Map.of("ac", 16, "weight", 55, "type", "Heavy"));
        putLore(TechLevel.TL1, ModuleCategory.ARMOR, "SHIELD", "Shield",
                Map.of("acBonus", 2, "weight", 6, "type", "Shield"));
        putLore(TechLevel.TL2, ModuleCategory.ARMOR, "PLATE_ARMOR", "Plate Armor",
                Map.of("ac", 18, "weight", 65, "type", "Heavy"));
        putLore(TechLevel.TL2, ModuleCategory.ARMOR, "CHAIN_SHIRT", "Chain Shirt",
                Map.of("ac", 13, "weight", 20, "type", "Medium"));
        putLore(TechLevel.TL3, ModuleCategory.ARMOR, "BRONZE_PLATE", "Bronze Plate",
                Map.of("ac", 16, "weight", 60, "type", "Heavy"));
        putLore(TechLevel.TL4, ModuleCategory.ARMOR, "MAGIC_PLATE", "Powered Armor",
                Map.of("ac", 19, "weight", 65, "type", "Powered"));
        putLore(TechLevel.TL5, ModuleCategory.ARMOR, "MAGIC_PLATE", "Body Armor",
                Map.of("ac", 17, "weight", 30, "type", "Ballistic"));
        putLore(TechLevel.TL6, ModuleCategory.ARMOR, "MAGIC_PLATE", "Reactive Armor",
                Map.of("ac", 18, "weight", 20, "type", "Reactive"));
        putLore(TechLevel.TL7, ModuleCategory.ARMOR, "MAGIC_PLATE", "Void Suit",
                Map.of("ac", 19, "weight", 15, "type", "Environmental"));
        putLore(TechLevel.TL8, ModuleCategory.ARMOR, "MAGIC_PLATE", "Energy Shield Armor",
                Map.of("ac", 20, "weight", 10, "type", "Energy Shield"));
        putLore(TechLevel.TL9, ModuleCategory.ARMOR, "MAGIC_PLATE", "Force Field Armor",
                Map.of("ac", 22, "weight", 5, "type", "Force Field"));
        putLore(TechLevel.TL10, ModuleCategory.ARMOR, "MAGIC_PLATE", "Nanite Armor",
                Map.of("ac", 25, "weight", 1, "type", "Nanite"));
        putLore(TechLevel.TL11, ModuleCategory.ARMOR, "MAGIC_PLATE", "Dimensional Shield",
                Map.of("ac", 30, "weight", 0, "type", "Dimensional"));
        putLore(TechLevel.TL12, ModuleCategory.ARMOR, "MAGIC_PLATE", "Omniversal Shield",
                Map.of("ac", 50, "weight", 0, "type", "Omniversal"));

        // --- POTION (all TL) ---
        putLore(TechLevel.TL1, ModuleCategory.POTION, "ANTIDOTE", "Antidote",
                Map.of("rarity", "Common", "gpValue", 50, "type", "Consumable"));
        putLore(TechLevel.TL1, ModuleCategory.POTION, "POISON_VIAL", "Poison Vial",
                Map.of("dc", 10, "damage", "2d4", "damageType", "Poison"));
        putLore(TechLevel.TL2, ModuleCategory.POTION, "GREATER_HEALING_POTION", "Healing Salve",
                Map.of("rarity", "Uncommon", "gpValue", 150, "healing", "4d4+4"));
        putLore(TechLevel.TL2, ModuleCategory.POTION, "POISON_VIAL_GREATER", "Greater Poison",
                Map.of("dc", 12, "damage", "4d4", "damageType", "Poison"));
        putLore(TechLevel.TL3, ModuleCategory.POTION, "ANTIDOTE", "Chemical Antidote",
                Map.of("rarity", "Common", "gpValue", 100, "type", "Chemical"));
        putLore(TechLevel.TL4, ModuleCategory.POTION, "GREATER_HEALING_POTION", "Medkit",
                Map.of("rarity", "Common", "gpValue", 200, "healing", "6d4+6"));
        putLore(TechLevel.TL5, ModuleCategory.POTION, "ANTIDOTE", "Auto-Injector Antidote",
                Map.of("rarity", "Common", "gpValue", 200, "type", "Auto-Injector"));
        putLore(TechLevel.TL5, ModuleCategory.POTION, "GREATER_HEALING_POTION", "Medigel",
                Map.of("rarity", "Common", "gpValue", 300, "healing", "8d4+8"));
        putLore(TechLevel.TL6, ModuleCategory.POTION, "ANTIDOTE", "Nanite Antidote",
                Map.of("rarity", "Common", "gpValue", 500, "type", "Nanite"));
        putLore(TechLevel.TL6, ModuleCategory.POTION, "GREATER_HEALING_POTION", "Regeneration Serum",
                Map.of("rarity", "Uncommon", "gpValue", 1000, "healing", "10d4+10"));
        putLore(TechLevel.TL7, ModuleCategory.POTION, "GREATER_HEALING_POTION", "Bio-Regenerator",
                Map.of("rarity", "Uncommon", "gpValue", 2000, "healing", "12d4+12"));
        putLore(TechLevel.TL8, ModuleCategory.POTION, "GREATER_HEALING_POTION", "Cellular Regenerator",
                Map.of("rarity", "Rare", "gpValue", 5000, "healing", "15d4+15"));
        putLore(TechLevel.TL9, ModuleCategory.POTION, "GREATER_HEALING_POTION", "Molecular Healer",
                Map.of("rarity", "Very Rare", "gpValue", 10000, "healing", "20d4+20"));
        putLore(TechLevel.TL10, ModuleCategory.POTION, "GREATER_HEALING_POTION", "Reality Healer",
                Map.of("rarity", "Legendary", "gpValue", 50000, "healing", "30d4+30"));

        // --- MAGIC_ITEM → Tech Items (all TL) ---
        putLore(TechLevel.TL1, ModuleCategory.MAGIC_ITEM, "WAND_OF_WISDOM", "Data Crystal",
                Map.of("rarity", "Uncommon", "type", "Data Storage", "wisdomScore", 10));
        putLore(TechLevel.TL1, ModuleCategory.MAGIC_ITEM, "POTION_OF_HEALING", "First Aid Kit",
                Map.of("rarity", "Common", "type", "Medical", "healing", "2d4+2"));
        putLore(TechLevel.TL2, ModuleCategory.MAGIC_ITEM, "RING_OF_PROTECTION", "Body Sensor",
                Map.of("rarity", "Rare", "type", "Cybernetic", "acBonus", 1, "saveBonus", 1));
        putLore(TechLevel.TL2, ModuleCategory.MAGIC_ITEM, "STAFF_OF_HEALING", "Medical Drone",
                Map.of("rarity", "Rare", "type", "Drone", "healingPerCharge", "1d4"));
        putLore(TechLevel.TL3, ModuleCategory.MAGIC_ITEM, "WINGED_BOOTS", "Grav Boots",
                Map.of("rarity", "Rare", "type", "Boots", "flySpeed", 30));
        putLore(TechLevel.TL3, ModuleCategory.MAGIC_ITEM, "BAG_OF_HOLDING", "Portable Storage Unit",
                Map.of("rarity", "Uncommon", "type", "Container", "capacity", "640 lbs"));
        putLore(TechLevel.TL4, ModuleCategory.MAGIC_ITEM, "STAFF_OF_POWER", "Power Staff",
                Map.of("rarity", "Very Rare", "type", "Weapon", "charges", 20));
        putLore(TechLevel.TL5, ModuleCategory.MAGIC_ITEM, "STAFF_OF_POWER", "Cybernetic Implant",
                Map.of("rarity", "Rare", "type", "Cybernetic", "statBonus", 2));
        putLore(TechLevel.TL6, ModuleCategory.MAGIC_ITEM, "STAFF_OF_POWER", "AI Assistant",
                Map.of("rarity", "Rare", "type", "AI", "intelligenceBonus", 3));
        putLore(TechLevel.TL7, ModuleCategory.MAGIC_ITEM, "STAFF_OF_POWER", "Psychobooster",
                Map.of("rarity", "Very Rare", "type", "Implant", "psiBonus", 3));
        putLore(TechLevel.TL8, ModuleCategory.MAGIC_ITEM, "STAFF_OF_POWER", "Nanite Swarm",
                Map.of("rarity", "Very Rare", "type", "Nanite", "repairRate", 10));
        putLore(TechLevel.TL9, ModuleCategory.MAGIC_ITEM, "STAFF_OF_POWER", "Quantum Core",
                Map.of("rarity", "Legendary", "type", "Power Source", "energyOutput", 10000));
        putLore(TechLevel.TL10, ModuleCategory.MAGIC_ITEM, "STAFF_OF_POWER", "Reality Engine Core",
                Map.of("rarity", "Artifact", "type", "Reality", "powerLevel", 100000));

        // --- NPC (all TL) ---
        putLore(TechLevel.TL0, ModuleCategory.NPC, "INNKEEPER", "Tribal Elder",
                Map.of("role", "Leader", "alignment", "Neutral", "level", 1));
        putLore(TechLevel.TL1, ModuleCategory.NPC, "INNKEEPER", "Innkeeper",
                Map.of("role", "Service", "alignment", "Neutral Good", "level", 1));
        putLore(TechLevel.TL1, ModuleCategory.NPC, "MERCHANT", "Merchant",
                Map.of("role", "Trade", "alignment", "Neutral", "level", 2));
        putLore(TechLevel.TL1, ModuleCategory.NPC, "GUARD_CAPTAIN", "Guard Captain",
                Map.of("role", "Military", "alignment", "Lawful Neutral", "level", 3));
        putLore(TechLevel.TL2, ModuleCategory.NPC, "KNIGHT", "Knight",
                Map.of("role", "Military", "alignment", "Lawful Good", "level", 5));
        putLore(TechLevel.TL2, ModuleCategory.NPC, "WIZARD", "Scholar",
                Map.of("role", "Scholar", "alignment", "Neutral", "level", 7));
        putLore(TechLevel.TL2, ModuleCategory.NPC, "BLACKSMITH", "Blacksmith",
                Map.of("role", "Craftsman", "alignment", "Neutral Good", "level", 3));
        putLore(TechLevel.TL3, ModuleCategory.NPC, "ALCHEMIST", "Chemist",
                Map.of("role", "Scientist", "alignment", "Neutral", "level", 5));
        putLore(TechLevel.TL3, ModuleCategory.NPC, "NOBLE", "Noble",
                Map.of("role", "Leader", "alignment", "Lawful Neutral", "level", 4));
        putLore(TechLevel.TL4, ModuleCategory.NPC, "DRAGONLORD", "Industrial Baron",
                Map.of("role", "Ruler", "alignment", "Lawful Evil", "level", 15));
        putLore(TechLevel.TL4, ModuleCategory.NPC, "ARCHMAGE", "Chief Engineer",
                Map.of("role", "Engineer", "alignment", "Neutral", "level", 15));
        putLore(TechLevel.TL5, ModuleCategory.NPC, "MERCHANT", "Corporate Executive",
                Map.of("role", "Trade", "alignment", "Neutral", "level", 10));
        putLore(TechLevel.TL6, ModuleCategory.NPC, "MERCHANT", "Stellar Trader",
                Map.of("role", "Trade", "alignment", "Neutral", "level", 10));
        putLore(TechLevel.TL6, ModuleCategory.NPC, "GUARD_CAPTAIN", "Security Chief",
                Map.of("role", "Military", "alignment", "Lawful Neutral", "level", 12));
        putLore(TechLevel.TL7, ModuleCategory.NPC, "MERCHANT", "Smuggler",
                Map.of("role", "Smuggler", "alignment", "Chaotic Neutral", "level", 12));
        putLore(TechLevel.TL7, ModuleCategory.NPC, "WIZARD", "Scientist",
                Map.of("role", "Scientist", "alignment", "Neutral", "level", 15));
        putLore(TechLevel.TL8, ModuleCategory.NPC, "MERCHANT", "Galactic Trader",
                Map.of("role", "Trade", "alignment", "Neutral", "level", 15));
        putLore(TechLevel.TL8, ModuleCategory.NPC, "KNIGHT", "Space Marine",
                Map.of("role", "Military", "alignment", "Lawful Good", "level", 18));
        putLore(TechLevel.TL9, ModuleCategory.NPC, "MERCHANT", "Galactic Emperor",
                Map.of("role", "Ruler", "alignment", "Lawful Neutral", "level", 20));
        putLore(TechLevel.TL9, ModuleCategory.NPC, "WIZARD", "AI Overmind",
                Map.of("role", "AI", "alignment", "Neutral", "level", 20));
        putLore(TechLevel.TL10, ModuleCategory.NPC, "ARCHMAGE", "Post-Human Entity",
                Map.of("role", "Entity", "alignment", "Neutral", "level", 20));

        // --- MONSTER (all TL) ---
        putLore(TechLevel.TL0, ModuleCategory.MONSTER, "GIANT_SPIDER", "Giant Spider",
                Map.of("cr", 0.5, "hp", 26, "ac", 12, "type", "Beast", "size", "Large"));
        putLore(TechLevel.TL0, ModuleCategory.MONSTER, "GIANT_WORM", "Giant Worm",
                Map.of("cr", 1, "hp", 45, "ac", 12, "type", "Beast", "size", "Huge"));
        putLore(TechLevel.TL0, ModuleCategory.MONSTER, "KOBOLD", "Giant Rat",
                Map.of("cr", 0.125, "hp", 22, "ac", 12, "type", "Beast", "size", "Small"));
        putLore(TechLevel.TL1, ModuleCategory.MONSTER, "GOBLIN", "Goblin Raider",
                Map.of("cr", 0.25, "hp", 7, "ac", 15, "type", "Humanoid", "size", "Small"));
        putLore(TechLevel.TL1, ModuleCategory.MONSTER, "SKELETON", "Skeleton",
                Map.of("cr", 1, "hp", 13, "ac", 13, "type", "Undead", "size", "Medium"));
        putLore(TechLevel.TL1, ModuleCategory.MONSTER, "ZOMBIE", "Zombie",
                Map.of("cr", 0.125, "hp", 22, "ac", 8, "type", "Undead", "size", "Medium"));
        putLore(TechLevel.TL2, ModuleCategory.MONSTER, "ORC", "Orc Warrior",
                Map.of("cr", 2, "hp", 15, "ac", 13, "type", "Humanoid", "size", "Medium"));
        putLore(TechLevel.TL2, ModuleCategory.MONSTER, "OGRE", "Ogre",
                Map.of("cr", 2, "hp", 59, "ac", 11, "type", "Giant", "size", "Huge"));
        putLore(TechLevel.TL3, ModuleCategory.MONSTER, "MIND_FLAYER", "Mutant Aberration",
                Map.of("cr", 10, "hp", 130, "ac", 13, "type", "Aberration", "size", "Medium"));
        putLore(TechLevel.TL3, ModuleCategory.MONSTER, "DRAGON_WYRM", "Dinosaur",
                Map.of("cr", 6, "hp", 136, "ac", 17, "type", "Beast", "size", "Large"));
        putLore(TechLevel.TL4, ModuleCategory.MONSTER, "ANCIENT_DRAGON", "Mech Dragon",
                Map.of("cr", 24, "hp", 546, "ac", 22, "type", "Construct", "size", "Gargantuan"));
        putLore(TechLevel.TL4, ModuleCategory.MONSTER, "LICH", "Mad Scientist",
                Map.of("cr", 21, "hp", 135, "ac", 17, "type", "Humanoid", "size", "Medium"));
        putLore(TechLevel.TL5, ModuleCategory.MONSTER, "LICH", "Cyborg Assassin",
                Map.of("cr", 15, "hp", 200, "ac", 20, "type", "Construct", "size", "Medium"));
        putLore(TechLevel.TL6, ModuleCategory.MONSTER, "MIND_FLAYER", "Alien Hive Mind",
                Map.of("cr", 15, "hp", 300, "ac", 18, "type", "Alien", "size", "Medium"));
        putLore(TechLevel.TL7, ModuleCategory.MONSTER, "MIND_FLAYER", "Void Creature",
                Map.of("cr", 18, "hp", 500, "ac", 20, "type", "Alien", "size", "Large"));
        putLore(TechLevel.TL8, ModuleCategory.MONSTER, "ANCIENT_DRAGON", "Space Leviathan",
                Map.of("cr", 25, "hp", 1000, "ac", 25, "type", "Alien", "size", "Gargantuan"));
        putLore(TechLevel.TL9, ModuleCategory.MONSTER, "ANCIENT_DRAGON", "Galactic War Machine",
                Map.of("cr", 30, "hp", 5000, "ac", 30, "type", "Construct", "size", "Gargantuan"));
        putLore(TechLevel.TL10, ModuleCategory.MONSTER, "ANCIENT_DRAGON", "Megastructure Defender",
                Map.of("cr", 40, "hp", 100000, "ac", 40, "type", "Construct", "size", "Colossal"));

        // --- ADVENTURE_GEAR (all TL) ---
        putLore(TechLevel.TL0, ModuleCategory.ADVENTURE_GEAR, "TORCH", "Torch",
                Map.of("cost", 0, "weight", 1, "duration", "6 hours"));
        putLore(TechLevel.TL0, ModuleCategory.ADVENTURE_GEAR, "ROPE", "Hemp Rope",
                Map.of("cost", 1, "weight", 10, "length", "50ft"));
        putLore(TechLevel.TL0, ModuleCategory.ADVENTURE_GEAR, "RATIONS", "Rations",
                Map.of("cost", 0, "weight", 2, "duration", "1 day"));
        putLore(TechLevel.TL1, ModuleCategory.ADVENTURE_GEAR, "BACKPACK", "Backpack",
                Map.of("cost", 2, "weight", 5, "capacity", "30-60 lbs"));
        putLore(TechLevel.TL1, ModuleCategory.ADVENTURE_GEAR, "CLIMBERS_KIT", "Climber's Kit",
                Map.of("cost", 25, "weight", 12));
        putLore(TechLevel.TL2, ModuleCategory.ADVENTURE_GEAR, "ALCHEMISTS_FIRE", "Greek Fire",
                Map.of("cost", 50, "weight", 1, "damage", "1d8", "damageType", "Fire"));
        putLore(TechLevel.TL3, ModuleCategory.ADVENTURE_GEAR, "POISON_TIP", "Poison Tip",
                Map.of("cost", 10, "weight", 0.1, "damage", "1d4", "damageType", "Poison"));
        putLore(TechLevel.TL4, ModuleCategory.ADVENTURE_GEAR, "GUNPOWDER", "Gunpowder",
                Map.of("cost", 50, "weight", 1, "damage", "2d6", "damageType", "Fire"));
        putLore(TechLevel.TL5, ModuleCategory.ADVENTURE_GEAR, "TORCH", "Flashlight",
                Map.of("cost", 5, "weight", 0.5, "duration", "24 hours"));
        putLore(TechLevel.TL5, ModuleCategory.ADVENTURE_GEAR, "RATIONS", "MRE Rations",
                Map.of("cost", 5, "weight", 1, "duration", "1 day"));
        putLore(TechLevel.TL6, ModuleCategory.ADVENTURE_GEAR, "TORCH", "Plasma Lamp",
                Map.of("cost", 50, "weight", 0.5, "duration", "100 hours"));
        putLore(TechLevel.TL6, ModuleCategory.ADVENTURE_GEAR, "RATIONS", "Nutrient Paste",
                Map.of("cost", 10, "weight", 0.5, "duration", "1 day"));
        putLore(TechLevel.TL6, ModuleCategory.ADVENTURE_GEAR, "BACKPACK", "Survival Kit",
                Map.of("cost", 100, "weight", 10, "capacity", "Emergency supplies"));
        putLore(TechLevel.TL7, ModuleCategory.ADVENTURE_GEAR, "BACKPACK", "Void Suit",
                Map.of("cost", 5000, "weight", 20, "duration", "72 hours"));
        putLore(TechLevel.TL8, ModuleCategory.ADVENTURE_GEAR, "BACKPACK", "Environmental Suit",
                Map.of("cost", 10000, "weight", 15, "duration", "168 hours"));
        putLore(TechLevel.TL9, ModuleCategory.ADVENTURE_GEAR, "BACKPACK", "Personal Force Field",
                Map.of("cost", 50000, "weight", 1, "duration", "Unlimited"));

        // --- DUNGEON_ROOM → Space Rooms (all TL) ---
        putLore(TechLevel.TL0, ModuleCategory.DUNGEON_ROOM, "CAVE_ENTRANCE", "Cave Entrance",
                Map.of("size", "Medium", "light", "Dim"));
        putLore(TechLevel.TL0, ModuleCategory.DUNGEON_ROOM, "CAVE_CHAMBER", "Cave Chamber",
                Map.of("size", "Large", "light", "Dark"));
        putLore(TechLevel.TL1, ModuleCategory.DUNGEON_ROOM, "TEMPLE_HALL", "Temple Hall",
                Map.of("size", "Large", "light", "Dim"));
        putLore(TechLevel.TL1, ModuleCategory.DUNGEON_ROOM, "TOMB_CHAMBER", "Tomb Chamber",
                Map.of("size", "Large", "light", "Dark"));
        putLore(TechLevel.TL2, ModuleCategory.DUNGEON_ROOM, "CASTLE_HALL", "Castle Great Hall",
                Map.of("size", "Huge", "light", "Dim"));
        putLore(TechLevel.TL2, ModuleCategory.DUNGEON_ROOM, "DUNGEON_CELL", "Prison Cell",
                Map.of("size", "Small", "light", "Dark"));
        putLore(TechLevel.TL3, ModuleCategory.DUNGEON_ROOM, "ARCANE_LAB", "Chemistry Lab",
                Map.of("size", "Large", "light", "Dim"));
        putLore(TechLevel.TL4, ModuleCategory.DUNGEON_ROOM, "STEAM_FACTORY", "Factory Floor",
                Map.of("size", "Huge", "light", "Dim"));
        putLore(TechLevel.TL5, ModuleCategory.DUNGEON_ROOM, "DUNGEON_CELL", "Interrogation Room",
                Map.of("size", "Small", "light", "Bright"));
        putLore(TechLevel.TL6, ModuleCategory.DUNGEON_ROOM, "CASTLE_HALL", "Command Center",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL6, ModuleCategory.DUNGEON_ROOM, "ARCANE_LAB", "Research Lab",
                Map.of("size", "Large", "light", "Bright"));
        putLore(TechLevel.TL7, ModuleCategory.DUNGEON_ROOM, "CASTLE_HALL", "Ship Bridge",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL7, ModuleCategory.DUNGEON_ROOM, "DUNGEON_CELL", "Airlock",
                Map.of("size", "Small", "light", "Bright"));
        putLore(TechLevel.TL8, ModuleCategory.DUNGEON_ROOM, "CASTLE_HALL", "Station Command Ring",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL8, ModuleCategory.DUNGEON_ROOM, "ARCANE_LAB", "Science Lab",
                Map.of("size", "Large", "light", "Bright"));
        putLore(TechLevel.TL9, ModuleCategory.DUNGEON_ROOM, "CASTLE_HALL", "Galactic Command",
                Map.of("size", "Colossal", "light", "Bright"));

        // --- New sci-fi DUNGEON_ROOM types (TL5-TL12) ---
        putLore(TechLevel.TL5, ModuleCategory.DUNGEON_ROOM, "OFFICE_LOBBY", "Office Lobby",
                Map.of("size", "Large", "light", "Bright"));
        putLore(TechLevel.TL5, ModuleCategory.DUNGEON_ROOM, "CONFERENCE_ROOM", "Conference Room",
                Map.of("size", "Medium", "light", "Bright"));
        putLore(TechLevel.TL5, ModuleCategory.DUNGEON_ROOM, "SERVER_ROOM", "Server Room",
                Map.of("size", "Medium", "light", "Bright"));
        putLore(TechLevel.TL5, ModuleCategory.DUNGEON_ROOM, "SURVEILLANCE_CENTER", "Surveillance Center",
                Map.of("size", "Large", "light", "Bright"));
        putLore(TechLevel.TL6, ModuleCategory.DUNGEON_ROOM, "COMMAND_CENTER", "Command Center",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL6, ModuleCategory.DUNGEON_ROOM, "RESEARCH_LAB", "Research Laboratory",
                Map.of("size", "Large", "light", "Bright"));
        putLore(TechLevel.TL6, ModuleCategory.DUNGEON_ROOM, "HOVER_DEPOT", "Hover Vehicle Depot",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL6, ModuleCategory.DUNGEON_ROOM, "ORBITAL_CONTROL", "Orbital Control Room",
                Map.of("size", "Large", "light", "Bright"));
        putLore(TechLevel.TL6, ModuleCategory.DUNGEON_ROOM, "MEDBAY", "Medical Bay",
                Map.of("size", "Medium", "light", "Bright"));
        putLore(TechLevel.TL7, ModuleCategory.DUNGEON_ROOM, "SHIP_BRIDGE", "Ship Bridge",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL7, ModuleCategory.DUNGEON_ROOM, "FUSION_CORE", "Fusion Reactor Core",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL7, ModuleCategory.DUNGEON_ROOM, "ASTEROID_MINE_SHAFT", "Asteroid Mine Shaft",
                Map.of("size", "Large", "light", "Dim"));
        putLore(TechLevel.TL7, ModuleCategory.DUNGEON_ROOM, "BIOTECH_LAB", "Biotech Laboratory",
                Map.of("size", "Large", "light", "Bright"));
        putLore(TechLevel.TL7, ModuleCategory.DUNGEON_ROOM, "DOCKING_BAY", "Docking Bay",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL7, ModuleCategory.DUNGEON_ROOM, "ZERO_G_CHAMBER", "Zero-G Chamber",
                Map.of("size", "Large", "light", "Bright"));
        putLore(TechLevel.TL8, ModuleCategory.DUNGEON_ROOM, "STARSHIP_BRIDGE", "Starship Bridge",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL8, ModuleCategory.DUNGEON_ROOM, "ANTIMATTER_CORE", "Antimatter Reactor Core",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL8, ModuleCategory.DUNGEON_ROOM, "NANOFAB_BAY", "Nanofabrication Bay",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL8, ModuleCategory.DUNGEON_ROOM, "JUMP_GATE_CONTROL", "Jump Gate Control",
                Map.of("size", "Large", "light", "Bright"));
        putLore(TechLevel.TL8, ModuleCategory.DUNGEON_ROOM, "OBSERVATION_DECK", "Observation Deck",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL8, ModuleCategory.DUNGEON_ROOM, "ENGINEERING", "Engineering Deck",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL8, ModuleCategory.DUNGEON_ROOM, "TRANSPORTER_ROOM", "Transporter Room",
                Map.of("size", "Medium", "light", "Bright"));
        putLore(TechLevel.TL9, ModuleCategory.DUNGEON_ROOM, "STARBASE_COMMAND", "Starbase Command Center",
                Map.of("size", "Colossal", "light", "Bright"));
        putLore(TechLevel.TL9, ModuleCategory.DUNGEON_ROOM, "AI_CORE_CHAMBER", "AI Core Chamber",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL9, ModuleCategory.DUNGEON_ROOM, "WORMHOLE_TERMINAL", "Wormhole Terminal",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL9, ModuleCategory.DUNGEON_ROOM, "DREADNOUGHT_BRIDGE", "Dreadnought Bridge",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL9, ModuleCategory.DUNGEON_ROOM, "PLANETARY_COMPUTER", "Planetary Computer Core",
                Map.of("size", "Colossal", "light", "Bright"));
        putLore(TechLevel.TL10, ModuleCategory.DUNGEON_ROOM, "MEGASTRUCTURE_HUB", "Megastructure Control Hub",
                Map.of("size", "Colossal", "light", "Bright"));
        putLore(TechLevel.TL10, ModuleCategory.DUNGEON_ROOM, "REALITY_ENGINE_ROOM", "Reality Engine Chamber",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL10, ModuleCategory.DUNGEON_ROOM, "MATRIOSKA_CORE", "Matrioshka Brain Core",
                Map.of("size", "Colossal", "light", "Bright"));
        putLore(TechLevel.TL11, ModuleCategory.DUNGEON_ROOM, "DIMENSIONAL_HUB", "Dimensional Hub",
                Map.of("size", "Colossal", "light", "Bright"));
        putLore(TechLevel.TL11, ModuleCategory.DUNGEON_ROOM, "TIME_CHAMBER", "Time Manipulation Chamber",
                Map.of("size", "Huge", "light", "Bright"));
        putLore(TechLevel.TL11, ModuleCategory.DUNGEON_ROOM, "UNIVERSE_SEEDER", "Universe Seeder Core",
                Map.of("size", "Colossal", "light", "Bright"));
        putLore(TechLevel.TL12, ModuleCategory.DUNGEON_ROOM, "OMNIVERSAL_THRONE", "Omniversal Throne Room",
                Map.of("size", "Colossal", "light", "Bright"));
        putLore(TechLevel.TL12, ModuleCategory.DUNGEON_ROOM, "COSMIC_LOOM_CHAMBER", "Cosmic Loom Chamber",
                Map.of("size", "Colossal", "light", "Bright"));
        putLore(TechLevel.TL12, ModuleCategory.DUNGEON_ROOM, "ABSOLUTE_KNOWLEDGE", "Absolute Knowledge Archive",
                Map.of("size", "Colossal", "light", "Bright"));

        // --- FURNITURE → Space Furniture (all TL) ---
        putLore(TechLevel.TL0, ModuleCategory.FURNITURE, "STONE_ALTAR", "Stone Altar",
                Map.of("hp", 20, "ac", 5));
        putLore(TechLevel.TL0, ModuleCategory.FURNITURE, "FIRE_PIT", "Fire Pit",
                Map.of("hp", 5, "light", "10ft"));
        putLore(TechLevel.TL1, ModuleCategory.FURNITURE, "STONE_THRONE", "Stone Throne",
                Map.of("hp", 50, "ac", 5));
        putLore(TechLevel.TL1, ModuleCategory.FURNITURE, "WOODEN_TABLE", "Wooden Table",
                Map.of("hp", 15, "ac", 5, "weight", 20));
        putLore(TechLevel.TL2, ModuleCategory.FURNITURE, "GOLDEN_THRONE", "Golden Throne",
                Map.of("hp", 30, "ac", 5, "gpValue", 200));
        putLore(TechLevel.TL3, ModuleCategory.FURNITURE, "BOOKSHELF", "Bookshelf",
                Map.of("hp", 10, "ac", 5, "weight", 50));
        putLore(TechLevel.TL4, ModuleCategory.FURNITURE, "TELEGRAPH", "Telegraph Machine",
                Map.of("hp", 15, "ac", 5, "weight", 30));
        putLore(TechLevel.TL5, ModuleCategory.FURNITURE, "STONE_THRONE", "Command Chair",
                Map.of("hp", 30, "ac", 10, "weight", 50));
        putLore(TechLevel.TL6, ModuleCategory.FURNITURE, "STONE_THRONE", "Captain's Chair",
                Map.of("hp", 50, "ac", 15, "weight", 100));
        putLore(TechLevel.TL6, ModuleCategory.FURNITURE, "WOODEN_TABLE", "Holo-Table",
                Map.of("hp", 20, "ac", 10, "weight", 50));
        putLore(TechLevel.TL7, ModuleCategory.FURNITURE, "STONE_THRONE", "Command Console",
                Map.of("hp", 100, "ac", 20, "weight", 200));
        putLore(TechLevel.TL7, ModuleCategory.FURNITURE, "WOODEN_TABLE", "Data Console",
                Map.of("hp", 50, "ac", 15, "weight", 100));
        putLore(TechLevel.TL8, ModuleCategory.FURNITURE, "STONE_THRONE", "AI Interface Terminal",
                Map.of("hp", 200, "ac", 25, "weight", 500));

        // --- New sci-fi FURNITURE types (TL5-TL12) ---
        putLore(TechLevel.TL5, ModuleCategory.FURNITURE, "OFFICE_DESK", "Office Desk",
                Map.of("hp", 20, "ac", 10, "weight", 50));
        putLore(TechLevel.TL5, ModuleCategory.FURNITURE, "OFFICE_CHAIR", "Ergonomic Chair",
                Map.of("hp", 10, "ac", 10, "weight", 30));
        putLore(TechLevel.TL5, ModuleCategory.FURNITURE, "SERVER_RACK", "Server Rack",
                Map.of("hp", 50, "ac", 15, "weight", 500));
        putLore(TechLevel.TL5, ModuleCategory.FURNITURE, "SECURITY_CONSOLE", "Security Console",
                Map.of("hp", 30, "ac", 12, "weight", 100));
        putLore(TechLevel.TL6, ModuleCategory.FURNITURE, "HOLO_TABLE", "Holographic Table",
                Map.of("hp", 20, "ac", 10, "weight", 200));
        putLore(TechLevel.TL6, ModuleCategory.FURNITURE, "CRYO_POD", "Cryogenic Pod",
                Map.of("hp", 100, "ac", 20, "weight", 1000));
        putLore(TechLevel.TL6, ModuleCategory.FURNITURE, "DIAGNOSTIC_BED", "Diagnostic Med-Bed",
                Map.of("hp", 50, "ac", 15, "weight", 300));
        putLore(TechLevel.TL6, ModuleCategory.FURNITURE, "CONSOLE_TERMINAL", "Operator Console",
                Map.of("hp", 30, "ac", 12, "weight", 150));
        putLore(TechLevel.TL7, ModuleCategory.FURNITURE, "COMMAND_CHAIR", "Captain's Command Chair",
                Map.of("hp", 100, "ac", 20, "weight", 400));
        putLore(TechLevel.TL7, ModuleCategory.FURNITURE, "REPPLICATOR", "Matter Replicator",
                Map.of("hp", 50, "ac", 15, "weight", 500));
        putLore(TechLevel.TL7, ModuleCategory.FURNITURE, "NAVIGATION_CONSOLE", "Navigation Console",
                Map.of("hp", 40, "ac", 18, "weight", 250));
        putLore(TechLevel.TL7, ModuleCategory.FURNITURE, "CONTAINMENT_UNIT", "Bio-Containment Unit",
                Map.of("hp", 80, "ac", 22, "weight", 200));
        putLore(TechLevel.TL8, ModuleCategory.FURNITURE, "HOLODECK_EMITTER", "Holodeck Emitter",
                Map.of("hp", 100, "ac", 20, "weight", 1000));
        putLore(TechLevel.TL8, ModuleCategory.FURNITURE, "TRICORDER", "Handheld Tricorder",
                Map.of("hp", 10, "ac", 15, "weight", 2));
        putLore(TechLevel.TL8, ModuleCategory.FURNITURE, "WARP_CONSOLE", "Warp Drive Console",
                Map.of("hp", 60, "ac", 22, "weight", 300));
        putLore(TechLevel.TL8, ModuleCategory.FURNITURE, "SHIELD_GENERATOR", "Shield Generator",
                Map.of("hp", 200, "ac", 25, "weight", 500));
        putLore(TechLevel.TL9, ModuleCategory.FURNITURE, "AI_INTERFACE", "AI Interface Terminal",
                Map.of("hp", 150, "ac", 28, "weight", 100));
        putLore(TechLevel.TL9, ModuleCategory.FURNITURE, "WORMHOLE_CONSOLE", "Wormhole Control Console",
                Map.of("hp", 100, "ac", 25, "weight", 500));
        putLore(TechLevel.TL9, ModuleCategory.FURNITURE, "GALACTIC_MAP", "Galactic Holographic Map",
                Map.of("hp", 50, "ac", 20, "weight", 2000));
        putLore(TechLevel.TL10, ModuleCategory.FURNITURE, "REALITY_CONSOLE", "Reality Engine Console",
                Map.of("hp", 500, "ac", 30, "weight", 0));
        putLore(TechLevel.TL10, ModuleCategory.FURNITURE, "STAR_HARVESTER", "Star Harvester Control",
                Map.of("hp", 1000, "ac", 35, "weight", 0));
        putLore(TechLevel.TL11, ModuleCategory.FURNITURE, "TIME_CONSOLE", "Temporal Control Console",
                Map.of("hp", 2000, "ac", 40, "weight", 0));
        putLore(TechLevel.TL11, ModuleCategory.FURNITURE, "DIMENSIONAL_VIEWER", "Dimensional Viewer",
                Map.of("hp", 1000, "ac", 35, "weight", 0));
        putLore(TechLevel.TL12, ModuleCategory.FURNITURE, "OMNIVERSAL_THRONE", "Omniversal Throne",
                Map.of("hp", 10000, "ac", 50, "weight", 0));
        putLore(TechLevel.TL12, ModuleCategory.FURNITURE, "COSMIC_LOOM", "Cosmic Loom",
                Map.of("hp", 50000, "ac", 50, "weight", 0));

        // --- TRAP → Security Traps (all TL) ---
        putLore(TechLevel.TL0, ModuleCategory.TRAP, "SPIKE_PIT", "Spike Pit",
                Map.of("dc", 10, "damage", "1d8", "damageType", "Piercing"));
        putLore(TechLevel.TL0, ModuleCategory.TRAP, "FALLING_ROCK", "Falling Rock Trap",
                Map.of("dc", 8, "damage", "2d6", "damageType", "Bludgeoning"));
        putLore(TechLevel.TL1, ModuleCategory.TRAP, "POISON_DART", "Poison Dart Trap",
                Map.of("dc", 13, "damage", "2d4", "damageType", "Poison"));
        putLore(TechLevel.TL1, ModuleCategory.TRAP, "PRESSURE_PLATE", "Pressure Plate Trap",
                Map.of("dc", 15, "damage", "1d10", "damageType", "Piercing"));
        putLore(TechLevel.TL2, ModuleCategory.TRAP, "FALLING_ROCKS", "Falling Rocks Trap",
                Map.of("dc", 13, "damage", "3d8", "damageType", "Bludgeoning"));
        putLore(TechLevel.TL2, ModuleCategory.TRAP, "ARROW_TRAP", "Arrow Trap",
                Map.of("dc", 14, "damage", "2d8", "damageType", "Piercing"));
        putLore(TechLevel.TL3, ModuleCategory.TRAP, "FIRE_TRAP", "Fire Trap",
                Map.of("dc", 14, "damage", "4d6", "damageType", "Fire"));
        putLore(TechLevel.TL4, ModuleCategory.TRAP, "STEAM_VENT", "Steam Vent Trap",
                Map.of("dc", 13, "damage", "4d6", "damageType", "Fire"));
        putLore(TechLevel.TL5, ModuleCategory.TRAP, "PRESSURE_PLATE", "Motion Sensor Trap",
                Map.of("dc", 15, "damage", "3d6", "damageType", "Piercing"));
        putLore(TechLevel.TL6, ModuleCategory.TRAP, "PRESSURE_PLATE", "Laser Grid",
                Map.of("dc", 16, "damage", "4d6", "damageType", "Energy"));
        putLore(TechLevel.TL7, ModuleCategory.TRAP, "PRESSURE_PLATE", "EMP Trap",
                Map.of("dc", 17, "damage", "5d6", "damageType", "Energy"));
        putLore(TechLevel.TL8, ModuleCategory.TRAP, "PRESSURE_PLATE", "Containment Field",
                Map.of("dc", 18, "damage", "6d6", "damageType", "Energy"));
        putLore(TechLevel.TL9, ModuleCategory.TRAP, "PRESSURE_PLATE", "Antimatter Trap",
                Map.of("dc", 20, "damage", "10d6", "damageType", "Energy"));

        // --- New sci-fi TRAP types (TL5-TL12) ---
        putLore(TechLevel.TL5, ModuleCategory.TRAP, "ELECTRIC_FENCE", "Electric Fence",
                Map.of("dc", 12, "damage", "2d6", "damageType", "Lightning"));
        putLore(TechLevel.TL5, ModuleCategory.TRAP, "CCTV_ALARM", "CCTV Alarm Trigger",
                Map.of("dc", 10, "damage", "0", "damageType", "None"));
        putLore(TechLevel.TL6, ModuleCategory.TRAP, "LASER_GRID", "Laser Grid",
                Map.of("dc", 16, "damage", "4d6", "damageType", "Energy"));
        putLore(TechLevel.TL6, ModuleCategory.TRAP, "EMP_PULSE", "EMP Pulse Trap",
                Map.of("dc", 14, "damage", "3d6", "damageType", "Energy"));
        putLore(TechLevel.TL6, ModuleCategory.TRAP, "GAS_VENT", "Neurotoxin Gas Vent",
                Map.of("dc", 15, "damage", "5d6", "damageType", "Poison"));
        putLore(TechLevel.TL7, ModuleCategory.TRAP, "CONTAINMENT_FIELD", "Containment Field",
                Map.of("dc", 18, "damage", "6d6", "damageType", "Force"));
        putLore(TechLevel.TL7, ModuleCategory.TRAP, "GRAVITY_WELL", "Gravity Well Trap",
                Map.of("dc", 17, "damage", "5d6", "damageType", "Bludgeoning"));
        putLore(TechLevel.TL7, ModuleCategory.TRAP, "AUTO_TURRET", "Auto-Turret",
                Map.of("dc", 16, "damage", "4d6", "damageType", "Piercing"));
        putLore(TechLevel.TL8, ModuleCategory.TRAP, "PHOTON_TORPEDO", "Photon Torpedo Trap",
                Map.of("dc", 20, "damage", "8d6", "damageType", "Energy"));
        putLore(TechLevel.TL8, ModuleCategory.TRAP, "SHIELD_DISRUPTOR", "Shield Disruptor",
                Map.of("dc", 18, "damage", "6d6", "damageType", "Force"));
        putLore(TechLevel.TL8, ModuleCategory.TRAP, "NANITE_SWARM", "Nanite Swarm Trap",
                Map.of("dc", 19, "damage", "7d6", "damageType", "Acid"));
        putLore(TechLevel.TL9, ModuleCategory.TRAP, "ANTIMATTER_BOMB", "Antimatter Bomb Trap",
                Map.of("dc", 22, "damage", "12d6", "damageType", "Force"));
        putLore(TechLevel.TL9, ModuleCategory.TRAP, "REALITY_GLITCH", "Reality Glitch Field",
                Map.of("dc", 21, "damage", "10d6", "damageType", "Force"));
        putLore(TechLevel.TL10, ModuleCategory.TRAP, "REALITY_COLLAPSE", "Reality Collapse Field",
                Map.of("dc", 25, "damage", "20d6", "damageType", "Force"));
        putLore(TechLevel.TL11, ModuleCategory.TRAP, "TIME_LOOP", "Time Loop Trap",
                Map.of("dc", 28, "damage", "15d6", "damageType", "Force"));
        putLore(TechLevel.TL12, ModuleCategory.TRAP, "EXISTENCE_ERASURE", "Existence Erasure Field",
                Map.of("dc", 30, "damage", "30d6", "damageType", "Force"));

        // --- DOOR → Airlocks (all TL) ---
        putLore(TechLevel.TL0, ModuleCategory.DOOR, "WOODEN_DOOR", "Wooden Door",
                Map.of("hp", 15, "ac", 10, "classDC", 10));
        putLore(TechLevel.TL1, ModuleCategory.DOOR, "IRON_DOOR", "Iron Door",
                Map.of("hp", 45, "ac", 15, "classDC", 15));
        putLore(TechLevel.TL1, ModuleCategory.DOOR, "SECRET_DOOR", "Secret Door",
                Map.of("hp", 15, "ac", 10, "classDC", 15));
        putLore(TechLevel.TL2, ModuleCategory.DOOR, "PORTCULLIS", "Portcullis",
                Map.of("hp", 60, "ac", 20, "classDC", 20));
        putLore(TechLevel.TL3, ModuleCategory.DOOR, "MAGIC_DOOR", "Reinforced Door",
                Map.of("hp", 80, "ac", 18, "classDC", 18));
        putLore(TechLevel.TL4, ModuleCategory.DOOR, "PORTCULLIS", "Blast Door",
                Map.of("hp", 100, "ac", 20, "classDC", 20));
        putLore(TechLevel.TL5, ModuleCategory.DOOR, "PORTCULLIS", "Security Door",
                Map.of("hp", 150, "ac", 22, "classDC", 22));
        putLore(TechLevel.TL6, ModuleCategory.DOOR, "PORTCULLIS", "Blast Door",
                Map.of("hp", 200, "ac", 25, "classDC", 25));
        putLore(TechLevel.TL7, ModuleCategory.DOOR, "PORTCULLIS", "Airlock Door",
                Map.of("hp", 300, "ac", 28, "classDC", 28));
        putLore(TechLevel.TL8, ModuleCategory.DOOR, "PORTCULLIS", "Force Field Door",
                Map.of("hp", 500, "ac", 30, "classDC", 30));
        putLore(TechLevel.TL9, ModuleCategory.DOOR, "PORTCULLIS", "Energy Barrier",
                Map.of("hp", 1000, "ac", 35, "classDC", 35));

        // --- New sci-fi DOOR types (TL5-TL12) ---
        putLore(TechLevel.TL5, ModuleCategory.DOOR, "ELECTRONIC_DOOR", "Electronic Door",
                Map.of("hp", 50, "ac", 15, "classDC", 8));
        putLore(TechLevel.TL5, ModuleCategory.DOOR, "SECURITY_GATE", "Security Gate",
                Map.of("hp", 100, "ac", 18, "classDC", 12));
        putLore(TechLevel.TL6, ModuleCategory.DOOR, "BLAST_DOOR", "Blast Door",
                Map.of("hp", 200, "ac", 25, "classDC", 15));
        putLore(TechLevel.TL6, ModuleCategory.DOOR, "AIRLOCK", "Airlock Door",
                Map.of("hp", 150, "ac", 22, "classDC", 14));
        putLore(TechLevel.TL7, ModuleCategory.DOOR, "FORCE_FIELD_DOOR", "Force Field Door",
                Map.of("hp", 100, "ac", 28, "classDC", 16));
        putLore(TechLevel.TL7, ModuleCategory.DOOR, "HERMETIC_DOOR", "Hermetic Seal Door",
                Map.of("hp", 180, "ac", 25, "classDC", 15));
        putLore(TechLevel.TL8, ModuleCategory.DOOR, "ENERGY_DOOR", "Energy Door",
                Map.of("hp", 150, "ac", 30, "classDC", 18));
        putLore(TechLevel.TL8, ModuleCategory.DOOR, "TRANSPORTER_LOCK", "Transporter Lock",
                Map.of("hp", 100, "ac", 28, "classDC", 17));
        putLore(TechLevel.TL9, ModuleCategory.DOOR, "DIMENSIONAL_DOOR", "Dimensional Door",
                Map.of("hp", 200, "ac", 35, "classDC", 20));
        putLore(TechLevel.TL10, ModuleCategory.DOOR, "TRANSWARP_GATE", "Transwarp Gate",
                Map.of("hp", 500, "ac", 40, "classDC", 25));
        putLore(TechLevel.TL11, ModuleCategory.DOOR, "DIMENSIONAL_PORTAL", "Dimensional Portal",
                Map.of("hp", 1000, "ac", 45, "classDC", 28));
        putLore(TechLevel.TL12, ModuleCategory.DOOR, "OMNIVERSAL_GATE", "Omniversal Gate",
                Map.of("hp", 10000, "ac", 50, "classDC", 30));

        // --- CHEST → Containers (all TL) ---
        putLore(TechLevel.TL0, ModuleCategory.CHEST, "WOODEN_BOX", "Wooden Box",
                Map.of("hp", 5, "ac", 5, "capacity", "Small"));
        putLore(TechLevel.TL1, ModuleCategory.CHEST, "IRON_BANDED_CHEST", "Iron-Banded Chest",
                Map.of("hp", 15, "ac", 10, "capacity", "Medium"));
        putLore(TechLevel.TL1, ModuleCategory.CHEST, "STRONGBOX", "Strongbox",
                Map.of("hp", 20, "ac", 12, "capacity", "Small"));
        putLore(TechLevel.TL2, ModuleCategory.CHEST, "TREASURE_CHEST", "Treasure Chest",
                Map.of("hp", 20, "ac", 10, "capacity", "Large"));
        putLore(TechLevel.TL3, ModuleCategory.CHEST, "TREASURE_CHEST", "Secure Locker",
                Map.of("hp", 30, "ac", 15, "capacity", "Large"));
        putLore(TechLevel.TL4, ModuleCategory.CHEST, "TREASURE_CHEST", "Cargo Container",
                Map.of("hp", 50, "ac", 18, "capacity", "Huge"));
        putLore(TechLevel.TL5, ModuleCategory.CHEST, "TREASURE_CHEST", "Secure Container",
                Map.of("hp", 100, "ac", 20, "capacity", "Huge"));
        putLore(TechLevel.TL6, ModuleCategory.CHEST, "TREASURE_CHEST", "Cargo Pod",
                Map.of("hp", 200, "ac", 22, "capacity", "Huge"));
        putLore(TechLevel.TL7, ModuleCategory.CHEST, "TREASURE_CHEST", "Vault Container",
                Map.of("hp", 500, "ac", 25, "capacity", "Huge"));
        putLore(TechLevel.TL8, ModuleCategory.CHEST, "TREASURE_CHEST", "Quantum Storage",
                Map.of("hp", 1000, "ac", 30, "capacity", "Dimensional"));

        // --- New sci-fi CHEST types (TL5-TL12) ---
        putLore(TechLevel.TL5, ModuleCategory.CHEST, "SAFE", "Electronic Safe",
                Map.of("hp", 100, "ac", 20, "capacity", "Small"));
        putLore(TechLevel.TL5, ModuleCategory.CHEST, "LOCKER", "Secure Locker",
                Map.of("hp", 30, "ac", 15, "capacity", "Small"));
        putLore(TechLevel.TL6, ModuleCategory.CHEST, "CARGO_CONTAINER", "Cargo Container",
                Map.of("hp", 200, "ac", 22, "capacity", "Huge"));
        putLore(TechLevel.TL6, ModuleCategory.CHEST, "VAULT", "Secure Vault",
                Map.of("hp", 500, "ac", 25, "capacity", "Large"));
        putLore(TechLevel.TL7, ModuleCategory.CHEST, "CRYO_STORAGE", "Cryogenic Storage Unit",
                Map.of("hp", 300, "ac", 25, "capacity", "Large"));
        putLore(TechLevel.TL7, ModuleCategory.CHEST, "ORBITAL_LOCKER", "Orbital Cargo Locker",
                Map.of("hp", 150, "ac", 22, "capacity", "Medium"));
        putLore(TechLevel.TL8, ModuleCategory.CHEST, "ANTIMATTER_CANISTER", "Antimatter Canister",
                Map.of("hp", 500, "ac", 30, "capacity", "Small"));
        putLore(TechLevel.TL8, ModuleCategory.CHEST, "QUANTUM_VAULT", "Quantum Vault",
                Map.of("hp", 1000, "ac", 30, "capacity", "Dimensional"));
        putLore(TechLevel.TL9, ModuleCategory.CHEST, "DIMENSIONAL_VAULT", "Dimensional Vault",
                Map.of("hp", 2000, "ac", 35, "capacity", "Dimensional"));
        putLore(TechLevel.TL10, ModuleCategory.CHEST, "OMNIVERSAL_CONTAINER", "Omniversal Container",
                Map.of("hp", 5000, "ac", 40, "capacity", "Omniversal"));
        putLore(TechLevel.TL11, ModuleCategory.CHEST, "TIMELINE_VAULT", "Timeline Vault",
                Map.of("hp", 10000, "ac", 45, "capacity", "Temporal"));
        putLore(TechLevel.TL12, ModuleCategory.CHEST, "OMNIVERSAL_VAULT", "Omniversal Vault",
                Map.of("hp", 100000, "ac", 50, "capacity", "Omniversal"));

        // --- SPELL → Tech Abilities (all TL) ---
        putLore(TechLevel.TL1, ModuleCategory.SPELL, "MAGIC_MISSILE", "Signal Flare",
                Map.of("level", 1, "school", "Signaling", "range", "120ft"));
        putLore(TechLevel.TL1, ModuleCategory.SPELL, "SHIELD", "Smoke Screen",
                Map.of("level", 1, "school", "Concealment", "range", "Self", "duration", "1 round"));
        putLore(TechLevel.TL2, ModuleCategory.SPELL, "FIREBALL", "Explosive Charge",
                Map.of("level", 3, "school", "Explosive", "range", "150ft", "damage", "8d6", "damageType", "Fire"));
        putLore(TechLevel.TL2, ModuleCategory.SPELL, "INVISIBILITY", "Cloaking Device",
                Map.of("level", 2, "school", "Stealth", "range", "Self", "duration", "1 hour"));
        putLore(TechLevel.TL3, ModuleCategory.SPELL, "COUNTERSPELL", "Electronic Countermeasures",
                Map.of("level", 3, "school", "Electronic Warfare", "range", "60ft"));
        putLore(TechLevel.TL3, ModuleCategory.SPELL, "TELEPORT", "Teleporter",
                Map.of("level", 5, "school", "Transport", "range", "10ft"));
        putLore(TechLevel.TL4, ModuleCategory.SPELL, "WISH", "AI Override",
                Map.of("level", 9, "school", "Hacking", "range", "150ft"));
        putLore(TechLevel.TL5, ModuleCategory.SPELL, "TELEPORT", "Drone Deployment",
                Map.of("level", 3, "school", "Drone", "range", "1000ft"));
        putLore(TechLevel.TL6, ModuleCategory.SPELL, "TELEPORT", "Hacking",
                Map.of("level", 3, "school", "Cyber", "range", "10000ft"));
        putLore(TechLevel.TL7, ModuleCategory.SPELL, "TELEPORT", "Psychic Pulse",
                Map.of("level", 5, "school", "Psi", "range", "100ft"));
        putLore(TechLevel.TL8, ModuleCategory.SPELL, "TELEPORT", "Nanite Injection",
                Map.of("level", 5, "school", "Nanite", "range", "Touch"));
        putLore(TechLevel.TL9, ModuleCategory.SPELL, "TELEPORT", "Quantum Manipulation",
                Map.of("level", 7, "school", "Quantum", "range", "1000ft"));
        putLore(TechLevel.TL10, ModuleCategory.SPELL, "TELEPORT", "Reality Warping",
                Map.of("level", 9, "school", "Reality", "range", "Unlimited"));

        // === ANIMAL mappings (all TL, Protean-style names) ===
        // Livestock
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "COW", "Protean",
                Map.of("milkYield", 20, "meatYield", 200, "type", "Protean", "size", "Large"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "PIG", "Protean Hog",
                Map.of("meatYield", 80, "type", "Protean", "size", "Medium"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "SHEEP", "Protean Fleece",
                Map.of("milkYield", 2, "woolYield", 3, "meatYield", 30, "type", "Protean", "size", "Medium"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "GOAT", "Protean Goat",
                Map.of("milkYield", 3, "woolYield", 1, "meatYield", 25, "type", "Protean", "size", "Medium"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "CHICKEN", "Protean Fowl",
                Map.of("eggRate", 5, "meatYield", 2, "type", "Protean", "size", "Tiny"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "DUCK", "Protean Waterfowl",
                Map.of("eggRate", 4, "meatYield", 1.5, "type", "Protean", "size", "Tiny"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "TURKEY", "Protean Turkey",
                Map.of("eggRate", 3, "meatYield", 15, "type", "Protean", "size", "Small"));
        // Transport
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "HORSE", "Protean Steed",
                Map.of("carryingCapacity", 100, "meatYield", 150, "type", "Protean", "size", "Large"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "DONKEY", "Protean Pack Beast",
                Map.of("carryingCapacity", 80, "meatYield", 60, "type", "Protean", "size", "Medium"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "MULE", "Protean Mule",
                Map.of("carryingCapacity", 120, "meatYield", 70, "type", "Protean", "size", "Large"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "CAMEL", "Protean Strider",
                Map.of("carryingCapacity", 150, "meatYield", 100, "type", "Protean", "size", "Huge"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "OX", "Protean Draft Beast",
                Map.of("carryingCapacity", 200, "meatYield", 250, "type", "Protean", "size", "Huge"));
        // Companion
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "DOG", "Protean Hound",
                Map.of("meatYield", 15, "type", "Protean", "size", "Medium"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "CAT", "Protean Cat",
                Map.of("meatYield", 3, "type", "Protean", "size", "Small"));
        // Industry
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "BEE", "Protean Bee",
                Map.of("type", "Protean", "size", "Tiny", "product", "Honey"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "SILK_WORM", "Protean Silk Worm",
                Map.of("type", "Protean", "size", "Tiny", "product", "Silk"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "SALMON", "Protean Fish",
                Map.of("meatYield", 4, "type", "Protean", "size", "Small"));
        // Wild
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "DEER", "Protean Deer",
                Map.of("meatYield", 40, "type", "Protean", "size", "Medium"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "RABBIT", "Protean Rabbit",
                Map.of("meatYield", 1, "type", "Protean", "size", "Tiny"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "BEAVER", "Protean Beaver",
                Map.of("meatYield", 8, "type", "Protean", "size", "Small"));
        putLore(TechLevel.TL0, ModuleCategory.ANIMAL, "OTTER", "Protean Otter",
                Map.of("meatYield", 5, "type", "Protean", "size", "Small"));
        // TL1+ animals
        putLore(TechLevel.TL1, ModuleCategory.ANIMAL, "LLAMA", "Protean Llama",
                Map.of("carryingCapacity", 60, "meatYield", 40, "type", "Protean", "size", "Large"));
        putLore(TechLevel.TL1, ModuleCategory.ANIMAL, "YAK", "Protean Yak",
                Map.of("milkYield", 5, "woolYield", 2, "carryingCapacity", 100, "meatYield", 120, "type", "Protean", "size", "Huge"));
        putLore(TechLevel.TL1, ModuleCategory.ANIMAL, "BUFFALO", "Protean Buffalo",
                Map.of("milkYield", 15, "carryingCapacity", 150, "meatYield", 300, "type", "Protean", "size", "Huge"));
        putLore(TechLevel.TL1, ModuleCategory.ANIMAL, "GOOSE", "Protean Goose",
                Map.of("eggRate", 3, "meatYield", 4, "type", "Protean", "size", "Small"));
        // TL2+ animals
        putLore(TechLevel.TL2, ModuleCategory.ANIMAL, "WAR_HORSE", "Protean War Steed",
                Map.of("carryingCapacity", 120, "meatYield", 200, "type", "Protean", "size", "Large"));
        putLore(TechLevel.TL2, ModuleCategory.ANIMAL, "HAWK", "Protean Hawk",
                Map.of("type", "Protean", "size", "Tiny"));
        putLore(TechLevel.TL2, ModuleCategory.ANIMAL, "BEAR", "Protean Bear",
                Map.of("meatYield", 150, "type", "Protean", "size", "Huge"));
        putLore(TechLevel.TL2, ModuleCategory.ANIMAL, "WOLF", "Protean Wolf",
                Map.of("meatYield", 30, "type", "Protean", "size", "Medium"));
        putLore(TechLevel.TL2, ModuleCategory.ANIMAL, "EAGLE", "Protean Eagle",
                Map.of("type", "Protean", "size", "Small"));
        // TL3+ animals
        putLore(TechLevel.TL3, ModuleCategory.ANIMAL, "ELEPHANT", "Protean Elephant",
                Map.of("milkYield", 60, "carryingCapacity", 500, "meatYield", 1000, "type", "Protean", "size", "Gargantuan"));
        putLore(TechLevel.TL3, ModuleCategory.ANIMAL, "ZEBRA", "Protean Zebra",
                Map.of("carryingCapacity", 80, "meatYield", 100, "type", "Protean", "size", "Large"));
        putLore(TechLevel.TL3, ModuleCategory.ANIMAL, "PONY", "Protean Pony",
                Map.of("carryingCapacity", 60, "meatYield", 50, "type", "Protean", "size", "Medium"));
        // TL4+ animals
        putLore(TechLevel.TL4, ModuleCategory.ANIMAL, "RACE_HORSE", "Protean Race Steed",
                Map.of("carryingCapacity", 80, "meatYield", 180, "type", "Protean", "size", "Large"));
        putLore(TechLevel.TL4, ModuleCategory.ANIMAL, "SHOW_COW", "Protean Show Beast",
                Map.of("milkYield", 25, "meatYield", 250, "type", "Protean", "size", "Large"));
        putLore(TechLevel.TL4, ModuleCategory.ANIMAL, "CANARY", "Protean Canary",
                Map.of("type", "Protean", "size", "Tiny"));
        // High-tech animals (TL5-TL12)
        putLore(TechLevel.TL5, ModuleCategory.ANIMAL, "COW", "Lab-Grown Bovine",
                Map.of("milkYield", 30, "meatYield", 250, "type", "Bio-Engineered", "size", "Large"));
        putLore(TechLevel.TL6, ModuleCategory.ANIMAL, "COW", "Synthetic Protean",
                Map.of("milkYield", 40, "meatYield", 300, "type", "Bio-Engineered", "size", "Large"));
        putLore(TechLevel.TL7, ModuleCategory.ANIMAL, "HORSE", "Hover Steed",
                Map.of("carryingCapacity", 200, "type", "Bio-Engineered", "size", "Large"));
        putLore(TechLevel.TL8, ModuleCategory.ANIMAL, "HORSE", "Anti-Gravity Mount",
                Map.of("carryingCapacity", 300, "type", "Bio-Engineered", "size", "Large"));
        putLore(TechLevel.TL9, ModuleCategory.ANIMAL, "DOG", "Combat Drone Hound",
                Map.of("type", "Construct", "size", "Medium"));
        putLore(TechLevel.TL10, ModuleCategory.ANIMAL, "DOG", "Nano Hound",
                Map.of("type", "Construct", "size", "Medium"));
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
        return TechLevel.TL0;
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
