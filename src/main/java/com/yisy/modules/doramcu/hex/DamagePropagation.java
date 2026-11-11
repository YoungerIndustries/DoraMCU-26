package com.yisy.modules.doramcu.hex;

import java.util.*;

/**
 * Handles damage propagation between exterior and interior cells.
 * <p>
 * When an exterior hull cell is destroyed, this class determines how damage
 * propagates to adjacent interior cells and compartments. It provides realistic
 * breach mechanics:
 * <ul>
 *   <li>Exterior breach → interior flooding/depressurization</li>
 *   <li>Secondary damage based on breach type and material</li>
 *   <li>Cascade effects through connected compartments</li>
 * </ul>
 * </p>
 * <p>
 * This class is stateless and operates on {@link HullModule} instances.
 * </p>
 */
public final class DamagePropagation {

    private DamagePropagation() {
        // Utility class — not instantiable
    }

    /**
     * The type of breach that occurs when an exterior cell is destroyed.
     */
    public enum BreachType {
        /** Hull breach in a fluid environment — causes flooding */
        FLOOD,

        /** Hull breach in vacuum — causes depressurization */
        DEPRESSURIZATION,

        /** Breach near explosive material — causes fire/explosion */
        FIRE,

        /** Structural breach — causes cascading damage */
        STRUCTURAL,

        /** Minor breach — no secondary effects */
        NONE
    }

    /**
     * Propagates damage from a destroyed exterior cell to adjacent interior cells.
     * <p>
     * When an exterior cell is destroyed, this method:
     * <ol>
     *   <li>Finds all face-adjacent interior cells</li>
     *   <li>Calculates breach damage based on the exterior cell's material</li>
     *   <li>Applies secondary damage to each adjacent interior cell</li>
     * </ol>
     * </p>
     *
     * @param module the hull module containing the damaged cell
     * @param damagedCell the destroyed exterior cell
     * @return list of interior cells that received secondary damage
     */
    public static List<M3space> propagate(HullModule module, M3space damagedCell) {
        List<M3space> affectedCells = new ArrayList<>();

        if (damagedCell == null || !damagedCell.isDestroyed()) {
            return affectedCells;
        }

        if (!module.isExteriorCell(damagedCell)) {
            return affectedCells;
        }

        // Check all face-adjacent neighbors
        for (M3space neighbor : damagedCell.faceNeighbors()) {
            M3space stored = module.get(neighbor);
            if (stored == null) {
                continue;
            }

            if (module.isInteriorCell(stored)) {
                // Apply secondary damage based on breach type
                int breachDamage = calculateBreachDamage(damagedCell.getMaterial());
                stored.damage(breachDamage);
                affectedCells.add(stored);
            }
        }

        return affectedCells;
    }

    /**
     * Propagates damage from a destroyed exterior cell to adjacent interior cells
     * and updates compartment secondary effects.
     *
     * @param module the hull module
     * @param damagedCell the destroyed exterior cell
     * @param compartments map of compartments to check for secondary effects
     * @param breachType the type of breach (determines secondary effects)
     * @return list of affected interior cells
     */
    public static List<M3space> propagateWithEffects(
            HullModule module,
            M3space damagedCell,
            Map<String, Compartment> compartments,
            BreachType breachType) {

        List<M3space> affectedCells = propagate(module, damagedCell);

        if (affectedCells.isEmpty() || compartments == null) {
            return affectedCells;
        }

        // Apply secondary effects to compartments containing affected cells
        for (Compartment compartment : compartments.values()) {
            for (M3space cell : affectedCells) {
                if (compartment.getCells().contains(cell)) {
                    applyBreachEffect(compartment, breachType);
                }
            }
        }

        return affectedCells;
    }

    /**
     * Calculates the secondary breach damage based on the exterior cell's material.
     * <p>
     * Stronger materials cause more secondary damage when breached because they
     * store more energy and create larger debris fields.
     * </p>
     *
     * @param material the material of the breached exterior cell
     * @return the secondary damage amount (1-5)
     */
    public static int calculateBreachDamage(Material material) {
        if (material == Material.Empty) {
            return 0;
        }
        // Breach damage scales with material strength
        // Explosive material causes maximum secondary damage
        if (material == Material.Explosive) {
            return 5;
        }
        return Math.max(1, material.ordinal() / 2);
    }

