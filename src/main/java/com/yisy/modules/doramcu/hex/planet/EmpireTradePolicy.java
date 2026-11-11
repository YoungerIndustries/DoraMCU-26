package com.yisy.modules.doramcu.hex.planet;

import java.util.*;

/**
 * Trade policies set by an AI empire that affect the player's routes.
 * <p>
 * Each AI empire sets trade policies that determine how cargo passing through
 * its territory is taxed, which cargo types are banned, and which planets
 * offer reduced tariffs. Policies change dynamically based on empire state
 * (at war, allied, etc.).
 * </p>
 * <p>
 * Key features:
 * <ul>
 *   <li><b>Tariffs</b>: Percentage tax on cargo passing through empire space (0-50%)</li>
 *   <li><b>Embargoes</b>: Complete ban on certain cargo types</li>
 *   <li><b>Free trade zones</b>: Reduced tariffs for high-reputation corporations</li>
 *   <li><b>War disruptions</b>: Routes through war zones have increased risk and cost</li>
 * </ul>
 * </p>
 */
public class EmpireTradePolicy {

    /** Tax on cargo through empire space (0-50%) */
    private int tariffPercentage;

    /** Banned cargo types */
    private final Set<CargoType> embargoedCargo;

    /** Planets with reduced tariffs */
    private final Set<String> freeTradePlanets;

    /** Routes through this empire have increased risk */
    private boolean isWarZone;

    /** Additional risk multiplier for war zones (1.0 = no extra risk, 2.0 = 2x risk) */
    private double warZoneRiskMultiplier;

    /** Discount for high-reputation corporations (0.0-1.0) */
    private double reputationDiscount;

    /**
     * Creates a new trade policy with default values.
     */
    public EmpireTradePolicy() {
        this.tariffPercentage = 10;
        this.embargoedCargo = new HashSet<>();
        this.freeTradePlanets = new HashSet<>();
        this.isWarZone = false;
        this.warZoneRiskMultiplier = 1.0;
        this.reputationDiscount = 0.1;
    }

    /**
     * Creates a new trade policy with specified tariff.
     *
     * @param tariffPercentage base tariff percentage (0-50)
     */
    public EmpireTradePolicy(int tariffPercentage) {
        this();
        setTariffPercentage(tariffPercentage);
    }

    /** @return the base tariff percentage (0-50) */
    public int getTariffPercentage() {
        return tariffPercentage;
    }

    /**
     * Sets the base tariff percentage.
     *
     * @param tariffPercentage percentage (0-50)
     */
    public void setTariffPercentage(int tariffPercentage) {
        this.tariffPercentage = Math.max(0, Math.min(50, tariffPercentage));
    }

    /** @return unmodifiable set of embargoed cargo types */
    public Set<CargoType> getEmbargoedCargo() {
        return Collections.unmodifiableSet(embargoedCargo);
    }

    /**
     * Adds a cargo type to the embargo list.
     *
     * @param cargo the cargo type to embargo
     */
    public void embargoCargo(CargoType cargo) {
        if (cargo != null) {
            embargoedCargo.add(cargo);
        }
    }

    /**
     * Removes a cargo type from the embargo list.
     *
     * @param cargo the cargo type to un-embargo
     */
    public void unembargoCargo(CargoType cargo) {
        if (cargo != null) {
            embargoedCargo.remove(cargo);
        }
    }

    /** @return unmodifiable set of free trade planet IDs */
    public Set<String> getFreeTradePlanets() {
        return Collections.unmodifiableSet(freeTradePlanets);
    }

    /**
     * Sets the free trade planets.
     *
     * @param planets set of planet IDs with reduced tariffs
     */
    public void setFreeTradePlanets(Set<String> planets) {
        this.freeTradePlanets.clear();
        if (planets != null) {
            this.freeTradePlanets.addAll(planets);
        }
    }

