package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for species/races across game modules.
 */
@Entity
@Table(name = "species", indexes = {
        @Index(name = "idx_sp_game_module", columnList = "game_module")
})
public class SpeciesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "species_code", nullable = false, unique = true, length = 50)
    private String speciesCode;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "game_module", length = 50)
    private String gameModule;

    @Column(name = "stat_modifiers", columnDefinition = "JSON")
    private String statModifiers;

    public SpeciesEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSpeciesCode() {
        return speciesCode;
    }

    public void setSpeciesCode(String speciesCode) {
        this.speciesCode = speciesCode;
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

    public String getGameModule() {
        return gameModule;
    }

    public void setGameModule(String gameModule) {
        this.gameModule = gameModule;
    }

    public String getStatModifiers() {
        return statModifiers;
    }

    public void setStatModifiers(String statModifiers) {
        this.statModifiers = statModifiers;
    }
}
