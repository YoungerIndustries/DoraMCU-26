package com.yisy.modules.doramcu.hex;

import java.util.*;

/**
 * Models a building as one or more {@link HullModule} instances representing floors.
 * <p>
 * A building uses the 3D hull model where:
 * <ul>
 *   <li><b>Z-axis = floors</b> (Z=0 = ground floor, Z=1 = second floor, etc.)</li>
 *   <li><b>X-Y plane = floor plan</b> (each cell = room section)</li>
 *   <li><b>Exterior cells = walls</b> (damageable)</li>
 *   <li><b>Interior cells = rooms</b> (compartmentalized)</li>
 * </ul>
 * </p>
 * <p>
 * Each floor is a separate HullModule, allowing different floor plans per level.
 * Compartments represent named rooms (e.g., "Lobby", "Server Room"), and doorways
 * define connections between adjacent cells.
 * </p>
 * <p>
 * Damage to exterior walls can breach into interior rooms, triggering compartment
 * secondary effects (fire, flood, depressurization).
 * </p>
 */
public class BuildingHull {

    private final String name;
    private final List<HullModule> floorModules;
    private final Map<String, Compartment> compartments;
    private final Map<String, List<M3space>> doorways;

    // ---- Graph-based connections between floors ----
    private final Map<String, HullModule> floorIdMap;
    private final List<BuildingConnection> connections;

    /**
     * Creates a new BuildingHull with the given name.
     *
     * @param name the name of the building
     */
    public BuildingHull(String name) {
        this.name = name;
        this.floorModules = new ArrayList<>();
        this.compartments = new HashMap<>();
        this.doorways = new HashMap<>();
        this.floorIdMap = new LinkedHashMap<>();
        this.connections = new ArrayList<>();
    }

    // ---- Accessors ----

    /** @return the building name */
    public String getName() {
        return name;
    }

    /** @return unmodifiable list of floor modules (index = floor number) */
    public List<HullModule> getFloorModules() {
        return List.copyOf(floorModules);
    }

    /** @return the number of floors */
    public int getFloorCount() {
        return floorModules.size();
    }

    /** @return unmodifiable map of compartments by name */
    public Map<String, Compartment> getCompartments() {
        return Map.copyOf(compartments);
    }

    /** @return unmodifiable map of doorways by name */
    public Map<String, List<M3space>> getDoorways() {
        return Map.copyOf(doorways);
    }

    // ---- Floor management ----

    /**
     * Adds a floor module at the end (next floor number).
     *
     * @param module the floor module to add
     */
    public void addFloor(HullModule module) {
        if (module == null) {
            throw new IllegalArgumentException("Floor module must not be null");
        }
        floorModules.add(module);
    }

    /**
     * Adds a floor module at a specific floor number.
     *
     * @param floor the floor number (0 = ground floor)
     * @param module the floor module
     */
    public void addFloorAt(int floor, HullModule module) {
        if (module == null) {
            throw new IllegalArgumentException("Floor module must not be null");
        }
        // Ensure the list is large enough
        while (floorModules.size() <= floor) {
            floorModules.add(null);
        }
        floorModules.set(floor, module);
    }

    /**
     * Creates and adds a new floor with the given dimensions.
     *
     * @param floor the floor number
     * @param width the floor width (X axis)
     * @param height the floor height (Y axis)
     * @param shape the shape of the floor (CUBE for full rectangle)
     * @return the created floor module
     */
    public HullModule createFloor(int floor, int width, int height, HullShape shape) {
        HullModule module = new HullModule(name + "-Floor-" + floor, width, height, 1);
        if (shape != null) {
            module.setShape(shape);
        }
        addFloorAt(floor, module);
        return module;
    }

    /**
     * Returns the floor module at the given floor number.
     *
     * @param floor the floor number (0 = ground floor)
     * @return the floor module, or null if not present
     */
    public HullModule getFloor(int floor) {
        if (floor < 0 || floor >= floorModules.size()) {
            return null;
        }
        return floorModules.get(floor);
    }

    // ---- Cell access ----

    /**
     * Retrieves a cell at the given floor and position.
     *
     * @param floor the floor number
     * @param x the x coordinate on the floor
     * @param y the y coordinate on the floor
     * @return the cell, or null if out of bounds
     */
    public M3space getCell(int floor, int x, int y) {
        HullModule floorModule = getFloor(floor);
        if (floorModule == null) {
            return null;
        }
        return floorModule.get(x, y, 0);
    }

    /**
     * Returns the compartment at the given floor and position.
     *
     * @param floor the floor number
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the compartment containing this cell, or null
     */
    public Compartment getCompartmentAt(int floor, int x, int y) {
        M3space cell = getCell(floor, x, y);
        if (cell == null) {
            return null;
        }
        for (Compartment compartment : compartments.values()) {
            if (compartment.getCells().contains(cell)) {
                return compartment;
            }
        }
        return null;
    }

