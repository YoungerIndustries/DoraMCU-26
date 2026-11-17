package com.yisy.modules.doramcu.fps.core;

import com.yisy.modules.doramcu.fps.FpsMapService;
import com.yisy.modules.doramcu.fps.core.model.FpsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages transitions between FPS maps/locations.
 * <p>
 * Defines which maps are connected and where the transition portals are placed.
 * When a player walks through a portal, this service validates the transition
 * and returns the target map data along with the spawn position on the other side.
 * </p>
 *
 * <p>Transition graph (Earth2026 theme - primary world):
 * <pre>
 *   Younger Industries HQ ── San Diego Streets ── Convention Center
 *         │                        │
 *         │                  NYC Rooftops
 *         │                        │
 *   Portal Room ────────── London Underground
 *         │
 *   Tokyo Neon District ── Area 51 ── Cthulhu's Call
 * </pre>
 * Cross-theme portals:
 * <pre>
 *   Portal Room (Earth2026) ↔ Dragon's Lair (DnD5e)
 *   Area 51 (Earth2026) ↔ Space Station (Traveller)
 *   Cthulhu's Call (Earth2026) ↔ Babylon 5 (Babylon5)
 * </pre>
 * </p>
 */
@Service
@org.springframework.context.annotation.DependsOn("fpsWebSocketConfig")
public class FpsTransitionService {

    private static final Logger log = LoggerFactory.getLogger(FpsTransitionService.class);

    private final FpsMapService mapService;

    /** Map from "sourceMapId:transitionId" to the transition info */
    private final Map<String, TransitionInfo> transitions = new HashMap<>();

    public record TransitionInfo(
            String sourceMapId,
            String sourceTransitionId,
            String targetMapId,
            String targetTransitionId,
            double spawnX,
            double spawnY,
            double spawnZ,
            String label
    ) {
    }

    public FpsTransitionService(FpsMapService mapService) {
        this.mapService = mapService;
    }

    @PostConstruct
    public void initTransitions() {
        // Wait for maps to be registered, then build transitions
        // We use map names for lookup since IDs are UUIDs
        defineAllTransitions();
        log.info("Initialized {} map transitions", transitions.size());
    }

    private void defineAllTransitions() {
        // Earth2026 transitions - primary world connections
        addBidirectionalTransition(
                "Younger Industries HQ", "hq_exit_streets",
                "San Diego Streets", "streets_hq_entrance",
                "Exit to San Diego Streets"
        );

        addBidirectionalTransition(
                "San Diego Streets", "streets_convention_entrance",
                "San Diego Convention Center", "convention_streets_exit",
                "Enter Convention Center"
        );

        addBidirectionalTransition(
                "San Diego Streets", "streets_nyc_portal",
                "NYC Rooftops", "nyc_sd_portal",
                "Travel to NYC Rooftops"
        );

        addBidirectionalTransition(
                "Younger Industries HQ", "hq_portal_room",
                "Portal Room", "portal_hq_entrance",
                "Enter the Portal Room"
        );

        addBidirectionalTransition(
                "Portal Room", "portal_london",
                "London Underground", "london_portal_exit",
                "Travel to London Underground"
        );

        addBidirectionalTransition(
                "Portal Room", "portal_tokyo",
                "Tokyo Neon District", "tokyo_portal_exit",
                "Travel to Tokyo Neon District"
        );

        addBidirectionalTransition(
                "Tokyo Neon District", "tokyo_area51",
                "Area 51", "area51_tokyo_entrance",
                "Travel to Area 51"
        );

        addBidirectionalTransition(
                "Area 51", "area51_cthulhu",
                "Cthulhu's Call", "cthulhu_area51_entrance",
                "Enter Cthulhu's Dimension"
        );

        // Cross-theme transitions
        addBidirectionalTransition(
                "Portal Room", "portal_dnd",
                "Dragon's Lair", "dnd_portal_exit",
                "Enter the Dragon's Realm (D&D)"
        );

        addBidirectionalTransition(
                "Area 51", "area51_traveller",
                "Space Station", "station_area51_entrance",
                "Warp to Space Station (Traveller)"
        );

        addBidirectionalTransition(
                "Cthulhu's Call", "cthulhu_babylon5",
                "Babylon 5", "b5_cthulhu_entrance",
                "Warp to Babylon 5 Station"
        );

        // DnD5e internal transitions
        addBidirectionalTransition(
                "Dragon's Lair", "dragon_forest",
                "Forgotten Forest", "forest_dragon_entrance",
                "Enter the Forgotten Forest"
        );

        addBidirectionalTransition(
                "Forgotten Forest", "forest_keep",
                "Crimson Keep", "keep_forest_entrance",
                "Enter Crimson Keep"
        );

        // Traveller internal transitions
        addBidirectionalTransition(
                "Space Station", "station_planet",
                "Planet Surface", "planet_station_entrance",
                "Descend to Planet Surface"
        );

        // Babylon5 internal transitions
        addBidirectionalTransition(
                "Babylon 5", "b5_zhans",
                "Zhan's Bar", "zhans_b5_entrance",
                "Enter Zhan's Bar"
        );
    }

