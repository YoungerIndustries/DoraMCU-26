package com.yisy.modules.doramcu.fps.core.model;

import java.util.UUID;

/**
 * Defines an FPS weapon with damage, fire rate, reload time, and ammo characteristics.
 */
public class FpsWeapon {

    public enum WeaponType {
        PISTOL, RIFLE, SHOTGUN, MACHINE_GUN, SNIPER, ROCKET_LAUNCHER, GRENADE
    }

    private String id;
    private String name;
    private WeaponType type;
    private int damage;
    private double fireRate;
    private double reloadTime;
    private int magSize;
    private int reserveAmmo;
    private boolean hitScan;
    private double projectileSpeed;
    private double recoilPitch;
    private double recoilYaw;
    private double range;
    private String description;

    public FpsWeapon() {
    }

    public FpsWeapon(String name, WeaponType type, int damage, double fireRate, double reloadTime, int magSize) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.damage = damage;
        this.fireRate = fireRate;
        this.reloadTime = reloadTime;
        this.magSize = magSize;
        this.reserveAmmo = magSize * 5;
        this.hitScan = true;
        this.projectileSpeed = 100.0;
        this.recoilPitch = 2.0;
        this.recoilYaw = 1.0;
        this.range = 100.0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public WeaponType getType() { return type; }
    public void setType(WeaponType type) { this.type = type; }
    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }
    public double getFireRate() { return fireRate; }
    public void setFireRate(double fireRate) { this.fireRate = fireRate; }
    public double getReloadTime() { return reloadTime; }
    public void setReloadTime(double reloadTime) { this.reloadTime = reloadTime; }
    public int getMagSize() { return magSize; }
    public void setMagSize(int magSize) { this.magSize = magSize; }
    public int getReserveAmmo() { return reserveAmmo; }
    public void setReserveAmmo(int reserveAmmo) { this.reserveAmmo = reserveAmmo; }
    public boolean isHitScan() { return hitScan; }
    public void setHitScan(boolean hitScan) { this.hitScan = hitScan; }
    public double getProjectileSpeed() { return projectileSpeed; }
    public void setProjectileSpeed(double projectileSpeed) { this.projectileSpeed = projectileSpeed; }
    public double getRecoilPitch() { return recoilPitch; }
    public void setRecoilPitch(double recoilPitch) { this.recoilPitch = recoilPitch; }
    public double getRecoilYaw() { return recoilYaw; }
    public void setRecoilYaw(double recoilYaw) { this.recoilYaw = recoilYaw; }
    public double getRange() { return range; }
    public void setRange(double range) { this.range = range; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
