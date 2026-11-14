package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.event.GalaxyEventService;
import com.yisy.modules.doramcu.event.GalaxyEventType;
import com.yisy.modules.doramcu.hex.BuildObjectType;
import com.yisy.modules.doramcu.model.GalaxyObjectState;
import com.yisy.modules.doramcu.repository.GalaxyObjectStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that manages the lifecycle state of all galaxy objects.
 * <p>
 * The Galaxy Overlord uses this service to:
 * <ul>
 *   <li>Record when objects are mapped during galaxy creation</li>
 *   <li>Track when objects are built on-demand for player interaction</li>
 *   <li>Record when objects are destroyed from conflict</li>
 *   <li>Schedule and track rebuilding of destroyed objects</li>
 *   <li>Query which objects need to be built near a player's position</li>
 *   <li>Serialize object metadata for state persistence</li>
 * </ul>
 * </p>
 * <p>
 * This ensures the galaxy manages the state of all objects and only builds
 * the objects it needs for player interactions, while maintaining the state
 * of objects that can be destroyed from conflict.
 * </p>
 */
@Service
public class GalaxyStateService {

    private static final Logger log = LoggerFactory.getLogger(GalaxyStateService.class);

    private final GalaxyObjectStateRepository stateRepository;
    private final GalaxyEventService eventService;

    public GalaxyStateService(GalaxyObjectStateRepository stateRepository,
                               GalaxyEventService eventService) {
        this.stateRepository = stateRepository;
        this.eventService = eventService;
    }

    // ---- Object Registration ----

    /**
     * Registers a new galaxy object in the state tracker.
     * <p>
     * Call this when the Galaxy Overlord maps an object during galaxy creation.
     * The object starts in MAPPED state (known to exist but not built).
     * </p>
     *
     * @param galaxyName the galaxy the object belongs to
     * @param objectType the type of object (PLANET, CITY_3D, etc.)
     * @param objectId unique identifier for the object
     * @param objectName human-readable name
     * @param sectorRow sector row position (0-9)
     * @param sectorCol sector column position (0-9)
     * @param metadata additional object properties as JSON
     * @return the saved state record
     */
    @Transactional
    public GalaxyObjectState registerObject(String galaxyName, String objectType,
                                             String objectId, String objectName,
                                             Integer sectorRow, Integer sectorCol,
                                             Map<String, Object> metadata) {
        GalaxyObjectState state = new GalaxyObjectState();
        state.setGalaxyName(galaxyName);
        state.setObjectType(objectType);
        state.setObjectId(objectId);
        state.setObjectName(objectName);
        state.setSectorRow(sectorRow);
        state.setSectorCol(sectorCol);
        state.setState(GalaxyObjectState.ObjectState.MAPPED);

        if (metadata != null && !metadata.isEmpty()) {
            state.setMetadataJson(serializeMetadata(metadata));
        }

        // Mark objects that typically need 3D detail
        if (objectType != null) {
            state.setRequires3D(needs3DDetail(objectType));
        }

        stateRepository.save(state);
        log.debug("Registered object: {} '{}' in galaxy '{}' at ({},{})",
                objectType, objectName, galaxyName, sectorRow, sectorCol);
        return state;
    }

    /**
     * Registers multiple objects at once (batch registration during galaxy mapping).
     *
     * @param galaxyName the galaxy name
     * @param registrations list of registration data
     * @return the saved state records
     */
    @Transactional
    public List<GalaxyObjectState> registerObjects(String galaxyName,
                                                     List<ObjectRegistration> registrations) {
        List<GalaxyObjectState> states = new ArrayList<>();
        for (ObjectRegistration reg : registrations) {
            states.add(registerObject(
                    galaxyName, reg.objectType, reg.objectId, reg.objectName,
                    reg.sectorRow, reg.sectorCol, reg.metadata));
        }
        log.info("Registered {} objects in galaxy '{}'", states.size(), galaxyName);
        return states;
    }

    // ---- State Transitions ----

