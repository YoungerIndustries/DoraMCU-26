package com.yisy.modules.doramcu.hex;

import java.util.*;

/**
 * Represents a named functional compartment within a hull structure.
 * <p>
 * A compartment groups a set of {@link M3space} cells that share a common function,
 * such as "Bridge", "Engine Room", or "Cargo Bay". Each compartment tracks its own
 * integrity based on the state of its constituent cells, and can experience secondary
 * effects like flooding, fire, or depressurization when exterior hull is breached.
 * </p>
 * <p>
 * Compartments provide the foundation for interior mapping and damage evaluation:
 * when an exterior cell is destroyed, the adjacent compartment's integrity decreases,
 * and if the breach is severe enough, secondary effects propagate into the compartment.
 * </p>
 */
public class Compartment {

    private final String name;
    private final CompartmentType type;
    private final List<M3space> cells;
    private boolean flooded;
    private boolean onFire;
    private boolean depressurized;

    /**
     * Creates a new compartment with the given name and type.
     *
     * @param name the display name of this compartment (e.g., "Bridge", "Engine Room")
     * @param type the functional type of this compartment
     */
    public Compartment(String name, CompartmentType type) {
        this.name = name;
        this.type = type;
        this.cells = new ArrayList<>();
        this.flooded = false;
        this.onFire = false;
        this.depressurized = false;
    }

    // ---- Cell management ----

    /**
     * Adds an M3space cell to this compartment.
     *
     * @param cell the cell to add
     * @return true if the cell was added
     */
    public boolean addCell(M3space cell) {
        if (cell == null) {
            throw new IllegalArgumentException("Cell must not be null");
        }
        return cells.add(cell);
    }

    /**
     * Removes an M3space cell from this compartment.
     *
     * @param cell the cell to remove
     * @return true if the cell was removed
     */
    public boolean removeCell(M3space cell) {
        return cells.remove(cell);
    }

    /**
     * Adds multiple cells to this compartment.
     *
     * @param newCells the cells to add
     * @return true if any cells were added
     */
    public boolean addAllCells(Collection<M3space> newCells) {
        if (newCells == null) {
            throw new IllegalArgumentException("Cells must not be null");
        }
        return cells.addAll(newCells);
    }

    /**
     * Clears all cells from this compartment.
     */
    public void clearCells() {
        cells.clear();
    }

    /**
     * @return unmodifiable list of cells in this compartment
     */
    public List<M3space> getCells() {
        return List.copyOf(cells);
    }

    /**
     * @return the number of cells in this compartment
     */
    public int getCellCount() {
        return cells.size();
    }

    // ---- Accessors ----

    /** @return the display name of this compartment */
    public String getName() {
        return name;
    }

    /** @return the functional type of this compartment */
    public CompartmentType getType() {
        return type;
    }

    // ---- Integrity calculations ----

    /**
     * Returns the compartment integrity as a ratio from 0.0 to 1.0.
     * <p>
     * Integrity is calculated based on the state of constituent cells:
     * <ul>
     *   <li>Destroyed cells contribute 0%</li>
     *   <li>Damaged cells contribute (currentHitpoints / maxHitpoints) * 50%</li>
     *   <li>Healthy cells contribute 100%</li>
     * </ul>
     * The final integrity is the average across all cells.
     * </p>
     *
     * @return integrity ratio (0.0 = completely destroyed, 1.0 = fully operational)
     */
    public double getIntegrityRatio() {
        if (cells.isEmpty()) {
            return 1.0;
        }
        double total = 0;
        for (M3space cell : cells) {
            if (cell.isDestroyed()) {
                total += 0;
            } else {
                int maxHp = cell.getMaxHitpoints();
                if (maxHp <= 0) {
                    total += 1.0;
                } else {
                    double cellRatio = (double) cell.getHitpoints() / maxHp;
                    total += cellRatio;
                }
            }
        }
        return total / cells.size();
    }

    /**
     * Returns the compartment integrity as an integer percentage (0-100).
     *
     * @return integrity percentage
     */
    public int getIntegrity() {
        return (int) Math.round(getIntegrityRatio() * 100);
    }

