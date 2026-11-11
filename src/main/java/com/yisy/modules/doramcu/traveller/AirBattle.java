package com.yisy.modules.doramcu.traveller;

import com.yisy.modules.doramcu.util.DiceUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Air battle engine supporting Abstract (CRT) and Detailed (multi-round) modes.
 */
public class AirBattle {
    public enum BattleMode { ABSTRACT, DETAILED }
    public enum BattleState { INITIATED, IN_PROGRESS, COMPLETED }
    public enum Altitude { LOW, MEDIUM, HIGH }
    public enum Side { ATTACKER, DEFENDER }

    private String id;
    private List<TravellerSpacecraft> attackerUnits = new ArrayList<>();
    private List<TravellerSpacecraft> defenderUnits = new ArrayList<>();
    private Altitude altitude = Altitude.MEDIUM;
    private BattleMode mode = BattleMode.ABSTRACT;
    private BattleState state = BattleState.INITIATED;
    private AirBattleResult result;
    private List<AirBattleEvent> events = new ArrayList<>();
    private int currentRound = 0;

    public AirBattle(String id) {
        this.id = id;
    }

    public void start() {
        if (attackerUnits.isEmpty() || defenderUnits.isEmpty()) {
            result = new AirBattleResult(id, "No units on one side", AirBattleResult.Outcome.DRAW);
            state = BattleState.COMPLETED;
            return;
        }
        state = BattleState.IN_PROGRESS;
        addEvent("Air battle initiated at " + altitude + " altitude");
    }

    public AirBattleResult resolveAbstract() {
        start();
        int attack = totalAttackValue(attackerUnits);
        int defense = totalDefenseValue(defenderUnits);
        int odds = calculateOdds(attack, defense);
        int roll = DiceUtil.roll2D6();
        String crtResult = lookupCRT(odds, roll);
        AirBattleResult.Outcome outcome = determineOutcome(crtResult);
        int attackerLosses = calculateLosses(crtResult, Side.ATTACKER);
        int defenderLosses = calculateLosses(crtResult, Side.DEFENDER);
        applyLosses(attackerUnits, attackerLosses);
        applyLosses(defenderUnits, defenderLosses);
        result = new AirBattleResult(id, crtResult, outcome, attackerLosses, defenderLosses, events);
        result.setOdds(odds);
        result.setDiceRoll(roll);
        state = BattleState.COMPLETED;
        addEvent("CRT result: " + crtResult + " (odds " + odds + ", roll " + roll + ")");
        return result;
    }

    public AirBattleResult fightToCompletion() {
        start();
        mode = BattleMode.DETAILED;
        while (state == BattleState.IN_PROGRESS && currentRound < 20) {
            fightRound();
        }
        if (attackerUnits.isEmpty()) {
            result = new AirBattleResult(id, "Defender victory", AirBattleResult.Outcome.DEFENDER_VICTORY, 0, 0, events);
        } else if (defenderUnits.isEmpty()) {
            result = new AirBattleResult(id, "Attacker victory", AirBattleResult.Outcome.ATTACKER_VICTORY, 0, 0, events);
        } else {
            result = new AirBattleResult(id, "Stalemate after " + currentRound + " rounds", AirBattleResult.Outcome.DRAW, 0, 0, events);
        }
        state = BattleState.COMPLETED;
        return result;
    }

    public void fightRound() {
        currentRound++;
        addEvent("Round " + currentRound + " begins");
        int atkAttack = totalAttackValue(attackerUnits);
        int defAttack = totalAttackValue(defenderUnits);
        int defDefense = totalDefenseValue(defenderUnits);
        int atkDefense = totalDefenseValue(attackerUnits);
        int defDamage = Math.max(0, atkAttack - defDefense / 2);
        int atkDamage = Math.max(0, defAttack - atkDefense / 2);
        applyDamage(attackerUnits, atkDamage);
        applyDamage(defenderUnits, defDamage);
        addEvent("Round " + currentRound + ": Attacker deals " + defDamage + ", Defender deals " + atkDamage);
        if (attackerUnits.isEmpty() || defenderUnits.isEmpty()) {
            state = BattleState.COMPLETED;
        }
    }

