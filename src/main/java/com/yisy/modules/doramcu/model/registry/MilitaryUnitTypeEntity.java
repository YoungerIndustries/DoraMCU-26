package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for military unit types.
 * <p>
 * Covers INFANTRY, MECHANIZED, TANK, ARTILLERY, ANTI_AIR, RECON, ENGINEER, AIR_SUPPORT, and MECH.
 * </p>
 */
@Entity
@Table(name = "military_unit_type", indexes = {
        @Index(name = "idx_mut_game_module", columnList = "game_module")
})
public class MilitaryUnitTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "unit_code", nullable = false, unique = true, length = 50)
    private String unitCode;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "game_module", length = 50)
    private String gameModule;

    public MilitaryUnitTypeEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getGameModule() {
        return gameModule;
    }

    public void setGameModule(String gameModule) {
        this.gameModule = gameModule;
    }
}
