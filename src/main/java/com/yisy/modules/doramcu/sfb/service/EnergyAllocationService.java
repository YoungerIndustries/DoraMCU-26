package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.sfb.model.SfbEnergyAllocation;
import com.yisy.modules.doramcu.sfb.model.SfbSSD;
import com.yisy.modules.doramcu.sfb.model.SfbShip;

/**
 * Validates and processes energy allocation forms.
 * <p>
 * Calculates available power from warp/impulse/auxiliary engines,
 * batteries, and capacitors based on the ship's SSD state.
 * </p>
 */
public class EnergyAllocationService {

    /**
     * Calculate available power from the ship's SSD.
     */
    public SfbEnergyAllocation calculateAvailablePower(SfbShip ship) {
        SfbSSD ssd = ship.getSsd();
        SfbEnergyAllocation ea = new SfbEnergyAllocation();

        // Warp engines provide power based on operational boxes
        ea.setWarpPower(ssd.getAvailableWarpPower());

        // Impulse engines
        ea.setImpulsePower(ssd.getAvailableImpulsePower());

        // Auxiliary engines
        ea.setAuxiliaryPower(ssd.getAvailableAuxiliaryPower());

        // Batteries (if operational)
        if (ssd.isSystemOperational(SfbSSD.SystemCategory.BATTERIES)) {
            ea.setBatteryPower(2); // Standard battery capacity
        }

        // Capacitors (if operational)
        if (ssd.isSystemOperational(SfbSSD.SystemCategory.CAPACITORS)) {
            ea.setCapacitorPower(2); // Standard capacitor capacity
        }

        return ea;
    }

    /**
     * Validate an energy allocation against the ship's available power.
     */
    public boolean validateAllocation(SfbShip ship, SfbEnergyAllocation allocation) {
        if (allocation == null) return false;

        SfbEnergyAllocation available = calculateAvailablePower(ship);
        return allocation.getTotalAllocatedPower() <= available.getTotalAvailablePower();
    }

    /**
     * Calculate the energy cost for a given speed.
     * Based on the movement.json energy cost table.
     */
    public int getSpeedEnergyCost(int speed) {
        if (speed <= 3) return 1;
        if (speed <= 6) return 2;
        if (speed <= 9) return 3;
        if (speed <= 12) return 4;
        if (speed <= 16) return 6;
        if (speed <= 20) return 8;
        if (speed <= 24) return 10;
        if (speed <= 28) return 12;
        return 14; // Speed 32
    }

    /**
     * Check if the ship has enough power for the given speed.
     */
    public boolean hasPowerForSpeed(SfbShip ship, int speed) {
        SfbEnergyAllocation ea = ship.getEnergyAllocation();
        if (ea == null) return false;

        int cost = getSpeedEnergyCost(speed);
        return ea.getWarpPower() >= cost;
    }
}