    public void reinforce(List<TravellerSpacecraft> units, Side side) {
        if (state != BattleState.IN_PROGRESS) return;
        if (side == Side.ATTACKER) {
            attackerUnits.addAll(units);
        } else {
            defenderUnits.addAll(units);
        }
        addEvent("Reinforcements arrived for " + side + ": " + units.size() + " units");
    }

    public void retreat() {
        if (state != BattleState.IN_PROGRESS) return;
        result = new AirBattleResult(id, "Retreat", AirBattleResult.Outcome.RETREAT, 0, 0, events);
        state = BattleState.COMPLETED;
        addEvent("Battle ended in retreat");
    }

    private int totalAttackValue(List<TravellerSpacecraft> units) {
        return units.stream().mapToInt(TravellerSpacecraft::getAttackValue).sum();
    }

    private int totalDefenseValue(List<TravellerSpacecraft> units) {
        return units.stream().mapToInt(TravellerSpacecraft::getDefenseValue).sum();
    }

    private int calculateOdds(int attack, int defense) {
        if (defense == 0) return 9;
        return Math.max(1, Math.min(9, attack * 3 / defense));
    }

    private String lookupCRT(int odds, int roll) {
        if (roll <= 3) return "Superior - Enemy destroyed";
        if (roll <= 5) return "Good - Enemy eliminated";
        if (roll <= 7) return "Even - Both sides lose";
        if (roll <= 9) return "Poor - Attacker loses";
        return "Hopeless - Attacker destroyed";
    }

    private AirBattleResult.Outcome determineOutcome(String crt) {
        if (crt.contains("Superior") || crt.contains("Good")) return AirBattleResult.Outcome.ATTACKER_VICTORY;
        if (crt.contains("Even")) return AirBattleResult.Outcome.PYRRHIC_VICTORY;
        if (crt.contains("Poor")) return AirBattleResult.Outcome.DEFENDER_VICTORY;
        return AirBattleResult.Outcome.DEFENDER_VICTORY;
    }

    private int calculateLosses(String crt, Side side) {
        if (crt.contains("Superior")) return side == Side.DEFENDER ? 3 : 0;
        if (crt.contains("Good")) return side == Side.DEFENDER ? 2 : 1;
        if (crt.contains("Even")) return 1;
        if (crt.contains("Poor")) return side == Side.ATTACKER ? 2 : 1;
        return side == Side.ATTACKER ? 3 : 0;
    }

    private void applyLosses(List<TravellerSpacecraft> units, int losses) {
        for (int i = 0; i < Math.min(losses, units.size()); i++) {
            TravellerSpacecraft removed = units.remove(units.size() - 1);
            addEvent(removed.getName() + " destroyed");
        }
    }

    private void applyDamage(List<TravellerSpacecraft> units, int damage) {
        int i = 0;
        int remaining = damage;
        while (i < units.size() && remaining > 0) {
            TravellerSpacecraft unit = units.get(i);
            unit.setHullPoints(unit.getHullPoints() - remaining);
            if (unit.getHullPoints() <= 0) {
                units.remove(i);
                addEvent(unit.getName() + " destroyed");
            } else {
                i++;
            }
            remaining = Math.max(0, remaining - 5);
        }
    }

    private void addEvent(String message) {
        AirBattleEvent event = new AirBattleEvent(currentRound, message);
        events.add(event);
    }

    // Getters
    public String getId() { return id; }
    public List<TravellerSpacecraft> getAttackerUnits() { return attackerUnits; }
    public List<TravellerSpacecraft> getDefenderUnits() { return defenderUnits; }
    public Altitude getAltitude() { return altitude; }
    public void setAltitude(Altitude altitude) { this.altitude = altitude; }
    public BattleMode getMode() { return mode; }
    public void setMode(BattleMode mode) { this.mode = mode; }
    public BattleState getState() { return state; }
    public AirBattleResult getResult() { return result; }
    public List<AirBattleEvent> getEvents() { return events; }
    public int getCurrentRound() { return currentRound; }
}