    // ---- Compartment management ----

    /**
     * Adds a compartment to the building.
     *
     * @param compartment the compartment to add
     */
    public void addCompartment(Compartment compartment) {
        if (compartment == null) {
            throw new IllegalArgumentException("Compartment must not be null");
        }
        compartments.put(compartment.getName(), compartment);
    }

    /**
     * Removes a compartment by name.
     *
     * @param name the compartment name
     * @return the removed compartment, or null
     */
    public Compartment removeCompartment(String name) {
        return compartments.remove(name);
    }

    /**
     * Returns a compartment by name.
     *
     * @param name the compartment name
     * @return the compartment, or null
     */
    public Compartment getCompartment(String name) {
        return compartments.get(name);
    }

    /**
     * Returns all compartments of a specific type.
     *
     * @param type the compartment type
     * @return list of matching compartments
     */
    public List<Compartment> getCompartmentsByType(CompartmentType type) {
        List<Compartment> result = new ArrayList<>();
        for (Compartment compartment : compartments.values()) {
            if (compartment.getType() == type) {
                result.add(compartment);
            }
        }
        return result;
    }

    // ---- Doorway management ----

    /**
     * Adds a doorway connecting two cells.
     *
     * @param name the doorway name
     * @param cellA the first cell
     * @param cellB the second cell
     */
    public void addDoorway(String name, M3space cellA, M3space cellB) {
        if (name == null || cellA == null || cellB == null) {
            throw new IllegalArgumentException("Name and cells must not be null");
        }
        doorways.put(name, List.of(cellA, cellB));
    }

    /**
     * Removes a doorway by name.
     *
     * @param name the doorway name
     * @return true if removed
     */
    public boolean removeDoorway(String name) {
        return doorways.remove(name) != null;
    }

    /**
     * Checks if two cells are connected by a doorway.
     *
     * @param cellA the first cell
     * @param cellB the second cell
     * @return true if a doorway connects them
     */
    public boolean hasDoorwayBetween(M3space cellA, M3space cellB) {
        for (List<M3space> doorway : doorways.values()) {
            if (doorway.contains(cellA) && doorway.contains(cellB)) {
                return true;
            }
        }
        return false;
    }

    // ---- Damage operations ----

    /**
     * Applies damage to an exterior wall cell and checks for breach.
     *
     * @param floor the floor number
     * @param x the x coordinate
     * @param y the y coordinate
     * @param damage the damage amount
     * @return list of affected compartments
     */
    public List<Compartment> applyBreach(int floor, int x, int y, int damage) {
        List<Compartment> affected = new ArrayList<>();
        M3space cell = getCell(floor, x, y);
        if (cell == null) {
            return affected;
        }

        cell.damage(damage);

        if (cell.isDestroyed()) {
            HullModule floorModule = getFloor(floor);
            if (floorModule != null && floorModule.isExteriorCell(cell)) {
                // Propagate damage to adjacent interior cells
                List<M3space> damagedCells = DamagePropagation.propagate(floorModule, cell);

                // Find affected compartments
                for (M3space damagedCell : damagedCells) {
                    for (Compartment compartment : compartments.values()) {
                        if (compartment.getCells().contains(damagedCell) && !affected.contains(compartment)) {
                            affected.add(compartment);
                        }
                    }
                }
            }
        }

        return affected;
    }

    /**
     * Returns all compartments affected by a hull breach at the given cell.
     *
     * @param floor the floor number
     * @param x the x coordinate
     * @param y the y coordinate
     * @return list of affected compartments
     */
    public List<Compartment> getAffectedCompartments(int floor, int x, int y) {
        M3space cell = getCell(floor, x, y);
        if (cell == null) {
            return new ArrayList<>();
        }
        HullModule floorModule = getFloor(floor);
        if (floorModule == null) {
            return new ArrayList<>();
        }
        return DamagePropagation.getAffectedCompartments(floorModule, cell, compartments);
    }

    // ---- Integrity calculations ----

    /**
     * Returns the overall building integrity (0.0 to 1.0).
     * <p>
     * Calculated as the average of all compartment integrities.
     * </p>
     *
     * @return integrity ratio
     */
    public double getOverallIntegrity() {
        if (compartments.isEmpty()) {
            return 1.0;
        }
        double total = 0;
        for (Compartment compartment : compartments.values()) {
            total += compartment.getIntegrityRatio();
        }
        return total / compartments.size();
    }

