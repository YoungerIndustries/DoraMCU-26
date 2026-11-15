package com.yisy.modules.doramcu.hex.planet.fps.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Complete world state snapshot for the FPS 3D renderer.
 * <p>
 * Includes vehicles, industries, stations, residential buildings, ships, starbases,
 * cargo, military units, and active battles.
 * </p>
 */
public class FpsWorldStateDto {

    private final int planetId;
    private final int currentTurn;
    private final List<FpsVehicleStateDto> vehicles;
    private final List<FpsIndustryStateDto> industries;
    private final List<FpsStationStateDto> stations;
    private final List<FpsResidentialStateDto> residential;
    private final List<FpsHQHubStateDto> hqHubs;
    private final List<FpsHullModuleStateDto> hullModules;
    private final List<FpsShipStateDto> ships;
    private final List<FpsStarBaseStateDto> starbases;
    private final List<FpsCargoStateDto> cargo;
    private final List<FpsMilitaryUnitStateDto> militaryUnits;
    private final List<FpsBattleStateDto> battles;
    private final List<FpsAICommanderStateDto> aiCommanders;
    private final List<FpsBattlefieldUnitStateDto> battlefieldUnits;

    public FpsWorldStateDto(int planetId, int currentTurn) {
        this.planetId = planetId;
        this.currentTurn = currentTurn;
        this.vehicles = new ArrayList<>();
        this.industries = new ArrayList<>();
        this.stations = new ArrayList<>();
        this.residential = new ArrayList<>();
        this.hqHubs = new ArrayList<>();
        this.hullModules = new ArrayList<>();
        this.ships = new ArrayList<>();
        this.starbases = new ArrayList<>();
        this.cargo = new ArrayList<>();
        this.militaryUnits = new ArrayList<>();
        this.battles = new ArrayList<>();
        this.aiCommanders = new ArrayList<>();
        this.battlefieldUnits = new ArrayList<>();
    }

    public int getPlanetId() { return planetId; }
    public int getCurrentTurn() { return currentTurn; }

    public List<FpsVehicleStateDto> getVehicles() {
        return Collections.unmodifiableList(vehicles);
    }

    public List<FpsIndustryStateDto> getIndustries() {
        return Collections.unmodifiableList(industries);
    }

    public List<FpsStationStateDto> getStations() {
        return Collections.unmodifiableList(stations);
    }

    public List<FpsResidentialStateDto> getResidential() {
        return Collections.unmodifiableList(residential);
    }

    public void addVehicle(FpsVehicleStateDto vehicle) {
        vehicles.add(vehicle);
    }

    public void addIndustry(FpsIndustryStateDto industry) {
        industries.add(industry);
    }

    public void addStation(FpsStationStateDto station) {
        stations.add(station);
    }

    public void addResidential(FpsResidentialStateDto building) {
        residential.add(building);
    }

    public List<FpsHQHubStateDto> getHqHubs() {
        return Collections.unmodifiableList(hqHubs);
    }

    public void addHqHub(FpsHQHubStateDto hq) {
        hqHubs.add(hq);
    }

    public List<FpsHullModuleStateDto> getHullModules() {
        return Collections.unmodifiableList(hullModules);
    }

    public void addHullModule(FpsHullModuleStateDto module) {
        hullModules.add(module);
    }

    // ---- Ships ----

    public List<FpsShipStateDto> getShips() {
        return Collections.unmodifiableList(ships);
    }

    public void addShip(FpsShipStateDto ship) {
        ships.add(ship);
    }

    // ---- Starbases ----

    public List<FpsStarBaseStateDto> getStarbases() {
        return Collections.unmodifiableList(starbases);
    }

    public void addStarbase(FpsStarBaseStateDto starbase) {
        starbases.add(starbase);
    }

    // ---- Cargo ----

    public List<FpsCargoStateDto> getCargo() {
        return Collections.unmodifiableList(cargo);
    }

