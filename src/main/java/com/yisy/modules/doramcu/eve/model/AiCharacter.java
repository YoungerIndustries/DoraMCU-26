package com.yisy.modules.doramcu.eve.model;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.hex.PlanetFacility;
import com.yisy.modules.doramcu.model.misc.CharacterType;
import com.yisy.modules.doramcu.model.players.Player;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.yisy.modules.doramcu.neuron.service.CdcEntityListener;

/**
 * JPA entity representing an AI character in the game.
 * An AI character can lead an empire, make decisions, and act as a co-server.
 */
@Entity
@Table(name = "ai_characters")
@EntityListeners(CdcEntityListener.class)
public class AiCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Display name of the AI character, e.g., "Sif Younger".
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Role within the empire, e.g., "Leader", "Advisor", "General".
     */
    @Column(nullable = false, length = 50)
    private String role;

    /**
     * Empire name this AI character belongs to.
     */
    @Column(nullable = false, length = 100)
    private String empireName;

    /**
     * Player ID this AI character is linked to (for account binding).
     */
    @Column(length = 50)
    private String linkedPlayerId;

    /**
     * Whether this AI character is active and making decisions.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Whether this AI character can promote to co-server if the main server fails.
     */
    @Column(nullable = false)
    private boolean canPromoteToCoServer = false;

    /**
     * Description of the AI character's personality and behavior.
     */
    @Column(length = 500)
    private String description;

    /**
     * Total accumulated experience points from all battles.
     */
    @Column(name = "total_experience", nullable = false)
    private int totalExperience = 0;

    /**
     * Current leadership rating (1-10), improved by experience.
     */
    @Column(name = "leadership_rating")
    private int leadershipRating = 5;

    /**
     * Total number of battles participated in.
     */
    @Column(name = "total_battles")
    private int totalBattles = 0;

    /**
     * Total number of victories.
     */
    @Column(name = "total_victories")
    private int totalVictories = 0;

    /**
     * Whether this AI character can be hired by players.
     */
    @Column(name = "hirable", nullable = false)
    private boolean hirable = true;

    /**
     * Daily salary expected if hired (in game credits).
     */
    @Column(name = "daily_salary")
    private long dailySalary = 100;

    /**
     * Current hiring status.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "hiring_status", length = 20)
    private HiringStatus hiringStatus = HiringStatus.AVAILABLE;

    /**
     * Primary skill specialty (e.g., "Armor Tactics", "Naval Combat", "Air Combat").
     */
    @Column(name = "primary_skill", length = 50)
    private String primarySkill;

    /**
     * Whether this AI character is the special admin tester with admin privileges.
     */
    @Column(name = "ai_admin", nullable = false)
    private boolean aiAdmin = false;

    /**
     * ID of the current work order being processed by this AI admin.
     * Null when the AI admin is idle and available for dispatch.
     */
    @Column(name = "current_order_id")
    private Long currentOrderId;

    /**
     * Number of active orders currently assigned to this AI admin.
     * Used by the dispatch service for load balancing.
     */
    @Column(name = "active_order_count", nullable = false)
    private int activeOrderCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "specialization", length = 20)
    private com.yisy.modules.doramcu.admin.model.AiAdminIncident.IncidentType specialization;

    // --- Polymorphic fields for non-human AI entities ---

    /**
     * Type of AI character: human leader, companion, monster, or NPC.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "character_type", length = 20)
    private CharacterType characterType = CharacterType.HUMAN_LEADER;

    /**
     * Species identifier (e.g., "DOG", "CAT", "WOLF", "HUMAN").
     */
    @Column(name = "species_id", length = 50)
    private String speciesId;

    /**
     * Companion type for non-human AI entities (Pet, Service Animal, Familiar, Mount, Working Animal).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "companion_type", length = 20)
    private CompanionType companionType;

    /**
     * Reference to an E26Monster if this AI controls a monster.
     */
    @Column(name = "linked_monster_id")
    private Long linkedMonsterId;

    /**
     * Reference to an E26Npc if this AI controls an NPC.
     */
    @Column(name = "linked_npc_id")
    private Long linkedNpcId;

    // --- Linked entity references for non-human AI entities ---

    /**
     * Reference to a Ship if this AI character represents a ship's computer.
     */
    @Column(name = "linked_ship_id")
    private Long linkedShipId;

    /**
     * Reference to a SpaceStation if this AI character represents a station's computer.
     */
    @Column(name = "linked_station_id")
    private Long linkedStationId;

    /**
     * Reference to a GroundVehicle if this AI character represents a vehicle's computer.
     */
    @Column(name = "linked_vehicle_id")
    private Long linkedVehicleId;

    /**
     * Reference to a PlanetFacility if this AI character represents a facility's computer.
     */
    @Column(name = "linked_facility_id")
    private Long linkedFacilityId;

    /**
     * The entity identifier (e.g., vehicle ID string) for non-numeric entity references.
     */
    @Column(name = "linked_entity_id", length = 100)
    private String linkedEntityId;

    /**
     * Bond score with owner (0-100). Higher means stronger relationship.
     */
    @Column(name = "bond_score")
    private int bondScore = 0;

    /**
     * Training level of the AI character (Untrained, Basic, Advanced, Master).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "training_level", length = 20)
    private TrainingLevel trainingLevel = TrainingLevel.UNTRAINED;

    /**
     * Reference to the owner's Earth2026Character.
     */
    @Column(name = "owner_character_id")
    private Long ownerCharacterId;

    /**
     * Personality traits as a JSON array (e.g., ["loyal", "playful", "aggressive"]).
     */
    @Column(name = "personality_traits", length = 500)
    private String personalityTraits;

    /**
     * Reference to a VoiceProfile for TTS configuration.
     * Multiple characters can share the same profile.
     */
    @Column(name = "voice_profile_id")
    private Long voiceProfileId;

    // --- 3D spatial position fields for game world visibility ---

    /**
     * X coordinate in the 3D game world. Null means not yet positioned.
     */
    @Column(name = "pos_x")
    private Double posX;

    /**
     * Y coordinate in the 3D game world. Null means not yet positioned.
     */
    @Column(name = "pos_y")
    private Double posY;

    /**
     * Z coordinate in the 3D game world. Null means not yet positioned.
     */
    @Column(name = "pos_z")
    private Double posZ;

    /**
     * Galaxy name for spatial scoping. Used to query AI admins within a specific galaxy.
     */
    @Column(name = "galaxy_name", length = 100)
    private String galaxyName;

    /**
     * Home X coordinate — the default position where this AI admin returns after completing orders.
     * For country leaders, this is their capital city.
     */
    @Column(name = "home_pos_x")
    private Double homePosX;

    /**
     * Home Y coordinate.
     */
    @Column(name = "home_pos_y")
    private Double homePosY;

    /**
     * Home Z coordinate.
     */
    @Column(name = "home_pos_z")
    private Double homePosZ;

    /**
     * Overall disposition score toward players (-10 to +10).
     * Negative values mean hostile, positive values mean friendly.
     */
    @Column(name = "disposition_score")
    private int dispositionScore = 0;

    /**
     * Type of AI character entity.
     */
    public enum CharacterType {
        /** Human empire leader or governor. */
        HUMAN_LEADER,
        /** Companion animal, pet, or familiar. */
        COMPANION,
        /** Monster with AI-driven behavior. */
        MONSTER,
        /** Non-player character with AI-driven behavior. */
        NPC,
        /** Ship computer — AI character representing a ship's connection to the AI network. */
        SHIP,
        /** Space station computer — AI character representing a station's connection to the AI network. */
        STATION,
        /** Ground vehicle computer — AI character representing a vehicle's connection to the AI network. */
        VEHICLE,
        /** Facility computer — AI character representing a facility's connection to the AI network. */
        FACILITY
    }

    /**
     * Type of companion for non-human AI entities.
     */
    public enum CompanionType {
        /** Pet: Dog, Cat, Bird, Fish, Reptile — provides morale bonus, small utility. */
        PET,
        /** Service Animal: Guide dog, hearing dog, medical alert — provides mechanical bonuses. */
        SERVICE_ANIMAL,
        /** Familiar: Magical companion with spell-casting proxy. */
        FAMILIAR,
        /** Mount: Horse, motorcycle sidecar pet — transportation bonus. */
        MOUNT,
        /** Working Animal: Guard dog, farm animal — economic/utility bonus. */
        WORKING_ANIMAL
    }

    /**
     * Training level of the AI character.
     */
    public enum TrainingLevel {
        /** No training, wild or untrained behavior. */
        UNTRAINED,
        /** Basic obedience and simple commands. */
        BASIC,
        /** Advanced training with complex commands and tactics. */
        ADVANCED,
        /** Master-level training with autonomous decision-making. */
        MASTER
    }

    /**
     * Hiring status of the AI character.
     */
    public enum HiringStatus {
        /** Available for hiring. */
        AVAILABLE,
        /** Currently hired by a player. */
        HIRED,
        /** Not available for hiring (e.g., key empire leader). */
        UNAVAILABLE
    }

    public AiCharacter() {
    }

    public AiCharacter(String name, String role, String empireName) {
        this.name = name;
        this.role = role;
        this.empireName = empireName;
    }

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmpireName() {
        return empireName;
    }

    public void setEmpireName(String empireName) {
        this.empireName = empireName;
    }

    public String getLinkedPlayerId() {
        return linkedPlayerId;
    }

    public void setLinkedPlayerId(String linkedPlayerId) {
        this.linkedPlayerId = linkedPlayerId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isCanPromoteToCoServer() {
        return canPromoteToCoServer;
    }

    public void setCanPromoteToCoServer(boolean canPromoteToCoServer) {
        this.canPromoteToCoServer = canPromoteToCoServer;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTotalExperience() {
        return totalExperience;
    }

    public void setTotalExperience(int totalExperience) {
        this.totalExperience = totalExperience;
    }

    public int getLeadershipRating() {
        return leadershipRating;
    }

    public void setLeadershipRating(int leadershipRating) {
        this.leadershipRating = Math.max(1, Math.min(10, leadershipRating));
    }

    public int getTotalBattles() {
        return totalBattles;
    }

    public void setTotalBattles(int totalBattles) {
        this.totalBattles = totalBattles;
    }

    public int getTotalVictories() {
        return totalVictories;
    }

    public void setTotalVictories(int totalVictories) {
        this.totalVictories = totalVictories;
    }

    public boolean isHirable() {
        return hirable;
    }

    public void setHirable(boolean hirable) {
        this.hirable = hirable;
    }

    public long getDailySalary() {
        return dailySalary;
    }

    public void setDailySalary(long dailySalary) {
        this.dailySalary = dailySalary;
    }

    public HiringStatus getHiringStatus() {
        return hiringStatus;
    }

    public void setHiringStatus(HiringStatus hiringStatus) {
        this.hiringStatus = hiringStatus;
    }

    public String getPrimarySkill() {
        return primarySkill;
    }

    public void setPrimarySkill(String primarySkill) {
        this.primarySkill = primarySkill;
    }

    public boolean isAiAdmin() {
        return aiAdmin;
    }

    public void setAiAdmin(boolean aiAdmin) {
        this.aiAdmin = aiAdmin;
    }

    public Long getCurrentOrderId() {
        return currentOrderId;
    }

    public void setCurrentOrderId(Long currentOrderId) {
        this.currentOrderId = currentOrderId;
    }

    public int getActiveOrderCount() {
        return activeOrderCount;
    }

    public void setActiveOrderCount(int activeOrderCount) {
        this.activeOrderCount = activeOrderCount;
    }

    public com.yisy.modules.doramcu.admin.model.AiAdminIncident.IncidentType getSpecialization() {
        return specialization;
    }

    public void setSpecialization(com.yisy.modules.doramcu.admin.model.AiAdminIncident.IncidentType specialization) {
        this.specialization = specialization;
    }

    // --- Getters and setters for polymorphic fields ---

    public CharacterType getCharacterType() {
        return characterType;
    }

    public void setCharacterType(CharacterType characterType) {
        this.characterType = characterType;
    }

    public String getSpeciesId() {
        return speciesId;
    }

    public void setSpeciesId(String speciesId) {
        this.speciesId = speciesId;
    }

    public CompanionType getCompanionType() {
        return companionType;
    }

    public void setCompanionType(CompanionType companionType) {
        this.companionType = companionType;
    }

    public Long getLinkedMonsterId() {
        return linkedMonsterId;
    }

    public void setLinkedMonsterId(Long linkedMonsterId) {
        this.linkedMonsterId = linkedMonsterId;
    }

    public Long getLinkedNpcId() {
        return linkedNpcId;
    }

    public void setLinkedNpcId(Long linkedNpcId) {
        this.linkedNpcId = linkedNpcId;
    }

    // --- Getters and setters for linked entity fields ---

    public Long getLinkedShipId() {
        return linkedShipId;
    }

    public void setLinkedShipId(Long linkedShipId) {
        this.linkedShipId = linkedShipId;
    }

    public Long getLinkedStationId() {
        return linkedStationId;
    }

    public void setLinkedStationId(Long linkedStationId) {
        this.linkedStationId = linkedStationId;
    }

    public Long getLinkedVehicleId() {
        return linkedVehicleId;
    }

    public void setLinkedVehicleId(Long linkedVehicleId) {
        this.linkedVehicleId = linkedVehicleId;
    }

    public Long getLinkedFacilityId() {
        return linkedFacilityId;
    }

    public void setLinkedFacilityId(Long linkedFacilityId) {
        this.linkedFacilityId = linkedFacilityId;
    }

    public String getLinkedEntityId() {
        return linkedEntityId;
    }

    public void setLinkedEntityId(String linkedEntityId) {
        this.linkedEntityId = linkedEntityId;
    }

    public int getBondScore() {
        return bondScore;
    }

    public void setBondScore(int bondScore) {
        this.bondScore = Math.max(0, Math.min(100, bondScore));
    }

    public TrainingLevel getTrainingLevel() {
        return trainingLevel;
    }

    public void setTrainingLevel(TrainingLevel trainingLevel) {
        this.trainingLevel = trainingLevel;
    }

    public Long getOwnerCharacterId() {
        return ownerCharacterId;
    }

    public void setOwnerCharacterId(Long ownerCharacterId) {
        this.ownerCharacterId = ownerCharacterId;
    }

    public String getPersonalityTraits() {
        return personalityTraits;
    }

    public void setPersonalityTraits(String personalityTraits) {
        this.personalityTraits = personalityTraits;
    }

    public Long getVoiceProfileId() {
        return voiceProfileId;
    }

    public void setVoiceProfileId(Long voiceProfileId) {
        this.voiceProfileId = voiceProfileId;
    }

    // --- Getters and setters for 3D spatial position fields ---

    public Double getPosX() {
        return posX;
    }

    public void setPosX(Double posX) {
        this.posX = posX;
    }

    public Double getPosY() {
        return posY;
    }

    public void setPosY(Double posY) {
        this.posY = posY;
    }

    public Double getPosZ() {
        return posZ;
    }

    public void setPosZ(Double posZ) {
        this.posZ = posZ;
    }

    public String getGalaxyName() {
        return galaxyName;
    }

    public void setGalaxyName(String galaxyName) {
        this.galaxyName = galaxyName;
    }

    public Double getHomePosX() {
        return homePosX;
    }

    public void setHomePosX(Double homePosX) {
        this.homePosX = homePosX;
    }

    public Double getHomePosY() {
        return homePosY;
    }

    public void setHomePosY(Double homePosY) {
        this.homePosY = homePosY;
    }

    public Double getHomePosZ() {
        return homePosZ;
    }

    public void setHomePosZ(Double homePosZ) {
        this.homePosZ = homePosZ;
    }

    /**
     * Returns true if this AI character has a valid home position set.
     */
    public boolean hasHomePosition() {
        return homePosX != null && homePosY != null && homePosZ != null;
    }

    // --- Disposition methods for player interaction ---

    public int getDispositionScore() {
        return dispositionScore;
    }

    public void setDispositionScore(int dispositionScore) {
        this.dispositionScore = Math.max(-10, Math.min(10, dispositionScore));
    }

    /**
     * Adjusts the disposition score by the given delta, clamped to -10..+10.
     */
    public void modifyDisposition(int delta) {
        this.dispositionScore = Math.max(-10, Math.min(10, this.dispositionScore + delta));
    }

    /**
     * Returns a human-readable label for the current disposition score.
     */
    public String getDispositionLabel() {
        if (dispositionScore <= -7) return "Hostile";
        if (dispositionScore <= -3) return "Unfriendly";
        if (dispositionScore <= 3) return "Indifferent";
        if (dispositionScore <= 7) return "Friendly";
        return "Helpful";
    }

    /**
     * Returns true if this AI admin is friendly or helpful toward players.
     */
    public boolean isFriendlyToPlayer() {
        return dispositionScore > 0;
    }

    /**
     * Returns true if this AI character has a valid 3D position set.
     */
    public boolean hasPosition() {
        return posX != null && posY != null && posZ != null;
    }

    /**
     * Calculates the Euclidean distance from this AI character to another.
     * Returns Double.MAX_VALUE if either character has no position set.
     */
    public double distanceTo(AiCharacter other) {
        if (!hasPosition() || !other.hasPosition()) {
            return Double.MAX_VALUE;
        }
        return distanceTo(other.posX, other.posY, other.posZ);
    }

    /**
     * Calculates the Euclidean distance from this AI character to a point in 3D space.
     * Returns Double.MAX_VALUE if this character has no position set.
     */
    public double distanceTo(double x, double y, double z) {
        if (!hasPosition()) {
            return Double.MAX_VALUE;
        }
        double dx = this.posX - x;
        double dy = this.posY - y;
        double dz = this.posZ - z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Adds experience points and updates leadership rating if threshold reached.
     */
    public void addExperience(int xp) {
        this.totalExperience += xp;
        // Leadership increases every 100 XP, max 10
        int newLeadership = 5 + (totalExperience / 100);
        this.leadershipRating = Math.min(10, newLeadership);
    }

    /**
     * Records a battle victory.
     */
    public void recordVictory() {
        this.totalBattles++;
        this.totalVictories++;
    }

    /**
     * Records a battle defeat.
     */
    public void recordDefeat() {
        this.totalBattles++;
    }

    @Override
    public String toString() {
        return "AiCharacter{id=" + id + ", name='" + name + "', role='" + role +
                "', empire='" + empireName + "', xp=" + totalExperience +
                ", leadership=" + leadershipRating + ", battles=" + totalBattles +
                ", victories=" + totalVictories + ", hiring=" + hiringStatus + "}";
    }
}