    /**
     * Returns the integrity of a specific floor (0.0 to 1.0).
     *
     * @param floor the floor number
     * @return integrity ratio, or 1.0 if floor has no compartments
     */
    public double getFloorIntegrity(int floor) {
        HullModule floorModule = getFloor(floor);
        if (floorModule == null) {
            return 0.0;
        }

        int totalCells = floorModule.getShapeCellCount();
        if (totalCells == 0) {
            return 1.0;
        }

        int destroyedCells = 0;
        for (M3space cell : floorModule.getShapeCells()) {
            if (cell.isDestroyed()) {
                destroyedCells++;
            }
        }
        return 1.0 - ((double) destroyedCells / totalCells);
    }

    /**
     * @return true if the building is structurally sound (integrity > 0.5)
     */
    public boolean isStructurallySound() {
        return getOverallIntegrity() > 0.5;
    }

    /**
     * @return true if the building is critically damaged (integrity <= 0.25)
     */
    public boolean isCriticallyDamaged() {
        return getOverallIntegrity() <= 0.25;
    }

    // ---- Repair ----

    /**
     * Repairs all damaged cells and compartments in the building.
     *
     * @return total number of cells repaired
     */
    public int repairAll() {
        int totalRepaired = 0;

        for (HullModule floorModule : floorModules) {
            if (floorModule == null) continue;
            for (M3space cell : floorModule.getShapeCells()) {
                if (cell.isDestroyed()) {
                    if (cell.repair()) {
                        totalRepaired++;
                    }
                } else if (cell.isDamaged()) {
                    cell.setHitpoints(cell.getMaxHitpoints());
                    totalRepaired++;
                }
            }
        }

        // Clear compartment secondary effects
        for (Compartment compartment : compartments.values()) {
            compartment.clearSecondaryEffects();
        }

        return totalRepaired;
    }

    // ---- Statistics ----

    /**
     * Returns the total number of cells across all floors.
     *
     * @return total cell count
     */
    public int getTotalCellCount() {
        int count = 0;
        for (HullModule floorModule : floorModules) {
            if (floorModule != null) {
                count += floorModule.getShapeCellCount();
            }
        }
        return count;
    }

