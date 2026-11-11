package com.yisy.modules.doramcu.babylon5.model;

import com.yisy.modules.doramcu.util.DiceUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Melee combat engine for Babylon 5.
 * <p>
 * Resolves melee combat between two combatants round by round. Each round
 * follows the combat sequence:
 * </p>
 * <ol>
 *   <li><b>Attack Roll</b> — attacker rolls 2D6; if ≤ attack value, the attack hits</li>
 *   <li><b>Parry Roll</b> — defender rolls 2D6; if ≤ parry value, the attack is blocked</li>
 *   <li><b>Damage Roll</b> — if the attack lands, damage is rolled based on weapon</li>
 *   <li><b>Armor Reduction</b> — armor protection is subtracted from damage</li>
 *   <li><b>Wound Check</b> — if damage is severe, a wound check may be required</li>
 * </ol>
 * <p>
 * Combat continues until one combatant is knocked out or dead.
 * </p>
 */
public class B5MeleeCombat {

    private static final int MAX_ROUNDS = 50;

    private final B5MeleeCombatant attacker;
    private final B5MeleeCombatant defender;
    private final List<CombatRound> rounds;
    private final List<String> combatLog;
    private int currentRound;
    private boolean isActive;
    private CombatOutcome outcome;

    /**
     * Possible outcomes of a melee combat encounter.
     */
    public enum CombatOutcome {
        IN_PROGRESS("In Progress"),
        ATTACKER_VICTORY("Attacker Victory"),
        DEFENDER_VICTORY("Defender Victory"),
        MUTUAL_DEFEAT("Mutual Defeat"),
        DRAW("Draw");

        private final String description;

