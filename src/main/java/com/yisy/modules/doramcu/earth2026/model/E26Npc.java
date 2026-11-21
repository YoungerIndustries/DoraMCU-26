package com.yisy.modules.doramcu.earth2026.model;
import com.yisy.modules.doramcu.model.players.Player;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing an Earth 2026 NPC (Non-Player Character).
 * <p>
 * NPCs are characters in the near-future world that the GM controls. This roster tracks
 * key NPCs with their motives, resources, relationships, and disposition toward
 * the player characters.
 * </p>
 * <p>
 * Key mechanics:
 * - Name & Type: NPC identity and role (Detective, Scientist, Mercenary, etc.)
 * - Threat Level: 1-10 for encounter balancing when hostile
 * - Occupation: Modern roles (Doctor, Lawyer, Journalist, Hacker, etc.)
 * - Motives: The NPC's goals, desires, and driving forces
 * - Resources: Cash, items, allies, and location information
 * - Relationships: Connections to other NPCs (allies, enemies, family)
 * - Disposition: Current attitude toward PCs (Hostile, Unfriendly, Indifferent, Friendly, Helpful)
 * - Disposition Score: Numeric disposition (-10 to +10) for tracking changes
 * - Active State: Whether the NPC is still active in the campaign
 * </p>
 */
@Entity
@Table(name = "e26_npcs", indexes = {
        @Index(name = "idx_e26_npc_name", columnList = "name"),
        @Index(name = "idx_e26_npc_type", columnList = "npcType"),
        @Index(name = "idx_e26_npc_disposition", columnList = "disposition"),
        @Index(name = "idx_e26_npc_occupation", columnList = "occupation")
})
public class E26Npc {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Name of the NPC.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Type of NPC: Civilian, Professional, Military, Law Enforcement, Criminal, Corporate, Government, Scientist, Media, Supernatural.
     */
    @Column(name = "npc_type", nullable = false, length = 20)
    private String npcType;

    /**
     * Occupation of the NPC (e.g., Detective, Doctor, Mercenary, Hacker).
     */
    @Column(nullable = false, length = 50)
    private String occupation;

    /**
     * Background story or history of the NPC.
     */
    @Column(length = 500)
    private String background;

    /**
     * Level of the NPC (1-10).
     */
    @Column(nullable = false)
    private int level = 1;

    /**
     * Threat Level of the NPC (1-10) for encounter balancing.
     */
    @Column(name = "threat_level", nullable = false)
    private int threatLevel;

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
     * Cash the NPC possesses (in USD).
     */
    @Column(name = "cash", nullable = false)
    private int cash;

    /**
     * Items or equipment the NPC possesses (JSON array).
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
     * @deprecated Use familyId instead for proper entity relationships.
     */
    @Deprecated
    @Column(length = 500)
    private String family;

    /**
     * Reference to the Earth2026Family this NPC belongs to.
     * Replaces the comma-separated family string with a proper entity relationship.
     */
    @Column(name = "family_id")
    private Long familyId;

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
     * Location or base of operations for the NPC.
     */
    @Column(name = "location", length = 200)
    private String location;

    /**
     * GM-only notes about this NPC.
     */
    @Column(name = "gm_notes", columnDefinition = "TEXT")
    private String gmNotes;

    /**
     * Whether this NPC is fictional (true) or a real-world animal (false).
     * Defaults to true for backward compatibility with existing NPCs.
     */
    @Column(name = "is_fictional", nullable = false)
    private boolean isFictional = true;

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

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(10, level));
    }

    public int getThreatLevel() {
        return threatLevel;
    }

    public void setThreatLevel(int threatLevel) {
        this.threatLevel = Math.max(1, Math.min(10, threatLevel));
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

    public int getCash() {
        return cash;
    }

    public void setCash(int cash) {
        this.cash = Math.max(0, cash);
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

    public Long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(Long familyId) {
        this.familyId = familyId;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getGmNotes() {
        return gmNotes;
    }

    public void setGmNotes(String gmNotes) {
        this.gmNotes = gmNotes;
    }

    public boolean isFictional() {
        return isFictional;
    }

    public void setFictional(boolean fictional) {
        isFictional = fictional;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "E26Npc{name='" + name + "', type='" + npcType + "', occupation='" + occupation + "', disposition='" + disposition + "'}";
    }
}
