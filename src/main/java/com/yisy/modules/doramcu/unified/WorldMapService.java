package com.yisy.modules.doramcu.unified;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages the unified world map as a graph of nodes and edges.
 * <p>
 * The world map tracks all discovered locations, factions, planets,
 * and their connections across all game modules.
 * </p>
 */
@Service
public class WorldMapService {

    private static final Logger log = LoggerFactory.getLogger(WorldMapService.class);

    private final Map<String, WorldNode> nodes = new HashMap<>();
    private final Map<String, WorldEdge> edges = new HashMap<>();

    @PostConstruct
    public void seedWorldMap() {
        log.info("Seeding unified world map with initial nodes and edges...");

        // ============================================================
        // Earth 2026 nodes
        // ============================================================
        addNode(new WorldNode("e26-earth", "Earth", WorldNode.NodeType.PLANET, "earth2026",
                0, 0, 0, "Home planet. Modern-day Earth with cities, organizations, and survival challenges.", null));
        addNode(new WorldNode("e26-newyork", "New York", WorldNode.NodeType.CITY, "earth2026",
                0.5, 0.3, 0, "Major metropolitan hub with organizations and real estate.", null));
        addNode(new WorldNode("e26-london", "London", WorldNode.NodeType.CITY, "earth2026",
                -0.3, 0.4, 0, "Historic city with underground networks and connections.", null));
        addNode(new WorldNode("e26-tokyo", "Tokyo", WorldNode.NodeType.CITY, "earth2026",
                0.8, -0.2, 0, "Technological center with advanced organizations.", null));
        addNode(new WorldNode("e26-mars", "Mars", WorldNode.NodeType.PLANET, "earth2026",
                2, 0, 0, "Red planet with early colonies and exploration outposts.", null));
        addNode(new WorldNode("e26-moon", "The Moon", WorldNode.NodeType.PLANET, "earth2026",
                0.3, 0.1, 0.5, "Lunar base with research facilities.", null));

        // ============================================================
        // Captain's Log nodes
        // ============================================================
        addNode(new WorldNode("cl-stardate", "Stardate Prime", WorldNode.NodeType.STAR_SYSTEM, "captains-log",
                -1, 1, 1, "Central star system with multiple habitable worlds.", null));
        addNode(new WorldNode("cl-vega", "Vega System", WorldNode.NodeType.STAR_SYSTEM, "captains-log",
                -1.5, 0.8, 1.2, "Distant star system with anomalies and missions.", null));
        addNode(new WorldNode("cl-starbase", "Starbase Alpha", WorldNode.NodeType.SPACE_STATION, "captains-log",
                -1.2, 0.9, 1.1, "Main starbase for fleet operations and resupply.", null));
        addNode(new WorldNode("cl-nexus", "Nexus Point", WorldNode.NodeType.SPACE_STATION, "captains-log",
                -0.8, 1.2, 0.9, "Strategic nexus for diplomatic missions.", null));

        // ============================================================
        // D&D 5e nodes
        // ============================================================
        addNode(new WorldNode("dnd-undermountain", "Undermountain", WorldNode.NodeType.DUNGEON, "dnd5e",
                1, -1, -0.5, "Vast dungeon complex beneath the city of Waterdeep.", null));
        addNode(new WorldNode("dnd-waterdeep", "Waterdeep", WorldNode.NodeType.CITY, "dnd5e",
                1, -0.8, -0.3, "City of Splendors, gateway to Undermountain.", null));
        addNode(new WorldNode("dnd-forgottenrealms", "Forgotten Realms", WorldNode.NodeType.TERRITORY, "dnd5e",
                1, -1, -0.4, "Vast fantasy realm with dungeons, monsters, and magic.", null));

        // ============================================================
        // Traveller nodes
        // ============================================================
        addNode(new WorldNode("trv-sol", "Sol System", WorldNode.NodeType.STAR_SYSTEM, "traveller",
                -2, -0.5, 0.5, "Home system. Earth, Mars, and the outer planets.", null));
        addNode(new WorldNode("trv-alphacentauri", "Alpha Centauri", WorldNode.NodeType.STAR_SYSTEM, "traveller",
                -2.5, -0.3, 0.6, "Nearest star system with trade routes.", null));
        addNode(new WorldNode("trv-antares", "Antares", WorldNode.NodeType.PLANET, "traveller",
                -2.2, -0.7, 0.4, "Red giant system with dangerous adventures.", null));
        addNode(new WorldNode("trv-coreworld", "Core World", WorldNode.NodeType.PLANET, "traveller",
                -1.8, -0.4, 0.7, "Imperial core world with high technology.", null));

        // ============================================================
        // Star Wars nodes
        // ============================================================
        addNode(new WorldNode("sw-tatooine", "Tatooine", WorldNode.NodeType.PLANET, "starwars",
                3, 1, 0, "Desert planet in the Outer Rim. Home of Anakin Skywalker.", null));
        addNode(new WorldNode("sw-hoth", "Hoth", WorldNode.NodeType.PLANET, "starwars",
                3.5, 1.2, 0.2, "Ice planet with Rebel base Echo Base.", null));
        addNode(new WorldNode("sw-endor", "Endor", WorldNode.NodeType.PLANET, "starwars",
                3.2, 0.8, -0.1, "Forest moon with Ewok tribes and the shield generator.", null));
        addNode(new WorldNode("sw-rebel", "Rebel Alliance", WorldNode.NodeType.FACTION, "starwars",
                3.1, 1, 0.1, "Galactic Rebellion fighting against the Empire.", null));
        addNode(new WorldNode("sw-empire", "Galactic Empire", WorldNode.NodeType.FACTION, "starwars",
                2.8, 0.9, 0.1, "The Empire controls the galaxy with fear and power.", null));

        // ============================================================
        // Babylon5 nodes
        // ============================================================
        addNode(new WorldNode("b5-station", "Babylon 5 Station", WorldNode.NodeType.SPACE_STATION, "babylon5",
                -3, 0, 1, "Diplomatic space station, neutral ground for all races.", null));
        addNode(new WorldNode("b5-earth", "Earth (B5)", WorldNode.NodeType.PLANET, "babylon5",
                -3.2, 0.2, 1.1, "Earth in the Babylon5 universe, controlled by the Earth Alliance.", null));
        addNode(new WorldNode("b5-minbari", "Minbari Space", WorldNode.NodeType.TERRITORY, "babylon5",
                -3.5, -0.3, 1.3, "Sacred territory of the Minbari race.", null));

        // ============================================================
        // Star Trek 4X nodes
        // ============================================================
        addNode(new WorldNode("st4x-federation", "Federation Core", WorldNode.NodeType.TERRITORY, "startrek4x",
                -1, -2, 0, "United Federation of Planets core territory.", null));
        addNode(new WorldNode("st4x-romulan", "Romulan Space", WorldNode.NodeType.TERRITORY, "startrek4x",
                -1.5, -2.2, 0.2, "Romulan Star Empire territory, shrouded in mystery.", null));
        addNode(new WorldNode("st4x-sector001", "Sector 001", WorldNode.NodeType.STAR_SYSTEM, "startrek4x",
                -0.8, -1.8, -0.1, "Unexplored sector with potential resources.", null));

        // ============================================================
        // Mech Warrior nodes
        // ============================================================
        addNode(new WorldNode("mw-thessalon", "Thessalon", WorldNode.NodeType.PLANET, "mechwarrior",
                2, -1, 0, "Capital world of the Lyran Commonwealth.", null));
        addNode(new WorldNode("mw-battlefield", "Battlefield Alpha", WorldNode.NodeType.LOCATION, "mechwarrior",
                2.2, -1.2, 0.1, "Active combat zone with mech battles.", null));

        // ============================================================
        // Zero Zero Seven nodes
        // ============================================================
        addNode(new WorldNode("z7-safehouse", "Safehouse Alpha", WorldNode.NodeType.LOCATION, "zerozeroseven",
                0, 2, 0, "Secure safehouse for covert operations.", null));
        addNode(new WorldNode("z7-headquarters", "Intelligence HQ", WorldNode.NodeType.LOCATION, "zerozeroseven",
                0.2, 2.2, 0.1, "Central intelligence headquarters.", null));
        addNode(new WorldNode("z7-fieldops", "Field Operations", WorldNode.NodeType.LOCATION, "zerozeroseven",
                -0.2, 1.8, -0.1, "Active field operations zone.", null));

        // ============================================================
        // Edges connecting nodes
        // ============================================================
        // Earth 2026 connections
        addEdge(new WorldEdge("e26-earth", "e26-newyork", WorldEdge.EdgeType.TRAVEL, 1, "Travel to New York", null));
        addEdge(new WorldEdge("e26-earth", "e26-london", WorldEdge.EdgeType.TRAVEL, 1, "Travel to London", null));
        addEdge(new WorldEdge("e26-earth", "e26-tokyo", WorldEdge.EdgeType.TRAVEL, 1, "Travel to Tokyo", null));
        addEdge(new WorldEdge("e26-earth", "e26-moon", WorldEdge.EdgeType.WARP_GATE, 2, "Space travel to Moon", null));
        addEdge(new WorldEdge("e26-earth", "e26-mars", WorldEdge.EdgeType.WARP_GATE, 3, "Space travel to Mars", null));

        // Captain's Log connections
        addEdge(new WorldEdge("cl-stardate", "cl-vega", WorldEdge.EdgeType.HYPERLANE, 2, "Warp route to Vega", null));
        addEdge(new WorldEdge("cl-stardate", "cl-starbase", WorldEdge.EdgeType.HYPERLANE, 1, "Warp to Starbase Alpha", null));
        addEdge(new WorldEdge("cl-starbase", "cl-nexus", WorldEdge.EdgeType.HYPERLANE, 1, "Warp to Nexus Point", null));

        // D&D connections
        addEdge(new WorldEdge("dnd-waterdeep", "dnd-undermountain", WorldEdge.EdgeType.TRAVEL, 1, "Descend into Undermountain", null));
        addEdge(new WorldEdge("dnd-waterdeep", "dnd-forgottenrealms", WorldEdge.EdgeType.TRAVEL, 2, "Journey into the Realms", null));

        // Traveller connections
        addEdge(new WorldEdge("trv-sol", "trv-alphacentauri", WorldEdge.EdgeType.HYPERLANE, 3, "Jump to Alpha Centauri", null));
        addEdge(new WorldEdge("trv-sol", "trv-coreworld", WorldEdge.EdgeType.HYPERLANE, 2, "Jump to Core World", null));

        // Star Wars connections
        addEdge(new WorldEdge("sw-tatooine", "sw-hoth", WorldEdge.EdgeType.HYPERLANE, 5, "Hyperspace route", null));
        addEdge(new WorldEdge("sw-hoth", "sw-endor", WorldEdge.EdgeType.HYPERLANE, 3, "Hyperspace route", null));
        addEdge(new WorldEdge("sw-rebel", "sw-empire", WorldEdge.EdgeType.COMBAT, 1, "Galactic Civil War", null));

        // Babylon5 connections
        addEdge(new WorldEdge("b5-station", "b5-earth", WorldEdge.EdgeType.HYPERLANE, 2, "Jump to Earth", null));
        addEdge(new WorldEdge("b5-station", "b5-minbari", WorldEdge.EdgeType.HYPERLANE, 3, "Jump to Minbari Space", null));

        // Star Trek 4X connections
        addEdge(new WorldEdge("st4x-federation", "st4x-romulan", WorldEdge.EdgeType.DIPLOMATIC, 2, "Federation-Romulan border", null));
        addEdge(new WorldEdge("st4x-federation", "st4x-sector001", WorldEdge.EdgeType.TRAVEL, 1, "Explore Sector 001", null));

        // Mech Warrior connections
        addEdge(new WorldEdge("mw-thessalon", "mw-battlefield", WorldEdge.EdgeType.COMBAT, 1, "Deploy to battlefield", null));

        // Zero Zero Seven connections
        addEdge(new WorldEdge("z7-headquarters", "z7-safehouse", WorldEdge.EdgeType.TRAVEL, 1, "Secure transport", null));
        addEdge(new WorldEdge("z7-headquarters", "z7-fieldops", WorldEdge.EdgeType.TRAVEL, 1, "Field deployment", null));

        // Cross-module connections
        addEdge(new WorldEdge("e26-earth", "cl-stardate", WorldEdge.EdgeType.WARP_GATE, 5, "Warp from Earth to star systems", null));
        addEdge(new WorldEdge("e26-earth", "sw-tatooine", WorldEdge.EdgeType.HYPERLANE, 8, "Hyperspace to Tatooine", null));
        addEdge(new WorldEdge("e26-earth", "b5-station", WorldEdge.EdgeType.WARP_GATE, 4, "Jump to Babylon 5", null));
        addEdge(new WorldEdge("e26-earth", "dnd-waterdeep", WorldEdge.EdgeType.WARP_GATE, 6, "Portal to Forgotten Realms", null));
        addEdge(new WorldEdge("e26-earth", "trv-sol", WorldEdge.EdgeType.WARP_GATE, 1, "Same solar system", null));

        log.info("World map seeded with {} nodes and {} edges", nodes.size(), edges.size());
    }

