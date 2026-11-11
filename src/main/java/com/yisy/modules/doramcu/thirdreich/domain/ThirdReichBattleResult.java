package com.yisy.modules.doramcu.thirdreich.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of a 3R4 ground battle.
 * <p>
 * Contains the outcome, casualties, unit losses, and a narrative summary
 * of the battle events.
 * </p>
 */
public class ThirdReichBattleResult {

    public enum Outcome {
        /** Attacker wins decisively */
        ATTACKER_VICTORY,
        /** Defender wins decisively */
        DEFENDER_VICTORY,
        /** Attacker wins but at great cost */
        PYRRHIC_VICTORY,
        /** Defender holds but takes heavy losses */
        DEFENDER_HOLDS,
        /** Both sides withdraw */
        DRAW,
        /** Battle ended in retreat */
        RETREAT
    }

    private final String battleId;
    private final Outcome outcome;
    private final List<String> events = new ArrayList<>();
    private int attackerCasualties;
    private int defenderCasualties;
    private int attackerUnitsLost;
    private int defenderUnitsLost;
    private int roundsFought;
    private String narrativeSummary;

    /**
     * Creates a new battle result.
     *
     * @param battleId the ID of the battle
     * @param outcome the outcome of the battle
     */
    public ThirdReichBattleResult(String battleId, Outcome outcome) {
        if (battleId == null || battleId.isBlank()) {
            throw new IllegalArgumentException("Battle ID must not be null or blank");
        }
        if (outcome == null) {
            throw new IllegalArgumentException("Outcome must not be null");
        }
        this.battleId = battleId;
        this.outcome = outcome;
    }

    // ---- Accessors ----

    /** Returns the battle ID. */
    public String getBattleId() {
        return battleId;
    }

    /** Returns the outcome. */
    public Outcome getOutcome() {
        return outcome;
    }

    /** Returns the list of battle events. */
    public List<String> getEvents() {
        return events;
    }

    /** Returns the number of attacker casualties. */
    public int getAttackerCasualties() {
        return attackerCasualties;
    }

    /** Sets the attacker casualties. */
    public void setAttackerCasualties(int attackerCasualties) {
        this.attackerCasualties = attackerCasualties;
    }

    /** Returns the number of defender casualties. */
    public int getDefenderCasualties() {
        return defenderCasualties;
    }

    /** Sets the defender casualties. */
    public void setDefenderCasualties(int defenderCasualties) {
        this.defenderCasualties = defenderCasualties;
    }

    /** Returns the number of attacker units destroyed. */
    public int getAttackerUnitsLost() {
        return attackerUnitsLost;
    }

    /** Sets the attacker units lost. */
    public void setAttackerUnitsLost(int attackerUnitsLost) {
        this.attackerUnitsLost = attackerUnitsLost;
    }

    /** Returns the number of defender units destroyed. */
    public int getDefenderUnitsLost() {
        return defenderUnitsLost;
    }

    /** Sets the defender units lost. */
    public void setDefenderUnitsLost(int defenderUnitsLost) {
        this.defenderUnitsLost = defenderUnitsLost;
    }

    /** Returns the number of rounds fought. */
    public int getRoundsFought() {
        return roundsFought;
    }

    /** Sets the number of rounds fought. */
    public void setRoundsFought(int roundsFought) {
        this.roundsFought = roundsFought;
    }

    /** Returns the narrative summary. */
    public String getNarrativeSummary() {
        return narrativeSummary;
    }

    /** Sets the narrative summary. */
    public void setNarrativeSummary(String narrativeSummary) {
        this.narrativeSummary = narrativeSummary;
    }

    // ---- Event Recording ----

    /**
     * Adds a battle event.
     */
    public void addEvent(String event) {
        events.add(event);
    }

    /**
     * Generates a default narrative summary based on the outcome and casualties.
     */
    public void generateNarrative() {
        StringBuilder sb = new StringBuilder();
        sb.append("After ").append(roundsFought).append(" rounds of combat, ");

        switch (outcome) {
            case ATTACKER_VICTORY:
                sb.append("the attacker achieved a decisive victory.");
                break;
            case DEFENDER_VICTORY:
                sb.append("the defender repelled the attack and won decisively.");
                break;
            case PYRRHIC_VICTORY:
                sb.append("the attacker won, but at a terrible cost.");
                break;
            case DEFENDER_HOLDS:
                sb.append("the defender held the line, though battered.");
                break;
            case DRAW:
                sb.append("both sides withdrew after a hard-fought battle.");
                break;
            case RETREAT:
                sb.append("the battle ended in retreat.");
                break;
        }

        sb.append(" The attacker suffered ").append(attackerCasualties)
                .append(" casualties and lost ").append(attackerUnitsLost).append(" units.");
        sb.append(" The defender suffered ").append(defenderCasualties)
                .append(" casualties and lost ").append(defenderUnitsLost).append(" units.");

        this.narrativeSummary = sb.toString();
    }

    @Override
    public String toString() {
        return String.format("3R4BattleResult{id='%s', outcome=%s, rounds=%d, " +
                        "atkCasualties=%d, defCasualties=%d, atkLost=%d, defLost=%d}",
                battleId, outcome, roundsFought, attackerCasualties, defenderCasualties,
                attackerUnitsLost, defenderUnitsLost);
    }
}
