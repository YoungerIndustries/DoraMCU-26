package com.yisy.modules.doramcu.gamecore;

import java.util.List;

/**
 * Shared interface for character management across all game modules.
 */
public interface CharacterService {

    /**
     * Get a character by ID.
     *
     * @param characterId the character ID
     * @return the character info or null if not found
     */
    CharacterInfo getCharacter(Long characterId);

    /**
     * List all characters.
     *
     * @return list of character info
     */
    List<CharacterInfo> listCharacters();

    /**
     * Update character ability scores.
     *
     * @param characterId the character ID
     * @param str strength
     * @param dex dexterity
     * @param con constitution
     * @param intelligence intelligence
     * @param wis wisdom
     * @param cha charisma
     */
    void updateAbilityScores(Long characterId, int str, int dex, int con, int intelligence, int wis, int cha);

    /**
     * Award experience points to a character.
     *
     * @param characterId the character ID
     * @param xp amount of XP to award
     * @return the updated character info
     */
    CharacterInfo awardExperience(Long characterId, int xp);

    /**
     * Information about a character.
     */
    class CharacterInfo {
        private Long id;
        private String name;
        private int level;
        private int experiencePoints;
        private int hitPoints;
        private int maxHitPoints;
        private int strScore;
        private int dexScore;
        private int conScore;
        private int intScore;
        private int wisScore;
        private int chaScore;
        private String location;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public int getExperiencePoints() { return experiencePoints; }
        public void setExperiencePoints(int experiencePoints) { this.experiencePoints = experiencePoints; }
        public int getHitPoints() { return hitPoints; }
        public void setHitPoints(int hitPoints) { this.hitPoints = hitPoints; }
        public int getMaxHitPoints() { return maxHitPoints; }
        public void setMaxHitPoints(int maxHitPoints) { this.maxHitPoints = maxHitPoints; }
        public int getStrScore() { return strScore; }
        public void setStrScore(int strScore) { this.strScore = strScore; }
        public int getDexScore() { return dexScore; }
        public void setDexScore(int dexScore) { this.dexScore = dexScore; }
        public int getConScore() { return conScore; }
        public void setConScore(int conScore) { this.conScore = conScore; }
        public int getIntScore() { return intScore; }
        public void setIntScore(int intScore) { this.intScore = intScore; }
        public int getWisScore() { return wisScore; }
        public void setWisScore(int wisScore) { this.wisScore = wisScore; }
        public int getChaScore() { return chaScore; }
        public void setChaScore(int chaScore) { this.chaScore = chaScore; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }
}
