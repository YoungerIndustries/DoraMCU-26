package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for space combat weapon types.
 * <p>
 * Covers BEAM, MISSILE, MASS_DRIVER, PULSION, PHOTON, TORPEDO, and COUNTERMEASURE.
 * </p>
 */
@Entity
@Table(name = "weapon_type", indexes = {
        @Index(name = "idx_wt_weapon_code", columnList = "weapon_code"),
        @Index(name = "idx_wt_strategic", columnList = "strategic_resource")
})
public class WeaponTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "weapon_code", nullable = false, unique = true, length = 50)
    private String weaponCode;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "strategic_resource", length = 50)
    private String strategicResource;

    @Column(name = "base_damage", nullable = false)
    private int baseDamage;

    @Column(name = "range_tier", nullable = false)
    private int rangeTier;

    @Column(name = "range_description", length = 20)
    private String rangeDescription;

    public WeaponTypeEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWeaponCode() {
        return weaponCode;
    }

    public void setWeaponCode(String weaponCode) {
        this.weaponCode = weaponCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getStrategicResource() {
        return strategicResource;
    }

    public void setStrategicResource(String strategicResource) {
        this.strategicResource = strategicResource;
    }

    public int getBaseDamage() {
        return baseDamage;
    }

    public void setBaseDamage(int baseDamage) {
        this.baseDamage = baseDamage;
    }

    public int getRangeTier() {
        return rangeTier;
    }

    public void setRangeTier(int rangeTier) {
        this.rangeTier = rangeTier;
    }

    public String getRangeDescription() {
        return rangeDescription;
    }

    public void setRangeDescription(String rangeDescription) {
        this.rangeDescription = rangeDescription;
    }
}
