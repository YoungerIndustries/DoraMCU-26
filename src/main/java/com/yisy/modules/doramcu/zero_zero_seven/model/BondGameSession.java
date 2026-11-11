package com.yisy.modules.doramcu.zero_zero_seven.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Unified game session state for the 007 Cold War 4X RPG.
 * <p>
 * Owns the hex map reference, active empires, current turn, and all game-related
 * entities. Both espionage and traditional 4X mechanics share the same session
 * so discoveries and actions persist across all gameplay layers.
 * </p>
 */
@Entity
@Table(name = "bond_game_sessions")
public class BondGameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** Reference to the hex map instance (stored as map ID). */
    @Column(nullable = false)
    private int mapId;

    /** Current game turn number. */
    @Column(nullable = false)
    private int currentTurn = 0;

    /** Year in the game (starts at 1950). */
    @Column(nullable = false)
    private int currentYear = 1950;

    /** Seed used for map generation. */
    @Column(nullable = false)
    private long mapSeed;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BondEmpire> empires = new ArrayList<>();

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BondAgent> agents = new ArrayList<>();

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SpyNetwork> spyNetworks = new ArrayList<>();

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CovertOperation> operations = new ArrayList<>();

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BondTerritory> territories = new ArrayList<>();

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BondDiplomaticRelation> diplomaticRelations = new ArrayList<>();

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BondTechResearch> techResearches = new ArrayList<>();

    public BondGameSession() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getMapId() { return mapId; }
    public void setMapId(int mapId) { this.mapId = mapId; }

    public int getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(int currentTurn) { this.currentTurn = currentTurn; }

    public int getCurrentYear() { return currentYear; }
    public void setCurrentYear(int currentYear) { this.currentYear = currentYear; }

    public long getMapSeed() { return mapSeed; }
    public void setMapSeed(long mapSeed) { this.mapSeed = mapSeed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<BondEmpire> getEmpires() { return empires; }
    public void setEmpires(List<BondEmpire> empires) { this.empires = empires; }

    public List<BondAgent> getAgents() { return agents; }
    public void setAgents(List<BondAgent> agents) { this.agents = agents; }

    public List<SpyNetwork> getSpyNetworks() { return spyNetworks; }
    public void setSpyNetworks(List<SpyNetwork> spyNetworks) { this.spyNetworks = spyNetworks; }

    public List<CovertOperation> getOperations() { return operations; }
    public void setOperations(List<CovertOperation> operations) { this.operations = operations; }

    public List<BondTerritory> getTerritories() { return territories; }
    public void setTerritories(List<BondTerritory> territories) { this.territories = territories; }

    public List<BondDiplomaticRelation> getDiplomaticRelations() { return diplomaticRelations; }
    public void setDiplomaticRelations(List<BondDiplomaticRelation> diplomaticRelations) { this.diplomaticRelations = diplomaticRelations; }

    public List<BondTechResearch> getTechResearches() { return techResearches; }
    public void setTechResearches(List<BondTechResearch> techResearches) { this.techResearches = techResearches; }

    /**
     * Advance to the next turn.
     */
    public void advanceTurn() {
        this.currentTurn++;
        this.currentYear++;
        this.updatedAt = LocalDateTime.now();
    }
}
