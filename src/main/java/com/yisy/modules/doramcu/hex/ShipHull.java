package com.yisy.modules.doramcu.hex;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages the 3D hull module grid for a ship.
 * <p>
 * A ship's hull is represented as one or more {@link HullModule} instances arranged in a 3D grid.
 * Each module is a 10×10×10 M3space grid where components are installed. This class handles
 * module initialization, component installation/removal at M3space positions, hull integrity
 * calculations, and hull repair operations.
 * <p>
 * Component installation verifies that target cells are within the hull shape and in EMPTY state
 * before placing the component. Removal frees all occupied cells back to EMPTY state.
 */
public class ShipHull {

    private final Ship ship;
    private final List<HullModule> hullModules;
    private int[] moduleGridDimensions;
    private final Map<ShipComponent, List<M3space>> componentSpaces;

    // ---- Graph-based connections between modules ----
    private final Map<String, HullModule> moduleIdMap;
    private final List<ShipConnection> connections;

    /**
     * Creates hull management for the given ship.
     *
     * @param ship the ship this hull belongs to
     */
    public ShipHull(Ship ship) {
        this.ship = ship;
        this.hullModules = new ArrayList<>();
        this.componentSpaces = new HashMap<ShipComponent, List<M3space>>();
        this.moduleIdMap = new LinkedHashMap<>();
        this.connections = new ArrayList<>();
        initializeModules();
    }

    // ---- Initialization ----

    private void initializeModules() {
        ShipType shipType = ship.getShipType();
        this.moduleGridDimensions = shipType.getModuleGridDimensions();
        int moduleCount = shipType.getHullModuleCount();
        for (int i = 0; i < moduleCount; i++) {
            HullModule module = new HullModule(ship.getName() + "-Module-" + i);
            hullModules.add(module);
        }
    }

    // ---- Accessors ----

    public List<HullModule> getHullModules() {
        return List.copyOf(hullModules);
    }

    public int[] getModuleGridDimensions() {
        return moduleGridDimensions.clone();
    }

    public int getHullModuleCount() {
        return hullModules.size();
    }

    public Map<ShipComponent, List<M3space>> getComponentSpaces() {
        return Map.copyOf(componentSpaces);
    }

    // ---- Module management ----

    public void setHullModules(List<HullModule> modules) {
        if (modules == null) {
            throw new IllegalArgumentException("Modules must not be null");
        }
        hullModules.clear();
        hullModules.addAll(modules);
    }

    public HullModule getModuleAt(int mx, int my, int mz) {
        if (mx < 0 || mx >= moduleGridDimensions[0] ||
            my < 0 || my >= moduleGridDimensions[1] ||
            mz < 0 || mz >= moduleGridDimensions[2]) {
            return null;
        }
        int index = mz * moduleGridDimensions[0] * moduleGridDimensions[1] +
                    my * moduleGridDimensions[0] + mx;
        if (index < hullModules.size()) {
            return hullModules.get(index);
        }
        return null;
    }

    public HullModule getModuleAt(int index) {
        if (index < 0 || index >= hullModules.size()) {
            return null;
        }
        return hullModules.get(index);
    }

    // ---- Component installation ----

    /**
     * Finds the module and local coordinates for a global M3space position.
     */
    public Map<String, Object> findModuleForPosition(int globalX, int globalY, int globalZ) {
        int mx = globalX / HullModule.WIDTH;
        int my = globalY / HullModule.HEIGHT;
        int mz = globalZ / HullModule.DEPTH;
        int localX = globalX % HullModule.WIDTH;
        int localY = globalY % HullModule.HEIGHT;
        int localZ = globalZ % HullModule.DEPTH;

        HullModule module = getModuleAt(mx, my, mz);
        if (module == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("module", module);
        result.put("localX", localX);
        result.put("localY", localY);
        result.put("localZ", localZ);
        return result;
    }

    /**
     * Installs a component at specific M3space positions within any hull module.
     */
    public boolean installComponent(ShipComponent component, List<M3space> spaces) {
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null");
        }
        if (spaces == null || spaces.isEmpty()) {
            throw new IllegalArgumentException("Spaces must not be empty");
        }
        if (hullModules.isEmpty()) {
            throw new IllegalStateException("No hull modules assigned");
        }
        if (ship.getComponentsInternal().contains(component)) {
            throw new IllegalStateException("Component already installed");
        }
        if (ship.getOccupiedSlots() >= ship.getMaxComponentSlots()) {
            throw new IllegalStateException("No available component slots");
        }

        for (M3space space : spaces) {
            HullModule module = findModuleForSpace(space);
            if (module == null || !module.isShapeCell(space)) {
                throw new IllegalArgumentException("Space " + space + " is outside the hull shape");
            }
            M3space stored = module.get(space);
            if (stored == null || !stored.isInState(HexState.EMPTY)) {
                throw new IllegalArgumentException("Space " + space + " is not available");
            }
        }

        for (M3space space : spaces) {
            HullModule module = findModuleForSpace(space);
            if (module != null) {
                M3space stored = module.get(space);
                if (stored != null) {
                    stored.place(component);
                }
            }
        }

        // Directly add to ship's component list to avoid circular call
        ship.getComponentsInternal().add(component);
        componentSpaces.put(component, new ArrayList<>(spaces));
        return true;
    }

