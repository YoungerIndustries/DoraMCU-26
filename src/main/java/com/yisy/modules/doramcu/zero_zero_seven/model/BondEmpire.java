package com.yisy.modules.doramcu.zero_zero_seven.model;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Country;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a Cold War-era empire in the 007 game.
 * <p>
 * Wraps Earth2026Country with Cold War-specific attributes: alignment, intelligence budget,
 * military strength, and diplomatic relations. Each empire competes for global dominance
 * through espionage, covert operations, and traditional 4X mechanics.
 * </p>
 */
@Entity
@Table(name = "bond_empires")
public class BondEmpire {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** ISO 3166-1 alpha-2 country code. */
    @Column(name = "iso_code", nullable = false, length = 2)
    private String isoCode;

    /** Description of the empire. */
    @Column(length = 500)
    private String description;

    /** Cold War alignment: WESTERN, SOVIET, NON_ALIGNED, AXIS. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ColdWarAlignment alignment;

    /** Intelligence budget per turn. */
    @Column(name = "intelligence_budget", nullable = false)
    private int intelligenceBudget = 0;

    /** Military strength rating (1-10). */
    @Column(name = "military_strength", nullable = false)
    private int militaryStrength = 5;

    /** Economic power rating (1-10). */
    @Column(name = "economic_power", nullable = false)
    private int economicPower = 5;

    /** Diplomatic influence rating (1-10). */
    @Column(name = "diplomatic_influence", nullable = false)
    private int diplomaticInfluence = 5;

    /** Number of active spy networks. */
    @Column(name = "spy_network_count", nullable = false)
    private int spyNetworkCount = 0;

    /** Total number of agents. */
    @Column(name = "agent_count", nullable = false)
    private int agentCount = 0;

    /** Research points available. */
    @Column(name = "research_points", nullable = false)
    private int researchPoints = 0;

    /** Economic output per turn. */
    @Column(name = "economy_output", nullable = false)
    private int economyOutput = 0;

    /** Reputation scores with other empires (empire name -> score -100 to 100). */
    @ElementCollection
    @MapKeyColumn(name = "empire_name")
    @Column(name = "reputation_score")
    private Map<String, Integer> reputationMap = new HashMap<>();

    /** List of controlled territory IDs. */
    @ElementCollection
    @CollectionTable(name = "bond_empire_territories", joinColumns = @JoinColumn(name = "empire_id"))
    @Column(name = "territory_id")
    private List<Long> controlledTerritories = new ArrayList<>();

    /** Reference to Earth2026Country for base data. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "earth2026_country_id")
    private Earth2026Country earth2026Country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private BondGameSession gameSession;

    public BondEmpire() {
    }

    public BondEmpire(String name, String isoCode, ColdWarAlignment alignment, BondGameSession gameSession) {
        this.name = name;
        this.isoCode = isoCode;
        this.alignment = alignment;
        this.gameSession = gameSession;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIsoCode() { return isoCode; }
    public void setIsoCode(String isoCode) { this.isoCode = isoCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ColdWarAlignment getAlignment() { return alignment; }
    public void setAlignment(ColdWarAlignment alignment) { this.alignment = alignment; }

    public int getIntelligenceBudget() { return intelligenceBudget; }
    public void setIntelligenceBudget(int intelligenceBudget) { this.intelligenceBudget = intelligenceBudget; }

    public int getMilitaryStrength() { return militaryStrength; }
    public void setMilitaryStrength(int militaryStrength) { this.militaryStrength = militaryStrength; }

    public int getEconomicPower() { return economicPower; }
    public void setEconomicPower(int economicPower) { this.economicPower = economicPower; }

    public int getDiplomaticInfluence() { return diplomaticInfluence; }
    public void setDiplomaticInfluence(int diplomaticInfluence) { this.diplomaticInfluence = diplomaticInfluence; }

    public int getSpyNetworkCount() { return spyNetworkCount; }
    public void setSpyNetworkCount(int spyNetworkCount) { this.spyNetworkCount = spyNetworkCount; }

    public int getAgentCount() { return agentCount; }
    public void setAgentCount(int agentCount) { this.agentCount = agentCount; }

    public int getResearchPoints() { return researchPoints; }
    public void setResearchPoints(int researchPoints) { this.researchPoints = researchPoints; }

    public int getEconomyOutput() { return economyOutput; }
    public void setEconomyOutput(int economyOutput) { this.economyOutput = economyOutput; }

    public Map<String, Integer> getReputationMap() { return reputationMap; }
    public void setReputationMap(Map<String, Integer> reputationMap) { this.reputationMap = reputationMap; }

    public List<Long> getControlledTerritories() { return controlledTerritories; }
    public void setControlledTerritories(List<Long> controlledTerritories) { this.controlledTerritories = controlledTerritories; }

    public Earth2026Country getEarth2026Country() { return earth2026Country; }
    public void setEarth2026Country(Earth2026Country earth2026Country) { this.earth2026Country = earth2026Country; }

    public BondGameSession getGameSession() { return gameSession; }
    public void setGameSession(BondGameSession gameSession) { this.gameSession = gameSession; }

    /**
     * Get reputation with another empire.
     */
    public int getReputation(String otherEmpire) {
        return reputationMap.getOrDefault(otherEmpire, 0);
    }

    /**
     * Modify reputation with another empire.
     */
    public void modifyReputation(String otherEmpire, int delta) {
        int current = reputationMap.getOrDefault(otherEmpire, 0);
        reputationMap.put(otherEmpire, Math.max(-100, Math.min(100, current + delta)));
    }

    /**
     * Check if this empire is at war with another.
     */
    public boolean isAtWarWith(String otherEmpire) {
        return getReputation(otherEmpire) < -50;
    }

    /**
     * Check if this empire is allied with another.
     */
    public boolean isAlliedWith(String otherEmpire) {
        return getReputation(otherEmpire) > 75;
    }

    /**
     * Calculate the overall power score of this empire.
     */
    public double getPowerScore() {
        return (militaryStrength * 0.4 + economicPower * 0.35 + diplomaticInfluence * 0.25);
    }

    public enum ColdWarAlignment {
        WESTERN,
        SOVIET,
        NON_ALIGNED,
        AXIS
    }
}
