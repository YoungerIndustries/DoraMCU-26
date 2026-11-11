package com.yisy.modules.doramcu.dto;

/**
 * Tracks the progressive unlock state of 4X layers in an RPG-first game.
 * <p>
 * Each 4X feature (Exploration, Diplomacy, Economy, Combat) starts locked
 * and unlocks when the player reaches the corresponding milestone:
 * <ul>
 *   <li>Exploration: 5 systems discovered</li>
 *   <li>Diplomacy: 1 NPC contact</li>
 *   <li>Economy: 3 colonies established</li>
 *   <li>Combat: 1 battle fought</li>
 * </ul>
 * </p>
 */
public class FourxUnlockState {

    /** Milestone threshold: systems discovered to unlock Exploration */
    public static final int EXPLORATION_THRESHOLD = 5;

    /** Milestone threshold: NPC contacts to unlock Diplomacy */
    public static final int DIPLOMACY_THRESHOLD = 1;

    /** Milestone threshold: colonies established to unlock Economy */
    public static final int ECONOMY_THRESHOLD = 3;

    /** Milestone threshold: battles fought to unlock Combat */
    public static final int COMBAT_THRESHOLD = 1;

    private boolean explorationUnlocked;
    private boolean diplomacyUnlocked;
    private boolean economyUnlocked;
    private boolean combatUnlocked;

    private int systemsDiscovered;
    private int npcContacts;
    private int coloniesEstablished;
    private int battlesFought;

    public FourxUnlockState() {
        this.explorationUnlocked = false;
        this.diplomacyUnlocked = false;
        this.economyUnlocked = false;
        this.combatUnlocked = false;
        this.systemsDiscovered = 0;
        this.npcContacts = 0;
        this.coloniesEstablished = 0;
        this.battlesFought = 0;
    }

    public boolean isExplorationUnlocked() {
        return explorationUnlocked;
    }

    public void setExplorationUnlocked(boolean explorationUnlocked) {
        this.explorationUnlocked = explorationUnlocked;
    }

    public boolean isDiplomacyUnlocked() {
        return diplomacyUnlocked;
    }

    public void setDiplomacyUnlocked(boolean diplomacyUnlocked) {
        this.diplomacyUnlocked = diplomacyUnlocked;
    }

    public boolean isEconomyUnlocked() {
        return economyUnlocked;
    }

    public void setEconomyUnlocked(boolean economyUnlocked) {
        this.economyUnlocked = economyUnlocked;
    }

    public boolean isCombatUnlocked() {
        return combatUnlocked;
    }

    public void setCombatUnlocked(boolean combatUnlocked) {
        this.combatUnlocked = combatUnlocked;
    }

    public int getSystemsDiscovered() {
        return systemsDiscovered;
    }

    public void setSystemsDiscovered(int systemsDiscovered) {
        this.systemsDiscovered = systemsDiscovered;
    }

    public int getNpcContacts() {
        return npcContacts;
    }

    public void setNpcContacts(int npcContacts) {
        this.npcContacts = npcContacts;
    }

    public int getColoniesEstablished() {
        return coloniesEstablished;
    }

    public void setColoniesEstablished(int coloniesEstablished) {
        this.coloniesEstablished = coloniesEstablished;
    }

    public int getBattlesFought() {
        return battlesFought;
    }

    public void setBattlesFought(int battlesFought) {
        this.battlesFought = battlesFought;
    }

    /**
     * Checks if any 4X layer is unlocked.
     *
     * @return true if at least one 4X layer is unlocked
     */
    public boolean isAnyUnlocked() {
        return explorationUnlocked || diplomacyUnlocked || economyUnlocked || combatUnlocked;
    }

    /**
     * Checks if all 4X layers are unlocked.
     *
     * @return true if all 4X layers are unlocked
     */
    public boolean isAllUnlocked() {
        return explorationUnlocked && diplomacyUnlocked && economyUnlocked && combatUnlocked;
    }

    @Override
    public String toString() {
        return "FourxUnlockState{" +
                "exploration=" + explorationUnlocked +
                ", diplomacy=" + diplomacyUnlocked +
                ", economy=" + economyUnlocked +
                ", combat=" + combatUnlocked +
                ", systems=" + systemsDiscovered +
                ", contacts=" + npcContacts +
                ", colonies=" + coloniesEstablished +
                ", battles=" + battlesFought +
                '}';
    }
}
