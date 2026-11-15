package com.yisy.modules.doramcu.gamecore;

import java.util.List;

/**
 * Shared interface for building instance management across all game modules.
 * <p>
 * Used for creating and managing building instances such as corporate HQs,
 * apartment buildings, and other multi-floor structures. Each building has
 * upper-level floors for offices/apartments and basement levels for dungeon
 * instances.
 * </p>
 * <p>
 * Earth2026 provides the primary implementation with Younger Industries HQ
 * and Apartment Building; other modules can adapt this for their own building types.
 * </p>
 */
public interface BuildingInstanceService {

    /**
     * Create a new building instance.
     *
     * @param buildingType the type of building (e.g., "HQ", "Apartment")
     * @param name         the building name
     * @param upperFloors  number of upper-level floors
     * @param basementLevels number of basement levels
     * @return the created building instance, or null if creation failed
     */
    BuildingInstance createBuilding(String buildingType, String name, int upperFloors, int basementLevels);

    /**
     * Get a building instance by ID.
     *
     * @param id the building ID
     * @return the building instance, or null if not found
     */
    BuildingInstance getBuilding(Long id);

    /**
     * List all building instances.
     *
     * @return list of all buildings
     */
    List<BuildingInstance> listBuildings();

    /**
     * List buildings by type.
     *
     * @param buildingType the building type filter
     * @return list of matching buildings
     */
    List<BuildingInstance> listBuildingsByType(String buildingType);

    /**
     * Get all floors in a building.
     *
     * @param buildingId the building ID
     * @return list of floor instances
     */
    List<FloorInstance> getFloors(Long buildingId);

    /**
     * Get a specific floor in a building.
     *
     * @param buildingId the building ID
     * @param floorLevel the floor level (positive for upper floors, negative for basement)
     * @return the floor instance, or null if not found
     */
    FloorInstance getFloor(Long buildingId, int floorLevel);

    /**
     * Configure a basement level for dungeon instances.
     *
     * @param buildingId     the building ID
     * @param basementLevel  the basement level (negative number, e.g., -1, -2, -3)
     * @param templateId     the dungeon template ID to use
     * @return the configured dungeon instance, or null if configuration failed
     */
    DungeonInstance configureBasement(Long buildingId, int basementLevel, String templateId);

    /**
     * Get all active dungeon instances in a building's basement levels.
     *
     * @param buildingId the building ID
     * @return list of active dungeon instances
     */
    List<DungeonInstance> getDungeonInstances(Long buildingId);

    /**
     * Get a dungeon instance by ID.
     *
     * @param id the dungeon instance ID
     * @return the dungeon instance, or null if not found
     */
    DungeonInstance getDungeonInstance(Long id);

    /**
     * Remove a dungeon instance from a basement level.
     *
     * @param dungeonInstanceId the dungeon instance ID
     * @return true if removed successfully
     */
    boolean removeDungeonInstance(Long dungeonInstanceId);

    /**
     * Get all available dungeon templates.
     *
     * @return list of available templates
     */
    List<DungeonInstanceTemplate> listDungeonTemplates();

    /**
     * Get a dungeon template by ID.
     *
     * @param templateId the template ID
     * @return the template, or null if not found
     */
    DungeonInstanceTemplate getDungeonTemplate(String templateId);

    /**
     * A building instance (e.g., Younger Industries HQ, Apartment Building).
     */
    class BuildingInstance {
        private Long id;
        private String name;
        private String buildingType;
        private int upperFloors;
        private int basementLevels;
        private double occupancyRate;
        private boolean active;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBuildingType() {
            return buildingType;
        }

        public void setBuildingType(String buildingType) {
            this.buildingType = buildingType;
        }

        public int getUpperFloors() {
            return upperFloors;
        }

        public void setUpperFloors(int upperFloors) {
            this.upperFloors = upperFloors;
        }

        public int getBasementLevels() {
            return basementLevels;
        }

        public void setBasementLevels(int basementLevels) {
            this.basementLevels = basementLevels;
        }