    /**
     * Marks an object as being built.
     */
    @Transactional
    public GalaxyObjectState startBuilding(String galaxyName, String objectType, String objectId) {
        GalaxyObjectState state = findObject(galaxyName, objectType, objectId);
        if (state == null) {
            log.warn("Cannot start building unknown object: {} '{}' in '{}'",
                    objectType, objectId, galaxyName);
            return null;
        }
        state.markBuilding();
        stateRepository.save(state);
        publishEvent(galaxyName, GalaxyEventType.BUILD_STARTED, state);
        log.info("Started building: {} '{}' in '{}'", objectType, objectId, galaxyName);
        return state;
    }

    /**
     * Marks an object as fully built.
     */
    @Transactional
    public GalaxyObjectState markBuilt(String galaxyName, String objectType, String objectId,
                                        Map<String, Object> metadata) {
        GalaxyObjectState state = findObject(galaxyName, objectType, objectId);
        if (state == null) {
            log.warn("Cannot mark built unknown object: {} '{}' in '{}'",
                    objectType, objectId, galaxyName);
            return null;
        }
        state.markBuilt();
        if (metadata != null && !metadata.isEmpty()) {
            state.setMetadataJson(serializeMetadata(metadata));
        }
        if (state.isRequires3D()) {
            state.setHas3DDetail(true);
        }
        stateRepository.save(state);
        publishEvent(galaxyName, GalaxyEventType.BUILD_COMPLETED, state);
        log.info("Marked built: {} '{}' in '{}'", objectType, objectId, galaxyName);
        return state;
    }

    /**
     * Marks an object as damaged.
     */
    @Transactional
    public GalaxyObjectState markDamaged(String galaxyName, String objectType, String objectId) {
        GalaxyObjectState state = findObject(galaxyName, objectType, objectId);
        if (state == null) return null;
        state.markDamaged();
        stateRepository.save(state);
        publishEvent(galaxyName, GalaxyEventType.OBJECT_DAMAGED, state);
        log.info("Marked damaged: {} '{}' in '{}'", objectType, objectId, galaxyName);
        return state;
    }

    /**
     * Marks an object as destroyed.
     * <p>
     * This is called when cities, transport routes, or other objects are destroyed
     * from conflict. The destruction is recorded with the destroyer's identity.
     * </p>
     *
     * @param galaxyName the galaxy name
     * @param objectType the object type
     * @param objectId the object ID
     * @param destroyedBy who or what destroyed the object
     * @return the updated state record
     */
    @Transactional
    public GalaxyObjectState destroyObject(String galaxyName, String objectType,
                                            String objectId, String destroyedBy) {
        GalaxyObjectState state = findObject(galaxyName, objectType, objectId);
        if (state == null) {
            log.warn("Cannot destroy unknown object: {} '{}' in '{}'",
                    objectType, objectId, galaxyName);
            return null;
        }
        state.markDestroyed(destroyedBy);
        stateRepository.save(state);
        publishEvent(galaxyName, GalaxyEventType.OBJECT_DESTROYED, state,
                "destroyedBy", destroyedBy);
        log.info("Destroyed: {} '{}' in '{}' by {}", objectType, objectId, galaxyName, destroyedBy);
        return state;
    }

    /**
     * Schedules a destroyed object for rebuilding.
     */
    @Transactional
    public GalaxyObjectState scheduleRebuild(String galaxyName, String objectType, String objectId) {
        GalaxyObjectState state = findObject(galaxyName, objectType, objectId);
        if (state == null) return null;
        state.setState(GalaxyObjectState.ObjectState.SCHEDULED_FOR_REBUILD);
        state.setUpdatedAt(LocalDateTime.now());
        stateRepository.save(state);
        log.info("Scheduled rebuild: {} '{}' in '{}'", objectType, objectId, galaxyName);
        return state;
    }

