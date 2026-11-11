package com.yisy.modules.doramcu.hex.planet;

import com.yisy.modules.doramcu.hex.Hex3D;

import java.util.*;

/**
 * Represents a settlement (town/city) on a planet surface.
 * <p>
 * Inspired by OpenTTD's {@code GSTown::FoundTown()}, a settlement:
 * <ul>
 *   <li>Occupies a cluster of {@link Hex3D} cells on the planet surface</li>
 *   <li>Grows in population when cargo goals are met</li>
 *   <li>Produces mail and passengers</li>
 *   <li>Consumes consumer goods (bread, meat, luxury items)</li>
 * </ul>
 * <p>
 * Settlement growth is driven by cargo delivery:
 * when a settlement receives enough of its desired cargo types,
 * the population increases and the town expands to adjacent hex cells.
 */
public class Settlement {

    private final String id;
    private final String name;
    private final Hex3D centerHex;
    private final List<Hex3D> occupiedHexes;
    private int population;
    private int maxPopulation;
    private int growthRate;
    private int turnsSinceLastGrowth;
    private final Map<CargoType, Integer> cargoGoals;
    private final Map<CargoType, Integer> cargoDelivered;
    private int mailProduction;
    private int passengerProduction;
    private boolean active;
    private String ownerEmpireId;

    /**
     * Settlement size categories that determine max population and growth.
     */
    public enum Size {
        VILLAGE(100, 500, 30),
        TOWN(500, 2000, 20),
        CITY(2000, 10000, 15),
        METROPOLIS(10000, 50000, 10);

        private final int minPopulation;
        private final int maxPopulation;
        private final int growthInterval;

        Size(int minPopulation, int maxPopulation, int growthInterval) {
            this.minPopulation = minPopulation;
            this.maxPopulation = maxPopulation;
            this.growthInterval = growthInterval;
        }

        public int getMinPopulation() { return minPopulation; }
        public int getMaxPopulation() { return maxPopulation; }
        public int getGrowthInterval() { return growthInterval; }
    }

    private Size size;