    /**
     * Adds a planet to the free trade zone.
     *
     * @param planetId the planet ID
     */
    public void addFreeTradePlanet(String planetId) {
        if (planetId != null) {
            freeTradePlanets.add(planetId);
        }
    }

    /** @return true if this empire is in a war zone */
    public boolean isWarZone() {
        return isWarZone;
    }

    /**
     * Sets whether this empire is in a war zone.
     *
     * @param isWarZone true if at war
     */
    public void setWarZone(boolean isWarZone) {
        this.isWarZone = isWarZone;
        this.warZoneRiskMultiplier = isWarZone ? 1.5 : 1.0;
    }

    /** @return the risk multiplier for routes through this empire */
    public double getWarZoneRiskMultiplier() {
        return warZoneRiskMultiplier;
    }

    /**
     * Sets the risk multiplier for war zones.
     *
     * @param multiplier risk multiplier (1.0 = no extra risk)
     */
    public void setWarZoneRiskMultiplier(double multiplier) {
        this.warZoneRiskMultiplier = Math.max(1.0, multiplier);
    }

    /** @return the reputation discount (0.0-1.0) */
    public double getReputationDiscount() {
        return reputationDiscount;
    }

    /**
     * Sets the reputation discount for high-reputation corporations.
     *
     * @param discount discount factor (0.0-1.0)
     */
    public void setReputationDiscount(double discount) {
        this.reputationDiscount = Math.max(0.0, Math.min(1.0, discount));
    }

    /**
     * Calculates the tariff for a specific cargo type and planet.
     *
     * @param cargo the cargo type
     * @param planetId the planet ID
     * @param reputation the corporation's reputation with this empire (0-100)
     * @return the effective tariff percentage (0-50)
     */
    public double calculateTariff(CargoType cargo, String planetId, int reputation) {
        // Embargoed cargo cannot pass
        if (cargo != null && embargoedCargo.contains(cargo)) {
            return -1.0; // Special value indicating embargo
        }

        double tariff = tariffPercentage;

        // Free trade planets have reduced tariffs
        if (planetId != null && freeTradePlanets.contains(planetId)) {
            tariff *= 0.5;
        }

        // High reputation reduces tariffs
        if (reputation > 50) {
            double discount = (reputation - 50) / 50.0 * reputationDiscount;
            tariff *= (1.0 - discount);
        }

        // War zones increase tariffs
        if (isWarZone) {
            tariff *= 1.2;
        }

        return Math.max(0, Math.min(50, tariff));
    }

    /**
     * Checks if a cargo type is allowed through this empire.
     *
     * @param cargo the cargo type
     * @return true if allowed, false if embargoed
     */
    public boolean isCargoAllowed(CargoType cargo) {
        if (cargo == null) {
            return true;
        }
        return !embargoedCargo.contains(cargo);
    }

    /**
     * Gets the route risk multiplier for routes through this empire.
     * War zones have higher risk.
     *
     * @return risk multiplier (1.0 = normal, >1.0 = increased risk)
     */
    public double getRouteRiskMultiplier() {
        return warZoneRiskMultiplier;
    }

    /**
     * Calculates the effective cost multiplier for a route through this empire.
     * Combines tariffs and risk into a single multiplier.
     *
     * @param planetId the planet ID
     * @param reputation the corporation's reputation (0-100)
     * @return cost multiplier (1.0 = no extra cost)
     */
    public double getCostMultiplier(String planetId, int reputation) {
        double tariff = calculateTariff(null, planetId, reputation);
        if (tariff < 0) {
            return Double.MAX_VALUE; // Embargoed
        }
        return 1.0 + (tariff / 100.0) * warZoneRiskMultiplier;
    }

    @Override
    public String toString() {
        return "EmpireTradePolicy{" +
                "tariffPercentage=" + tariffPercentage +
                ", embargoedCargo=" + embargoedCargo +
                ", freeTradePlanets=" + freeTradePlanets +
                ", isWarZone=" + isWarZone +
                ", warZoneRiskMultiplier=" + warZoneRiskMultiplier +
                '}';
    }
}
