package com.yisy.modules.doramcu.gamecore;
import com.yisy.modules.doramcu.model.players.Player;

import java.util.List;
import java.util.Map;

/**
 * Shared interface for combat resolution across all game modules.
 * <p>
 * Different modules implement different combat systems (D&amp;D tactical, Traveller firearms,
 * Earth2026 modern combat), but this interface provides a unified API for combat encounters.
 * </p>
 */
public interface CombatService {

    /**
     * Create a new combat encounter.
     *
     * @param encounterName name of the encounter
     * @return the created combat session ID
     */
    Long createCombat(String encounterName);

    /**
     * Get a combat session by ID.
     *
     * @param combatId the combat session ID
     * @return the combat info or null if not found
     */
    CombatInfo getCombat(Long combatId);

    /**
     * Add a combatant to a combat session.
     *
     * @param combatId the combat session ID
     * @param name name of the combatant
     * @param armorClass armor class or equivalent defense value
     * @param maxHp maximum hit points
     * @param isCharacter true if this is a player character
     * @return the combatant info
     */
    CombatantInfo addCombatant(Long combatId, String name, int armorClass, int maxHp, boolean isCharacter);

    /**
     * Roll initiative for all combatants.
     *
     * @param combatId the combat session ID
     * @param dexModifiers map of combatant names to dexterity modifiers
     * @return the updated combat info
     */
    CombatInfo rollInitiative(Long combatId, Map<String, Integer> dexModifiers);

    /**
     * Make an attack in combat.
     *
     * @param combatId the combat session ID
     * @param request the attack request
     * @return the attack result
     */
    AttackResult makeAttack(Long combatId, AttackRequest request);

    /**
     * Heal a combatant.
     *
     * @param combatId the combat session ID
     * @param name name of the combatant
     * @param amount amount to heal
     * @return the updated combatant info
     */
    CombatantInfo healCombatant(Long combatId, String name, int amount);

    /**
     * End a combat session.
     *
     * @param combatId the combat session ID
     * @param status the final status (e.g., "VICTORY", "DEFEAT", "RETREAT")
     * @return the final combat info
     */
    CombatInfo endCombat(Long combatId, String status);

    /**
     * Request to make an attack.
     */
    static class AttackRequest {
        private String attackerName;
        private String targetName;
        private int attackBonus;
        private String damageFormula;
        private String damageType;
        private boolean advantage;
        private boolean disadvantage;

        public String getAttackerName() { return attackerName; }
        public void setAttackerName(String attackerName) { this.attackerName = attackerName; }
        public String getTargetName() { return targetName; }
        public void setTargetName(String targetName) { this.targetName = targetName; }
        public int getAttackBonus() { return attackBonus; }
        public void setAttackBonus(int attackBonus) { this.attackBonus = attackBonus; }
        public String getDamageFormula() { return damageFormula; }
        public void setDamageFormula(String damageFormula) { this.damageFormula = damageFormula; }
        public String getDamageType() { return damageType; }
        public void setDamageType(String damageType) { this.damageType = damageType; }
        public boolean isAdvantage() { return advantage; }
        public void setAdvantage(boolean advantage) { this.advantage = advantage; }
        public boolean isDisadvantage() { return disadvantage; }
        public void setDisadvantage(boolean disadvantage) { this.disadvantage = disadvantage; }
    }

    /**
     * Result of an attack.
     */
    static class AttackResult {
        private String attackerName;
        private String targetName;
        private int attackRoll;
        private int attackTotal;
        private boolean hit;
        private boolean criticalHit;
        private boolean criticalFail;
        private int damageDealt;
        private String damageType;
        private int remainingHp;
        private String message;

        public String getAttackerName() { return attackerName; }
        public void setAttackerName(String attackerName) { this.attackerName = attackerName; }
        public String getTargetName() { return targetName; }
        public void setTargetName(String targetName) { this.targetName = targetName; }
        public int getAttackRoll() { return attackRoll; }
        public void setAttackRoll(int attackRoll) { this.attackRoll = attackRoll; }
        public int getAttackTotal() { return attackTotal; }
        public void setAttackTotal(int attackTotal) { this.attackTotal = attackTotal; }
        public boolean isHit() { return hit; }
        public void setHit(boolean hit) { this.hit = hit; }
        public boolean isCriticalHit() { return criticalHit; }
        public void setCriticalHit(boolean criticalHit) { this.criticalHit = criticalHit; }
        public boolean isCriticalFail() { return criticalFail; }
        public void setCriticalFail(boolean criticalFail) { this.criticalFail = criticalFail; }
        public int getDamageDealt() { return damageDealt; }
        public void setDamageDealt(int damageDealt) { this.damageDealt = damageDealt; }
        public String getDamageType() { return damageType; }
        public void setDamageType(String damageType) { this.damageType = damageType; }
        public int getRemainingHp() { return remainingHp; }
        public void setRemainingHp(int remainingHp) { this.remainingHp = remainingHp; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * Information about a combat session.
     */
    static class CombatInfo {
        private Long id;
        private String encounterName;
        private String status;
        private int currentRound;
        private List<CombatantInfo> combatants;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getEncounterName() { return encounterName; }
        public void setEncounterName(String encounterName) { this.encounterName = encounterName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getCurrentRound() { return currentRound; }
        public void setCurrentRound(int currentRound) { this.currentRound = currentRound; }
        public List<CombatantInfo> getCombatants() { return combatants; }
        public void setCombatants(List<CombatantInfo> combatants) { this.combatants = combatants; }
    }

    /**
     * Information about a combatant.
     */
    static class CombatantInfo {
        private String name;
        private int armorClass;
        private int currentHp;
        private int maxHp;
        private int initiative;
        private int turnOrder;
        private boolean isCharacter;
        private boolean alive;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getArmorClass() { return armorClass; }
        public void setArmorClass(int armorClass) { this.armorClass = armorClass; }
        public int getCurrentHp() { return currentHp; }
        public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }
        public int getMaxHp() { return maxHp; }
        public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
        public int getInitiative() { return initiative; }
        public void setInitiative(int initiative) { this.initiative = initiative; }
        public int getTurnOrder() { return turnOrder; }
        public void setTurnOrder(int turnOrder) { this.turnOrder = turnOrder; }
        public boolean isCharacter() { return isCharacter; }
        public void setCharacter(boolean character) { this.isCharacter = character; }
        public boolean isAlive() { return alive; }
        public void setAlive(boolean alive) { this.alive = alive; }
    }
}
