package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for universal character statistics.
 * <p>
 * Covers DEXTERITY, TECHNICAL, STRENGTH, INTELLIGENCE, WISDOM, CHARISMA, and CONSTITUTION.
 * </p>
 */
@Entity
@Table(name = "character_stat", indexes = {
        @Index(name = "idx_cs_stat_code", columnList = "stat_code")
})
public class CharacterStatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "stat_code", nullable = false, unique = true, length = 20)
    private String statCode;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "min_value")
    private int minValue;

    @Column(name = "max_value")
    private int maxValue;

    public CharacterStatEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatCode() {
        return statCode;
    }

    public void setStatCode(String statCode) {
        this.statCode = statCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMinValue() {
        return minValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }
}