    /**
     * Returns the total number of destroyed cells.
     *
     * @return destroyed cell count
     */
    public int getDestroyedCellCount() {
        int count = 0;
        for (HullModule floorModule : floorModules) {
            if (floorModule != null) {
                for (M3space cell : floorModule.getShapeCells()) {
                    if (cell.isDestroyed()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Returns the total number of damaged (but not destroyed) cells.
     *
     * @return damaged cell count
     */
    public int getDamagedCellCount() {
        int count = 0;
        for (HullModule floorModule : floorModules) {
            if (floorModule != null) {
                for (M3space cell : floorModule.getShapeCells()) {
                    if (cell.isDamaged() && !cell.isDestroyed()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    // ---- Graph-based Connection Management ----

    /**
     * Registers a floor with a unique ID for graph-based connections.
     *
     * @param floorId the unique identifier for this floor
     * @param module the HullModule instance
     */
    public void registerFloor(String floorId, HullModule module) {
        if (floorId == null || floorId.isBlank()) {
            throw new IllegalArgumentException("floorId must not be null or blank");
        }
        if (module == null) {
            throw new IllegalArgumentException("module must not be null");
        }
        floorIdMap.put(floorId, module);
    }

    /**
     * Returns the floor with the given ID, or null if not found.
     *
     * @param floorId the floor ID
     * @return the HullModule instance, or null
     */
    public HullModule getFloorById(String floorId) {
        return floorIdMap.get(floorId);
    }

    /**
     * Connects two floors at specific doorway cells (e.g., stairs, elevators).
     *
     * @param fromFloorId the source floor ID
     * @param fromCell the doorway cell in the source floor
     * @param toFloorId the target floor ID
     * @param toCell the doorway cell in the target floor
     * @return the created connection
     */
    public BuildingConnection connectFloors(String fromFloorId, M3space fromCell, String toFloorId, M3space toCell) {
        HullModule fromFloor = floorIdMap.get(fromFloorId);
        HullModule toFloor = floorIdMap.get(toFloorId);
        if (fromFloor == null) {
            throw new IllegalArgumentException("Floor not found: " + fromFloorId);
        }
        if (toFloor == null) {
            throw new IllegalArgumentException("Floor not found: " + toFloorId);
        }
        BuildingConnection connection = new BuildingConnection(fromFloorId, fromCell, toFloorId, toCell);
        connections.add(connection);
        return connection;
    }

    /**
     * @return unmodifiable list of all connections
     */
    public List<BuildingConnection> getConnections() {
        return Collections.unmodifiableList(connections);
    }

    /**
     * Returns all floors directly connected to the given floor.
     *
     * @param floorId the floor ID
     * @return map of adjacent floor IDs to their HullModule instances
     */
    public Map<String, HullModule> getAdjacentFloors(String floorId) {
        Map<String, HullModule> adjacent = new LinkedHashMap<>();
        for (BuildingConnection conn : connections) {
            String otherId = conn.getOtherFloorId(floorId);
            if (otherId != null && !adjacent.containsKey(otherId)) {
                HullModule otherFloor = floorIdMap.get(otherId);
                if (otherFloor != null) {
                    adjacent.put(otherId, otherFloor);
                }
            }
        }
        return adjacent;
    }

    /**
     * Finds a path between two floors using BFS across connections.
     * <p>
     * Returns the sequence of floor IDs from the start floor to the end floor.
     * </p>
     *
     * @param fromFloorId the starting floor ID
     * @param toFloorId the target floor ID
     * @return list of floor IDs forming the path, or null if no path exists
     */
    public List<String> findPath(String fromFloorId, String toFloorId) {
        if (!floorIdMap.containsKey(fromFloorId)) {
            throw new IllegalArgumentException("Floor not found: " + fromFloorId);
        }
        if (!floorIdMap.containsKey(toFloorId)) {
            throw new IllegalArgumentException("Floor not found: " + toFloorId);
        }
        if (fromFloorId.equals(toFloorId)) {
            return Collections.singletonList(fromFloorId);
        }

        // BFS
        Queue<String> queue = new LinkedList<>();
        Map<String, String> visited = new HashMap<>();
        queue.add(fromFloorId);
        visited.put(fromFloorId, null);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            Map<String, HullModule> adjacent = getAdjacentFloors(current);
            for (String neighborId : adjacent.keySet()) {
                if (!visited.containsKey(neighborId)) {
                    visited.put(neighborId, current);
                    if (neighborId.equals(toFloorId)) {
                        List<String> path = new ArrayList<>();
                        String node = toFloorId;
                        while (node != null) {
                            path.add(node);
                            node = visited.get(node);
                        }
                        Collections.reverse(path);
                        return path;
                    }
                    queue.add(neighborId);
                }
            }
        }
        return null;
    }

    /**
     * Checks if two floors are connected (directly or indirectly).
     */
    public boolean isConnected(String fromFloorId, String toFloorId) {
        return findPath(fromFloorId, toFloorId) != null;
    }

    // ---- Object contract ----

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuildingHull that = (BuildingHull) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return String.format("BuildingHull{name='%s', floors=%d, compartments=%d, doorways=%d, integrity=%d%%}",
                name, getFloorCount(), compartments.size(), doorways.size(),
                (int) Math.round(getOverallIntegrity() * 100));
    }

    // ---- Nested classes ----

    /**
     * Represents a connection (stairs, elevator, ramp) between two floors.
     */
    public static class BuildingConnection {
        private final String fromFloorId;
        private final M3space fromCell;
        private final String toFloorId;
        private final M3space toCell;

        public BuildingConnection(String fromFloorId, M3space fromCell, String toFloorId, M3space toCell) {
            if (fromFloorId == null || fromFloorId.isBlank()) {
                throw new IllegalArgumentException("fromFloorId must not be null or blank");
            }
            if (toFloorId == null || toFloorId.isBlank()) {
                throw new IllegalArgumentException("toFloorId must not be null or blank");
            }
            if (fromCell == null) {
                throw new IllegalArgumentException("fromCell must not be null");
            }
            if (toCell == null) {
                throw new IllegalArgumentException("toCell must not be null");
            }
            this.fromFloorId = fromFloorId;
            this.fromCell = fromCell;
            this.toFloorId = toFloorId;
            this.toCell = toCell;
        }

        public String getFromFloorId() { return fromFloorId; }
        public M3space getFromCell() { return fromCell; }
        public String getToFloorId() { return toFloorId; }
        public M3space getToCell() { return toCell; }

        public String getOtherFloorId(String floorId) {
            if (fromFloorId.equals(floorId)) return toFloorId;
            if (toFloorId.equals(floorId)) return fromFloorId;
            return null;
        }

        public M3space getOtherCell(String floorId) {
            if (fromFloorId.equals(floorId)) return toCell;
            if (toFloorId.equals(floorId)) return fromCell;
            return null;
        }

        public boolean involvesFloor(String floorId) {
            return fromFloorId.equals(floorId) || toFloorId.equals(floorId);
        }

        @Override
        public String toString() {
            return String.format("BuildingConnection{%s(%d,%d,%d) <-> %s(%d,%d,%d)}",
                    fromFloorId, fromCell.getX(), fromCell.getY(), fromCell.getZ(),
                    toFloorId, toCell.getX(), toCell.getY(), toCell.getZ());
        }
    }
}