    /**
     * Determines the breach type based on the exterior cell's material and context.
     *
     * @param material the material of the breached cell
     * @param inFluid whether the breach is in a fluid environment (ocean, atmosphere)
     * @return the breach type
     */
    public static BreachType determineBreachType(Material material, boolean inFluid) {
        if (material == Material.Explosive) {
            return BreachType.FIRE;
        }
        if (inFluid) {
            return BreachType.FLOOD;
        }
        return BreachType.DEPRESSURIZATION;
    }

    /**
     * Applies a breach effect to a compartment based on the breach type.
     *
     * @param compartment the compartment to affect
     * @param breachType the type of breach
     */
    public static void applyBreachEffect(Compartment compartment, BreachType breachType) {
        switch (breachType) {
            case FLOOD:
                compartment.setFlooded(true);
                break;
            case DEPRESSURIZATION:
                compartment.setDepressurized(true);
                break;
            case FIRE:
                compartment.setOnFire(true);
                break;
            case STRUCTURAL:
                // Structural breaches cause additional cell damage
                for (M3space cell : compartment.getCells()) {
                    if (!cell.isDestroyed()) {
                        cell.damage(1);
                    }
                }
                break;
            case NONE:
            default:
                // No secondary effects
                break;
        }
    }

    /**
     * Propagates cascading damage through connected compartments.
     * <p>
     * If a compartment is critically damaged, adjacent compartments may also
     * suffer secondary effects due to structural instability.
     * </p>
     *
     * @param module the hull module
     * @param compartments map of all compartments
     * @return total number of cells affected by cascading damage
     */
    public static int propagateCascade(HullModule module, Map<String, Compartment> compartments) {
        int totalAffected = 0;

        // Find all critical compartments
        List<Compartment> criticalCompartments = new ArrayList<>();
        for (Compartment compartment : compartments.values()) {
            if (compartment.isCritical()) {
                criticalCompartments.add(compartment);
            }
        }

        // For each critical compartment, check adjacent cells in other compartments
        for (Compartment critical : criticalCompartments) {
            for (M3space cell : critical.getCells()) {
                if (cell.isDestroyed()) {
                    for (M3space neighbor : cell.faceNeighbors()) {
                        M3space stored = module.get(neighbor);
                        if (stored != null && !critical.getCells().contains(stored)) {
                            // This neighbor belongs to a different compartment
                            stored.damage(1);
                            totalAffected++;
                        }
                    }
                }
            }
        }

        return totalAffected;
    }

    /**
     * Checks if a path between two cells is blocked by destroyed cells.
     * <p>
     * Used to determine if compartments are still accessible from each other.
     * </p>
     *
     * @param module the hull module
     * @param from the starting cell
     * @param to the target cell
     * @return true if a clear path exists (no destroyed cells blocking)
     */
    public static boolean isPathClear(HullModule module, M3space from, M3space to) {
        // Simple BFS to check connectivity
        Set<M3space> visited = new HashSet<>();
        Queue<M3space> queue = new LinkedList<>();

        queue.add(from);
        visited.add(from);

        while (!queue.isEmpty()) {
            M3space current = queue.poll();
            if (current.equals(to)) {
                return true;
            }

            for (M3space neighbor : current.faceNeighbors()) {
                M3space stored = module.get(neighbor);
                if (stored != null && !stored.isDestroyed() && !visited.contains(stored)) {
                    visited.add(stored);
                    queue.add(stored);
                }
            }
        }

        return false;
    }

    /**
     * Returns the list of compartments affected by a hull breach at the given cell.
     *
     * @param module the hull module
     * @param breachedCell the breached exterior cell
     * @param compartments map of all compartments
     * @return list of compartments containing adjacent interior cells
     */
    public static List<Compartment> getAffectedCompartments(
            HullModule module,
            M3space breachedCell,
            Map<String, Compartment> compartments) {

        List<Compartment> affected = new ArrayList<>();

        if (breachedCell == null || compartments == null) {
            return affected;
        }

        // Find all adjacent interior cells
        for (M3space neighbor : breachedCell.faceNeighbors()) {
            M3space stored = module.get(neighbor);
            if (stored == null || !module.isInteriorCell(stored)) {
                continue;
            }

            // Find which compartment this cell belongs to
            for (Compartment compartment : compartments.values()) {
                if (compartment.getCells().contains(stored) && !affected.contains(compartment)) {
                    affected.add(compartment);
                }
            }
        }

        return affected;
    }
}