    /**
     * Installs a component at a single M3space position within the first hull module.
     */
    public boolean installComponent(ShipComponent component, int x, int y, int z) {
        if (hullModules.isEmpty()) {
            throw new IllegalStateException("No hull modules assigned");
        }
        HullModule module = hullModules.get(0);
        M3space space = module.get(x, y, z);
        if (space == null) {
            throw new IllegalArgumentException("Position (" + x + ", " + y + ", " + z + ") is out of bounds");
        }
        return installComponent(component, List.of(space));
    }

    /**
     * Installs a component at a single M3space position within a specific hull module.
     */
    public boolean installComponent(ShipComponent component, int moduleIndex, int x, int y, int z) {
        HullModule module = getModuleAt(moduleIndex);
        if (module == null) {
            throw new IllegalArgumentException("Module index " + moduleIndex + " is out of bounds");
        }
        M3space space = module.get(x, y, z);
        if (space == null) {
            throw new IllegalArgumentException("Position (" + x + ", " + y + ", " + z + ") is out of bounds");
        }
        return installComponent(component, List.of(space));
    }

    /**
     * Removes an installed component and frees its occupied M3space cells.
     */
    public boolean removeComponent(ShipComponent component) {
        List<M3space> spaces = componentSpaces.remove(component);
        if (spaces != null) {
            for (M3space space : spaces) {
                HullModule module = findModuleForSpace(space);
                if (module != null) {
                    M3space stored = module.get(space);
                    if (stored != null) {
                        stored.remove();
                    }
                }
            }
        }
        // Directly remove from ship's component list to avoid circular call
        boolean removed = ship.getComponentsInternal().remove(component);
        if (removed) {
            ship.recalculateStatsInternal();
        }
        return removed;
    }

    /**
     * Clears all components and frees their occupied M3space cells.
     */
    public void clearAllComponents() {
        for (List<M3space> spaces : componentSpaces.values()) {
            for (M3space space : spaces) {
                HullModule module = findModuleForSpace(space);
                if (module != null) {
                    M3space stored = module.get(space);
                    if (stored != null) {
                        stored.remove();
                    }
                }
            }
        }
        componentSpaces.clear();
    }

    /**
     * Returns the M3space cells occupied by the given component.
     */
    public List<M3space> getOccupiedSpaces(ShipComponent component) {
        return List.copyOf(componentSpaces.getOrDefault(component, List.of()));
    }

    /**
     * Returns all M3space cells currently occupied by any component.
     */
    public List<M3space> getAllOccupiedSpaces() {
        List<M3space> all = new ArrayList<>();
        for (List<M3space> spaces : componentSpaces.values()) {
            all.addAll(spaces);
        }
        return all;
    }

    /**
     * Checks if a given M3space position is available in the first module.
     */
    public boolean isSpaceAvailable(int x, int y, int z) {
        if (hullModules.isEmpty()) {
            return false;
        }
        HullModule module = hullModules.get(0);
        M3space space = module.get(x, y, z);
        if (space == null) {
            return false;
        }
        return module.isShapeCell(space) && space.isInState(HexState.EMPTY);
    }

    /**
     * Checks if a given M3space position is available in a specific module.
     */
    public boolean isSpaceAvailable(int moduleIndex, int x, int y, int z) {
        HullModule module = getModuleAt(moduleIndex);
        if (module == null) {
            return false;
        }
        M3space space = module.get(x, y, z);
        if (space == null) {
            return false;
        }
        return module.isShapeCell(space) && space.isInState(HexState.EMPTY);
    }

    /**
     * Returns the total number of available M3space cells across all hull modules.
     */
    public int getAvailableSpaces() {
        int count = 0;
        for (HullModule module : hullModules) {
            count += (int) module.getShapeCells().stream()
                    .filter(space -> space.isInState(HexState.EMPTY))
                    .count();
        }
        return count;
    }

    // ---- Hull Integrity ----

    public double getHullIntegrity() {
        if (hullModules.isEmpty()) {
            return ship.getHullHealthRatio();
        }
        int totalCells = 0;
        int destroyedCells = 0;
        for (HullModule module : hullModules) {
            totalCells += module.getShapeCellCount();
            destroyedCells += (int) module.getShapeCells().stream()
                    .filter(M3space::isDestroyed)
                    .count();
        }
        if (totalCells == 0) {
            return 0.0;
        }
        return 1.0 - ((double) destroyedCells / totalCells);
    }