    /**
     * Creates a new settlement at the given hex position.
     *
     * @param id        unique identifier for this settlement
     * @param name      human-readable name
     * @param centerHex the center hex cell on the planet surface
     * @param size      initial size category
     */
    public Settlement(String id, String name, Hex3D centerHex, Size size) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Settlement ID must not be empty");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Settlement name must not be empty");
        }
        if (centerHex == null) {
            throw new IllegalArgumentException("Center hex must not be null");
        }
        if (size == null) {
            throw new IllegalArgumentException("Size must not be null");
        }
        this.id = id;
        this.name = name;
        this.centerHex = centerHex;
        this.occupiedHexes = new ArrayList<>();
        this.occupiedHexes.add(centerHex);
        this.size = size;
        this.population = size.getMinPopulation() / 2;
        this.maxPopulation = size.getMaxPopulation();
        this.growthRate = size.getGrowthInterval();
        this.turnsSinceLastGrowth = 0;
        this.cargoGoals = new EnumMap<>(CargoType.class);
        this.cargoDelivered = new EnumMap<>(CargoType.class);
        this.mailProduction = 0;
        this.passengerProduction = 0;
        this.active = true;
        this.ownerEmpireId = null;
        initializeCargoGoals();
    }

    /**
     * Initializes default cargo goals based on settlement size.
     * Settlements want food, mail, and luxury goods.
     */
    private void initializeCargoGoals() {
        cargoGoals.put(CargoType.BREAD, 10);
        cargoGoals.put(CargoType.MEAT, 5);
        cargoGoals.put(CargoType.MAIL, 8);
        cargoGoals.put(CargoType.LUXURY, 3);
    }

    /** @return the unique settlement ID */
    public String getId() {
        return id;
    }

    /** @return the settlement name */
    public String getName() {
        return name;
    }

    /** @return the center hex position */
    public Hex3D getCenterHex() {
        return centerHex;
    }

    /** @return all hex cells occupied by this settlement */
    public List<Hex3D> getOccupiedHexes() {
        return Collections.unmodifiableList(occupiedHexes);
    }

    /** @return the current population */
    public int getPopulation() {
        return population;
    }

    /** @return the maximum population for current size */
    public int getMaxPopulation() {
        return maxPopulation;
    }

    /** @return the growth rate (turns between growth checks) */
    public int getGrowthRate() {
        return growthRate;
    }

    /**
     * Sets the growth rate.
     *
     * @param growthRate turns between growth checks (lower = faster growth)
     */
    public void setGrowthRate(int growthRate) {
        if (growthRate < 1) {
            throw new IllegalArgumentException("Growth rate must be positive: " + growthRate);
        }
        this.growthRate = growthRate;
    }

    /** @return the cargo goals (type -> desired amount per turn) */
    public Map<CargoType, Integer> getCargoGoals() {
        return Collections.unmodifiableMap(cargoGoals);
    }

    /**
     * Sets a cargo goal for this settlement.
     *
     * @param cargoType the cargo type
     * @param amount    desired amount per turn
     */
    public void setCargoGoal(CargoType cargoType, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cargo goal amount must be non-negative: " + amount);
        }
        if (amount == 0) {
            cargoGoals.remove(cargoType);
        } else {
            cargoGoals.put(cargoType, amount);
        }
    }

    /** @return the cargo delivered this turn (type -> amount) */
    public Map<CargoType, Integer> getCargoDelivered() {
        return Collections.unmodifiableMap(cargoDelivered);
    }

    /**
     * Records cargo delivered to this settlement.
     *
     * @param cargoType the cargo type
     * @param amount    amount delivered
     */
    public void deliverCargo(CargoType cargoType, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Delivered amount must be non-negative: " + amount);
        }
        cargoDelivered.merge(cargoType, amount, Integer::sum);
    }

    /**
     * Processes a turn: checks cargo goals, grows population, produces mail/passengers.
     *
     * @return true if the settlement grew this turn
     */
    public boolean processTurn() {
        if (!active) return false;

        turnsSinceLastGrowth++;
        boolean grew = false;

        // Check growth conditions
        if (turnsSinceLastGrowth >= growthRate && population < maxPopulation) {
            double cargoSatisfaction = calculateCargoSatisfaction();
            if (cargoSatisfaction > 0.5) {
                // Grow population
                int growth = (int) (population * 0.1 * cargoSatisfaction);
                growth = Math.max(1, growth);
                population = Math.min(maxPopulation, population + growth);
                turnsSinceLastGrowth = 0;
                grew = true;

                // Check for size upgrade
                checkSizeUpgrade();
            }
        }

        // Produce mail and passengers based on population
        mailProduction = Math.max(1, population / 100);
        passengerProduction = Math.max(1, population / 200);

        // Reset delivered cargo for next turn
        cargoDelivered.clear();

        return grew;
    }

    /**
     * @return the cargo satisfaction ratio (0.0 to 1.0)
     * Higher means more cargo goals are being met.
     */
    private double calculateCargoSatisfaction() {
        if (cargoGoals.isEmpty()) return 1.0;
        int satisfied = 0;
        for (Map.Entry<CargoType, Integer> entry : cargoGoals.entrySet()) {
            int delivered = cargoDelivered.getOrDefault(entry.getKey(), 0);
            if (delivered >= entry.getValue()) {
                satisfied++;
            }
        }
        return (double) satisfied / cargoGoals.size();
    }

    /** Checks if the settlement should upgrade to a larger size. */
    private void checkSizeUpgrade() {
        if (size == Size.VILLAGE && population >= Size.TOWN.getMinPopulation()) {
            size = Size.TOWN;
        } else if (size == Size.TOWN && population >= Size.CITY.getMinPopulation()) {
            size = Size.CITY;
        } else if (size == Size.CITY && population >= Size.METROPOLIS.getMinPopulation()) {
            size = Size.METROPOLIS;
        }
        if (size != null) {
            maxPopulation = size.getMaxPopulation();
            growthRate = size.getGrowthInterval();
        }
    }

    /** @return the current size category */
    public Size getSize() {
        return size;
    }

    /** @return mail produced this turn */
    public int getMailProduction() {
        return mailProduction;
    }

    /** @return passengers produced this turn */
    public int getPassengerProduction() {
        return passengerProduction;
    }

    /** @return true if the settlement is active */
    public boolean isActive() {
        return active;
    }

    /** Activates or deactivates this settlement. */
    public void setActive(boolean active) {
        this.active = active;
    }

    /** @return the owner empire ID, or null if independent */
    public String getOwnerEmpireId() {
        return ownerEmpireId;
    }

    /**
     * Sets the owner empire.
     *
     * @param empireId the empire ID
     */
    public void setOwnerEmpireId(String empireId) {
        this.ownerEmpireId = empireId;
    }

    /**
     * Expands the settlement to adjacent hex cells.
     *
     * @param maxHexes maximum number of hex cells to occupy
     * @return the number of new hexes added
     */
    public int expand(int maxHexes) {
        if (occupiedHexes.size() >= maxHexes) return 0;
        int added = 0;
        List<Hex3D> current = new ArrayList<>(occupiedHexes);
        for (Hex3D hex : current) {
            if (occupiedHexes.size() >= maxHexes) break;
            for (Hex3D neighbor : hex.neighbors()) {
                if (!occupiedHexes.contains(neighbor)) {
                    occupiedHexes.add(neighbor);
                    added++;
                    if (occupiedHexes.size() >= maxHexes) break;
                }
            }
        }
        return added;
    }

    @Override
    public String toString() {
        return String.format("Settlement{%s, pop=%d, size=%s, hexes=%d}",
                name, population, size, occupiedHexes.size());
    }
}
