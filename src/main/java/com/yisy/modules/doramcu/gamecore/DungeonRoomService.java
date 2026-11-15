package com.yisy.modules.doramcu.gamecore;

import java.util.List;

/**
 * Shared interface for dungeon/interior room management across all game modules.
 * <p>
 * Used for building interiors, 3D city rooms, structure layouts, and underground facilities.
 * D&amp;D provides the full implementation; Earth2026 and other modules can use this
 * to create interior rooms for buildings, offices, and structures.
 * </p>
 */
public interface DungeonRoomService {

    /**
     * Create a new dungeon/interior room.
     *
     * @param room the room to create
     * @return the created room
     */
    DungeonRoom createRoom(DungeonRoom room);

    /**
     * Get a room by ID.
     *
     * @param id the room ID
     * @return the room or null if not found
     */
    DungeonRoom getRoom(Long id);

    /**
     * List all rooms in a dungeon/building.
     *
     * @param dungeonId the dungeon/building identifier
     * @return list of rooms
     */
    List<DungeonRoom> listRoomsByDungeon(String dungeonId);

    /**
     * List rooms by theme.
     *
     * @param theme the room theme (e.g., "Combat", "Trap", "Treasure", "Social")
     * @return list of rooms
     */
    List<DungeonRoom> listRoomsByTheme(String theme);

    /**
     * Discover a room (mark as discovered).
     *
     * @param id the room ID
     * @return the discovered room
     */
    DungeonRoom discoverRoom(Long id);

    /**
     * Add a connection between two rooms.
     *
     * @param fromId the source room ID
     * @param toId the target room ID
     */
    void addConnection(Long fromId, Long toId);

    /**
     * Get rooms connected to a specific room.
     *
     * @param roomId the room ID
     * @return list of connected rooms
     */
    List<DungeonRoom> getConnectedRooms(Long roomId);

    /**
     * Create circular connections for all rooms in a dungeon.
     *
     * @param dungeonId the dungeon/building identifier
     */
    void createCircularConnections(String dungeonId);

    /**
     * Make a trap check for a room.
     *
     * @param roomId the room ID
     * @param dexModifier dexterity modifier
     * @param proficiencyBonus proficiency bonus
     * @param roll the d20 roll
     * @return the trap check result
     */
    TrapCheckResult makeTrapCheck(Long roomId, int dexModifier, int proficiencyBonus, int roll);

    /**
     * Discover treasure in a room.
     *
     * @param roomId the room ID
     * @return the treasure result
     */
    TreasureResult discoverTreasure(Long roomId);

    /**
     * A dungeon/interior room.
     */
    class DungeonRoom {
        private Long id;
        private String name;
        private String description;
        private String dungeonId;
        private String theme;
        private String size;
        private String connections;
        private String trap;
        private Integer trapDc;
        private String trapDamage;
        private String trapDamageExpr;
        private String treasure;
        private Integer goldPieces;
        private String magicRarity;
        private String encounter;
        private String encounterDifficulty;
        private Integer xpReward;
        private Integer minLevel;
        private Integer maxLevel;
        private boolean discovered;
        private boolean active;
        private String dmSecrets;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getDungeonId() { return dungeonId; }
        public void setDungeonId(String dungeonId) { this.dungeonId = dungeonId; }
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }
        public String getConnections() { return connections; }
        public void setConnections(String connections) { this.connections = connections; }
        public String getTrap() { return trap; }
        public void setTrap(String trap) { this.trap = trap; }
        public Integer getTrapDc() { return trapDc; }
        public void setTrapDc(Integer trapDc) { this.trapDc = trapDc; }
        public String getTrapDamage() { return trapDamage; }
        public void setTrapDamage(String trapDamage) { this.trapDamage = trapDamage; }
        public String getTrapDamageExpr() { return trapDamageExpr; }
        public void setTrapDamageExpr(String trapDamageExpr) { this.trapDamageExpr = trapDamageExpr; }
        public String getTreasure() { return treasure; }
        public void setTreasure(String treasure) { this.treasure = treasure; }
        public Integer getGoldPieces() { return goldPieces; }
        public void setGoldPieces(Integer goldPieces) { this.goldPieces = goldPieces; }
        public String getMagicRarity() { return magicRarity; }
        public void setMagicRarity(String magicRarity) { this.magicRarity = magicRarity; }
        public String getEncounter() { return encounter; }
        public void setEncounter(String encounter) { this.encounter = encounter; }
        public String getEncounterDifficulty() { return encounterDifficulty; }
        public void setEncounterDifficulty(String encounterDifficulty) { this.encounterDifficulty = encounterDifficulty; }
        public Integer getXpReward() { return xpReward; }
        public void setXpReward(Integer xpReward) { this.xpReward = xpReward; }
        public Integer getMinLevel() { return minLevel; }
        public void setMinLevel(Integer minLevel) { this.minLevel = minLevel; }
        public Integer getMaxLevel() { return maxLevel; }
        public void setMaxLevel(Integer maxLevel) { this.maxLevel = maxLevel; }
        public boolean isDiscovered() { return discovered; }
        public void setDiscovered(boolean discovered) { this.discovered = discovered; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public String getDmSecrets() { return dmSecrets; }
        public void setDmSecrets(String dmSecrets) { this.dmSecrets = dmSecrets; }
    }

    /**
     * Result of a trap check.
     */
    class TrapCheckResult {
        private final Long roomId;
        private final String roomName;
        private final Integer dc;
        private final boolean success;
        private final boolean criticalFailure;
        private final int damageMultiplier;
        private final String damageType;
        private final String damageExpr;

        public TrapCheckResult(Long roomId, String roomName, Integer dc, boolean success,
                               boolean criticalFailure, int damageMultiplier,
                               String damageType, String damageExpr) {
            this.roomId = roomId;
            this.roomName = roomName;
            this.dc = dc;
            this.success = success;
            this.criticalFailure = criticalFailure;
            this.damageMultiplier = damageMultiplier;
            this.damageType = damageType;
            this.damageExpr = damageExpr;
        }

        public Long getRoomId() { return roomId; }
        public String getRoomName() { return roomName; }
        public Integer getDc() { return dc; }
        public boolean isSuccess() { return success; }
        public boolean isCriticalFailure() { return criticalFailure; }
        public int getDamageMultiplier() { return damageMultiplier; }
        public String getDamageType() { return damageType; }
        public String getDamageExpr() { return damageExpr; }
    }

    /**
     * Result of treasure discovery.
     */
    class TreasureResult {
        private final Long roomId;
        private final String roomName;
        private final String treasure;
        private final int goldPieces;
        private final String magicRarity;

        public TreasureResult(Long roomId, String roomName, String treasure,
                              int goldPieces, String magicRarity) {
            this.roomId = roomId;
            this.roomName = roomName;
            this.treasure = treasure;
            this.goldPieces = goldPieces;
            this.magicRarity = magicRarity;
        }

        public Long getRoomId() { return roomId; }
        public String getRoomName() { return roomName; }
        public String getTreasure() { return treasure; }
        public int getGoldPieces() { return goldPieces; }
        public String getMagicRarity() { return magicRarity; }
    }
}