    public int getDestroyedCellCount() {
        int count = 0;
        for (HullModule module : hullModules) {
            count += (int) module.getShapeCells().stream()
                    .filter(M3space::isDestroyed)
                    .count();
        }
        return count;
    }

    public int getDamagedCellCount() {
        int count = 0;
        for (HullModule module : hullModules) {
            count += (int) module.getShapeCells().stream()
                    .filter(space -> space.isDamaged() && !space.isDestroyed())
                    .count();
        }
        return count;
    }

    /**
     * Repairs all destroyed M3space cells across all hull modules.
     */
    public int repairHull() {
        int count = 0;
        for (HullModule module : hullModules) {
            for (M3space space : module.getShapeCells()) {
                if (space.isDestroyed() && space.repair()) {
                    count++;
                }
            }
        }
        return count;
    }

    // ---- Hull shape and material ----

    public int getHullCellCount() {
        int count = 0;
        for (HullModule module : hullModules) {
            count += module.getShapeCellCount();
        }
        return count;
    }

    public HullShape getHullShape() {
        if (hullModules.isEmpty()) {
            return HullShape.CUBE;
        }
        return hullModules.get(0).getShape();
    }

    public void setHullShape(HullShape shape) {
        if (shape == null) {
            throw new IllegalArgumentException("Shape must not be null");
        }
        for (HullModule module : hullModules) {
            module.setShape(shape);
        }
    }

