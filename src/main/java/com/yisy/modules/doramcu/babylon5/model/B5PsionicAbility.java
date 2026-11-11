package com.yisy.modules.doramcu.babylon5.model;

import java.util.*;

/**
 * A character's psionic profile for the Babylon 5 universe.
 * <p>
 * Psionic ability is detected during character creation using the IN (Intuition) score.
 * Characters with IN 8+ may have psionic potential. The detection roll determines
 * the level of psionic strength, from latent to master.
 * </p>
 * <p>
 * In the B5 universe, psionic ability is regulated by the Psi Corps. Unregistered
 * psions are considered dangerous and may be hunted by Psi Corps operatives.
 * Characters with psionic ability must decide whether to register with the Psi Corps
 * or operate underground.
 * </p>
 * <p>
 * Psionic powers cause fatigue when used. Each use adds fatigue points; when
 * fatigue equals or exceeds the character's IN score, they cannot use powers
 * until they rest (1 hour of rest removes 1 fatigue point).
 * </p>
 * <p>
 * Psi Corps training can enhance a character's psionic abilities over time,
 * unlocking new disciplines and increasing power levels.
 * </p>
 */
public class B5PsionicAbility {

    /**
     * Levels of psionic detection strength in the B5 universe.
     */
    public enum DetectionLevel {
        /**
         * No psionic ability detected — a "non-psion".
         */
        NONE(0, "None", "No psionic ability"),

        /**
         * Latent psionic ability — very weak, may develop later.
         * Common in children before Psi Corps screening.
         */
        LATENT(1, "Latent", "Very weak psionic potential"),

        /**
         * Weak psionic ability — basic powers available.
         * Typical of untrained psions or Psi Corps recruits.
         */
        WEAK(2, "Weak", "Basic psionic powers"),

        /**
         * Average psionic ability — reliable powers.
         * Typical of trained Psi Corps operatives.
         */
        AVERAGE(3, "Average", "Reliable psionic powers"),

        /**
         * Strong psionic ability — powerful effects.
         * Typical of senior Psi Corps operatives.
         */
        STRONG(4, "Strong", "Powerful psionic effects"),

        /**
         * Master psionic — rare and extremely powerful.
         * Only a handful of individuals in the B5 universe reach this level.
         * Examples: Lennier (Minbari psion), certain Vorlon/Shadow entities.
         */
        MASTER(5, "Master", "Extremely powerful psionic abilities");

        private final int level;
        private final String displayName;
        private final String description;

        DetectionLevel(int level, String displayName, String description) {
            this.level = level;
            this.displayName = displayName;
            this.description = description;
        }

