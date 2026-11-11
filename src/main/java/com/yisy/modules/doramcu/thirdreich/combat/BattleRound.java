package com.yisy.modules.doramcu.thirdreich.combat;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single round of 3R4 ground combat.
 * <p>
 * Each round contains the actions taken, damage dealt, and morale checks
 * for both sides.
 * </p>
 */
public class BattleRound {

    public enum Side {
        ATTACKER, DEFENDER
    }

    private final int roundNumber;
    private final List<String> events = new ArrayList<>();
    private int attackerDamageDealt;
    private int defenderDamageDealt;
    private int attackerMoraleLoss;
    private int defenderMoraleLoss;
    private int attackerUnitsDestroyed;
    private int defenderUnitsDestroyed;

    /**
     * Creates a new battle round.
     *
     * @param roundNumber the round number (1-based)
     */
    public BattleRound(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    // ---- Accessors ----

    /** Returns the round number. */
    public int getRoundNumber() {
        return roundNumber;
    }

    /** Returns the list of events in this round. */
    public List<String> getEvents() {
        return events;
    }

    /** Returns the damage dealt by the attacker. */
    public int getAttackerDamageDealt() {
        return attackerDamageDealt;
    }

    /** Sets the damage dealt by the attacker. */
    public void setAttackerDamageDealt(int attackerDamageDealt) {
        this.attackerDamageDealt = attackerDamageDealt;
    }

    /** Returns the damage dealt by the defender. */
    public int getDefenderDamageDealt() {
        return defenderDamageDealt;
    }

    /** Sets the damage dealt by the defender. */
    public void setDefenderDamageDealt(int defenderDamageDealt) {
        this.defenderDamageDealt = defenderDamageDealt;
    }

    /** Returns the morale loss for the attacker. */
    public int getAttackerMoraleLoss() {
        return attackerMoraleLoss;
    }

    /** Sets the morale loss for the attacker. */
    public void setAttackerMoraleLoss(int attackerMoraleLoss) {
        this.attackerMoraleLoss = attackerMoraleLoss;
    }

    /** Returns the morale loss for the defender. */
    public int getDefenderMoraleLoss() {
        return defenderMoraleLoss;
    }

    /** Sets the morale loss for the defender. */
    public void setDefenderMoraleLoss(int defenderMoraleLoss) {
        this.defenderMoraleLoss = defenderMoraleLoss;
    }

    /** Returns the number of attacker units destroyed. */
    public int getAttackerUnitsDestroyed() {
        return attackerUnitsDestroyed;
    }

    /** Sets the number of attacker units destroyed. */
    public void setAttackerUnitsDestroyed(int attackerUnitsDestroyed) {
        this.attackerUnitsDestroyed = attackerUnitsDestroyed;
    }

    /** Returns the number of defender units destroyed. */
    public int getDefenderUnitsDestroyed() {
        return defenderUnitsDestroyed;
    }

    /** Sets the number of defender units destroyed. */
    public void setDefenderUnitsDestroyed(int defenderUnitsDestroyed) {
        this.defenderUnitsDestroyed = defenderUnitsDestroyed;
    }

    // ---- Event Recording ----

    /**
     * Records an event in this round.
     */
    public void addEvent(String event) {
        events.add(event);
    }

    @Override
    public String toString() {
        return String.format("Round %d: atkDmg=%d, defDmg=%d, atkMorale=%d, defMorale=%d, " +
                        "atkLost=%d, defLost=%d",
                roundNumber, attackerDamageDealt, defenderDamageDealt,
                attackerMoraleLoss, defenderMoraleLoss,
                attackerUnitsDestroyed, defenderUnitsDestroyed);
    }
}