    public Material getExteriorMaterial() {
        Map<Material, Long> breakdown = getHullMaterialBreakdown();
        if (breakdown.isEmpty()) {
            return Material.Aluminum;
        }
        return breakdown.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Material.Aluminum);
    }

    public void setExteriorMaterial(Material material) {
        for (HullModule module : hullModules) {
            module.setExteriorMaterial(material);
        }
    }

    public void setInteriorMaterial(Material material) {
        for (HullModule module : hullModules) {
            module.setInteriorMaterial(material);
        }
    }

    public Map<Material, Long> getHullMaterialBreakdown() {
        Map<Material, Long> breakdown = new java.util.EnumMap<>(Material.class);
        for (HullModule module : hullModules) {
            for (M3space cell : module.getExteriorCells()) {
                Material mat = cell.getMaterial();
                breakdown.merge(mat, 1L, Long::sum);
            }
        }
        return Map.copyOf(breakdown);
    }

    // ---- Internal helper ----

    private HullModule findModuleForSpace(M3space space) {
        for (HullModule module : hullModules) {
            for (M3space stored : module.getAll()) {
                if (stored == space) {
                    return module;
                }
            }
        }
        return null;
    }

    // ---- Graph-based Connection Management ----

    /**
     * Registers a module with a unique ID for graph-based connections.
     * <p>
     * This enables pathfinding and adjacency queries between modules.
     * </p>
     *
     * @param moduleId the unique identifier for this module
     * @param module the HullModule instance
     */
    public void registerModule(String moduleId, HullModule module) {
        if (moduleId == null || moduleId.isBlank()) {
            throw new IllegalArgumentException("moduleId must not be null or blank");
        }
        if (module == null) {
            throw new IllegalArgumentException("module must not be null");
        }
        moduleIdMap.put(moduleId, module);
    }

    /**
     * Returns the module with the given ID, or null if not found.
     *
     * @param moduleId the module ID
     * @return the HullModule instance, or null
     */
    public HullModule getModuleById(String moduleId) {
        return moduleIdMap.get(moduleId);
    }

    /**
     * @return unmodifiable map of module IDs to HullModules
     */
    public Map<String, HullModule> getModuleIdMap() {
        return Collections.unmodifiableMap(moduleIdMap);
    }

    /**
     * Connects two modules at specific passage cells.
     * <p>
     * Creates a bidirectional connection that enables pathfinding and
     * damage propagation between modules.
     * </p>
     *
     * @param fromModuleId the source module ID
     * @param fromCell the passage cell in the source module
     * @param toModuleId the target module ID
     * @param toCell the passage cell in the target module
     * @return the created connection
     */
    public ShipConnection connectModules(String fromModuleId, M3space fromCell, String toModuleId, M3space toCell) {
        HullModule fromModule = moduleIdMap.get(fromModuleId);
        HullModule toModule = moduleIdMap.get(toModuleId);
        if (fromModule == null) {
            throw new IllegalArgumentException("Module not found: " + fromModuleId);
        }
        if (toModule == null) {
            throw new IllegalArgumentException("Module not found: " + toModuleId);
        }
        ShipConnection connection = new ShipConnection(fromModuleId, fromCell, toModuleId, toCell);
        connections.add(connection);
        return connection;
    }

    /**
     * Removes a connection between two modules.
     *
     * @param connection the connection to remove
     * @return true if removed
     */
    public boolean removeConnection(ShipConnection connection) {
        return connections.remove(connection);
    }

    /**
     * @return unmodifiable list of all connections
     */
    public List<ShipConnection> getConnections() {
        return Collections.unmodifiableList(connections);
    }

    /**
     * Returns all modules directly connected to the given module.
     *
     * @param moduleId the module ID
     * @return map of adjacent module IDs to their HullModule instances
     */
    public Map<String, HullModule> getAdjacentModules(String moduleId) {
        Map<String, HullModule> adjacent = new LinkedHashMap<>();
        for (ShipConnection conn : connections) {
            String otherId = conn.getOtherModuleId(moduleId);
            if (otherId != null && !adjacent.containsKey(otherId)) {
                HullModule otherModule = moduleIdMap.get(otherId);
                if (otherModule != null) {
                    adjacent.put(otherId, otherModule);
                }
            }
        }
        return adjacent;
    }

    /**
     * Returns all connections involving the given module.
     *
     * @param moduleId the module ID
     * @return list of connections involving this module
     */
    public List<ShipConnection> getModuleConnections(String moduleId) {
        return connections.stream()
                .filter(conn -> conn.involvesModule(moduleId))
                .collect(Collectors.toList());
    }

    /**
     * Finds a path between two modules using BFS across connections.
     * <p>
     * Returns the sequence of module IDs from the start module to the end module.
     * Useful for crew movement simulation, damage propagation analysis,
     * and boarding mechanics.
     * </p>
     *
     * @param fromModuleId the starting module ID
     * @param toModuleId the target module ID
     * @return list of module IDs forming the path, or null if no path exists
     */
    public List<String> findPath(String fromModuleId, String toModuleId) {
        if (!moduleIdMap.containsKey(fromModuleId)) {
            throw new IllegalArgumentException("Module not found: " + fromModuleId);
        }
        if (!moduleIdMap.containsKey(toModuleId)) {
            throw new IllegalArgumentException("Module not found: " + toModuleId);
        }
        if (fromModuleId.equals(toModuleId)) {
            return Collections.singletonList(fromModuleId);
        }

        // BFS
        Queue<String> queue = new LinkedList<>();
        Map<String, String> visited = new HashMap<>();
        queue.add(fromModuleId);
        visited.put(fromModuleId, null);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            Map<String, HullModule> adjacent = getAdjacentModules(current);
            for (String neighborId : adjacent.keySet()) {
                if (!visited.containsKey(neighborId)) {
                    visited.put(neighborId, current);
                    if (neighborId.equals(toModuleId)) {
                        // Reconstruct path
                        List<String> path = new ArrayList<>();
                        String node = toModuleId;
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
        return null;  // No path found
    }

    /**
     * Checks if two modules are connected (directly or indirectly).
     *
     * @param fromModuleId the starting module ID
     * @param toModuleId the target module ID
     * @return true if a path exists between the modules
     */
    public boolean isConnected(String fromModuleId, String toModuleId) {
        return findPath(fromModuleId, toModuleId) != null;
    }

    /**
     * Returns the set of reachable module IDs from the given module.
     * <p>
     * Useful for breach containment analysis: if a module is breached,
     * which other modules can the breach spread to?
     * </p>
     *
     * @param moduleId the starting module ID
     * @return set of reachable module IDs (including the starting module)
     */
    public Set<String> getReachableModules(String moduleId) {
        Set<String> reachable = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(moduleId);
        reachable.add(moduleId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            Map<String, HullModule> adjacent = getAdjacentModules(current);
            for (String neighborId : adjacent.keySet()) {
                if (reachable.add(neighborId)) {
                    queue.add(neighborId);
                }
            }
        }
        return reachable;
    }

    /**
     * Returns the number of isolated module groups (connected components).
     * <p>
     * A value of 1 means all modules are connected. Higher values indicate
     * isolated sections of the ship hull.
     * </p>
     *
     * @return the number of connected components
     */
    public int getConnectedComponentCount() {
        if (moduleIdMap.isEmpty()) return 0;
        Set<String> visited = new HashSet<>();
        int components = 0;

        for (String moduleId : moduleIdMap.keySet()) {
            if (!visited.contains(moduleId)) {
                components++;
                // BFS from this module
                Queue<String> queue = new LinkedList<>();
                queue.add(moduleId);
                visited.add(moduleId);
                while (!queue.isEmpty()) {
                    String current = queue.poll();
                    Map<String, HullModule> adjacent = getAdjacentModules(current);
                    for (String neighborId : adjacent.keySet()) {
                        if (visited.add(neighborId)) {
                            queue.add(neighborId);
                        }
                    }
                }
            }
        }
        return components;
    }
}
