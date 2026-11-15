package com.yisy.modules.doramcu.shared.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds combat context information used by the rules-aware combat adapter.
 * <p>
 * When a combat action is about to be resolved, the adapter consults the rules
 * database using this context to determine applicable modifiers (cover bonuses,
 * terrain effects, special abilities, faction advantages, etc.).
 * </p>
 * <p>
 * Example: a Star Wars attack from behind cover on the Hoth battlefield would
 * set terrainType="SNOW", coverType="HALF", and gameSystem=STAR_WARS, allowing
 * the adapter to look up the relevant cover bonus rules and terrain penalties.
 * </p>
 */
public class RulesContext {

    /** The game system governing this combat (e.g., STAR_WARS, BABYLON_5, DND_5E). */
    private GameSystem gameSystem;

    /** The terrain type affecting combat (e.g., "URBAN", "FOREST", "SNOW", "DESERT", "SPACE"). */
    private String terrainType;

    /** The cover type of the defender (e.g., "NONE", "HALF", "TOTAL", "CONCEALMENT"). */
    private String coverType;

    /** The attacker's faction (e.g., "EMPIRE", "REBELS", "CENTRAL_COMMAND"). */
    private String attackerFaction;

    /** The defender's faction. */
    private String defenderFaction;

    /** The attacker's unit type or class (e.g., "TROOPER", "JEDI", "RANGER"). */
    private String attackerType;

    /** The defender's unit type or class. */
    private String defenderType;

    /** The weapon or attack type (e.g., "BLASTER", "LIGHTSABER", "FIREARM", "MELEE"). */
    private String weaponType;

    /** The damage type (e.g., "ENERGY", "KINETIC", "PIERCING", "SLASHING"). */
    private String damageType;

    /** Special conditions affecting the combat (e.g., "SURPRISE", "FLANKING", "HIGH_GROUND"). */
    private List<String> specialConditions = new ArrayList<>();

    /** The scenario or battle name (e.g., "HOTH", "YAVIN", "ZHEN"). */
    private String scenarioName;

    // --- Getters and Setters ---

    public GameSystem getGameSystem() {
        return gameSystem;
    }

    public void setGameSystem(GameSystem gameSystem) {
        this.gameSystem = gameSystem;
    }

    public String getTerrainType() {
        return terrainType;
    }

    public void setTerrainType(String terrainType) {
        this.terrainType = terrainType;
    }

    public String getCoverType() {
        return coverType;
    }

    public void setCoverType(String coverType) {
        this.coverType = coverType;
    }

    public String getAttackerFaction() {
        return attackerFaction;
    }

    public void setAttackerFaction(String attackerFaction) {
        this.attackerFaction = attackerFaction;
    }

    public String getDefenderFaction() {
        return defenderFaction;
    }

    public void setDefenderFaction(String defenderFaction) {
        this.defenderFaction = defenderFaction;
    }

    public String getAttackerType() {
        return attackerType;
    }

    public void setAttackerType(String attackerType) {
        this.attackerType = attackerType;
    }

    public String getDefenderType() {
        return defenderType;
    }

    public void setDefenderType(String defenderType) {
        this.defenderType = defenderType;
    }

    public String getWeaponType() {
        return weaponType;
    }

    public void setWeaponType(String weaponType) {
        this.weaponType = weaponType;
    }

    public String getDamageType() {
        return damageType;
    }

    public void setDamageType(String damageType) {
        this.damageType = damageType;
    }

    public List<String> getSpecialConditions() {
        return specialConditions;
    }

    public void setSpecialConditions(List<String> specialConditions) {
        this.specialConditions = specialConditions;
    }

    public RulesContext addSpecialCondition(String condition) {
        this.specialConditions.add(condition);
        return this;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    /**
     * Returns a descriptive string summarizing the context for logging.
     */
    public String describe() {
        StringBuilder sb = new StringBuilder();
        if (gameSystem != null) sb.append(gameSystem).append(" ");
        if (terrainType != null) sb.append("terrain=").append(terrainType).append(" ");
        if (coverType != null) sb.append("cover=").append(coverType).append(" ");
        if (attackerFaction != null) sb.append(attackerFaction).append(" vs ");
        if (defenderFaction != null) sb.append(defenderFaction).append(" ");
        if (weaponType != null) sb.append("weapon=").append(weaponType).append(" ");
        if (!specialConditions.isEmpty()) sb.append("conditions=").append(specialConditions);
        return sb.toString().trim();
    }
}
