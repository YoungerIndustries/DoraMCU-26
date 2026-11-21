package com.yisy.modules.doramcu.sfb.model;

/**
 * Represents energy allocation in SFB.
 */
public class SfbEnergyAllocation {
    private String shipId;
    private double weapons;
    private double shields;
    private double engines;
    private int warpPower;
    private int impulsePower;
    private int auxiliaryPower;
    private int batteryPower;
    private int capacitorPower;
    private int totalAllocatedPower;
    private int totalAvailablePower;
    private int shieldAllocation;
    private int weaponAllocation;
    private int specialSystemAllocation;
    private int ewAllocation;
    private int[] shieldReinforcement;

    public SfbEnergyAllocation() {}

    public SfbEnergyAllocation(String shipId, double weapons, double shields, double engines) {
        this.shipId = shipId;
        this.weapons = weapons;
        this.shields = shields;
        this.engines = engines;
    }

    public String getShipId() { return shipId; }
    public void setShipId(String shipId) { this.shipId = shipId; }
    public double getWeapons() { return weapons; }
    public void setWeapons(double weapons) { this.weapons = weapons; }
    public double getShields() { return shields; }
    public void setShields(double shields) { this.shields = shields; }
    public double getEngines() { return engines; }
    public void setEngines(double engines) { this.engines = engines; }
    public int getWarpPower() { return warpPower; }
    public void setWarpPower(int warpPower) { this.warpPower = warpPower; }
    public int getImpulsePower() { return impulsePower; }
    public void setImpulsePower(int impulsePower) { this.impulsePower = impulsePower; }
    public int getAuxiliaryPower() { return auxiliaryPower; }
    public void setAuxiliaryPower(int auxiliaryPower) { this.auxiliaryPower = auxiliaryPower; }
    public int getBatteryPower() { return batteryPower; }
    public void setBatteryPower(int batteryPower) { this.batteryPower = batteryPower; }
    public int getCapacitorPower() { return capacitorPower; }
    public void setCapacitorPower(int capacitorPower) { this.capacitorPower = capacitorPower; }
    public int getTotalAllocatedPower() { return totalAllocatedPower; }
    public void setTotalAllocatedPower(int totalAllocatedPower) { this.totalAllocatedPower = totalAllocatedPower; }
    public int getTotalAvailablePower() { return totalAvailablePower; }
    public void setTotalAvailablePower(int totalAvailablePower) { this.totalAvailablePower = totalAvailablePower; }
    public boolean hasPower(int amount) { return totalAvailablePower >= amount; }
    public void consumePower(int amount) { totalAvailablePower -= amount; totalAllocatedPower += amount; }
    public int getShieldAllocation() { return shieldAllocation; }
    public void setShieldAllocation(int shieldAllocation) { this.shieldAllocation = shieldAllocation; }
    public int getWeaponAllocation() { return weaponAllocation; }
    public void setWeaponAllocation(int weaponAllocation) { this.weaponAllocation = weaponAllocation; }
    public int getSpecialSystemAllocation() { return specialSystemAllocation; }
    public void setSpecialSystemAllocation(int specialSystemAllocation) { this.specialSystemAllocation = specialSystemAllocation; }
    public int getEwAllocation() { return ewAllocation; }
    public void setEwAllocation(int ewAllocation) { this.ewAllocation = ewAllocation; }
    public int[] getShieldReinforcement() { return shieldReinforcement; }
    public void setShieldReinforcement(int[] shieldReinforcement) { this.shieldReinforcement = shieldReinforcement; }
}