    public void addCargo(FpsCargoStateDto cargo) {
        this.cargo.add(cargo);
    }

    // ---- Military Units ----

    public List<FpsMilitaryUnitStateDto> getMilitaryUnits() {
        return Collections.unmodifiableList(militaryUnits);
    }

    public void addMilitaryUnit(FpsMilitaryUnitStateDto unit) {
        militaryUnits.add(unit);
    }

    // ---- Battles ----

    public List<FpsBattleStateDto> getBattles() {
        return Collections.unmodifiableList(battles);
    }

    public void addBattle(FpsBattleStateDto battle) {
        battles.add(battle);
    }

    // ---- AI Commanders ----

    public List<FpsAICommanderStateDto> getAiCommanders() {
        return Collections.unmodifiableList(aiCommanders);
    }

    public void addAiCommander(FpsAICommanderStateDto commander) {
        aiCommanders.add(commander);
    }

    // ---- Battlefield Units ----

    public List<FpsBattlefieldUnitStateDto> getBattlefieldUnits() {
        return Collections.unmodifiableList(battlefieldUnits);
    }

    public void addBattlefieldUnit(FpsBattlefieldUnitStateDto unit) {
        battlefieldUnits.add(unit);
    }

    /**
     * Station state for the FPS 3D renderer.
     */
    public static class FpsStationStateDto {
        private final String id;
        private final String name;
        private final String stationType;
        private final int x, y, z;
        private final int cargoWaiting;
        private final int passengersWaiting;

