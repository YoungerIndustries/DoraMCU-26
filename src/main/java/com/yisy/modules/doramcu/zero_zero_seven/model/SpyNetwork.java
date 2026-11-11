package com.yisy.modules.doramcu.zero_zero_seven.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a spy network established in a foreign territory.
 * <p>
 * Spy networks are the backbone of intelligence operations, allowing empires to gather
 * intelligence, recruit assets, and launch covert operations in foreign territories.
 * Networks can be upgraded (level 1-5) but carry a detection risk.
 * </p>
 */
@Entity
@Table(name = "bond_spy_networks")
public class SpyNetwork {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** Description of the network. */
    @Column(length = 500)
    private String description;

    /** Location territory ID. */
    @Column(name = "location_id", nullable = false)
    private Long locationId;

    /** Owner empire ID. */
    @Column(name = "owner_empire_id", nullable = false)
    private Long ownerEmpireId;

    /** Network level (1-5, upgradeable). */
    @Column(nullable = false)
    private int level = 1;

    /** Intelligence gathered per turn. */
    @Column(name = "intelligence_gathered", nullable = false)
    private int intelligenceGathered = 0;

    /** Detection risk (0-100). */
    @Column(name = "detection_risk", nullable = false)
    private int detectionRisk = 0;

    /** Whether the network is active. */
    @Column(nullable = false)
    private boolean active = true;

    /** List of agent IDs assigned to this network. */
    @ElementCollection
    @CollectionTable(name = "bond_spy_network_agents", joinColumns = @JoinColumn(name = "network_id"))
    @Column(name = "agent_id")
    private List<Long> agents = new ArrayList<>();

    /** Number of safehouses. */
    @Column(name = "safehouse_count", nullable = false)
    private int safehouseCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private BondGameSession gameSession;

    public SpyNetwork() {
    }

    public SpyNetwork(String name, Long locationId, Long ownerEmpireId, BondGameSession gameSession) {
        this.name = name;
        this.locationId = locationId;
        this.ownerEmpireId = ownerEmpireId;
        this.gameSession = gameSession;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }

    public Long getOwnerEmpireId() { return ownerEmpireId; }
    public void setOwnerEmpireId(Long ownerEmpireId) { this.ownerEmpireId = ownerEmpireId; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = Math.max(1, Math.min(5, level)); }

    public int getIntelligenceGathered() { return intelligenceGathered; }
    public void setIntelligenceGathered(int intelligenceGathered) { this.intelligenceGathered = intelligenceGathered; }

    public int getDetectionRisk() { return detectionRisk; }
    public void setDetectionRisk(int detectionRisk) { this.detectionRisk = Math.max(0, Math.min(100, detectionRisk)); }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<Long> getAgents() { return agents; }
    public void setAgents(List<Long> agents) { this.agents = agents; }

    public int getSafehouseCount() { return safehouseCount; }
    public void setSafehouseCount(int safehouseCount) { this.safehouseCount = safehouseCount; }

    public BondGameSession getGameSession() { return gameSession; }
    public void setGameSession(BondGameSession gameSession) { this.gameSession = gameSession; }

    /**
     * Assign an agent to this network.
     */
    public void assignAgent(Long agentId) {
        agents.add(agentId);
    }

    /**
     * Remove an agent from this network.
     */
    public void removeAgent(Long agentId) {
        agents.remove(agentId);
    }

    /**
     * Upgrade the network level.
     */
    public void upgrade() {
        if (level < 5) {
            this.level++;
            this.intelligenceGathered += 5;
            this.detectionRisk += 10;
        }
    }
}
