package com.yisy.modules.doramcu.hex.planet;

import com.yisy.modules.doramcu.gamecore.DungeonRoomService;
import com.yisy.modules.doramcu.model.PlanetBuildingModuleEntity;
import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.techlevel.TechLevelService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates 3D interior dungeon rooms for buildings on the planet surface.
 * <p>
 * Each building type (settlement, industry, station) has a template that defines
 * the rooms, connections, traps, treasure, and encounters. The templates are derived
 * from the biodome database document and use the D&D Dungeon Building module
 * (DungeonRoomService) to create explorable interiors.
 * </p>
 * <p>
 * The generator is tech-level aware: it uses {@link TechLevelService} to look up
 * tech-appropriate room names, descriptions, traps, and treasure from the module
 * database. For example, a TL2 house generates "Stone Hut" rooms, while a TL8
 * house generates "Habitation Module" rooms. If no tech-specific module is found,
 * it falls back to generic hardcoded names.
 * </p>
 * <p>
 * Building types and their room templates:
 * <ul>
 *   <li>Residential House — 5 rooms (porch, living room, kitchen, bedroom, storage)</li>
 *   <li>Town Hall — 5 rooms (entrance, council chamber, records, mayor's office, vault)</li>
 *   <li>Market — 4 rooms (entrance, food stalls, goods stalls, auction house)</li>
 *   <li>Warehouse — 4 rooms (loading dock, storage, office, secure storage)</li>
 *   <li>Bus Stop — 2 rooms (waiting area, ticket booth)</li>
 *   <li>Rail Station — 7 rooms (entrance, waiting hall, ticket office, platforms, control, storage)</li>
 *   <li>Boat Dock — 6 rooms (entrance, cargo warehouse, passenger terminal, office, captain's, waterfront)</li>
 *   <li>Airport — 7 rooms (terminal, check-in, security, gate, control tower, hangar, maintenance)</li>
 *   <li>Space Port — 10 rooms (entrance, check-in, customs, observation, docking bays, command, hangar, maintenance, fuel)</li>
 *   <li>Inn — 7 rooms (entrance, common room, kitchen, corridor, 3 rooms)</li>
 *   <li>Church/Temple — 4 rooms (entrance, hall, chapel, clergy quarters)</li>
 *   <li>Police Station — 4 rooms (entrance, desk, evidence, holding cells)</li>
 *   <li>Factory — 5 rooms (loading dock, production, raw materials, control, break room)</li>
 * </ul>
 * </p>
 */
public class BuildingInteriorGenerator {

    private final DungeonRoomService dungeonService;
    private final TechLevelService techLevelService;
    private final Random random;

    /**
     * Creates a generator with tech-level support.
     *
     * @param dungeonService the service for creating dungeon rooms
     * @param techLevelService the service for looking up tech-appropriate modules (may be null for backward compatibility)
     * @param seed random seed for reproducible generation
     */
    public BuildingInteriorGenerator(DungeonRoomService dungeonService, TechLevelService techLevelService, long seed) {
        this.dungeonService = dungeonService;
        this.techLevelService = techLevelService;
        this.random = new Random(seed);
    }

    /**
     * Creates a generator without tech-level support (backward compatibility).
     *
     * @param dungeonService the service for creating dungeon rooms
     * @param seed random seed for reproducible generation
     */
    public BuildingInteriorGenerator(DungeonRoomService dungeonService, long seed) {
        this(dungeonService, null, seed);
    }

    // ---- Room Template Definition ----

    /**
     * Defines a single room in a building template.
     * Each room has a generic type that can be looked up in the tech level database
     * to find the tech-appropriate name, description, trap, and treasure.
     */
    private static class RoomTemplate {
        final String roomId;
        final String genericType;
        final String fallbackName;
        final String fallbackDescription;
        final String theme;
        final String size;
        final String trap;
        final Integer trapDc;
        final String treasure;

        RoomTemplate(String roomId, String genericType, String fallbackName, String fallbackDescription,
                     String theme, String size, String trap, Integer trapDc, String treasure) {
            this.roomId = roomId;
            this.genericType = genericType;
            this.fallbackName = fallbackName;
            this.fallbackDescription = fallbackDescription;
            this.theme = theme;
            this.size = size;
            this.trap = trap;
            this.trapDc = trapDc;
            this.treasure = treasure;
        }
    }

    /**
     * Resolves a room template for a given tech level, looking up tech-appropriate
     * name, description, trap, and treasure from the module database.
     * Falls back to hardcoded values if no tech-specific module is found.
     */
    private RoomTemplate resolveRoomTemplate(TechLevel techLevel, RoomTemplate template) {
        if (techLevelService == null || techLevel == null) {
            return template;
        }

        // Look up DUNGEON_ROOM module for this tech level and generic type
        List<PlanetBuildingModuleEntity> modules = techLevelService.getModulesByTechLevelAndCategory(
                techLevel, ModuleCategory.DUNGEON_ROOM);

        String resolvedName = template.fallbackName;
        String resolvedDescription = template.fallbackDescription;
        String resolvedTrap = template.trap;
        Integer resolvedTrapDc = template.trapDc;
        String resolvedTreasure = template.treasure;

        // Try to find a matching module by generic type
        for (PlanetBuildingModuleEntity module : modules) {
            if (module.getGenericType() != null && module.getGenericType().equals(template.genericType)) {
                if (module.getLoreName() != null && !module.getLoreName().isEmpty()) {
                    resolvedName = module.getLoreName();
                }
                if (module.getDescription() != null && !module.getDescription().isEmpty()) {
                    resolvedDescription = module.getDescription();
                }
                // Use module's DC if available and template has a trap
                if (template.trap != null && module.getDc() != null) {
                    resolvedTrapDc = module.getDc();
                }
                break;
            }
        }

        // If no exact match, try to find any module with a similar name
        if (resolvedName.equals(template.fallbackName)) {
            for (PlanetBuildingModuleEntity module : modules) {
                String loreName = module.getLoreName();
                if (loreName != null && loreName.toLowerCase().contains(template.genericType.toLowerCase().replace("_", " ").replace("ROOM", "").trim().toLowerCase())) {
                    resolvedName = loreName;
                    if (module.getDescription() != null && !module.getDescription().isEmpty()) {
                        resolvedDescription = module.getDescription();
                    }
                    break;
                }
            }
        }

        return new RoomTemplate(template.roomId, template.genericType, resolvedName, resolvedDescription,
                template.theme, template.size, resolvedTrap, resolvedTrapDc, resolvedTreasure);
    }

    /**
     * Looks up a tech-appropriate trap name for a given tech level.
     */
    private String resolveTrapName(TechLevel techLevel, String genericTrapType) {
        if (techLevelService == null || techLevel == null || genericTrapType == null) {
            return genericTrapType;
        }

        List<PlanetBuildingModuleEntity> modules = techLevelService.getModulesByTechLevelAndCategory(
                techLevel, ModuleCategory.TRAP);

        for (PlanetBuildingModuleEntity module : modules) {
            if (module.getGenericType() != null && module.getGenericType().equals(genericTrapType)) {
                String loreName = module.getLoreName();
                if (loreName != null && !loreName.isEmpty()) {
                    return loreName;
                }
            }
        }
        return genericTrapType;
    }

    /**
     * Looks up a tech-appropriate treasure name for a given tech level.
     */
    private String resolveTreasureName(TechLevel techLevel, String genericTreasureType) {
        if (techLevelService == null || techLevel == null || genericTreasureType == null) {
            return genericTreasureType;
        }

        List<PlanetBuildingModuleEntity> modules = techLevelService.getModulesByTechLevelAndCategory(
                techLevel, ModuleCategory.TREASURE);

        for (PlanetBuildingModuleEntity module : modules) {
            if (module.getGenericType() != null && module.getGenericType().equals(genericTreasureType)) {
                String loreName = module.getLoreName();
                if (loreName != null && !loreName.isEmpty()) {
                    return loreName;
                }
            }
        }
        return genericTreasureType;
    }

    // ---- Public API (with TechLevel) ----

    /**
     * Generates interior rooms for a settlement based on its size and tech level.
     * <p>
     * Larger settlements get more building types:
     * <ul>
     *   <li>Village: House + Market</li>
     *   <li>Town: House + Market + Town Hall + Inn</li>
     *   <li>City: House + Market + Town Hall + Inn + Church + Police Station + Warehouse</li>
     *   <li>Metropolis: All buildings + Space Port</li>
     * </ul>
     * </p>
     *
     * @param settlement the settlement to generate interiors for
     * @param techLevel the technology level for this settlement (determines room names and features)
     * @return the list of dungeon IDs created (one per building)
     */
    public List<String> generateSettlementInteriors(Settlement settlement, TechLevel techLevel) {
        List<String> dungeonIds = new ArrayList<>();
        String prefix = settlement.getId();

        // Every settlement has at least a house and market
        dungeonIds.add(generateHouse(prefix + "-house-1", techLevel));
        dungeonIds.add(generateMarket(prefix + "-market-1", techLevel));

        switch (settlement.getSize()) {
            case VILLAGE:
                break;
            case TOWN:
                dungeonIds.add(generateTownHall(prefix + "-townhall-1", techLevel));
                dungeonIds.add(generateInn(prefix + "-inn-1", techLevel));
                break;
            case CITY:
                dungeonIds.add(generateTownHall(prefix + "-townhall-1", techLevel));
                dungeonIds.add(generateInn(prefix + "-inn-1", techLevel));
                dungeonIds.add(generateChurch(prefix + "-church-1", techLevel));
                dungeonIds.add(generatePoliceStation(prefix + "-police-1", techLevel));
                dungeonIds.add(generateWarehouse(prefix + "-warehouse-1", techLevel));
                break;
            case METROPOLIS:
                dungeonIds.add(generateTownHall(prefix + "-townhall-1", techLevel));
                dungeonIds.add(generateInn(prefix + "-inn-1", techLevel));
                dungeonIds.add(generateChurch(prefix + "-church-1", techLevel));
                dungeonIds.add(generatePoliceStation(prefix + "-police-1", techLevel));
                dungeonIds.add(generateWarehouse(prefix + "-warehouse-1", techLevel));
                dungeonIds.add(generateSpacePort(prefix + "-spaceport-1", techLevel));
                // Extra houses for metropolis
                for (int i = 2; i <= 5; i++) {
                    dungeonIds.add(generateHouse(prefix + "-house-" + i, techLevel));
                }
                break;
        }

        settlement.setDungeonIds(dungeonIds);
        return dungeonIds;
    }

    /**
     * Generates interior rooms for a settlement using a default tech level (TL2).
     *
     * @param settlement the settlement to generate interiors for
     * @return the list of dungeon IDs created (one per building)
     */
    public List<String> generateSettlementInteriors(Settlement settlement) {
        return generateSettlementInteriors(settlement, TechLevel.TL2);
    }

    /**
     * Generates interior rooms for an industry based on its tech level.
     * <p>
     * All industries use the Factory template with production-specific naming.
     * </p>
     *
     * @param industry the industry to generate interiors for
     * @param techLevel the technology level for this industry
     * @return the dungeon ID created
     */
    public String generateIndustryInterior(Industry industry, TechLevel techLevel) {
        String dungeonId = generateFactory(industry.getId(), techLevel);
        industry.setDungeonId(dungeonId);
        return dungeonId;
    }

    /**
     * Generates interior rooms for an industry using a default tech level (TL2).
     *
     * @param industry the industry to generate interiors for
     * @return the dungeon ID created
     */
    public String generateIndustryInterior(Industry industry) {
        return generateIndustryInterior(industry, TechLevel.TL2);
    }

    /**
     * Generates interior rooms for a station based on its infrastructure type and tech level.
     * <p>
     * The infrastructure type determines the room template:
     * <ul>
     *   <li>STATION → Bus Stop (small) or Rail Station (large)</li>
     *   <li>PORT → Boat Dock</li>
     *   <li>AIRPORT → Airport</li>
     * </ul>
     * </p>
     *
     * @param station the station to generate interiors for
     * @param techLevel the technology level for this station
     * @return the dungeon ID created
     */
    public String generateStationInterior(Station station, TechLevel techLevel) {
        TerrainData.InfrastructureType type = station.getInfrastructureType();
        if (type == null) {
            type = TerrainData.InfrastructureType.STATION;
        }

        String dungeonId;
        switch (type) {
            case PORT:
                dungeonId = generateBoatDock(station.getId(), techLevel);
                break;
            case AIRPORT:
                dungeonId = generateAirport(station.getId(), techLevel);
                break;
            case STATION:
            default:
                // Use rail station for larger stations, bus stop for smaller
                if (station.getCatchmentArea().size() > 3) {
                    dungeonId = generateRailStation(station.getId(), techLevel);
                } else {
                    dungeonId = generateBusStop(station.getId(), techLevel);
                }
                break;
        }
        station.setDungeonId(dungeonId);
        return dungeonId;
    }

    /**
     * Generates interior rooms for a station using a default tech level (TL2).
     *
     * @param station the station to generate interiors for
     * @return the dungeon ID created
     */
    public String generateStationInterior(Station station) {
        return generateStationInterior(station, TechLevel.TL2);
    }

    // ---- Room Template Generators (Tech Level Aware) ----

    /**
     * Residential House: 5 rooms
     * RH-01 Front Porch → RH-02 Living Room → RH-03 Kitchen, RH-04 Bedroom → RH-05 Storage
     */
    public String generateHouse(String dungeonId, TechLevel techLevel) {
        RoomTemplate[] rooms = {
            new RoomTemplate("RH-01", "PORCH", "Front Porch", "A welcoming porch with a bench and potted plants.", "Social", "Small", null, null, null),
            new RoomTemplate("RH-02", "LIVING_QUARTERS", "Living Room", "A cozy living room with a fireplace and comfortable furniture.", "Social", "Medium", null, null, null),
            new RoomTemplate("RH-03", "KITCHEN", "Kitchen", "A well-equipped kitchen with a large table for meals.", "Social", "Small", null, null, null),
            new RoomTemplate("RH-04", "BEDROOM", "Bedroom", "A quiet bedroom with a bed and personal belongings.", "Social", "Small", null, null, "Hidden Cache (Common)"),
            new RoomTemplate("RH-05", "STORAGE_CLOSET", "Storage Closet", "A small storage closet with shelves and supplies.", "Storage", "Small", null, null, null)
        };
        Long[] ids = resolveAndCreate(dungeonId, techLevel, rooms);

        // Connections: Porch → Living Room → Kitchen, Bedroom → Storage
        connect(ids[0], ids[1]);
        connect(ids[1], ids[2]);
        connect(ids[1], ids[3]);
        connect(ids[3], ids[4]);

        return dungeonId;
    }

    public String generateHouse(String dungeonId) {
        return generateHouse(dungeonId, TechLevel.TL2);
    }

    /**
     * Town Hall: 5 rooms
     * TH-01 Grand Entrance → TH-02 Council Chamber → TH-03 Records, TH-04 Mayor's Office → TH-05 Vault
     */
    public String generateTownHall(String dungeonId, TechLevel techLevel) {
        RoomTemplate[] rooms = {
            new RoomTemplate("TH-01", "GRAND_ENTRANCE", "Grand Entrance", "A grand entrance hall with marble columns and a reception desk.", "Social", "Large", null, null, null),
            new RoomTemplate("TH-02", "COUNCIL_CHAMBER", "Council Chamber", "The council chamber where town decisions are made.", "Social", "Large", null, null, "NPC Dialogue"),
            new RoomTemplate("TH-03", "RECORDS_OFFICE", "Public Records", "Public records office with filing cabinets and documents.", "Social", "Medium", null, null, null),
            new RoomTemplate("TH-04", "MAYORS_OFFICE", "Mayor's Office", "The mayor's private office with a desk and portraits.", "Social", "Medium", null, null, "Quest Giver"),
            new RoomTemplate("TH-05", "VAULT", "Vault", "A secure vault containing the town's gold reserves.", "Storage", "Small", "Lock Trap", 15, "Gold Reserve (Rare)")
        };
        Long[] ids = resolveAndCreate(dungeonId, techLevel, rooms);

        connect(ids[0], ids[1]);
        connect(ids[0], ids[2]);
        connect(ids[1], ids[3]);
        connect(ids[3], ids[4]);

        return dungeonId;
    }

    public String generateTownHall(String dungeonId) {
        return generateTownHall(dungeonId, TechLevel.TL2);
    }

    /**
     * Market: 4 rooms
     * MK-01 Entrance → MK-02 Food Stalls, MK-03 Goods Stalls, MK-04 Auction House
     */
    public String generateMarket(String dungeonId, TechLevel techLevel) {
        RoomTemplate[] rooms = {
            new RoomTemplate("MK-01", "MARKET_ENTRANCE", "Market Entrance", "The bustling entrance to the market with colorful banners.", "Social", "Large", null, null, null),
            new RoomTemplate("MK-02", "FOOD_STALL", "Food Stall Area", "Fresh food stalls offering bread, meat, and fruits.", "Social", "Medium", null, null, "Merchant NPC"),
            new RoomTemplate("MK-03", "GOODS_STALL", "Goods Stall Area", "Various goods stalls selling tools, clothes, and trinkets.", "Social", "Medium", null, null, "Merchant NPC"),
            new RoomTemplate("MK-04", "AUCTION_HOUSE", "Auction House", "An auction house where rare items are sold to the highest bidder.", "Social", "Medium", null, null, "Rare Item (Uncommon)")
        };
        Long[] ids = resolveAndCreate(dungeonId, techLevel, rooms);

        connect(ids[0], ids[1]);
        connect(ids[0], ids[2]);
        connect(ids[0], ids[3]);

        return dungeonId;
    }

    public String generateMarket(String dungeonId) {
        return generateMarket(dungeonId, TechLevel.TL2);
    }

    /**
     * Warehouse: 4 rooms
     * WH-01 Loading Dock → WH-02 Storage, WH-03 Office → WH-04 Secure Storage
     */
    public String generateWarehouse(String dungeonId, TechLevel techLevel) {
        RoomTemplate[] rooms = {
            new RoomTemplate("WH-01", "LOADING_DOCK", "Loading Dock", "A large loading dock with ramps and cargo carts.", "Storage", "Large", null, null, null),
            new RoomTemplate("WH-02", "MAIN_STORAGE", "Main Storage", "Rows of shelves stacked with goods and supplies.", "Storage", "Large", null, null, null),
            new RoomTemplate("WH-03", "OFFICE", "Office", "The warehouse manager's office with ledgers and maps.", "Social", "Small", null, null, "Clerk NPC"),
            new RoomTemplate("WH-04", "SECURE_STORAGE", "Secure Storage", "A locked storage area for valuable cargo.", "Storage", "Medium", "Lock Trap", 12, "Valuable Cargo (Uncommon)")
        };
        Long[] ids = resolveAndCreate(dungeonId, techLevel, rooms);

        connect(ids[0], ids[1]);
        connect(ids[0], ids[2]);
        connect(ids[1], ids[3]);

        return dungeonId;
    }

    public String generateWarehouse(String dungeonId) {
        return generateWarehouse(dungeonId, TechLevel.TL2);
    }

    /**
     * Bus Stop: 2 rooms
     * BS-01 Waiting Area → BS-02 Ticket Booth
     */
    public String generateBusStop(String dungeonId, TechLevel techLevel) {
        RoomTemplate[] rooms = {
            new RoomTemplate("BS-01", "WAITING_AREA", "Waiting Area", "A small sheltered waiting area with benches.", "Social", "Small", null, null, null),
            new RoomTemplate("BS-02", "TICKET_BOOTH", "Ticket Booth", "A ticket booth where passengers purchase fares.", "Social", "Small", null, null, "Ticket Agent NPC")
        };
        Long[] ids = resolveAndCreate(dungeonId, techLevel, rooms);

        connect(ids[0], ids[1]);

        return dungeonId;
    }

    public String generateBusStop(String dungeonId) {
        return generateBusStop(dungeonId, TechLevel.TL2);
    }

    /**
     * Rail Station: 7 rooms
     * RS-01 Entrance → RS-02 Waiting Hall → RS-03 Ticket Office, RS-04 Platform A, RS-05 Platform B
     * RS-04 → RS-06 Control Room → RS-07 Storage
     */
    public String generateRailStation(String dungeonId, TechLevel techLevel) {
        RoomTemplate[] rooms = {
            new RoomTemplate("RS-01", "MAIN_ENTRANCE", "Main Entrance", "The grand entrance to the rail station with a large clock.", "Social", "Large", null, null, null),
            new RoomTemplate("RS-02", "WAITING_HALL", "Waiting Hall", "A spacious waiting hall with seating and departure boards.", "Social", "Large", null, null, null),
            new RoomTemplate("RS-03", "TICKET_OFFICE", "Ticket Office", "The ticket office where passengers buy and collect tickets.", "Social", "Medium", null, null, "Ticket Agent NPC"),
            new RoomTemplate("RS-04", "PLATFORM", "Platform A", "Platform A with tracks and a waiting train.", "Social", "Large", null, null, "Train Arrival Event"),
            new RoomTemplate("RS-05", "PLATFORM", "Platform B", "Platform B with tracks and departing trains.", "Social", "Large", null, null, "Train Arrival Event"),
            new RoomTemplate("RS-06", "CONTROL_ROOM", "Control Room", "The station control room with signals and switches.", "Social", "Small", null, null, "Station Master NPC"),
            new RoomTemplate("RS-07", "STORAGE_ROOM", "Storage Room", "A storage room for station supplies and equipment.", "Storage", "Small", null, null, null)
        };
        Long[] ids = resolveAndCreate(dungeonId, techLevel, rooms);

        connect(ids[0], ids[1]);
        connect(ids[0], ids[2]);
        connect(ids[1], ids[3]);
        connect(ids[1], ids[4]);
        connect(ids[3], ids[5]);
        connect(ids[5], ids[6]);

        return dungeonId;
    }

    public String generateRailStation(String dungeonId) {
        return generateRailStation(dungeonId, TechLevel.TL2);
    }

    /**
     * Boat Dock: 6 rooms
     * BD-01 Entrance → BD-02 Cargo Warehouse, BD-03 Passenger Terminal
     * BD-02 → BD-04 Dock Office, BD-03 → BD-05 Captain's Quarters
     * BD-02, BD-03 → BD-06 Waterfront
     */
    public String generateBoatDock(String dungeonId, TechLevel techLevel) {
        RoomTemplate[] rooms = {
            new RoomTemplate("BD-01", "DOCK_ENTRANCE", "Dock Entrance", "The entrance to the boat dock with the smell of salt air.", "Social", "Large", null, null, null),
            new RoomTemplate("BD-02", "CARGO_WAREHOUSE", "Cargo Warehouse", "A warehouse for cargo being loaded onto ships.", "Storage", "Large", null, null, null),
            new RoomTemplate("BD-03", "PASSENGER_TERMINAL", "Passenger Terminal", "The passenger terminal with departure and arrival boards.", "Social", "Medium", null, null, null),
            new RoomTemplate("BD-04", "DOCK_OFFICE", "Dock Office", "The dock master's office with shipping manifests.", "Social", "Small", null, null, "Dock Master NPC"),
            new RoomTemplate("BD-05", "CAPTAINS_QUARTERS", "Captain's Quarters", "The captain's private quarters with a treasure map on the wall.", "Social", "Small", null, null, "Treasure Map (Uncommon)"),
            new RoomTemplate("BD-06", "WATERFRONT", "Waterfront", "The open waterfront where ships dock and unload.", "Social", "Large", null, null, "Sea Monster Event (Rare)")
        };
        Long[] ids = resolveAndCreate(dungeonId, techLevel, rooms);

        connect(ids[0], ids[1]);
        connect(ids[0], ids[2]);
        connect(ids[1], ids[3]);
        connect(ids[2], ids[4]);
        connect(ids[1], ids[5]);
        connect(ids[2], ids[5]);

        return dungeonId;
    }

    public String generateBoatDock(String dungeonId) {
        return generateBoatDock(dungeonId, TechLevel.TL2);
    }

    /**
     * Airport: 7 rooms
     * AP-01 Terminal → AP-02 Check-in, AP-03 Security, AP-04 Gate
     * AP-02 → AP-05 Control Tower, AP-04 → AP-06 Hangar → AP-07 Maintenance
     */
    public String generateAirport(String dungeonId, TechLevel techLevel) {
        RoomTemplate[] rooms = {
            new RoomTemplate("AP-01", "MAIN_TERMINAL", "Main Terminal", "The main airport terminal with large windows overlooking the runway.", "Social", "Large", null, null, null),
            new RoomTemplate("AP-02", "CHECKIN_HALL", "Check-in Hall", "The check-in hall with counters and luggage belts.", "Social", "Large", null, null, "Check-in Agent NPC"),
            new RoomTemplate("AP-03", "SECURITY_CHECK", "Security Check", "Security screening with metal detectors and scanners.", "Combat", "Medium", "Scanner Trap", 10, null),
            new RoomTemplate("AP-04", "GATE_AREA", "Gate Area", "The gate area where passengers board aircraft.", "Social", "Large", null, null, "Boarding Event"),
            new RoomTemplate("AP-05", "CONTROL_TOWER", "Control Tower", "The air traffic control tower with panoramic views.", "Social", "Small", null, null, "Air Traffic Controller NPC"),
            new RoomTemplate("AP-06", "HANGAR", "Hangar", "A large hangar housing aircraft for maintenance.", "Storage", "Large", null, null, null),
            new RoomTemplate("AP-07", "MAINTENANCE_BAY", "Maintenance Bay", "The maintenance bay with tools and spare parts.", "Crafting", "Medium", null, null, "Mechanic NPC")
        };
        Long[] ids = resolveAndCreate(dungeonId, techLevel, rooms);

        connect(ids[0], ids[1]);
        connect(ids[0], ids[2]);
        connect(ids[0], ids[3]);
        connect(ids[1], ids[4]);
        connect(ids[3], ids[5]);
        connect(ids[5], ids[6]);

        return dungeonId;
    }

    public String generateAirport(String dungeonId) {
        return generateAirport(dungeonId, TechLevel.TL2);
    }

    /**
     * Space Port: 10 rooms
     * SP-01 Grand Entrance → SP-02 Check-in, SP-03 Customs, SP-04 Observation
     * SP-02 → SP-05 Docking Bay Alpha, SP-06 Docking Bay Beta
     * SP-05 → SP-07 Command, SP-08 Hangar → SP-09 Maintenance → SP-10 Fuel Station
     */
    public String generateSpacePort(String dungeonId, TechLevel techLevel) {
        RoomTemplate[] rooms = {
            new RoomTemplate("SP-01", "GRAND_ENTRANCE", "Grand Entrance", "A massive entrance hall with holographic displays and alien visitors.", "Social", "Huge", null, null, null),
            new RoomTemplate("SP-02", "CHECKIN_HALL", "Check-in Hall", "The check-in hall with multi-species service counters.", "Social", "Large", null, null, "Check-in Agent NPC"),
            new RoomTemplate("SP-03", "CUSTOMS_SECURITY", "Customs & Security", "Customs and security with advanced scanning technology.", "Combat", "Large", "Scanner Trap", 15, null),
            new RoomTemplate("SP-04", "OBSERVATION_DECK", "Observation Deck", "An observation deck with a view of space and docked ships.", "Social", "Large", null, null, "Space View Event"),
            new RoomTemplate("SP-05", "DOCKING_BAY", "Docking Bay Alpha", "Docking Bay Alpha where starships arrive from distant systems.", "Social", "Huge", null, null, "Ship Arrival Event"),
            new RoomTemplate("SP-06", "DOCKING_BAY", "Docking Bay Beta", "Docking Bay Beta where starships depart for new worlds.", "Social", "Huge", null, null, "Ship Departure Event"),
            new RoomTemplate("SP-07", "COMMAND_CENTER", "Command Center", "The space port command center monitoring all operations.", "Social", "Medium", null, null, "Port Authority NPC"),
            new RoomTemplate("SP-08", "HANGAR", "Hangar", "A massive hangar for ship repairs and refueling.", "Storage", "Huge", null, null, null),
            new RoomTemplate("SP-09", "MAINTENANCE_BAY", "Maintenance Bay", "The maintenance bay with advanced engineering equipment.", "Crafting", "Large", null, null, "Engineer NPC"),
            new RoomTemplate("SP-10", "FUEL_STATION", "Fuel Station", "The fuel station with volatile propellant tanks.", "Storage", "Medium", "Explosion Trap", 18, null)
        };
        Long[] ids = resolveAndCreate(dungeonId, techLevel, rooms);

        connect(ids[0], ids[1]);
        connect(ids[0], ids[2]);
        connect(ids[0], ids[3]);
        connect(ids[1], ids[4]);
        connect(ids[1], ids[5]);
        connect(ids[4], ids[6]);
        connect(ids[4], ids[7]);
        connect(ids[7], ids[8]);
        connect(ids[8], ids[9]);

        return dungeonId;
    }

    public String generateSpacePort(String dungeonId) {
        return generateSpacePort(dungeonId, TechLevel.TL2);
    }

    /**
     * Inn: 7 rooms
     * IN-01 Entrance → IN-02 Common Room, IN-03 Kitchen
     * IN-02 → IN-04 Corridor → IN-05 Room 1, IN-06 Room 2, IN-07 Room 3
     */
    public String generateInn(String dungeonId, TechLevel techLevel) {
        RoomTemplate[] rooms = {
            new RoomTemplate("IN-01", "INN_ENTRANCE", "Entrance", "The inn entrance with a bell above the door.", "Social", "Medium", null, null, null),
            new RoomTemplate("IN-02", "COMMON_ROOM", "Common Room", "A lively common room with a bar, fireplace, and rumor traders.", "Social", "Large", null, null, "Rumor Gossip Event"),
            new RoomTemplate("IN-03", "KITCHEN", "Kitchen", "The inn kitchen where meals are prepared.", "Social", "Medium", null, null, "Cook NPC"),
            new RoomTemplate("IN-04", "CORRIDOR", "Room Corridor", "A long corridor leading to the guest rooms.", "Social", "Medium", null, null, null),
            new RoomTemplate("IN-05", "GUEST_ROOM", "Room 1", "A guest room with a single bed and a hidden stash.", "Social", "Small", null, null, "Hidden Stash (Common)"),
            new RoomTemplate("IN-06", "GUEST_ROOM", "Room 2", "A guest room with twin beds and a window.", "Social", "Small", null, null, null),
            new RoomTemplate("IN-07", "GUEST_ROOM", "Room 3", "A guest room with a double bed and a desk.", "Social", "Small", null, null, null)
        };
        Long[] ids = resolveAndCreate(dungeonId, techLevel, rooms);

        connect(ids[0], ids[1]);
        connect(ids[0], ids[2]);
        connect(ids[1], ids[3]);
        connect(ids[3], ids[4]);
        connect(ids[3], ids[5]);
        connect(ids[3], ids[6]);

        return dungeonId;
    }

    public String generateInn(String dungeonId) {
        return generateInn(dungeonId, TechLevel.TL2);
    }

    /**
     * Church/Temple: 4 rooms
     * CH-01 Entrance → CH-02 Main Hall → CH-03 Chapel, CH-04 Clergy Quarters
     */
    public String generateChurch(String dungeonId, TechLevel techLevel) {
        RoomTemplate[] rooms = {
            new RoomTemplate("CH-01", "TEMPLE_ENTRANCE", "Entrance", "The church entrance with stained glass windows.", "Social", "Medium", null, null, null),
            new RoomTemplate("CH-02", "MAIN_HALL", "Main Hall", "The main hall with pews and an altar.", "Social", "Large", null, null, "Prayer Event"),
            new RoomTemplate("CH-03", "CHAPEL", "Chapel", "A small chapel with a holy relic on display.", "Social", "Small", null, null, "Holy Relic (Rare)"),
            new RoomTemplate("CH-04", "CLERGY_QUARTERS", "Clergy Quarters", "The clergy quarters where priests reside.", "Social", "Small", null, null, "Priest NPC")
        };
        Long[] ids = resolveAndCreate(dungeonId, techLevel, rooms);

        connect(ids[0], ids[1]);
        connect(ids[1], ids[2]);
        connect(ids[1], ids[3]);

        return dungeonId;
    }

    public String generateChurch(String dungeonId) {
        return generateChurch(dungeonId, TechLevel.TL2);
    }

    /**
     * Police Station: 4 rooms
     * PS-01 Entrance → PS-02 Main Desk, PS-03 Evidence
     * PS-02 → PS-04 Holding Cells
     */
    public String generatePoliceStation(String dungeonId, TechLevel techLevel) {
        RoomTemplate[] rooms = {
            new RoomTemplate("PS-01", "STATION_ENTRANCE", "Entrance", "The police station entrance with a reinforced door.", "Social", "Medium", null, null, null),
            new RoomTemplate("PS-02", "MAIN_DESK", "Main Desk", "The main desk where officers handle reports.", "Social", "Medium", null, null, "Officer NPC"),
            new RoomTemplate("PS-03", "EVIDENCE_ROOM", "Evidence Room", "A locked evidence room with stolen goods and case files.", "Storage", "Small", "Lock Trap", 14, "Stolen Goods (Uncommon)"),
            new RoomTemplate("PS-04", "HOLDING_CELLS", "Holding Cells", "Secure holding cells for detained suspects.", "Combat", "Medium", "Alarm Trap", 12, null)
        };
        Long[] ids = resolveAndCreate(dungeonId, techLevel, rooms);

        connect(ids[0], ids[1]);
        connect(ids[0], ids[2]);
        connect(ids[1], ids[3]);

        return dungeonId;
    }

    public String generatePoliceStation(String dungeonId) {
        return generatePoliceStation(dungeonId, TechLevel.TL2);
    }

    /**
     * Factory: 5 rooms
     * FC-01 Loading Dock → FC-02 Production Floor, FC-03 Raw Materials
     * FC-02 → FC-04 Control Room, FC-05 Break Room
     */
    public String generateFactory(String dungeonId, TechLevel techLevel) {
        RoomTemplate[] rooms = {
            new RoomTemplate("FC-01", "LOADING_DOCK", "Loading Dock", "The factory loading dock with conveyor belts and forklifts.", "Storage", "Large", null, null, null),
            new RoomTemplate("FC-02", "PRODUCTION_FLOOR", "Production Floor", "The main production floor with machinery and assembly lines.", "Industry", "Large", "Machinery Trap", 13, null),
            new RoomTemplate("FC-03", "RAW_MATERIAL_STORAGE", "Raw Material Storage", "Storage area for raw materials waiting to be processed.", "Storage", "Medium", null, null, null),
            new RoomTemplate("FC-04", "CONTROL_ROOM", "Control Room", "The factory control room monitoring production metrics.", "Social", "Small", null, null, "Foreman NPC"),
            new RoomTemplate("FC-05", "BREAK_ROOM", "Break Room", "The workers' break room with a vending machine and tables.", "Social", "Small", null, null, "Worker NPC")
        };
        Long[] ids = resolveAndCreate(dungeonId, techLevel, rooms);

        connect(ids[0], ids[1]);
        connect(ids[0], ids[2]);
        connect(ids[1], ids[3]);
        connect(ids[1], ids[4]);

        return dungeonId;
    }

    public String generateFactory(String dungeonId) {
        return generateFactory(dungeonId, TechLevel.TL2);
    }

    /**
     * Apartment Building: Full 3D blueprint for character homes at all settlements.
     * <p>
     * This is the residential building where new characters can find a home. It serves
     * as the starting point for player characters, providing apartment units, common areas,
     * and basement dungeon instances for exploration.
     * </p>
     * <p>
     * Structure (40 rooms across multiple floors):
     * <ul>
     *   <li>Ground Floor: Lobby, Reception, Mail Room, Package Lockers, Security Office</li>
     *   <li>Floor 1: 3x Apartment Units (Studio, 1-Bedroom, 2-Bedroom)</li>
     *   <li>Floor 2: 3x Apartment Units (1-Bedroom, 2-Bedroom, Penthouse Suite)</li>
     *   <li>Floor 3: 3x Apartment Units (Garden View, City View, Executive Suite)</li>
     *   <li>Floor 4: 2x Apartment Units (Luxury Suite, Family Apartment)</li>
     *   <li>Common Areas: Gym, Laundry Room, Rooftop Terrace, Community Room</li>
     *   <li>Basement B1: Storage, Maintenance, Utility Room</li>
     *   <li>Basement B2: Dungeon Hub, Training Room, Secret Passage</li>
     *   <li>Basement B3: Deep Dungeon, Treasure Vault, Emergency Exit</li>
     * </ul>
     * </p>
     *
     * @param dungeonId the base dungeon ID for this apartment building
     * @param techLevel the technology level of the settlement
     * @return the dungeon ID created
     */
    public String generateApartmentBuilding(String dungeonId, TechLevel techLevel) {
        RoomTemplate[] rooms = {
            // === Ground Floor ===
            new RoomTemplate("AB-01", "LOBBY", "Main Lobby",
                    "A spacious lobby with a reception desk, seating area, and potted plants.",
                    "Social", "Large", null, null, "Concierge NPC"),
            new RoomTemplate("AB-02", "RECEPTION", "Reception Desk",
                    "The reception desk where tenants check in and receive packages.",
                    "Social", "Small", null, null, "Receptionist NPC"),
            new RoomTemplate("AB-03", "MAIL_ROOM", "Mail Room",
                    "A mail room with individual mailboxes for each apartment unit.",
                    "Storage", "Small", null, null, null),
            new RoomTemplate("AB-04", "PACKAGE_LOCKERS", "Package Lockers",
                    "Automated package lockers for secure delivery storage.",
                    "Storage", "Small", null, null, null),
            new RoomTemplate("AB-05", "SECURITY_OFFICE", "Security Office",
                    "The building security office with monitors and access logs.",
                    "Social", "Small", null, null, "Security Guard NPC"),

            // === Floor 1: Studio & 1-Bedroom Units ===
            new RoomTemplate("AB-06", "APARTMENT_UNIT", "Studio Apartment 1A",
                    "A cozy studio apartment with a kitchenette and small bathroom.",
                    "Social", "Small", null, null, "Tenant NPC"),
            new RoomTemplate("AB-07", "APARTMENT_UNIT", "1-Bedroom Apartment 1B",
                    "A one-bedroom apartment with a separate living room and kitchen.",
                    "Social", "Medium", null, null, "Tenant NPC"),
            new RoomTemplate("AB-08", "APARTMENT_UNIT", "2-Bedroom Apartment 1C",
                    "A two-bedroom apartment with a balcony and full kitchen.",
                    "Social", "Medium", null, null, "Tenant NPC"),

            // === Floor 2: 1-Bedroom & 2-Bedroom Units ===
            new RoomTemplate("AB-09", "APARTMENT_UNIT", "1-Bedroom Apartment 2A",
                    "A one-bedroom apartment on the second floor with city views.",
                    "Social", "Medium", null, null, "Tenant NPC"),
            new RoomTemplate("AB-10", "APARTMENT_UNIT", "2-Bedroom Apartment 2B",
                    "A spacious two-bedroom apartment with a dining area.",
                    "Social", "Medium", null, null, "Tenant NPC"),
            new RoomTemplate("AB-11", "APARTMENT_UNIT", "Penthouse Suite 2C",
                    "A luxury penthouse suite with premium finishes and panoramic views.",
                    "Social", "Large", null, null, "Wealthy Tenant NPC"),

            // === Floor 3: Garden & City View Units ===
            new RoomTemplate("AB-12", "APARTMENT_UNIT", "Garden View Apartment 3A",
                    "An apartment overlooking the garden courtyard with natural light.",
                    "Social", "Medium", null, null, "Tenant NPC"),
            new RoomTemplate("AB-13", "APARTMENT_UNIT", "City View Apartment 3B",
                    "An apartment with floor-to-ceiling windows and city skyline views.",
                    "Social", "Medium", null, null, "Tenant NPC"),
            new RoomTemplate("AB-14", "APARTMENT_UNIT", "Executive Suite 3C",
                    "An executive suite with a home office and premium amenities.",
                    "Social", "Large", null, null, "Executive Tenant NPC"),

            // === Floor 4: Luxury & Family Units ===
            new RoomTemplate("AB-15", "APARTMENT_UNIT", "Luxury Suite 4A",
                    "A top-floor luxury suite with a private terrace and jacuzzi.",
                    "Social", "Large", null, null, "VIP Tenant NPC"),
            new RoomTemplate("AB-16", "APARTMENT_UNIT", "Family Apartment 4B",
                    "A large family apartment with three bedrooms and a playroom.",
                    "Social", "Large", null, null, "Family NPC"),

            // === Common Areas ===
            new RoomTemplate("AB-17", "GYM", "Fitness Center",
                    "A well-equipped fitness center with cardio and weight machines.",
                    "Social", "Medium", null, null, "Trainer NPC"),
            new RoomTemplate("AB-18", "LAUNDRY", "Laundry Room",
                    "A laundry room with washers, dryers, and folding stations.",
                    "Social", "Small", null, null, null),
            new RoomTemplate("AB-19", "ROOFTOP_TERRACE", "Rooftop Terrace",
                    "An open rooftop terrace with seating, grills, and city views.",
                    "Social", "Large", null, null, null),
            new RoomTemplate("AB-20", "COMMUNITY_ROOM", "Community Room",
                    "A community room for tenant events, meetings, and gatherings.",
                    "Social", "Medium", null, null, "Community Manager NPC"),

            // === Basement B1: Storage & Maintenance ===
            new RoomTemplate("AB-21", "STORAGE_ROOM", "Basement Storage",
                    "Rows of storage lockers and shelves for tenant belongings.",
                    "Storage", "Medium", null, null, null),
            new RoomTemplate("AB-22", "MAINTENANCE", "Maintenance Bay",
                    "A maintenance bay with tools, generators, and building systems.",
                    "Crafting", "Medium", null, null, "Maintenance Worker NPC"),
            new RoomTemplate("AB-23", "UTILITY_ROOM", "Utility Room",
                    "The building's utility room with electrical panels and water heaters.",
                    "Storage", "Small", "Electric Trap", 10, null),

            // === Basement B2: Dungeon Hub ===
            new RoomTemplate("AB-24", "BASEMENT_ENTRANCE", "Basement Entrance",
                    "A dimly lit entrance to the underground levels with flickering lights.",
                    "Combat", "Small", null, null, null),
            new RoomTemplate("AB-25", "DUNGEON_HUB", "Dungeon Hub",
                    "The central hub of the basement dungeon with multiple corridors leading deeper.",
                    "Combat", "Large", "Alarm Trap", 14, null),
            new RoomTemplate("AB-26", "TRAINING_ROOM", "Training Room",
                    "A hidden training room used by underground fighters.",
                    "Combat", "Medium", null, null, "Fighter NPC"),
            new RoomTemplate("AB-27", "SECRET_PASSAGE", "Secret Passage",
                    "A hidden passage leading to underground tunnels beneath the city.",
                    "Combat", "Small", "Collapse Trap", 12, "Escape Route"),

            // === Basement B3: Deep Dungeon ===
            new RoomTemplate("AB-28", "DEEP_DUNGEON", "Deep Dungeon",
                    "The deepest level of the basement dungeon, dark and foreboding.",
                    "Combat", "Large", "Poison Trap", 16, "Ancient Relic (Rare)"),
            new RoomTemplate("AB-29", "TREASURE_VAULT", "Treasure Vault",
                    "A hidden vault containing treasures from previous tenants.",
                    "Storage", "Medium", "Lock Trap", 18, "Treasure Cache (Uncommon)"),
            new RoomTemplate("AB-30", "EMERGENCY_EXIT", "Emergency Exit",
                    "An emergency exit leading to the streets below.",
                    "Combat", "Small", null, null, null),

            // === Additional Rooms ===
            new RoomTemplate("AB-31", "ELEVATOR", "Elevator",
                    "An elevator connecting all floors of the building.",
                    "Social", "Small", null, null, null),
            new RoomTemplate("AB-32", "HALLWAY", "Floor 1 Hallway",
                    "The hallway connecting apartments on the first floor.",
                    "Social", "Small", null, null, null),
            new RoomTemplate("AB-33", "HALLWAY", "Floor 2 Hallway",
                    "The hallway connecting apartments on the second floor.",
                    "Social", "Small", null, null, null),
            new RoomTemplate("AB-34", "HALLWAY", "Floor 3 Hallway",
                    "The hallway connecting apartments on the third floor.",
                    "Social", "Small", null, null, null),
            new RoomTemplate("AB-35", "HALLWAY", "Floor 4 Hallway",
                    "The hallway connecting apartments on the top floor.",
                    "Social", "Small", null, null, null),
            new RoomTemplate("AB-36", "KITCHEN", "Shared Kitchen",
                    "A shared kitchen for tenants who prefer communal cooking.",
                    "Social", "Medium", null, null, null),
            new RoomTemplate("AB-37", "PLAYROOM", "Children's Playroom",
                    "A playroom for children with toys and games.",
                    "Social", "Small", null, null, null),
            new RoomTemplate("AB-38", "MEDICAL_BAY", "First Aid Station",
                    "A small first aid station for minor injuries and emergencies.",
                    "Social", "Small", null, null, "Nurse NPC"),
            new RoomTemplate("AB-39", "CAFETERIA", "Building Cafeteria",
                    "A small cafeteria serving meals and snacks to residents.",
                    "Social", "Medium", null, null, "Chef NPC"),
            new RoomTemplate("AB-40", "LIBRARY", "Building Library",
                    "A quiet library with books, magazines, and reading nooks.",
                    "Social", "Medium", null, null, "Librarian NPC")
        };
        Long[] ids = resolveAndCreate(dungeonId, techLevel, rooms);

        // === Ground Floor Connections ===
        connect(ids[0], ids[1]);   // Lobby → Reception
        connect(ids[0], ids[2]);   // Lobby → Mail Room
        connect(ids[0], ids[3]);   // Lobby → Package Lockers
        connect(ids[0], ids[4]);   // Lobby → Security Office
        connect(ids[0], ids[30]);  // Lobby → Elevator

        // === Floor 1: Studio & 1-Bedroom Units ===
        connect(ids[30], ids[5]);  // Elevator → Studio 1A
        connect(ids[30], ids[6]);  // Elevator → 1-Bedroom 1B
        connect(ids[30], ids[7]);  // Elevator → 2-Bedroom 1C
        connect(ids[5], ids[31]);  // Studio 1A → Hallway
        connect(ids[6], ids[31]);  // 1-Bedroom 1B → Hallway
        connect(ids[7], ids[31]);  // 2-Bedroom 1C → Hallway

        // === Floor 2: 1-Bedroom & 2-Bedroom Units ===
        connect(ids[30], ids[8]);  // Elevator → 1-Bedroom 2A
        connect(ids[30], ids[9]);  // Elevator → 2-Bedroom 2B
        connect(ids[30], ids[10]); // Elevator → Penthouse 2C
        connect(ids[8], ids[32]);  // 1-Bedroom 2A → Hallway
        connect(ids[9], ids[32]);  // 2-Bedroom 2B → Hallway
        connect(ids[10], ids[32]); // Penthouse 2C → Hallway

        // === Floor 3: Garden & City View Units ===
        connect(ids[30], ids[11]); // Elevator → Garden View 3A
        connect(ids[30], ids[12]); // Elevator → City View 3B
        connect(ids[30], ids[13]); // Elevator → Executive Suite 3C
        connect(ids[11], ids[33]); // Garden View 3A → Hallway
        connect(ids[12], ids[33]); // City View 3B → Hallway
        connect(ids[13], ids[33]); // Executive Suite 3C → Hallway

        // === Floor 4: Luxury & Family Units ===
        connect(ids[30], ids[14]); // Elevator → Luxury Suite 4A
        connect(ids[30], ids[15]); // Elevator → Family Apartment 4B
        connect(ids[14], ids[34]); // Luxury Suite 4A → Hallway
        connect(ids[15], ids[34]); // Family Apartment 4B → Hallway

        // === Common Areas ===
        connect(ids[0], ids[16]);  // Lobby → Gym
        connect(ids[0], ids[17]);  // Lobby → Laundry
        connect(ids[0], ids[18]);  // Lobby → Rooftop Terrace
        connect(ids[0], ids[19]);  // Lobby → Community Room
        connect(ids[16], ids[35]); // Gym → Shared Kitchen
        connect(ids[19], ids[36]); // Community Room → Playroom
        connect(ids[19], ids[37]); // Community Room → First Aid
        connect(ids[19], ids[38]); // Community Room → Cafeteria
        connect(ids[19], ids[39]); // Community Room → Library

        // === Basement B1: Storage & Maintenance ===
        connect(ids[0], ids[23]);  // Lobby → Basement Entrance
        connect(ids[23], ids[20]); // Basement → Storage
        connect(ids[23], ids[21]); // Basement → Maintenance
        connect(ids[23], ids[22]); // Basement → Utility

        // === Basement B2: Dungeon Hub ===
        connect(ids[23], ids[24]); // Basement → Dungeon Hub
        connect(ids[24], ids[25]); // Dungeon Hub → Training Room
        connect(ids[24], ids[26]); // Dungeon Hub → Secret Passage

        // === Basement B3: Deep Dungeon ===
        connect(ids[24], ids[27]); // Dungeon Hub → Deep Dungeon
        connect(ids[24], ids[28]); // Dungeon Hub → Treasure Vault
        connect(ids[26], ids[29]); // Secret Passage → Emergency Exit

        return dungeonId;
    }

    public String generateApartmentBuilding(String dungeonId) {
        return generateApartmentBuilding(dungeonId, TechLevel.TL2);
    }

    /**
     * Younger Industries HQ Hub: Full 3D blueprint for major capital landing locations.
     * <p>
     * This is the primary corporate headquarters for Younger Industries on each major
     * capital city. It serves as the landing point for characters, the hub for corporate
     * operations, and the gateway to basement dungeon instances.
     * </p>
     * <p>
     * Structure (25 rooms across multiple floors):
     * <ul>
     *   <li>Ground Floor: Grand Lobby, Reception, Security Checkpoint, Mail Center</li>
     *   <li>Upper Floors (1-3): Executive Offices, R&D Labs, Conference Rooms, Break Rooms</li>
     *   <li>Roof: Helipad/Landing Pad, Server Room</li>
     *   <li>Basement (B1-B3): Dungeon Hub, Training Room, Armory, Storage, Escape Routes</li>
     * </ul>
     * </p>
     *
     * @param dungeonId the base dungeon ID for this HQ
     * @param techLevel the technology level of the capital city
     * @return the dungeon ID created
     */
    public String generateYoungerIndustriesHQ(String dungeonId, TechLevel techLevel) {
        RoomTemplate[] rooms = {
            // === Ground Floor ===
            new RoomTemplate("HQ-01", "GRAND_LOBBY", "Grand Lobby",
                    "A massive lobby with marble floors, holographic displays, and a reception desk.",
                    "Social", "Huge", null, null, "Receptionist NPC"),
            new RoomTemplate("HQ-02", "RECEPTION", "Reception Area",
                    "The reception area with security badges and visitor logs.",
                    "Social", "Large", null, null, "Security Guard NPC"),
            new RoomTemplate("HQ-03", "SECURITY_CHECKPOINT", "Security Checkpoint",
                    "Advanced security scanning with biometric verification.",
                    "Combat", "Medium", "Scanner Trap", 15, null),
            new RoomTemplate("HQ-04", "MAIL_CENTER", "Mail & Package Center",
                    "A busy mail center with automated sorting and package lockers.",
                    "Storage", "Medium", null, null, null),

            // === Upper Floor 1: Executive ===
            new RoomTemplate("HQ-05", "EXECUTIVE_OFFICE", "CEO Office",
                    "The CEO's corner office with panoramic views of the city.",
                    "Social", "Large", null, null, "CEO NPC"),
            new RoomTemplate("HQ-06", "EXECUTIVE_OFFICE", "CFO Office",
                    "The CFO's office with financial displays and trading terminals.",
                    "Social", "Medium", null, null, "CFO NPC"),
            new RoomTemplate("HQ-07", "CONFERENCE_ROOM", "Board Room",
                    "A large conference room with a holographic table for board meetings.",
                    "Social", "Large", null, null, "Board Meeting Event"),

            // === Upper Floor 2: R&D ===
            new RoomTemplate("HQ-08", "RESEARCH_LAB", "R&D Lab Alpha",
                    "A cutting-edge research laboratory with advanced equipment.",
                    "Crafting", "Large", "Hazard Trap", 18, "Prototype Item (Rare)"),
            new RoomTemplate("HQ-09", "RESEARCH_LAB", "R&D Lab Beta",
                    "A secondary research lab focused on prototype testing.",
                    "Crafting", "Medium", "Hazard Trap", 16, null),
            new RoomTemplate("HQ-10", "SERVER_ROOM", "Server Room",
                    "Climate-controlled server room housing corporate data.",
                    "Storage", "Medium", "Electric Trap", 20, "Data Cache (Uncommon)"),

            // === Upper Floor 3: Operations ===
            new RoomTemplate("HQ-11", "OPERATIONS_CENTER", "Operations Center",
                    "The operations center monitoring all corporate activities.",
                    "Social", "Large", null, null, "Operations Manager NPC"),
            new RoomTemplate("HQ-12", "BREAK_ROOM", "Employee Break Room",
                    "A spacious break room with vending machines and a kitchen.",
                    "Social", "Medium", null, null, "Employee NPC"),
            new RoomTemplate("HQ-13", "TRAINING_ROOM", "Training Room",
                    "A training room for new employee orientation and safety drills.",
                    "Social", "Medium", null, null, "Trainer NPC"),

            // === Rooftop ===
            new RoomTemplate("HQ-14", "HELIPAD", "Helipad / Landing Pad",
                    "A rooftop helipad for executive transport and emergency landings.",
                    "Social", "Large", null, null, "Pilot NPC"),
            new RoomTemplate("HQ-15", "ROOFTOP_GARDEN", "Rooftop Garden",
                    "A landscaped rooftop garden with seating and city views.",
                    "Social", "Medium", null, null, null),

            // === Basement B1: Dungeon Hub ===
            new RoomTemplate("HQ-16", "BASEMENT_ENTRANCE", "Basement Entrance",
                    "A dimly lit entrance to the underground levels with flickering lights.",
                    "Combat", "Small", null, null, null),
            new RoomTemplate("HQ-17", "DUNGEON_HUB", "Dungeon Hub",
                    "The central hub of the basement dungeon with multiple corridors leading deeper.",
                    "Combat", "Large", "Alarm Trap", 14, null),
            new RoomTemplate("HQ-18", "ARMORY", "Armory",
                    "A locked armory containing weapons and protective gear.",
                    "Combat", "Medium", "Lock Trap", 16, "Weapon Cache (Rare)"),

            // === Basement B2: Deep Storage ===
            new RoomTemplate("HQ-19", "STORAGE_ROOM", "Deep Storage",
                    "Rows of shelves with old equipment, archives, and forgotten projects.",
                    "Storage", "Medium", null, null, "Hidden Cache (Uncommon)"),
            new RoomTemplate("HQ-20", "MAINTENANCE", "Maintenance Bay",
                    "A maintenance bay with tools, generators, and utility systems.",
                    "Crafting", "Medium", null, null, "Engineer NPC"),

            // === Basement B3: Escape & Secrets ===
            new RoomTemplate("HQ-21", "SECRET_PASSAGE", "Secret Passage",
                    "A hidden passage leading to underground tunnels beneath the city.",
                    "Combat", "Small", "Collapse Trap", 12, "Escape Route"),
            new RoomTemplate("HQ-22", "VAULT", "Corporate Vault",
                    "A heavily secured vault containing corporate secrets and valuables.",
                    "Storage", "Medium", "Lock Trap", 22, "Corporate Secrets (Legendary)"),
            new RoomTemplate("HQ-23", "ESCAPE_ROUTE", "Emergency Exit",
                    "An emergency exit leading to the streets below.",
                    "Combat", "Small", null, null, null),

            // === Additional Rooms ===
            new RoomTemplate("HQ-24", "MEDICAL_BAY", "Medical Bay",
                    "A medical bay with first aid stations and a doctor's office.",
                    "Social", "Small", null, null, "Doctor NPC"),
            new RoomTemplate("HQ-25", "CAFETERIA", "Cafeteria",
                    "A large cafeteria serving meals to all employees across floors.",
                    "Social", "Large", null, null, "Chef NPC")
        };
        Long[] ids = resolveAndCreate(dungeonId, techLevel, rooms);

        // === Ground Floor Connections ===
        connect(ids[0], ids[1]);   // Lobby → Reception
        connect(ids[1], ids[2]);   // Reception → Security
        connect(ids[0], ids[3]);   // Lobby → Mail Center

        // === Upper Floor 1: Executive ===
        connect(ids[0], ids[4]);   // Lobby → CEO Office (elevator)
        connect(ids[4], ids[5]);   // CEO → CFO
        connect(ids[4], ids[6]);   // CEO → Board Room

        // === Upper Floor 2: R&D ===
        connect(ids[4], ids[7]);   // CEO → R&D Alpha
        connect(ids[7], ids[8]);   // R&D Alpha → R&D Beta
        connect(ids[7], ids[9]);   // R&D Alpha → Server Room

        // === Upper Floor 3: Operations ===
        connect(ids[4], ids[10]);  // CEO → Operations Center
        connect(ids[10], ids[11]); // Operations → Break Room
        connect(ids[10], ids[12]); // Operations → Training Room

        // === Rooftop ===
        connect(ids[0], ids[13]);  // Lobby → Helipad (elevator)
        connect(ids[13], ids[14]); // Helipad → Rooftop Garden

        // === Basement B1: Dungeon Hub ===
        connect(ids[0], ids[15]);  // Lobby → Basement Entrance
        connect(ids[15], ids[16]); // Basement → Dungeon Hub
        connect(ids[16], ids[17]); // Dungeon Hub → Armory

        // === Basement B2: Deep Storage ===
        connect(ids[16], ids[18]); // Dungeon Hub → Deep Storage
        connect(ids[16], ids[19]); // Dungeon Hub → Maintenance

        // === Basement B3: Escape & Secrets ===
        connect(ids[18], ids[20]); // Deep Storage → Secret Passage
        connect(ids[16], ids[21]); // Dungeon Hub → Corporate Vault
        connect(ids[20], ids[22]); // Secret Passage → Emergency Exit

        // === Additional Rooms ===
        connect(ids[11], ids[23]); // Break Room → Medical Bay
        connect(ids[11], ids[24]); // Break Room → Cafeteria

        return dungeonId;
    }

    public String generateYoungerIndustriesHQ(String dungeonId) {
        return generateYoungerIndustriesHQ(dungeonId, TechLevel.TL2);
    }

    // ---- Template Resolution Helper ----

    /**
     * Resolves room templates for a given tech level and creates the rooms.
     * Returns an array of room IDs in the same order as the input templates.
     */
    private Long[] resolveAndCreate(String dungeonId, TechLevel techLevel, RoomTemplate[] templates) {
        Long[] ids = new Long[templates.length];
        for (int i = 0; i < templates.length; i++) {
            RoomTemplate resolved = resolveRoomTemplate(techLevel, templates[i]);
            String trapName = (resolved.trap != null) ? resolveTrapName(techLevel, resolved.trap) : null;
            ids[i] = createRoom(dungeonId, resolved.roomId, resolved.fallbackName, resolved.fallbackDescription,
                    resolved.theme, resolved.size, trapName, resolved.trapDc, resolved.treasure);
        }
        return ids;
    }

    // ---- Helper Methods ----

    /**
     * Creates a single dungeon room and registers it with the service.
     */
    private Long createRoom(String dungeonId, String roomId, String name, String description,
                            String theme, String size, String trap, Integer trapDc, String treasure) {
        DungeonRoomService.DungeonRoom room = new DungeonRoomService.DungeonRoom();
        room.setDungeonId(dungeonId);
        room.setName(name);
        room.setDescription(description);
        room.setTheme(theme);
        room.setSize(size);
        room.setActive(true);
        room.setDiscovered(false);

        if (trap != null) {
            room.setTrap(trap);
            room.setTrapDc(trapDc != null ? trapDc : 10);
        }
        if (treasure != null) {
            // Determine if this is a treasure item or an encounter/NPC
            boolean isEncounter = treasure.contains("NPC") || treasure.contains("Event")
                    || treasure.contains("Dialogue") || treasure.contains("Giver")
                    || treasure.contains("Gossip") || treasure.contains("Item");
            if (isEncounter) {
                room.setEncounter(treasure);
            }
            // Set treasure for items with rarity indicators
            if (treasure.contains("(") && treasure.contains(")")) {
                room.setTreasure(treasure);
                room.setGoldPieces(random.nextInt(100) + 10);
            }
        }

        DungeonRoomService.DungeonRoom created = dungeonService.createRoom(room);
        return created.getId();
    }

    /**
     * Adds a bidirectional connection between two rooms.
     */
    private void connect(Long fromId, Long toId) {
        dungeonService.addConnection(fromId, toId);
        dungeonService.addConnection(toId, fromId);
    }
}
