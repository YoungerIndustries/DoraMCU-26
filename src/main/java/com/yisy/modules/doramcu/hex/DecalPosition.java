package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a named position on an asset where decals can be placed.
 * <p>
 * Decal positions are predefined templates that correspond to physical locations
 * on the asset surface. Different asset types support different positions:
 * </p>
 * <ul>
 *   <li><b>Ships</b>: nose, hull_port, hull_starboard, wing_left, wing_right, tail, dorsal, ventral</li>
 *   <li><b>Space Stations</b>: ring_inner, ring_outer, core, docking_port, solar_panel</li>
 *   <li><b>Ground Vehicles</b>: turret, hull_front, hull_left, hull_right, rear, roof</li>
 *   <li><b>Buildings</b>: facade_front, facade_back, facade_left, facade_right, roof, entrance</li>
 * </ul>
 */
public class DecalPosition {

    /** Ship: front section of the hull */
    public static final String NOSE = "nose";

    /** Ship: left side of the hull (port) */
    public static final String HULL_PORT = "hull_port";

    /** Ship: right side of the hull (starboard) */
    public static final String HULL_STARBOARD = "hull_starboard";

    /** Ship: left wing */
    public static final String WING_LEFT = "wing_left";

    /** Ship: right wing */
    public static final String WING_RIGHT = "wing_right";

    /** Ship: rear section */
    public static final String TAIL = "tail";

    /** Ship: top surface */
    public static final String DORSAL = "dorsal";

    /** Ship: bottom surface */
    public static final String VENTRAL = "ventral";

    /** Space Station: inner ring */
    public static final String RING_INNER = "ring_inner";

    /** Space Station: outer ring */
    public static final String RING_OUTER = "ring_outer";

    /** Space Station: central core */
    public static final String CORE = "core";

    /** Space Station: docking port area */
    public static final String DOCKING_PORT = "docking_port";

    /** Space Station: solar panel array */
    public static final String SOLAR_PANEL = "solar_panel";

    /** Ground Vehicle: turret */
    public static final String TURRET = "turret";

    /** Ground Vehicle: front of hull */
    public static final String HULL_FRONT = "hull_front";

    /** Ground Vehicle: left side of hull */
    public static final String HULL_LEFT = "hull_left";

    /** Ground Vehicle: right side of hull */
    public static final String HULL_RIGHT = "hull_right";

    /** Ground Vehicle: rear of hull */
    public static final String REAR = "rear";

    /** Ground Vehicle: top surface */
    public static final String ROOF = "roof";

    /** Building: front facade */
    public static final String FACADE_FRONT = "facade_front";

    /** Building: back facade */
    public static final String FACADE_BACK = "facade_back";

    /** Building: left facade */
    public static final String FACADE_LEFT = "facade_left";

    /** Building: right facade */
    public static final String FACADE_RIGHT = "facade_right";

    /** Building: roof */
    public static final String BUILDING_ROOF = "roof";

    /** Building: main entrance */
    public static final String ENTRANCE = "entrance";

    /**
     * Returns all decal positions valid for ships.
     */
    public static List<String> getShipPositions() {
        return List.of(NOSE, HULL_PORT, HULL_STARBOARD, WING_LEFT, WING_RIGHT, TAIL, DORSAL, VENTRAL);
    }

    /**
     * Returns all decal positions valid for space stations.
     */
    public static List<String> getSpaceStationPositions() {
        return List.of(RING_INNER, RING_OUTER, CORE, DOCKING_PORT, SOLAR_PANEL);
    }

    /**
     * Returns all decal positions valid for ground vehicles.
     */
    public static List<String> getGroundVehiclePositions() {
        return List.of(TURRET, HULL_FRONT, HULL_LEFT, HULL_RIGHT, REAR, ROOF);
    }

    /**
     * Returns all decal positions valid for buildings.
     */
    public static List<String> getBuildingPositions() {
        return List.of(FACADE_FRONT, FACADE_BACK, FACADE_LEFT, FACADE_RIGHT, BUILDING_ROOF, ENTRANCE);
    }

    /**
     * Returns all decal positions valid for the given skin type.
     *
     * @param skinType the type of asset
     * @return list of valid position names
     */
    public static List<String> getPositionsForType(SkinType skinType) {
        return switch (skinType) {
            case SHIP -> getShipPositions();
            case SPACE_STATION -> getSpaceStationPositions();
            case GROUND_VEHICLE -> getGroundVehiclePositions();
            case BUILDING -> getBuildingPositions();
        };
    }
}
