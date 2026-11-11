package com.yisy.modules.doramcu.shared.model;

/**
 * Enum identifying which game system a rule entry belongs to.
 * <p>
 * Used by the unified {@link RulesEntry} and {@link RulesTable} entities
 * to distinguish rules from different game systems in a single shared database.
 * </p>
 */
public enum GameSystem {
    /** Classic Traveller rules (2D6-based). */
    TRAVELLER,

    /** Captain's Log Solo RPG rules (2d20-based). */
    CAPTAINS_LOG,

    /** Universal rules applicable to all game systems. */
    UNIVERSAL,

    /** DoraMCU 4X game engine rules. */
    DORAMCU_4X,

    /** D&D 5th Edition rules. */
    DND_5E,

    /** Subsector RPG rules. */
    SUBSECTOR_RPG,

    /** Theme-based game rules. */
    THEME_BASED,

    /** Babylon 5 universe rules. */
    BABYLON_5,

    /** Star Frontiers Battle rules. */
    SFB
}