    /**
     * @return true if the compartment is fully operational (integrity > 0.5)
     */
    public boolean isOperational() {
        return getIntegrityRatio() > 0.5;
    }

    /**
     * @return true if the compartment is critically damaged (integrity <= 0.25)
     */
    public boolean isCritical() {
        return getIntegrityRatio() <= 0.25;
    }

    /**
     * @return true if the compartment is completely destroyed (integrity = 0)
     */
    public boolean isDestroyed() {
        return cells.isEmpty() || getIntegrityRatio() == 0.0;
    }

    /**
     * Returns the number of destroyed cells in this compartment.
     *
     * @return count of destroyed cells
     */
    public int getDestroyedCellCount() {
        int count = 0;
        for (M3space cell : cells) {
            if (cell.isDestroyed()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the number of damaged (but not destroyed) cells in this compartment.
     *
     * @return count of damaged cells
     */
    public int getDamagedCellCount() {
        int count = 0;
        for (M3space cell : cells) {
            if (cell.isDamaged() && !cell.isDestroyed()) {
                count++;
            }
        }
        return count;
    }

    // ---- Secondary effects ----

    /** @return true if this compartment is flooded */
    public boolean isFlooded() {
        return flooded;
    }

    /**
     * Sets the flooded state of this compartment.
     *
     * @param flooded the new flooded state
     */
    public void setFlooded(boolean flooded) {
        this.flooded = flooded;
    }

    /** @return true if this compartment is on fire */
    public boolean isOnFire() {
        return onFire;
    }

    /**
     * Sets the fire state of this compartment.
     *
     * @param onFire the new fire state
     */
    public void setOnFire(boolean onFire) {
        this.onFire = onFire;
    }

    /** @return true if this compartment is depressurized */
    public boolean isDepressurized() {
        return depressurized;
    }

    /**
     * Sets the depressurization state of this compartment.
     *
     * @param depressurized the new depressurization state
     */
    public void setDepressurized(boolean depressurized) {
        this.depressurized = depressurized;
    }

    /**
     * Clears all secondary effects (flood, fire, depressurization).
     */
    public void clearSecondaryEffects() {
        this.flooded = false;
        this.onFire = false;
        this.depressurized = false;
    }

    // ---- Breach damage from HexLayer integration ----

    /**
     * Called when a {@link HexLayer} associated with this compartment has layers destroyed.
     * Each destroyed layer reduces the compartment integrity by damaging the constituent cells.
     * <p>
     * This method provides the integration point between the HexLayer abstraction (P2)
     * and the Compartment model (P0): when exterior hull layers are breached, the interior
     * compartment integrity decreases proportionally.
     * </p>
     *
     * @param layersDestroyed the number of new layers that were destroyed
     */
    public void damageFromBreach(int layersDestroyed) {
        if (layersDestroyed <= 0 || cells.isEmpty()) {
            return;
        }
        // Damage each cell proportionally to the number of layers destroyed
        // Each destroyed layer reduces cell hitpoints by a fraction of max hitpoints
        int damagePerCell = Math.max(1, layersDestroyed * 10);
        for (M3space cell : cells) {
            if (!cell.isDestroyed()) {
                cell.damage(damagePerCell);
            }
        }
        // If all layers are gone, trigger depressurization for space-based compartments
        if (layersDestroyed >= 2) {
            this.depressurized = true;
        }
    }

    // ---- Repair ----

    /**
     * Repairs all destroyed and damaged cells in this compartment.
     *
     * @return the number of cells repaired
     */
    public int repair() {
        int count = 0;
        for (M3space cell : cells) {
            if (cell.isDestroyed()) {
                if (cell.repair()) {
                    count++;
                }
            } else if (cell.isDamaged()) {
                cell.setHitpoints(cell.getMaxHitpoints());
                count++;
            }
        }
        clearSecondaryEffects();
        return count;
    }

    // ---- Object contract ----

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Compartment that = (Compartment) o;
        return Objects.equals(name, that.name) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return String.format("Compartment{name='%s', type=%s, cells=%d, integrity=%d%%, flooded=%s, fire=%s, depressurized=%s}",
                name, type, cells.size(), getIntegrity(), flooded, onFire, depressurized);
    }
}