    /**
     * Add a bidirectional transition between two maps.
     */
    private void addBidirectionalTransition(
            String sourceMapName, String sourceTransitionId,
            String targetMapName, String targetTransitionId,
            String label) {

        FpsMap sourceMap = mapService.getMap(sourceMapName).orElse(null);
        FpsMap targetMap = mapService.getMap(targetMapName).orElse(null);

        if (sourceMap == null || targetMap == null) {
            log.warn("Cannot create transition: source '{}' or target '{}' not found",
                    sourceMapName, targetMapName);
            return;
        }

        // Calculate portal positions at the edges of each map
        double sourceX = sourceMap.getMapWidth() / 4;
        double sourceZ = -sourceMap.getMapDepth() / 2 + 2;
        double targetX = -targetMap.getMapWidth() / 4;
        double targetZ = -targetMap.getMapDepth() / 2 + 2;

        // Add transition point to source map
        sourceMap.getTransitionPoints().add(new FpsMap.TransitionPoint(
                sourceTransitionId,
                sourceX, 1.0, sourceZ,
                targetMap.getId(), targetTransitionId, label
        ));

        // Add return transition point to target map
        String returnLabel = "Return to " + sourceMapName;
        targetMap.getTransitionPoints().add(new FpsMap.TransitionPoint(
                targetTransitionId,
                targetX, 1.0, targetZ,
                sourceMap.getId(), sourceTransitionId, returnLabel
        ));

        // Register in the lookup map
        transitions.put(sourceMap.getId() + ":" + sourceTransitionId, new TransitionInfo(
                sourceMap.getId(), sourceTransitionId,
                targetMap.getId(), targetTransitionId,
                targetX, 1.7, targetZ, label
        ));

        transitions.put(targetMap.getId() + ":" + targetTransitionId, new TransitionInfo(
                targetMap.getId(), targetTransitionId,
                sourceMap.getId(), sourceTransitionId,
                sourceX, 1.7, sourceZ, returnLabel
        ));
    }

    /**
     * Find a transition near the given position in the specified map.
     * Returns the transition info if the player is within 3 units of a portal.
     */
    public Optional<TransitionInfo> findTransitionNear(String mapId, double x, double y, double z) {
        FpsMap map = mapService.getMapById(mapId).orElse(null);
        if (map == null) return Optional.empty();

        for (FpsMap.TransitionPoint tp : map.getTransitionPoints()) {
            double dx = x - tp.x();
            double dz = z - tp.z();
            double distance = Math.sqrt(dx * dx + dz * dz);

            if (distance < 3.0) {
                String key = mapId + ":" + tp.id();
                return transitions.values().stream()
                        .filter(t -> t.sourceMapId().equals(mapId) && t.sourceTransitionId().equals(tp.id()))
                        .findFirst();
            }
        }
        return Optional.empty();
    }

    /**
     * Get all transitions for a specific map (for sending to client).
     */
    public List<FpsMap.TransitionPoint> getTransitionsForMap(String mapId) {
        return mapService.getMapById(mapId)
                .map(FpsMap::getTransitionPoints)
                .orElse(Collections.emptyList());
    }

    /**
     * Get all available maps grouped by theme.
     */
    public Map<String, List<MapSummary>> getMapsByTheme() {
        return mapService.getAllMaps().stream()
                .collect(Collectors.groupingBy(
                        m -> m.getTheme() != null ? m.getTheme() : "Unknown",
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                lists -> lists.stream()
                                        .map(m -> new MapSummary(m.getId(), m.getName(), m.getDescription(), m.getTheme()))
                                        .collect(Collectors.toList())
                        )
                ));
    }

    public record MapSummary(String id, String name, String description, String theme) {
    }

    /**
     * Get all transitions from a specific map.
     */
    public List<TransitionInfo> getTransitionsFrom(String mapId) {
        return transitions.values().stream()
                .filter(t -> t.sourceMapId().equals(mapId))
                .collect(Collectors.toList());
    }
}