        public double getOccupancyRate() {
            return occupancyRate;
        }

        public void setOccupancyRate(double occupancyRate) {
            this.occupancyRate = occupancyRate;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    /**
     * A floor within a building.
     */
    class FloorInstance {
        private Long id;
        private Long buildingId;
        private int floorLevel;
        private String floorType;
        private String description;
        private int capacity;
        private int currentOccupancy;
        private boolean active;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getBuildingId() {
            return buildingId;
        }

        public void setBuildingId(Long buildingId) {
            this.buildingId = buildingId;
        }

        public int getFloorLevel() {
            return floorLevel;
        }

        public void setFloorLevel(int floorLevel) {
            this.floorLevel = floorLevel;
        }

        public String getFloorType() {
            return floorType;
        }

        public void setFloorType(String floorType) {
            this.floorType = floorType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }

        public int getCurrentOccupancy() {
            return currentOccupancy;
        }

        public void setCurrentOccupancy(int currentOccupancy) {
            this.currentOccupancy = currentOccupancy;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    /**
     * An active dungeon instance in a basement level.
     */
    class DungeonInstance {
        private Long id;
        private Long buildingId;
        private int basementLevel;
        private String templateId;
        private String templateName;
        private String theme;
        private int difficulty;
        private int roomCount;
        private String status;
        private boolean active;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getBuildingId() {
            return buildingId;
        }

        public void setBuildingId(Long buildingId) {
            this.buildingId = buildingId;
        }

        public int getBasementLevel() {
            return basementLevel;
        }

        public void setBasementLevel(int basementLevel) {
            this.basementLevel = basementLevel;
        }

        public String getTemplateId() {
            return templateId;
        }

        public void setTemplateId(String templateId) {
            this.templateId = templateId;
        }

        public String getTemplateName() {
            return templateName;
        }

        public void setTemplateName(String templateName) {
            this.templateName = templateName;
        }

        public String getTheme() {
            return theme;
        }

        public void setTheme(String theme) {
            this.theme = theme;
        }

        public int getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(int difficulty) {
            this.difficulty = difficulty;
        }

        public int getRoomCount() {
            return roomCount;
        }

        public void setRoomCount(int roomCount) {
            this.roomCount = roomCount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    /**
     * A template for dungeon instance generation.
     */
    class DungeonInstanceTemplate {
        private String id;
        private String name;
        private String description;
        private String theme;
        private int minDifficulty;
        private int maxDifficulty;
        private int minRoomCount;
        private int maxRoomCount;
        private String encounterTypes;
        private String trapTypes;
        private String treasureTypes;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getTheme() {
            return theme;
        }

        public void setTheme(String theme) {
            this.theme = theme;
        }

        public int getMinDifficulty() {
            return minDifficulty;
        }

        public void setMinDifficulty(int minDifficulty) {
            this.minDifficulty = minDifficulty;
        }

        public int getMaxDifficulty() {
            return maxDifficulty;
        }

        public void setMaxDifficulty(int maxDifficulty) {
            this.maxDifficulty = maxDifficulty;
        }

        public int getMinRoomCount() {
            return minRoomCount;
        }

        public void setMinRoomCount(int minRoomCount) {
            this.minRoomCount = minRoomCount;
        }

        public int getMaxRoomCount() {
            return maxRoomCount;
        }

        public void setMaxRoomCount(int maxRoomCount) {
            this.maxRoomCount = maxRoomCount;
        }

        public String getEncounterTypes() {
            return encounterTypes;
        }

        public void setEncounterTypes(String encounterTypes) {
            this.encounterTypes = encounterTypes;
        }

        public String getTrapTypes() {
            return trapTypes;
        }

        public void setTrapTypes(String trapTypes) {
            this.trapTypes = trapTypes;
        }

        public String getTreasureTypes() {
            return treasureTypes;
        }

        public void setTreasureTypes(String treasureTypes) {
            this.treasureTypes = treasureTypes;
        }
    }
}
