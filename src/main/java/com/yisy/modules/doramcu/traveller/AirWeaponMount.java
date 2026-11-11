package com.yisy.modules.doramcu.traveller;

/**
 * Weapon mounted on a Traveller spacecraft.
 */
public class AirWeaponMount {
    public enum MountType { GUN, MISSILE, LASER }

    private String id;
    private MountType mountType;
    private String weaponName;
    private int damageCode;
    private int range;
    private int ammoCount;
    private int arcOfFire;

    public AirWeaponMount() {}

    public AirWeaponMount(String id, MountType mountType, String weaponName, int damageCode, int range) {
        this.id = id;
        this.mountType = mountType;
        this.weaponName = weaponName;
        this.damageCode = damageCode;
        this.range = range;
        this.ammoCount = mountType == MountType.LASER ? -1 : 10;
        this.arcOfFire = 90;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public MountType getMountType() { return mountType; }
    public void setMountType(MountType mountType) { this.mountType = mountType; }
    public String getWeaponName() { return weaponName; }
    public void setWeaponName(String weaponName) { this.weaponName = weaponName; }
    public int getDamageCode() { return damageCode; }
    public void setDamageCode(int damageCode) { this.damageCode = damageCode; }
    public int getRange() { return range; }
    public void setRange(int range) { this.range = range; }
    public int getAmmoCount() { return ammoCount; }
    public void setAmmoCount(int ammoCount) { this.ammoCount = ammoCount; }
    public int getArcOfFire() { return arcOfFire; }
    public void setArcOfFire(int arcOfFire) { this.arcOfFire = arcOfFire; }
}
