package com.yisy.modules.doramcu.hex.planet;

import com.yisy.modules.doramcu.hex.Hex3D;

import java.util.*;

/**
 * Represents an individual industry instance on the planet surface.
 * <p>
 * An industry:
 * <ul>
 *   <li>Occupies a single {@link Hex3D} cell on the planet surface</li>
 *   <li>Produces cargo based on its {@link IndustryType}</li>
 *   <li>Consumes input cargo to maintain production</li>
 *   <li>Has a production level (0.0 to 1.0) based on input availability</li>
 * </ul>
 * <p>
 * Production follows the OpenTTD pattern: if an industry receives enough
 * input cargo, it produces output at full capacity. Without inputs,
 * production drops proportionally.
 */
public class Industry {

    private final String id;
    private final IndustryType type;
    private final Hex3D position;
    private final Map<CargoType, Integer> inputInventory;
    private final Map<CargoType, Integer> outputInventory;
    private double productionLevel;
    private int turnsSinceBuilt;
    private boolean active;
    private String ownerEmpireId;

    /**
     * Creates a new industry at the given hex position.
     *
     * @param id     unique identifier for this industry
     * @param type   the type of industry (defines inputs/outputs)
     * @param position the hex cell where the industry is built
     */
    public Industry(String id, IndustryType type, Hex3D position) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Industry ID must not be empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Industry type must not be null");
        }
        if (position == null) {
            throw new IllegalArgumentException("Position must not be null");
        }
        this.id = id;
        this.type = type;
        this.position = position;
        this.inputInventory = new EnumMap<>(CargoType.class);
        this.outputInventory = new EnumMap<>(CargoType.class);
        this.productionLevel = 1.0;
        this.turnsSinceBuilt = 0;
        this.active = true;
        this.ownerEmpireId = null;
    }

    /** @return the unique industry ID */
    public String getId() {
        return id;
    }

    /** @return the industry type */
    public IndustryType getType() {
        return type;
    }

    /** @return the hex position on the planet surface */
    public Hex3D getPosition() {
        return position;
    }

    /** @return the hex position on the planet surface (alias for getPosition) */
    public Hex3D getLocation() {
        return position;
    }

    /** @return the input inventory (cargo waiting to be processed) */
    public Map<CargoType, Integer> getInputInventory() {
        return Collections.unmodifiableMap(inputInventory);
    }

    /** @return the output inventory (produced cargo waiting to be collected) */
    public Map<CargoType, Integer> getOutputInventory() {
        return Collections.unmodifiableMap(outputInventory);
    }

    /** @return the current production level (0.0 to 1.0) */
    public double getProductionLevel() {
        return productionLevel;
    }

    /** @return the number of turns since this industry was built */
    public int getTurnsSinceBuilt() {
        return turnsSinceBuilt;
    }

    /** @return true if the industry is active */
    public boolean isActive() {
        return active;
    }

    /** Activates or deactivates this industry. */
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
     * Delivers input cargo to this industry.
     *
     * @param cargoType the cargo type
     * @param amount    amount delivered
     */
    public void deliverInput(CargoType cargoType, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative: " + amount);
        }
        inputInventory.merge(cargoType, amount, Integer::sum);
    }

    /**
     * Collects output cargo from this industry.
     *
     * @param cargoType the cargo type to collect
     * @param amount    maximum amount to collect
     * @return the actual amount collected
     */
    public int collectOutput(CargoType cargoType, int amount) {
        int available = outputInventory.getOrDefault(cargoType, 0);
        int collected = Math.min(amount, available);
        if (collected > 0) {
            int remaining = available - collected;
            if (remaining == 0) {
                outputInventory.remove(cargoType);
            } else {
                outputInventory.put(cargoType, remaining);
            }
        }
        return collected;
    }

    /**
     * Processes a turn: consumes inputs, produces outputs, updates production level.
     *
     * @return the output produced this turn (type -> amount)
     */
    public Map<CargoType, Integer> processTurn() {
        Map<CargoType, Integer> produced = new EnumMap<>(CargoType.class);
        if (!active) return produced;

        turnsSinceBuilt++;

        // Calculate production level based on input availability
        productionLevel = calculateProductionLevel();

        // Consume inputs proportionally
        consumeInputs();

        // Produce outputs based on production level
        for (Map.Entry<CargoType, Integer> entry : type.getOutput().entrySet()) {
            int amount = (int) (entry.getValue() * productionLevel);
            if (amount > 0) {
                produced.put(entry.getKey(), amount);
                outputInventory.merge(entry.getKey(), amount, Integer::sum);
            }
        }

        return produced;
    }

    /**
     * @return the production level based on input availability (0.0 to 1.0)
     */
    private double calculateProductionLevel() {
        Map<CargoType, Integer> requiredInputs = type.getInput();
        if (requiredInputs.isEmpty()) return 1.0;

        double minRatio = Double.MAX_VALUE;
        for (Map.Entry<CargoType, Integer> entry : requiredInputs.entrySet()) {
            int available = inputInventory.getOrDefault(entry.getKey(), 0);
            double ratio = (double) available / entry.getValue();
            minRatio = Math.min(minRatio, ratio);
        }
        return Math.min(1.0, minRatio);
    }

    /** Consumes input cargo based on current production level. */
    private void consumeInputs() {
        Map<CargoType, Integer> requiredInputs = type.getInput();
        for (Map.Entry<CargoType, Integer> entry : requiredInputs.entrySet()) {
            int available = inputInventory.getOrDefault(entry.getKey(), 0);
            int needed = (int) (entry.getValue() * productionLevel);
            int consumed = Math.min(available, needed);
            if (consumed > 0) {
                int remaining = available - consumed;
                if (remaining == 0) {
                    inputInventory.remove(entry.getKey());
                } else {
                    inputInventory.put(entry.getKey(), remaining);
                }
            }
        }
    }

    /**
     * @return the construction cost for this industry type
     */
    public int getConstructionCost() {
        return type.getConstructionCost();
    }

    /**
     * @return the per-turn maintenance cost
     */
    public int getMaintenanceCost() {
        return type.getMaintenanceCost();
    }

    @Override
    public String toString() {
        return String.format("Industry{%s, level=%.1f, pos=%s}",
                type.getDisplayName(), productionLevel, position);
    }
}
