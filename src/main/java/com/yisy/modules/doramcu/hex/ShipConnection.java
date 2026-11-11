package com.yisy.modules.doramcu.hex;

import java.util.Objects;

/**
 * Represents a connection (passage) between two HullModules in a ship.
 * <p>
 * A connection links a specific cell in one module to a specific cell in another module,
 * forming the edges of the ship's internal graph. Each module is identified by a unique string ID,
 * and the passage cells are the {@link M3space} positions where crew can move between modules.
 * </p>
 * <p>
 * Connections are bidirectional: traversing from module A to module B is the same as
 * traversing from module B to module A.
 * </p>
 * <p>
 * Used by {@link ShipHull} to enable pathfinding, damage propagation, and crew movement
 * simulation between ship sections.
 * </p>
 */
public class ShipConnection {

    private final String fromModuleId;
    private final M3space fromCell;
    private final String toModuleId;
    private final M3space toCell;

    /**
     * Creates a new connection between two modules at specific passage cells.
     *
     * @param fromModuleId the ID of the source module
     * @param fromCell the passage cell in the source module
     * @param toModuleId the ID of the target module
     * @param toCell the passage cell in the target module
     */
    public ShipConnection(String fromModuleId, M3space fromCell, String toModuleId, M3space toCell) {
        if (fromModuleId == null || fromModuleId.isBlank()) {
            throw new IllegalArgumentException("fromModuleId must not be null or blank");
        }
        if (toModuleId == null || toModuleId.isBlank()) {
            throw new IllegalArgumentException("toModuleId must not be null or blank");
        }
        if (fromCell == null) {
            throw new IllegalArgumentException("fromCell must not be null");
        }
        if (toCell == null) {
            throw new IllegalArgumentException("toCell must not be null");
        }
        this.fromModuleId = fromModuleId;
        this.fromCell = fromCell;
        this.toModuleId = toModuleId;
        this.toCell = toCell;
    }

    /** @return the ID of the source module */
    public String getFromModuleId() {
        return fromModuleId;
    }

    /** @return the passage cell in the source module */
    public M3space getFromCell() {
        return fromCell;
    }

    /** @return the ID of the target module */
    public String getToModuleId() {
        return toModuleId;
    }

    /** @return the passage cell in the target module */
    public M3space getToCell() {
        return toCell;
    }

    /**
     * Returns the other module ID given one of the connected module IDs.
     * <p>
     * If the given moduleId matches fromModuleId, returns toModuleId, and vice versa.
     * </p>
     *
     * @param moduleId one of the connected module IDs
     * @return the other module ID, or null if moduleId is not part of this connection
     */
    public String getOtherModuleId(String moduleId) {
        if (fromModuleId.equals(moduleId)) {
            return toModuleId;
        }
        if (toModuleId.equals(moduleId)) {
            return fromModuleId;
        }
        return null;
    }

    /**
     * Returns the other module's passage cell given one of the connected module IDs.
     *
     * @param moduleId one of the connected module IDs
     * @return the other module's passage cell, or null if moduleId is not part of this connection
     */
    public M3space getOtherCell(String moduleId) {
        if (fromModuleId.equals(moduleId)) {
            return toCell;
        }
        if (toModuleId.equals(moduleId)) {
            return fromCell;
        }
        return null;
    }

    /**
     * Checks whether this connection involves the given module.
     *
     * @param moduleId the module ID to check
     * @return true if the module is part of this connection
     */
    public boolean involvesModule(String moduleId) {
        return fromModuleId.equals(moduleId) || toModuleId.equals(moduleId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShipConnection that = (ShipConnection) o;
        return fromModuleId.equals(that.fromModuleId) && toModuleId.equals(that.toModuleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromModuleId, toModuleId);
    }

    @Override
    public String toString() {
        return String.format("ShipConnection{%s(%d,%d,%d) <-> %s(%d,%d,%d)}",
                fromModuleId, fromCell.getX(), fromCell.getY(), fromCell.getZ(),
                toModuleId, toCell.getX(), toCell.getY(), toCell.getZ());
    }
}
