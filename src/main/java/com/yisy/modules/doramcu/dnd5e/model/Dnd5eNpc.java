package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a D&D 5e NPC (Non-Player Character).
 * <p>
 * NPCs are characters in the world that the DM controls. This roster tracks
 * key NPCs with their motives, resources, relationships, and disposition toward
 * the player characters.
 * </p>
 * <p>
 * Key mechanics:
 * - Name & Type: NPC identity and creature type (Human, Elf, Dragon, etc.)
 * - Challenge Rating: CR for encounter balancing when hostile
 * - Motives: The NPC's goals, desires, and driving forces
 * - Resources: Gold, items, allies, and lair information
 * - Relationships: Connections to other NPCs (allies, enemies, family)
 * - Disposition: Current attitude toward PCs (Hostile, Unfriendly, Indifferent, Friendly, Helpful)
 * - Disposition Score: Numeric disposition (-10 to +10) for tracking changes
 * - Active State: Whether the NPC is still active in the campaign
 * </p>
 */
@Entity
@Table(name = "dnd5e_npcs", indexes = {
        @Index(name = "idx_dnd5e_npc_name", columnList = "name"),
        @Index(name = "idx_dnd5e_npc_type", columnList = "npcType"),
        @Index(name = "idx_dnd5e_npc_disposition", columnList = "disposition")
})
public class Dnd5eNpc {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Name of the NPC.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Type of NPC: Human, Elf, Dwarf, Dragon, Undead, etc.
     */
    @Column(name = "npc_type", nullable = false, length = 50)
    private String npcType;

    /**
     * Race of the NPC (e.g., Mountain Dwarf, High Elf).
     */
    @Column(length = 50)
    private String race;

    /**
     * Class of the NPC (e.g., Fighter, Wizard, Rogue).
     */
    @Column(length = 50)
    private String npcClass;

    /**
     * Level of the NPC (for class-based NPCs).
     */
    @Column(nullable = false)
    private int level = 1;

    /**
     * Challenge Rating of the NPC (0-30).
     */
    @Column(name = "challenge_rating", nullable = false)
    private int challengeRating;

    /**
     * Description of the NPC's appearance, personality, and role.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * The NPC's primary motive or goal.
     */
    @Column(nullable = false, length = 500)
    private String motive;

    /**
     * Secondary motives or secrets (JSON array).
     */
    @Column(name = "secondary_motives", columnDefinition = "TEXT")
    private String secondaryMotives = "[]";

    /**
     * Hit Points of the NPC.
     */
    @Column(name = "hit_points", nullable = false)
    private int hitPoints;

    /**
     * Armor Class of the NPC.
     */
    @Column(name = "armor_class", nullable = false)
    private int armorClass;

    /**
     * Gold pieces the NPC possesses.
     */
    @Column(name = "gold_pieces", nullable = false)
    private int goldPieces;

    /**
     * Items or treasures the NPC possesses (JSON array).
     */
    @Column(columnDefinition = "TEXT")
    private String items = "[]";

    /**
     * Allies of the NPC (comma-separated NPC names or IDs).
     */
    @Column(length = 500)
    private String allies;

    /**
     * Enemies of the NPC (comma-separated NPC names or IDs).
     */
    @Column(length = 500)
    private String enemies;

    /**
     * Family or close relationships (comma-separated NPC names or IDs).
     */
    @Column(length = 500)
    private String family;

    /**
     * Current disposition toward player characters.
     * Values: Hostile, Unfriendly, Indifferent, Friendly, Helpful
     */
    @Column(nullable = false, length = 20)
    private String disposition = "Indifferent";

    /**
     * Numeric disposition score (-10 to +10).
     * -10 to -6: Hostile
     * -5 to -2: Unfriendly
     * -1 to +1: Indifferent
     * +2 to +5: Friendly
     * +6 to +10: Helpful
     */
    @Column(name = "disposition_score", nullable = false)
    private int dispositionScore = 0;

    /**
     * Whether this NPC is active in the campaign.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Whether this NPC is a key antagonist/villain.
     */
    @Column(name = "is_villain", nullable = false)
    private boolean isVillain = false;

    /**
     * Whether this NPC is a key ally/mentor.
     */
    @Column(name = "is_ally", nullable = false)
    private boolean isAlly = false;

    /**
     * Lair or base of operations for the NPC.
     */
    @Column(name = "lair_location", length = 200)
    private String lairLocation;

    /**
     * DM-only notes about this NPC.
     */
    @Column(columnDefinition = "TEXT")
    private String dmNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

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

    public String getNpcType() {
        return npcType;
    }

    public void setNpcType(String npcType) {
        this.npcType = npcType;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getNpcClass() {
        return npcClass;
    }

    public void setNpcClass(String npcClass) {
        this.npcClass = npcClass;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public int getChallengeRating() {
        return challengeRating;
    }

    public void setChallengeRating(int challengeRating) {
        this.challengeRating = Math.max(0, Math.min(30, challengeRating));
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMotive() {
        return motive;
    }

    public void setMotive(String motive) {
        this.motive = motive;
    }

    public String getSecondaryMotives() {
        return secondaryMotives;
    }

    public void setSecondaryMotives(String secondaryMotives) {
        this.secondaryMotives = secondaryMotives;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public void setHitPoints(int hitPoints) {
        this.hitPoints = Math.max(0, hitPoints);
    }

    public int getArmorClass() {
        return armorClass;
    }

    public void setArmorClass(int armorClass) {
        this.armorClass = armorClass;
    }

    public int getGoldPieces() {
        return goldPieces;
    }

    public void setGoldPieces(int goldPieces) {
        this.goldPieces = Math.max(0, goldPieces);
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public String getAllies() {
        return allies;
    }

    public void setAllies(String allies) {
        this.allies = allies;
    }

    public String getEnemies() {
        return enemies;
    }

    public void setEnemies(String enemies) {
        this.enemies = enemies;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getDisposition() {
        return disposition;
    }

    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    public int getDispositionScore() {
        return dispositionScore;
    }

    public void setDispositionScore(int dispositionScore) {
        this.dispositionScore = Math.max(-10, Math.min(10, dispositionScore));
        // Auto-update disposition string based on score
        if (dispositionScore <= -6) {
            this.disposition = "Hostile";
        } else if (dispositionScore <= -2) {
            this.disposition = "Unfriendly";
        } else if (dispositionScore <= 1) {
            this.disposition = "Indifferent";
        } else if (dispositionScore <= 5) {
            this.disposition = "Friendly";
        } else {
            this.disposition = "Helpful";
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isVillain() {
        return isVillain;
    }

    public void setVillain(boolean villain) {
        isVillain = villain;
    }

    public boolean isAlly() {
        return isAlly;
    }

    public void setAlly(boolean ally) {
        isAlly = ally;
    }

    public String getLairLocation() {
        return lairLocation;
    }

    public void setLairLocation(String lairLocation) {
        this.lairLocation = lairLocation;
    }

    public String getDmNotes() {
        return dmNotes;
    }

    public void setDmNotes(String dmNotes) {
        this.dmNotes = dmNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Dnd5eNpc{name='" + name + "', type='" + npcType + "', CR=" + challengeRating + ", disposition='" + disposition + "'}";
    }
}
