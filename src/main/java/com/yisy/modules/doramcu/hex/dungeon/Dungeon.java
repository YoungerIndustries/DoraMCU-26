package com.yisy.modules.doramcu.hex.dungeon;

import com.yisy.modules.doramcu.hex.Compartment;
import com.yisy.modules.doramcu.hex.HullModule;
import com.yisy.modules.doramcu.hex.M3space;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a dungeon as a graph of connected {@link HullModule} rooms.
 * <p>
 * Each room is a separate HullModule instance (instantiated from a {@link DungeonModuleTemplate}),
 * connected at specific cell positions (doorways) via {@link DungeonConnection} objects.
 * This is the hybrid approach: grid-based rooms connected as a graph.
 * </p>
 * <p>
 * The dungeon tracks room positions in 3D space, manages connections between rooms,
 * and provides pathfinding, integrity tracking, and compartment management.
 * </p>
 */
public class Dungeon {

    private final String name;
    private final String description;
    private final Map<String, HullModule> rooms;
    private final List<DungeonConnection> connections;
    private final Map<String, Point3D> roomPositions;
    private int difficulty;

    /**
     * Creates a new dungeon with the given name and description.
     *
     * @param name the dungeon name
     * @param description the dungeon description
     */
    public Dungeon(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Dungeon name must not be null or blank");
        }
        this.name = name;
        this.description = description;
        this.rooms = new LinkedHashMap<>();
        this.connections = new ArrayList<>();
        this.roomPositions = new HashMap<>();
        this.difficulty = 1;
    }

    // ---- Room management ----

    /**
     * Adds a room to this dungeon with the given ID.
     *
     * @param roomId the unique identifier for this room
     * @param module the HullModule instance for this room
     * @throws IllegalArgumentException if roomId is already used
     */
    public void addRoom(String roomId, HullModule module) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId must not be null or blank");
        }
        if (module == null) {
            throw new IllegalArgumentException("module must not be null");
        }
        if (rooms.containsKey(roomId)) {
            throw new IllegalArgumentException("Room ID already exists: " + roomId);
        }
        rooms.put(roomId, module);
    }

    /**
     * Removes a room from this dungeon and all its connections.
     *
     * @param roomId the room ID to remove
     * @return true if the room was removed
     */
    public boolean removeRoom(String roomId) {
        if (!rooms.containsKey(roomId)) return false;
        rooms.remove(roomId);
        roomPositions.remove(roomId);
        connections.removeIf(conn -> conn.involvesRoom(roomId));
        return true;
    }

    /**
     * Sets the 3D position of a room in dungeon space.
     *
     * @param roomId the room ID
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate (floor level)
     */
    public void setRoomPosition(String roomId, int x, int y, int z) {
        if (!rooms.containsKey(roomId)) {
            throw new IllegalArgumentException("Room not found: " + roomId);
        }
        roomPositions.put(roomId, new Point3D(x, y, z));
    }

    /**
     * Returns the 3D position of a room, or null if not set.
     *
     * @param roomId the room ID
     * @return the room's position, or null
     */
    public Point3D getRoomPosition(String roomId) {
        return roomPositions.get(roomId);
    }

    /**
     * @return the room with the given ID, or null if not found
     */
    public HullModule getRoom(String roomId) {
        return rooms.get(roomId);
    }

    /**
     * @return unmodifiable map of all rooms
     */
    public Map<String, HullModule> getRooms() {
        return Collections.unmodifiableMap(rooms);
    }

    /**
     * @return the number of rooms in this dungeon
     */
    public int getRoomCount() {
        return rooms.size();
    }

    // ---- Connection management ----

    /**
     * Connects two rooms at specific doorway cells.
     *
     * @param fromRoomId the source room ID
     * @param fromCell the doorway cell in the source room
     * @param toRoomId the target room ID
     * @param toCell the doorway cell in the target room
     * @return the created connection
     */
    public DungeonConnection connectRooms(String fromRoomId, M3space fromCell, String toRoomId, M3space toCell) {
        if (!rooms.containsKey(fromRoomId)) {
            throw new IllegalArgumentException("Room not found: " + fromRoomId);
        }
        if (!rooms.containsKey(toRoomId)) {
            throw new IllegalArgumentException("Room not found: " + toRoomId);
        }
        DungeonConnection connection = new DungeonConnection(fromRoomId, fromCell, toRoomId, toCell);
        connections.add(connection);
        return connection;
    }

    /**
     * Removes a connection between two rooms.
     *
     * @param connection the connection to remove
     * @return true if removed
     */
    public boolean removeConnection(DungeonConnection connection) {
        return connections.remove(connection);
    }

    /**
     * @return unmodifiable list of all connections
     */
    public List<DungeonConnection> getConnections() {
        return Collections.unmodifiableList(connections);
    }

    /**
     * Returns all rooms directly connected to the given room.
     *
     * @param roomId the room ID
     * @return map of adjacent room IDs to their HullModule instances
     */
    public Map<String, HullModule> getAdjacentRooms(String roomId) {
        Map<String, HullModule> adjacent = new LinkedHashMap<>();
        for (DungeonConnection conn : connections) {
            String otherId = conn.getOtherRoomId(roomId);
            if (otherId != null && !adjacent.containsKey(otherId)) {
                HullModule otherRoom = rooms.get(otherId);
                if (otherRoom != null) {
                    adjacent.put(otherId, otherRoom);
                }
            }
        }
        return adjacent;
    }

    /**
     * Returns all connections involving the given room.
     *
     * @param roomId the room ID
     * @return list of connections involving this room
     */
    public List<DungeonConnection> getRoomConnections(String roomId) {
        return connections.stream()
                .filter(conn -> conn.involvesRoom(roomId))
                .collect(Collectors.toList());
    }

    // ---- Pathfinding ----

    /**
     * Finds a path between two rooms using BFS across connections.
     * <p>
     * Returns the sequence of room IDs from the start room to the end room.
     * </p>
     *
     * @param fromRoomId the starting room ID
     * @param toRoomId the target room ID
     * @return list of room IDs forming the path, or null if no path exists
     */
    public List<String> findPath(String fromRoomId, String toRoomId) {
        if (!rooms.containsKey(fromRoomId)) {
            throw new IllegalArgumentException("Room not found: " + fromRoomId);
        }
        if (!rooms.containsKey(toRoomId)) {
            throw new IllegalArgumentException("Room not found: " + toRoomId);
        }
        if (fromRoomId.equals(toRoomId)) {
            return Collections.singletonList(fromRoomId);
        }

        // BFS
        Queue<String> queue = new LinkedList<>();
        Map<String, String> visited = new HashMap<>();
        queue.add(fromRoomId);
        visited.put(fromRoomId, null);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            Map<String, HullModule> adjacent = getAdjacentRooms(current);
            for (String neighborId : adjacent.keySet()) {
                if (!visited.containsKey(neighborId)) {
                    visited.put(neighborId, current);
                    if (neighborId.equals(toRoomId)) {
                        // Reconstruct path
                        List<String> path = new ArrayList<>();
                        String node = toRoomId;
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

    // ---- Cell lookup ----

    /**
     * Retrieves a cell at the given coordinates within a specific room.
     *
     * @param roomId the room ID
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @return the cell, or null if not found
     */
    public M3space getCellAt(String roomId, int x, int y, int z) {
        HullModule room = rooms.get(roomId);
        if (room == null) return null;
        return room.getCell(x, y, z);
    }

    // ---- Compartment management ----

    /**
     * Returns all compartments across all rooms in the dungeon.
     *
     * @return map of room ID to list of compartments
     */
    public Map<String, List<Compartment>> getCompartments() {
        Map<String, List<Compartment>> result = new LinkedHashMap<>();
        for (Map.Entry<String, HullModule> entry : rooms.entrySet()) {
            List<Compartment> comps = entry.getValue().getCompartments();
            if (!comps.isEmpty()) {
                result.put(entry.getKey(), comps);
            }
        }
        return result;
    }

    /**
     * Returns the compartment containing a cell at the given coordinates.
     *
     * @param roomId the room ID
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @return the compartment, or null if not found
     */
    public Compartment getCompartmentAt(String roomId, int x, int y, int z) {
        HullModule room = rooms.get(roomId);
        if (room == null) return null;
        M3space cell = room.getCell(x, y, z);
        if (cell == null) return null;
        return room.getCompartmentAt(cell);
    }

    // ---- Integrity ----

    /**
     * Calculates the overall dungeon integrity as a ratio (0.0 to 1.0).
     * <p>
     * Integrity is the average of all room compartment integrities.
     * Rooms with no compartments contribute 1.0 (fully intact).
     * </p>
     *
     * @return overall integrity ratio
     */
    public double getIntegrity() {
        if (rooms.isEmpty()) return 1.0;
        double totalIntegrity = 0.0;
        for (HullModule room : rooms.values()) {
            List<Compartment> compartments = room.getCompartments();
            if (compartments.isEmpty()) {
                totalIntegrity += 1.0;
            } else {
                for (Compartment compartment : compartments) {
                    totalIntegrity += compartment.getIntegrityRatio();
                }
                totalIntegrity /= compartments.size();
            }
        }
        return totalIntegrity / rooms.size();
    }

    // ---- Accessors ----

    /** @return the dungeon name */
    public String getName() {
        return name;
    }

    /** @return the dungeon description */
    public String getDescription() {
        return description;
    }

    /** @return the difficulty rating (1-10) */
    public int getDifficulty() {
        return difficulty;
    }

    /**
     * Sets the difficulty rating.
     *
     * @param difficulty the difficulty (1-10)
     */
    public void setDifficulty(int difficulty) {
        this.difficulty = Math.max(1, Math.min(10, difficulty));
    }

    @Override
    public String toString() {
        return String.format("Dungeon{name='%s', rooms=%d, connections=%d, difficulty=%d}",
                name, rooms.size(), connections.size(), difficulty);
    }

    // ---- Nested classes ----

    /**
     * Simple 3D point for room positioning in dungeon space.
     */
    public static class Point3D {
        private final int x, y, z;

        public Point3D(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point3D point3D = (Point3D) o;
            return x == point3D.x && y == point3D.y && z == point3D.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }

        @Override
        public String toString() {
            return String.format("(%d,%d,%d)", x, y, z);
        }
    }
}
