package com.yisy.modules.doramcu.star_trek_4x.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Unified game session state for the Star Trek 4x game.
 * <p>
 * Owns the galaxy reference, active factions, current turn, and gameplay mode
 * (Captain's Log narrative mode or 4x strategic mode). Both modes share the
 * same Galaxy instance so discoveries and actions persist across modes.
 * </p>
 */
@Entity
@Table(name = "st4x_game_sessions")
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** Reference to the hex.Galaxy instance (stored as sector count for now). */
    @Column(nullable = false)
    private int galaxyId;

    /** Current game turn number. */
    @Column(nullable = false)
    private int currentTurn = 0;

    /** Current gameplay mode: CAPTAINS_LOG or FOUR_X. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameMode mode = GameMode.FOUR_X;

    /** Seed used for galaxy generation. */
    @Column(nullable = false)
    private long galaxySeed;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StarTrekFaction> factions = new ArrayList<>();

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Colony> colonies = new ArrayList<>();

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TradeRoute> tradeRoutes = new ArrayList<>();

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DiplomaticRelation> diplomaticRelations = new ArrayList<>();

    public GameSession() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getGalaxyId() { return galaxyId; }
    public void setGalaxyId(int galaxyId) { this.galaxyId = galaxyId; }

    public int getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(int currentTurn) { this.currentTurn = currentTurn; }

    public GameMode getMode() { return mode; }
    public void setMode(GameMode mode) { this.mode = mode; }

    public long getGalaxySeed() { return galaxySeed; }
    public void setGalaxySeed(long galaxySeed) { this.galaxySeed = galaxySeed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<StarTrekFaction> getFactions() { return factions; }
    public void setFactions(List<StarTrekFaction> factions) { this.factions = factions; }

    public List<Colony> getColonies() { return colonies; }
    public void setColonies(List<Colony> colonies) { this.colonies = colonies; }

    public List<TradeRoute> getTradeRoutes() { return tradeRoutes; }
    public void setTradeRoutes(List<TradeRoute> tradeRoutes) { this.tradeRoutes = tradeRoutes; }

    public List<DiplomaticRelation> getDiplomaticRelations() { return diplomaticRelations; }
    public void setDiplomaticRelations(List<DiplomaticRelation> diplomaticRelations) { this.diplomaticRelations = diplomaticRelations; }

    /**
     * Advance to the next turn.
     */
    public void advanceTurn() {
        this.currentTurn++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Switch between gameplay modes.
     */
    public void switchMode(GameMode newMode) {
        this.mode = newMode;
        this.updatedAt = LocalDateTime.now();
    }

    public enum GameMode {
        CAPTAINS_LOG,
        FOUR_X
    }
}