        CombatOutcome(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Result of a single attack attempt within a combat round.
     */
    public enum AttackResult {
        MISS("Miss"),
        PARRIED("Parried"),
        HIT("Hit"),
        CRITICAL("Critical Hit");

        private final String description;

        AttackResult(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * A single round of melee combat.
     */
    public static class CombatRound {
        private final int roundNumber;
        private final AttackResult attackResult;
        private final int damageDealt;
        private final int damageAfterArmor;
        private final String logEntry;

        CombatRound(int roundNumber, AttackResult attackResult, int damageDealt, int damageAfterArmor, String logEntry) {
            this.roundNumber = roundNumber;
            this.attackResult = attackResult;
            this.damageDealt = damageDealt;
            this.damageAfterArmor = damageAfterArmor;
            this.logEntry = logEntry;
        }

        public int getRoundNumber() {
            return roundNumber;
        }

        public AttackResult getAttackResult() {
            return attackResult;
        }

        public int getDamageDealt() {
            return damageDealt;
        }

        public int getDamageAfterArmor() {
            return damageAfterArmor;
        }

        public String getLogEntry() {
            return logEntry;
        }
    }

    /**
     * A combatant in melee combat.
     */
    public static class B5MeleeCombatant {
        private String name;
        private int strength;
        private int dexterity;
        private int endurance;
        private int hitPoints;
        private int maxHitPoints;
        private B5MeleeWeapon weapon;
        private B5Armor armor;
        private boolean isKnockedOut;

        public B5MeleeCombatant(String name, int strength, int dexterity, int endurance) {
            this.name = name;
            this.strength = strength;
            this.dexterity = dexterity;
            this.endurance = endurance;
            this.maxHitPoints = strength * 2 + endurance;
            this.hitPoints = this.maxHitPoints;
            this.weapon = B5MeleeWeapon.FISTS;
            this.armor = B5Armor.NONE;
            this.isKnockedOut = false;
        }

        public String getName() {
            return name;
        }

        public int getStrength() {
            return strength;
        }

        public int getDexterity() {
            return dexterity;
        }

        public int getEndurance() {
            return endurance;
        }

        public int getHitPoints() {
            return hitPoints;
        }

        public int getMaxHitPoints() {
            return maxHitPoints;
        }

        public B5MeleeWeapon getWeapon() {
            return weapon;
        }

        public void setWeapon(B5MeleeWeapon weapon) {
            this.weapon = weapon;
        }

        public B5Armor getArmor() {
            return armor;
        }

        public void setArmor(B5Armor armor) {
            this.armor = armor;
        }

        public boolean isKnockedOut() {
            return isKnockedOut;
        }

        /**
         * Returns the attack value for this combatant.
         * Attack = (St + Dx) / 2 + weapon bonus
         */
        public int getAttackValue() {
            return (strength + dexterity) / 2 + (weapon == B5MeleeWeapon.FISTS ? 0 : 1);
        }

        /**
         * Returns the parry value for this combatant.
         * Parry = Dx + weapon parry + armor penalty
         */
        public int getParryValue() {
            return dexterity + weapon.getParry() - armor.getEncumbrance();
        }

        /**
         * Takes damage and updates hit points.
         */
        public void takeDamage(int damage) {
            hitPoints -= damage;
            if (hitPoints <= 0) {
                hitPoints = 0;
                isKnockedOut = true;
            }
        }
    }

    public B5MeleeCombat(B5MeleeCombatant attacker, B5MeleeCombatant defender) {
        this.attacker = attacker;
        this.defender = defender;
        this.rounds = new ArrayList<>();
        this.combatLog = new ArrayList<>();
        this.currentRound = 0;
        this.isActive = true;
        this.outcome = CombatOutcome.IN_PROGRESS;
    }

    /**
     * Resolves a single round of combat.
     */
    public CombatRound resolveRound() {
        if (!isActive) {
            return null;
        }

        currentRound++;
        combatLog.add("=== Round " + currentRound + " ===");

        // Attack roll
        int attackRoll = DiceUtil.roll2D6();
        int attackValue = attacker.getAttackValue();
        combatLog.add(attacker.getName() + " attacks (roll " + attackRoll + ", need " + attackValue + ")");

        if (attackRoll > attackValue) {
            combatLog.add(attacker.getName() + " misses!");
            CombatRound round = new CombatRound(currentRound, AttackResult.MISS, 0, 0, attacker.getName() + " misses!");
            rounds.add(round);
            return round;
        }

        // Parry roll
        int parryRoll = DiceUtil.roll2D6();
        int parryValue = defender.getParryValue();
        combatLog.add(defender.getName() + " parries (roll " + parryRoll + ", need " + parryValue + ")");

        if (parryRoll <= parryValue) {
            combatLog.add(defender.getName() + " parries the attack!");
            CombatRound round = new CombatRound(currentRound, AttackResult.PARRIED, 0, 0, defender.getName() + " parries!");
            rounds.add(round);
            return round;
        }

        // Damage roll
        int damageRoll = DiceUtil.rollD6();
        int weaponDamage = attacker.getWeapon().getDamage();
        int totalDamage = damageRoll * weaponDamage;

        // Critical hit check
        AttackResult result = (attackRoll <= 3) ? AttackResult.CRITICAL : AttackResult.HIT;
        if (result == AttackResult.CRITICAL) {
            totalDamage *= 2;
            combatLog.add("CRITICAL HIT!");
        }

        // Armor reduction
        int armorProtection = defender.getArmor().getProtection();
        int damageAfterArmor = Math.max(0, totalDamage - armorProtection);

        defender.takeDamage(damageAfterArmor);
        combatLog.add(attacker.getName() + " hits for " + damageAfterArmor + " damage (" + totalDamage + " - " + armorProtection + " armor)");
        combatLog.add(defender.getName() + " HP: " + defender.getHitPoints() + "/" + defender.getMaxHitPoints());

        CombatRound round = new CombatRound(currentRound, result, totalDamage, damageAfterArmor,
                attacker.getName() + " hits for " + damageAfterArmor + " damage");
        rounds.add(round);

        // Check for combat end
        checkCombatEnd();
        return round;
    }

    /**
     * Resolves the entire combat automatically.
     */
    public void resolveCombat() {
        while (isActive && currentRound < MAX_ROUNDS) {
            resolveRound();
        }
        if (isActive && currentRound >= MAX_ROUNDS) {
            outcome = CombatOutcome.DRAW;
            isActive = false;
            combatLog.add("Combat ended: Draw (max rounds reached)");
        }
    }

    private void checkCombatEnd() {
        if (attacker.isKnockedOut() && defender.isKnockedOut()) {
            outcome = CombatOutcome.MUTUAL_DEFEAT;
            isActive = false;
            combatLog.add("Combat ended: Mutual Defeat");
        } else if (defender.isKnockedOut()) {
            outcome = CombatOutcome.ATTACKER_VICTORY;
            isActive = false;
            combatLog.add("Combat ended: " + attacker.getName() + " wins!");
        } else if (attacker.isKnockedOut()) {
            outcome = CombatOutcome.DEFENDER_VICTORY;
            isActive = false;
            combatLog.add("Combat ended: " + defender.getName() + " wins!");
        }
    }

    public B5MeleeCombatant getAttacker() {
        return attacker;
    }

    public B5MeleeCombatant getDefender() {
        return defender;
    }

    public List<CombatRound> getRounds() {
        return Collections.unmodifiableList(rounds);
    }

    public List<String> getCombatLog() {
        return Collections.unmodifiableList(combatLog);
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public boolean isActive() {
        return isActive;
    }

    public CombatOutcome getOutcome() {
        return outcome;
    }
}
