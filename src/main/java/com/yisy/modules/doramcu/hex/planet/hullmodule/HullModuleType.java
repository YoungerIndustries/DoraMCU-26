package com.yisy.modules.doramcu.hex.planet.hullmodule;
import com.yisy.modules.doramcu.hex.planet.CargoType;
import com.yisy.modules.doramcu.hex.planet.TerrainType;

import java.util.Map;
import java.util.Set;

/**
 * Unified structure type enum for all buildable modules on the planet surface.
 * <p>
 * This replaces scattered building type definitions with a single source of truth
 * for all structure types: residential, commercial, industrial, infrastructure,
 * and custom player-created modules.
 * </p>
 * <p>
 * Each type defines:
 * <ul>
 *   <li>Display name and category</li>
 *   <li>Construction cost and maintenance cost</li>
 *   <li>Floor count and basement levels</li>
 *   <li>Required terrain types for placement</li>
 *   <li>Input/output cargo for production facilities</li>
 *   <li>Capacity metrics (apartments, offices, cargo, passengers)</li>
 * </ul>
 * </p>
 */
public enum HullModuleType {
    // ---- Residential Buildings ----

    APARTMENT_BUILDING("Apartment Building", Category.RESIDENTIAL,
            2000, 50,
            10, 3,
            Set.of(TerrainType.GRASSLAND, TerrainType.FOREST, TerrainType.DESERT),
            Map.of(), Map.of(),
            40, 0, 0, 0),

    RESIDENTIAL_HOUSE("Residential House", Category.RESIDENTIAL,
            500, 10,
            2, 1,
            Set.of(TerrainType.GRASSLAND, TerrainType.FOREST),
            Map.of(), Map.of(),
            4, 0, 0, 0),

    TOWN_HOUSE("Town House", Category.RESIDENTIAL,
            1000, 20,
            3, 1,
            Set.of(TerrainType.GRASSLAND),
            Map.of(), Map.of(),
            8, 0, 0, 0),

    // ---- Commercial Buildings ----

    YOUNGER_HQ("Younger Industries HQ", Category.COMMERCIAL,
            10000, 200,
            6, 3,
            Set.of(TerrainType.GRASSLAND),
            Map.of(), Map.of(),
            0, 25, 0, 0),

    SHOP("Shop", Category.COMMERCIAL,
            800, 15,
            1, 0,
            Set.of(TerrainType.GRASSLAND),
            Map.of(), Map.of(),
            0, 0, 0, 0),

