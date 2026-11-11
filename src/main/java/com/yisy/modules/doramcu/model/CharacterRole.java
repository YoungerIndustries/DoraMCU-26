package com.yisy.modules.doramcu.model;

/**
 * Defines the relationship between a player and a character.
 */
public enum CharacterRole {

    /** The player's primary RPG avatar (one per game type). */
    COMMANDER,

    /** AI crew member that travels with the player through wormholes. */
    CREW,

    /** Player-owned character, not currently on active crew. */
    OWNED,

    /** AI character the player has interacted with (NPC, enemy, ally). */
    ENCOUNTERED
}
