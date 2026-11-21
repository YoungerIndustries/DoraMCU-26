package com.yisy.modules.doramcu.earth2026.model;
import com.yisy.modules.doramcu.model.players.Player;

import jakarta.persistence.*;

/**
 * JPA entity representing a universal relationship connection between any two entities.
 * <p>
 * This is the core of the connection network, enabling relationships between:
 * - Characters, NPCs, AI Characters, Companions, and Monsters
 * </p>
 * <p>
 * Key mechanics:
 * - Source/Target: Any entity type can connect to any other entity type
 * - Connection Type: Family, Ally, Enemy, Mentor, Romantic, Employer, Employee, Spy Handler, Spy Asset, Guild Member, Rival, Debtor, Creditor, etc.
 * - Strength: 0-100, relationship bond/trust level
 * - Secrecy Level: 0-100, how hidden this relationship is (0=public, 100=classified)
 * - Notes: GM notes about this relationship
 * </p>
 * <p>
 * Use cases:
 * - Espionage rings: Spy handlers connected to field agents
 * - Merchant guilds: Suppliers connected to merchants
 * - Villain organizations: Henchmen connected to villains
 * - Family trees: Extended family connections across entity types
 * </p>
 */
@Entity
@Table(name = "e26_connections", indexes = {
        @Index(name = "idx_e26_connection_source", columnList = "sourceType,sourceId"),
        @Index(name = "idx_e26_connection_target", columnList = "targetType,targetId"),
        @Index(name = "idx_e26_connection_type", columnList = "connectionType"),
        @Index(name = "idx_e26_connection_active", columnList = "active")
})
public class Earth2026Connection {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Type of the source entity (the entity initiating the connection).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private EntityType sourceType;

    /**
     * ID of the source entity.
     */
    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    /**
     * Type of the target entity (the entity being connected to).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private EntityType targetType;

    /**
     * ID of the target entity.
     */
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    /**
     * Type of connection: Family, Ally, Enemy, Mentor, Romantic, Employer, Employee, etc.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "connection_type", nullable = false, length = 20)
    private ConnectionType connectionType;

    /**
     * Strength of the connection (0-100). Higher means stronger bond/trust.
     */
    @Column(name = "strength")
    private int strength = 50;

    /**
     * Secrecy level (0-100). 0=public knowledge, 100=highly classified.
     * Used for espionage mechanics to determine if a relationship is discoverable.
     */
    @Column(name = "secrecy_level")
    private int secrecyLevel = 0;

    /**
     * Whether this connection is active.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * GM notes about this relationship. Free-form text for context.
     */
    @Column(length = 1000)
    private String notes;

    /**
     * Types of entities that can participate in connections.
     */
    public enum EntityType {
        /** Earth2026Character — player characters. */
        CHARACTER,
        /** E26Npc — non-player characters. */
        NPC,
        /** AiCharacter — AI-driven entities. */
        AI_CHARACTER,
        /** Earth2026Companion — pets, familiars, service animals. */
        COMPANION,
        /** E26Monster — monsters and creatures. */
        MONSTER
    }

    /**
     * Types of connections between entities.
     */
    public enum ConnectionType {
        /** Family relationship (blood or marriage). */
        FAMILY,
        /** Ally or supporter. */
        ALLY,
        /** Enemy or adversary. */
        ENEMY,
        /** Mentor or teacher. */
        MENTOR,
        /** Romantic partner. */
        ROMANTIC,
        /** Employer (source employs target). */
        EMPLOYER,
        /** Employee (source is employed by target). */
        EMPLOYEE,
        /** Spy handler (source handles target as a spy). */
        SPY_HANDLER,
        /** Spy asset (source is a spy asset of target). */
        SPY_ASSET,
        /** Guild member connection. */
        GUILD_MEMBER,
        /** Rival or competitor. */
        RIVAL,
        /** Debtor (source owes target). */
        DEBTOR,
        /** Creditor (source is owed by target). */
        CREDITOR,
        /** Informant (source provides information to target). */
        INFORMANT,
        /** Double agent (source secretly works for target). */
        DOUBLE_AGENT,
        /** Blackmail target (source is blackmailed by target). */
        BLACKMAILED,
        /** Debt of honor (source owes target a favor). */
        DEBT_OF_HONOR,
        /** Other custom connection type. */
        OTHER
    }

    public Earth2026Connection() {
    }

    public Earth2026Connection(EntityType sourceType, Long sourceId, EntityType targetType, Long targetId, ConnectionType connectionType) {
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.connectionType = connectionType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EntityType getSourceType() {
        return sourceType;
    }

    public void setSourceType(EntityType sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public EntityType getTargetType() {
        return targetType;
    }

    public void setTargetType(EntityType targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = Math.max(0, Math.min(100, strength));
    }

    public int getSecrecyLevel() {
        return secrecyLevel;
    }

    public void setSecrecyLevel(int secrecyLevel) {
        this.secrecyLevel = Math.max(0, Math.min(100, secrecyLevel));
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Increases the connection strength.
     */
    public void increaseStrength(int amount) {
        this.strength = Math.min(100, this.strength + amount);
    }

    /**
     * Decreases the connection strength.
     */
    public void decreaseStrength(int amount) {
        this.strength = Math.max(0, this.strength - amount);
    }

    @Override
    public String toString() {
        return "Earth2026Connection{id=" + id + ", source=" + sourceType + ":" + sourceId +
                ", target=" + targetType + ":" + targetId +
                ", type=" + connectionType + ", strength=" + strength +
                ", secrecy=" + secrecyLevel + "}";
    }
}
