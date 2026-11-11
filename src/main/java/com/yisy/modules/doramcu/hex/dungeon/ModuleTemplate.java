package com.yisy.modules.doramcu.hex.dungeon;

import com.yisy.modules.doramcu.hex.Compartment;
import com.yisy.modules.doramcu.hex.HullModule;
import com.yisy.modules.doramcu.hex.M3space;
import com.yisy.modules.doramcu.hex.ShapeDefinition;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Abstract base class for all module templates across building types.
 * <p>
 * Provides shared properties and behavior for reusable, database-persisted
 * module templates that can instantiate {@link HullModule} instances.
 * Subclasses specialize for different domains:
 * </p>
 * <ul>
 *   <li>{@link DungeonModuleTemplate} — dungeon rooms and corridors</li>
 *   <li>{@link ShipModuleTemplate} — ship sections (bridge, engine room, etc.)</li>
 *   <li>{@link StationModuleTemplate} — station sections (command module, docking ring, etc.)</li>
 *   <li>{@link FacilityModuleTemplate} — facility sections (mining shaft, lab wing, etc.)</li>
 * </ul>
 * <p>
 * Shared base properties include name, dimensions, serialized shape cells,
 * serialized compartments, serialized connection points, and tags for filtering.
 * </p>
 */
@MappedSuperclass
public abstract class ModuleTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int width;

    @Column(nullable = false)
    private int height;

    @Column(nullable = false)
    private int depth;

    /** Serialized cell coordinates as JSON array: ["0,0,0","1,0,0",...] */
    @Column(name = "shape_cells_json", columnDefinition = "TEXT")
    private String shapeCellsJson;

    /** Serialized compartment definitions as JSON */
    @Column(name = "compartments_json", columnDefinition = "TEXT")
    private String compartmentsJson;

    /** Serialized doorway/connection positions as JSON */
    @Column(name = "connection_points_json", columnDefinition = "TEXT")
    private String connectionPointsJson;

    /** Difficulty/complexity rating 1-10 */
    @Column
    private int difficulty = 1;

    /** Comma-separated search tags */
    @Column(length = 500)
    private String tags;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ---- Getters and Setters ----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public String getShapeCellsJson() {
        return shapeCellsJson;
    }

    public void setShapeCellsJson(String shapeCellsJson) {
        this.shapeCellsJson = shapeCellsJson;
    }

    public String getCompartmentsJson() {
        return compartmentsJson;
    }

    public void setCompartmentsJson(String compartmentsJson) {
        this.compartmentsJson = compartmentsJson;
    }

    public String getConnectionPointsJson() {
        return connectionPointsJson;
    }

    public void setConnectionPointsJson(String connectionPointsJson) {
        this.connectionPointsJson = connectionPointsJson;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = Math.max(1, Math.min(10, difficulty));
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ---- Helper methods ----

    /**
     * Converts the serialized shape cells JSON to a {@link ShapeDefinition}.
     *
     * @return a ShapeDefinition representing the module's footprint
     * @throws IllegalArgumentException if shapeCellsJson is null or empty
     */
    public ShapeDefinition toShapeDefinition() {
        if (shapeCellsJson == null || shapeCellsJson.isBlank()) {
            throw new IllegalArgumentException("Shape cells JSON is not set for template: " + name);
        }
        Set<M3space> cells = parseCellCoordinates(shapeCellsJson);
        return new com.yisy.modules.doramcu.hex.MaskShape(cells);
    }

    /**
     * Instantiates a {@link HullModule} from this template.
     *
     * @return a new HullModule instance based on this template
     */
    public HullModule toHullModule() {
        HullModule module = new HullModule(name, width, height, depth);
        ShapeDefinition shape = toShapeDefinition();
        module.setShape(shape);

        // Set up compartments from stored definitions
        if (compartmentsJson != null && !compartmentsJson.isBlank()) {
            List<Compartment> compartments = parseCompartmentsJson(compartmentsJson, module);
            for (Compartment compartment : compartments) {
                module.addCompartment(compartment);
            }
        }

        return module;
    }

    /**
     * Parses the connection points JSON and returns a list of doorway positions.
     *
     * @return list of connection point maps, or empty list if not set
     */
    public List<Map<String, Object>> getConnectionPoints() {
        if (connectionPointsJson == null || connectionPointsJson.isBlank()) {
            return Collections.emptyList();
        }
        return parseConnectionPointsJson(connectionPointsJson);
    }

    // ---- Static parsing helpers ----

    /**
     * Parses a JSON array of "x,y,z" coordinate strings into a set of M3space cells.
     */
    public static Set<M3space> parseCellCoordinates(String json) {
        Set<M3space> cells = new HashSet<>();
        String cleaned = json.trim();
        if (cleaned.startsWith("[")) cleaned = cleaned.substring(1);
        if (cleaned.endsWith("]")) cleaned = cleaned.substring(0, cleaned.length() - 1);

        String[] parts = cleaned.split("\",\"");
        for (String part : parts) {
            part = part.trim().replace("\"", "");
            if (part.isBlank()) continue;
            String[] coords = part.split(",");
            if (coords.length == 3) {
                try {
                    int x = Integer.parseInt(coords[0].trim());
                    int y = Integer.parseInt(coords[1].trim());
                    int z = Integer.parseInt(coords[2].trim());
                    cells.add(new M3space(x, y, z));
                } catch (NumberFormatException e) {
                    // Skip invalid coordinates
                }
            }
        }
        return cells;
    }

    /**
     * Parses compartment definitions JSON into a list of Compartment objects.
     */
    public static List<Compartment> parseCompartmentsJson(String json, HullModule module) {
        List<Compartment> compartments = new ArrayList<>();
        String cleaned = json.trim();
        if (cleaned.startsWith("[")) cleaned = cleaned.substring(1);
        if (cleaned.endsWith("]")) cleaned = cleaned.substring(0, cleaned.length() - 1);

        String[] parts = cleaned.split("\\},\\{");
        for (String part : parts) {
            part = part.trim().replaceFirst("^\\{", "").replaceAll("\\}$", "");
            if (part.isBlank()) continue;

            String name = extractJsonString(part, "name");
            String typeStr = extractJsonString(part, "type");
            String cellsStr = extractJsonArray(part, "cells");

            if (name == null || typeStr == null) continue;

            com.yisy.modules.doramcu.hex.CompartmentType type = mapToCompartmentType(typeStr);

            Compartment compartment = new Compartment(name, type);
            if (cellsStr != null) {
                Set<M3space> cells = parseCellCoordinates("[" + cellsStr + "]");
                for (M3space cell : cells) {
                    M3space moduleCell = module.getCell(cell.getX(), cell.getY(), cell.getZ());
                    if (moduleCell != null) {
                        compartment.addCell(moduleCell);
                    }
                }
            }
            compartments.add(compartment);
        }
        return compartments;
    }

    /**
     * Maps a type string to a CompartmentType.
     * Handles both standard CompartmentType names and domain-specific type names.
     */
    protected static com.yisy.modules.doramcu.hex.CompartmentType mapToCompartmentType(String typeStr) {
        String upper = typeStr.toUpperCase();
        // Try direct match first
        try {
            return com.yisy.modules.doramcu.hex.CompartmentType.valueOf(upper);
        } catch (IllegalArgumentException e) {
            // Fall through to domain-specific mapping
        }
        return switch (upper) {
            case "THRONES_ROOM", "BOSS_CHAMBER" -> com.yisy.modules.doramcu.hex.CompartmentType.COMMAND;
            case "TREASURE_VAULT", "STORAGE" -> com.yisy.modules.doramcu.hex.CompartmentType.CARGO;
            case "LIBRARY", "SANCTUM" -> com.yisy.modules.doramcu.hex.CompartmentType.RESEARCH;
            case "PRISON" -> com.yisy.modules.doramcu.hex.CompartmentType.CREW;
            case "ARMORY", "WORKSHOP" -> com.yisy.modules.doramcu.hex.CompartmentType.WEAPONS;
            case "CORRIDOR", "INTERSECTION" -> com.yisy.modules.doramcu.hex.CompartmentType.TRANSIT;
            case "TRAP_ROOM", "CRYPT", "PIT" -> com.yisy.modules.doramcu.hex.CompartmentType.DEFENSE;
            default -> com.yisy.modules.doramcu.hex.CompartmentType.EMPTY;
        };
    }

    /**
     * Parses connection points JSON into a list of doorway position maps.
     */
    protected static List<Map<String, Object>> parseConnectionPointsJson(String json) {
        List<Map<String, Object>> points = new ArrayList<>();
        String cleaned = json.trim();
        if (cleaned.startsWith("[")) cleaned = cleaned.substring(1);
        if (cleaned.endsWith("]")) cleaned = cleaned.substring(0, cleaned.length() - 1);

        String[] parts = cleaned.split("\\},\\{");
        for (String part : parts) {
            part = part.trim().replaceFirst("^\\{", "").replaceAll("\\}$", "");
            if (part.isBlank()) continue;

            Map<String, Object> point = new HashMap<>();
            String xStr = extractJsonValue(part, "x");
            String yStr = extractJsonValue(part, "y");
            String zStr = extractJsonValue(part, "z");
            if (xStr != null) point.put("x", Integer.parseInt(xStr));
            if (yStr != null) point.put("y", Integer.parseInt(yStr));
            if (zStr != null) point.put("z", Integer.parseInt(zStr));
            String direction = extractJsonString(part, "direction");
            if (direction != null) point.put("direction", direction);

            points.add(point);
        }
        return points;
    }

    /**
     * Extracts a string value from a simple JSON object string.
     */
    protected static String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\":\"([^\"]*)\"";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (matcher.find()) return matcher.group(1);
        return null;
    }

    /**
     * Extracts a JSON array value from a simple JSON object string.
     */
    protected static String extractJsonArray(String json, String key) {
        String pattern = "\"" + key + "\":\\[([^\\]]*)\\]";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (matcher.find()) return matcher.group(1);
        return null;
    }

    /**
     * Extracts a numeric value from a simple JSON object string.
     */
    protected static String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":(\\d+)";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (matcher.find()) return matcher.group(1);
        return null;
    }

    /**
     * Serializes a set of M3space cells to a JSON array string.
     */
    public static String serializeCells(Set<M3space> cells) {
        if (cells == null || cells.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (M3space cell : cells) {
            if (!first) sb.append(",");
            sb.append("\"").append(cell.getX()).append(",")
              .append(cell.getY()).append(",")
              .append(cell.getZ()).append("\"");
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("ModuleTemplate{id=%d, name='%s', dimensions=%dx%dx%d, difficulty=%d}",
                id, name, width, height, depth, difficulty);
    }
}
