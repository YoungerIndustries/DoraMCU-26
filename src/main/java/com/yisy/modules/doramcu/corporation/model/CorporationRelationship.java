package com.yisy.modules.doramcu.corporation.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Tracks bilateral relationships between corporations.
 * <p>
 * Each relationship represents the connection between two corporations,
 * including the relationship type, trust level, and interaction history.
 * </p>
 */
@Entity
@Table(name = "corporation_relationships", indexes = {
        @Index(name = "idx_corp_rel_corp1", columnList = "corporation1Id"),
        @Index(name = "idx_corp_rel_corp2", columnList = "corporation2Id"),
        @Index(name = "idx_corp_rel_type", columnList = "relationshipType")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_corp_rel_pair", columnNames = {"corporation1Id", "corporation2Id"})
})
public class CorporationRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "corporation1_id", nullable = false)
    private Long corporation1Id;

    @Column(name = "corporation2_id", nullable = false)
    private Long corporation2Id;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false, length = 20)
    private RelationshipType relationshipType = RelationshipType.NEUTRAL;

    @Column(name = "trust_level", nullable = false)
    private Integer trustLevel = 0; // 0-100

    @Column(name = "established_at", nullable = false)
    private LocalDateTime establishedAt;

    @Column(name = "last_interaction_at")
    private LocalDateTime lastInteractionAt;

    @Version
    private Integer version;

    public CorporationRelationship() {
        this.establishedAt = LocalDateTime.now();
    }

    public CorporationRelationship(Long corporation1Id, Long corporation2Id) {
        this();
        this.corporation1Id = corporation1Id;
        this.corporation2Id = corporation2Id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCorporation1Id() {
        return corporation1Id;
    }

    public void setCorporation1Id(Long corporation1Id) {
        this.corporation1Id = corporation1Id;
    }

    public Long getCorporation2Id() {
        return corporation2Id;
    }

    public void setCorporation2Id(Long corporation2Id) {
        this.corporation2Id = corporation2Id;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(RelationshipType relationshipType) {
        this.relationshipType = relationshipType;
        this.lastInteractionAt = LocalDateTime.now();
    }

    public Integer getTrustLevel() {
        return trustLevel;
    }

    public void setTrustLevel(Integer trustLevel) {
        this.trustLevel = Math.max(0, Math.min(100, trustLevel));
    }

    public void increaseTrust(int amount) {
        this.trustLevel = Math.min(100, this.trustLevel + amount);
        this.lastInteractionAt = LocalDateTime.now();
    }

    public void decreaseTrust(int amount) {
        this.trustLevel = Math.max(0, this.trustLevel - amount);
        this.lastInteractionAt = LocalDateTime.now();
    }

    public LocalDateTime getEstablishedAt() {
        return establishedAt;
    }

    public void setEstablishedAt(LocalDateTime establishedAt) {
        this.establishedAt = establishedAt;
    }

    public LocalDateTime getLastInteractionAt() {
        return lastInteractionAt;
    }

    public void setLastInteractionAt(LocalDateTime lastInteractionAt) {
        this.lastInteractionAt = lastInteractionAt;
    }

    public Integer getVersion() {
        return version;
    }

    /**
     * Check if the relationship is friendly (trade partner, joint venture, or ally).
     */
    public boolean isFriendly() {
        return relationshipType == RelationshipType.TRADE_PARTNER
                || relationshipType == RelationshipType.JOINT_VENTURE
                || relationshipType == RelationshipType.ALLY;
    }

    /**
     * Check if the relationship is hostile.
     */
    public boolean isHostile() {
        return relationshipType == RelationshipType.HOSTILE;
    }

    /**
     * Check if a trade agreement can be proposed.
     */
    public boolean canProposeTrade() {
        return relationshipType == RelationshipType.NEUTRAL
                || relationshipType == RelationshipType.TRADE_PARTNER;
    }

    /**
     * Check if a joint venture can be proposed.
     */
    public boolean canProposeJointVenture() {
        return relationshipType == RelationshipType.TRADE_PARTNER
                || relationshipType == RelationshipType.ALLY;
    }

    @Override
    public String toString() {
        return "CorporationRelationship{" +
                "id=" + id +
                ", corporation1Id=" + corporation1Id +
                ", corporation2Id=" + corporation2Id +
                ", relationshipType=" + relationshipType +
                ", trustLevel=" + trustLevel +
                '}';
    }

    /**
     * Types of corporation relationships.
     */
    public enum RelationshipType {
        NEUTRAL("Neutral", "No special relationship"),
        TRADE_PARTNER("Trade Partner", "Mutual trade benefits"),
        JOINT_VENTURE("Joint Venture", "Active collaboration on contracts"),
        HOSTILE("Hostile", "Adversarial relationship"),
        ALLY("Ally", "Strong mutual alliance");

        private final String displayName;
        private final String description;

        RelationshipType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }
}
