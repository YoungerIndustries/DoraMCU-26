package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * JPA entity for individual FPS weapons.
 */
@Entity
@Table(name = "fps_weapon", indexes = {
        @Index(name = "idx_fw_weapon_type", columnList = "weapon_type_code")
})
public class FpsWeaponEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "weapon_uuid", nullable = false, unique = true, length = 36)
    private String weaponUuid;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "weapon_type_code", nullable = false, length = 50)
    private String weaponTypeCode;

    @Column(nullable = false)
    private int damage;

    @Column(name = "fire_rate", nullable = false)
    private Double fireRate;

    @Column(name = "reload_time", nullable = false)
    private Double reloadTime;

    @Column(name = "mag_size", nullable = false)
    private int magSize;

    @Column(name = "reserve_ammo", nullable = false)
    private int reserveAmmo;

    @Column(name = "is_hit_scan", nullable = false)
    private boolean isHitScan;

    @Column(name = "projectile_speed")
    private Double projectileSpeed;

    @Column(name = "recoil_pitch")
    private Double recoilPitch;

    @Column(name = "recoil_yaw")
    private Double recoilYaw;

    @Column
    private Double range;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "model_3d_path", length = 500)
    private String model3dPath;

    @Column(name = "texture_path", length = 500)
    private String texturePath;

    public FpsWeaponEntity() {
        this.weaponUuid = UUID.randomUUID().toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWeaponUuid() {
        return weaponUuid;
    }

    public void setWeaponUuid(String weaponUuid) {
        this.weaponUuid = weaponUuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWeaponTypeCode() {
        return weaponTypeCode;
    }

    public void setWeaponTypeCode(String weaponTypeCode) {
        this.weaponTypeCode = weaponTypeCode;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public Double getFireRate() {
        return fireRate;
    }

    public void setFireRate(Double fireRate) {
        this.fireRate = fireRate;
    }

    public Double getReloadTime() {
        return reloadTime;
    }

    public void setReloadTime(Double reloadTime) {
        this.reloadTime = reloadTime;
    }

    public int getMagSize() {
        return magSize;
    }

    public void setMagSize(int magSize) {
        this.magSize = magSize;
    }

    public int getReserveAmmo() {
        return reserveAmmo;
    }

    public void setReserveAmmo(int reserveAmmo) {
        this.reserveAmmo = reserveAmmo;
    }

    public boolean isHitScan() {
        return isHitScan;
    }

    public void setHitScan(boolean hitScan) {
        isHitScan = hitScan;
    }

    public Double getProjectileSpeed() {
        return projectileSpeed;
    }

    public void setProjectileSpeed(Double projectileSpeed) {
        this.projectileSpeed = projectileSpeed;
    }

    public Double getRecoilPitch() {
        return recoilPitch;
    }

    public void setRecoilPitch(Double recoilPitch) {
        this.recoilPitch = recoilPitch;
    }

    public Double getRecoilYaw() {
        return recoilYaw;
    }

    public void setRecoilYaw(Double recoilYaw) {
        this.recoilYaw = recoilYaw;
    }

    public Double getRange() {
        return range;
    }

    public void setRange(Double range) {
        this.range = range;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModel3dPath() {
        return model3dPath;
    }

    public void setModel3dPath(String model3dPath) {
        this.model3dPath = model3dPath;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public void setTexturePath(String texturePath) {
        this.texturePath = texturePath;
    }
}
