package com.yisy.modules.doramcu.hex.dungeon;

import com.yisy.modules.doramcu.hex.M3space;

import java.util.Objects;

/**
 * Represents a connection (doorway) between two rooms in a {@link Dungeon}.
 * <p>
 * A connection links a specific cell in one room to a specific cell in another room,
 * forming the edges of the dungeon graph. Each room is identified by a unique string ID,
 * and the doorway cells are the {@link M3space} positions where passage between rooms occurs.
 * </p>
 * <p>
 * Connections are bidirectional: traversing from room A to room B is the same as
 * traversing from room B to room A.
 * </p>
 */
public class DungeonConnection {

    private final String fromRoomId;
    private final M3space fromCell;
    private final String toRoomId;
    private final M3space toCell;

    /**
     * Creates a new connection between two rooms at specific doorway cells.
     *
     * @param fromRoomId the ID of the source room
     * @param fromCell the doorway cell in the source room
     * @param toRoomId the ID of the target room
     * @param toCell the doorway cell in the target room
     */
    public DungeonConnection(String fromRoomId, M3space fromCell, String toRoomId, M3space toCell) {
        if (fromRoomId == null || fromRoomId.isBlank()) {
            throw new IllegalArgumentException("fromRoomId must not be null or blank");
        }
        if (toRoomId == null || toRoomId.isBlank()) {
            throw new IllegalArgumentException("toRoomId must not be null or blank");
        }
        if (fromCell == null) {
            throw new IllegalArgumentException("fromCell must not be null");
        }
        if (toCell == null) {
            throw new IllegalArgumentException("toCell must not be null");
        }
        this.fromRoomId = fromRoomId;
        this.fromCell = fromCell;
        this.toRoomId = toRoomId;
        this.toCell = toCell;
    }

    /** @return the ID of the source room */
    public String getFromRoomId() {
        return fromRoomId;
    }

    /** @return the doorway cell in the source room */
    public M3space getFromCell() {
        return fromCell;
    }

    /** @return the ID of the target room */
    public String getToRoomId() {
        return toRoomId;
    }

    /** @return the doorway cell in the target room */
    public M3space getToCell() {
        return toCell;
    }

    /**
     * Returns the other room ID given one of the connected room IDs.
     * <p>
     * If the given roomId matches fromRoomId, returns toRoomId, and vice versa.
     * </p>
     *
     * @param roomId one of the connected room IDs
     * @return the other room ID, or null if roomId is not part of this connection
     */
    public String getOtherRoomId(String roomId) {
        if (fromRoomId.equals(roomId)) {
            return toRoomId;
        }
        if (toRoomId.equals(roomId)) {
            return fromRoomId;
        }
        return null;
    }

    /**
     * Returns the other room's doorway cell given one of the connected room IDs.
     *
     * @param roomId one of the connected room IDs
     * @return the other room's doorway cell, or null if roomId is not part of this connection
     */
    public M3space getOtherCell(String roomId) {
        if (fromRoomId.equals(roomId)) {
            return toCell;
        }
        if (toRoomId.equals(roomId)) {
            return fromCell;
        }
        return null;
    }

    /**
     * Checks whether this connection involves the given room.
     *
     * @param roomId the room ID to check
     * @return true if the room is part of this connection
     */
    public boolean involvesRoom(String roomId) {
        return fromRoomId.equals(roomId) || toRoomId.equals(roomId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DungeonConnection that = (DungeonConnection) o;
        return fromRoomId.equals(that.fromRoomId) && toRoomId.equals(that.toRoomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromRoomId, toRoomId);
    }

    @Override
    public String toString() {
        return String.format("DungeonConnection{%s(%d,%d,%d) <-> %s(%d,%d,%d)}",
                fromRoomId, fromCell.getX(), fromCell.getY(), fromCell.getZ(),
                toRoomId, toCell.getX(), toCell.getY(), toCell.getZ());
    }
}
