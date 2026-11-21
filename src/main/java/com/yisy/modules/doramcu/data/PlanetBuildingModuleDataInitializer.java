package com.yisy.modules.doramcu.data;
import com.yisy.modules.doramcu.model.galaxy.PlanetBuildingModuleEntity;

import com.yisy.modules.doramcu.repository.galaxy.PlanetBuildingModuleRepository;
import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Seeds the planet building module database with ~100 generic modules
 * across all tech levels and categories.
 * <p>
 * These are the base modules that game-specific adapters will map to
 * lore-appropriate names and stats.
 * </p>
 */
@Configuration
public class PlanetBuildingModuleDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(PlanetBuildingModuleDataInitializer.class);

    @Bean
    @ConditionalOnProperty(name = "app.seed-data", havingValue = "true", matchIfMissing = false)
    CommandLineRunner seedBuildingModules(PlanetBuildingModuleRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                log.info("Planet building modules already seeded. Skipping.");
                return;
            }
            log.info("Seeding planet building modules database...");
            int count = 0;

            // === TL0: Prehistoric ===
            count += seedTL0(repository);
            // === TL1: Ancient ===
            count += seedTL1(repository);
            // === TL2: Medieval ===
            count += seedTL2(repository);
            // === TL3: Renaissance ===
            count += seedTL3(repository);
            // === TL4: Industrial ===
            count += seedTL4(repository);
            // === TL5: Modern ===
            count += seedTL5(repository);
            // === TL6: Information Age ===
            count += seedTL6(repository);
            // === TL7: Interplanetary ===
            count += seedTL7(repository);
            // === TL8: Interstellar ===
            count += seedTL8(repository);
            // === TL9: Galactic ===
            count += seedTL9(repository);
            // === TL10: Post-Human ===
            count += seedTL10(repository);
            // === TL11: Transcendent ===
            count += seedTL11(repository);
            // === TL12: Post-Singularity ===
            count += seedTL12(repository);

            // === D&D Dungeon Objects (TL0–TL4) ===
            count += seedDnDDungeonObjects(repository);

            // === Sci-Fi Interior Objects (TL5–TL12) ===
            count += seedSciFiInteriorObjects(repository);

            // === Building Room Types (TL0–TL12) ===
            // Seeds all room generic types used by BuildingInteriorGenerator
            count += seedBuildingRoomTypes(repository);

            // === Animals (TL0–TL4, real animals exist at all tech levels) ===
            count += seedAnimals(repository);

            log.info("Seeded {} planet building modules.", count);
        };
    }

    private int seedTL0(PlanetBuildingModuleRepository repo) {
        int c = 0;
        // SETTLEMENT
        c += save(repo, TechLevel.TL0, ModuleCategory.SETTLEMENT, "CAVE_DWELLING", "Cave Dwelling", 10, 5,
                "A natural cave used as shelter by hunter-gatherers");
        c += save(repo, TechLevel.TL0, ModuleCategory.SETTLEMENT, "HUT_VILLAGE", "Hut Village", 20, 20,
                "A small cluster of wooden huts");
        // INDUSTRY
        c += save(repo, TechLevel.TL0, ModuleCategory.INDUSTRY, "HUNTING_GROUND", "Hunting Ground", 5, 10,
                "Designated area for hunting wild game");
        c += save(repo, TechLevel.TL0, ModuleCategory.INDUSTRY, "GATHERING_SITE", "Gathering Site", 5, 8,
                "Area for collecting wild plants and berries");
        // VEHICLE
        c += save(repo, TechLevel.TL0, ModuleCategory.VEHICLE, "RAFT", "Wooden Raft", 15, 3,
                "Simple floating platform for river crossing");
        // INFRASTRUCTURE
        c += save(repo, TechLevel.TL0, ModuleCategory.INFRASTRUCTURE, "FIRE_PIT", "Fire Pit", 5, 1,
                "Central fire for warmth and cooking");
        // DEFENSE
        c += save(repo, TechLevel.TL0, ModuleCategory.DEFENSE, "PALISADE", "Wooden Palisade", 30, 1,
                "A fence of sharpened wooden stakes for defense");
        return c;
    }

    private int seedTL1(PlanetBuildingModuleRepository repo) {
        int c = 0;
        // SETTLEMENT
        c += save(repo, TechLevel.TL1, ModuleCategory.SETTLEMENT, "STONE_HUT_VILLAGE", "Stone Hut Village", 50, 50,
                "Village of stone and mud-brick homes");
        c += save(repo, TechLevel.TL1, ModuleCategory.SETTLEMENT, "TEMPLE_COMPOUND", "Temple Compound", 100, 30,
                "Religious center with stone temples");
        // INDUSTRY
        c += save(repo, TechLevel.TL1, ModuleCategory.INDUSTRY, "FARM", "Basic Farm", 40, 25,
                "Open-field agriculture with bronze tools");
        c += save(repo, TechLevel.TL1, ModuleCategory.INDUSTRY, "BRONZE_SMITHY", "Bronze Smithy", 60, 15,
                "Workshop for crafting bronze tools and weapons");
        c += save(repo, TechLevel.TL1, ModuleCategory.INDUSTRY, "CLAY_POTTERY", "Clay Pottery", 30, 10,
                "Production of clay vessels and storage jars");
        // VEHICLE
        c += save(repo, TechLevel.TL1, ModuleCategory.VEHICLE, "OX_CART", "Ox-Drawn Cart", 40, 5,
                "Simple cart pulled by oxen for transport");
        c += save(repo, TechLevel.TL1, ModuleCategory.VEHICLE, "ROWBOAT", "Rowboat", 35, 4,
                "Wooden boat propelled by oars");
        // INFRASTRUCTURE
        c += save(repo, TechLevel.TL1, ModuleCategory.INFRASTRUCTURE, "STONE_ROAD", "Stone Road", 80, 1,
                "Paved road for trade and travel");
        c += save(repo, TechLevel.TL1, ModuleCategory.INFRASTRUCTURE, "WELL", "Stone Well", 25, 1,
                "Deep well for clean water supply");
        // DEFENSE
        c += save(repo, TechLevel.TL1, ModuleCategory.DEFENSE, "STONE_WALL", "Stone Wall", 100, 1,
                "Fortified stone wall around settlement");
        c += save(repo, TechLevel.TL1, ModuleCategory.DEFENSE, "WATCH_TOWER", "Watch Tower", 60, 1,
                "Elevated platform for spotting enemies");
        // COMMUNICATION
        c += save(repo, TechLevel.TL1, ModuleCategory.COMMUNICATION, "SMOKE_SIGNAL", "Smoke Signal Tower", 20, 1,
                "Tower for sending smoke signals over distances");
        return c;
    }

    private int seedTL2(PlanetBuildingModuleRepository repo) {
        int c = 0;
        // SETTLEMENT
        c += save(repo, TechLevel.TL2, ModuleCategory.SETTLEMENT, "STONE_CASTLE", "Stone Castle", 200, 100,
                "Fortified stone residence of a lord or noble");
        c += save(repo, TechLevel.TL2, ModuleCategory.SETTLEMENT, "MARKET_TOWN", "Market Town", 150, 200,
                "Town centered around a trading market");
        c += save(repo, TechLevel.TL2, ModuleCategory.SETTLEMENT, "MONASTERY", "Monastery", 120, 50,
                "Religious community with scriptorium and gardens");
        // INDUSTRY
        c += save(repo, TechLevel.TL2, ModuleCategory.INDUSTRY, "BLACKSMITH_FORGE", "Blacksmith Forge", 80, 20,
                "Iron forge producing tools, armor, and weapons");
        c += save(repo, TechLevel.TL2, ModuleCategory.INDUSTRY, "WATER_MILL", "Water Mill", 100, 30,
                "Water-powered mill for grinding grain");
        c += save(repo, TechLevel.TL2, ModuleCategory.INDUSTRY, "WOOL_WORKSHOP", "Wool Workshop", 60, 15,
                "Textile production from sheep wool");
        c += save(repo, TechLevel.TL2, ModuleCategory.INDUSTRY, "QUARRY", "Stone Quarry", 70, 25,
                "Extraction of building stone");
        // VEHICLE
        c += save(repo, TechLevel.TL2, ModuleCategory.VEHICLE, "HORSE_DRAWN_CART", "Horse-Drawn Cart", 60, 8,
                "Faster cart pulled by horses");
        c += save(repo, TechLevel.TL2, ModuleCategory.VEHICLE, "CARGO_SHIP", "Cargo Ship", 150, 20,
                "Sailing vessel for coastal trade");
        // INFRASTRUCTURE
        c += save(repo, TechLevel.TL2, ModuleCategory.INFRASTRUCTURE, "STONE_BRIDGE", "Stone Bridge", 120, 1,
                "Arched stone bridge over rivers");
        c += save(repo, TechLevel.TL2, ModuleCategory.INFRASTRUCTURE, "CATHEDRAL", "Cathedral", 300, 1,
                "Large stone church as community center");
        // DEFENSE
        c += save(repo, TechLevel.TL2, ModuleCategory.DEFENSE, "CASTLE_WALL", "Castle Wall", 250, 1,
                "Thick fortified walls with battlements");
        c += save(repo, TechLevel.TL2, ModuleCategory.DEFENSE, "BARRACKS", "Barracks", 100, 1,
                "Housing for trained soldiers");
        // COMMUNICATION
        c += save(repo, TechLevel.TL2, ModuleCategory.COMMUNICATION, "MOUNTED_MESSENGER", "Mounted Messenger Station", 40, 1,
                "Relay station for horse-borne messages");
        return c;
    }

    private int seedTL3(PlanetBuildingModuleRepository repo) {
        int c = 0;
        // SETTLEMENT
        c += save(repo, TechLevel.TL3, ModuleCategory.SETTLEMENT, "HARBOR_CITY", "Harbor City", 300, 500,
                "Port city with shipyards and trade warehouses");
        c += save(repo, TechLevel.TL3, ModuleCategory.SETTLEMENT, "UNIVERSITY", "University", 200, 100,
                "Center of learning and scholarship");
        // INDUSTRY
        c += save(repo, TechLevel.TL3, ModuleCategory.INDUSTRY, "GUNPOWDER_WORKS", "Gunpowder Works", 120, 20,
                "Production of early firearms and explosives");
        c += save(repo, TechLevel.TL3, ModuleCategory.INDUSTRY, "PRINTING_PRESS", "Printing Press", 100, 30,
                "Mass production of books and documents");
        c += save(repo, TechLevel.TL3, ModuleCategory.INDUSTRY, "SHIPYARD", "Shipyard", 180, 40,
                "Construction and repair of sailing vessels");
        // VEHICLE
        c += save(repo, TechLevel.TL3, ModuleCategory.VEHICLE, "GALLEON", "Galleon", 250, 50,
                "Large sailing warship for exploration and trade");
        c += save(repo, TechLevel.TL3, ModuleCategory.VEHICLE, "STAGE_COACH", "Stage Coach", 80, 10,
                "Scheduled passenger transport by road");
        // INFRASTRUCTURE
        c += save(repo, TechLevel.TL3, ModuleCategory.INFRASTRUCTURE, "CANAL", "Canal", 200, 1,
                "Artificial waterway for boat transport");
        c += save(repo, TechLevel.TL3, ModuleCategory.INFRASTRUCTURE, "LANTERN_POST", "Lantern Post Network", 50, 1,
                "Oil-lit street lamps for night navigation");
        // DEFENSE
        c += save(repo, TechLevel.TL3, ModuleCategory.DEFENSE, "ARTILLERY_FORT", "Artillery Fort", 300, 1,
                "Fortification with cannon emplacements");
        c += save(repo, TechLevel.TL3, ModuleCategory.DEFENSE, "MOAT", "Defensive Moat", 100, 1,
                "Water-filled trench around fortifications");
        // COMMUNICATION
        c += save(repo, TechLevel.TL3, ModuleCategory.COMMUNICATION, "POST_OFFICE", "Post Office", 60, 1,
                "Centralized mail and letter delivery system");
        return c;
    }

    private int seedTL4(PlanetBuildingModuleRepository repo) {
        int c = 0;
        // SETTLEMENT
        c += save(repo, TechLevel.TL4, ModuleCategory.SETTLEMENT, "INDUSTRIAL_CITY", "Industrial City", 500, 2000,
                "City dominated by factories and worker housing");
        c += save(repo, TechLevel.TL4, ModuleCategory.SETTLEMENT, "RAILWAY_STATION", "Railway Station", 200, 500,
                "Hub for train passenger and cargo transport");
        // INDUSTRY
        c += save(repo, TechLevel.TL4, ModuleCategory.INDUSTRY, "STEAM_FACTORY", "Steam Factory", 300, 100,
                "Steam-powered manufacturing plant");
        c += save(repo, TechLevel.TL4, ModuleCategory.INDUSTRY, "COAL_MINE", "Coal Mine", 200, 80,
                "Underground extraction of coal for fuel");
        c += save(repo, TechLevel.TL4, ModuleCategory.INDUSTRY, "STEEL_MILL", "Steel Mill", 400, 120,
                "Mass production of steel beams and rails");
        c += save(repo, TechLevel.TL4, ModuleCategory.INDUSTRY, "TEXTILE_MILL", "Textile Mill", 250, 60,
                "Mechanized fabric production");
        // VEHICLE
        c += save(repo, TechLevel.TL4, ModuleCategory.VEHICLE, "STEAM_LOCOMOTIVE", "Steam Locomotive", 200, 50,
                "Rail-powered train for passengers and cargo");
        c += save(repo, TechLevel.TL4, ModuleCategory.VEHICLE, "STEAMSHIP", "Steamship", 350, 100,
                "Ocean-going vessel powered by steam engines");
        c += save(repo, TechLevel.TL4, ModuleCategory.VEHICLE, "BUS_DEPOT", "Bus Depot", 150, 30,
                "Hub for horse-drawn and early motor buses");
        // INFRASTRUCTURE
        c += save(repo, TechLevel.TL4, ModuleCategory.INFRASTRUCTURE, "RAILROAD", "Railroad", 500, 1,
                "Network of train tracks connecting cities");
        c += save(repo, TechLevel.TL4, ModuleCategory.INFRASTRUCTURE, "TELEGRAPH_STATION", "Telegraph Station", 100, 1,
                "Electrical messaging network");
        c += save(repo, TechLevel.TL4, ModuleCategory.INFRASTRUCTURE, "GAS_LAMP_GRID", "Gas Lamp Grid", 80, 1,
                "City-wide gas-powered street lighting");
        // DEFENSE
        c += save(repo, TechLevel.TL4, ModuleCategory.DEFENSE, "IRONCLAD_FORT", "Ironclad Fort", 400, 1,
                "Fortification with armored walls and heavy guns");
        // COMMUNICATION
        c += save(repo, TechLevel.TL4, ModuleCategory.COMMUNICATION, "TELEGRAPH_NETWORK", "Telegraph Network", 200, 1,
                "Long-distance electrical communication lines");
        return c;
    }

    private int seedTL5(PlanetBuildingModuleRepository repo) {
        int c = 0;
        // SETTLEMENT
        c += save(repo, TechLevel.TL5, ModuleCategory.SETTLEMENT, "METROPOLIS", "Metropolis", 1000, 10000,
                "Large modern city with skyscrapers and infrastructure");
        c += save(repo, TechLevel.TL5, ModuleCategory.SETTLEMENT, "AIRPORT", "Airport", 500, 2000,
                "Hub for commercial air travel");
        // INDUSTRY
        c += save(repo, TechLevel.TL5, ModuleCategory.INDUSTRY, "ASSEMBLY_PLANT", "Assembly Plant", 500, 200,
                "Automated assembly line manufacturing");
        c += save(repo, TechLevel.TL5, ModuleCategory.INDUSTRY, "OIL_REFINERY", "Oil Refinery", 600, 150,
                "Processing of crude oil into fuel and products");
        c += save(repo, TechLevel.TL5, ModuleCategory.INDUSTRY, "CHEMICAL_PLANT", "Chemical Plant", 400, 100,
                "Production of industrial chemicals");
        // VEHICLE
        c += save(repo, TechLevel.TL5, ModuleCategory.VEHICLE, "AUTOMOBILE", "Automobile", 100, 5,
                "Mass-produced personal vehicle");
        c += save(repo, TechLevel.TL5, ModuleCategory.VEHICLE, "COMMERCIAL_AIRLINER", "Commercial Airliner", 800, 200,
                "Large passenger aircraft for intercontinental travel");
        c += save(repo, TechLevel.TL5, ModuleCategory.VEHICLE, "TANK", "Military Tank", 300, 10,
                "Armored combat vehicle");
        // INFRASTRUCTURE
        c += save(repo, TechLevel.TL5, ModuleCategory.INFRASTRUCTURE, "HIGHWAY", "Highway", 800, 1,
                "Multi-lane paved road for high-speed travel");
        c += save(repo, TechLevel.TL5, ModuleCategory.INFRASTRUCTURE, "POWER_PLANT", "Power Plant", 600, 1,
                "Electrical generation facility");
        c += save(repo, TechLevel.TL5, ModuleCategory.INFRASTRUCTURE, "SEWER_SYSTEM", "Sewer System", 400, 1,
                "Underground waste management network");
        // DEFENSE
        c += save(repo, TechLevel.TL5, ModuleCategory.DEFENSE, "MILITARY_BASE", "Military Base", 800, 1,
                "Large armed forces installation");
        c += save(repo, TechLevel.TL5, ModuleCategory.DEFENSE, "ANTI_AIR_BATTERY", "Anti-Air Battery", 300, 1,
                "Surface-to-air missile defense system");
        // COMMUNICATION
        c += save(repo, TechLevel.TL5, ModuleCategory.COMMUNICATION, "RADIO_TOWER", "Radio Tower", 200, 1,
                "Broadcast tower for radio and TV signals");
        c += save(repo, TechLevel.TL5, ModuleCategory.COMMUNICATION, "CELL_TOWER", "Cell Tower", 150, 1,
                "Mobile phone network relay station");
        return c;
    }

    private int seedTL6(PlanetBuildingModuleRepository repo) {
        int c = 0;
        // SETTLEMENT
        c += save(repo, TechLevel.TL6, ModuleCategory.SETTLEMENT, "SPACEPORT_CITY", "Spaceport City", 2000, 50000,
                "City built around a space launch facility");
        c += save(repo, TechLevel.TL6, ModuleCategory.SETTLEMENT, "RESEARCH_CAMPUS", "Research Campus", 800, 5000,
                "Advanced scientific research complex");
        // INDUSTRY
        c += save(repo, TechLevel.TL6, ModuleCategory.INDUSTRY, "SEMICONDUCTOR_FAB", "Semiconductor Fab", 1000, 300,
                "Microchip manufacturing facility");
        c += save(repo, TechLevel.TL6, ModuleCategory.INDUSTRY, "NUCLEAR_PLANT", "Nuclear Power Plant", 1500, 500,
                "Nuclear fission energy generation");
        c += save(repo, TechLevel.TL6, ModuleCategory.INDUSTRY, "ROBOTICS_FACTORY", "Robotics Factory", 800, 200,
                "Assembly of industrial robots and drones");
        // VEHICLE
        c += save(repo, TechLevel.TL6, ModuleCategory.VEHICLE, "HOVER_TRANSPORT", "Hover Transport", 300, 20,
                "Ground-effect vehicle for fast transport");
        c += save(repo, TechLevel.TL6, ModuleCategory.VEHICLE, "ORBITAL_ROCKET", "Orbital Rocket", 2000, 100,
                "Launch vehicle for orbital cargo and crew");
        c += save(repo, TechLevel.TL6, ModuleCategory.VEHICLE, "SUBORBITAL_SHUTTLE", "Suborbital Shuttle", 1500, 50,
                "Reusable short-range space transport");
        // INFRASTRUCTURE
        c += save(repo, TechLevel.TL6, ModuleCategory.INFRASTRUCTURE, "ORBITAL_ELEVATOR", "Orbital Elevator", 5000, 1,
                "Tether-based space launch system");
        c += save(repo, TechLevel.TL6, ModuleCategory.INFRASTRUCTURE, "DATA_CENTER", "Data Center", 500, 1,
                "Large-scale computing and storage facility");
        c += save(repo, TechLevel.TL6, ModuleCategory.INFRASTRUCTURE, "SOLAR_FARM", "Solar Farm", 400, 1,
                "Photovoltaic energy generation array");
        // DEFENSE
        c += save(repo, TechLevel.TL6, ModuleCategory.DEFENSE, "MISSILE_SILEO", "Missile Silo", 1000, 1,
                "Underground ballistic missile launch facility");
        c += save(repo, TechLevel.TL6, ModuleCategory.DEFENSE, "SATELLITE_DEFENSE", "Defense Satellite Network", 2000, 1,
                "Orbital surveillance and early warning system");
        // COMMUNICATION
        c += save(repo, TechLevel.TL6, ModuleCategory.COMMUNICATION, "SATELLITE_UPLINK", "Satellite Uplink Station", 300, 1,
                "Ground station for satellite communication");
        c += save(repo, TechLevel.TL6, ModuleCategory.COMMUNICATION, "FIBER_OPTIC_HUB", "Fiber Optic Hub", 200, 1,
                "High-speed internet backbone node");
        return c;
    }

    private int seedTL7(PlanetBuildingModuleRepository repo) {
        int c = 0;
        // SETTLEMENT
        c += save(repo, TechLevel.TL7, ModuleCategory.SETTLEMENT, "COLONIAL_OUTPOST", "Colonial Outpost", 3000, 10000,
                "First settlement on an extraterrestrial world");
        c += save(repo, TechLevel.TL7, ModuleCategory.SETTLEMENT, "ORBITAL_HABITAT", "Orbital Habitat", 5000, 50000,
                "Living station in orbit around a planet");
        // INDUSTRY
        c += save(repo, TechLevel.TL7, ModuleCategory.INDUSTRY, "FUSION_REACTOR", "Fusion Reactor", 3000, 1000,
                "Clean energy from nuclear fusion");
        c += save(repo, TechLevel.TL7, ModuleCategory.INDUSTRY, "ASTEROID_MINE", "Asteroid Mine", 2000, 500,
                "Extraction of rare minerals from space rocks");
        c += save(repo, TechLevel.TL7, ModuleCategory.INDUSTRY, "BIOTECH_LAB", "Biotech Lab", 1500, 200,
                "Genetic engineering and medical research");
        // VEHICLE
        c += save(repo, TechLevel.TL7, ModuleCategory.VEHICLE, "INTERPLANETARY_SHIP", "Interplanetary Ship", 5000, 200,
                "Vessel capable of travel between planets");
        c += save(repo, TechLevel.TL7, ModuleCategory.VEHICLE, "MARS_ROVER", "Planetary Rover", 500, 10,
                "Autonomous surface exploration vehicle");
        // INFRASTRUCTURE
        c += save(repo, TechLevel.TL7, ModuleCategory.INFRASTRUCTURE, "LUNAR_BASE", "Lunar Base", 8000, 1,
                "Permanent facility on the Moon");
        c += save(repo, TechLevel.TL7, ModuleCategory.INFRASTRUCTURE, "SPACE_ELEVATOR", "Space Elevator", 10000, 1,
                "Direct ground-to-orbit transport tether");
        // DEFENSE
        c += save(repo, TechLevel.TL7, ModuleCategory.DEFENSE, "ORBITAL_GUN", "Orbital Defense Gun", 3000, 1,
                "Space-based kinetic weapon platform");
        c += save(repo, TechLevel.TL7, ModuleCategory.DEFENSE, "PLANETARY_SHIELD", "Planetary Shield Generator", 5000, 1,
                "Energy dome protecting a settlement");
        // COMMUNICATION
        c += save(repo, TechLevel.TL7, ModuleCategory.COMMUNICATION, "DEEP_SPACE_ARRAY", "Deep Space Array", 2000, 1,
                "Long-range communication with distant colonies");
        return c;
    }

    private int seedTL8(PlanetBuildingModuleRepository repo) {
        int c = 0;
        // SETTLEMENT
        c += save(repo, TechLevel.TL8, ModuleCategory.SETTLEMENT, "SPACE_STATION", "Space Station", 10000, 100000,
                "Large habitable station in deep space");
        c += save(repo, TechLevel.TL8, ModuleCategory.SETTLEMENT, "DYSON_RING", "Dyson Ring Segment", 50000, 1000000,
                "Partial Dyson sphere for energy collection");
        // INDUSTRY
        c += save(repo, TechLevel.TL8, ModuleCategory.INDUSTRY, "ANTIMATTER_PLANT", "Antimatter Plant", 10000, 5000,
                "Production of antimatter fuel for FTL drives");
        c += save(repo, TechLevel.TL8, ModuleCategory.INDUSTRY, "NANOFAB", "Nanofabrication Plant", 5000, 2000,
                "Molecular-scale manufacturing facility");
        // VEHICLE
        c += save(repo, TechLevel.TL8, ModuleCategory.VEHICLE, "FTL_CRUISER", "FTL Cruiser", 20000, 500,
                "Faster-than-light interstellar warship");
        c += save(repo, TechLevel.TL8, ModuleCategory.VEHICLE, "TRANSPORT_SHUTTLE", "Transport Shuttle", 3000, 100,
                "Short-range FTL cargo and passenger shuttle");
        // INFRASTRUCTURE
        c += save(repo, TechLevel.TL8, ModuleCategory.INFRASTRUCTURE, "JUMP_GATE_STATION", "Jump Gate Station", 30000, 1,
                "FTL gateway connecting distant star systems");
        c += save(repo, TechLevel.TL8, ModuleCategory.INFRASTRUCTURE, "STARLIFT", "Starlift", 20000, 1,
                "Beamed-energy launch system from planetary surface");
        // DEFENSE
        c += save(repo, TechLevel.TL8, ModuleCategory.DEFENSE, "ENERGY_WEAPON", "Energy Weapon Platform", 8000, 1,
                "Directed energy weapon for orbital defense");
        c += save(repo, TechLevel.TL8, ModuleCategory.DEFENSE, "SHIELD_GENERATOR", "Shield Generator Array", 10000, 1,
                "Planetary-scale energy shield network");
        // COMMUNICATION
        c += save(repo, TechLevel.TL8, ModuleCategory.COMMUNICATION, "HYPERNET_RELAY", "Hypernet Relay", 5000, 1,
                "FTL communication relay station");
        // CORPORATE OFFICE
        c += save(repo, TechLevel.TL8, ModuleCategory.INFRASTRUCTURE, "CORPORATE_OFFICE", "Corporate Office", 8000, 500,
                "Physical corporate headquarters providing bonuses: +2 contract slots per level, -10% hiring cost per level");
        return c;
    }

    private int seedTL9(PlanetBuildingModuleRepository repo) {
        int c = 0;
        // SETTLEMENT
        c += save(repo, TechLevel.TL9, ModuleCategory.SETTLEMENT, "STARBASE", "Starbase", 50000, 5000000,
                "Massive military and civilian space station");
        c += save(repo, TechLevel.TL9, ModuleCategory.SETTLEMENT, "WORLD_CITY", "World City", 100000, 50000000,
                "Planet-spanning megacity covering an entire world");
        // INDUSTRY
        c += save(repo, TechLevel.TL9, ModuleCategory.INDUSTRY, "STAR_FORGE", "Star Forge", 50000, 100000,
                "Shipyard capable of building capital ships");
        c += save(repo, TechLevel.TL9, ModuleCategory.INDUSTRY, "AI_CORE", "AI Core Facility", 20000, 50000,
                "Housing for advanced artificial general intelligence");
        // VEHICLE
        c += save(repo, TechLevel.TL9, ModuleCategory.VEHICLE, "DREADNOUGHT", "Dreadnought", 100000, 5000,
                "Empire-scale capital warship");
        c += save(repo, TechLevel.TL9, ModuleCategory.VEHICLE, "WORMHOLE_DRIVE", "Wormhole Drive Vessel", 80000, 1000,
                "Ship using wormhole technology for instant travel");
        // INFRASTRUCTURE
        c += save(repo, TechLevel.TL9, ModuleCategory.INFRASTRUCTURE, "WORMHOLE_NETWORK", "Wormhole Network Node", 100000, 1,
                "Stable wormhole connecting galaxy sectors");
        c += save(repo, TechLevel.TL9, ModuleCategory.INFRASTRUCTURE, "PLANETARY_COMPUTER", "Planetary Computer", 50000, 1,
                "World-scale computing matrix");
        // DEFENSE
        c += save(repo, TechLevel.TL9, ModuleCategory.DEFENSE, "PLANET_CRUSHER", "Planet Defense Grid", 200000, 1,
                "System-wide planetary defense network");
        // COMMUNICATION
        c += save(repo, TechLevel.TL9, ModuleCategory.COMMUNICATION, "QUANTUM_NET", "Quantum Communication Network", 30000, 1,
                "Instant galaxy-wide communication via quantum entanglement");
        return c;
    }

    private int seedTL10(PlanetBuildingModuleRepository repo) {
        int c = 0;
        // SETTLEMENT
        c += save(repo, TechLevel.TL10, ModuleCategory.SETTLEMENT, "MEGASTRUCTURE_HUB", "Megastructure Hub", 500000, 100000000,
                "Central control for Dyson sphere or Matrioshka brain");
        // INDUSTRY
        c += save(repo, TechLevel.TL10, ModuleCategory.INDUSTRY, "STAR_LIFTER", "Star Lifter", 200000, 500000,
                "Harvesting stellar matter for construction");
        c += save(repo, TechLevel.TL10, ModuleCategory.INDUSTRY, "REALITY_ENGINE", "Reality Engine", 500000, 1000000,
                "Device manipulating physical constants locally");
        // VEHICLE
        c += save(repo, TechLevel.TL10, ModuleCategory.VEHICLE, "WARP_GATE", "Warp Gate", 300000, 100000,
                "Permanent FTL portal between galaxies");
        // INFRASTRUCTURE
        c += save(repo, TechLevel.TL10, ModuleCategory.INFRASTRUCTURE, "DYSON_SPHERE", "Dyson Sphere", 1000000, 1,
                "Complete stellar energy capture structure");
        c += save(repo, TechLevel.TL10, ModuleCategory.INFRASTRUCTURE, "MATRIOSKA_BRAIN", "Matrioshka Brain", 800000, 1,
                "Computing structure using entire star's energy");
        // DEFENSE
        c += save(repo, TechLevel.TL10, ModuleCategory.DEFENSE, "VOID_SHIELD", "Void Shield", 500000, 1,
                "Dimensional barrier protecting a star system");
        // COMMUNICATION
        c += save(repo, TechLevel.TL10, ModuleCategory.COMMUNICATION, "THOUGHT_NET", "Thought Network", 200000, 1,
                "Direct mind-to-mind communication across light-years");
        return c;
    }

    private int seedTL11(PlanetBuildingModuleRepository repo) {
        int c = 0;
        // SETTLEMENT
        c += save(repo, TechLevel.TL11, ModuleCategory.SETTLEMENT, "DIMENSIONAL_HUB", "Dimensional Hub", 1000000, 1000000000,
                "Gateway between parallel dimensions");
        // INDUSTRY
        c += save(repo, TechLevel.TL11, ModuleCategory.INDUSTRY, "UNIVERSE_SEEDER", "Universe Seeder", 5000000, 10000000,
                "Device for creating pocket universes");
        // VEHICLE
        c += save(repo, TechLevel.TL11, ModuleCategory.VEHICLE, "TIME_VESSEL", "Time Vessel", 2000000, 10000,
                "Ship capable of controlled time travel");
        // INFRASTRUCTURE
        c += save(repo, TechLevel.TL11, ModuleCategory.INFRASTRUCTURE, "DIMENSIONAL_BRIDGE", "Dimensional Bridge", 3000000, 1,
                "Stable passage between parallel realities");
        // DEFENSE
        c += save(repo, TechLevel.TL11, ModuleCategory.DEFENSE, "TIMELINE_ANCHOR", "Timeline Anchor", 5000000, 1,
                "Prevents temporal paradoxes and timeline corruption");
        // COMMUNICATION
        c += save(repo, TechLevel.TL11, ModuleCategory.COMMUNICATION, "OMNI_CHANNEL", "Omni-Channel", 1000000, 1,
                "Communication across all dimensions simultaneously");
        return c;
    }

    private int seedTL12(PlanetBuildingModuleRepository repo) {
        int c = 0;
        // SETTLEMENT
        c += save(repo, TechLevel.TL12, ModuleCategory.SETTLEMENT, "OMNIVERSAL_THRONE", "Omniversal Throne", 10000000, 1000000000,
                "Seat of post-singularity consciousness");
        // INDUSTRY
        c += save(repo, TechLevel.TL12, ModuleCategory.INDUSTRY, "COSMIC_LOOM", "Cosmic Loom", 100000000, 100000000,
                "Weaves the fabric of spacetime itself");
        // VEHICLE
        c += save(repo, TechLevel.TL12, ModuleCategory.VEHICLE, "OMNIVERSAL_TRAVELER", "Omniversal Traveler", 50000000, 100000,
                "Vessel traversing all of existence");
        // INFRASTRUCTURE
        c += save(repo, TechLevel.TL12, ModuleCategory.INFRASTRUCTURE, "COSMIC_AXIS", "Cosmic Axis", 200000000, 1,
                "Central pillar connecting all universes");
        // DEFENSE
        c += save(repo, TechLevel.TL12, ModuleCategory.DEFENSE, "EXISTENCE_SHIELD", "Existence Shield", 100000000, 1,
                "Protects reality from existential threats");
        // COMMUNICATION
        c += save(repo, TechLevel.TL12, ModuleCategory.COMMUNICATION, "ABSOLUTE_KNOWLEDGE", "Absolute Knowledge Network", 50000000, 1,
                "Omniscient information network spanning all of existence");
        return c;
    }

    private int save(PlanetBuildingModuleRepository repo, TechLevel techLevel, ModuleCategory category,
                     String genericType, String genericName, int baseCost, int baseCapacity, String description) {
        PlanetBuildingModuleEntity entity = new PlanetBuildingModuleEntity(
                techLevel, category, genericType, genericName, baseCost, baseCapacity, description);
        repo.save(entity);
        return 1;
    }

    // === D&D Dungeon Object Seeding ===

    private int seedDnDDungeonObjects(PlanetBuildingModuleRepository repo) {
        int c = 0;
        c += seedDnDTL0(repo);
        c += seedDnDTL1(repo);
        c += seedDnDTL2(repo);
        c += seedDnDTL3(repo);
        c += seedDnDTL4(repo);
        return c;
    }

    // --- TL0: Prehistoric dungeon objects ---
    private int seedDnDTL0(PlanetBuildingModuleRepository repo) {
        int c = 0;

        // DUNGEON_ROOM
        c += saveRoom(repo, TechLevel.TL0, "CAVE_ENTRANCE", "Cave Entrance",
                "A dark cave opening in a rocky cliff face");
        c += saveRoom(repo, TechLevel.TL0, "CAVE_CHAMBER", "Cave Chamber",
                "A large underground chamber with stalactites");
        c += saveRoom(repo, TechLevel.TL0, "UNDERGROUND_POOL", "Underground Pool",
                "A subterranean pool of fresh water");

        // FURNITURE
        c += saveFurniture(repo, TechLevel.TL0, "STONE_ALTAR", "Stone Altar", 5,
                "A rough-hewn stone altar for primitive rituals", null);
        c += saveFurniture(repo, TechLevel.TL0, "FIRE_PIT", "Fire Pit", 0,
                "A stone-ringed fire pit for warmth and cooking", null);
        c += saveFurniture(repo, TechLevel.TL0, "FUR_BED", "Fur Bed", 2,
                "Animal furs piled on the ground for sleeping", 5);

        // TREASURE
        c += saveTreasure(repo, TechLevel.TL0, "BONE_NECKLACE", "Bone Necklace", "Common", 1,
                "A necklace of carved animal bones");
        c += saveTreasure(repo, TechLevel.TL0, "STONE_AMULET", "Stone Amulet", "Common", 2,
                "A carved stone amulet with protective markings");

        // TRAP
        c += saveTrap(repo, TechLevel.TL0, "SPIKE_PIT", "Spike Pit", 10, "piercing", "1d8",
                "Hidden pit with sharpened stakes");
        c += saveTrap(repo, TechLevel.TL0, "FALLING_ROCK", "Falling Rock Trap", 8, "bludgeoning", "2d6",
                "Loose rocks triggered by pressure plates");

        // DOOR
        c += saveDoor(repo, TechLevel.TL0, "WOODEN_DOOR", "Wooden Door", 10,
                "A simple wooden door", 15, null);

        // CHEST
        c += saveChest(repo, TechLevel.TL0, "WOODEN_BOX", "Wooden Box", 5,
                "A simple wooden storage box");

        // MONSTER
        c += saveMonster(repo, TechLevel.TL0, "GIANT_SPIDER", "Giant Spider", 0.5, "Beast", "Large", 26, 12,
                null, null, "A massive spider lurking in dark caves");
        c += saveMonster(repo, TechLevel.TL0, "GIANT_WORM", "Giant Worm", 1, "Beast", "Huge", 45, 12,
                null, null, "A burrowing worm that attacks from below");
        c += saveMonster(repo, TechLevel.TL0, "KOBOLD", "Kobold", 0.125, "Humanoid", "Small", 22, 12,
                null, null, "A small reptilian humanoid scavenger");

        // WEAPON
        c += saveWeapon(repo, TechLevel.TL0, "STONE_SPEAR", "Stone Spear", 1, "piercing", "1d6", 6,
                "A spear with a sharpened stone tip");
        c += saveWeapon(repo, TechLevel.TL0, "STONE_AXE", "Stone Axe", 1, "slashing", "1d6", 4,
                "An axe with a flint blade");
        c += saveWeapon(repo, TechLevel.TL0, "BOW", "Shortbow", 25, "piercing", "1d8", 2,
                "A simple wooden bow for hunting");

        // ARMOR
        c += saveArmor(repo, TechLevel.TL0, "LEATHER_ARMOR", "Leather Armor", 10, 11, 10,
                "Padded leather armor for basic protection");
        c += saveArmor(repo, TechLevel.TL0, "HIDE_ARMOR", "Hide Armor", 15, 12, 12,
                "Thick animal hide armor");

        // ADVENTURE_GEAR
        c += saveGear(repo, TechLevel.TL0, "TORCH", "Torch", 0, null, null, 1,
                "Provides light for 6 hours");
        c += saveGear(repo, TechLevel.TL0, "ROPE", "Hemp Rope", 1, null, null, 10,
                "50 feet of hemp rope");
        c += saveGear(repo, TechLevel.TL0, "RATIONS", "Rations", 0, null, null, 2,
                "One day of dried food and water");
        c += saveGear(repo, TechLevel.TL0, "WATER_SKIN", "Water Skin", 2, null, null, 5,
                "Holds one gallon of water");

        return c;
    }

    // --- TL1: Ancient dungeon objects ---
    private int seedDnDTL1(PlanetBuildingModuleRepository repo) {
        int c = 0;

        // DUNGEON_ROOM
        c += saveRoom(repo, TechLevel.TL1, "TEMPLE_HALL", "Temple Hall",
                "A grand hall with stone pillars and murals");
        c += saveRoom(repo, TechLevel.TL1, "TOMB_CHAMBER", "Tomb Chamber",
                "An ancient burial chamber with sarcophagi");
        c += saveRoom(repo, TechLevel.TL1, "TREASURE_VAULT", "Treasure Vault",
                "A sealed room filled with ancient treasure");

        // FURNITURE
        c += saveFurniture(repo, TechLevel.TL1, "STONE_THRONE", "Stone Throne", 50,
                "A carved stone throne for a ruler", null);
        c += saveFurniture(repo, TechLevel.TL1, "BRONZE_BRAZIER", "Bronze Brazier", 10,
                "A bronze brazier for burning incense", 15);
        c += saveFurniture(repo, TechLevel.TL1, "WOODEN_TABLE", "Wooden Table", 5,
                "A sturdy wooden table", 20);
        c += saveFurniture(repo, TechLevel.TL1, "WOODEN_CHAIR", "Wooden Chair", 5,
                "A simple wooden chair", 5);

        // TREASURE
        c += saveTreasure(repo, TechLevel.TL1, "GOLD_COINS", "Gold Coins", "Common", 100,
                "A pouch of gold coins");
        c += saveTreasure(repo, TechLevel.TL1, "GEMSTONE", "Gemstone", "Uncommon", 50,
                "A polished gemstone of varying quality");
        c += saveTreasure(repo, TechLevel.TL1, "JEWELRY", "Jewelry", "Uncommon", 75,
                "Fine jewelry with precious stones");

        // TRAP
        c += saveTrap(repo, TechLevel.TL1, "POISON_DART", "Poison Dart Trap", 13, "poison", "2d4",
                "Hidden darts coated with venom");
        c += saveTrap(repo, TechLevel.TL1, "PRESSURE_PLATE", "Pressure Plate Trap", 15, "piercing", "1d10",
                "A hidden pressure plate triggering a mechanism");
        c += saveTrap(repo, TechLevel.TL1, "GAS_VENT", "Gas Vent Trap", 12, "poison", "3d6",
                "Toxic gas released from hidden vents");

        // DOOR
        c += saveDoor(repo, TechLevel.TL1, "IRON_DOOR", "Iron Door", 20,
                "A heavy iron-reinforced door", 45, null);
        c += saveDoor(repo, TechLevel.TL1, "SECRET_DOOR", "Secret Door", 15,
                "A hidden door disguised as a wall", null, 15);

        // CHEST
        c += saveChest(repo, TechLevel.TL1, "IRON_BANDED_CHEST", "Iron-Banded Chest", 10,
                "A sturdy chest reinforced with iron bands");
        c += saveChest(repo, TechLevel.TL1, "STRONGBOX", "Strongbox", 15,
                "A locked metal box for valuables");

        // MONSTER
        c += saveMonster(repo, TechLevel.TL1, "GOBLIN", "Goblin", 0.25, "Humanoid", "Small", 7, 15,
                null, null, "A small, cunning humanoid raider");
        c += saveMonster(repo, TechLevel.TL1, "SKELETON", "Skeleton", 1, "Undead", "Medium", 13, 13,
                null, null, "An undead warrior reanimated by dark magic");
        c += saveMonster(repo, TechLevel.TL1, "ZOMBIE", "Zombie", 0.125, "Undead", "Medium", 22, 8,
                null, null, "A shambling undead corpse");
        c += saveMonster(repo, TechLevel.TL1, "GNOLL", "Gnoll", 1, "Humanoid", "Medium", 22, 12,
                null, null, "A hyena-headed humanoid predator");

        // MAGIC_ITEM
        c += saveMagicItem(repo, TechLevel.TL1, "WAND_OF_WISDOM", "Wand of Wisdom", "Uncommon",
                "A wand that enhances wisdom scores");
        c += saveMagicItem(repo, TechLevel.TL1, "POTION_OF_HEALING", "Potion of Healing", "Common",
                "Restores 2d4+2 hit points");

        // NPC
        c += saveNpc(repo, TechLevel.TL1, "INNKEEPER", "Innkeeper",
                "A friendly innkeeper who runs a local tavern");
        c += saveNpc(repo, TechLevel.TL1, "MERCHANT", "Merchant",
                "A traveling merchant with various goods");
        c += saveNpc(repo, TechLevel.TL1, "GUARD_CAPTAIN", "Guard Captain",
                "A seasoned guard leader");

        // SPELL
        c += saveSpell(repo, TechLevel.TL1, "MAGIC_MISSILE", "Magic Missile", 1, "Evocation", null, null,
                "Creates darts of magical force that strike automatically");
        c += saveSpell(repo, TechLevel.TL1, "SHIELD", "Shield", 1, "Abjuration", null, null,
                "Creates a barrier of force that blocks attacks");
        c += saveSpell(repo, TechLevel.TL1, "BURNING_HANDS", "Burning Hands", 1, "Evocation", "fire", "3d6",
                "Flames erupt from your hands in a cone");

        // POTION
        c += savePotion(repo, TechLevel.TL1, "ANTIDOTE", "Antidote", "Common", null, null, null, 50,
                "Cures one type of poison");
        c += savePotion(repo, TechLevel.TL1, "POISON_VIAL", "Poison Vial", null, 10, "poison", "2d4", null,
                "A vial of contact poison");

        // WEAPON
        c += saveWeapon(repo, TechLevel.TL1, "SHORTSWORD", "Shortsword", 10, "piercing", "1d6", 2,
                "A lightweight one-handed sword");
        c += saveWeapon(repo, TechLevel.TL1, "LONGSWORD", "Longsword", 15, "slashing", "1d8", 3,
                "A versatile one-handed sword");
        c += saveWeapon(repo, TechLevel.TL1, "LONGBOW", "Longbow", 50, "piercing", "1d8", 2,
                "A powerful two-handed bow");

        // ARMOR
        c += saveArmor(repo, TechLevel.TL1, "CHAIN_MAIL", "Chain Mail", 75, 16, 55,
                "Interlocking metal rings for heavy protection");
        c += saveArmor(repo, TechLevel.TL1, "SHIELD", "Shield", 10, 2, 6,
                "A wooden shield reinforced with metal");

        // ADVENTURE_GEAR
        c += saveGear(repo, TechLevel.TL1, "BACKPACK", "Backpack", 2, null, null, 5,
                "A sturdy leather backpack");
        c += saveGear(repo, TechLevel.TL1, "HAMMER", "Hammer", 1, "bludgeoning", "1d8", 3,
                "A heavy hammer for breaking objects");
        c += saveGear(repo, TechLevel.TL1, "CLIMBERS_KIT", "Climber's Kit", 25, null, null, 12,
                "Includes pitons, ropes, and carabiners");

        return c;
    }

    // --- TL2: Medieval dungeon objects ---
    private int seedDnDTL2(PlanetBuildingModuleRepository repo) {
        int c = 0;

        // DUNGEON_ROOM
        c += saveRoom(repo, TechLevel.TL2, "CASTLE_HALL", "Castle Great Hall",
                "A grand hall with banners and a long table");
        c += saveRoom(repo, TechLevel.TL2, "DUNGEON_CELL", "Dungeon Cell",
                "A dark stone cell with iron bars");
        c += saveRoom(repo, TechLevel.TL2, "ARMORY", "Armory",
                "A room filled with weapons and armor");
        c += saveRoom(repo, TechLevel.TL2, "BOSS_CHAMBER", "Boss Chamber",
                "A large chamber for a powerful enemy");

        // FURNITURE
        c += saveFurniture(repo, TechLevel.TL2, "GOLDEN_THRONE", "Golden Throne", 200,
                "An ornate throne of gold and jewels", null);
        c += saveFurniture(repo, TechLevel.TL2, "TAPESTRY", "Tapestry", 20,
                "A woven wall hanging depicting a scene", 5);
        c += saveFurniture(repo, TechLevel.TL2, "IRON_BED", "Iron Bed", 10,
                "A bed with an iron frame", 15);
        c += saveFurniture(repo, TechLevel.TL2, "CANDLESTICK", "Candlestick", 1,
                "A metal candle holder", 2);

        // TREASURE
        c += saveTreasure(repo, TechLevel.TL2, "GOLD_HOARD", "Gold Hoard", "Rare", 500,
                "A large pile of gold coins and treasure");
        c += saveTreasure(repo, TechLevel.TL2, "DIAMOND", "Diamond", "Rare", 200,
                "A flawless diamond of great value");
        c += saveTreasure(repo, TechLevel.TL2, "CROWN_JEWELS", "Crown Jewels", "Very Rare", 1000,
                "A set of royal jewels and regalia");

        // TRAP
        c += saveTrap(repo, TechLevel.TL2, "FALLING_ROCKS", "Falling Rocks Trap", 13, "bludgeoning", "3d8",
                "Massive rocks dropped from above");
        c += saveTrap(repo, TechLevel.TL2, "ARROW_TRAP", "Arrow Trap", 14, "piercing", "2d8",
                "Hidden crossbows firing arrows");
        c += saveTrap(repo, TechLevel.TL2, "MAGIC_TRAP", "Magic Trap", 15, "force", "4d6",
                "A trap that releases magical energy");

        // DOOR
        c += saveDoor(repo, TechLevel.TL2, "PORTCULLIS", "Portcullis", 30,
                "A heavy iron gate that drops from above", 60, null);
        c += saveDoor(repo, TechLevel.TL2, "MAGIC_DOOR", "Magic Door", 50,
                "A door protected by magical wards", null, 15);

        // CHEST
        c += saveChest(repo, TechLevel.TL2, "TREASURE_CHEST", "Treasure Chest", 20,
                "An ornate chest for storing valuables");

        // MONSTER
        c += saveMonster(repo, TechLevel.TL2, "ORC", "Orc", 2, "Humanoid", "Medium", 15, 13,
                null, null, "A brutal humanoid warrior");
        c += saveMonster(repo, TechLevel.TL2, "OGRE", "Ogre", 2, "Giant", "Huge", 59, 11,
                null, null, "A giant humanoid brute");
        c += saveMonster(repo, TechLevel.TL2, "WOLF", "Giant Wolf", 1, "Beast", "Large", 22, 13,
                null, null, "A massive wolf that hunts in packs");
        c += saveMonster(repo, TechLevel.TL2, "BANSHEE", "Banshee", 3, "Undead", "Medium", 45, 12,
                null, null, "A wailing spirit that causes fear");

        // MAGIC_ITEM
        c += saveMagicItem(repo, TechLevel.TL2, "RING_OF_PROTECTION", "Ring of Protection", "Rare",
                "Grants +1 to AC and saving throws");
        c += saveMagicItem(repo, TechLevel.TL2, "STAFF_OF_HEALING", "Staff of Healing", "Rare",
                "A staff that can restore hit points");
        c += saveMagicItem(repo, TechLevel.TL2, "AMULET_OF_HEALTH", "Amulet of Health", "Rare",
                "Sets Constitution to 19");

        // NPC
        c += saveNpc(repo, TechLevel.TL2, "KNIGHT", "Knight",
                "A noble warrior sworn to a lord");
        c += saveNpc(repo, TechLevel.TL2, "WIZARD", "Wizard",
                "A powerful spellcaster");
        c += saveNpc(repo, TechLevel.TL2, "BLACKSMITH", "Blacksmith",
                "A skilled metalworker who crafts weapons");

        // SPELL
        c += saveSpell(repo, TechLevel.TL2, "FIREBALL", "Fireball", 3, "Evocation", "fire", "8d6",
                "Creates an intense burst of flames");
        c += saveSpell(repo, TechLevel.TL2, "LIGHTNING_BOLT", "Lightning Bolt", 3, "Evocation", "lightning", "8d6",
                "A bolt of lightning strikes in a line");
        c += saveSpell(repo, TechLevel.TL2, "INVISIBILITY", "Invisibility", 2, "Illusion", null, null,
                "Makes a creature invisible");

        // POTION
        c += savePotion(repo, TechLevel.TL2, "GREATER_HEALING_POTION", "Greater Potion of Healing", "Uncommon", null, null, null, 150,
                "Restores 4d4+4 hit points");
        c += savePotion(repo, TechLevel.TL2, "POISON_VIAL_GREATER", "Greater Poison Vial", null, 12, "poison", "4d4", null,
                "A more potent contact poison");

        // WEAPON
        c += saveWeapon(repo, TechLevel.TL2, "WARHAMMER", "Warhammer", 15, "bludgeoning", "1d8", 2,
                "A heavy hammer for smashing armor");
        c += saveWeapon(repo, TechLevel.TL2, "HALBERD", "Halberd", 20, "slashing", "1d10", 6,
                "A polearm with an axe blade");
        c += saveWeapon(repo, TechLevel.TL2, "CROSSBOW", "Heavy Crossbow", 50, "piercing", "1d10", 18,
                "A powerful ranged weapon");

        // ARMOR
        c += saveArmor(repo, TechLevel.TL2, "PLATE_ARMOR", "Plate Armor", 1500, 18, 65,
                "Full plate metal armor");
        c += saveArmor(repo, TechLevel.TL2, "CHAIN_SHIRT", "Chain Shirt", 125, 13, 20,
                "Flexible chain mail shirt");

        // ADVENTURE_GEAR
        c += saveGear(repo, TechLevel.TL2, "ALCHEMISTS_FIRE", "Alchemist's Fire", 50, "fire", "1d8", 1,
                "Flammable liquid in a flask");
        c += saveGear(repo, TechLevel.TL2, "HOLY_WATER", "Holy Water", 25, "radiant", "2d6", 0,
                "Water blessed for use against undead");

        return c;
    }

    // --- TL3: Renaissance dungeon objects ---
    private int seedDnDTL3(PlanetBuildingModuleRepository repo) {
        int c = 0;

        // DUNGEON_ROOM
        c += saveRoom(repo, TechLevel.TL3, "ARCANE_LAB", "Arcane Laboratory",
                "A wizard's laboratory with magical apparatus");
        c += saveRoom(repo, TechLevel.TL3, "LIBRARY", "Ancient Library",
                "A vast library of ancient tomes and scrolls");
        c += saveRoom(repo, TechLevel.TL3, "OBSERVATORY", "Observatory",
                "A tower for studying the stars");

        // FURNITURE
        c += saveFurniture(repo, TechLevel.TL3, "BOOKSHELF", "Bookshelf", 10,
                "A large wooden bookshelf filled with tomes", 50);
        c += saveFurniture(repo, TechLevel.TL3, "WRITING_DESK", "Writing Desk", 15,
                "An ornate desk with ink and parchment", 20);
        c += saveFurniture(repo, TechLevel.TL3, "ASTROLABE", "Astrolabe", 50,
                "An intricate device for celestial navigation", 5);

        // TREASURE
        c += saveTreasure(repo, TechLevel.TL3, "ANCIENT_ARTIFACT", "Ancient Artifact", "Very Rare", 500,
                "A mysterious artifact of unknown power");
        c += saveTreasure(repo, TechLevel.TL3, "SPELL_SCROLL", "Spell Scroll", "Uncommon", 100,
                "A scroll containing a magical spell");

        // TRAP
        c += saveTrap(repo, TechLevel.TL3, "FIRE_TRAP", "Fire Trap", 14, "fire", "4d6",
                "A trap that erupts in flames");
        c += saveTrap(repo, TechLevel.TL3, "LIGHTNING_TRAP", "Lightning Trap", 15, "lightning", "5d6",
                "A trap that discharges electricity");

        // MONSTER
        c += saveMonster(repo, TechLevel.TL3, "MIND_FLAYER", "Mind Flayer", 10, "Aberration", "Medium", 130, 13,
                null, null, "An intelligent aberration that feeds on brains");
        c += saveMonster(repo, TechLevel.TL3, "DRAGON_WYRM", "Young Dragon", 6, "Dragon", "Large", 136, 17,
                "fire", "5d8", "A young dragon with breath weapons");

        // MAGIC_ITEM
        c += saveMagicItem(repo, TechLevel.TL3, "WINGED_BOOTS", "Winged Boots", "Rare",
                "Boots that grant the ability to fly");
        c += saveMagicItem(repo, TechLevel.TL3, "BAG_OF_HOLDING", "Bag of Holding", "Uncommon",
                "A bag with an extradimensional interior");

        // NPC
        c += saveNpc(repo, TechLevel.TL3, "ALCHEMIST", "Alchemist",
                "A scholar who creates potions and poisons");
        c += saveNpc(repo, TechLevel.TL3, "NOBLE", "Noble",
                "A wealthy and influential aristocrat");

        // SPELL
        c += saveSpell(repo, TechLevel.TL3, "COUNTERSPELL", "Counterspell", 3, "Abjuration", null, null,
                "Interrupts another creature's spellcasting");
        c += saveSpell(repo, TechLevel.TL3, "TELEPORT", "Teleport", 5, "Conjuration", null, null,
                "Instantly transports you to a distant location");

        // WEAPON
        c += saveWeapon(repo, TechLevel.TL3, "HANDGUN", "Handgun", 75, "piercing", "1d10", 2,
                "An early firearm for close range");

        // ARMOR
        c += saveArmor(repo, TechLevel.TL3, "BRONZE_PLATE", "Bronze Plate", 500, 16, 60,
                "Plate armor made of bronze");

        // ADVENTURE_GEAR
        c += saveGear(repo, TechLevel.TL3, "POISON_TIP", "Poison Tip", 10, "poison", "1d4", 0,
                "A weapon tip coated with poison");

        return c;
    }

    // --- TL4: Industrial dungeon objects (Eberron-style) ---
    private int seedDnDTL4(PlanetBuildingModuleRepository repo) {
        int c = 0;

        // DUNGEON_ROOM
        c += saveRoom(repo, TechLevel.TL4, "STEAM_FACTORY", "Steam Factory",
                "A factory powered by steam engines");
        c += saveRoom(repo, TechLevel.TL4, "RAILWAY_TUNNEL", "Railway Tunnel",
                "An underground tunnel for trains");

        // FURNITURE
        c += saveFurniture(repo, TechLevel.TL4, "TELEGRAPH", "Telegraph Machine", 100,
                "A device for sending messages over wires", 30);
        c += saveFurniture(repo, TechLevel.TL4, "GAS_LAMP", "Gas Lamp", 10,
                "A lamp powered by gas for bright light", 5);

        // TREASURE
        c += saveTreasure(repo, TechLevel.TL4, "DRAGON_HOARD", "Dragon's Hoard", "Legendary", 50000,
                "A massive hoard of gold and magical items");

        // TRAP
        c += saveTrap(repo, TechLevel.TL4, "STEAM_VENT", "Steam Vent Trap", 13, "fire", "4d6",
                "Scalding steam released from pipes");
        c += saveTrap(repo, TechLevel.TL4, "ELECTRIC_TRAP", "Electric Trap", 14, "lightning", "6d6",
                "A trap that shocks with electricity");

        // MONSTER
        c += saveMonster(repo, TechLevel.TL4, "ANCIENT_DRAGON", "Ancient Red Dragon", 24, "Dragon", "Gargantuan", 546, 22,
                "fire", "7d6", "An ancient dragon of immense power");
        c += saveMonster(repo, TechLevel.TL4, "LICH", "Lich", 21, "Undead", "Medium", 135, 17,
                "necrotic", "4d8", "An undead wizard of terrible power");
        c += saveMonster(repo, TechLevel.TL4, "DEVIL", "Archdevil", 21, "Fiend", "Large", 145, 19,
                "fire", "5d10", "A powerful devil from the Nine Hells");

        // MAGIC_ITEM
        c += saveMagicItem(repo, TechLevel.TL4, "STAFF_OF_POWER", "Staff of Power", "Very Rare",
                "A powerful staff with many magical abilities");
        c += saveMagicItem(repo, TechLevel.TL4, "DEATHS_DOOR", "Death's Door", "Legendary",
                "A legendary sword that drains life");
        c += saveMagicItem(repo, TechLevel.TL4, "ROBE_OF_THE_ARCHMAGE", "Robe of the Archmage", "Legendary",
                "A robe that enhances spellcasting");

        // NPC
        c += saveNpc(repo, TechLevel.TL4, "DRAGONLORD", "Dragonlord",
                "A powerful ruler who commands dragons");
        c += saveNpc(repo, TechLevel.TL4, "ARCHMAGE", "Archmage",
                "The most powerful wizard in the realm");

        // SPELL
        c += saveSpell(repo, TechLevel.TL4, "WISH", "Wish", 9, "Conjuration", null, null,
                "The most powerful spell in existence");
        c += saveSpell(repo, TechLevel.TL4, "METAMORPHOSIS", "Metamorphosis", 8, "Transmutation", null, null,
                "Transforms a creature into a new form");

        // WEAPON
        c += saveWeapon(repo, TechLevel.TL4, "RIFLE", "Rifle", 100, "piercing", "1d10", 5,
                "A long-range firearm");
        c += saveWeapon(repo, TechLevel.TL4, "MAGIC_LONGSWORD", "Magic Longsword +1", 500, "slashing", "1d8+1", 3,
                "A longsword enchanted to strike harder");

        // ARMOR
        c += saveArmor(repo, TechLevel.TL4, "MAGIC_PLATE", "Magic Plate +1", 2000, 19, 65,
                "Plate armor enchanted for better protection");

        // ADVENTURE_GEAR
        c += saveGear(repo, TechLevel.TL4, "GUNPOWDER", "Gunpowder", 50, "fire", "2d6", 1,
                "Explosive powder for firearms and traps");

        return c;
    }

    // === Dungeon object save helper ===

    private int saveDnD(PlanetBuildingModuleRepository repo, TechLevel techLevel, ModuleCategory category,
                        String genericType, String genericName, int baseCost, int baseCapacity, String gameType,
                        String description, Double cr, String rarity, Integer dc,
                        String damageType, String damageExpr, Integer spellLevel, String spellSchool,
                        String creatureType, String size, Integer hp, Integer ac, Integer gpValue, Integer weightLb) {
        PlanetBuildingModuleEntity entity = new PlanetBuildingModuleEntity(
                techLevel, category, genericType, genericName, baseCost, baseCapacity, description);
        entity.setGameType(gameType);
        if (cr != null) entity.setCr(cr);
        if (rarity != null) entity.setRarity(rarity);
        if (dc != null) entity.setDc(dc);
        if (damageType != null) entity.setDamageType(damageType);
        if (damageExpr != null) entity.setDamageExpr(damageExpr);
        if (spellLevel != null) entity.setSpellLevel(spellLevel);
        if (spellSchool != null) entity.setSpellSchool(spellSchool);
        if (creatureType != null) entity.setCreatureType(creatureType);
        if (size != null) entity.setSize(size);
        if (hp != null) entity.setHp(hp);
        if (ac != null) entity.setAc(ac);
        if (gpValue != null) entity.setGpValue(gpValue);
        if (weightLb != null) entity.setWeightLb(weightLb);
        repo.save(entity);
        return 1;
    }

    // === Shortcuts for common dungeon object types ===

    private int saveRoom(PlanetBuildingModuleRepository repo, TechLevel tl, String type, String name, String desc) {
        return saveDnD(repo, tl, ModuleCategory.DUNGEON_ROOM, type, name, 0, 1, "DND_5E", desc,
                null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private int saveFurniture(PlanetBuildingModuleRepository repo, TechLevel tl, String type, String name,
                              int cost, String desc, Integer weightLb) {
        return saveDnD(repo, tl, ModuleCategory.FURNITURE, type, name, cost, 1, "DND_5E", desc,
                null, null, null, null, null, null, null, null, null, null, null, null, weightLb);
    }

    private int saveTreasure(PlanetBuildingModuleRepository repo, TechLevel tl, String type, String name,
                             String rarity, Integer gpValue, String desc) {
        return saveDnD(repo, tl, ModuleCategory.TREASURE, type, name, 0, 1, "DND_5E", desc,
                null, rarity, null, null, null, null, null, null, null, null, null, gpValue, null);
    }

    private int saveTrap(PlanetBuildingModuleRepository repo, TechLevel tl, String type, String name,
                         Integer dc, String damageType, String damageExpr, String desc) {
        return saveDnD(repo, tl, ModuleCategory.TRAP, type, name, 0, 1, "DND_5E", desc,
                null, null, dc, damageType, damageExpr, null, null, null, null, null, null, null, null);
    }

    private int saveDoor(PlanetBuildingModuleRepository repo, TechLevel tl, String type, String name,
                         int cost, String desc, Integer hp, Integer dc) {
        return saveDnD(repo, tl, ModuleCategory.DOOR, type, name, cost, 1, "DND_5E", desc,
                null, null, dc, null, null, null, null, null, null, hp, null, null, null);
    }

    private int saveChest(PlanetBuildingModuleRepository repo, TechLevel tl, String type, String name,
                          int cost, String desc) {
        return saveDnD(repo, tl, ModuleCategory.CHEST, type, name, cost, 1, "DND_5E", desc,
                null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private int saveMonster(PlanetBuildingModuleRepository repo, TechLevel tl, String type, String name,
                            double cr, String creatureType, String size, int hp, int ac,
                            String damageType, String damageExpr, String desc) {
        return saveDnD(repo, tl, ModuleCategory.MONSTER, type, name, 0, 1, "DND_5E", desc,
                cr, null, null, damageType, damageExpr, null, null, creatureType, size, hp, ac, null, null);
    }

    private int saveMagicItem(PlanetBuildingModuleRepository repo, TechLevel tl, String type, String name,
                              String rarity, String desc) {
        return saveDnD(repo, tl, ModuleCategory.MAGIC_ITEM, type, name, 0, 1, "DND_5E", desc,
                null, rarity, null, null, null, null, null, null, null, null, null, null, null);
    }

    private int saveNpc(PlanetBuildingModuleRepository repo, TechLevel tl, String type, String name, String desc) {
        return saveDnD(repo, tl, ModuleCategory.NPC, type, name, 0, 1, "DND_5E", desc,
                null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private int saveSpell(PlanetBuildingModuleRepository repo, TechLevel tl, String type, String name,
                          int spellLevel, String spellSchool, String damageType, String damageExpr, String desc) {
        return saveDnD(repo, tl, ModuleCategory.SPELL, type, name, 0, 1, "DND_5E", desc,
                null, null, null, damageType, damageExpr, spellLevel, spellSchool, null, null, null, null, null, null);
    }

    private int savePotion(PlanetBuildingModuleRepository repo, TechLevel tl, String type, String name,
                           String rarity, Integer dc, String damageType, String damageExpr,
                           Integer gpValue, String desc) {
        return saveDnD(repo, tl, ModuleCategory.POTION, type, name, 0, 1, "DND_5E", desc,
                null, rarity, dc, damageType, damageExpr, null, null, null, null, null, null, gpValue, null);
    }

    private int saveWeapon(PlanetBuildingModuleRepository repo, TechLevel tl, String type, String name,
                           int cost, String damageType, String damageExpr, Integer weightLb, String desc) {
        return saveDnD(repo, tl, ModuleCategory.WEAPON, type, name, cost, 1, "DND_5E", desc,
                null, null, null, damageType, damageExpr, null, null, null, null, null, null, null, weightLb);
    }

    private int saveArmor(PlanetBuildingModuleRepository repo, TechLevel tl, String type, String name,
                          int cost, Integer ac, Integer weightLb, String desc) {
        return saveDnD(repo, tl, ModuleCategory.ARMOR, type, name, cost, 1, "DND_5E", desc,
                null, null, null, null, null, null, null, null, null, null, ac, null, weightLb);
    }

    private int saveGear(PlanetBuildingModuleRepository repo, TechLevel tl, String type, String name,
                         int cost, String damageType, String damageExpr, Integer weightLb, String desc) {
        return saveDnD(repo, tl, ModuleCategory.ADVENTURE_GEAR, type, name, cost, 1, "DND_5E", desc,
                null, null, null, damageType, damageExpr, null, null, null, null, null, null, null, weightLb);
    }

    // === Sci-Fi Interior Object Seeding (TL5–TL12) ===
    // Uses Traveller and Star Trek lore for high-tech building interiors

    private int seedSciFiInteriorObjects(PlanetBuildingModuleRepository repo) {
        int c = 0;
        c += seedSciFiTL5(repo);
        c += seedSciFiTL6(repo);
        c += seedSciFiTL7(repo);
        c += seedSciFiTL8(repo);
        c += seedSciFiTL9(repo);
        c += seedSciFiTL10(repo);
        c += seedSciFiTL11(repo);
        c += seedSciFiTL12(repo);
        return c;
    }

    // --- TL5: Modern interiors ---
    private int seedSciFiTL5(PlanetBuildingModuleRepository repo) {
        int c = 0;
        // DUNGEON_ROOM
        c += saveSciFiRoom(repo, TechLevel.TL5, "OFFICE_LOBBY", "Office Lobby",
                "Modern office lobby with reception desk and security checkpoint");
        c += saveSciFiRoom(repo, TechLevel.TL5, "CONFERENCE_ROOM", "Conference Room",
                "Boardroom with video conferencing equipment");
        c += saveSciFiRoom(repo, TechLevel.TL5, "SERVER_ROOM", "Server Room",
                "Climate-controlled data center with server racks");
        c += saveSciFiRoom(repo, TechLevel.TL5, "SURVEILLANCE_CENTER", "Surveillance Center",
                "Security monitoring room with wall of screens");
        // FURNITURE
        c += saveSciFiFurniture(repo, TechLevel.TL5, "OFFICE_DESK", "Office Desk", 200,
                "Modern ergonomic desk with built-in power outlets", 50);
        c += saveSciFiFurniture(repo, TechLevel.TL5, "OFFICE_CHAIR", "Ergonomic Chair", 150,
                "Adjustable office chair with lumbar support", 30);
        c += saveSciFiFurniture(repo, TechLevel.TL5, "SERVER_RACK", "Server Rack", 5000,
                "Standard 42U server rack with cooling", 500);
        c += saveSciFiFurniture(repo, TechLevel.TL5, "SECURITY_CONSOLE", "Security Console", 3000,
                "Multi-monitor security monitoring station", 100);
        // TRAP
        c += saveSciFiTrap(repo, TechLevel.TL5, "ELECTRIC_FENCE", "Electric Fence", 12, "lightning", "2d6",
                "High-voltage perimeter fence");
        c += saveSciFiTrap(repo, TechLevel.TL5, "CCTV_ALARM", "CCTV Alarm Trigger", 10, null, null,
                "Motion-activated camera system that alerts security");
        // DOOR
        c += saveSciFiDoor(repo, TechLevel.TL5, "ELECTRONIC_DOOR", "Electronic Door", 500,
                "Keycard-access reinforced door", 50, 8);
        c += saveSciFiDoor(repo, TechLevel.TL5, "SECURITY_GATE", "Security Gate", 1000,
                "Biometric-scanned security gate", 100, 12);
        // CHEST
        c += saveSciFiChest(repo, TechLevel.TL5, "SAFE", "Electronic Safe", 2000,
                "Digital combination safe with biometric lock");
        c += saveSciFiChest(repo, TechLevel.TL5, "LOCKER", "Secure Locker", 200,
                "Personal storage locker with electronic lock");
        return c;
    }

    // --- TL6: Information Age interiors ---
    private int seedSciFiTL6(PlanetBuildingModuleRepository repo) {
        int c = 0;
        // DUNGEON_ROOM
        c += saveSciFiRoom(repo, TechLevel.TL6, "COMMAND_CENTER", "Command Center",
                "Mission control with holographic displays and operator stations");
        c += saveSciFiRoom(repo, TechLevel.TL6, "RESEARCH_LAB", "Research Laboratory",
                "Advanced scientific lab with containment fields");
        c += saveSciFiRoom(repo, TechLevel.TL6, "HOVER_DEPOT", "Hover Vehicle Depot",
                "Parking and maintenance bay for hover transports");
        c += saveSciFiRoom(repo, TechLevel.TL6, "ORBITAL_CONTROL", "Orbital Control Room",
                "Launch control center monitoring orbital operations");
        c += saveSciFiRoom(repo, TechLevel.TL6, "MEDBAY", "Medical Bay",
                "Advanced medical facility with diagnostic equipment");
        // FURNITURE
        c += saveSciFiFurniture(repo, TechLevel.TL6, "HOLO_TABLE", "Holographic Table", 10000,
                "Table with holographic display surface", 200);
        c += saveSciFiFurniture(repo, TechLevel.TL6, "CRYO_POD", "Cryogenic Pod", 50000,
                "Stasis pod for long-duration space travel", 1000);
        c += saveSciFiFurniture(repo, TechLevel.TL6, "DIAGNOSTIC_BED", "Diagnostic Med-Bed", 25000,
                "Medical bed with full-body scanning capability", 300);
        c += saveSciFiFurniture(repo, TechLevel.TL6, "CONSOLE_TERMINAL", "Operator Console", 15000,
                "Touch-screen operator terminal with neural interface", 150);
        // TRAP
        c += saveSciFiTrap(repo, TechLevel.TL6, "LASER_GRID", "Laser Grid", 16, "fire", "4d6",
                "Invisible laser security grid");
        c += saveSciFiTrap(repo, TechLevel.TL6, "EMP_PULSE", "EMP Pulse Trap", 14, "lightning", "3d6",
                "Electromagnetic pulse that disables electronics");
        c += saveSciFiTrap(repo, TechLevel.TL6, "GAS_VENT", "Neurotoxin Gas Vent", 15, "poison", "5d6",
                "Toxic gas release from ceiling vents");
        // DOOR
        c += saveSciFiDoor(repo, TechLevel.TL6, "BLAST_DOOR", "Blast Door", 5000,
                "Reinforced blast door with magnetic lock", 200, 15);
        c += saveSciFiDoor(repo, TechLevel.TL6, "AIRLOCK", "Airlock Door", 8000,
                "Pressurized airlock with environmental seals", 150, 14);
        // CHEST
        c += saveSciFiChest(repo, TechLevel.TL6, "CARGO_CONTAINER", "Cargo Container", 3000,
                "Standardized cargo container with RFID tracking");
        c += saveSciFiChest(repo, TechLevel.TL6, "VAULT", "Secure Vault", 50000,
                "High-security vault with multi-factor authentication");
        return c;
    }

    // --- TL7: Interplanetary interiors ---
    private int seedSciFiTL7(PlanetBuildingModuleRepository repo) {
        int c = 0;
        // DUNGEON_ROOM
        c += saveSciFiRoom(repo, TechLevel.TL7, "SHIP_BRIDGE", "Ship Bridge",
                "Command center of an interplanetary vessel");
        c += saveSciFiRoom(repo, TechLevel.TL7, "FUSION_CORE", "Fusion Reactor Core",
                "Glowing fusion reactor powering the facility");
        c += saveSciFiRoom(repo, TechLevel.TL7, "ASTEROID_MINE_SHAFT", "Asteroid Mine Shaft",
                "Underground mining tunnel in an asteroid");
        c += saveSciFiRoom(repo, TechLevel.TL7, "BIOTECH_LAB", "Biotech Laboratory",
                "Genetic engineering lab with containment protocols");
        c += saveSciFiRoom(repo, TechLevel.TL7, "DOCKING_BAY", "Docking Bay",
                "Large bay for docking interplanetary ships");
        c += saveSciFiRoom(repo, TechLevel.TL7, "ZERO_G_CHAMBER", "Zero-G Chamber",
                "Gravity-neutralized training and exercise room");
        // FURNITURE
        c += saveSciFiFurniture(repo, TechLevel.TL7, "COMMAND_CHAIR", "Captain's Command Chair", 20000,
                "Ergonomic command chair with integrated controls", 400);
        c += saveSciFiFurniture(repo, TechLevel.TL7, "REPPLICATOR", "Matter Replicator", 100000,
                "Device that assembles objects from raw materials", 500);
        c += saveSciFiFurniture(repo, TechLevel.TL7, "NAVIGATION_CONSOLE", "Navigation Console", 30000,
                "Star chart and navigation control station", 250);
        c += saveSciFiFurniture(repo, TechLevel.TL7, "CONTAINMENT_UNIT", "Bio-Containment Unit", 15000,
                "Sealed container for hazardous biological samples", 200);
        // TRAP
        c += saveSciFiTrap(repo, TechLevel.TL7, "CONTAINMENT_FIELD", "Containment Field", 18, "force", "6d6",
                "Energy field that traps intruders");
        c += saveSciFiTrap(repo, TechLevel.TL7, "GRAVITY_WELL", "Gravity Well Trap", 17, "bludgeoning", "5d6",
                "Localized gravity anomaly that crushes targets");
        c += saveSciFiTrap(repo, TechLevel.TL7, "AUTO_TURRET", "Auto-Turret", 16, "piercing", "4d6",
                "Automated defense turret with motion sensors");
        // DOOR
        c += saveSciFiDoor(repo, TechLevel.TL7, "FORCE_FIELD_DOOR", "Force Field Door", 10000,
                "Energy barrier door with access codes", 100, 16);
        c += saveSciFiDoor(repo, TechLevel.TL7, "HERMETIC_DOOR", "Hermetic Seal Door", 7000,
                "Airtight door for vacuum environments", 180, 15);
        // CHEST
        c += saveSciFiChest(repo, TechLevel.TL7, "CRYO_STORAGE", "Cryogenic Storage Unit", 25000,
                "Ultra-cold storage for sensitive materials");
        c += saveSciFiChest(repo, TechLevel.TL7, "ORBITAL_LOCKER", "Orbital Cargo Locker", 5000,
                "Zero-gravity secured cargo container");
        return c;
    }

    // --- TL8: Interstellar interiors ---
    private int seedSciFiTL8(PlanetBuildingModuleRepository repo) {
        int c = 0;
        // DUNGEON_ROOM
        c += saveSciFiRoom(repo, TechLevel.TL8, "STARSHIP_BRIDGE", "Starship Bridge",
                "Bridge of an FTL-capable starship with viewscreen");
        c += saveSciFiRoom(repo, TechLevel.TL8, "ANTIMATTER_CORE", "Antimatter Reactor Core",
                "Blinding antimatter containment chamber");
        c += saveSciFiRoom(repo, TechLevel.TL8, "NANOFAB_BAY", "Nanofabrication Bay",
                "Molecular assembly floor with nanite swarms");
        c += saveSciFiRoom(repo, TechLevel.TL8, "JUMP_GATE_CONTROL", "Jump Gate Control",
                "Control room for FTL jump gate operations");
        c += saveSciFiRoom(repo, TechLevel.TL8, "OBSERVATION_DECK", "Observation Deck",
                "Transparent dome overlooking space and stars");
        c += saveSciFiRoom(repo, TechLevel.TL8, "ENGINEERING", "Engineering Deck",
                "Main engineering with warp core access");
        c += saveSciFiRoom(repo, TechLevel.TL8, "TRANSPORTER_ROOM", "Transporter Room",
                "Teleportation chamber with pattern buffers");
        // FURNITURE
        c += saveSciFiFurniture(repo, TechLevel.TL8, "HOLODECK_EMITTER", "Holodeck Emitter", 200000,
                "Holographic simulation room projector", 1000);
        c += saveSciFiFurniture(repo, TechLevel.TL8, "TRICORDER", "Handheld Tricorder", 5000,
                "Multi-function scanning and analysis device", 2);
        c += saveSciFiFurniture(repo, TechLevel.TL8, "WARP_CONSOLE", "Warp Drive Console", 50000,
                "Warp drive control and monitoring station", 300);
        c += saveSciFiFurniture(repo, TechLevel.TL8, "SHIELD_GENERATOR", "Shield Generator", 100000,
                "Deflector shield control unit", 500);
        // TRAP
        c += saveSciFiTrap(repo, TechLevel.TL8, "PHOTON_TORPEDO", "Photon Torpedo Trap", 20, "fire", "8d6",
                "Hidden photon torpedo launcher");
        c += saveSciFiTrap(repo, TechLevel.TL8, "SHIELD_DISRUPTOR", "Shield Disruptor", 18, "force", "6d6",
                "Device that disables personal shields");
        c += saveSciFiTrap(repo, TechLevel.TL8, "NANITE_SWARM", "Nanite Swarm Trap", 19, "acid", "7d6",
                "Cloud of destructive nanobots");
        // DOOR
        c += saveSciFiDoor(repo, TechLevel.TL8, "ENERGY_DOOR", "Energy Door", 20000,
                "Plasma energy barrier with retinal scan", 150, 18);
        c += saveSciFiDoor(repo, TechLevel.TL8, "TRANSPORTER_LOCK", "Transporter Lock", 15000,
                "Door that blocks transporter beams", 100, 17);
        // CHEST
        c += saveSciFiChest(repo, TechLevel.TL8, "ANTIMATTER_CANISTER", "Antimatter Canister", 100000,
                "Shielded container for antimatter fuel");
        c += saveSciFiChest(repo, TechLevel.TL8, "QUANTUM_VAULT", "Quantum Vault", 200000,
                "Quantum-encrypted secure storage");
        return c;
    }

    // --- TL9: Galactic interiors ---
    private int seedSciFiTL9(PlanetBuildingModuleRepository repo) {
        int c = 0;
        // DUNGEON_ROOM
        c += saveSciFiRoom(repo, TechLevel.TL9, "STARBASE_COMMAND", "Starbase Command Center",
                "Massive command center overseeing galactic operations");
        c += saveSciFiRoom(repo, TechLevel.TL9, "AI_CORE_CHAMBER", "AI Core Chamber",
                "Housing for a galactic-scale artificial intelligence");
        c += saveSciFiRoom(repo, TechLevel.TL9, "WORMHOLE_TERMINAL", "Wormhole Terminal",
                "Stable wormhole gateway to another sector");
        c += saveSciFiRoom(repo, TechLevel.TL9, "DREADNOUGHT_BRIDGE", "Dreadnought Bridge",
                "Command deck of an empire-scale warship");
        c += saveSciFiRoom(repo, TechLevel.TL9, "PLANETARY_COMPUTER", "Planetary Computer Core",
                "World-scale computing matrix processing center");
        // FURNITURE
        c += saveSciFiFurniture(repo, TechLevel.TL9, "AI_INTERFACE", "AI Interface Terminal", 500000,
                "Direct neural link to galactic AI", 100);
        c += saveSciFiFurniture(repo, TechLevel.TL9, "WORMHOLE_CONSOLE", "Wormhole Control Console", 300000,
                "Controls for stable wormhole navigation", 500);
        c += saveSciFiFurniture(repo, TechLevel.TL9, "GALACTIC_MAP", "Galactic Holographic Map", 200000,
                "Real-time 3D map of the entire galaxy", 2000);
        // TRAP
        c += saveSciFiTrap(repo, TechLevel.TL9, "ANTIMATTER_BOMB", "Antimatter Bomb Trap", 22, "force", "12d6",
                "Hidden antimatter detonation device");
        c += saveSciFiTrap(repo, TechLevel.TL9, "REALITY_GLITCH", "Reality Glitch Field", 21, "force", "10d6",
                "Localized reality distortion that damages matter");
        // DOOR
        c += saveSciFiDoor(repo, TechLevel.TL9, "DIMENSIONAL_DOOR", "Dimensional Door", 500000,
                "Portal that shifts between dimensions", 200, 20);
        // CHEST
        c += saveSciFiChest(repo, TechLevel.TL9, "DIMENSIONAL_VAULT", "Dimensional Vault", 1000000,
                "Storage that exists partially in another dimension");
        return c;
    }

    // --- TL10: Post-Human interiors ---
    private int seedSciFiTL10(PlanetBuildingModuleRepository repo) {
        int c = 0;
        // DUNGEON_ROOM
        c += saveSciFiRoom(repo, TechLevel.TL10, "MEGASTRUCTURE_HUB", "Megastructure Control Hub",
                "Central control for a Dyson sphere or similar megastructure");
        c += saveSciFiRoom(repo, TechLevel.TL10, "REALITY_ENGINE_ROOM", "Reality Engine Chamber",
                "Room where physical constants are manipulated");
        c += saveSciFiRoom(repo, TechLevel.TL10, "MATRIOSKA_CORE", "Matrioshka Brain Core",
                "Computing core using an entire star's energy");
        // FURNITURE
        c += saveSciFiFurniture(repo, TechLevel.TL10, "REALITY_CONSOLE", "Reality Engine Console", 1000000,
                "Interface for manipulating local physics", 0);
        c += saveSciFiFurniture(repo, TechLevel.TL10, "STAR_HARVESTER", "Star Harvester Control", 5000000,
                "Controls for extracting stellar matter", 0);
        // TRAP
        c += saveSciFiTrap(repo, TechLevel.TL10, "REALITY_COLLAPSE", "Reality Collapse Field", 25, "force", "20d6",
                "Zone where reality begins to unravel");
        // DOOR
        c += saveSciFiDoor(repo, TechLevel.TL10, "TRANSWARP_GATE", "Transwarp Gate", 5000000,
                "Gateway to transwarp lanes", 500, 25);
        // CHEST
        c += saveSciFiChest(repo, TechLevel.TL10, "OMNIVERSAL_CONTAINER", "Omniversal Container", 10000000,
                "Container that holds objects from multiple universes");
        return c;
    }

    // --- TL11: Transcendent interiors ---
    private int seedSciFiTL11(PlanetBuildingModuleRepository repo) {
        int c = 0;
        // DUNGEON_ROOM
        c += saveSciFiRoom(repo, TechLevel.TL11, "DIMENSIONAL_HUB", "Dimensional Hub",
                "Gateway connecting parallel dimensions");
        c += saveSciFiRoom(repo, TechLevel.TL11, "TIME_CHAMBER", "Time Manipulation Chamber",
                "Room where time flows differently");
        c += saveSciFiRoom(repo, TechLevel.TL11, "UNIVERSE_SEEDER", "Universe Seeder Core",
                "Device for creating pocket universes");
        // FURNITURE
        c += saveSciFiFurniture(repo, TechLevel.TL11, "TIME_CONSOLE", "Temporal Control Console", 10000000,
                "Interface for controlling time flow", 0);
        c += saveSciFiFurniture(repo, TechLevel.TL11, "DIMENSIONAL_VIEWER", "Dimensional Viewer", 5000000,
                "Device that shows events across dimensions", 0);
        // TRAP
        c += saveSciFiTrap(repo, TechLevel.TL11, "TIME_LOOP", "Time Loop Trap", 28, "force", "15d6",
                "Traps victims in an endless time loop");
        // DOOR
        c += saveSciFiDoor(repo, TechLevel.TL11, "DIMENSIONAL_PORTAL", "Dimensional Portal", 20000000,
                "Stable passage between parallel realities", 1000, 28);
        // CHEST
        c += saveSciFiChest(repo, TechLevel.TL11, "TIMELINE_VAULT", "Timeline Vault", 50000000,
                "Secure storage across multiple timelines");
        return c;
    }

    // --- TL12: Post-Singularity interiors ---
    private int seedSciFiTL12(PlanetBuildingModuleRepository repo) {
        int c = 0;
        // DUNGEON_ROOM
        c += saveSciFiRoom(repo, TechLevel.TL12, "OMNIVERSAL_THRONE", "Omniversal Throne Room",
                "Seat of post-singularity consciousness overseeing all existence");
        c += saveSciFiRoom(repo, TechLevel.TL12, "COSMIC_LOOM_CHAMBER", "Cosmic Loom Chamber",
                "Room where the fabric of spacetime is woven");
        c += saveSciFiRoom(repo, TechLevel.TL12, "ABSOLUTE_KNOWLEDGE", "Absolute Knowledge Archive",
                "Omniscient information repository of all existence");
        // FURNITURE
        c += saveSciFiFurniture(repo, TechLevel.TL12, "OMNIVERSAL_THRONE", "Omniversal Throne", 100000000,
                "Seat of ultimate power over all realities", 0);
        c += saveSciFiFurniture(repo, TechLevel.TL12, "COSMIC_LOOM", "Cosmic Loom", 500000000,
                "Device that weaves the fabric of spacetime", 0);
        // TRAP
        c += saveSciFiTrap(repo, TechLevel.TL12, "EXISTENCE_ERASURE", "Existence Erasure Field", 30, "force", "30d6",
                "Zone that erases targets from all of existence");
        // DOOR
        c += saveSciFiDoor(repo, TechLevel.TL12, "OMNIVERSAL_GATE", "Omniversal Gate", 1000000000,
                "Gateway to all of existence simultaneously", 10000, 30);
        // CHEST
        c += saveSciFiChest(repo, TechLevel.TL12, "OMNIVERSAL_VAULT", "Omniversal Vault", 5000000000L,
                "Container holding artifacts from all of existence");
        return c;
    }

    // === Sci-Fi interior save helpers ===

    private int saveSciFiRoom(PlanetBuildingModuleRepository repo, TechLevel tl, String type, String name, String desc) {
        PlanetBuildingModuleEntity entity = new PlanetBuildingModuleEntity(
                tl, ModuleCategory.DUNGEON_ROOM, type, name, 0, 1, desc);
        entity.setGameType("TRAVELLER");
        repo.save(entity);
        return 1;
    }

    private int saveSciFiFurniture(PlanetBuildingModuleRepository repo, TechLevel tl, String type, String name,
                                    int cost, String desc, Integer weightLb) {
        PlanetBuildingModuleEntity entity = new PlanetBuildingModuleEntity(
                tl, ModuleCategory.FURNITURE, type, name, cost, 1, desc);
        entity.setGameType("TRAVELLER");
        if (weightLb != null) entity.setWeightLb(weightLb);
        repo.save(entity);
        return 1;
    }

    private int saveSciFiTrap(PlanetBuildingModuleRepository repo, TechLevel tl, String type, String name,
                              Integer dc, String damageType, String damageExpr, String desc) {
        PlanetBuildingModuleEntity entity = new PlanetBuildingModuleEntity(
                tl, ModuleCategory.TRAP, type, name, 0, 1, desc);
        entity.setGameType("TRAVELLER");
        if (dc != null) entity.setDc(dc);
        if (damageType != null) entity.setDamageType(damageType);
        if (damageExpr != null) entity.setDamageExpr(damageExpr);
        repo.save(entity);
        return 1;
    }

    private int saveSciFiDoor(PlanetBuildingModuleRepository repo, TechLevel tl, String type, String name,
                              int cost, String desc, Integer hp, Integer dc) {
        PlanetBuildingModuleEntity entity = new PlanetBuildingModuleEntity(
                tl, ModuleCategory.DOOR, type, name, cost, 1, desc);
        entity.setGameType("TRAVELLER");
        if (hp != null) entity.setHp(hp);
        if (dc != null) entity.setDc(dc);
        repo.save(entity);
        return 1;
    }

    private int saveSciFiChest(PlanetBuildingModuleRepository repo, TechLevel tl, String type, String name,
                               long cost, String desc) {
        PlanetBuildingModuleEntity entity = new PlanetBuildingModuleEntity(
                tl, ModuleCategory.CHEST, type, name, (int) Math.min(cost, Integer.MAX_VALUE), 1, desc);
        entity.setGameType("TRAVELLER");
        repo.save(entity);
        return 1;
    }

    // === Building Room Type Seeding (TL0–TL12) ===
    // Seeds all room generic types used by BuildingInteriorGenerator for every tech level

    /**
     * Saves a generic building room module (not game-specific).
     */
    private int saveRoom(PlanetBuildingModuleRepository repo, TechLevel tl, String type, String name) {
        return save(repo, tl, ModuleCategory.DUNGEON_ROOM, type, name, 0, 1,
                "Building room: " + name);
    }

    private int seedBuildingRoomTypes(PlanetBuildingModuleRepository repo) {
        int c = 0;
        c += seedBuildingRoomsTL0(repo);
        c += seedBuildingRoomsTL1(repo);
        c += seedBuildingRoomsTL2(repo);
        c += seedBuildingRoomsTL3(repo);
        c += seedBuildingRoomsTL4(repo);
        c += seedBuildingRoomsTL5(repo);
        c += seedBuildingRoomsTL6(repo);
        c += seedBuildingRoomsTL7(repo);
        c += seedBuildingRoomsTL8(repo);
        c += seedBuildingRoomsTL9(repo);
        c += seedBuildingRoomsTL10(repo);
        c += seedBuildingRoomsTL11(repo);
        c += seedBuildingRoomsTL12(repo);
        return c;
    }

    // --- TL0: Prehistoric building rooms ---
    private int seedBuildingRoomsTL0(PlanetBuildingModuleRepository repo) {
        int c = 0;
        c += saveRoom(repo, TechLevel.TL0, "PORCH", "Cave Opening");
        c += saveRoom(repo, TechLevel.TL0, "LIVING_QUARTERS", "Cave Living Area");
        c += saveRoom(repo, TechLevel.TL0, "KITCHEN", "Fire Pit Cooking Area");
        c += saveRoom(repo, TechLevel.TL0, "BEDROOM", "Fur Sleeping Nook");
        c += saveRoom(repo, TechLevel.TL0, "STORAGE_CLOSET", "Rock Storage Crevice");
        c += saveRoom(repo, TechLevel.TL0, "GRAND_ENTRANCE", "Cave Entrance");
        c += saveRoom(repo, TechLevel.TL0, "COUNCIL_CHAMBER", "Elder Gathering Cave");
        c += saveRoom(repo, TechLevel.TL0, "RECORDS_OFFICE", "Pictograph Wall");
        c += saveRoom(repo, TechLevel.TL0, "MAYORS_OFFICE", "Chief's Cave");
        c += saveRoom(repo, TechLevel.TL0, "VAULT", "Hidden Rock Cache");
        c += saveRoom(repo, TechLevel.TL0, "MARKET_ENTRANCE", "Trading Grounds");
        c += saveRoom(repo, TechLevel.TL0, "FOOD_STALL", "Food Sharing Area");
        c += saveRoom(repo, TechLevel.TL0, "GOODS_STALL", "Barter Grounds");
        c += saveRoom(repo, TechLevel.TL0, "AUCTION_HOUSE", "Trophy Display");
        c += saveRoom(repo, TechLevel.TL0, "LOADING_DOCK", "River Landing");
        c += saveRoom(repo, TechLevel.TL0, "MAIN_STORAGE", "Supply Cave");
        c += saveRoom(repo, TechLevel.TL0, "OFFICE", "Shaman's Den");
        c += saveRoom(repo, TechLevel.TL0, "SECURE_STORAGE", "Hidden Cave Cache");
        c += saveRoom(repo, TechLevel.TL0, "WAITING_AREA", "Resting Spot");
        c += saveRoom(repo, TechLevel.TL0, "TICKET_BOOTH", "Guide Post");
        c += saveRoom(repo, TechLevel.TL0, "MAIN_ENTRANCE", "Trail Entrance");
        c += saveRoom(repo, TechLevel.TL0, "WAITING_HALL", "Gathering Area");
        c += saveRoom(repo, TechLevel.TL0, "TICKET_OFFICE", "Guide Station");
        c += saveRoom(repo, TechLevel.TL0, "PLATFORM", "Riverbank Path");
        c += saveRoom(repo, TechLevel.TL0, "CONTROL_ROOM", "Lookout Rock");
        c += saveRoom(repo, TechLevel.TL0, "STORAGE_ROOM", "Tool Cache");
        c += saveRoom(repo, TechLevel.TL0, "DOCK_ENTRANCE", "River Approach");
        c += saveRoom(repo, TechLevel.TL0, "CARGO_WAREHOUSE", "Raft Storage");
        c += saveRoom(repo, TechLevel.TL0, "PASSENGER_TERMINAL", "Traveler Camp");
        c += saveRoom(repo, TechLevel.TL0, "DOCK_OFFICE", "River Guide Hut");
        c += saveRoom(repo, TechLevel.TL0, "CAPTAINS_QUARTERS", "Raft Leader's Cave");
        c += saveRoom(repo, TechLevel.TL0, "WATERFRONT", "Riverbank");
        c += saveRoom(repo, TechLevel.TL0, "MAIN_TERMINAL", "Trail Junction");
        c += saveRoom(repo, TechLevel.TL0, "CHECKIN_HALL", "Traveler Assembly");
        c += saveRoom(repo, TechLevel.TL0, "SECURITY_CHECK", "Guard Post");
        c += saveRoom(repo, TechLevel.TL0, "GATE_AREA", "Trail Gate");
        c += saveRoom(repo, TechLevel.TL0, "CONTROL_TOWER", "High Rock Lookout");
        c += saveRoom(repo, TechLevel.TL0, "HANGAR", "Raft Shelter");
        c += saveRoom(repo, TechLevel.TL0, "MAINTENANCE_BAY", "Tool Repair Area");
        c += saveRoom(repo, TechLevel.TL0, "CUSTOMS_SECURITY", "Border Guard Post");
        c += saveRoom(repo, TechLevel.TL0, "OBSERVATION_DECK", "High Lookout");
        c += saveRoom(repo, TechLevel.TL0, "DOCKING_BAY", "Raft Dock");
        c += saveRoom(repo, TechLevel.TL0, "COMMAND_CENTER", "Chief's Lookout");
        c += saveRoom(repo, TechLevel.TL0, "FUEL_STATION", "Firewood Pile");
        c += saveRoom(repo, TechLevel.TL0, "INN_ENTRANCE", "Shelter Entrance");
        c += saveRoom(repo, TechLevel.TL0, "COMMON_ROOM", "Communal Cave");
        c += saveRoom(repo, TechLevel.TL0, "CORRIDOR", "Cave Passage");
        c += saveRoom(repo, TechLevel.TL0, "GUEST_ROOM", "Guest Nook");
        c += saveRoom(repo, TechLevel.TL0, "TEMPLE_ENTRANCE", "Sacred Cave");
        c += saveRoom(repo, TechLevel.TL0, "MAIN_HALL", "Ritual Chamber");
        c += saveRoom(repo, TechLevel.TL0, "CHAPEL", "Spirit Shrine");
        c += saveRoom(repo, TechLevel.TL0, "CLERGY_QUARTERS", "Shaman's Cave");
        c += saveRoom(repo, TechLevel.TL0, "STATION_ENTRANCE", "Guard Post");
        c += saveRoom(repo, TechLevel.TL0, "MAIN_DESK", "Guard Leader Post");
        c += saveRoom(repo, TechLevel.TL0, "EVIDENCE_ROOM", "Captured Goods Cave");
        c += saveRoom(repo, TechLevel.TL0, "HOLDING_CELLS", "Pit Prison");
        c += saveRoom(repo, TechLevel.TL0, "PRODUCTION_FLOOR", "Crafting Area");
        c += saveRoom(repo, TechLevel.TL0, "RAW_MATERIAL_STORAGE", "Raw Material Pile");
        c += saveRoom(repo, TechLevel.TL0, "BREAK_ROOM", "Rest Cave");
        return c;
    }

    // --- TL1: Ancient building rooms ---
    private int seedBuildingRoomsTL1(PlanetBuildingModuleRepository repo) {
        int c = 0;
        c += saveRoom(repo, TechLevel.TL1, "PORCH", "Stone Porch");
        c += saveRoom(repo, TechLevel.TL1, "LIVING_QUARTERS", "Mud-Brick Living Room");
        c += saveRoom(repo, TechLevel.TL1, "KITCHEN", "Clay Oven Kitchen");
        c += saveRoom(repo, TechLevel.TL1, "BEDROOM", "Mat Bedroom");
        c += saveRoom(repo, TechLevel.TL1, "STORAGE_CLOSET", "Storage Alcove");
        c += saveRoom(repo, TechLevel.TL1, "GRAND_ENTRANCE", "Temple Entrance");
        c += saveRoom(repo, TechLevel.TL1, "COUNCIL_CHAMBER", "Council of Elders");
        c += saveRoom(repo, TechLevel.TL1, "RECORDS_OFFICE", "Scribe's Room");
        c += saveRoom(repo, TechLevel.TL1, "MAYORS_OFFICE", "Governor's Chamber");
        c += saveRoom(repo, TechLevel.TL1, "VAULT", "Temple Treasury");
        c += saveRoom(repo, TechLevel.TL1, "MARKET_ENTRANCE", "Market Gate");
        c += saveRoom(repo, TechLevel.TL1, "FOOD_STALL", "Bread and Wine Stall");
        c += saveRoom(repo, TechLevel.TL1, "GOODS_STALL", "Pottery Stall");
        c += saveRoom(repo, TechLevel.TL1, "AUCTION_HOUSE", "Merchant's Auction");
        c += saveRoom(repo, TechLevel.TL1, "LOADING_DOCK", "Cart Loading Area");
        c += saveRoom(repo, TechLevel.TL1, "MAIN_STORAGE", "Granary");
        c += saveRoom(repo, TechLevel.TL1, "OFFICE", "Scribe's Office");
        c += saveRoom(repo, TechLevel.TL1, "SECURE_STORAGE", "Locked Strongroom");
        c += saveRoom(repo, TechLevel.TL1, "WAITING_AREA", "Sheltered Rest");
        c += saveRoom(repo, TechLevel.TL1, "TICKET_BOOTH", "Toll Booth");
        c += saveRoom(repo, TechLevel.TL1, "MAIN_ENTRANCE", "Stone Archway");
        c += saveRoom(repo, TechLevel.TL1, "WAITING_HALL", "Colonnade");
        c += saveRoom(repo, TechLevel.TL1, "TICKET_OFFICE", "Toll Office");
        c += saveRoom(repo, TechLevel.TL1, "PLATFORM", "Stone Platform");
        c += saveRoom(repo, TechLevel.TL1, "CONTROL_ROOM", "Signal Tower");
        c += saveRoom(repo, TechLevel.TL1, "STORAGE_ROOM", "Supply Room");
        c += saveRoom(repo, TechLevel.TL1, "DOCK_ENTRANCE", "Harbor Gate");
        c += saveRoom(repo, TechLevel.TL1, "CARGO_WAREHOUSE", "Cargo Shed");
        c += saveRoom(repo, TechLevel.TL1, "PASSENGER_TERMINAL", "Traveler's Hall");
        c += saveRoom(repo, TechLevel.TL1, "DOCK_OFFICE", "Harbor Master's Office");
        c += saveRoom(repo, TechLevel.TL1, "CAPTAINS_QUARTERS", "Captain's Cabin");
        c += saveRoom(repo, TechLevel.TL1, "WATERFRONT", "Harbor Wharf");
        c += saveRoom(repo, TechLevel.TL1, "MAIN_TERMINAL", "Caravanserai");
        c += saveRoom(repo, TechLevel.TL1, "CHECKIN_HALL", "Registration Hall");
        c += saveRoom(repo, TechLevel.TL1, "SECURITY_CHECK", "Guard Checkpoint");
        c += saveRoom(repo, TechLevel.TL1, "GATE_AREA", "City Gate");
        c += saveRoom(repo, TechLevel.TL1, "CONTROL_TOWER", "Watch Tower");
        c += saveRoom(repo, TechLevel.TL1, "HANGAR", "Ship Shed");
        c += saveRoom(repo, TechLevel.TL1, "MAINTENANCE_BAY", "Repair Yard");
        c += saveRoom(repo, TechLevel.TL1, "CUSTOMS_SECURITY", "Customs House");
        c += saveRoom(repo, TechLevel.TL1, "OBSERVATION_DECK", "Tower View");
        c += saveRoom(repo, TechLevel.TL1, "DOCKING_BAY", "Ship Dock");
        c += saveRoom(repo, TechLevel.TL1, "COMMAND_CENTER", "Harbor Command");
        c += saveRoom(repo, TechLevel.TL1, "FUEL_STATION", "Tallow Store");
        c += saveRoom(repo, TechLevel.TL1, "INN_ENTRANCE", "Tavern Entrance");
        c += saveRoom(repo, TechLevel.TL1, "COMMON_ROOM", "Tavern Hall");
        c += saveRoom(repo, TechLevel.TL1, "CORRIDOR", "Stone Corridor");
        c += saveRoom(repo, TechLevel.TL1, "GUEST_ROOM", "Inn Room");
        c += saveRoom(repo, TechLevel.TL1, "TEMPLE_ENTRANCE", "Temple Steps");
        c += saveRoom(repo, TechLevel.TL1, "MAIN_HALL", "Temple Hall");
        c += saveRoom(repo, TechLevel.TL1, "CHAPEL", "Inner Sanctum");
        c += saveRoom(repo, TechLevel.TL1, "CLERGY_QUARTERS", "Priest's Quarters");
        c += saveRoom(repo, TechLevel.TL1, "STATION_ENTRANCE", "Guard House");
        c += saveRoom(repo, TechLevel.TL1, "MAIN_DESK", "Guard Desk");
        c += saveRoom(repo, TechLevel.TL1, "EVIDENCE_ROOM", "Confiscated Goods");
        c += saveRoom(repo, TechLevel.TL1, "HOLDING_CELLS", "Stone Cells");
        c += saveRoom(repo, TechLevel.TL1, "PRODUCTION_FLOOR", "Workshop Floor");
        c += saveRoom(repo, TechLevel.TL1, "RAW_MATERIAL_STORAGE", "Material Store");
        c += saveRoom(repo, TechLevel.TL1, "BREAK_ROOM", "Worker's Rest");
        return c;
    }

    // --- TL2: Medieval building rooms ---
    private int seedBuildingRoomsTL2(PlanetBuildingModuleRepository repo) {
        int c = 0;
        c += saveRoom(repo, TechLevel.TL2, "PORCH", "Stone Porch");
        c += saveRoom(repo, TechLevel.TL2, "LIVING_QUARTERS", "Great Hall");
        c += saveRoom(repo, TechLevel.TL2, "KITCHEN", "Hearth Kitchen");
        c += saveRoom(repo, TechLevel.TL2, "BEDROOM", "Chamber");
        c += saveRoom(repo, TechLevel.TL2, "STORAGE_CLOSET", "Storage Room");
        c += saveRoom(repo, TechLevel.TL2, "GRAND_ENTRANCE", "Castle Entrance");
        c += saveRoom(repo, TechLevel.TL2, "COUNCIL_CHAMBER", "Council Chamber");
        c += saveRoom(repo, TechLevel.TL2, "RECORDS_OFFICE", "Scriptorium");
        c += saveRoom(repo, TechLevel.TL2, "MAYORS_OFFICE", "Lord's Chamber");
        c += saveRoom(repo, TechLevel.TL2, "VAULT", "Castle Vault");
        c += saveRoom(repo, TechLevel.TL2, "MARKET_ENTRANCE", "Market Square");
        c += saveRoom(repo, TechLevel.TL2, "FOOD_STALL", "Meat and Bread Stall");
        c += saveRoom(repo, TechLevel.TL2, "GOODS_STALL", "Goods Stall");
        c += saveRoom(repo, TechLevel.TL2, "AUCTION_HOUSE", "Auction House");
        c += saveRoom(repo, TechLevel.TL2, "LOADING_DOCK", "Loading Dock");
        c += saveRoom(repo, TechLevel.TL2, "MAIN_STORAGE", "Warehouse Storage");
        c += saveRoom(repo, TechLevel.TL2, "OFFICE", "Manager's Office");
        c += saveRoom(repo, TechLevel.TL2, "SECURE_STORAGE", "Armory Storage");
        c += saveRoom(repo, TechLevel.TL2, "WAITING_AREA", "Waiting Porch");
        c += saveRoom(repo, TechLevel.TL2, "TICKET_BOOTH", "Toll Booth");
        c += saveRoom(repo, TechLevel.TL2, "MAIN_ENTRANCE", "Station Entrance");
        c += saveRoom(repo, TechLevel.TL2, "WAITING_HALL", "Waiting Hall");
        c += saveRoom(repo, TechLevel.TL2, "TICKET_OFFICE", "Ticket Office");
        c += saveRoom(repo, TechLevel.TL2, "PLATFORM", "Stone Platform");
        c += saveRoom(repo, TechLevel.TL2, "CONTROL_ROOM", "Signal Room");
        c += saveRoom(repo, TechLevel.TL2, "STORAGE_ROOM", "Supply Room");
        c += saveRoom(repo, TechLevel.TL2, "DOCK_ENTRANCE", "Dock Gate");
        c += saveRoom(repo, TechLevel.TL2, "CARGO_WAREHOUSE", "Cargo Warehouse");
        c += saveRoom(repo, TechLevel.TL2, "PASSENGER_TERMINAL", "Passenger Hall");
        c += saveRoom(repo, TechLevel.TL2, "DOCK_OFFICE", "Dock Master's Office");
        c += saveRoom(repo, TechLevel.TL2, "CAPTAINS_QUARTERS", "Captain's Quarters");
        c += saveRoom(repo, TechLevel.TL2, "WATERFRONT", "Dockside");
        c += saveRoom(repo, TechLevel.TL2, "MAIN_TERMINAL", "Inn Terminal");
        c += saveRoom(repo, TechLevel.TL2, "CHECKIN_HALL", "Check-in Hall");
        c += saveRoom(repo, TechLevel.TL2, "SECURITY_CHECK", "Guard Checkpoint");
        c += saveRoom(repo, TechLevel.TL2, "GATE_AREA", "Gate Area");
        c += saveRoom(repo, TechLevel.TL2, "CONTROL_TOWER", "Watch Tower");
        c += saveRoom(repo, TechLevel.TL2, "HANGAR", "Ship Hangar");
        c += saveRoom(repo, TechLevel.TL2, "MAINTENANCE_BAY", "Repair Bay");
        c += saveRoom(repo, TechLevel.TL2, "CUSTOMS_SECURITY", "Customs House");
        c += saveRoom(repo, TechLevel.TL2, "OBSERVATION_DECK", "Tower View");
        c += saveRoom(repo, TechLevel.TL2, "DOCKING_BAY", "Ship Dock");
        c += saveRoom(repo, TechLevel.TL2, "COMMAND_CENTER", "Dock Command");
        c += saveRoom(repo, TechLevel.TL2, "FUEL_STATION", "Tallow Store");
        c += saveRoom(repo, TechLevel.TL2, "INN_ENTRANCE", "Inn Entrance");
        c += saveRoom(repo, TechLevel.TL2, "COMMON_ROOM", "Common Room");
        c += saveRoom(repo, TechLevel.TL2, "CORRIDOR", "Stone Corridor");
        c += saveRoom(repo, TechLevel.TL2, "GUEST_ROOM", "Guest Room");
        c += saveRoom(repo, TechLevel.TL2, "TEMPLE_ENTRANCE", "Church Entrance");
        c += saveRoom(repo, TechLevel.TL2, "MAIN_HALL", "Nave");
        c += saveRoom(repo, TechLevel.TL2, "CHAPEL", "Side Chapel");
        c += saveRoom(repo, TechLevel.TL2, "CLERGY_QUARTERS", "Clergy Quarters");
        c += saveRoom(repo, TechLevel.TL2, "STATION_ENTRANCE", "Guard House");
        c += saveRoom(repo, TechLevel.TL2, "MAIN_DESK", "Guard Desk");
        c += saveRoom(repo, TechLevel.TL2, "EVIDENCE_ROOM", "Evidence Room");
        c += saveRoom(repo, TechLevel.TL2, "HOLDING_CELLS", "Dungeon Cells");
        c += saveRoom(repo, TechLevel.TL2, "PRODUCTION_FLOOR", "Workshop Floor");
        c += saveRoom(repo, TechLevel.TL2, "RAW_MATERIAL_STORAGE", "Material Store");
        c += saveRoom(repo, TechLevel.TL2, "BREAK_ROOM", "Break Room");
        return c;
    }

    // --- TL3: Renaissance building rooms ---
    private int seedBuildingRoomsTL3(PlanetBuildingModuleRepository repo) {
        int c = 0;
        c += saveRoom(repo, TechLevel.TL3, "PORCH", "Ornate Porch");
        c += saveRoom(repo, TechLevel.TL3, "LIVING_QUARTERS", "Parlor");
        c += saveRoom(repo, TechLevel.TL3, "KITCHEN", "Tiled Kitchen");
        c += saveRoom(repo, TechLevel.TL3, "BEDROOM", "Canopy Bedroom");
        c += saveRoom(repo, TechLevel.TL3, "STORAGE_CLOSET", "Cabinet Storage");
        c += saveRoom(repo, TechLevel.TL3, "GRAND_ENTRANCE", "Grand Foyer");
        c += saveRoom(repo, TechLevel.TL3, "COUNCIL_CHAMBER", "Town Council");
        c += saveRoom(repo, TechLevel.TL3, "RECORDS_OFFICE", "Records Office");
        c += saveRoom(repo, TechLevel.TL3, "MAYORS_OFFICE", "Mayor's Office");
        c += saveRoom(repo, TechLevel.TL3, "VAULT", "Bank Vault");
        c += saveRoom(repo, TechLevel.TL3, "MARKET_ENTRANCE", "Market Plaza");
        c += saveRoom(repo, TechLevel.TL3, "FOOD_STALL", "Spice and Food Stall");
        c += saveRoom(repo, TechLevel.TL3, "GOODS_STALL", "Exotic Goods Stall");
        c += saveRoom(repo, TechLevel.TL3, "AUCTION_HOUSE", "Auction House");
        c += saveRoom(repo, TechLevel.TL3, "LOADING_DOCK", "Warehouse Dock");
        c += saveRoom(repo, TechLevel.TL3, "MAIN_STORAGE", "Warehouse Storage");
        c += saveRoom(repo, TechLevel.TL3, "OFFICE", "Office");
        c += saveRoom(repo, TechLevel.TL3, "SECURE_STORAGE", "Secure Storage");
        c += saveRoom(repo, TechLevel.TL3, "WAITING_AREA", "Waiting Lounge");
        c += saveRoom(repo, TechLevel.TL3, "TICKET_BOOTH", "Ticket Booth");
        c += saveRoom(repo, TechLevel.TL3, "MAIN_ENTRANCE", "Station Entrance");
        c += saveRoom(repo, TechLevel.TL3, "WAITING_HALL", "Waiting Hall");
        c += saveRoom(repo, TechLevel.TL3, "TICKET_OFFICE", "Ticket Office");
        c += saveRoom(repo, TechLevel.TL3, "PLATFORM", "Platform");
        c += saveRoom(repo, TechLevel.TL3, "CONTROL_ROOM", "Signal Room");
        c += saveRoom(repo, TechLevel.TL3, "STORAGE_ROOM", "Storage Room");
        c += saveRoom(repo, TechLevel.TL3, "DOCK_ENTRANCE", "Harbor Entrance");
        c += saveRoom(repo, TechLevel.TL3, "CARGO_WAREHOUSE", "Cargo Warehouse");
        c += saveRoom(repo, TechLevel.TL3, "PASSENGER_TERMINAL", "Passenger Terminal");
        c += saveRoom(repo, TechLevel.TL3, "DOCK_OFFICE", "Dock Office");
        c += saveRoom(repo, TechLevel.TL3, "CAPTAINS_QUARTERS", "Captain's Quarters");
        c += saveRoom(repo, TechLevel.TL3, "WATERFRONT", "Harbor Waterfront");
        c += saveRoom(repo, TechLevel.TL3, "MAIN_TERMINAL", "Travel Terminal");
        c += saveRoom(repo, TechLevel.TL3, "CHECKIN_HALL", "Check-in Hall");
        c += saveRoom(repo, TechLevel.TL3, "SECURITY_CHECK", "Security Check");
        c += saveRoom(repo, TechLevel.TL3, "GATE_AREA", "Gate Area");
        c += saveRoom(repo, TechLevel.TL3, "CONTROL_TOWER", "Control Tower");
        c += saveRoom(repo, TechLevel.TL3, "HANGAR", "Ship Hangar");
        c += saveRoom(repo, TechLevel.TL3, "MAINTENANCE_BAY", "Maintenance Bay");
        c += saveRoom(repo, TechLevel.TL3, "CUSTOMS_SECURITY", "Customs House");
        c += saveRoom(repo, TechLevel.TL3, "OBSERVATION_DECK", "Observation Deck");
        c += saveRoom(repo, TechLevel.TL3, "DOCKING_BAY", "Ship Dock");
        c += saveRoom(repo, TechLevel.TL3, "COMMAND_CENTER", "Harbor Command");
        c += saveRoom(repo, TechLevel.TL3, "FUEL_STATION", "Coal Store");
        c += saveRoom(repo, TechLevel.TL3, "INN_ENTRANCE", "Inn Entrance");
        c += saveRoom(repo, TechLevel.TL3, "COMMON_ROOM", "Common Room");
        c += saveRoom(repo, TechLevel.TL3, "CORRIDOR", "Corridor");
        c += saveRoom(repo, TechLevel.TL3, "GUEST_ROOM", "Guest Room");
        c += saveRoom(repo, TechLevel.TL3, "TEMPLE_ENTRANCE", "Cathedral Entrance");
        c += saveRoom(repo, TechLevel.TL3, "MAIN_HALL", "Cathedral Nave");
        c += saveRoom(repo, TechLevel.TL3, "CHAPEL", "Chapel");
        c += saveRoom(repo, TechLevel.TL3, "CLERGY_QUARTERS", "Clergy Quarters");
        c += saveRoom(repo, TechLevel.TL3, "STATION_ENTRANCE", "Police Station");
        c += saveRoom(repo, TechLevel.TL3, "MAIN_DESK", "Main Desk");
        c += saveRoom(repo, TechLevel.TL3, "EVIDENCE_ROOM", "Evidence Room");
        c += saveRoom(repo, TechLevel.TL3, "HOLDING_CELLS", "Holding Cells");
        c += saveRoom(repo, TechLevel.TL3, "PRODUCTION_FLOOR", "Factory Floor");
        c += saveRoom(repo, TechLevel.TL3, "RAW_MATERIAL_STORAGE", "Material Storage");
        c += saveRoom(repo, TechLevel.TL3, "BREAK_ROOM", "Break Room");
        return c;
    }

    // --- TL4: Industrial building rooms ---
    private int seedBuildingRoomsTL4(PlanetBuildingModuleRepository repo) {
        int c = 0;
        c += saveRoom(repo, TechLevel.TL4, "PORCH", "Front Porch");
        c += saveRoom(repo, TechLevel.TL4, "LIVING_QUARTERS", "Living Room");
        c += saveRoom(repo, TechLevel.TL4, "KITCHEN", "Kitchen");
        c += saveRoom(repo, TechLevel.TL4, "BEDROOM", "Bedroom");
        c += saveRoom(repo, TechLevel.TL4, "STORAGE_CLOSET", "Storage Closet");
        c += saveRoom(repo, TechLevel.TL4, "GRAND_ENTRANCE", "Grand Entrance");
        c += saveRoom(repo, TechLevel.TL4, "COUNCIL_CHAMBER", "Council Chamber");
        c += saveRoom(repo, TechLevel.TL4, "RECORDS_OFFICE", "Records Office");
        c += saveRoom(repo, TechLevel.TL4, "MAYORS_OFFICE", "Mayor's Office");
        c += saveRoom(repo, TechLevel.TL4, "VAULT", "Vault");
        c += saveRoom(repo, TechLevel.TL4, "MARKET_ENTRANCE", "Market Entrance");
        c += saveRoom(repo, TechLevel.TL4, "FOOD_STALL", "Food Stall");
        c += saveRoom(repo, TechLevel.TL4, "GOODS_STALL", "Goods Stall");
        c += saveRoom(repo, TechLevel.TL4, "AUCTION_HOUSE", "Auction House");
        c += saveRoom(repo, TechLevel.TL4, "LOADING_DOCK", "Loading Dock");
        c += saveRoom(repo, TechLevel.TL4, "MAIN_STORAGE", "Main Storage");
        c += saveRoom(repo, TechLevel.TL4, "OFFICE", "Office");
        c += saveRoom(repo, TechLevel.TL4, "SECURE_STORAGE", "Secure Storage");
        c += saveRoom(repo, TechLevel.TL4, "WAITING_AREA", "Waiting Area");
        c += saveRoom(repo, TechLevel.TL4, "TICKET_BOOTH", "Ticket Booth");
        c += saveRoom(repo, TechLevel.TL4, "MAIN_ENTRANCE", "Main Entrance");
        c += saveRoom(repo, TechLevel.TL4, "WAITING_HALL", "Waiting Hall");
        c += saveRoom(repo, TechLevel.TL4, "TICKET_OFFICE", "Ticket Office");
        c += saveRoom(repo, TechLevel.TL4, "PLATFORM", "Rail Platform");
        c += saveRoom(repo, TechLevel.TL4, "CONTROL_ROOM", "Control Room");
        c += saveRoom(repo, TechLevel.TL4, "STORAGE_ROOM", "Storage Room");
        c += saveRoom(repo, TechLevel.TL4, "DOCK_ENTRANCE", "Dock Entrance");
        c += saveRoom(repo, TechLevel.TL4, "CARGO_WAREHOUSE", "Cargo Warehouse");
        c += saveRoom(repo, TechLevel.TL4, "PASSENGER_TERMINAL", "Passenger Terminal");
        c += saveRoom(repo, TechLevel.TL4, "DOCK_OFFICE", "Dock Office");
        c += saveRoom(repo, TechLevel.TL4, "CAPTAINS_QUARTERS", "Captain's Quarters");
        c += saveRoom(repo, TechLevel.TL4, "WATERFRONT", "Waterfront");
        c += saveRoom(repo, TechLevel.TL4, "MAIN_TERMINAL", "Main Terminal");
        c += saveRoom(repo, TechLevel.TL4, "CHECKIN_HALL", "Check-in Hall");
        c += saveRoom(repo, TechLevel.TL4, "SECURITY_CHECK", "Security Check");
        c += saveRoom(repo, TechLevel.TL4, "GATE_AREA", "Gate Area");
        c += saveRoom(repo, TechLevel.TL4, "CONTROL_TOWER", "Control Tower");
        c += saveRoom(repo, TechLevel.TL4, "HANGAR", "Hangar");
        c += saveRoom(repo, TechLevel.TL4, "MAINTENANCE_BAY", "Maintenance Bay");
        c += saveRoom(repo, TechLevel.TL4, "CUSTOMS_SECURITY", "Customs & Security");
        c += saveRoom(repo, TechLevel.TL4, "OBSERVATION_DECK", "Observation Deck");
        c += saveRoom(repo, TechLevel.TL4, "DOCKING_BAY", "Docking Bay");
        c += saveRoom(repo, TechLevel.TL4, "COMMAND_CENTER", "Command Center");
        c += saveRoom(repo, TechLevel.TL4, "FUEL_STATION", "Fuel Station");
        c += saveRoom(repo, TechLevel.TL4, "INN_ENTRANCE", "Inn Entrance");
        c += saveRoom(repo, TechLevel.TL4, "COMMON_ROOM", "Common Room");
        c += saveRoom(repo, TechLevel.TL4, "CORRIDOR", "Corridor");
        c += saveRoom(repo, TechLevel.TL4, "GUEST_ROOM", "Guest Room");
        c += saveRoom(repo, TechLevel.TL4, "TEMPLE_ENTRANCE", "Church Entrance");
        c += saveRoom(repo, TechLevel.TL4, "MAIN_HALL", "Main Hall");
        c += saveRoom(repo, TechLevel.TL4, "CHAPEL", "Chapel");
        c += saveRoom(repo, TechLevel.TL4, "CLERGY_QUARTERS", "Clergy Quarters");
        c += saveRoom(repo, TechLevel.TL4, "STATION_ENTRANCE", "Station Entrance");
        c += saveRoom(repo, TechLevel.TL4, "MAIN_DESK", "Main Desk");
        c += saveRoom(repo, TechLevel.TL4, "EVIDENCE_ROOM", "Evidence Room");
        c += saveRoom(repo, TechLevel.TL4, "HOLDING_CELLS", "Holding Cells");
        c += saveRoom(repo, TechLevel.TL4, "PRODUCTION_FLOOR", "Production Floor");
        c += saveRoom(repo, TechLevel.TL4, "RAW_MATERIAL_STORAGE", "Raw Material Storage");
        c += saveRoom(repo, TechLevel.TL4, "BREAK_ROOM", "Break Room");
        return c;
    }

    // --- TL5: Modern building rooms ---
    private int seedBuildingRoomsTL5(PlanetBuildingModuleRepository repo) {
        int c = 0;
        c += saveRoom(repo, TechLevel.TL5, "PORCH", "Front Porch");
        c += saveRoom(repo, TechLevel.TL5, "LIVING_QUARTERS", "Living Room");
        c += saveRoom(repo, TechLevel.TL5, "KITCHEN", "Modern Kitchen");
        c += saveRoom(repo, TechLevel.TL5, "BEDROOM", "Bedroom");
        c += saveRoom(repo, TechLevel.TL5, "STORAGE_CLOSET", "Storage Closet");
        c += saveRoom(repo, TechLevel.TL5, "GRAND_ENTRANCE", "Lobby");
        c += saveRoom(repo, TechLevel.TL5, "COUNCIL_CHAMBER", "City Council Room");
        c += saveRoom(repo, TechLevel.TL5, "RECORDS_OFFICE", "Records Office");
        c += saveRoom(repo, TechLevel.TL5, "MAYORS_OFFICE", "Mayor's Office");
        c += saveRoom(repo, TechLevel.TL5, "VAULT", "Bank Vault");
        c += saveRoom(repo, TechLevel.TL5, "MARKET_ENTRANCE", "Shopping Mall Entrance");
        c += saveRoom(repo, TechLevel.TL5, "FOOD_STALL", "Food Court");
        c += saveRoom(repo, TechLevel.TL5, "GOODS_STALL", "Retail Store");
        c += saveRoom(repo, TechLevel.TL5, "AUCTION_HOUSE", "Auction House");
        c += saveRoom(repo, TechLevel.TL5, "LOADING_DOCK", "Loading Dock");
        c += saveRoom(repo, TechLevel.TL5, "MAIN_STORAGE", "Warehouse Storage");
        c += saveRoom(repo, TechLevel.TL5, "OFFICE", "Office");
        c += saveRoom(repo, TechLevel.TL5, "SECURE_STORAGE", "Secure Storage");
        c += saveRoom(repo, TechLevel.TL5, "WAITING_AREA", "Waiting Area");
        c += saveRoom(repo, TechLevel.TL5, "TICKET_BOOTH", "Ticket Booth");
        c += saveRoom(repo, TechLevel.TL5, "MAIN_ENTRANCE", "Main Entrance");
        c += saveRoom(repo, TechLevel.TL5, "WAITING_HALL", "Waiting Hall");
        c += saveRoom(repo, TechLevel.TL5, "TICKET_OFFICE", "Ticket Office");
        c += saveRoom(repo, TechLevel.TL5, "PLATFORM", "Bus Platform");
        c += saveRoom(repo, TechLevel.TL5, "CONTROL_ROOM", "Control Room");
        c += saveRoom(repo, TechLevel.TL5, "STORAGE_ROOM", "Storage Room");
        c += saveRoom(repo, TechLevel.TL5, "DOCK_ENTRANCE", "Port Entrance");
        c += saveRoom(repo, TechLevel.TL5, "CARGO_WAREHOUSE", "Cargo Warehouse");
        c += saveRoom(repo, TechLevel.TL5, "PASSENGER_TERMINAL", "Passenger Terminal");
        c += saveRoom(repo, TechLevel.TL5, "DOCK_OFFICE", "Port Office");
        c += saveRoom(repo, TechLevel.TL5, "CAPTAINS_QUARTERS", "Captain's Quarters");
        c += saveRoom(repo, TechLevel.TL5, "WATERFRONT", "Waterfront");
        c += saveRoom(repo, TechLevel.TL5, "MAIN_TERMINAL", "Airport Terminal");
        c += saveRoom(repo, TechLevel.TL5, "CHECKIN_HALL", "Check-in Hall");
        c += saveRoom(repo, TechLevel.TL5, "SECURITY_CHECK", "Security Checkpoint");
        c += saveRoom(repo, TechLevel.TL5, "GATE_AREA", "Boarding Gate");
        c += saveRoom(repo, TechLevel.TL5, "CONTROL_TOWER", "Control Tower");
        c += saveRoom(repo, TechLevel.TL5, "HANGAR", "Aircraft Hangar");
        c += saveRoom(repo, TechLevel.TL5, "MAINTENANCE_BAY", "Maintenance Bay");
        c += saveRoom(repo, TechLevel.TL5, "CUSTOMS_SECURITY", "Customs & Security");
        c += saveRoom(repo, TechLevel.TL5, "OBSERVATION_DECK", "Observation Deck");
        c += saveRoom(repo, TechLevel.TL5, "DOCKING_BAY", "Docking Bay");
        c += saveRoom(repo, TechLevel.TL5, "COMMAND_CENTER", "Command Center");
        c += saveRoom(repo, TechLevel.TL5, "FUEL_STATION", "Fuel Station");
        c += saveRoom(repo, TechLevel.TL5, "INN_ENTRANCE", "Hotel Lobby");
        c += saveRoom(repo, TechLevel.TL5, "COMMON_ROOM", "Lounge");
        c += saveRoom(repo, TechLevel.TL5, "CORRIDOR", "Hallway");
        c += saveRoom(repo, TechLevel.TL5, "GUEST_ROOM", "Hotel Room");
        c += saveRoom(repo, TechLevel.TL5, "TEMPLE_ENTRANCE", "Church Entrance");
        c += saveRoom(repo, TechLevel.TL5, "MAIN_HALL", "Main Hall");
        c += saveRoom(repo, TechLevel.TL5, "CHAPEL", "Chapel");
        c += saveRoom(repo, TechLevel.TL5, "CLERGY_QUARTERS", "Clergy Quarters");
        c += saveRoom(repo, TechLevel.TL5, "STATION_ENTRANCE", "Police Station");
        c += saveRoom(repo, TechLevel.TL5, "MAIN_DESK", "Front Desk");
        c += saveRoom(repo, TechLevel.TL5, "EVIDENCE_ROOM", "Evidence Room");
        c += saveRoom(repo, TechLevel.TL5, "HOLDING_CELLS", "Holding Cells");
        c += saveRoom(repo, TechLevel.TL5, "PRODUCTION_FLOOR", "Assembly Line");
        c += saveRoom(repo, TechLevel.TL5, "RAW_MATERIAL_STORAGE", "Material Storage");
        c += saveRoom(repo, TechLevel.TL5, "BREAK_ROOM", "Break Room");
        return c;
    }

    // --- TL6: Information Age building rooms ---
    private int seedBuildingRoomsTL6(PlanetBuildingModuleRepository repo) {
        int c = 0;
        c += saveRoom(repo, TechLevel.TL6, "PORCH", "Pressurized Entryway");
        c += saveRoom(repo, TechLevel.TL6, "LIVING_QUARTERS", "Habitation Module");
        c += saveRoom(repo, TechLevel.TL6, "KITCHEN", "Galley");
        c += saveRoom(repo, TechLevel.TL6, "BEDROOM", "Sleeping Quarters");
        c += saveRoom(repo, TechLevel.TL6, "STORAGE_CLOSET", "Storage Locker");
        c += saveRoom(repo, TechLevel.TL6, "GRAND_ENTRANCE", "Main Airlock");
        c += saveRoom(repo, TechLevel.TL6, "COUNCIL_CHAMBER", "Mission Control");
        c += saveRoom(repo, TechLevel.TL6, "RECORDS_OFFICE", "Data Archive");
        c += saveRoom(repo, TechLevel.TL6, "MAYORS_OFFICE", "Director's Office");
        c += saveRoom(repo, TechLevel.TL6, "VAULT", "Secure Data Vault");
        c += saveRoom(repo, TechLevel.TL6, "MARKET_ENTRANCE", "Commercial Hub");
        c += saveRoom(repo, TechLevel.TL6, "FOOD_STALL", "Nutrition Station");
        c += saveRoom(repo, TechLevel.TL6, "GOODS_STALL", "Supply Kiosk");
        c += saveRoom(repo, TechLevel.TL6, "AUCTION_HOUSE", "Trading Floor");
        c += saveRoom(repo, TechLevel.TL6, "LOADING_DOCK", "Cargo Dock");
        c += saveRoom(repo, TechLevel.TL6, "MAIN_STORAGE", "Cargo Bay");
        c += saveRoom(repo, TechLevel.TL6, "OFFICE", "Operations Office");
        c += saveRoom(repo, TechLevel.TL6, "SECURE_STORAGE", "Secure Locker");
        c += saveRoom(repo, TechLevel.TL6, "WAITING_AREA", "Waiting Lounge");
        c += saveRoom(repo, TechLevel.TL6, "TICKET_BOOTH", "Kiosk");
        c += saveRoom(repo, TechLevel.TL6, "MAIN_ENTRANCE", "Main Entrance");
        c += saveRoom(repo, TechLevel.TL6, "WAITING_HALL", "Departure Lounge");
        c += saveRoom(repo, TechLevel.TL6, "TICKET_OFFICE", "Ticket Office");
        c += saveRoom(repo, TechLevel.TL6, "PLATFORM", "Hover Platform");
        c += saveRoom(repo, TechLevel.TL6, "CONTROL_ROOM", "Control Room");
        c += saveRoom(repo, TechLevel.TL6, "STORAGE_ROOM", "Storage Room");
        c += saveRoom(repo, TechLevel.TL6, "DOCK_ENTRANCE", "Dock Entrance");
        c += saveRoom(repo, TechLevel.TL6, "CARGO_WAREHOUSE", "Cargo Warehouse");
        c += saveRoom(repo, TechLevel.TL6, "PASSENGER_TERMINAL", "Passenger Terminal");
        c += saveRoom(repo, TechLevel.TL6, "DOCK_OFFICE", "Dock Office");
        c += saveRoom(repo, TechLevel.TL6, "CAPTAINS_QUARTERS", "Captain's Quarters");
        c += saveRoom(repo, TechLevel.TL6, "WATERFRONT", "Waterfront");
        c += saveRoom(repo, TechLevel.TL6, "MAIN_TERMINAL", "Launch Terminal");
        c += saveRoom(repo, TechLevel.TL6, "CHECKIN_HALL", "Check-in Hall");
        c += saveRoom(repo, TechLevel.TL6, "SECURITY_CHECK", "Security Scan");
        c += saveRoom(repo, TechLevel.TL6, "GATE_AREA", "Launch Gate");
        c += saveRoom(repo, TechLevel.TL6, "CONTROL_TOWER", "Launch Control");
        c += saveRoom(repo, TechLevel.TL6, "HANGAR", "Vehicle Hangar");
        c += saveRoom(repo, TechLevel.TL6, "MAINTENANCE_BAY", "Maintenance Bay");
        c += saveRoom(repo, TechLevel.TL6, "CUSTOMS_SECURITY", "Customs & Security");
        c += saveRoom(repo, TechLevel.TL6, "OBSERVATION_DECK", "Observation Deck");
        c += saveRoom(repo, TechLevel.TL6, "DOCKING_BAY", "Docking Bay");
        c += saveRoom(repo, TechLevel.TL6, "COMMAND_CENTER", "Command Center");
        c += saveRoom(repo, TechLevel.TL6, "FUEL_STATION", "Fuel Station");
        c += saveRoom(repo, TechLevel.TL6, "INN_ENTRANCE", "Lodging Entrance");
        c += saveRoom(repo, TechLevel.TL6, "COMMON_ROOM", "Common Area");
        c += saveRoom(repo, TechLevel.TL6, "CORRIDOR", "Corridor");
        c += saveRoom(repo, TechLevel.TL6, "GUEST_ROOM", "Guest Quarters");
        c += saveRoom(repo, TechLevel.TL6, "TEMPLE_ENTRANCE", "Chapel Entrance");
        c += saveRoom(repo, TechLevel.TL6, "MAIN_HALL", "Chapel Hall");
        c += saveRoom(repo, TechLevel.TL6, "CHAPEL", "Chapel");
        c += saveRoom(repo, TechLevel.TL6, "CLERGY_QUARTERS", "Chaplain's Quarters");
        c += saveRoom(repo, TechLevel.TL6, "STATION_ENTRANCE", "Security Station");
        c += saveRoom(repo, TechLevel.TL6, "MAIN_DESK", "Front Desk");
        c += saveRoom(repo, TechLevel.TL6, "EVIDENCE_ROOM", "Evidence Room");
        c += saveRoom(repo, TechLevel.TL6, "HOLDING_CELLS", "Detention Cells");
        c += saveRoom(repo, TechLevel.TL6, "PRODUCTION_FLOOR", "Production Floor");
        c += saveRoom(repo, TechLevel.TL6, "RAW_MATERIAL_STORAGE", "Material Storage");
        c += saveRoom(repo, TechLevel.TL6, "BREAK_ROOM", "Crew Mess");
        return c;
    }

    // --- TL7: Interplanetary building rooms ---
    private int seedBuildingRoomsTL7(PlanetBuildingModuleRepository repo) {
        int c = 0;
        c += saveRoom(repo, TechLevel.TL7, "PORCH", "Airlock Entry");
        c += saveRoom(repo, TechLevel.TL7, "LIVING_QUARTERS", "Crew Quarters");
        c += saveRoom(repo, TechLevel.TL7, "KITCHEN", "Ship Galley");
        c += saveRoom(repo, TechLevel.TL7, "BEDROOM", "Crew Berth");
        c += saveRoom(repo, TechLevel.TL7, "STORAGE_CLOSET", "Personal Locker");
        c += saveRoom(repo, TechLevel.TL7, "GRAND_ENTRANCE", "Main Airlock");
        c += saveRoom(repo, TechLevel.TL7, "COUNCIL_CHAMBER", "Bridge Conference");
        c += saveRoom(repo, TechLevel.TL7, "RECORDS_OFFICE", "Ship's Log");
        c += saveRoom(repo, TechLevel.TL7, "MAYORS_OFFICE", "Commander's Office");
        c += saveRoom(repo, TechLevel.TL7, "VAULT", "Secure Vault");
        c += saveRoom(repo, TechLevel.TL7, "MARKET_ENTRANCE", "Trade Hub");
        c += saveRoom(repo, TechLevel.TL7, "FOOD_STALL", "Replicator Station");
        c += saveRoom(repo, TechLevel.TL7, "GOODS_STALL", "Supply Depot");
        c += saveRoom(repo, TechLevel.TL7, "AUCTION_HOUSE", "Trade Exchange");
        c += saveRoom(repo, TechLevel.TL7, "LOADING_DOCK", "Cargo Airlock");
        c += saveRoom(repo, TechLevel.TL7, "MAIN_STORAGE", "Cargo Hold");
        c += saveRoom(repo, TechLevel.TL7, "OFFICE", "Station Office");
        c += saveRoom(repo, TechLevel.TL7, "SECURE_STORAGE", "Secure Locker");
        c += saveRoom(repo, TechLevel.TL7, "WAITING_AREA", "Waiting Area");
        c += saveRoom(repo, TechLevel.TL7, "TICKET_BOOTH", "Access Kiosk");
        c += saveRoom(repo, TechLevel.TL7, "MAIN_ENTRANCE", "Station Entrance");
        c += saveRoom(repo, TechLevel.TL7, "WAITING_HALL", "Transit Lounge");
        c += saveRoom(repo, TechLevel.TL7, "TICKET_OFFICE", "Access Office");
        c += saveRoom(repo, TechLevel.TL7, "PLATFORM", "Shuttle Platform");
        c += saveRoom(repo, TechLevel.TL7, "CONTROL_ROOM", "Control Room");
        c += saveRoom(repo, TechLevel.TL7, "STORAGE_ROOM", "Storage Room");
        c += saveRoom(repo, TechLevel.TL7, "DOCK_ENTRANCE", "Dock Airlock");
        c += saveRoom(repo, TechLevel.TL7, "CARGO_WAREHOUSE", "Cargo Hold");
        c += saveRoom(repo, TechLevel.TL7, "PASSENGER_TERMINAL", "Passenger Terminal");
        c += saveRoom(repo, TechLevel.TL7, "DOCK_OFFICE", "Dock Office");
        c += saveRoom(repo, TechLevel.TL7, "CAPTAINS_QUARTERS", "Captain's Quarters");
        c += saveRoom(repo, TechLevel.TL7, "WATERFRONT", "Dockside");
        c += saveRoom(repo, TechLevel.TL7, "MAIN_TERMINAL", "Spaceport Terminal");
        c += saveRoom(repo, TechLevel.TL7, "CHECKIN_HALL", "Check-in Hall");
        c += saveRoom(repo, TechLevel.TL7, "SECURITY_CHECK", "Security Scan");
        c += saveRoom(repo, TechLevel.TL7, "GATE_AREA", "Shuttle Gate");
        c += saveRoom(repo, TechLevel.TL7, "CONTROL_TOWER", "Flight Control");
        c += saveRoom(repo, TechLevel.TL7, "HANGAR", "Shuttle Hangar");
        c += saveRoom(repo, TechLevel.TL7, "MAINTENANCE_BAY", "Maintenance Bay");
        c += saveRoom(repo, TechLevel.TL7, "CUSTOMS_SECURITY", "Customs & Security");
        c += saveRoom(repo, TechLevel.TL7, "OBSERVATION_DECK", "Observation Deck");
        c += saveRoom(repo, TechLevel.TL7, "DOCKING_BAY", "Docking Bay");
        c += saveRoom(repo, TechLevel.TL7, "COMMAND_CENTER", "Command Center");
        c += saveRoom(repo, TechLevel.TL7, "FUEL_STATION", "Propellant Station");
        c += saveRoom(repo, TechLevel.TL7, "INN_ENTRANCE", "Lodging Entrance");
        c += saveRoom(repo, TechLevel.TL7, "COMMON_ROOM", "Crew Lounge");
        c += saveRoom(repo, TechLevel.TL7, "CORRIDOR", "Corridor");
        c += saveRoom(repo, TechLevel.TL7, "GUEST_ROOM", "Guest Quarters");
        c += saveRoom(repo, TechLevel.TL7, "TEMPLE_ENTRANCE", "Chapel Entrance");
        c += saveRoom(repo, TechLevel.TL7, "MAIN_HALL", "Chapel");
        c += saveRoom(repo, TechLevel.TL7, "CHAPEL", "Meditation Room");
        c += saveRoom(repo, TechLevel.TL7, "CLERGY_QUARTERS", "Chaplain's Quarters");
        c += saveRoom(repo, TechLevel.TL7, "STATION_ENTRANCE", "Security Station");
        c += saveRoom(repo, TechLevel.TL7, "MAIN_DESK", "Security Desk");
        c += saveRoom(repo, TechLevel.TL7, "EVIDENCE_ROOM", "Evidence Room");
        c += saveRoom(repo, TechLevel.TL7, "HOLDING_CELLS", "Detention Cells");
        c += saveRoom(repo, TechLevel.TL7, "PRODUCTION_FLOOR", "Manufacturing Floor");
        c += saveRoom(repo, TechLevel.TL7, "RAW_MATERIAL_STORAGE", "Material Storage");
        c += saveRoom(repo, TechLevel.TL7, "BREAK_ROOM", "Crew Mess");
        return c;
    }

    // --- TL8: Interstellar building rooms ---
    private int seedBuildingRoomsTL8(PlanetBuildingModuleRepository repo) {
        int c = 0;
        c += saveRoom(repo, TechLevel.TL8, "PORCH", "Transporter Pad");
        c += saveRoom(repo, TechLevel.TL8, "LIVING_QUARTERS", "Crew Quarters");
        c += saveRoom(repo, TechLevel.TL8, "KITCHEN", "Replicator Galley");
        c += saveRoom(repo, TechLevel.TL8, "BEDROOM", "Crew Berth");
        c += saveRoom(repo, TechLevel.TL8, "STORAGE_CLOSET", "Personal Locker");
        c += saveRoom(repo, TechLevel.TL8, "GRAND_ENTRANCE", "Main Transporter Room");
        c += saveRoom(repo, TechLevel.TL8, "COUNCIL_CHAMBER", "Ship's Bridge");
        c += saveRoom(repo, TechLevel.TL8, "RECORDS_OFFICE", "Computer Core");
        c += saveRoom(repo, TechLevel.TL8, "MAYORS_OFFICE", "Captain's Ready Room");
        c += saveRoom(repo, TechLevel.TL8, "VAULT", "Quantum Vault");
        c += saveRoom(repo, TechLevel.TL8, "MARKET_ENTRANCE", "Commercial Ring");
        c += saveRoom(repo, TechLevel.TL8, "FOOD_STALL", "Replicator Bar");
        c += saveRoom(repo, TechLevel.TL8, "GOODS_STALL", "Supply Kiosk");
        c += saveRoom(repo, TechLevel.TL8, "AUCTION_HOUSE", "Trade Exchange");
        c += saveRoom(repo, TechLevel.TL8, "LOADING_DOCK", "Cargo Transporter");
        c += saveRoom(repo, TechLevel.TL8, "MAIN_STORAGE", "Cargo Bay");
        c += saveRoom(repo, TechLevel.TL8, "OFFICE", "Station Office");
        c += saveRoom(repo, TechLevel.TL8, "SECURE_STORAGE", "Secure Locker");
        c += saveRoom(repo, TechLevel.TL8, "WAITING_AREA", "Waiting Area");
        c += saveRoom(repo, TechLevel.TL8, "TICKET_BOOTH", "Access Kiosk");
        c += saveRoom(repo, TechLevel.TL8, "MAIN_ENTRANCE", "Station Entrance");
        c += saveRoom(repo, TechLevel.TL8, "WAITING_HALL", "Transit Lounge");
        c += saveRoom(repo, TechLevel.TL8, "TICKET_OFFICE", "Access Office");
        c += saveRoom(repo, TechLevel.TL8, "PLATFORM", "Shuttle Platform");
        c += saveRoom(repo, TechLevel.TL8, "CONTROL_ROOM", "Control Room");
        c += saveRoom(repo, TechLevel.TL8, "STORAGE_ROOM", "Storage Room");
        c += saveRoom(repo, TechLevel.TL8, "DOCK_ENTRANCE", "Dock Airlock");
        c += saveRoom(repo, TechLevel.TL8, "CARGO_WAREHOUSE", "Cargo Bay");
        c += saveRoom(repo, TechLevel.TL8, "PASSENGER_TERMINAL", "Passenger Terminal");
        c += saveRoom(repo, TechLevel.TL8, "DOCK_OFFICE", "Dock Office");
        c += saveRoom(repo, TechLevel.TL8, "CAPTAINS_QUARTERS", "Captain's Quarters");
        c += saveRoom(repo, TechLevel.TL8, "WATERFRONT", "Dockside");
        c += saveRoom(repo, TechLevel.TL8, "MAIN_TERMINAL", "Spaceport Terminal");
        c += saveRoom(repo, TechLevel.TL8, "CHECKIN_HALL", "Check-in Hall");
        c += saveRoom(repo, TechLevel.TL8, "SECURITY_CHECK", "Security Scan");
        c += saveRoom(repo, TechLevel.TL8, "GATE_AREA", "Shuttle Gate");
        c += saveRoom(repo, TechLevel.TL8, "CONTROL_TOWER", "Flight Control");
        c += saveRoom(repo, TechLevel.TL8, "HANGAR", "Shuttle Hangar");
        c += saveRoom(repo, TechLevel.TL8, "MAINTENANCE_BAY", "Engineering Bay");
        c += saveRoom(repo, TechLevel.TL8, "CUSTOMS_SECURITY", "Customs & Security");
        c += saveRoom(repo, TechLevel.TL8, "OBSERVATION_DECK", "Observation Deck");
        c += saveRoom(repo, TechLevel.TL8, "DOCKING_BAY", "Docking Bay");
        c += saveRoom(repo, TechLevel.TL8, "COMMAND_CENTER", "Command Center");
        c += saveRoom(repo, TechLevel.TL8, "FUEL_STATION", "Antimatter Station");
        c += saveRoom(repo, TechLevel.TL8, "INN_ENTRANCE", "Lodging Entrance");
        c += saveRoom(repo, TechLevel.TL8, "COMMON_ROOM", "Crew Lounge");
        c += saveRoom(repo, TechLevel.TL8, "CORRIDOR", "Corridor");
        c += saveRoom(repo, TechLevel.TL8, "GUEST_ROOM", "Guest Quarters");
        c += saveRoom(repo, TechLevel.TL8, "TEMPLE_ENTRANCE", "Chapel Entrance");
        c += saveRoom(repo, TechLevel.TL8, "MAIN_HALL", "Chapel");
        c += saveRoom(repo, TechLevel.TL8, "CHAPEL", "Meditation Room");
        c += saveRoom(repo, TechLevel.TL8, "CLERGY_QUARTERS", "Chaplain's Quarters");
        c += saveRoom(repo, TechLevel.TL8, "STATION_ENTRANCE", "Security Station");
        c += saveRoom(repo, TechLevel.TL8, "MAIN_DESK", "Security Desk");
        c += saveRoom(repo, TechLevel.TL8, "EVIDENCE_ROOM", "Evidence Room");
        c += saveRoom(repo, TechLevel.TL8, "HOLDING_CELLS", "Detention Cells");
        c += saveRoom(repo, TechLevel.TL8, "PRODUCTION_FLOOR", "Nanofab Floor");
        c += saveRoom(repo, TechLevel.TL8, "RAW_MATERIAL_STORAGE", "Material Storage");
        c += saveRoom(repo, TechLevel.TL8, "BREAK_ROOM", "Crew Mess");
        return c;
    }

    // --- TL9: Galactic building rooms ---
    private int seedBuildingRoomsTL9(PlanetBuildingModuleRepository repo) {
        int c = 0;
        c += saveRoom(repo, TechLevel.TL9, "PORCH", "Dimensional Airlock");
        c += saveRoom(repo, TechLevel.TL9, "LIVING_QUARTERS", "Habitation Suite");
        c += saveRoom(repo, TechLevel.TL9, "KITCHEN", "Matter Synthesis Galley");
        c += saveRoom(repo, TechLevel.TL9, "BEDROOM", "Stasis Berth");
        c += saveRoom(repo, TechLevel.TL9, "STORAGE_CLOSET", "Extradimensional Locker");
        c += saveRoom(repo, TechLevel.TL9, "GRAND_ENTRANCE", "Starbase Entrance");
        c += saveRoom(repo, TechLevel.TL9, "COUNCIL_CHAMBER", "Fleet Command");
        c += saveRoom(repo, TechLevel.TL9, "RECORDS_OFFICE", "AI Archive");
        c += saveRoom(repo, TechLevel.TL9, "MAYORS_OFFICE", "Fleet Admiral's Office");
        c += saveRoom(repo, TechLevel.TL9, "VAULT", "Dimensional Vault");
        c += saveRoom(repo, TechLevel.TL9, "MARKET_ENTRANCE", "Galactic Trade Hub");
        c += saveRoom(repo, TechLevel.TL9, "FOOD_STALL", "Synthesis Bar");
        c += saveRoom(repo, TechLevel.TL9, "GOODS_STALL", "Exotic Supply Depot");
        c += saveRoom(repo, TechLevel.TL9, "AUCTION_HOUSE", "Galactic Exchange");
        c += saveRoom(repo, TechLevel.TL9, "LOADING_DOCK", "Cargo Airlock");
        c += saveRoom(repo, TechLevel.TL9, "MAIN_STORAGE", "Cargo Hold");
        c += saveRoom(repo, TechLevel.TL9, "OFFICE", "Station Office");
        c += saveRoom(repo, TechLevel.TL9, "SECURE_STORAGE", "Secure Locker");
        c += saveRoom(repo, TechLevel.TL9, "WAITING_AREA", "Waiting Area");
        c += saveRoom(repo, TechLevel.TL9, "TICKET_BOOTH", "Access Kiosk");
        c += saveRoom(repo, TechLevel.TL9, "MAIN_ENTRANCE", "Station Entrance");
        c += saveRoom(repo, TechLevel.TL9, "WAITING_HALL", "Transit Lounge");
        c += saveRoom(repo, TechLevel.TL9, "TICKET_OFFICE", "Access Office");
        c += saveRoom(repo, TechLevel.TL9, "PLATFORM", "Wormhole Platform");
        c += saveRoom(repo, TechLevel.TL9, "CONTROL_ROOM", "Control Room");
        c += saveRoom(repo, TechLevel.TL9, "STORAGE_ROOM", "Storage Room");
        c += saveRoom(repo, TechLevel.TL9, "DOCK_ENTRANCE", "Dock Airlock");
        c += saveRoom(repo, TechLevel.TL9, "CARGO_WAREHOUSE", "Cargo Hold");
        c += saveRoom(repo, TechLevel.TL9, "PASSENGER_TERMINAL", "Passenger Terminal");
        c += saveRoom(repo, TechLevel.TL9, "DOCK_OFFICE", "Dock Office");
        c += saveRoom(repo, TechLevel.TL9, "CAPTAINS_QUARTERS", "Captain's Quarters");
        c += saveRoom(repo, TechLevel.TL9, "WATERFRONT", "Dockside");
        c += saveRoom(repo, TechLevel.TL9, "MAIN_TERMINAL", "Starbase Terminal");
        c += saveRoom(repo, TechLevel.TL9, "CHECKIN_HALL", "Check-in Hall");
        c += saveRoom(repo, TechLevel.TL9, "SECURITY_CHECK", "Security Scan");
        c += saveRoom(repo, TechLevel.TL9, "GATE_AREA", "Wormhole Gate");
        c += saveRoom(repo, TechLevel.TL9, "CONTROL_TOWER", "Wormhole Control");
        c += saveRoom(repo, TechLevel.TL9, "HANGAR", "Capital Ship Hangar");
        c += saveRoom(repo, TechLevel.TL9, "MAINTENANCE_BAY", "Engineering Bay");
        c += saveRoom(repo, TechLevel.TL9, "CUSTOMS_SECURITY", "Customs & Security");
        c += saveRoom(repo, TechLevel.TL9, "OBSERVATION_DECK", "Observation Deck");
        c += saveRoom(repo, TechLevel.TL9, "DOCKING_BAY", "Docking Bay");
        c += saveRoom(repo, TechLevel.TL9, "COMMAND_CENTER", "Fleet Command");
        c += saveRoom(repo, TechLevel.TL9, "FUEL_STATION", "Wormhole Fuel Station");
        c += saveRoom(repo, TechLevel.TL9, "INN_ENTRANCE", "Lodging Entrance");
        c += saveRoom(repo, TechLevel.TL9, "COMMON_ROOM", "Crew Lounge");
        c += saveRoom(repo, TechLevel.TL9, "CORRIDOR", "Corridor");
        c += saveRoom(repo, TechLevel.TL9, "GUEST_ROOM", "Guest Quarters");
        c += saveRoom(repo, TechLevel.TL9, "TEMPLE_ENTRANCE", "Chapel Entrance");
        c += saveRoom(repo, TechLevel.TL9, "MAIN_HALL", "Chapel");
        c += saveRoom(repo, TechLevel.TL9, "CHAPEL", "Meditation Room");
        c += saveRoom(repo, TechLevel.TL9, "CLERGY_QUARTERS", "Chaplain's Quarters");
        c += saveRoom(repo, TechLevel.TL9, "STATION_ENTRANCE", "Security Station");
        c += saveRoom(repo, TechLevel.TL9, "MAIN_DESK", "Security Desk");
        c += saveRoom(repo, TechLevel.TL9, "EVIDENCE_ROOM", "Evidence Room");
        c += saveRoom(repo, TechLevel.TL9, "HOLDING_CELLS", "Detention Cells");
        c += saveRoom(repo, TechLevel.TL9, "PRODUCTION_FLOOR", "Star Forge Floor");
        c += saveRoom(repo, TechLevel.TL9, "RAW_MATERIAL_STORAGE", "Material Storage");
        c += saveRoom(repo, TechLevel.TL9, "BREAK_ROOM", "Crew Mess");
        return c;
    }

    // --- TL10: Post-Human building rooms ---
    private int seedBuildingRoomsTL10(PlanetBuildingModuleRepository repo) {
        int c = 0;
        c += saveRoom(repo, TechLevel.TL10, "PORCH", "Transwarp Airlock");
        c += saveRoom(repo, TechLevel.TL10, "LIVING_QUARTERS", "Megastructure Hab");
        c += saveRoom(repo, TechLevel.TL10, "KITCHEN", "Reality Kitchen");
        c += saveRoom(repo, TechLevel.TL10, "BEDROOM", "Stasis Chamber");
        c += saveRoom(repo, TechLevel.TL10, "STORAGE_CLOSET", "Omniversal Locker");
        c += saveRoom(repo, TechLevel.TL10, "GRAND_ENTRANCE", "Megastructure Entrance");
        c += saveRoom(repo, TechLevel.TL10, "COUNCIL_CHAMBER", "Megastructure Command");
        c += saveRoom(repo, TechLevel.TL10, "RECORDS_OFFICE", "Matrioshka Archive");
        c += saveRoom(repo, TechLevel.TL10, "MAYORS_OFFICE", "Overseer's Office");
        c += saveRoom(repo, TechLevel.TL10, "VAULT", "Omniversal Vault");
        c += saveRoom(repo, TechLevel.TL10, "MARKET_ENTRANCE", "Stellar Trade Hub");
        c += saveRoom(repo, TechLevel.TL10, "FOOD_STALL", "Matter Synthesis Bar");
        c += saveRoom(repo, TechLevel.TL10, "GOODS_STALL", "Stellar Supply Depot");
        c += saveRoom(repo, TechLevel.TL10, "AUCTION_HOUSE", "Stellar Exchange");
        c += saveRoom(repo, TechLevel.TL10, "LOADING_DOCK", "Stellar Cargo Dock");
        c += saveRoom(repo, TechLevel.TL10, "MAIN_STORAGE", "Stellar Cargo Hold");
        c += saveRoom(repo, TechLevel.TL10, "OFFICE", "Megastructure Office");
        c += saveRoom(repo, TechLevel.TL10, "SECURE_STORAGE", "Secure Locker");
        c += saveRoom(repo, TechLevel.TL10, "WAITING_AREA", "Waiting Area");
        c += saveRoom(repo, TechLevel.TL10, "TICKET_BOOTH", "Access Kiosk");
        c += saveRoom(repo, TechLevel.TL10, "MAIN_ENTRANCE", "Megastructure Entrance");
        c += saveRoom(repo, TechLevel.TL10, "WAITING_HALL", "Transit Lounge");
        c += saveRoom(repo, TechLevel.TL10, "TICKET_OFFICE", "Access Office");
        c += saveRoom(repo, TechLevel.TL10, "PLATFORM", "Transwarp Platform");
        c += saveRoom(repo, TechLevel.TL10, "CONTROL_ROOM", "Reality Control");
        c += saveRoom(repo, TechLevel.TL10, "STORAGE_ROOM", "Storage Room");
        c += saveRoom(repo, TechLevel.TL10, "DOCK_ENTRANCE", "Dock Airlock");
        c += saveRoom(repo, TechLevel.TL10, "CARGO_WAREHOUSE", "Cargo Hold");
        c += saveRoom(repo, TechLevel.TL10, "PASSENGER_TERMINAL", "Passenger Terminal");
        c += saveRoom(repo, TechLevel.TL10, "DOCK_OFFICE", "Dock Office");
        c += saveRoom(repo, TechLevel.TL10, "CAPTAINS_QUARTERS", "Captain's Quarters");
        c += saveRoom(repo, TechLevel.TL10, "WATERFRONT", "Dockside");
        c += saveRoom(repo, TechLevel.TL10, "MAIN_TERMINAL", "Megastructure Terminal");
        c += saveRoom(repo, TechLevel.TL10, "CHECKIN_HALL", "Check-in Hall");
        c += saveRoom(repo, TechLevel.TL10, "SECURITY_CHECK", "Security Scan");
        c += saveRoom(repo, TechLevel.TL10, "GATE_AREA", "Transwarp Gate");
        c += saveRoom(repo, TechLevel.TL10, "CONTROL_TOWER", "Transwarp Control");
        c += saveRoom(repo, TechLevel.TL10, "HANGAR", "Megastructure Hangar");
        c += saveRoom(repo, TechLevel.TL10, "MAINTENANCE_BAY", "Reality Engineering");
        c += saveRoom(repo, TechLevel.TL10, "CUSTOMS_SECURITY", "Customs & Security");
        c += saveRoom(repo, TechLevel.TL10, "OBSERVATION_DECK", "Stellar Observation");
        c += saveRoom(repo, TechLevel.TL10, "DOCKING_BAY", "Docking Bay");
        c += saveRoom(repo, TechLevel.TL10, "COMMAND_CENTER", "Megastructure Command");
        c += saveRoom(repo, TechLevel.TL10, "FUEL_STATION", "Stellar Fuel Station");
        c += saveRoom(repo, TechLevel.TL10, "INN_ENTRANCE", "Lodging Entrance");
        c += saveRoom(repo, TechLevel.TL10, "COMMON_ROOM", "Crew Lounge");
        c += saveRoom(repo, TechLevel.TL10, "CORRIDOR", "Corridor");
        c += saveRoom(repo, TechLevel.TL10, "GUEST_ROOM", "Guest Quarters");
        c += saveRoom(repo, TechLevel.TL10, "TEMPLE_ENTRANCE", "Chapel Entrance");
        c += saveRoom(repo, TechLevel.TL10, "MAIN_HALL", "Chapel");
        c += saveRoom(repo, TechLevel.TL10, "CHAPEL", "Meditation Room");
        c += saveRoom(repo, TechLevel.TL10, "CLERGY_QUARTERS", "Chaplain's Quarters");
        c += saveRoom(repo, TechLevel.TL10, "STATION_ENTRANCE", "Security Station");
        c += saveRoom(repo, TechLevel.TL10, "MAIN_DESK", "Security Desk");
        c += saveRoom(repo, TechLevel.TL10, "EVIDENCE_ROOM", "Evidence Room");
        c += saveRoom(repo, TechLevel.TL10, "HOLDING_CELLS", "Detention Cells");
        c += saveRoom(repo, TechLevel.TL10, "PRODUCTION_FLOOR", "Reality Engine Floor");
        c += saveRoom(repo, TechLevel.TL10, "RAW_MATERIAL_STORAGE", "Stellar Material Storage");
        c += saveRoom(repo, TechLevel.TL10, "BREAK_ROOM", "Crew Mess");
        return c;
    }

    // --- TL11: Transcendent building rooms ---
    private int seedBuildingRoomsTL11(PlanetBuildingModuleRepository repo) {
        int c = 0;
        c += saveRoom(repo, TechLevel.TL11, "PORCH", "Dimensional Portal");
        c += saveRoom(repo, TechLevel.TL11, "LIVING_QUARTERS", "Dimensional Hab");
        c += saveRoom(repo, TechLevel.TL11, "KITCHEN", "Dimensional Kitchen");
        c += saveRoom(repo, TechLevel.TL11, "BEDROOM", "Time Stasis Chamber");
        c += saveRoom(repo, TechLevel.TL11, "STORAGE_CLOSET", "Timeline Locker");
        c += saveRoom(repo, TechLevel.TL11, "GRAND_ENTRANCE", "Dimensional Hub");
        c += saveRoom(repo, TechLevel.TL11, "COUNCIL_CHAMBER", "Dimensional Command");
        c += saveRoom(repo, TechLevel.TL11, "RECORDS_OFFICE", "Timeline Archive");
        c += saveRoom(repo, TechLevel.TL11, "MAYORS_OFFICE", "Dimensional Overseer");
        c += saveRoom(repo, TechLevel.TL11, "VAULT", "Timeline Vault");
        c += saveRoom(repo, TechLevel.TL11, "MARKET_ENTRANCE", "Dimensional Trade Hub");
        c += saveRoom(repo, TechLevel.TL11, "FOOD_STALL", "Dimensional Bar");
        c += saveRoom(repo, TechLevel.TL11, "GOODS_STALL", "Dimensional Supply");
        c += saveRoom(repo, TechLevel.TL11, "AUCTION_HOUSE", "Dimensional Exchange");
        c += saveRoom(repo, TechLevel.TL11, "LOADING_DOCK", "Dimensional Dock");
        c += saveRoom(repo, TechLevel.TL11, "MAIN_STORAGE", "Dimensional Hold");
        c += saveRoom(repo, TechLevel.TL11, "OFFICE", "Dimensional Office");
        c += saveRoom(repo, TechLevel.TL11, "SECURE_STORAGE", "Secure Locker");
        c += saveRoom(repo, TechLevel.TL11, "WAITING_AREA", "Waiting Area");
        c += saveRoom(repo, TechLevel.TL11, "TICKET_BOOTH", "Access Kiosk");
        c += saveRoom(repo, TechLevel.TL11, "MAIN_ENTRANCE", "Dimensional Entrance");
        c += saveRoom(repo, TechLevel.TL11, "WAITING_HALL", "Transit Lounge");
        c += saveRoom(repo, TechLevel.TL11, "TICKET_OFFICE", "Access Office");
        c += saveRoom(repo, TechLevel.TL11, "PLATFORM", "Dimensional Platform");
        c += saveRoom(repo, TechLevel.TL11, "CONTROL_ROOM", "Temporal Control");
        c += saveRoom(repo, TechLevel.TL11, "STORAGE_ROOM", "Storage Room");
        c += saveRoom(repo, TechLevel.TL11, "DOCK_ENTRANCE", "Dock Airlock");
        c += saveRoom(repo, TechLevel.TL11, "CARGO_WAREHOUSE", "Cargo Hold");
        c += saveRoom(repo, TechLevel.TL11, "PASSENGER_TERMINAL", "Passenger Terminal");
        c += saveRoom(repo, TechLevel.TL11, "DOCK_OFFICE", "Dock Office");
        c += saveRoom(repo, TechLevel.TL11, "CAPTAINS_QUARTERS", "Captain's Quarters");
        c += saveRoom(repo, TechLevel.TL11, "WATERFRONT", "Dockside");
        c += saveRoom(repo, TechLevel.TL11, "MAIN_TERMINAL", "Dimensional Terminal");
        c += saveRoom(repo, TechLevel.TL11, "CHECKIN_HALL", "Check-in Hall");
        c += saveRoom(repo, TechLevel.TL11, "SECURITY_CHECK", "Security Scan");
        c += saveRoom(repo, TechLevel.TL11, "GATE_AREA", "Dimensional Gate");
        c += saveRoom(repo, TechLevel.TL11, "CONTROL_TOWER", "Temporal Control");
        c += saveRoom(repo, TechLevel.TL11, "HANGAR", "Dimensional Hangar");
        c += saveRoom(repo, TechLevel.TL11, "MAINTENANCE_BAY", "Temporal Engineering");
        c += saveRoom(repo, TechLevel.TL11, "CUSTOMS_SECURITY", "Customs & Security");
        c += saveRoom(repo, TechLevel.TL11, "OBSERVATION_DECK", "Dimensional View");
        c += saveRoom(repo, TechLevel.TL11, "DOCKING_BAY", "Docking Bay");
        c += saveRoom(repo, TechLevel.TL11, "COMMAND_CENTER", "Dimensional Command");
        c += saveRoom(repo, TechLevel.TL11, "FUEL_STATION", "Temporal Fuel");
        c += saveRoom(repo, TechLevel.TL11, "INN_ENTRANCE", "Lodging Entrance");
        c += saveRoom(repo, TechLevel.TL11, "COMMON_ROOM", "Crew Lounge");
        c += saveRoom(repo, TechLevel.TL11, "CORRIDOR", "Corridor");
        c += saveRoom(repo, TechLevel.TL11, "GUEST_ROOM", "Guest Quarters");
        c += saveRoom(repo, TechLevel.TL11, "TEMPLE_ENTRANCE", "Chapel Entrance");
        c += saveRoom(repo, TechLevel.TL11, "MAIN_HALL", "Chapel");
        c += saveRoom(repo, TechLevel.TL11, "CHAPEL", "Meditation Room");
        c += saveRoom(repo, TechLevel.TL11, "CLERGY_QUARTERS", "Chaplain's Quarters");
        c += saveRoom(repo, TechLevel.TL11, "STATION_ENTRANCE", "Security Station");
        c += saveRoom(repo, TechLevel.TL11, "MAIN_DESK", "Security Desk");
        c += saveRoom(repo, TechLevel.TL11, "EVIDENCE_ROOM", "Evidence Room");
        c += saveRoom(repo, TechLevel.TL11, "HOLDING_CELLS", "Detention Cells");
        c += saveRoom(repo, TechLevel.TL11, "PRODUCTION_FLOOR", "Universe Seeder Floor");
        c += saveRoom(repo, TechLevel.TL11, "RAW_MATERIAL_STORAGE", "Dimensional Material");
        c += saveRoom(repo, TechLevel.TL11, "BREAK_ROOM", "Crew Mess");
        return c;
    }

    // --- TL12: Post-Singularity building rooms ---
    private int seedBuildingRoomsTL12(PlanetBuildingModuleRepository repo) {
        int c = 0;
        c += saveRoom(repo, TechLevel.TL12, "PORCH", "Omniversal Gate");
        c += saveRoom(repo, TechLevel.TL12, "LIVING_QUARTERS", "Omniversal Hab");
        c += saveRoom(repo, TechLevel.TL12, "KITCHEN", "Existence Kitchen");
        c += saveRoom(repo, TechLevel.TL12, "BEDROOM", "Eternal Stasis");
        c += saveRoom(repo, TechLevel.TL12, "STORAGE_CLOSET", "Omniversal Locker");
        c += saveRoom(repo, TechLevel.TL12, "GRAND_ENTRANCE", "Omniversal Throne Room");
        c += saveRoom(repo, TechLevel.TL12, "COUNCIL_CHAMBER", "Omniversal Command");
        c += saveRoom(repo, TechLevel.TL12, "RECORDS_OFFICE", "Absolute Knowledge");
        c += saveRoom(repo, TechLevel.TL12, "MAYORS_OFFICE", "Omniversal Overseer");
        c += saveRoom(repo, TechLevel.TL12, "VAULT", "Omniversal Vault");
        c += saveRoom(repo, TechLevel.TL12, "MARKET_ENTRANCE", "Omniversal Trade");
        c += saveRoom(repo, TechLevel.TL12, "FOOD_STALL", "Existence Bar");
        c += saveRoom(repo, TechLevel.TL12, "GOODS_STALL", "Omniversal Supply");
        c += saveRoom(repo, TechLevel.TL12, "AUCTION_HOUSE", "Omniversal Exchange");
        c += saveRoom(repo, TechLevel.TL12, "LOADING_DOCK", "Omniversal Dock");
        c += saveRoom(repo, TechLevel.TL12, "MAIN_STORAGE", "Omniversal Hold");
        c += saveRoom(repo, TechLevel.TL12, "OFFICE", "Omniversal Office");
        c += saveRoom(repo, TechLevel.TL12, "SECURE_STORAGE", "Secure Locker");
        c += saveRoom(repo, TechLevel.TL12, "WAITING_AREA", "Waiting Area");
        c += saveRoom(repo, TechLevel.TL12, "TICKET_BOOTH", "Access Kiosk");
        c += saveRoom(repo, TechLevel.TL12, "MAIN_ENTRANCE", "Omniversal Entrance");
        c += saveRoom(repo, TechLevel.TL12, "WAITING_HALL", "Transit Lounge");
        c += saveRoom(repo, TechLevel.TL12, "TICKET_OFFICE", "Access Office");
        c += saveRoom(repo, TechLevel.TL12, "PLATFORM", "Omniversal Platform");
        c += saveRoom(repo, TechLevel.TL12, "CONTROL_ROOM", "Existence Control");
        c += saveRoom(repo, TechLevel.TL12, "STORAGE_ROOM", "Storage Room");
        c += saveRoom(repo, TechLevel.TL12, "DOCK_ENTRANCE", "Dock Airlock");
        c += saveRoom(repo, TechLevel.TL12, "CARGO_WAREHOUSE", "Cargo Hold");
        c += saveRoom(repo, TechLevel.TL12, "PASSENGER_TERMINAL", "Passenger Terminal");
        c += saveRoom(repo, TechLevel.TL12, "DOCK_OFFICE", "Dock Office");
        c += saveRoom(repo, TechLevel.TL12, "CAPTAINS_QUARTERS", "Captain's Quarters");
        c += saveRoom(repo, TechLevel.TL12, "WATERFRONT", "Dockside");
        c += saveRoom(repo, TechLevel.TL12, "MAIN_TERMINAL", "Omniversal Terminal");
        c += saveRoom(repo, TechLevel.TL12, "CHECKIN_HALL", "Check-in Hall");
        c += saveRoom(repo, TechLevel.TL12, "SECURITY_CHECK", "Security Scan");
        c += saveRoom(repo, TechLevel.TL12, "GATE_AREA", "Omniversal Gate");
        c += saveRoom(repo, TechLevel.TL12, "CONTROL_TOWER", "Existence Control");
        c += saveRoom(repo, TechLevel.TL12, "HANGAR", "Omniversal Hangar");
        c += saveRoom(repo, TechLevel.TL12, "MAINTENANCE_BAY", "Existence Engineering");
        c += saveRoom(repo, TechLevel.TL12, "CUSTOMS_SECURITY", "Customs & Security");
        c += saveRoom(repo, TechLevel.TL12, "OBSERVATION_DECK", "Omniversal View");
        c += saveRoom(repo, TechLevel.TL12, "DOCKING_BAY", "Docking Bay");
        c += saveRoom(repo, TechLevel.TL12, "COMMAND_CENTER", "Omniversal Command");
        c += saveRoom(repo, TechLevel.TL12, "FUEL_STATION", "Existence Fuel");
        c += saveRoom(repo, TechLevel.TL12, "INN_ENTRANCE", "Lodging Entrance");
        c += saveRoom(repo, TechLevel.TL12, "COMMON_ROOM", "Crew Lounge");
        c += saveRoom(repo, TechLevel.TL12, "CORRIDOR", "Corridor");
        c += saveRoom(repo, TechLevel.TL12, "GUEST_ROOM", "Guest Quarters");
        c += saveRoom(repo, TechLevel.TL12, "TEMPLE_ENTRANCE", "Chapel Entrance");
        c += saveRoom(repo, TechLevel.TL12, "MAIN_HALL", "Chapel");
        c += saveRoom(repo, TechLevel.TL12, "CHAPEL", "Meditation Room");
        c += saveRoom(repo, TechLevel.TL12, "CLERGY_QUARTERS", "Chaplain's Quarters");
        c += saveRoom(repo, TechLevel.TL12, "STATION_ENTRANCE", "Security Station");
        c += saveRoom(repo, TechLevel.TL12, "MAIN_DESK", "Security Desk");
        c += saveRoom(repo, TechLevel.TL12, "EVIDENCE_ROOM", "Evidence Room");
        c += saveRoom(repo, TechLevel.TL12, "HOLDING_CELLS", "Detention Cells");
        c += saveRoom(repo, TechLevel.TL12, "PRODUCTION_FLOOR", "Cosmic Loom Floor");
        c += saveRoom(repo, TechLevel.TL12, "RAW_MATERIAL_STORAGE", "Existence Material");
        c += saveRoom(repo, TechLevel.TL12, "BREAK_ROOM", "Crew Mess");
        return c;
    }

    // === Animal Seeding ===

    private int seedAnimals(PlanetBuildingModuleRepository repo) {
        int c = 0;

        // Animals exist at TL0 (prehistoric) and up — they don't change with tech level
        // but their lore names do via TechLevelAdapter

        // --- Livestock ---
        c += saveAnimal(repo, TechLevel.TL0, "COW", "Cow", 20.0, null, null, null, 200.0,
                "Domesticated bovine for milk and meat");
        c += saveAnimal(repo, TechLevel.TL0, "PIG", "Pig", null, null, null, null, 80.0,
                "Domesticated swine for meat production");
        c += saveAnimal(repo, TechLevel.TL0, "SHEEP", "Sheep", 2.0, 3.0, null, null, 30.0,
                "Wool-producing livestock");
        c += saveAnimal(repo, TechLevel.TL0, "GOAT", "Goat", 3.0, 1.0, null, null, 25.0,
                "Hardy livestock for milk and meat");
        c += saveAnimal(repo, TechLevel.TL0, "CHICKEN", "Chicken", null, null, null, 5, 2.0,
                "Poultry for eggs and meat");
        c += saveAnimal(repo, TechLevel.TL0, "DUCK", "Duck", null, null, null, 4, 1.5,
                "Waterfowl for eggs and meat");
        c += saveAnimal(repo, TechLevel.TL0, "TURKEY", "Turkey", null, null, null, 3, 15.0,
                "Large poultry for meat");

        // --- Transport ---
        c += saveAnimal(repo, TechLevel.TL0, "HORSE", "Horse", null, null, 100, null, 150.0,
                "Primary transport and draft animal");
        c += saveAnimal(repo, TechLevel.TL0, "DONKEY", "Donkey", null, null, 80, null, 60.0,
                "Sturdy pack animal for carrying loads");
        c += saveAnimal(repo, TechLevel.TL0, "MULE", "Mule", null, null, 120, null, 70.0,
                "Hybrid of horse and donkey, excellent pack animal");
        c += saveAnimal(repo, TechLevel.TL0, "CAMEL", "Camel", null, null, 150, null, 100.0,
                "Desert transport animal, can go without water for days");
        c += saveAnimal(repo, TechLevel.TL0, "OX", "Ox", null, null, 200, null, 250.0,
                "Castrated bull used as draft animal for plowing and hauling");

        // --- Companion ---
        c += saveAnimal(repo, TechLevel.TL0, "DOG", "Dog", null, null, null, null, 15.0,
                "Loyal companion animal for hunting and guarding");
        c += saveAnimal(repo, TechLevel.TL0, "CAT", "Cat", null, null, null, null, 3.0,
                "Companion animal for pest control");

        // --- Industry ---
        c += saveAnimal(repo, TechLevel.TL0, "BEE", "Bee", null, null, null, null, 0.0,
                "Honey-producing insect, essential for pollination");
        c += saveAnimal(repo, TechLevel.TL0, "SILK_WORM", "Silk Worm", null, null, null, null, 0.0,
                "Caterpillar that produces silk for textiles");
        c += saveAnimal(repo, TechLevel.TL0, "SALMON", "Salmon", null, null, null, null, 4.0,
                "Fish for food and trade");

        // --- Wild ---
        c += saveAnimal(repo, TechLevel.TL0, "DEER", "Deer", null, null, null, null, 40.0,
                "Wild game animal for hunting");
        c += saveAnimal(repo, TechLevel.TL0, "RABBIT", "Rabbit", null, null, null, null, 1.0,
                "Small wild game animal");
        c += saveAnimal(repo, TechLevel.TL0, "BEAVER", "Beaver", null, null, null, null, 8.0,
                "Rodent valued for fur and dam-building");
        c += saveAnimal(repo, TechLevel.TL0, "OTTER", "Otter", null, null, null, null, 5.0,
                "Aquatic mammal valued for fur");

        // --- Additional animals for TL1+ ---
        c += saveAnimal(repo, TechLevel.TL1, "LLAMA", "Llama", null, null, 60, null, 40.0,
                "Andean pack animal and wool source");
        c += saveAnimal(repo, TechLevel.TL1, "YAK", "Yak", 5.0, 2.0, 100, null, 120.0,
                "High-altitude livestock for milk, wool, and transport");
        c += saveAnimal(repo, TechLevel.TL1, "BUFFALO", "Buffalo", 15.0, null, 150, null, 300.0,
                "Large bovine for milk, meat, and draft work");
        c += saveAnimal(repo, TechLevel.TL1, "GOOSE", "Goose", null, null, null, 3, 4.0,
                "Large waterfowl for eggs, meat, and guarding");
        c += saveAnimal(repo, TechLevel.TL1, "GOAT_FISH", "Goat Fish", null, null, null, null, 2.0,
                "Freshwater fish for food");

        // --- TL2: Medieval animals ---
        c += saveAnimal(repo, TechLevel.TL2, "WAR_HORSE", "War Horse", null, null, 120, null, 200.0,
                "Trained horse for cavalry and knightly combat");
        c += saveAnimal(repo, TechLevel.TL2, "HAWK", "Hawk", null, null, null, null, 0.5,
                "Bird of prey for falconry hunting");
        c += saveAnimal(repo, TechLevel.TL2, "BEAR", "Bear", null, null, null, null, 150.0,
                "Large wild animal, hunted for meat and fur");
        c += saveAnimal(repo, TechLevel.TL2, "WOLF", "Wolf", null, null, null, null, 30.0,
                "Wild canine, sometimes domesticated for hunting");
        c += saveAnimal(repo, TechLevel.TL2, "EAGLE", "Eagle", null, null, null, null, 1.0,
                "Majestic bird of prey, symbol of power");

        // --- TL3: Renaissance animals ---
        c += saveAnimal(repo, TechLevel.TL3, "ELEPHANT", "Elephant", 60.0, null, 500, null, 1000.0,
                "Massive animal for transport and labor");
        c += saveAnimal(repo, TechLevel.TL3, "ZEBRA", "Zebra", null, null, 80, null, 100.0,
                "Striped equid, sometimes domesticated for riding");
        c += saveAnimal(repo, TechLevel.TL3, "PONY", "Pony", null, null, 60, null, 50.0,
                "Small horse for children and light work");

        // --- TL4: Industrial animals ---
        c += saveAnimal(repo, TechLevel.TL4, "RACE_HORSE", "Race Horse", null, null, 80, null, 180.0,
                "Bred for speed and competition");
        c += saveAnimal(repo, TechLevel.TL4, "SHOW_COW", "Show Cow", 25.0, null, null, null, 250.0,
                "Prize livestock for exhibitions");
        c += saveAnimal(repo, TechLevel.TL4, "CANARY", "Canary", null, null, null, null, 0.1,
                "Small songbird, used in mines to detect gas");

        return c;
    }

    private int saveAnimal(PlanetBuildingModuleRepository repo, TechLevel techLevel,
                           String genericType, String genericName,
                           Double milkYield, Double woolYield, Integer carryingCapacity,
                           Integer eggRate, Double meatYield, String description) {
        PlanetBuildingModuleEntity entity = new PlanetBuildingModuleEntity(
                techLevel, ModuleCategory.ANIMAL, genericType, genericName, 0, 1, description);
        entity.setMilkYield(milkYield);
        entity.setWoolYield(woolYield);
        entity.setCarryingCapacity(carryingCapacity);
        entity.setEggRate(eggRate);
        entity.setMeatYield(meatYield);
        repo.save(entity);
        return 1;
    }
}
