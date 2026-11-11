package com.yisy.modules.doramcu.dnd5e.dto;

import java.util.List;

/**
 * Response from casting a spell.
 */
public class SpellCastResponse {

    private String spellName;
    private int spellLevel;
    private int slotLevelUsed;
    private boolean concentration;
    private boolean ritual;
    private int damageRoll;
    private int healingRoll;
    private String damageFormula;
    private String healingFormula;
    private int slotsRemaining;
    private String description;

    public SpellCastResponse() {}

    public String getSpellName() { return spellName; }
    public void setSpellName(String spellName) { this.spellName = spellName; }

    public int getSpellLevel() { return spellLevel; }
    public void setSpellLevel(int spellLevel) { this.spellLevel = spellLevel; }

    public int getSlotLevelUsed() { return slotLevelUsed; }
    public void setSlotLevelUsed(int slotLevelUsed) { this.slotLevelUsed = slotLevelUsed; }

    public boolean isConcentration() { return concentration; }
    public void setConcentration(boolean concentration) { this.concentration = concentration; }

    public boolean isRitual() { return ritual; }
    public void setRitual(boolean ritual) { this.ritual = ritual; }

    public int getDamageRoll() { return damageRoll; }
    public void setDamageRoll(int damageRoll) { this.damageRoll = damageRoll; }

    public int getHealingRoll() { return healingRoll; }
    public void setHealingRoll(int healingRoll) { this.healingRoll = healingRoll; }

    public String getDamageFormula() { return damageFormula; }
    public void setDamageFormula(String damageFormula) { this.damageFormula = damageFormula; }

    public String getHealingFormula() { return healingFormula; }
    public void setHealingFormula(String healingFormula) { this.healingFormula = healingFormula; }

    public int getSlotsRemaining() { return slotsRemaining; }
    public void setSlotsRemaining(int slotsRemaining) { this.slotsRemaining = slotsRemaining; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
