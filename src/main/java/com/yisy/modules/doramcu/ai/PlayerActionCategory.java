package com.yisy.modules.doramcu.ai;

/**
 * Categories of player actions tracked for behavioral personality analysis.
 * <p>
 * Each category maps to one or more personality axes in the DynamicAiPersonality model.
 * The PlayerActionAnalyzer uses these categories to accumulate behavioral evidence.
 * </p>
 */
public enum PlayerActionCategory {
    /** Combat engagement — maps to aggression axis. */
    COMBAT,

    /** Combat retreat — maps to riskTolerance (inverse) axis. */
    RETREAT,

    /** Fleet movement and scouting — maps to expansionDrive axis. */
    EXPANSION,

    /** Building ships — maps to aggression and economicFocus axes. */
    BUILD_SHIP,

    /** Building stations — maps to economicFocus axis. */
    BUILD_STATION,

    /** Upgrading stations — maps to scientificFocus axis. */
    RESEARCH,

    /** Fortifying positions — maps to diplomaticStance (defensive) axis. */
    DEFENSIVE,

    /** Waiting/passive actions — maps to riskTolerance (inverse) axis. */
    PASSIVE,

    /** Helping allies or sharing resources — maps to altruism axis. */
    ALTRUISTIC,

    /** Deceptive maneuvers or ambushes — maps to cunning axis. */
    DECEPTIVE,

    /** Persistent actions after losses — maps to determination axis. */
    PERSISTENT,

    /** Strategy changes in response to threats — maps to adaptability axis. */
    ADAPTIVE,

    /** Multi-front fleet coordination — maps to leadership axis. */
    LEADERSHIP,

    /** Attacking undefended targets — maps to honor axis. */
    HONOR
}