    /**
     * Adds a node to the world map.
     */
    public void addNode(WorldNode node) {
        nodes.put(node.getId(), node);
        log.debug("Added world node: {} ({})", node.getName(), node.getType());
    }

    /**
     * Adds an edge to the world map.
     */
    public void addEdge(WorldEdge edge) {
        edges.put(edge.getId(), edge);
        log.debug("Added world edge: {} -> {} ({})", edge.getSourceId(), edge.getTargetId(), edge.getType());
    }

    /**
     * Returns all nodes, optionally filtered by module.
     */
    public List<WorldNode> getNodes(String moduleFilter) {
        if (moduleFilter == null || moduleFilter.isEmpty()) {
            return new ArrayList<>(nodes.values());
        }
        return nodes.values().stream()
                .filter(n -> n.getModule().equals(moduleFilter))
                .collect(Collectors.toList());
    }

    /**
     * Returns all edges.
     */
    public List<WorldEdge> getEdges() {
        return new ArrayList<>(edges.values());
    }

    /**
     * Returns a specific node by ID.
     */
    public Optional<WorldNode> getNode(String nodeId) {
        return Optional.ofNullable(nodes.get(nodeId));
    }

    /**
     * Marks a node as discovered.
     */
    public boolean discoverNode(String nodeId) {
        WorldNode node = nodes.get(nodeId);
        if (node != null && !node.isDiscovered()) {
            node.setDiscovered(true);
            log.info("Discovered node: {}", node.getName());
            return true;
        }
        return false;
    }

    /**
     * Returns only discovered nodes.
     */
    public List<WorldNode> getDiscoveredNodes() {
        return nodes.values().stream()
                .filter(WorldNode::isDiscovered)
                .collect(Collectors.toList());
    }

    /**
     * Returns edges connected to a specific node.
     */
    public List<WorldEdge> getConnectedEdges(String nodeId) {
        return edges.values().stream()
                .filter(e -> e.getSourceId().equals(nodeId) || e.getTargetId().equals(nodeId))
                .collect(Collectors.toList());
    }

    /**
     * Returns the full world map as a summary.
     */
    public Map<String, Object> getWorldMapSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalNodes", nodes.size());
        summary.put("discoveredNodes", (long) nodes.values().stream().filter(WorldNode::isDiscovered).count());
        summary.put("totalEdges", edges.size());
        summary.put("modules", nodes.values().stream().map(WorldNode::getModule).distinct().collect(Collectors.toList()));
        return summary;
    }

    /**
     * Clears the world map.
     */
    public void clear() {
        nodes.clear();
        edges.clear();
        log.info("World map cleared");
    }
}
