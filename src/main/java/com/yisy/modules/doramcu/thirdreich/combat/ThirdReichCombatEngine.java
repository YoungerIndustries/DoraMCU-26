package com.yisy.modules.doramcu.thirdreich.combat;

import com.yisy.modules.doramcu.thirdreich.commander.AICommander;
import com.yisy.modules.doramcu.thirdreich.domain.*;
import com.yisy.modules.doramcu.util.DiceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Main combat resolution engine for 3R4 ground battles.
 * <p>
 * Resolves battles round-by-round using CRT (Combat Results Table) mechanics,
 * applying terrain modifiers, commander leadership bonuses, and unit quality factors.
 * </p>
 * <p>
 * The engine accepts two sides of 3R4GroundUnits with optional commanders,
 * resolves the battle, and returns a ThirdReichBattleResult with detailed outcomes.
 * </p>
 */
public class ThirdReichCombatEngine {

    private static final int MAX_ROUNDS = 20;
    private static final int MORALE_ROUT_THRESHOLD = 0;

    private final CombatResultsTable crt = new CombatResultsTable();

    /**
     * Resolves a complete ground battle.
     *
     * @param battle the battle to resolve
     * @param attackerCommander the attacker's commander, or null
     * @param defenderCommander the defender's commander, or null
     * @return the battle result
     */
    public ThirdReichBattleResult resolveBattle(ThirdReichBattle battle,
                                                 AICommander attackerCommander,
                                                 AICommander defenderCommander) {
        if (battle == null) {
            throw new IllegalArgumentException("Battle must not be null");
        }

        battle.start();

        if (battle.getAttackerUnits().isEmpty() || battle.getDefenderUnits().isEmpty()) {
            ThirdReichBattleResult result = new ThirdReichBattleResult(
                    battle.getId(), ThirdReichBattleResult.Outcome.DRAW);
            result.addEvent("Battle cannot proceed: one side has no units");
            battle.endBattle(result);
            return result;
        }

        int totalAttackerCasualties = 0;
        int totalDefenderCasualties = 0;
        int attackerUnitsLost = 0;
        int defenderUnitsLost = 0;

        // Fight round by round
        while (battle.getState() == ThirdReichBattle.BattleState.IN_PROGRESS
                && battle.getCurrentRound() < MAX_ROUNDS) {
            BattleRound round = fightRound(battle, attackerCommander, defenderCommander);

            totalAttackerCasualties += round.getAttackerDamageDealt();
            totalDefenderCasualties += round.getDefenderDamageDealt();
            attackerUnitsLost += round.getAttackerUnitsDestroyed();
            defenderUnitsLost += round.getDefenderUnitsDestroyed();

            // Check for battle end conditions
            if (!hasEffectiveUnits(battle.getAttackerUnits())) {
                battle.endBattle(createResult(battle, ThirdReichBattleResult.Outcome.DEFENDER_VICTORY,
                        totalAttackerCasualties, totalDefenderCasualties,
                        attackerUnitsLost, defenderUnitsLost));
                return battle.getResult();
            }
            if (!hasEffectiveUnits(battle.getDefenderUnits())) {
                battle.endBattle(createResult(battle, ThirdReichBattleResult.Outcome.ATTACKER_VICTORY,
                        totalAttackerCasualties, totalDefenderCasualties,
                        attackerUnitsLost, defenderUnitsLost));
                return battle.getResult();
            }
        }

        // Determine final outcome
        ThirdReichBattleResult.Outcome outcome;
        int atkPower = battle.getAttackerTotalAttack();
        int defPower = battle.getDefenderTotalDefense();

        if (atkPower > defPower * 2) {
            outcome = ThirdReichBattleResult.Outcome.ATTACKER_VICTORY;
        } else if (defPower > atkPower * 2) {
            outcome = ThirdReichBattleResult.Outcome.DEFENDER_VICTORY;
        } else if (atkPower > defPower) {
            outcome = ThirdReichBattleResult.Outcome.PYRRHIC_VICTORY;
        } else if (defPower > atkPower) {
            outcome = ThirdReichBattleResult.Outcome.DEFENDER_HOLDS;
        } else {
            outcome = ThirdReichBattleResult.Outcome.DRAW;
        }

        ThirdReichBattleResult result = createResult(battle, outcome,
                totalAttackerCasualties, totalDefenderCasualties,
                attackerUnitsLost, defenderUnitsLost);
        battle.endBattle(result);
        return result;
    }

