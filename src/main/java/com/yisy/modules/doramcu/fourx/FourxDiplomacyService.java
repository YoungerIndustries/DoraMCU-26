package com.yisy.modules.doramcu.fourx;

import java.util.List;
import java.util.Map;

/**
 * Shared interface for the Diplomacy 4X pillar.
 * <p>
 * Implementing services manage faction relations, treaties, war/peace declarations,
 * and diplomatic events. Each game module (B5, SFB, Star Trek 4X) provides its own
 * implementation that maps to its persistent entities.
 * </p>
 */
public interface FourxDiplomacyService {

    /**
     * Get or create a diplomatic relation between two factions.
     *
     * @param faction1Id the first faction ID
     * @param faction2Id the second faction ID
     * @return the diplomatic relation
     */
    FourxDiplomaticRelation getOrCreateRelation(String faction1Id, String faction2Id);

    /**
     * Modify the relation score between two factions.
     *
     * @param faction1Id the first faction ID
     * @param faction2Id the second faction ID
     * @param delta      the change in relation score
     * @return updated relation
     */
    FourxDiplomaticRelation modifyRelation(String faction1Id, String faction2Id, int delta);

    /**
     * Declare war on another faction.
     *
     * @param attackerId the attacker faction ID
     * @param defenderId the defender faction ID
     * @return updated relation
     */
    FourxDiplomaticRelation declareWar(String attackerId, String defenderId);

    /**
     * Establish peace with another faction.
     *
     * @param faction1Id the first faction ID
     * @param faction2Id the second faction ID
     * @return updated relation
     */
    FourxDiplomaticRelation establishPeace(String faction1Id, String faction2Id);

    /**
     * Add a treaty between two factions.
     *
     * @param faction1Id the first faction ID
     * @param faction2Id the second faction ID
     * @param treatyType the type of treaty (e.g., "TRADE", "MUTUAL_DEFENSE", "NON_AGGRESSION")
     * @return updated relation
     */
    FourxDiplomaticRelation addTreaty(String faction1Id, String faction2Id, String treatyType);

    /**
     * Remove a treaty between two factions.
     *
     * @param faction1Id the first faction ID
     * @param faction2Id the second faction ID
     * @param treatyType the type of treaty to remove
     * @return updated relation
     */
    FourxDiplomaticRelation removeTreaty(String faction1Id, String faction2Id, String treatyType);

    /**
     * Get all relations for a faction.
     *
     * @param factionId the faction ID
     * @return list of relations
     */
    List<FourxDiplomaticRelation> getRelations(String factionId);

    /**
     * Check if two factions are at war.
     *
     * @param faction1Id the first faction ID
     * @param faction2Id the second faction ID
     * @return true if at war
     */
    boolean isAtWar(String faction1Id, String faction2Id);

    /**
     * Get all factions in the game.
     *
     * @return list of all factions
     */
    List<FourxFaction> getAllFactions();

    /**
     * Get a faction by ID.
     *
     * @param factionId the faction ID
     * @return the faction, or null if not found
     */
    FourxFaction getFaction(String factionId);

    /**
     * Generate a random diplomatic event for a faction.
     *
     * @param factionId the faction ID
     * @return event details as a map
     */
    Map<String, Object> generateDiplomaticEvent(String factionId);
}
