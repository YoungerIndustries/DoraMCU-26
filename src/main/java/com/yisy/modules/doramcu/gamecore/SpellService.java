package com.yisy.modules.doramcu.gamecore;

import java.util.List;

/**
 * Shared interface for spell/supernatural abilities across all game modules.
 * <p>
 * D&amp;D provides spells, Traveller provides psionics, and Earth2026 can use
 * this for supernatural abilities when the supernaturalEnabled flag is set.
 * </p>
 */
public interface SpellService {

    /**
     * List all available spells.
     *
     * @return list of spells
     */
    List<Spell> listSpells();

    /**
     * Get a spell by ID.
     *
     * @param spellId the spell ID
     * @return the spell or null if not found
     */
    Spell getSpell(Long spellId);

    /**
     * List spells by level.
     *
     * @param level the spell level (0 = cantrip)
     * @return list of spells
     */
    List<Spell> listSpellsByLevel(int level);

    /**
     * Cast a spell.
     *
     * @param characterId the character ID
     * @param spellId the spell ID
     * @param targetId optional target ID
     * @return the cast result
     */
    SpellCastResult castSpell(Long characterId, Long spellId, Long targetId);

    /**
     * A spell.
     */
    static class Spell {
        private Long id;
        private String name;
        private int level;
        private String school;
        private String castingTime;
        private String range;
        private String components;
        private String duration;
        private String description;
        private boolean requiresConcentration;
        private boolean ritual;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public String getSchool() { return school; }
        public void setSchool(String school) { this.school = school; }
        public String getCastingTime() { return castingTime; }
        public void setCastingTime(String castingTime) { this.castingTime = castingTime; }
        public String getRange() { return range; }
        public void setRange(String range) { this.range = range; }
        public String getComponents() { return components; }
        public void setComponents(String components) { this.components = components; }
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public boolean isRequiresConcentration() { return requiresConcentration; }
        public void setRequiresConcentration(boolean requiresConcentration) { this.requiresConcentration = requiresConcentration; }
        public boolean isRitual() { return ritual; }
        public void setRitual(boolean ritual) { this.ritual = ritual; }
    }

    /**
     * Result of casting a spell.
     */
    static class SpellCastResult {
        private final Long spellId;
        private final String spellName;
        private final boolean success;
        private final String message;
        private final int damageDealt;

        public SpellCastResult(Long spellId, String spellName, boolean success, String message, int damageDealt) {
            this.spellId = spellId;
            this.spellName = spellName;
            this.success = success;
            this.message = message;
            this.damageDealt = damageDealt;
        }

        public Long getSpellId() { return spellId; }
        public String getSpellName() { return spellName; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getDamageDealt() { return damageDealt; }
    }
}
