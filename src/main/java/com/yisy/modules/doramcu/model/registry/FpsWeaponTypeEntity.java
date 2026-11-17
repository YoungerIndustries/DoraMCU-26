package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for FPS weapon types.
 * <p>
 * Covers PISTOL, RIFLE, SHOTGUN, MACHINE_GUN, SNIPER, ROCKET_LAUNCHER, and GRENADE.
 * </p>
 */
@Entity
@Table(name = "fps_weapon_type", indexes = {
        @Index(name = "idx_fwt_weapon_code", columnList = "weapon_code")
})
public class FpsWeaponTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "weapon_code", nullable = false, unique = true, length = 50)
    private String weaponCode;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    public FpsWeaponTypeEntity() {
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
}
