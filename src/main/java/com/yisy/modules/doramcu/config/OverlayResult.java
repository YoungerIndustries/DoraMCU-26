package com.yisy.modules.doramcu.config;

import com.yisy.modules.doramcu.gamecore.GameMode;
import com.yisy.modules.doramcu.gamecore.GameType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Result of the overlay pipeline for a single galaxy.
 * <p>
 * Tracks which phases completed, how many empires and NPCs were created,
 * and which game modes were initialized.
 * </p>
 */
public class OverlayResult {

    private String galaxyName;
    private GameType gameType;
    private Set<GameMode> modesInitialized;
    private int empiresCreated;
    private int npcsCreated;
    private int systemsAdded;
    private List<String> phasesCompleted;

    public OverlayResult() {
        this.modesInitialized = new HashSet<>();
        this.phasesCompleted = new ArrayList<>();
    }

    public String getGalaxyName() {
        return galaxyName;
    }

    public void setGalaxyName(String galaxyName) {
        this.galaxyName = galaxyName;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public Set<GameMode> getModesInitialized() {
        return modesInitialized;
    }

    public void setModesInitialized(Set<GameMode> modesInitialized) {
        this.modesInitialized = modesInitialized;
    }

    public int getEmpiresCreated() {
        return empiresCreated;
    }

    public void setEmpiresCreated(int empiresCreated) {
        this.empiresCreated = empiresCreated;
    }

    public int getNpcsCreated() {
        return npcsCreated;
    }

    public void setNpcsCreated(int npcsCreated) {
        this.npcsCreated = npcsCreated;
    }

    public int getSystemsAdded() {
        return systemsAdded;
    }

    public void setSystemsAdded(int systemsAdded) {
        this.systemsAdded = systemsAdded;
    }

    public List<String> getPhasesCompleted() {
        return phasesCompleted;
    }

    public void setPhasesCompleted(List<String> phasesCompleted) {
        this.phasesCompleted = phasesCompleted;
    }

    @Override
    public String toString() {
        return "OverlayResult{" +
                "galaxyName='" + galaxyName + '\'' +
                ", gameType=" + gameType +
                ", modesInitialized=" + modesInitialized +
                ", empiresCreated=" + empiresCreated +
                ", npcsCreated=" + npcsCreated +
                ", systemsAdded=" + systemsAdded +
                ", phasesCompleted=" + phasesCompleted +
                '}';
    }
}
