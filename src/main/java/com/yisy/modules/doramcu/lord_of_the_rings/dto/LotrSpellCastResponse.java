package com.yisy.modules.doramcu.lord_of_the_rings.dto;

/**
 * Response for spell casting in LOTR.
 */
public class LotrSpellCastResponse {

    private String characterName;
    private String spellName;
    private int spellLevel;
    private boolean success;
    private int spellCheck;
    private int dc;
    private int saveDc;
    private int saveRoll;
    private boolean saveSuccess;
    private int damage;
    private String damageType;
    private String description;

    public LotrSpellCastResponse() {
    }

    public LotrSpellCastResponse(String characterName, String spellName, int saveDc, int saveRoll,
                                 boolean saveSuccess, int damage, String damageType, String description) {
        this.characterName = characterName;
        this.spellName = spellName;
        this.saveDc = saveDc;
        this.saveRoll = saveRoll;
        this.saveSuccess = saveSuccess;
        this.damage = damage;
        this.damageType = damageType;
        this.description = description;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public String getSpellName() {
        return spellName;
    }

    public void setSpellName(String spellName) {
        this.spellName = spellName;
    }

    public int getSaveDc() {
        return saveDc;
    }

    public void setSaveDc(int saveDc) {
        this.saveDc = saveDc;
    }

    public int getSaveRoll() {
        return saveRoll;
    }

    public void setSaveRoll(int saveRoll) {
        this.saveRoll = saveRoll;
    }

    public boolean isSaveSuccess() {
        return saveSuccess;
    }

    public void setSaveSuccess(boolean saveSuccess) {
        this.saveSuccess = saveSuccess;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public String getDamageType() {
        return damageType;
    }

    public void setDamageType(String damageType) {
        this.damageType = damageType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSpellLevel() {
        return spellLevel;
    }

    public void setSpellLevel(int spellLevel) {
        this.spellLevel = spellLevel;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getSpellCheck() {
        return spellCheck;
    }

    public void setSpellCheck(int spellCheck) {
        this.spellCheck = spellCheck;
    }

    public int getDc() {
        return dc;
    }

    public void setDc(int dc) {
        this.dc = dc;
    }
}
