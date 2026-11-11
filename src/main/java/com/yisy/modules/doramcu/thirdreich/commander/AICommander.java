package com.yisy.modules.doramcu.thirdreich.commander;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an AI commander in the 3R4 ground combat engine.
 * <p>
 * A commander leads ground units during battle, with leadership stats and traits
 * that modify combat resolution. Commanders can be historical profiles (e.g., Rommel)
 * or generated from game characters.
 * </p>
 */
public class AICommander {

    public enum TacticalPreference {
        /** Aggressive: favors attack, accepts higher risk */
        AGGRESSIVE,
        /** Defensive: favors holding positions, minimizes losses */
        DEFENSIVE,
        /** Balanced: adapts to situation */
        BALANCED
    }

    private final String id;
    private final String name;
    private int leadership;
    private final List<CommanderTrait> traits = new ArrayList<>();
    private TacticalPreference tacticalPreference;
    private String sourceGame;
    private boolean isHistoricalProfile;

    /**
     * Link to the shared AiCharacter for experience tracking and hiring.
     * Null for standalone commanders; set when integrated with the shared system.
     */
    private Long linkedAiCharacterId;

    /**
     * Creates a new AI commander.
     *
     * @param id the unique identifier
     * @param name the commander's name
     */
    public AICommander(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Commander ID must not be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Commander name must not be null or blank");
        }
        this.id = id;
        this.name = name;
        this.leadership = 5;
        this.tacticalPreference = TacticalPreference.BALANCED;
        this.isHistoricalProfile = false;
    }

    // ---- Accessors ----

    /** Returns the unique identifier. */
    public String getId() {
        return id;
    }

    /** Returns the commander's name. */
    public String getName() {
        return name;
    }

    /** Returns the leadership rating (1-10). */
    public int getLeadership() {
        return leadership;
    }

    /** Sets the leadership rating. */
    public void setLeadership(int leadership) {
        this.leadership = Math.max(1, Math.min(10, leadership));
    }

    /** Returns the list of commander traits. */
    public List<CommanderTrait> getTraits() {
        return traits;
    }

    /** Adds a trait to this commander. */
    public void addTrait(CommanderTrait trait) {
        if (trait != null && !traits.contains(trait)) {
            traits.add(trait);
        }
    }

    /** Returns the tactical preference. */
    public TacticalPreference getTacticalPreference() {
        return tacticalPreference;
    }

    /** Sets the tactical preference. */
    public void setTacticalPreference(TacticalPreference tacticalPreference) {
        this.tacticalPreference = tacticalPreference;
    }

    /** Returns the source game system, or null if native 3R4. */
    public String getSourceGame() {
        return sourceGame;
    }

    /** Sets the source game system. */
    public void setSourceGame(String sourceGame) {
        this.sourceGame = sourceGame;
    }

    /** Returns true if this is a predefined historical profile. */
    public boolean isHistoricalProfile() {
        return isHistoricalProfile;
    }

    /** Sets whether this is a historical profile. */
    public void setHistoricalProfile(boolean historicalProfile) {
        isHistoricalProfile = historicalProfile;
    }

    /** Returns the linked shared AiCharacter ID, or null if standalone. */
    public Long getLinkedAiCharacterId() {
        return linkedAiCharacterId;
    }

    /** Sets the linked shared AiCharacter ID for experience tracking. */
    public void setLinkedAiCharacterId(Long linkedAiCharacterId) {
        this.linkedAiCharacterId = linkedAiCharacterId;
    }

    // ---- Combat Modifiers ----

    /**
     * Returns the total attack bonus from all traits.
     */
    public int getTotalAttackBonus() {
        int bonus = traits.stream().mapToInt(CommanderTrait::getAttackBonus).sum();
        if (tacticalPreference == TacticalPreference.AGGRESSIVE) {
            bonus += 1;
        }
        return bonus;
    }

    /**
     * Returns the total defense bonus from all traits.
     */
    public int getTotalDefenseBonus() {
        int bonus = traits.stream().mapToInt(CommanderTrait::getDefenseBonus).sum();
        if (tacticalPreference == TacticalPreference.DEFENSIVE) {
            bonus += 1;
        }
        return bonus;
    }

    /**
     * Returns the leadership modifier for CRT resolution.
     * Leadership 1-4: -1, 5-6: 0, 7-8: +1, 9-10: +2
     */
    public int getLeadershipModifier() {
        if (leadership >= 9) return 2;
        if (leadership >= 7) return 1;
        if (leadership >= 5) return 0;
        return -1;
    }

    @Override
    public String toString() {
        return String.format("Commander{id='%s', name='%s', leadership=%d, traits=%s, preference=%s}",
                id, name, leadership, traits, tacticalPreference);
    }
}
