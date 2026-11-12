package com.yisy.modules.doramcu.unified;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Concrete implementation of {@link UnifiedCharacter} that provides a simple
 * data holder for the unified character representation.
 * <p>
 * Used by {@link CharacterMapper} implementations to return unified characters
 * without requiring the game-specific character to implement the interface.
 * </p>
 */
public class AbstractUnifiedCharacter implements UnifiedCharacter {

    private final Long id;
    private final String name;
    private final int age;
    private final String race;
    private final Map<String, Integer> abilities;
    private final int hitPoints;
    private final int maxHitPoints;
    private final int experiencePoints;
    private final int level;
    private final List<SkillEntry> skills;
    private final String equipment;
    private final String status;
    private final boolean alive;
    private final GameType gameType;
    private final Map<String, Object> metadata;

    public AbstractUnifiedCharacter(
            Long id,
            String name,
            int age,
            String race,
            Map<String, Integer> abilities,
            int hitPoints,
            int maxHitPoints,
            int experiencePoints,
            int level,
            List<SkillEntry> skills,
            String equipment,
            String status,
            boolean alive,
            GameType gameType,
            Map<String, Object> metadata) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.race = race;
        this.abilities = abilities != null ? Collections.unmodifiableMap(new java.util.HashMap<>(abilities)) : Collections.emptyMap();
        this.hitPoints = hitPoints;
        this.maxHitPoints = maxHitPoints;
        this.experiencePoints = experiencePoints;
        this.level = level;
        this.skills = skills != null ? Collections.unmodifiableList(new java.util.ArrayList<>(skills)) : Collections.emptyList();
        this.equipment = equipment;
        this.status = status;
        this.alive = alive;
        this.gameType = gameType;
        this.metadata = metadata != null ? Collections.unmodifiableMap(new java.util.HashMap<>(metadata)) : Collections.emptyMap();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public String getRace() {
        return race;
    }

    @Override
    public Map<String, Integer> getAbilities() {
        return abilities;
    }

    @Override
    public int getHitPoints() {
        return hitPoints;
    }

    @Override
    public int getMaxHitPoints() {
        return maxHitPoints;
    }

    @Override
    public int getExperiencePoints() {
        return experiencePoints;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public List<SkillEntry> getSkills() {
        return skills;
    }

    @Override
    public String getEquipment() {
        return equipment;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public GameType getGameType() {
        return gameType;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "UnifiedCharacter{name='" + name + "', gameType=" + gameType +
                ", abilities=" + abilities + ", hp=" + hitPoints + "/" + maxHitPoints + "}";
    }
}
