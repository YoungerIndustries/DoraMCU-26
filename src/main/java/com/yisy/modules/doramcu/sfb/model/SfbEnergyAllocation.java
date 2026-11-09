package com.yisy.modules.doramcu.sfb.model;

/**
 * Represents the per-turn energy allocation form for an SFB ship.
 * <p>
 * Each turn, the player fills out an energy allocation form specifying
 * how much power goes to shields, weapons, and special systems.
 * Power comes from warp engines, impulse engines, auxiliary engines,
 * batteries, and capacitors.
 * </p>
 */
public class SfbEnergyAllocation {

    // Power sources
    private int warpPower;
    private int impulsePower;
    private int auxiliaryPower;
    private int batteryPower;
    private int capacitorPower;

    // Allocations
    private int shieldAllocation;
    private int weaponAllocation;
    private int specialSystemAllocation;
    private int ewAllocation; // Electronic Warfare

    // Reinforcement
    private int[] shieldReinforcement; // 6 directions

    public SfbEnergyAllocation() {
        this.shieldReinforcement = new int[6];
    }

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

    public int getShieldAllocation() { return shieldAllocation; }
    public void setShieldAllocation(int shieldAllocation) { this.shieldAllocation = shieldAllocation; }

    public int getWeaponAllocation() { return weaponAllocation; }
    public void setWeaponAllocation(int weaponAllocation) { this.weaponAllocation = weaponAllocation; }

    public int getSpecialSystemAllocation() { return specialSystemAllocation; }
    public void setSpecialSystemAllocation(int specialSystemAllocation) { this.specialSystemAllocation = specialSystemAllocation; }

    public int getEwAllocation() { return ewAllocation; }
    public void setEwAllocation(int ewAllocation) { this.ewAllocation = ewAllocation; }

    public int[] getShieldReinforcement() { return shieldReinforcement; }
    public void setShieldReinforcement(int[] shieldReinforcement) {
        this.shieldReinforcement = shieldReinforcement;
    }

    public void setShieldReinforcement(int direction, int points) {
        if (direction >= 0 && direction < 6) {
            this.shieldReinforcement[direction] = points;
        }
    }

    /**
     * Calculate the total available power from all sources.
     */
    public int getTotalAvailablePower() {
        return warpPower + impulsePower + auxiliaryPower + batteryPower + capacitorPower;
    }

    /**
     * Calculate the total power allocated to all categories.
     */
    public int getTotalAllocatedPower() {
        int total = shieldAllocation + weaponAllocation + specialSystemAllocation + ewAllocation;
        for (int points : shieldReinforcement) {
            total += points;
        }
        return total;
    }

    /**
     * Get the reserve power (available - allocated).
     */
    public int getReservePower() {
        return getTotalAvailablePower() - getTotalAllocatedPower();
    }

    /**
     * Validate the energy allocation.
     * Returns true if the allocation is valid (allocated <= available).
     */
    public boolean validate() {
        return getTotalAllocatedPower() <= getTotalAvailablePower();
    }

    /**
     * Check if there is enough power for the given cost.
     * Uses weapon allocation first, then reserve power.
     */
    public boolean hasPower(int cost) {
        return getTotalAllocatedPower() + cost <= getTotalAvailablePower();
    }

    /**
     * Consume power for a weapon shot.
     */
    public void consumePower(int cost) {
        weaponAllocation += cost;
    }

    /**
     * Reset the allocation for a new turn, keeping power sources.
     */
    public void resetAllocation() {
        shieldAllocation = 0;
        weaponAllocation = 0;
        specialSystemAllocation = 0;
        ewAllocation = 0;
        for (int i = 0; i < 6; i++) {
            shieldReinforcement[i] = 0;
        }
    }

    @Override
    public String toString() {
        return "SfbEnergyAllocation{available=" + getTotalAvailablePower() +
                ", allocated=" + getTotalAllocatedPower() + ", reserve=" + getReservePower() + "}";
    }
}
