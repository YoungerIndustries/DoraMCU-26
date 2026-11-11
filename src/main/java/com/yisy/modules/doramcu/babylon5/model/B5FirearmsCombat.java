package com.yisy.modules.doramcu.babylon5.model;

import com.yisy.modules.doramcu.util.DiceUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Firearms combat engine for Babylon 5.
 * <p>
 * Resolves firearms combat between two combatants round by round.
 * Each round follows the combat sequence:
 * </p>
 * <ol>
 *   <li><b>Attack Roll</b> — attacker rolls 2D6; if ≤ attack value, the attack hits</li>
 *   <li><b>Dodge Roll</b> — defender rolls 2D6; if ≤ dodge value, the attack is dodged</li>
 *   <li><b>Damage Roll</b> — if the attack lands, damage is rolled based on weapon</li>
 *   <li><b>Armor Reduction</b> — armor protection is subtracted from damage</li>
 * </ol>
 * <p>
 * Range affects the attack value: Near (0), Intermediate (-1), Far (-2).
 * </p>
 */
public class B5FirearmsCombat {

    private static final int MAX_ROUNDS = 50;

    private final B5FirearmsCombatant attacker;
    private final B5FirearmsCombatant defender;
    private final List<CombatRound> rounds;
    private final List<String> combatLog;
    private int currentRound;
    private boolean isActive;
    private CombatOutcome outcome;

    /**
     * Possible outcomes of a firearms combat encounter.
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
        DODGED("Dodged"),
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
     * A single round of firearms combat.
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
     * A combatant in firearms combat.
     */
    public static class B5FirearmsCombatant {
        private String name;
        private int dexterity;
        private int endurance;
        private int hitPoints;
        private int maxHitPoints;
        private B5Firearm weapon;
        private B5Armor armor;
        private boolean isKnockedOut;

        public B5FirearmsCombatant(String name, int dexterity, int endurance) {
            this.name = name;
            this.dexterity = dexterity;
            this.endurance = endurance;
            this.maxHitPoints = dexterity + endurance * 2;
            this.hitPoints = this.maxHitPoints;
            this.weapon = B5Firearm.PISTOL;
            this.armor = B5Armor.NONE;
            this.isKnockedOut = false;
        }

        public String getName() {
            return name;
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

        public B5Firearm getWeapon() {
            return weapon;
        }

        public void setWeapon(B5Firearm weapon) {
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
         * Attack = weapon attack value + Dx/2
         */
        public int getAttackValue() {
            return weapon.getAttackValue() + dexterity / 2;
        }

        /**
         * Returns the dodge value for this combatant.
         * Dodge = Dx - armor encumbrance
         */
        public int getDodgeValue() {
            return dexterity - armor.getEncumbrance();
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

    public B5FirearmsCombat(B5FirearmsCombatant attacker, B5FirearmsCombatant defender) {
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

        // Attack roll with range modifier
        int attackRoll = DiceUtil.roll2D6();
        int attackValue = attacker.getAttackValue();
        combatLog.add(attacker.getName() + " attacks (roll " + attackRoll + ", need " + attackValue + ")");

        if (attackRoll > attackValue) {
            combatLog.add(attacker.getName() + " misses!");
            CombatRound round = new CombatRound(currentRound, AttackResult.MISS, 0, 0, attacker.getName() + " misses!");
            rounds.add(round);
            return round;
        }

        // Dodge roll
        int dodgeRoll = DiceUtil.roll2D6();
        int dodgeValue = defender.getDodgeValue();
        combatLog.add(defender.getName() + " dodges (roll " + dodgeRoll + ", need " + dodgeValue + ")");

        if (dodgeRoll <= dodgeValue) {
            combatLog.add(defender.getName() + " dodges the attack!");
            CombatRound round = new CombatRound(currentRound, AttackResult.DODGED, 0, 0, defender.getName() + " dodges!");
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

    public B5FirearmsCombatant getAttacker() {
        return attacker;
    }

    public B5FirearmsCombatant getDefender() {
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