        public int getLevel() {
            return level;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Psi Corps training ranks that enhance psionic abilities.
     */
    public enum PsiCorpsRank {
        /**
         * Not registered with the Psi Corps — untrained psion.
         */
        UNREGISTERED("Unregistered", 0),

        /**
         * Psi Corps recruit — basic training.
         */
        RECRUIT("Recruit", 1),

        /**
         * Psi Corps operative — field-ready.
         */
        OPERATIVE("Operative", 2),

        /**
         * Psi Corps senior operative — advanced training.
         */
        SENIOR_OPERATIVE("Senior Operative", 3),

        /**
         * Psi Corps commander — leadership role.
         */
        COMMANDER("Commander", 4),

        /**
         * Psi Corps director — highest rank.
         */
        DIRECTOR("Director", 5);

        private final String displayName;
        private final int trainingBonus;

        PsiCorpsRank(String displayName, int trainingBonus) {
            this.displayName = displayName;
            this.trainingBonus = trainingBonus;
        }

        public String getDisplayName() {
            return displayName;
        }

        /**
         * Returns the training bonus added to psionic checks.
         */
        public int getTrainingBonus() {
            return trainingBonus;
        }
    }

    private DetectionLevel detectionLevel;
    private PsiCorpsRank psiCorpsRank;
    private final Map<B5PsionicDiscipline, Integer> disciplineLevels;
    private int fatigue;
    private final int maxFatigue;
    private boolean registered;

    /**
     * Creates a new psionic ability profile.
     *
     * @param inScore the character's IN (Intuition) score, used for max fatigue
     */
    public B5PsionicAbility(int inScore) {
        this.detectionLevel = DetectionLevel.NONE;
        this.psiCorpsRank = PsiCorpsRank.UNREGISTERED;
        this.disciplineLevels = new EnumMap<>(B5PsionicDiscipline.class);
        this.fatigue = 0;
        this.maxFatigue = Math.max(inScore, 2);
        this.registered = false;
    }

    /**
     * Performs psionic detection for a character during character creation.
     * <p>
     * Roll 2D6 and compare against the character's IN score:
     * - Roll &lt;= IN: Psionic detected (level based on how much lower)
     * - Roll &gt; IN: No psionic ability
     * </p>
     * <p>
     * In the B5 universe, Psi Corps screening is mandatory for all citizens.
     * Those who test positive are required to join the Psi Corps.
     * </p>
     *
     * @param inScore the character's IN (Intuition) score
     * @param roll the 2D6 detection roll result
     * @return true if psionic ability was detected
     */
    public boolean detectPsionic(int inScore, int roll) {
        if (inScore < 8) {
            this.detectionLevel = DetectionLevel.NONE;
            return false;
        }
        if (roll <= inScore - 4) {
            this.detectionLevel = DetectionLevel.MASTER;
        } else if (roll <= inScore - 2) {
            this.detectionLevel = DetectionLevel.STRONG;
        } else if (roll <= inScore) {
            this.detectionLevel = DetectionLevel.AVERAGE;
        } else if (roll <= inScore + 2) {
            this.detectionLevel = DetectionLevel.WEAK;
        } else if (roll <= inScore + 4) {
            this.detectionLevel = DetectionLevel.LATENT;
        } else {
            this.detectionLevel = DetectionLevel.NONE;
        }
        return this.detectionLevel != DetectionLevel.NONE;
    }

    /**
     * Registers the character with the Psi Corps.
     * <p>
     * Once registered, the character receives training and can advance
     * through the Psi Corps ranks.
     * </p>
     *
     * @return true if registration was successful
     */
    public boolean registerWithPsiCorps() {
        if (detectionLevel == DetectionLevel.NONE) {
            return false;
        }
        this.registered = true;
        this.psiCorpsRank = PsiCorpsRank.RECRUIT;
        return true;
    }

    /**
     * Advances the character's Psi Corps rank.
     *
     * @return true if the rank was advanced
     */
    public boolean advancePsiCorpsRank() {
        if (!registered) {
            return false;
        }
        PsiCorpsRank[] ranks = PsiCorpsRank.values();
        int currentIndex = Arrays.asList(ranks).indexOf(psiCorpsRank);
        if (currentIndex < ranks.length - 1) {
            this.psiCorpsRank = ranks[currentIndex + 1];
            return true;
        }
        return false;
    }

    /**
     * Adds a psionic discipline to the character's abilities.
     *
     * @param discipline the discipline
     * @param level the proficiency level (1-5)
     */
    public void addDiscipline(B5PsionicDiscipline discipline, int level) {
        if (discipline == null) {
            throw new IllegalArgumentException("Discipline must not be null");
        }
        if (level < 1 || level > 5) {
            throw new IllegalArgumentException("Discipline level must be 1-5, got: " + level);
        }
        this.disciplineLevels.put(discipline, level);
    }

    /**
     * Returns the level of a specific discipline.
     *
     * @param discipline the discipline
     * @return the level (0 if not learned)
     */
    public int getDisciplineLevel(B5PsionicDiscipline discipline) {
        return disciplineLevels.getOrDefault(discipline, 0);
    }

    /**
     * Returns all learned disciplines with their levels.
     *
     * @return unmodifiable map of discipline to level
     */
    public Map<B5PsionicDiscipline, Integer> getDisciplines() {
        return Collections.unmodifiableMap(disciplineLevels);
    }

    /**
     * Returns the detection level of this character's psionic ability.
     */
    public DetectionLevel getDetectionLevel() {
        return detectionLevel;
    }

    /**
     * Sets the detection level (used when loading saved characters).
     */
    public void setDetectionLevel(DetectionLevel level) {
        this.detectionLevel = Objects.requireNonNull(level, "Level must not be null");
    }

    /**
     * Returns the Psi Corps rank of this character.
     */
    public PsiCorpsRank getPsiCorpsRank() {
        return psiCorpsRank;
    }

    /**
     * Sets the Psi Corps rank (used when loading saved characters).
     */
    public void setPsiCorpsRank(PsiCorpsRank rank) {
        this.psiCorpsRank = Objects.requireNonNull(rank, "Rank must not be null");
    }

    /**
     * Returns whether the character is registered with the Psi Corps.
     */
    public boolean isRegistered() {
        return registered;
    }

    /**
     * Returns the current fatigue points.
     */
    public int getFatigue() {
        return fatigue;
    }

    /**
     * Returns the maximum fatigue before powers are blocked.
     */
    public int getMaxFatigue() {
        return maxFatigue;
    }

    /**
     * Adds fatigue from using a psionic power.
     *
     * @param amount the fatigue points to add
     * @return true if the character can still use powers after this
     */
    public boolean addFatigue(int amount) {
        fatigue += amount;
        return fatigue < maxFatigue;
    }

    /**
     * Removes fatigue from resting.
     *
     * @param amount the fatigue points to remove
     */
    public void rest(int amount) {
        fatigue = Math.max(0, fatigue - amount);
    }

    /**
     * Checks if the character can currently use psionic powers.
     *
     * @return true if not fatigued and has detected ability
     */
    public boolean canUsePowers() {
        return detectionLevel != DetectionLevel.NONE && fatigue < maxFatigue;
    }

    /**
     * Performs a psionic ability check.
     * <p>
     * Roll 2D6 + discipline level + Psi Corps training bonus against a target number.
     * </p>
     *
     * @param discipline the discipline being used
     * @param roll the 2D6 roll result
     * @param targetNumber the target number to meet or exceed
     * @return true if the check succeeds
     */
    public boolean performPsionicCheck(B5PsionicDiscipline discipline, int roll, int targetNumber) {
        if (!canUsePowers()) {
            return false;
        }
        int disciplineLevel = getDisciplineLevel(discipline);
        int trainingBonus = psiCorpsRank.getTrainingBonus();
        int total = roll + disciplineLevel + trainingBonus;
        return total >= targetNumber;
    }

    /**
     * Returns a summary of the psionic ability.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Psi: ").append(detectionLevel.getDisplayName());
        if (registered) {
            sb.append(" | Psi Corps: ").append(psiCorpsRank.getDisplayName());
        } else if (detectionLevel != DetectionLevel.NONE) {
            sb.append(" | Unregistered");
        }
        if (!disciplineLevels.isEmpty()) {
            sb.append(" | Disciplines: ");
            boolean first = true;
            for (Map.Entry<B5PsionicDiscipline, Integer> entry : disciplineLevels.entrySet()) {
                if (!first) sb.append(", ");
                sb.append(entry.getKey().getDisplayName()).append("=").append(entry.getValue());
                first = false;
            }
        }
        sb.append(" | Fatigue: ").append(fatigue).append("/").append(maxFatigue);
        return sb.toString();
    }
}