    WAREHOUSE("Warehouse", Category.COMMERCIAL,
            1500, 30,
            2, 0,
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT),
            Map.of(), Map.of(),
            0, 0, 500, 0),

    // ---- Primary Industries (Raw Materials) ----

    MINE("Mine", Category.INDUSTRY,
            500, 10,
            1, 2,
            Set.of(TerrainType.MOUNTAIN, TerrainType.HIGHLAND),
            Map.of(), Map.of(CargoType.ORE, 10),
            0, 0, 0, 0),

    COAL_MINE("Coal Mine", Category.INDUSTRY,
            400, 8,
            1, 2,
            Set.of(TerrainType.MOUNTAIN, TerrainType.HIGHLAND, TerrainType.GRASSLAND),
            Map.of(), Map.of(CargoType.COAL, 10),
            0, 0, 0, 0),

    OIL_WELL("Oil Well", Category.INDUSTRY,
            800, 15,
            1, 1,
            Set.of(TerrainType.DESERT, TerrainType.GRASSLAND, TerrainType.DEEP_OCEAN),
            Map.of(), Map.of(CargoType.OIL, 8),
            0, 0, 0, 0),

    LOGGING_CAMP("Logging Camp", Category.INDUSTRY,
            300, 5,
            1, 1,
            Set.of(TerrainType.FOREST, TerrainType.JUNGLE),
            Map.of(), Map.of(CargoType.WOOD, 12),
            0, 0, 0, 0),

    FARM("Farm", Category.INDUSTRY,
            350, 6,
            1, 1,
            Set.of(TerrainType.GRASSLAND, TerrainType.SAVANNA),
            Map.of(), Map.of(CargoType.GRAIN, 15),
            0, 0, 0, 0),

    RANCH("Ranch", Category.INDUSTRY,
            400, 8,
            1, 1,
            Set.of(TerrainType.GRASSLAND, TerrainType.SAVANNA),
            Map.of(), Map.of(CargoType.MEAT, 10),
            0, 0, 0, 0),

    FISHING_PORT("Fishing Port", Category.INDUSTRY,
            450, 10,
            1, 1,
            Set.of(TerrainType.DEEP_OCEAN, TerrainType.BEACH),
            Map.of(), Map.of(CargoType.FISH, 12),
            0, 0, 0, 0),

    ORCHARD("Orchard", Category.INDUSTRY,
            300, 5,
            1, 1,
            Set.of(TerrainType.GRASSLAND, TerrainType.FOREST),
            Map.of(), Map.of(CargoType.FRUIT, 14),
            0, 0, 0, 0),

    // ---- Secondary Industries (Processing) ----

    STEEL_MILL("Steel Mill", Category.INDUSTRY,
            1200, 25,
            2, 1,
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT),
            Map.of(CargoType.ORE, 8, CargoType.COAL, 6), Map.of(CargoType.STEEL, 10),
            0, 0, 0, 0),

    CEMENT_PLANT("Cement Plant", Category.INDUSTRY,
            900, 18,
            2, 1,
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT),
            Map.of(CargoType.ORE, 10), Map.of(CargoType.CONCRETE, 12),
            0, 0, 0, 0),

    SAWMILL("Sawmill", Category.INDUSTRY,
            600, 12,
            1, 1,
            Set.of(TerrainType.FOREST, TerrainType.GRASSLAND),
            Map.of(CargoType.WOOD, 10), Map.of(CargoType.LUMBER, 12),
            0, 0, 0, 0),

    BAKERY("Bakery", Category.INDUSTRY,
            500, 10,
            1, 0,
            Set.of(TerrainType.GRASSLAND),
            Map.of(CargoType.GRAIN, 8), Map.of(CargoType.BREAD, 10),
            0, 0, 0, 0),

    REFINERY("Refinery", Category.INDUSTRY,
            1500, 30,
            2, 1,
            Set.of(TerrainType.DESERT, TerrainType.GRASSLAND),
            Map.of(CargoType.OIL, 10), Map.of(CargoType.REFINED_OIL, 12),
            0, 0, 0, 0),

    GLASS_FACTORY("Glass Factory", Category.INDUSTRY,
            800, 15,
            2, 1,
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT),
            Map.of(CargoType.CONCRETE, 8), Map.of(CargoType.GLASS, 10),
            0, 0, 0, 0),

    SLAUGHTERHOUSE("Slaughterhouse", Category.INDUSTRY,
            600, 12,
            1, 1,
            Set.of(TerrainType.GRASSLAND),
            Map.of(CargoType.LIVESTOCK, 8), Map.of(CargoType.MEAT, 10),
            0, 0, 0, 0),

    CHEMICAL_PLANT("Chemical Plant", Category.INDUSTRY,
            1200, 25,
            2, 1,
            Set.of(TerrainType.DESERT, TerrainType.GRASSLAND),
            Map.of(CargoType.OIL, 8), Map.of(CargoType.PLASTIC, 10),
            0, 0, 0, 0),

    // ---- Tertiary Industries (Advanced Manufacturing) ----

    ELECTRONICS_FACTORY("Electronics Factory", Category.INDUSTRY,
            2000, 40,
            3, 1,
            Set.of(TerrainType.GRASSLAND),
            Map.of(CargoType.STEEL, 6, CargoType.REFINED_OIL, 4), Map.of(CargoType.ELECTRONICS, 8),
            0, 0, 0, 0),

    MACHINE_SHOP("Machine Shop", Category.INDUSTRY,
            1800, 35,
            3, 1,
            Set.of(TerrainType.GRASSLAND),
            Map.of(CargoType.STEEL, 8), Map.of(CargoType.MACHINERY, 6),
            0, 0, 0, 0),

    LUXURY_FACTORY("Luxury Factory", Category.INDUSTRY,
            2500, 50,
            3, 1,
            Set.of(TerrainType.GRASSLAND),
            Map.of(CargoType.ELECTRONICS, 4, CargoType.BREAD, 4), Map.of(CargoType.LUXURY, 6),
            0, 0, 0, 0),

    ADVANCED_CHEMICALS("Advanced Chemicals", Category.INDUSTRY,
            2200, 45,
            3, 1,
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT),
            Map.of(CargoType.CHEMICALS, 8, CargoType.REFINED_OIL, 4), Map.of(CargoType.PLASTIC, 8),
            0, 0, 0, 0),

    // ---- Space Industries ----

    ROCKET_PARTS_FACTORY("Rocket Parts Factory", Category.INDUSTRY,
            5000, 100,
            4, 2,
            Set.of(TerrainType.GRASSLAND),
            Map.of(CargoType.STEEL, 10, CargoType.ELECTRONICS, 8), Map.of(CargoType.ROCKET_PARTS, 6),
            0, 0, 0, 0),

    SPACECRAFT_ASSEMBLY("Spacecraft Assembly", Category.INDUSTRY,
            8000, 150,
            5, 2,
            Set.of(TerrainType.GRASSLAND),
            Map.of(CargoType.ROCKET_PARTS, 8, CargoType.MACHINERY, 6), Map.of(CargoType.SPACECRAFT, 4),
            0, 0, 0, 0),

    LANDER_FACTORY("Lander Factory", Category.INDUSTRY,
            6000, 120,
            4, 2,
            Set.of(TerrainType.GRASSLAND),
            Map.of(CargoType.ROCKET_PARTS, 6, CargoType.ELECTRONICS, 4), Map.of(CargoType.LANDERS, 4),
            0, 0, 0, 0),

    ROCKET_LAUNCH_PAD("Rocket Launch Pad", Category.INDUSTRY,
            10000, 200,
            1, 0,
            Set.of(TerrainType.DESERT, TerrainType.GRASSLAND),
            Map.of(CargoType.SPACECRAFT, 1), Map.of(),
            0, 0, 0, 0),

    // ---- Infrastructure ----

    RAIL_STATION("Rail Station", Category.INFRASTRUCTURE,
            50, 3,
            1, 0,
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT, TerrainType.FOREST),
            Map.of(), Map.of(),
            0, 0, 200, 200),

    BUS_STOP("Bus Stop", Category.INFRASTRUCTURE,
            10, 1,
            1, 0,
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT, TerrainType.FOREST),
            Map.of(), Map.of(),
            0, 0, 50, 50),

    MONORAIL_STATION("Monorail Station", Category.INFRASTRUCTURE,
            100, 5,
            1, 0,
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT, TerrainType.FOREST),
            Map.of(), Map.of(),
            0, 0, 300, 300),

    MAGLEV_STATION("Maglev Station", Category.INFRASTRUCTURE,
            200, 10,
            1, 0,
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT),
            Map.of(), Map.of(),
            0, 0, 400, 400),

    BOAT_DOCK("Boat Dock", Category.INFRASTRUCTURE,
            150, 5,
            1, 0,
            Set.of(TerrainType.GRASSLAND, TerrainType.DEEP_OCEAN, TerrainType.BEACH),
            Map.of(), Map.of(),
            0, 0, 100, 100),

    PORT("Port", Category.INFRASTRUCTURE,
            500, 20,
            1, 0,
            Set.of(TerrainType.DEEP_OCEAN, TerrainType.BEACH),
            Map.of(), Map.of(),
            0, 0, 500, 500),

    AIRPORT("Airport", Category.INFRASTRUCTURE,
            500, 20,
            1, 0,
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT),
            Map.of(), Map.of(),
            0, 0, 200, 500),

    HELIPORT("Heliport", Category.INFRASTRUCTURE,
            300, 10,
            1, 0,
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT),
            Map.of(), Map.of(),
            0, 0, 100, 100),

    SPACEPORT("Space Port", Category.INFRASTRUCTURE,
            2000, 50,
            2, 0,
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT),
            Map.of(), Map.of(),
            0, 0, 1000, 1000),

    ORBITAL_STATION("Orbital Station", Category.INFRASTRUCTURE,
            15000, 300,
            3, 0,
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT),
            Map.of(), Map.of(),
            0, 0, 5000, 5000),

    CARGO_STATION("Cargo Station", Category.INFRASTRUCTURE,
            200, 8,
            1, 0,
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT, TerrainType.FOREST),
            Map.of(), Map.of(),
            0, 0, 1000, 100),

    GENERAL_STATION("General Station", Category.INFRASTRUCTURE,
            50, 3,
            1, 0,
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT, TerrainType.FOREST),
            Map.of(), Map.of(),
            0, 0, 200, 200),

    DEPOT("Depot", Category.INFRASTRUCTURE,
            300, 10,
            1, 0,
            Set.of(TerrainType.GRASSLAND),
            Map.of(), Map.of(),
            0, 0, 0, 0),

    // ---- Military Structures ----

    ARMY_BASE("Army Base", Category.MILITARY,
            3000, 60,
            3, 2,
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT),
            Map.of(CargoType.STEEL, 10, CargoType.REFINED_OIL, 5), Map.of(),
            0, 0, 0, 0),

    NAVY_BASE("Navy Base", Category.MILITARY,
            5000, 100,
            3, 1,
            Set.of(TerrainType.DEEP_OCEAN, TerrainType.BEACH),
            Map.of(CargoType.STEEL, 15, CargoType.REFINED_OIL, 10), Map.of(),
            0, 0, 0, 0),

    SPACE_NAVY_BASE("Space Navy Base", Category.MILITARY,
            10000, 200,
            4, 2,
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT),
            Map.of(CargoType.STEEL, 20, CargoType.ELECTRONICS, 10, CargoType.REFINED_OIL, 10), Map.of(),
            0, 0, 0, 0),

    BARRACKS("Barracks", Category.MILITARY,
            500, 10,
            2, 1,
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT, TerrainType.FOREST),
            Map.of(), Map.of(),
            0, 0, 0, 0),

    TANK_FACTORY("Tank Factory", Category.MILITARY,
            4000, 80,
            3, 1,
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT),
            Map.of(CargoType.STEEL, 15, CargoType.MACHINERY, 5), Map.of(CargoType.MACHINERY, 8),
            0, 0, 0, 0),

    ARTILLERY_FORT("Artillery Fort", Category.MILITARY,
            2000, 40,
            2, 2,
            Set.of(TerrainType.HIGHLAND, TerrainType.MOUNTAIN),
            Map.of(CargoType.STEEL, 5), Map.of(),
            0, 0, 0, 0),

    MISSILE_SILO("Missile Silo", Category.MILITARY,
            8000, 150,
            1, 4,
            Set.of(TerrainType.DESERT, TerrainType.GRASSLAND),
            Map.of(CargoType.ELECTRONICS, 10, CargoType.REFINED_OIL, 10), Map.of(),
            0, 0, 0, 0),

    AIR_BASE("Air Base", Category.MILITARY,
            6000, 120,
            2, 1,
            Set.of(TerrainType.GRASSLAND, TerrainType.DESERT),
            Map.of(CargoType.REFINED_OIL, 15, CargoType.ELECTRONICS, 5), Map.of(),
            0, 0, 0, 0),

    MILITARY_ACADEMY("Military Academy", Category.MILITARY,
            1500, 30,
            3, 1,
            Set.of(TerrainType.GRASSLAND, TerrainType.FOREST),
            Map.of(), Map.of(),
            0, 0, 0, 0),

    // ---- Custom Player-Created Modules ----

    CUSTOM("Custom Module", Category.CUSTOM,
            1000, 20,
            2, 1,
            Set.of(TerrainType.GRASSLAND),
            Map.of(), Map.of(),
            0, 0, 0, 0);

    private final String displayName;
    private final Category category;
    private final int constructionCost;
    private final int maintenanceCost;
    private final int floors;
    private final int basementLevels;
    private final Set<TerrainType> requiredTerrain;
    private final Map<CargoType, Integer> inputCargo;
    private final Map<CargoType, Integer> outputCargo;
    private final int apartmentUnits;
    private final int officeUnits;
    private final int cargoCapacity;
    private final int passengerCapacity;

    HullModuleType(String displayName, Category category,
                   int constructionCost, int maintenanceCost,
                   int floors, int basementLevels,
                   Set<TerrainType> requiredTerrain,
                   Map<CargoType, Integer> inputCargo, Map<CargoType, Integer> outputCargo,
                   int apartmentUnits, int officeUnits,
                   int cargoCapacity, int passengerCapacity) {
        this.displayName = displayName;
        this.category = category;
        this.constructionCost = constructionCost;
        this.maintenanceCost = maintenanceCost;
        this.floors = floors;
        this.basementLevels = basementLevels;
        this.requiredTerrain = requiredTerrain;
        this.inputCargo = inputCargo;
        this.outputCargo = outputCargo;
        this.apartmentUnits = apartmentUnits;
        this.officeUnits = officeUnits;
        this.cargoCapacity = cargoCapacity;
        this.passengerCapacity = passengerCapacity;
    }

    public String getDisplayName() { return displayName; }
    public Category getCategory() { return category; }
    public int getConstructionCost() { return constructionCost; }
    public int getMaintenanceCost() { return maintenanceCost; }
    public int getFloors() { return floors; }
    public int getBasementLevels() { return basementLevels; }
    public Set<TerrainType> getRequiredTerrain() { return requiredTerrain; }
    public Map<CargoType, Integer> getInputCargo() { return inputCargo; }
    public Map<CargoType, Integer> getOutputCargo() { return outputCargo; }
    public int getApartmentUnits() { return apartmentUnits; }
    public int getOfficeUnits() { return officeUnits; }
    public int getCargoCapacity() { return cargoCapacity; }
    public int getPassengerCapacity() { return passengerCapacity; }

    public boolean canBuildOn(TerrainType terrain) {
        return requiredTerrain.contains(terrain);
    }

    /**
     * Map from IndustryType name to HullModuleType.
     * Used by DefaultPlanetSurfaceBuilder to create HullModule instances for industries.
     */
    public static HullModuleType fromIndustryType(String industryTypeName) {
        try {
            return valueOf(industryTypeName);
        } catch (IllegalArgumentException e) {
            // Fallback to MINE for unknown industry types
            return MINE;
        }
    }

    /**
     * Map from Station.StationType name to HullModuleType.
     * Used by DefaultPlanetSurfaceBuilder to create HullModule instances for stations.
     */
    public static HullModuleType fromStationType(String stationTypeName) {
        switch (stationTypeName) {
            case "BUS_STOP": return BUS_STOP;
            case "RAIL_STATION": return RAIL_STATION;
            case "MONORAIL_STATION": return MONORAIL_STATION;
            case "MAGLEV_STATION": return MAGLEV_STATION;
            case "BOAT_DOCK": return BOAT_DOCK;
            case "PORT": return PORT;
            case "AIRPORT": return AIRPORT;
            case "HELIPORT": return HELIPORT;
            case "SPACEPORT": return SPACEPORT;
            case "ORBITAL_STATION": return ORBITAL_STATION;
            case "CARGO_STATION": return CARGO_STATION;
            case "GENERAL": return GENERAL_STATION;
            default: return GENERAL_STATION;
        }
    }

    /**
     * Categories of buildable structures.
     */
    public enum Category {
        RESIDENTIAL,
        COMMERCIAL,
        INDUSTRY,
        INFRASTRUCTURE,
        MILITARY,
        CUSTOM
    }
}
