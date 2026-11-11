package com.yisy.modules.doramcu.sfb.model;

import com.yisy.modules.doramcu.util.DiceUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Round-by-round firearms combat engine for SFB.
 * <p>
 * Follows the Traveller-style firearms combat system with attack/dodge/damage rolls.
 * Each round, combatants roll to attack, the defender rolls to dodge, and damage
 * is calculated if the attack succeeds.
 * </p>
 */
public class SfbFirearmsCombat {

    private final SfbFirearmsCombatant attacker;
    private final SfbFirearmsCombatant defender;
    private final List<CombatRound> rounds = new ArrayList<>();
    private CombatOutcome outcome = CombatOutcome.IN_PROGRESS;
    private boolean active = true;

    public SfbFirearmsCombat(SfbFirearmsCombatant attacker, SfbFirearmsCombatant defender) {
        this.attacker = attacker;
        this.defender = defender;
    }

    /**
     * Executes a single round of combat.
     *
     * @return true if combat is still active after this round
     */
    public boolean executeRound() {
        if (!active) {
            return false;
        }

        CombatRound round = new CombatRound();
        round.setRoundNumber(rounds.size() + 1);

        // Attacker attacks
        int attackRoll = DiceUtil.roll2D6() + attacker.getDx();
        int dodgeRoll = DiceUtil.roll2D6() + defender.getDx();

        round.setAttackRoll(attackRoll);
        round.setDodgeRoll(dodgeRoll);

        if (attackRoll > dodgeRoll) {
            // Hit!
            int damage = DiceUtil.roll2D6() + attacker.getWeapon().getDamage();
            int armorReduction = defender.getArmor().getProtection();
            int actualDamage = Math.max(0, damage - armorReduction);
            round.setDamage(actualDamage);
            round.setHit(true);

            // Critical hit on natural 12
            if (attackRoll >= 12 + defender.getDx()) {
                actualDamage *= 2;
                round.setCritical(true);
            }

            defender.setHitPoints(defender.getHitPoints() - actualDamage);
        } else {
            round.setHit(false);
            round.setDamage(0);
        }

        rounds.add(round);

        // Check for knockout
        if (defender.getHitPoints() <= 0) {
            defender.setHitPoints(0);
            active = false;
            outcome = CombatOutcome.ATTACKER_WINS;
        }

        return active;
    }

    /**
     * Resolves the entire combat automatically.
     */
    public void resolveCombat() {
        while (active) {
            executeRound();
        }
    }

    /**
     * Returns the combat outcome.
     */
    public CombatOutcome getOutcome() {
        return outcome;
    }

    /**
     * Returns whether combat is still active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Returns the list of combat rounds.
     */
    public List<CombatRound> getRounds() {
        return rounds;
    }

    /**
     * Returns the attacker.
     */
    public SfbFirearmsCombatant getAttacker() {
        return attacker;
    }

    /**
     * Returns the defender.
     */
    public SfbFirearmsCombatant getDefender() {
        return defender;
    }

    /**
     * A combatant in firearms combat.
     */
    public static class SfbFirearmsCombatant {
        private final String name;
        private final int dx;
        private final int en;
        private int hitPoints;
        private final int maxHitPoints;
        private SfbFirearm weapon = SfbFirearm.PHASER_PISTOL;
        private SfbArmor armor = SfbArmor.NONE;

        public SfbFirearmsCombatant(String name, int dx, int en) {
            this.name = name;
            this.dx = dx;
            this.en = en;
            this.maxHitPoints = dx + en * 2;
            this.hitPoints = this.maxHitPoints;
        }

        public String getName() { return name; }
        public int getDx() { return dx; }
        public int getEn() { return en; }
        public int getHitPoints() { return hitPoints; }
        public int getMaxHitPoints() { return maxHitPoints; }
        public SfbFirearm getWeapon() { return weapon; }
        public SfbArmor getArmor() { return armor; }

        public void setHitPoints(int hitPoints) { this.hitPoints = hitPoints; }
        public void setWeapon(SfbFirearm weapon) { this.weapon = weapon; }
        public void setArmor(SfbArmor armor) { this.armor = armor; }
    }

    /**
     * A single round of firearms combat.
     */
    public static class CombatRound {
        private int roundNumber;
        private int attackRoll;
        private int dodgeRoll;
        private boolean hit;
        private boolean critical;
        private int damage;

        public int getRoundNumber() { return roundNumber; }
        public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }
        public int getAttackRoll() { return attackRoll; }
        public void setAttackRoll(int attackRoll) { this.attackRoll = attackRoll; }
        public int getDodgeRoll() { return dodgeRoll; }
        public void setDodgeRoll(int dodgeRoll) { this.dodgeRoll = dodgeRoll; }
        public boolean isHit() { return hit; }
        public void setHit(boolean hit) { this.hit = hit; }
        public boolean isCritical() { return critical; }
        public void setCritical(boolean critical) { this.critical = critical; }
        public int getDamage() { return damage; }
        public void setDamage(int damage) { this.damage = damage; }
    }

    /**
     * Possible outcomes of firearms combat.
     */
    public enum CombatOutcome {
        IN_PROGRESS,
        ATTACKER_WINS,
        DEFENDER_WINS,
        DRAW
    }
}
