package com.yisy.modules.doramcu.fourx;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JPA entity for persistent 4X faction data.
 * <p>
 * Stores faction information in the database for cross-session persistence.
 * Each faction belongs to a game session and tracks its resources, reputation,
 * and controlled territory.
 * </p>
 */
@Entity
@Table(name = "fourx_factions")
public class FourxFactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String factionId;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 50)
    private String gameModule;

    @Column(length = 20)
    private String combatStyle;

    @Column(length = 20)
    private String defaultStance;

    /** Reputation scores serialized as JSON. */
    @Column(columnDefinition = "TEXT")
    private String reputationMapJson;

    /** Controlled sector IDs serialized as JSON. */
    @Column(columnDefinition = "TEXT")
    private String controlledSectorsJson;

    @Column(nullable = false)
    private int fleetStrength = 0;

    @Column(nullable = false)
    private int colonyCount = 0;

    @Column(nullable = false)
    private int researchPoints = 0;

    @Column(nullable = false)
    private int economyOutput = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private FourxSessionEntity session;

    public FourxFactionEntity() {
    }

    public FourxFactionEntity(String factionId, String name, String gameModule) {
        this.factionId = factionId;
        this.name = name;
        this.gameModule = gameModule;
        this.defaultStance = "NEUTRAL";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFactionId() { return factionId; }
    public void setFactionId(String factionId) { this.factionId = factionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGameModule() { return gameModule; }
    public void setGameModule(String gameModule) { this.gameModule = gameModule; }

    public String getCombatStyle() { return combatStyle; }
    public void setCombatStyle(String combatStyle) { this.combatStyle = combatStyle; }

    public String getDefaultStance() { return defaultStance; }
    public void setDefaultStance(String defaultStance) { this.defaultStance = defaultStance; }

    public String getReputationMapJson() { return reputationMapJson; }
    public void setReputationMapJson(String reputationMapJson) { this.reputationMapJson = reputationMapJson; }

    public String getControlledSectorsJson() { return controlledSectorsJson; }
    public void setControlledSectorsJson(String controlledSectorsJson) { this.controlledSectorsJson = controlledSectorsJson; }

    public int getFleetStrength() { return fleetStrength; }
    public void setFleetStrength(int fleetStrength) { this.fleetStrength = fleetStrength; }

    public int getColonyCount() { return colonyCount; }
    public void setColonyCount(int colonyCount) { this.colonyCount = colonyCount; }

    public int getResearchPoints() { return researchPoints; }
    public void setResearchPoints(int researchPoints) { this.researchPoints = researchPoints; }

    public int getEconomyOutput() { return economyOutput; }
    public void setEconomyOutput(int economyOutput) { this.economyOutput = economyOutput; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public FourxSessionEntity getSession() { return session; }
    public void setSession(FourxSessionEntity session) { this.session = session; }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Convert to the shared DTO.
     */
    public FourxFaction toDto() {
        FourxFaction dto = new FourxFaction(factionId, name, gameModule);
        dto.setDescription(description);
        dto.setCombatStyle(combatStyle);
        if (defaultStance != null) {
            try {
                dto.setDefaultStance(FourxFaction.DiplomaticStance.valueOf(defaultStance));
            } catch (IllegalArgumentException e) {
                dto.setDefaultStance(FourxFaction.DiplomaticStance.NEUTRAL);
            }
        }
        dto.setFleetStrength(fleetStrength);
        dto.setColonyCount(colonyCount);
        dto.setResearchPoints(researchPoints);
        dto.setEconomyOutput(economyOutput);
        return dto;
    }
}
