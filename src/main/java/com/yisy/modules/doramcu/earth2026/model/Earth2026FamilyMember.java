package com.yisy.modules.doramcu.earth2026.model;
import com.yisy.modules.doramcu.model.players.Player;

import jakarta.persistence.*;

/**
 * JPA entity representing a member of an Earth 2026 family.
 * <p>
 * This is a join entity that links characters or NPCs to a family with
 * relationship types and bond scores.
 * </p>
 * <p>
 * Key mechanics:
 * - Family: Reference to the parent family
 * - Character ID: Reference to an Earth2026Character (player character)
 * - NPC ID: Reference to an E26Npc (non-player character)
 * - Relationship Type: Spouse, Parent, Child, Sibling, Extended, Friend, Mentor, Rival, Ally, Enemy
 * - Bond Score: 0-100, relationship strength
 * </p>
 */
@Entity
@Table(name = "e26_family_members", indexes = {
        @Index(name = "idx_e26_family_member_family", columnList = "familyId"),
        @Index(name = "idx_e26_family_member_character", columnList = "characterId"),
        @Index(name = "idx_e26_family_member_npc", columnList = "npcId")
})
public class Earth2026FamilyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Reference to the parent family.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Earth2026Family family;

    /**
     * Family ID for quick queries (denormalized from family reference).
     */
    @Column(name = "family_id", insertable = false, updatable = false)
    private Long familyId;

    /**
     * Reference to an Earth2026Character (player character).
     * Null if this is an NPC-only member.
     */
    @Column(name = "character_id")
    private Long characterId;

    /**
     * Reference to an E26Npc (non-player character).
     * Null if this is a player character-only member.
     */
    @Column(name = "npc_id")
    private Long npcId;

    /**
     * Type of relationship: Spouse, Parent, Child, Sibling, Extended, Friend, Mentor, Rival, Ally, Enemy.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false, length = 20)
    private RelationshipType relationshipType;

    /**
     * Bond score with the family (0-100). Higher means stronger relationship.
     */
    @Column(name = "bond_score")
    private int bondScore = 0;

    /**
     * Whether this membership is active.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Type of relationship within the family.
     */
    public enum RelationshipType {
        /** Spouse or partner. */
        SPOUSE,
        /** Parent. */
        PARENT,
        /** Child. */
        CHILD,
        /** Sibling (brother or sister). */
        SIBLING,
        /** Extended family (aunt, uncle, cousin, etc.). */
        EXTENDED,
        /** Close friend (not blood-related). */
        FRIEND,
        /** Mentor or teacher. */
        MENTOR,
        /** Rival or competitor. */
        RIVAL,
        /** Ally or supporter. */
        ALLY,
        /** Enemy or adversary. */
        ENEMY
    }

    public Earth2026FamilyMember() {
    }

    public Earth2026FamilyMember(Earth2026Family family, RelationshipType relationshipType) {
        this.family = family;
        this.relationshipType = relationshipType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Earth2026Family getFamily() {
        return family;
    }

    public void setFamily(Earth2026Family family) {
        this.family = family;
    }

    public Long getFamilyId() {
        return familyId;
    }

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public Long getNpcId() {
        return npcId;
    }

    public void setNpcId(Long npcId) {
        this.npcId = npcId;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(RelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }

    public int getBondScore() {
        return bondScore;
    }

    public void setBondScore(int bondScore) {
        this.bondScore = Math.max(0, Math.min(100, bondScore));
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Increases the bond score with the family.
     */
    public void increaseBond(int amount) {
        this.bondScore = Math.min(100, this.bondScore + amount);
    }

    /**
     * Decreases the bond score with the family.
     */
    public void decreaseBond(int amount) {
        this.bondScore = Math.max(0, this.bondScore - amount);
    }

    @Override
    public String toString() {
        return "Earth2026FamilyMember{id=" + id + ", familyId=" + familyId +
                ", characterId=" + characterId + ", npcId=" + npcId +
                ", relationship=" + relationshipType + ", bond=" + bondScore + "}";
    }
}
