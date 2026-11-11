package com.yisy.modules.doramcu.traveller;

import java.util.List;

/**
 * Result of an air battle.
 */
public class AirBattleResult {
    public enum Outcome { ATTACKER_VICTORY, DEFENDER_VICTORY, PYRRHIC_VICTORY, DRAW, RETREAT }

    private String battleId;
    private String description;
    private Outcome outcome;
    private int attackerLosses;
    private int defenderLosses;
    private List<AirBattleEvent> events;
    private int odds;
    private int diceRoll;

    public AirBattleResult() {}

    public AirBattleResult(String battleId, String description, Outcome outcome) {
        this.battleId = battleId;
        this.description = description;
        this.outcome = outcome;
    }

    public AirBattleResult(String battleId, String description, Outcome outcome,
                           int attackerLosses, int defenderLosses, List<AirBattleEvent> events) {
        this.battleId = battleId;
        this.description = description;
        this.outcome = outcome;
        this.attackerLosses = attackerLosses;
        this.defenderLosses = defenderLosses;
        this.events = events;
    }

    public String getBattleId() { return battleId; }
    public String getDescription() { return description; }
    public Outcome getOutcome() { return outcome; }
    public int getAttackerLosses() { return attackerLosses; }
    public int getDefenderLosses() { return defenderLosses; }
    public List<AirBattleEvent> getEvents() { return events; }
    public int getOdds() { return odds; }
    public void setOdds(int odds) { this.odds = odds; }
    public int getDiceRoll() { return diceRoll; }
    public void setDiceRoll(int diceRoll) { this.diceRoll = diceRoll; }
}
