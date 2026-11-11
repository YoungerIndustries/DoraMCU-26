package com.yisy.modules.doramcu.star_trek_4x.model;

import com.yisy.modules.doramcu.sfb.model.SfbEmpire;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extends SfbEmpire with diplomacy, reputation, treaties, and territorial control.
 * <p>
 * A faction in the Star Trek 4x game represents an empire (Federation, Klingon, Romulan, etc.)
 * with diplomatic stances, reputation scores, active treaties, and controlled sectors.
 * </p>
 */
@Entity
@Table(name = "st4x_factions")
public class StarTrekFaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    /** The SFB empire this faction is based on. */
    @Column(length = 100)
    private String sfbEmpireName;

    /** Galaxy: Alpha Octant, Andromeda, M81, etc. */
    @Column(length = 50)
    private String galaxy;

    /** Combat doctrine: OFFENSIVE, DEFENSIVE, BALANCED, GUERRILLA. */
    @Column(length = 50)
    private String combatStyle;

    /** Default diplomatic stance: NEUTRAL, HOSTILE, FRIENDLY, ALLIED. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiplomaticStance defaultStance = DiplomaticStance.NEUTRAL;

    /** Reputation scores with other factions (faction name -> score -100 to 100). */
    @ElementCollection
    @MapKeyColumn(name = "faction_name")
    @Column(name = "reputation_score")
    private Map<String, Integer> reputationMap = new HashMap<>();

    /** List of controlled sector IDs. */
    @ElementCollection
    @CollectionTable(name = "st4x_faction_sectors", joinColumns = @JoinColumn(name = "faction_id"))
    @Column(name = "sector_id")
    private List<Integer> controlledSectors = new ArrayList<>();

    /** Total fleet strength. */
    @Column(nullable = false)
    private int fleetStrength = 0;

    /** Total number of colonies. */
    @Column(nullable = false)
    private int colonyCount = 0;

    /** Research points available. */
    @Column(nullable = false)
    private int researchPoints = 0;

    /** Economic output per turn. */
    @Column(nullable = false)
    private int economyOutput = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;

    public StarTrekFaction() {
    }

    public StarTrekFaction(String name, String sfbEmpireName, GameSession gameSession) {
        this.name = name;
        this.sfbEmpireName = sfbEmpireName;
        this.gameSession = gameSession;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSfbEmpireName() { return sfbEmpireName; }
    public void setSfbEmpireName(String sfbEmpireName) { this.sfbEmpireName = sfbEmpireName; }

    public String getGalaxy() { return galaxy; }
    public void setGalaxy(String galaxy) { this.galaxy = galaxy; }

    public String getCombatStyle() { return combatStyle; }
    public void setCombatStyle(String combatStyle) { this.combatStyle = combatStyle; }

    public DiplomaticStance getDefaultStance() { return defaultStance; }
    public void setDefaultStance(DiplomaticStance defaultStance) { this.defaultStance = defaultStance; }

    public Map<String, Integer> getReputationMap() { return reputationMap; }
    public void setReputationMap(Map<String, Integer> reputationMap) { this.reputationMap = reputationMap; }

    public List<Integer> getControlledSectors() { return controlledSectors; }
    public void setControlledSectors(List<Integer> controlledSectors) { this.controlledSectors = controlledSectors; }

    public int getFleetStrength() { return fleetStrength; }
    public void setFleetStrength(int fleetStrength) { this.fleetStrength = fleetStrength; }

    public int getColonyCount() { return colonyCount; }
    public void setColonyCount(int colonyCount) { this.colonyCount = colonyCount; }

    public int getResearchPoints() { return researchPoints; }
    public void setResearchPoints(int researchPoints) { this.researchPoints = researchPoints; }

    public int getEconomyOutput() { return economyOutput; }
    public void setEconomyOutput(int economyOutput) { this.economyOutput = economyOutput; }

    public GameSession getGameSession() { return gameSession; }
    public void setGameSession(GameSession gameSession) { this.gameSession = gameSession; }

    /**
     * Get reputation with another faction.
     */
    public int getReputation(String otherFaction) {
        return reputationMap.getOrDefault(otherFaction, 0);
    }

    /**
     * Modify reputation with another faction.
     */
    public void modifyReputation(String otherFaction, int delta) {
        int current = reputationMap.getOrDefault(otherFaction, 0);
        reputationMap.put(otherFaction, Math.max(-100, Math.min(100, current + delta)));
    }

    /**
     * Check if this faction is at war with another.
     */
    public boolean isAtWarWith(String otherFaction) {
        return getReputation(otherFaction) < -50;
    }

    /**
     * Check if this faction is allied with another.
     */
    public boolean isAlliedWith(String otherFaction) {
        return getReputation(otherFaction) > 75;
    }

    public enum DiplomaticStance {
        HOSTILE,
        NEUTRAL,
        FRIENDLY,
        ALLIED
    }
}