    /**
     * Marks a destroyed object as rebuilt.
     */
    @Transactional
    public GalaxyObjectState markRebuilt(String galaxyName, String objectType, String objectId) {
        GalaxyObjectState state = findObject(galaxyName, objectType, objectId);
        if (state == null) return null;
        state.markRebuilt();
        stateRepository.save(state);
        publishEvent(galaxyName, GalaxyEventType.OBJECT_REBUILT, state);
        log.info("Rebuilt: {} '{}' in '{}' (rebuild count: {})",
                objectType, objectId, galaxyName, state.getRebuildCount());
        return state;
    }

    // ---- Query Methods ----

    /**
     * Finds a specific object's state.
     */
    @Transactional(readOnly = true)
    public GalaxyObjectState findObject(String galaxyName, String objectType, String objectId) {
        return stateRepository.findByGalaxyNameAndObjectTypeAndObjectId(
                galaxyName, objectType, objectId);
    }

    /**
     * Gets all objects in a galaxy.
     */
    @Transactional(readOnly = true)
    public List<GalaxyObjectState> getAllObjects(String galaxyName) {
        return stateRepository.findByGalaxyName(galaxyName);
    }

    /**
     * Gets all objects of a specific type in a galaxy.
     */
    @Transactional(readOnly = true)
    public List<GalaxyObjectState> getObjectsByType(String galaxyName, String objectType) {
        return stateRepository.findByGalaxyNameAndObjectType(galaxyName, objectType);
    }

    /**
     * Gets all destroyed objects in a galaxy.
     */
    @Transactional(readOnly = true)
    public List<GalaxyObjectState> getDestroyedObjects(String galaxyName) {
        return stateRepository.findByGalaxyNameAndState(
                galaxyName, GalaxyObjectState.ObjectState.DESTROYED);
    }

    /**
     * Gets objects that need to be built near a player's position.
     * <p>
     * This is the core method for on-demand building. The Galaxy Overlord calls
     * this when a player enters a new sector to determine which objects need
     * to be built for player interaction.
     * </p>
     *
     * @param galaxyName the galaxy name
     * @param playerSectorRow player's current sector row
     * @param playerSectorCol player's current sector column
     * @param radius search radius in sectors
     * @return objects that are mapped but not yet built near the player
     */
    @Transactional(readOnly = true)
    public List<GalaxyObjectState> getObjectsToBuildNearPlayer(
            String galaxyName, int playerSectorRow, int playerSectorCol, int radius) {
        int minRow = Math.max(0, playerSectorRow - radius);
        int maxRow = Math.min(9, playerSectorRow + radius);
        int minCol = Math.max(0, playerSectorCol - radius);
        int maxCol = Math.min(9, playerSectorCol + radius);

        List<GalaxyObjectState> nearby = stateRepository.findNearSector(
                galaxyName, minRow, maxRow, minCol, maxCol);

        // Filter to objects that need building
        return nearby.stream()
                .filter(obj -> obj.getState() == GalaxyObjectState.ObjectState.MAPPED ||
                               obj.getState() == GalaxyObjectState.ObjectState.SCHEDULED_FOR_REBUILD)
                .collect(Collectors.toList());
    }

    /**
     * Gets objects on a specific planet that need 3D detail.
     */
    @Transactional(readOnly = true)
    public List<GalaxyObjectState> getObjectsNeeding3DDetail(String galaxyName, String planetName) {
        List<GalaxyObjectState> onPlanet = stateRepository.findByGalaxyNameAndPlanetName(
                galaxyName, planetName);
        return onPlanet.stream()
                .filter(obj -> obj.isRequires3D() && !obj.isHas3DDetail())
                .collect(Collectors.toList());
    }

    /**
     * Gets a summary of object states in a galaxy.
     *
     * @return map of state name to count
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getGalaxyStateSummary(String galaxyName) {
        List<Object[]> results = stateRepository.countByStateGrouped(galaxyName);
        Map<String, Long> summary = new HashMap<>();
        for (Object[] result : results) {
            summary.put(result[0].toString(), (Long) result[1]);
        }
        return summary;
    }

    /**
     * Gets the total number of tracked objects in a galaxy.
     */
    @Transactional(readOnly = true)
    public long getObjectCount(String galaxyName) {
        return stateRepository.countByGalaxyName(galaxyName);
    }

