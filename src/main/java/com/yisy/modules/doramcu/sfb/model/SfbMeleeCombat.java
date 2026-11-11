package com.yisy.modules.doramcu.sfb.model;

import com.yisy.modules.doramcu.util.DiceUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Round-by-round melee combat engine for SFB.
 * <p>
 * Follows the Traveller-style melee combat system with attack/parry/damage rolls.
 * Each round, combatants roll to attack, the defender rolls to parry, and damage
 * is calculated if the attack succeeds.
 * </p>
 */
public class SfbMeleeCombat {

    private final SfbMeleeCombatant attacker;
    private final SfbMeleeCombatant defender;
    private final List<CombatRound> rounds = new ArrayList<>();
    private CombatOutcome outcome = CombatOutcome.IN_PROGRESS;
    private boolean active = true;

    public SfbMeleeCombat(SfbMeleeCombatant attacker, SfbMeleeCombatant defender) {
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
        int attackRoll = DiceUtil.roll2D6() + attacker.getSt();
        int parryRoll = DiceUtil.roll2D6() + defender.getDx() + defender.getWeapon().getParry();

        round.setAttackRoll(attackRoll);
        round.setParryRoll(parryRoll);

        if (attackRoll > parryRoll) {
            // Hit!
            int damage = DiceUtil.roll2D6() + attacker.getWeapon().getDamage();
            int armorReduction = defender.getArmor().getProtection();
            int actualDamage = Math.max(0, damage - armorReduction);
            round.setDamage(actualDamage);
            round.setHit(true);

            // Critical hit on natural 12
            if (attackRoll >= 12 + defender.getDx() + defender.getWeapon().getParry()) {
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
    public SfbMeleeCombatant getAttacker() {
        return attacker;
    }

    /**
     * Returns the defender.
     */
    public SfbMeleeCombatant getDefender() {
        return defender;
    }

    /**
     * A combatant in melee combat.
     */
    public static class SfbMeleeCombatant {
        private final String name;
        private final int st;
        private final int dx;
        private final int en;
        private int hitPoints;
        private final int maxHitPoints;
        private SfbMeleeWeapon weapon = SfbMeleeWeapon.FISTS;
        private SfbArmor armor = SfbArmor.NONE;

        public SfbMeleeCombatant(String name, int st, int dx, int en) {
            this.name = name;
            this.st = st;
            this.dx = dx;
            this.en = en;
            this.maxHitPoints = st * 2 + en;
            this.hitPoints = this.maxHitPoints;
        }

        public String getName() { return name; }
        public int getSt() { return st; }
        public int getDx() { return dx; }
        public int getEn() { return en; }
        public int getHitPoints() { return hitPoints; }
        public int getMaxHitPoints() { return maxHitPoints; }
        public SfbMeleeWeapon getWeapon() { return weapon; }
        public SfbArmor getArmor() { return armor; }

        public void setHitPoints(int hitPoints) { this.hitPoints = hitPoints; }
        public void setWeapon(SfbMeleeWeapon weapon) { this.weapon = weapon; }
        public void setArmor(SfbArmor armor) { this.armor = armor; }
    }

    /**
     * A single round of combat.
     */
    public static class CombatRound {
        private int roundNumber;
        private int attackRoll;
        private int parryRoll;
        private boolean hit;
        private boolean critical;
        private int damage;

        public int getRoundNumber() { return roundNumber; }
        public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }
        public int getAttackRoll() { return attackRoll; }
        public void setAttackRoll(int attackRoll) { this.attackRoll = attackRoll; }
        public int getParryRoll() { return parryRoll; }
        public void setParryRoll(int parryRoll) { this.parryRoll = parryRoll; }
        public boolean isHit() { return hit; }
        public void setHit(boolean hit) { this.hit = hit; }
        public boolean isCritical() { return critical; }
        public void setCritical(boolean critical) { this.critical = critical; }
        public int getDamage() { return damage; }
        public void setDamage(int damage) { this.damage = damage; }
    }

    /**
     * Possible outcomes of melee combat.
     */
    public enum CombatOutcome {
        IN_PROGRESS,
        ATTACKER_WINS,
        DEFENDER_WINS,
        DRAW
    }
}