    /**
     * Fights a single round of combat.
     */
    private BattleRound fightRound(ThirdReichBattle battle,
                                    AICommander attackerCommander,
                                    AICommander defenderCommander) {
        battle.nextRound();
        BattleRound round = new BattleRound(battle.getCurrentRound());

        // Calculate effective attack and defense with modifiers
        int atkAttack = calculateSideAttack(battle.getAttackerUnits(), attackerCommander);
        int defDefense = calculateSideDefense(battle.getDefenderUnits(), defenderCommander, battle.getTerrainContext());

        // Attacker strikes first
        double odds = CombatResultsTable.calculateOdds(atkAttack, defDefense);
        int diceRoll = DiceUtil.roll2D6();
        CombatResultsTable.CombatResult result = crt.lookup(odds, diceRoll);

        int defCasualtyPercent = CombatResultsTable.getDefenderCasualtyPercent(result);
        int defMoralePenalty = CombatResultsTable.getDefenderMoralePenalty(result);

        int defDamage = applyDamage(battle.getDefenderUnits(), defCasualtyPercent);
        applyMorale(battle.getDefenderUnits(), defMoralePenalty);

        round.setAttackerDamageDealt(defDamage);
        round.setDefenderMoraleLoss(defMoralePenalty);
        round.addEvent("Attacker strikes: " + result + " (odds " + String.format("%.1f", odds) + ", roll " + diceRoll + ")");

        // Check if defender is still effective
        if (!hasEffectiveUnits(battle.getDefenderUnits())) {
            round.addEvent("Defender eliminated");
            return round;
        }

        // Defender counter-attacks
        int defAttack = calculateSideAttack(battle.getDefenderUnits(), defenderCommander);
        int atkDefense = calculateSideDefense(battle.getAttackerUnits(), attackerCommander, battle.getTerrainContext());

        odds = CombatResultsTable.calculateOdds(defAttack, atkDefense);
        diceRoll = DiceUtil.roll2D6();
        result = crt.lookup(odds, diceRoll);

        int atkCasualtyPercent = CombatResultsTable.getAttackerCasualtyPercent(result);
        int atkMoralePenalty = CombatResultsTable.getAttackerMoralePenalty(result);

        int atkDamage = applyDamage(battle.getAttackerUnits(), atkCasualtyPercent);
        applyMorale(battle.getAttackerUnits(), atkMoralePenalty);

        round.setDefenderDamageDealt(atkDamage);
        round.setAttackerMoraleLoss(atkMoralePenalty);
        round.addEvent("Defender counter-attacks: " + result + " (odds " + String.format("%.1f", odds) + ", roll " + diceRoll + ")");

        // Count destroyed units
        int atkLost = 0;
        int defLost = 0;
        for (ThirdReichGroundUnit unit : battle.getAttackerUnits()) {
            if (!unit.isCombatEffective()) atkLost++;
        }
        for (ThirdReichGroundUnit unit : battle.getDefenderUnits()) {
            if (!unit.isCombatEffective()) defLost++;
        }
        round.setAttackerUnitsDestroyed(atkLost);
        round.setDefenderUnitsDestroyed(defLost);

        return round;
    }

    /**
     * Calculates the total attack power for a side, including commander bonuses.
     */
    private int calculateSideAttack(List<ThirdReichGroundUnit> units, AICommander commander) {
        int total = units.stream()
                .filter(ThirdReichGroundUnit::isCombatEffective)
                .mapToInt(ThirdReichGroundUnit::getEffectiveAttack)
                .sum();

        if (commander != null) {
            total += commander.getTotalAttackBonus();
            total += commander.getLeadershipModifier();
        }

        return Math.max(1, total);
    }

    /**
     * Calculates the total defense power for a side, including commander and terrain bonuses.
     */
    private int calculateSideDefense(List<ThirdReichGroundUnit> units, AICommander commander,
                                      TerrainContext terrain) {
        int total = units.stream()
                .filter(ThirdReichGroundUnit::isCombatEffective)
                .mapToInt(ThirdReichGroundUnit::getEffectiveDefense)
                .sum();

        if (commander != null) {
            total += commander.getTotalDefenseBonus();
            total += commander.getLeadershipModifier();
        }

        if (terrain != null) {
            total += terrain.getTotalDefenseBonus();
        }

        return Math.max(1, total);
    }

    /**
     * Applies damage to all units in a side proportionally.
     *
     * @return total damage dealt
     */
    private int applyDamage(List<ThirdReichGroundUnit> units, int casualtyPercent) {
        List<ThirdReichGroundUnit> effective = new ArrayList<>();
        for (ThirdReichGroundUnit unit : units) {
            if (unit.isCombatEffective()) {
                effective.add(unit);
            }
        }

        if (effective.isEmpty()) return 0;

        int totalDamage = 0;
        for (ThirdReichGroundUnit unit : effective) {
            int damage = Math.max(1, unit.getMaxStrength() * casualtyPercent / 100);
            // Randomize damage slightly
            damage += DiceUtil.rollDSides(5) - 3;
            damage = Math.max(1, damage);
            unit.takeDamage(damage);
            totalDamage += damage;
        }

        return totalDamage;
    }

    /**
     * Applies morale changes to all units in a side.
     */
    private void applyMorale(List<ThirdReichGroundUnit> units, int moralePenalty) {
        for (ThirdReichGroundUnit unit : units) {
            if (unit.isCombatEffective()) {
                unit.adjustMorale(-moralePenalty);
            }
        }
    }

    /**
     * Checks if a side has any combat-effective units.
     */
    private boolean hasEffectiveUnits(List<ThirdReichGroundUnit> units) {
        return units.stream().anyMatch(ThirdReichGroundUnit::isCombatEffective);
    }

    /**
     * Creates a battle result with the given parameters.
     */
    private ThirdReichBattleResult createResult(ThirdReichBattle battle,
                                                 ThirdReichBattleResult.Outcome outcome,
                                                 int attackerCasualties, int defenderCasualties,
                                                 int attackerUnitsLost, int defenderUnitsLost) {
        ThirdReichBattleResult result = new ThirdReichBattleResult(battle.getId(), outcome);
        result.setAttackerCasualties(attackerCasualties);
        result.setDefenderCasualties(defenderCasualties);
        result.setAttackerUnitsLost(attackerUnitsLost);
        result.setDefenderUnitsLost(defenderUnitsLost);
        result.setRoundsFought(battle.getCurrentRound());
        result.generateNarrative();
        return result;
    }
}