        public FpsStationStateDto(String id, String name, String stationType, int x, int y, int z,
                                   int cargoWaiting, int passengersWaiting) {
            this.id = id;
            this.name = name;
            this.stationType = stationType;
            this.x = x;
            this.y = y;
            this.z = z;
            this.cargoWaiting = cargoWaiting;
            this.passengersWaiting = passengersWaiting;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getStationType() { return stationType; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
        public int getCargoWaiting() { return cargoWaiting; }
        public int getPassengersWaiting() { return passengersWaiting; }
    }

    /**
     * Residential building state for the FPS 3D renderer.
     * <p>
     * Represents apartment buildings with upper floors for residential use
     * and basement levels for dungeon instances. This is where new characters
     * can find a home, with apartment units, common areas, and basement dungeons.
     * </p>
     */
    public static class FpsResidentialStateDto {
        private final String id;
        private final String name;
        private final String buildingType; // "APARTMENT_BUILDING", "RESIDENTIAL_HOUSE", "TOWN_HOUSE"
        private final String settlementName; // name of the settlement this building is in
        private final int x, y, z;
        private final int floors;
        private final int basementLevels;
        private final int totalApartments; // total number of apartment units
        private final int occupiedApartments; // currently occupied apartments
        private final int occupancy; // percentage 0-100
        private final int totalRooms; // total dungeon rooms in the blueprint
        private final boolean active;
        private final String dungeonId; // links to generated dungeon rooms

        public FpsResidentialStateDto(String id, String name, String buildingType,
                                        String settlementName,
                                        int x, int y, int z,
                                        int floors, int basementLevels,
                                        int totalApartments, int occupiedApartments,
                                        int occupancy, int totalRooms,
                                        boolean active, String dungeonId) {
            this.id = id;
            this.name = name;
            this.buildingType = buildingType;
            this.settlementName = settlementName;
            this.x = x;
            this.y = y;
            this.z = z;
            this.floors = floors;
            this.basementLevels = basementLevels;
            this.totalApartments = totalApartments;
            this.occupiedApartments = occupiedApartments;
            this.occupancy = occupancy;
            this.totalRooms = totalRooms;
            this.active = active;
            this.dungeonId = dungeonId;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getBuildingType() { return buildingType; }
        public String getSettlementName() { return settlementName; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
        public int getFloors() { return floors; }
        public int getBasementLevels() { return basementLevels; }
        public int getTotalApartments() { return totalApartments; }
        public int getOccupiedApartments() { return occupiedApartments; }
        public int getOccupancy() { return occupancy; }
        public int getTotalRooms() { return totalRooms; }
        public boolean isActive() { return active; }
        public String getDungeonId() { return dungeonId; }
    }

    /**
     * Younger Industries HQ Hub state for the FPS 3D renderer.
     * <p>
     * Represents the corporate headquarters building at major capital cities.
     * The HQ has multiple upper floors for corporate offices and basement levels
     * for dungeon instances. This is the primary landing point for characters.
     * </p>
     */
    public static class FpsHQHubStateDto {
        private final String id;
        private final String name;
        private final String capitalName; // name of the capital city
        private final int x, y, z;
        private final int officeFloors;
        private final int basementLevels;
        private final int totalRooms;
        private final int occupiedOffices;
        private final boolean active;
        private final String dungeonId; // links to generated dungeon rooms

        public FpsHQHubStateDto(String id, String name, String capitalName,
                                 int x, int y, int z,
                                 int officeFloors, int basementLevels,
                                 int totalRooms, int occupiedOffices,
                                 boolean active, String dungeonId) {
            this.id = id;
            this.name = name;
            this.capitalName = capitalName;
            this.x = x;
            this.y = y;
            this.z = z;
            this.officeFloors = officeFloors;
            this.basementLevels = basementLevels;
            this.totalRooms = totalRooms;
            this.occupiedOffices = occupiedOffices;
            this.active = active;
            this.dungeonId = dungeonId;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getCapitalName() { return capitalName; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
        public int getOfficeFloors() { return officeFloors; }
        public int getBasementLevels() { return basementLevels; }
        public int getTotalRooms() { return totalRooms; }
        public int getOccupiedOffices() { return occupiedOffices; }
        public boolean isActive() { return active; }
        public String getDungeonId() { return dungeonId; }
    }

    /**
     * Unified HullModule state for the FPS 3D renderer.
     * <p>
     * Represents any buildable structure on the planet using the unified HullModule system.
     * This replaces separate residential/HQ/industry lists with a single unified model
     * that supports all structure types: apartments, HQs, mines, factories, stations, etc.
     * </p>
     */
    public static class FpsHullModuleStateDto {
        private final String id;
        private final String name;
        private final String moduleType; // "APARTMENT_BUILDING", "MINE", "STEEL_MILL", etc.
        private final String category; // "RESIDENTIAL", "COMMERCIAL", "INDUSTRY", "INFRASTRUCTURE", "CUSTOM"
        private final int x, y, z;
        private final int floors;
        private final int basementLevels;
        private final int totalCapacity;
        private final int occupiedCapacity;
        private final double occupancyRate;
        private final Integer productionLevel;
        private final String ownerId;
        private final boolean active;
        private final String dungeonId;

        public FpsHullModuleStateDto(String id, String name, String moduleType, String category,
                                      int x, int y, int z,
                                      int floors, int basementLevels,
                                      int totalCapacity, int occupiedCapacity,
                                      double occupancyRate, Integer productionLevel,
                                      String ownerId, boolean active, String dungeonId) {
            this.id = id;
            this.name = name;
            this.moduleType = moduleType;
            this.category = category;
            this.x = x;
            this.y = y;
            this.z = z;
            this.floors = floors;
            this.basementLevels = basementLevels;
            this.totalCapacity = totalCapacity;
            this.occupiedCapacity = occupiedCapacity;
            this.occupancyRate = occupancyRate;
            this.productionLevel = productionLevel;
            this.ownerId = ownerId;
            this.active = active;
            this.dungeonId = dungeonId;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getModuleType() { return moduleType; }
        public String getCategory() { return category; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
        public int getFloors() { return floors; }
        public int getBasementLevels() { return basementLevels; }
        public int getTotalCapacity() { return totalCapacity; }
        public int getOccupiedCapacity() { return occupiedCapacity; }
        public double getOccupancyRate() { return occupancyRate; }
        public Integer getProductionLevel() { return productionLevel; }
        public String getOwnerId() { return ownerId; }
        public boolean isActive() { return active; }
        public String getDungeonId() { return dungeonId; }
    }

    /**
     * AI Commander state for the FPS 3D renderer.
     * <p>
     * Represents an AI commander leading military units on the battlefield.
     * Includes leadership stats, tactical preference, command radius, and unit count.
     * </p>
     */
    public static class FpsAICommanderStateDto {
        private final String id;
        private final String name;
        private final String factionId;
        private final String factionName;
        private final int leadership;
        private final String tacticalPreference;
        private final int commandRadius;
        private final int unitCount;
        private final int x, y, z;
        private final boolean inCombat;
        private final int attackBonus;
        private final int defenseBonus;

        public FpsAICommanderStateDto(String id, String name, String factionId, String factionName,
                                       int leadership, String tacticalPreference, int commandRadius,
                                       int unitCount, int x, int y, int z,
                                       boolean inCombat, int attackBonus, int defenseBonus) {
            this.id = id;
            this.name = name;
            this.factionId = factionId;
            this.factionName = factionName;
            this.leadership = leadership;
            this.tacticalPreference = tacticalPreference;
            this.commandRadius = commandRadius;
            this.unitCount = unitCount;
            this.x = x;
            this.y = y;
            this.z = z;
            this.inCombat = inCombat;
            this.attackBonus = attackBonus;
            this.defenseBonus = defenseBonus;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getFactionId() { return factionId; }
        public String getFactionName() { return factionName; }
        public int getLeadership() { return leadership; }
        public String getTacticalPreference() { return tacticalPreference; }
        public int getCommandRadius() { return commandRadius; }
        public int getUnitCount() { return unitCount; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
        public boolean isInCombat() { return inCombat; }
        public int getAttackBonus() { return attackBonus; }
        public int getDefenseBonus() { return defenseBonus; }
    }

    /**
     * Battlefield unit state for the FPS 3D renderer.
     * <p>
     * Represents an individual unit (soldier, vehicle, mech) on the battlefield
     * with position, health, formation, and commander assignment.
     * </p>
     */
    public static class FpsBattlefieldUnitStateDto {
        private final String id;
        private final String name;
        private final String unitType;
        private final String factionId;
        private final String factionName;
        private final String commanderId;
        private final int strength;
        private final int maxStrength;
        private final int morale;
        private final int x, y, z;
        private final int formationX, formationY;
        private final boolean inCombat;
        private final boolean moving;
        private final String targetId;

        public FpsBattlefieldUnitStateDto(String id, String name, String unitType,
                                           String factionId, String factionName,
                                           String commanderId,
                                           int strength, int maxStrength, int morale,
                                           int x, int y, int z,
                                           int formationX, int formationY,
                                           boolean inCombat, boolean moving,
                                           String targetId) {
            this.id = id;
            this.name = name;
            this.unitType = unitType;
            this.factionId = factionId;
            this.factionName = factionName;
            this.commanderId = commanderId;
            this.strength = strength;
            this.maxStrength = maxStrength;
            this.morale = morale;
            this.x = x;
            this.y = y;
            this.z = z;
            this.formationX = formationX;
            this.formationY = formationY;
            this.inCombat = inCombat;
            this.moving = moving;
            this.targetId = targetId;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getUnitType() { return unitType; }
        public String getFactionId() { return factionId; }
        public String getFactionName() { return factionName; }
        public String getCommanderId() { return commanderId; }
        public int getStrength() { return strength; }
        public int getMaxStrength() { return maxStrength; }
        public int getMorale() { return morale; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
        public int getFormationX() { return formationX; }
        public int getFormationY() { return formationY; }
        public boolean isInCombat() { return inCombat; }
        public boolean isMoving() { return moving; }
        public String getTargetId() { return targetId; }
    }
}
