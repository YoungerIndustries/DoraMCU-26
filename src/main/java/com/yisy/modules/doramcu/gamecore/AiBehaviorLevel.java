package com.yisy.modules.doramcu.gamecore;

/**
 * AI behavior level for monsters and NPCs.
 * <p>
 * Determines which AI subsystem is used for a creature, scaled by intelligence.
 * A dog doesn't need full AI personality like a human villain.
 * </p>
 * <p>
 * Intelligence-based default mapping:
 * <ul>
 *   <li>Intel 1-2 → NONE (animals, mindless creatures)</li>
 *   <li>Intel 3-5 → BASIC (simple beasts, reactive only)</li>
 *   <li>Intel 6-10 → SIMPLE (goblins, orcs, basic decision-making)</li>
 *   <li>Intel 11-15 → MODERATE (tactical — flanking, targeting)</li>
 *   <li>Intel 16-20 → ADVANCED (strategic — coordinating, using environment)</li>
 *   <li>Intel 21-30 → FULL (full AiCharacter with AiPersonality)</li>
 * </ul>
 * </p>
 */
public enum AiBehaviorLevel {
    /**
     * No AI — static entity, no autonomous behavior.
     * Used for mindless creatures, objects, and simple animals.
     */
    NONE,

    /**
     * Reactive only — flee on low HP, attack when provoked.
     * Used for simple beasts and basic creatures.
     */
    BASIC,

    /**
     * Basic decision-making — patrol, guard, follow simple orders.
     * Used for goblins, orcs, and creatures with basic intelligence.
     */
    SIMPLE,

    /**
     * Tactical AI — flanking, targeting weak enemies, using cover.
     * Used for trained soldiers and intelligent creatures.
     */
    MODERATE,

    /**
     * Strategic AI — coordinating with allies, using environment, planning.
     * Used for commanders, archmages, and highly intelligent creatures.
     */
    ADVANCED,

    /**
     * Full AI — complete AiCharacter with AiPersonality.
     * Planning, dialogue, complex decisions, adaptive behavior.
     * Used for villains, leaders, and creatures with exceptional intelligence.
     */
    FULL;

    /**
     * Compute the default AI behavior level based on an intelligence score.
     *
     * @param intel the intelligence score (1-30)
     * @return the appropriate AiBehaviorLevel
     */
    public static AiBehaviorLevel fromIntelligence(int intel) {
        if (intel <= 2) return NONE;
        if (intel <= 5) return BASIC;
        if (intel <= 10) return SIMPLE;
        if (intel <= 15) return MODERATE;
        if (intel <= 20) return ADVANCED;
        return FULL;
    }
}
