package com.yisy.modules.doramcu.fourx;

import java.util.Map;

/**
 * Tracks which 4X layers are unlocked for a game session.
 */
public class FourxUnlockState {

    private boolean explorationUnlocked;
    private boolean expansionUnlocked;
    private boolean exploitationUnlocked;
    private boolean exterminationUnlocked;
    private boolean diplomacyUnlocked;

    public FourxUnlockState() {
    }

    public boolean isExplorationUnlocked() { return explorationUnlocked; }
    public void setExplorationUnlocked(boolean explorationUnlocked) { this.explorationUnlocked = explorationUnlocked; }

    public boolean isExpansionUnlocked() { return expansionUnlocked; }
    public void setExpansionUnlocked(boolean expansionUnlocked) { this.expansionUnlocked = expansionUnlocked; }

    public boolean isExploitationUnlocked() { return exploitationUnlocked; }
    public void setExploitationUnlocked(boolean exploitationUnlocked) { this.exploitationUnlocked = exploitationUnlocked; }

    public boolean isExterminationUnlocked() { return exterminationUnlocked; }
    public void setExterminationUnlocked(boolean exterminationUnlocked) { this.exterminationUnlocked = exterminationUnlocked; }

    public boolean isDiplomacyUnlocked() { return diplomacyUnlocked; }
    public void setDiplomacyUnlocked(boolean diplomacyUnlocked) { this.diplomacyUnlocked = diplomacyUnlocked; }

    public Map<String, Boolean> toMap() {
        return Map.of(
                "exploration", explorationUnlocked,
                "expansion", expansionUnlocked,
                "exploitation", exploitationUnlocked,
                "extermination", exterminationUnlocked,
                "diplomacy", diplomacyUnlocked
        );
    }

    public boolean isAllUnlocked() {
        return explorationUnlocked && expansionUnlocked && exploitationUnlocked && exterminationUnlocked && diplomacyUnlocked;
    }

    public boolean isAnyUnlocked() {
        return explorationUnlocked || expansionUnlocked || exploitationUnlocked || exterminationUnlocked || diplomacyUnlocked;
    }

    public boolean isCombatUnlocked() {
        return exterminationUnlocked;
    }

    public boolean isEconomyUnlocked() {
        return exploitationUnlocked;
    }
}
