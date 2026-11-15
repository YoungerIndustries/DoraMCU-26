package com.yisy.modules.doramcu.shared.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a set of modifiers extracted from the rules database for a combat action.
 * <p>
 * The RulesAwareCombatAdapter queries the rules database using a {@link RulesContext}
 * and returns a RulesModifier that aggregates all applicable bonuses, penalties,
 * and special effects that should be applied to the attack roll and damage.
 * </p>
 * <p>
 * Example: a Star Wars trooper attacking from behind half cover might receive
 * attackBonus=2 (cover), defenseBonus=2 (cover), and a note explaining the
 * source rule for each modifier.
 * </p>
 */
public class RulesModifier {

    /** Bonus to the attack roll (positive = easier to hit). */
    private int attackBonus;

    /** Bonus to the defender's AC / defense value (positive = harder to hit). */
    private int defenseBonus;

    /** Flat modifier to damage dealt (positive = more damage). */
    private int damageModifier;

    /** Whether the attacker has advantage (roll twice, take higher). */
    private boolean advantage;

    /** Whether the attacker has disadvantage (roll twice, take lower). */
    private boolean disadvantage;

    /** Whether this attack is treated as a critical hit regardless of roll. */
    private boolean autoCritical;

    /** Whether this attack automatically misses. */
    private boolean autoMiss;

    /** Multiplier to damage (e.g., 2 for double damage, 0 for no damage). Default 1. */
    private int damageMultiplier = 1;

    /** The dice formula to use for this attack, if specified by rules (e.g., "2d6+3"). */
    private String diceFormula;

    /** The damage type, if overridden by rules (e.g., "ENERGY" instead of "KINETIC"). */
    private String damageType;

    /** List of rule sources that contributed to these modifiers. */
    private List<String> ruleSources = new ArrayList<>();

    /** Human-readable description of all applied modifiers. */
    private String description;

    // --- Getters and Setters ---

    public int getAttackBonus() {
        return attackBonus;
    }

    public void setAttackBonus(int attackBonus) {
        this.attackBonus = attackBonus;
    }

    public int getDefenseBonus() {
        return defenseBonus;
    }

    public void setDefenseBonus(int defenseBonus) {
        this.defenseBonus = defenseBonus;
    }

    public int getDamageModifier() {
        return damageModifier;
    }

    public void setDamageModifier(int damageModifier) {
        this.damageModifier = damageModifier;
    }

    public boolean isAdvantage() {
        return advantage;
    }

    public void setAdvantage(boolean advantage) {
        this.advantage = advantage;
    }

    public boolean isDisadvantage() {
        return disadvantage;
    }

    public void setDisadvantage(boolean disadvantage) {
        this.disadvantage = disadvantage;
    }

    public boolean isAutoCritical() {
        return autoCritical;
    }

    public void setAutoCritical(boolean autoCritical) {
        this.autoCritical = autoCritical;
    }

    public boolean isAutoMiss() {
        return autoMiss;
    }

    public void setAutoMiss(boolean autoMiss) {
        this.autoMiss = autoMiss;
    }

    public int getDamageMultiplier() {
        return damageMultiplier;
    }

    public void setDamageMultiplier(int damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    public String getDiceFormula() {
        return diceFormula;
    }

    public void setDiceFormula(String diceFormula) {
        this.diceFormula = diceFormula;
    }

    public String getDamageType() {
        return damageType;
    }

    public void setDamageType(String damageType) {
        this.damageType = damageType;
    }

    public List<String> getRuleSources() {
        return ruleSources;
    }

    public void setRuleSources(List<String> ruleSources) {
        this.ruleSources = ruleSources;
    }

    public RulesModifier addRuleSource(String source) {
        this.ruleSources.add(source);
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Combines this modifier with another, accumulating bonuses and effects.
     * <p>
     * If both modifiers set advantage/disadvantage, they cancel out to normal.
     * </p>
     *
     * @param other the other modifier to combine with
     * @return this modifier (modified in place)
     */
    public RulesModifier combineWith(RulesModifier other) {
        this.attackBonus += other.attackBonus;
        this.defenseBonus += other.defenseBonus;
        this.damageModifier += other.damageModifier;

        // Advantage/disadvantage logic
        if (other.advantage && !this.disadvantage) {
            this.advantage = true;
        } else if (other.disadvantage && !this.advantage) {
            this.disadvantage = true;
        }
        // If other has both advantage and disadvantage, they cancel — no change needed

        this.autoCritical = this.autoCritical || other.autoCritical;
        this.autoMiss = this.autoMiss || other.autoMiss;

        if (other.damageMultiplier != 1) {
            this.damageMultiplier *= other.damageMultiplier;
        }

        if (other.diceFormula != null && !other.diceFormula.isEmpty()) {
            this.diceFormula = other.diceFormula;
        }
        if (other.damageType != null && !other.damageType.isEmpty()) {
            this.damageType = other.damageType;
        }

        this.ruleSources.addAll(other.ruleSources);

        // Build combined description
        StringBuilder desc = new StringBuilder();
        if (this.description != null) desc.append(this.description);
        if (other.description != null) {
            if (desc.length() > 0) desc.append("; ");
            desc.append(other.description);
        }
        this.description = desc.toString();
        return this;
    }

    /**
     * Returns true if this modifier has no effects (all defaults).
     */
    public boolean isEmpty() {
        return attackBonus == 0
                && defenseBonus == 0
                && damageModifier == 0
                && !advantage
                && !disadvantage
                && !autoCritical
                && !autoMiss
                && damageMultiplier == 1
                && (diceFormula == null || diceFormula.isEmpty())
                && (damageType == null || damageType.isEmpty())
                && ruleSources.isEmpty();
    }

    /**
     * Creates a no-op modifier (all defaults).
     */
    public static RulesModifier none() {
        return new RulesModifier();
    }

    /**
     * Creates a modifier with a simple attack bonus.
     */
    public static RulesModifier attackBonus(int bonus, String source) {
        RulesModifier m = new RulesModifier();
        m.attackBonus = bonus;
        m.ruleSources.add(source);
        m.description = "Attack bonus: " + (bonus >= 0 ? "+" : "") + bonus + " (" + source + ")";
        return m;
    }

    /**
     * Creates a modifier with a simple defense bonus.
     */
    public static RulesModifier defenseBonus(int bonus, String source) {
        RulesModifier m = new RulesModifier();
        m.defenseBonus = bonus;
        m.ruleSources.add(source);
        m.description = "Defense bonus: " + (bonus >= 0 ? "+" : "") + bonus + " (" + source + ")";
        return m;
    }

    @Override
    public String toString() {
        return "RulesModifier{atk=" + (attackBonus >= 0 ? "+" : "") + attackBonus
                + ", def=" + (defenseBonus >= 0 ? "+" : "") + defenseBonus
                + ", dmg=" + (damageModifier >= 0 ? "+" : "") + damageModifier
                + ", mult=" + damageMultiplier
                + (advantage ? ", ADV" : "")
                + (disadvantage ? ", DIS" : "")
                + (autoCritical ? ", CRIT" : "")
                + (autoMiss ? ", MISS" : "")
                + (description != null ? ", desc='" + description + "'" : "")
                + "}";
    }
}
