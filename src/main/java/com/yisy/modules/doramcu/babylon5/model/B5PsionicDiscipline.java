package com.yisy.modules.doramcu.babylon5.model;

/**
 * The six psionic disciplines from the Babylon 5 universe, based on the Psi Corps.
 * <p>
 * In Babylon 5, psionic ability is a natural talent that can be trained and controlled
 * through the Psi Corps. Each discipline represents a category of psychic power.
 * Characters with psionic ability can develop one or more disciplines over time.
 * </p>
 * <p>
 * The Psi Corps was established by the Earth Alliance to train and control psionic
 * individuals. The six disciplines mirror the Traveller system but are themed
 * around the B5 universe's psionic lore.
 * </p>
 * <p>
 * Psionic ability is detected during character creation using the IN (Intuition) score.
 * Characters with IN 8+ may have psionic potential.
 * </p>
 */
public enum B5PsionicDiscipline {

    /**
     * Mind reading, mental attacks, illusion, empathy.
     * The most common psionic discipline, used by Psi Corps operatives for
     * interrogation, communication, and mental defense.
     * <p>
     * Uses the character's IN (Intuition) score for checks.
     * </p>
     */
    TELEPATHY("Telepathy", "Mind reading, mental attacks, illusion, empathy", 1),

    /**
     * Remote viewing, scrying, precognition.
     * Used by Psi Corps for surveillance, reconnaissance, and limited
     * foresight into future events.
     * <p>
     * Uses the character's IN (Intuition) score for checks.
     * </p>
     */
    CLAIRVOYANCE("Clairvoyance", "Remote viewing, scrying, precognition", 2),

    /**
     * Moving objects, force blasts, telekinetic manipulation.
     * The most physically powerful discipline, used for combat and
     * heavy lifting. Rare among natural psions.
     * <p>
     * Uses the character's IN (Intuition) score for checks.
     * </p>
     */
    TELEKINESIS("Telekinesis", "Moving objects, force blasts, manipulation", 3),

    /**
     * Detecting life, psionics, thoughts, emotions.
     * Used by Psi Corps for detection of hidden psions, surveillance,
     * and threat assessment.
     * <p>
     * Uses the character's IN (Intuition) score for checks.
     * </p>
     */
    AWARENESS("Awareness", "Detecting life, psionics, thoughts, emotions", 4),

    /**
     * Short-range movement, phasing through matter.
     * Extremely rare and dangerous. Used for infiltration and escape.
     * Can cause physical harm to the user if misused.
     * <p>
     * Uses the character's IN (Intuition) score for checks.
     * </p>
     */
    TELEPORTATION("Teleportation", "Short-range movement, phasing", 5),

    /**
     * Time manipulation, probability alteration, reality warping.
     * The rarest and most dangerous discipline. Only a handful of
     * individuals in the B5 universe have demonstrated these abilities.
     * Associated with Vorlon and Shadow psionic technology.
     * <p>
     * Uses the character's IN (Intuition) score for checks.
     * </p>
     */
    SPECIAL("Special", "Time manipulation, probability, reality warping", 6);

    private final String displayName;
    private final String description;
    private final int tableValue;

    B5PsionicDiscipline(String displayName, String description, int tableValue) {
        this.displayName = displayName;
        this.description = description;
        this.tableValue = tableValue;
    }

    /**
     * Returns the human-readable name of this discipline.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns a description of what this discipline can do.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the 2D6 table value for this discipline (used in psionic ability tables).
     */
    public int getTableValue() {
        return tableValue;
    }
}
