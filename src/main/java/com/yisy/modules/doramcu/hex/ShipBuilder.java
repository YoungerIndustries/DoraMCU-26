package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.hex.dungeon.ShipModuleTemplate;

import java.util.*;

/**
 * Assembles {@link Ship} instances from {@link ShipModuleTemplate} definitions.
 * <p>
 * The builder reconstructs {@link HullModule} instances from serialized template data,
 * including shape definitions, compartments, and connection points. It then connects
 * modules at passage positions to form the complete ship graph.
 * </p>
 * <p>
 * Usage:
 * <pre>
 * ShipBuilder builder = new ShipBuilder();
 * Ship ship = builder.assemble("My Ship", ShipType.FRIGATE, List.of(template1, template2), connections);
 * </pre>
 * </p>
 */
public class ShipBuilder {

    private int moduleCounter;

    /**
     * Creates a new ShipBuilder.
     */
    public ShipBuilder() {
        this.moduleCounter = 0;
    }

    /**
     * Builds a single module from a template, instantiating a new {@link HullModule}.
     *
     * @param template the module template to instantiate
     * @return a new HullModule instance
     */
    public HullModule buildFromTemplate(ShipModuleTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("Template must not be null");
        }
        return template.toHullModule();
    }

    /**
     * Assembles a ship from templates and connection definitions.
     * <p>
     * Each template is instantiated as a separate module. Connections define how modules
     * are linked at passage cells. The ship's ShipHull is configured with graph-based
     * connections for pathfinding.
     * </p>
     *
     * @param shipName the name of the ship
     * @param shipType the ship type classification
     * @param templates list of module templates to instantiate
     * @param connections list of connection definitions
     * @return the assembled ship
     */
    public Ship assemble(String shipName, ShipType shipType,
                         List<ShipModuleTemplate> templates,
                         List<ConnectionDefinition> connections) {
        Ship ship = new Ship(0, shipType);
        ship.setName(shipName);
        ShipHull hull = new ShipHull(ship);

        // Instantiate all modules
        Map<Integer, String> indexToModuleId = new HashMap<>();
        for (int i = 0; i < templates.size(); i++) {
            ShipModuleTemplate template = templates.get(i);
            HullModule module = buildFromTemplate(template);
            String moduleId = generateModuleId(template.getName());
            hull.registerModule(moduleId, module);

            // Add to hull modules list
            hull.getHullModules(); // ensure list is initialized
            // We need to set the modules directly
            List<HullModule> modules = new ArrayList<>();
            for (HullModule hm : hull.getHullModules()) {
                modules.add(hm);
            }
            modules.add(module);
            hull.setHullModules(modules);

            indexToModuleId.put(i, moduleId);
        }

        // Create connections
        if (connections != null) {
            for (ConnectionDefinition conn : connections) {
                String fromModuleId = indexToModuleId.get(conn.fromModuleIndex);
                String toModuleId = indexToModuleId.get(conn.toModuleIndex);
                if (fromModuleId != null && toModuleId != null) {
                    hull.connectModules(fromModuleId, conn.fromCell, toModuleId, conn.toCell);
                }
            }
        }

        return ship;
    }

    /**
     * Connects two modules at passage cells within an existing ship hull.
     *
     * @param hull the ship hull to modify
     * @param fromModuleId the source module ID
     * @param fromCell the passage cell in the source module
     * @param toModuleId the target module ID
     * @param toCell the passage cell in the target module
     */
    public void connectAtPassage(ShipHull hull, String fromModuleId, M3space fromCell,
                                  String toModuleId, M3space toCell) {
        hull.connectModules(fromModuleId, fromCell, toModuleId, toCell);
    }

    /**
     * Generates a unique module ID based on the template name.
     *
     * @param templateName the template name
     * @return a unique module ID
     */
    private String generateModuleId(String templateName) {
        moduleCounter++;
        return templateName.toLowerCase().replace(" ", "_") + "_" + moduleCounter;
    }

    /**
     * Defines a connection between two modules by template index.
     * <p>
     * Used during assembly to specify which modules connect at which cells.
     * </p>
     */
    public static class ConnectionDefinition {
        private final int fromModuleIndex;
        private final M3space fromCell;
        private final int toModuleIndex;
        private final M3space toCell;

        /**
         * Creates a connection definition.
         *
         * @param fromModuleIndex index of the source module in the templates list
         * @param fromCell the passage cell in the source module
         * @param toModuleIndex index of the target module in the templates list
         * @param toCell the passage cell in the target module
         */
        public ConnectionDefinition(int fromModuleIndex, M3space fromCell,
                                     int toModuleIndex, M3space toCell) {
            this.fromModuleIndex = fromModuleIndex;
            this.fromCell = fromCell;
            this.toModuleIndex = toModuleIndex;
            this.toCell = toCell;
        }

        public int getFromModuleIndex() { return fromModuleIndex; }
        public M3space getFromCell() { return fromCell; }
        public int getToModuleIndex() { return toModuleIndex; }
        public M3space getToCell() { return toCell; }
    }
}
