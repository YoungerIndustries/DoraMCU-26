package com.yisy.modules.doramcu.unified;

import java.util.List;
import java.util.Map;

/**
 * Unified combat engine interface that normalizes combat mechanics
 * across all game modules.
 * <p>
 * Each game module provides a concrete implementation that knows its
 * combat rules:
 * <ul>
 *   <li>Traveller: firearms/melee combat with 2D6</li>
 *   <li>SFB: ship/ground combat with energy allocation</li>
 *   <li>D&D 5e: initiative, rounds, actions</li>
 *   <li>Earth2026: modern combat</li>
 *   <li>Captain's Log: 2d20 combat</li>
 * </ul>
 * </p>
 */
public interface CombatEngine {

    /**
     * Returns the game type this combat engine handles.
     *
     * @return the game type
     */
    GameType getGameType();

    /**
     * Initiates a combat encounter between participants.
     *
     * @param participants the combatants
     * @param environment  the combat environment (terrain, weather, etc.)
     * @return the combat session
     */
    CombatSession initiateCombat(List<Combatant> participants, Map<String, Object> environment);

    /**
     * Executes a single round of combat.
     *
     * @param session the combat session
     * @return the round result
     */
    CombatRoundResult executeRound(CombatSession session);

    /**
     * Resolves a single attack.
     *
     * @param attacker the attacker
     * @param target   the target
     * @param weapon   the weapon used
     * @return the attack result
     */
    AttackResult resolveAttack(Combatant attacker, Combatant target, Map<String, Object> weapon);

    /**
     * Applies damage to a combatant.
     *
     * @param target the target
     * @param damage the damage amount
     * @param type   the damage type
     * @return the damage result
     */
    DamageResult applyDamage(Combatant target, int damage, DamageType type);

    /**
     * Checks if the combat has ended and determines the winner.
     *
     * @param session the combat session
     * @return the victory result
     */
    VictoryResult checkVictory(CombatSession session);

    /**
     * Types of damage in the unified system.
     */
    enum DamageType {
        BALLISTIC, ENERGY, PLASMA, MELEE, EXPLOSIVE, RADIATION, PSIONIC, MAGIC, ACID, FIRE, COLD
    }

    /**
     * A combatant in a combat encounter.
     */
    interface Combatant {
        String getId();
        String getName();
        int getHitPoints();
        int getMaxHitPoints();
        int getArmorClass();
        int getAttackBonus();
        int getDefenseBonus();
        boolean isAlive();
        void setHitPoints(int hitPoints);
    }

    /**
     * A combat session that tracks the state of an encounter.
     */
    interface CombatSession {
        String getId();
        List<Combatant> getParticipants();
        int getCurrentRound();
        boolean isActive();
        Map<String, Object> getEnvironment();
    }

    /**
     * Result of a combat round.
     */
    interface CombatRoundResult {
        int getRoundNumber();
        List<AttackResult> getAttacks();
        String getDescription();
    }

    /**
     * Result of a single attack.
     */
    interface AttackResult {
        String getAttackerId();
        String getTargetId();
        boolean isHit();
        int getDamage();
        DamageType getDamageType();
        String getDescription();
    }

    /**
     * Result of applying damage.
     */
    interface DamageResult {
        String getTargetId();
        int getDamageDealt();
        int getHitPointsRemaining();
        boolean isAlive();
        String getDescription();
    }

    /**
     * Result of checking victory conditions.
     */
    interface VictoryResult {
        boolean isCombatEnded();
        String getWinnerId();
        VictoryType getVictoryType();
        String getDescription();

        enum VictoryType {
            VICTORY, DEFEAT, MUTUAL_DEFEAT, DRAW, RETREAT
        }
    }
}