    // ---- Galaxy Reset ----

    /**
     * Clears all object states for a galaxy (used when resetting/regenerating a galaxy).
     */
    @Transactional
    public void clearGalaxyStates(String galaxyName) {
        long count = stateRepository.countByGalaxyName(galaxyName);
        stateRepository.deleteByGalaxyName(galaxyName);
        log.info("Cleared {} object states for galaxy '{}'", count, galaxyName);
    }

    // ---- Serialization Helpers ----

    /**
     * Serializes a metadata map to JSON string.
     * <p>
     * Uses simple JSON serialization without external dependencies.
     * </p>
     */
    public String serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) return null;

        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
            sb.append(serializeValue(entry.getValue()));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Deserializes a JSON string to a metadata map.
     * <p>
     * Simple JSON parser for the metadata format produced by serializeMetadata.
     * For complex JSON, consider using a proper JSON library.
     * </p>
     */
    public Map<String, Object> deserializeMetadata(String json) {
        if (json == null || json.isBlank()) return Collections.emptyMap();
        // For production use, integrate with Jackson/Gson
        // This is a simple placeholder
        return Collections.singletonMap("_rawJson", json);
    }

    private String serializeValue(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + escapeJson((String) value) + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        return "\"" + escapeJson(value.toString()) + "\"";
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ---- Utility Methods ----

    /**
     * Checks if an object type typically needs 3D detail for player interaction.
     */
    private boolean needs3DDetail(String objectType) {
        if (objectType == null) return false;
        return objectType.equals(BuildObjectType.CITY_3D.name()) ||
               objectType.equals(BuildObjectType.SETTLEMENT.name()) ||
               objectType.equals(BuildObjectType.SPACE_STATION.name()) ||
               objectType.equals(BuildObjectType.YOUNGER_OFFICE.name()) ||
               objectType.equals(BuildObjectType.INDUSTRY.name());
    }

    /**
     * Publishes a galaxy event for state changes.
     */
    private void publishEvent(String galaxyName, GalaxyEventType eventType,
                               GalaxyObjectState state, String... extraProperties) {
        if (eventService == null) return;

        List<String> pairs = new ArrayList<>();
        pairs.add("galaxyName");
        pairs.add(galaxyName);
        pairs.add("objectType");
        pairs.add(state.getObjectType());
        pairs.add("objectId");
        pairs.add(state.getObjectId());
        pairs.add("objectName");
        pairs.add(state.getObjectName() != null ? state.getObjectName() : "");
        pairs.add("state");
        pairs.add(state.getState().name());

        if (extraProperties != null && extraProperties.length % 2 == 0) {
            for (String p : extraProperties) {
                pairs.add(p != null ? p : "");
            }
        }

        eventService.publish(eventType, galaxyName, null,
                pairs.toArray(new String[0]));
    }

    // ---- Data Classes ----

    /**
     * Registration data for a galaxy object.
     */
    public static class ObjectRegistration {
        public final String objectType;
        public final String objectId;
        public final String objectName;
        public final Integer sectorRow;
        public final Integer sectorCol;
        public final Map<String, Object> metadata;

        public ObjectRegistration(String objectType, String objectId, String objectName,
                                   Integer sectorRow, Integer sectorCol,
                                   Map<String, Object> metadata) {
            this.objectType = objectType;
            this.objectId = objectId;
            this.objectName = objectName;
            this.sectorRow = sectorRow;
            this.sectorCol = sectorCol;
            this.metadata = metadata;
        }

        /**
         * Creates a registration with no metadata.
         */
        public ObjectRegistration(String objectType, String objectId, String objectName,
                                   Integer sectorRow, Integer sectorCol) {
            this(objectType, objectId, objectName, sectorRow, sectorCol, null);
        }
    }
}
