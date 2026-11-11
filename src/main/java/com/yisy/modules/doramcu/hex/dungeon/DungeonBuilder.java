package com.yisy.modules.doramcu.hex.dungeon;

import com.yisy.modules.doramcu.hex.HullModule;
import com.yisy.modules.doramcu.hex.M3space;

import java.util.*;

/**
 * Service that assembles {@link Dungeon} instances from {@link DungeonModuleTemplate} definitions.
 * <p>
 * The builder reconstructs {@link HullModule} instances from serialized template data,
 * including shape definitions, compartments, and connection points. It then connects
 * rooms at doorway positions to form the complete dungeon graph.
 * </p>
 * <p>
 * Usage:
 * <pre>
 * DungeonBuilder builder = new DungeonBuilder();
 * Dungeon dungeon = builder.assemble(List.of(template1, template2), connections);
 * </pre>
 * </p>
 */
public class DungeonBuilder {

    private int roomCounter;

    /**
     * Creates a new DungeonBuilder.
     */
    public DungeonBuilder() {
        this.roomCounter = 0;
    }

    /**
     * Builds a single room from a template, instantiating a new {@link HullModule}.
     * <p>
     * Reconstructs the shape from serialized cell coordinates and sets up compartments.
     * </p>
     *
     * @param template the module template to instantiate
     * @return a new HullModule instance
     */
    public HullModule buildFromTemplate(DungeonModuleTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("Template must not be null");
        }
        return template.toHullModule();
    }

    /**
     * Assembles a multi-room dungeon from templates and connection definitions.
     * <p>
     * Each template is instantiated as a separate room. Connections define how rooms
     * are linked at doorway cells.
     * </p>
     *
     * @param dungeonName the name of the dungeon
     * @param dungeonDescription the dungeon description
     * @param templates list of module templates to instantiate
     * @param connections list of connection definitions (fromRoomIndex, fromCell, toRoomIndex, toCell)
     * @return the assembled dungeon
     */
    public Dungeon assemble(String dungeonName, String dungeonDescription,
                            List<DungeonModuleTemplate> templates,
                            List<ConnectionDefinition> connections) {
        Dungeon dungeon = new Dungeon(dungeonName, dungeonDescription);

        // Instantiate all rooms
        Map<Integer, String> indexToRoomId = new HashMap<>();
        for (int i = 0; i < templates.size(); i++) {
            DungeonModuleTemplate template = templates.get(i);
            HullModule module = buildFromTemplate(template);
            String roomId = generateRoomId(template.getName());
            dungeon.addRoom(roomId, module);
            indexToRoomId.put(i, roomId);
        }

        // Create connections
        if (connections != null) {
            for (ConnectionDefinition conn : connections) {
                String fromRoomId = indexToRoomId.get(conn.fromRoomIndex);
                String toRoomId = indexToRoomId.get(conn.toRoomIndex);
                if (fromRoomId != null && toRoomId != null) {
                    dungeon.connectRooms(fromRoomId, conn.fromCell, toRoomId, conn.toCell);
                }
            }
        }

        return dungeon;
    }

    /**
     * Connects two rooms at doorway cells within an existing dungeon.
     * <p>
     * This is a convenience method that finds the doorway cells in each room
     * and creates the connection.
     * </p>
     *
     * @param dungeon the dungeon to modify
     * @param fromRoomId the source room ID
     * @param fromCell the doorway cell in the source room
     * @param toRoomId the target room ID
     * @param toCell the doorway cell in the target room
     */
    public void connectAtDoorway(Dungeon dungeon, String fromRoomId, M3space fromCell,
                                  String toRoomId, M3space toCell) {
        dungeon.connectRooms(fromRoomId, fromCell, toRoomId, toCell);
    }

    /**
     * Generates a unique room ID based on the template name.
     *
     * @param templateName the template name
     * @return a unique room ID
     */
    private String generateRoomId(String templateName) {
        roomCounter++;
        return templateName.toLowerCase().replace(" ", "_") + "_" + roomCounter;
    }

    /**
     * Defines a connection between two rooms by template index.
     * <p>
     * Used during assembly to specify which rooms connect at which cells.
     * </p>
     */
    public static class ConnectionDefinition {
        private final int fromRoomIndex;
        private final M3space fromCell;
        private final int toRoomIndex;
        private final M3space toCell;

        /**
         * Creates a connection definition.
         *
         * @param fromRoomIndex index of the source room in the templates list
         * @param fromCell the doorway cell in the source room
         * @param toRoomIndex index of the target room in the templates list
         * @param toCell the doorway cell in the target room
         */
        public ConnectionDefinition(int fromRoomIndex, M3space fromCell,
                                     int toRoomIndex, M3space toCell) {
            this.fromRoomIndex = fromRoomIndex;
            this.fromCell = fromCell;
            this.toRoomIndex = toRoomIndex;
            this.toCell = toCell;
        }

        public int getFromRoomIndex() { return fromRoomIndex; }
        public M3space getFromCell() { return fromCell; }
        public int getToRoomIndex() { return toRoomIndex; }
        public M3space getToCell() { return toCell; }
    }
}
