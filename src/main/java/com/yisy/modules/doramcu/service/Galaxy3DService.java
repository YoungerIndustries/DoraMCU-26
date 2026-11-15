package com.yisy.modules.doramcu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 3D galaxy data service for VR/WebGL galaxy visualization.
 * <p>
 * Exports galaxy data in formats suitable for 3D rendering engines like Three.js/WebXR.
 * Provides celestial body positions, colors, sizes, and relationships for immersive
 * galaxy exploration.
 * </p>
 * <p>
 * Features:
 * <ul>
 *   <li>Celestial body export with 3D positions, colors, and sizes</li>
 *   <li>Sector grid data for navigation overlays</li>
 *   <li>Star system relationships and connections</li>
 *   <li>Camera presets for common viewing angles</li>
 *   <li>Level-of-detail (LOD) control for performance</li>
 *   <li>Galaxy statistics for UI overlays</li>
 * </ul>
 * </p>
 */
@Service
public class Galaxy3DService {

    private static final Logger log = LoggerFactory.getLogger(Galaxy3DService.class);
    private static final int MAX_BODIES_PER_QUERY = 10000;

    /** Types of celestial bodies for 3D rendering. */
    public enum BodyType {
        STAR("star", "Star system center"),
        PLANET("planet", "Orbiting planet"),
        MOON("moon", "Orbiting moon"),
        GAS_GIANT("gas_giant", "Gas giant planet"),
        ASTEROID_BELT("asteroid_belt", "Asteroid belt ring"),
        NEBULA("nebula", "Nebula cloud"),
        BLACK_HOLE("black_hole", "Black hole singularity"),
        COMET("comet", "Comet with tail"),
        SPACE_STATION("space_station", "Artificial space station"),
        ASTEROID("asteroid", "Individual asteroid"),
        SECTOR_GRID("sector_grid", "Sector boundary grid"),
        FLEET("fleet", "Space fleet marker");

        private final String id;
        private final String description;

        BodyType(String id, String description) {
            this.id = id;
            this.description = description;
        }

        public String getId() { return id; }
        public String getDescription() { return description; }
    }

    /** Color schemes for different celestial body types. */
    public enum ColorScheme {
        REALISTIC("realistic", "Realistic astronomical colors"),
        SCIFI("scifi", "Science fiction enhanced colors"),
        HEATMAP("heatmap", "Heat map based on activity"),
        EMPIRE("empire", "Color by empire ownership"),
        TERRAIN("terrain", "Color by terrain type");

        private final String id;
        private final String description;

        ColorScheme(String id, String description) {
            this.id = id;
            this.description = description;
        }

        public String getId() { return id; }
        public String getDescription() { return description; }
    }

    /**
     * 3D position in galactic space.
     */
    public static class Position3D {
        private final double x;
        private final double y;
        private final double z;

        public Position3D(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }

        public double distanceTo(Position3D other) {
            double dx = x - other.x;
            double dy = y - other.y;
            double dz = z - other.z;
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }

        @Override
        public String toString() {
            return String.format("Position3D(%.2f, %.2f, %.2f)", x, y, z);
        }
    }

    /**
     * 3D celestial body data for rendering.
     */
    public static class CelestialBody3D {
        private final String id;
        private final String name;
        private final BodyType type;
        private final Position3D position;
        private final double size;
        private final String color;
        private final String parentId;
        private final Map<String, Object> properties;

        private CelestialBody3D(Builder builder) {
            this.id = builder.id;
            this.name = builder.name;
            this.type = builder.type;
            this.position = builder.position;
            this.size = builder.size;
            this.color = builder.color;
            this.parentId = builder.parentId;
            this.properties = new HashMap<>(builder.properties);
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public BodyType getType() { return type; }
        public Position3D getPosition() { return position; }
        public double getSize() { return size; }
        public String getColor() { return color; }
        public String getParentId() { return parentId; }
        public Map<String, Object> getProperties() { return properties; }

        public static Builder builder(String id, BodyType type) {
            return new Builder(id, type);
        }

        public static class Builder {
            private final String id;
            private String name = "";
            private final BodyType type;
            private Position3D position = new Position3D(0, 0, 0);
            private double size = 1.0;
            private String color = "#ffffff";
            private String parentId;
            private final Map<String, Object> properties = new HashMap<>();

            public Builder(String id, BodyType type) {
                this.id = id;
                this.type = type;
            }

            public Builder name(String name) { this.name = name; return this; }
            public Builder position(Position3D position) { this.position = position; return this; }
            public Builder position(double x, double y, double z) { this.position = new Position3D(x, y, z); return this; }
            public Builder size(double size) { this.size = size; return this; }
            public Builder color(String color) { this.color = color; return this; }
            public Builder parentId(String parentId) { this.parentId = parentId; return this; }
            public Builder property(String key, Object value) { this.properties.put(key, value); return this; }
            public CelestialBody3D build() { return new CelestialBody3D(this); }
        }
    }

    /**
     * Camera preset for common viewing angles.
     */
    public static class CameraPreset {
        private final String id;
        private final String name;
        private final Position3D position;
        private final Position3D target;
        private final double fov;
        private final double minDistance;
        private final double maxDistance;

        private CameraPreset(Builder builder) {
            this.id = builder.id;
            this.name = builder.name;
            this.position = builder.position;
            this.target = builder.target;
            this.fov = builder.fov;
            this.minDistance = builder.minDistance;
            this.maxDistance = builder.maxDistance;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public Position3D getPosition() { return position; }
        public Position3D getTarget() { return target; }
        public double getFov() { return fov; }
        public double getMinDistance() { return minDistance; }
        public double getMaxDistance() { return maxDistance; }

        public static Builder builder(String id, String name) {
            return new Builder(id, name);
        }

        public static class Builder {
            private final String id;
            private final String name;
            private Position3D position = new Position3D(0, 0, 100);
            private Position3D target = new Position3D(0, 0, 0);
            private double fov = 60.0;
            private double minDistance = 1.0;
            private double maxDistance = 10000.0;

            public Builder(String id, String name) {
                this.id = id;
                this.name = name;
            }

            public Builder position(Position3D position) { this.position = position; return this; }
            public Builder position(double x, double y, double z) { this.position = new Position3D(x, y, z); return this; }
            public Builder target(Position3D target) { this.target = target; return this; }
            public Builder target(double x, double y, double z) { this.target = new Position3D(x, y, z); return this; }
            public Builder fov(double fov) { this.fov = fov; return this; }
            public Builder minDistance(double minDistance) { this.minDistance = minDistance; return this; }
            public Builder maxDistance(double maxDistance) { this.maxDistance = maxDistance; return this; }
            public CameraPreset build() { return new CameraPreset(this); }
        }
    }

    /**
     * Galaxy 3D data export for a specific galaxy.
     */
    public static class Galaxy3DData {
        private final String galaxyName;
        private final List<CelestialBody3D> bodies;
        private final List<CameraPreset> cameraPresets;
        private final ColorScheme colorScheme;
        private final int totalBodies;
        private final Map<BodyType, Integer> bodyCounts;

        private Galaxy3DData(Builder builder) {
            this.galaxyName = builder.galaxyName;
            this.bodies = new ArrayList<>(builder.bodies);
            this.cameraPresets = new ArrayList<>(builder.cameraPresets);
            this.colorScheme = builder.colorScheme;
            this.totalBodies = builder.totalBodies;
            this.bodyCounts = new EnumMap<>(builder.bodyCounts);
        }

        public String getGalaxyName() { return galaxyName; }
        public List<CelestialBody3D> getBodies() { return bodies; }
        public List<CameraPreset> getCameraPresets() { return cameraPresets; }
        public ColorScheme getColorScheme() { return colorScheme; }
        public int getTotalBodies() { return totalBodies; }
        public Map<BodyType, Integer> getBodyCounts() { return bodyCounts; }

        public static Builder builder(String galaxyName) {
            return new Builder(galaxyName);
        }

        public static class Builder {
            private final String galaxyName;
            private final List<CelestialBody3D> bodies = new ArrayList<>();
            private final List<CameraPreset> cameraPresets = new ArrayList<>();
            private ColorScheme colorScheme = ColorScheme.REALISTIC;
            private int totalBodies;
            private final Map<BodyType, Integer> bodyCounts = new EnumMap<>(BodyType.class);

            public Builder(String galaxyName) {
                this.galaxyName = galaxyName;
                for (BodyType type : BodyType.values()) {
                    bodyCounts.put(type, 0);
                }
            }

            public Builder bodies(List<CelestialBody3D> bodies) {
                this.bodies.addAll(bodies);
                this.totalBodies = bodies.size();
                bodies.forEach(b -> this.bodyCounts.merge(b.getType(), 1, Integer::sum));
                return this;
            }

            public Builder cameraPresets(List<CameraPreset> presets) {
                this.cameraPresets.addAll(presets);
                return this;
            }

            public Builder colorScheme(ColorScheme colorScheme) {
                this.colorScheme = colorScheme;
                return this;
            }

            public Galaxy3DData build() {
                this.totalBodies = bodies.size();
                return new Galaxy3DData(this);
            }
        }
    }

    // ---- Service state ----

    private final Map<String, Galaxy3DData> galaxyCache = new ConcurrentHashMap<>();
    private final Map<String, List<CameraPreset>> cameraPresets = new ConcurrentHashMap<>();

    public Galaxy3DService() {
        initializeCameraPresets();
    }

    /**
     * Initialize default camera presets.
     */
    private void initializeCameraPresets() {
        List<CameraPreset> presets = Arrays.asList(
                CameraPreset.builder("top_down", "Top Down")
                        .position(0, 1000, 0)
                        .target(0, 0, 0)
                        .fov(45.0)
                        .build(),
                CameraPreset.builder("galactic_plane", "Galactic Plane")
                        .position(500, 100, 500)
                        .target(0, 0, 0)
                        .fov(60.0)
                        .build(),
                CameraPreset.builder("close_up", "Close Up")
                        .position(0, 50, 50)
                        .target(0, 0, 0)
                        .fov(30.0)
                        .minDistance(1.0)
                        .maxDistance(200.0)
                        .build(),
                CameraPreset.builder("wide_angle", "Wide Angle")
                        .position(0, 2000, 2000)
                        .target(0, 0, 0)
                        .fov(90.0)
                        .build(),
                CameraPreset.builder("sector_view", "Sector View")
                        .position(100, 50, 100)
                        .target(0, 0, 0)
                        .fov(45.0)
                        .minDistance(10.0)
                        .maxDistance(500.0)
                        .build()
        );
        cameraPresets.put("default", presets);
    }

    /**
     * Get 3D galaxy data for a galaxy name.
     *
     * @param galaxyName the galaxy name
     * @param colorScheme the color scheme to use
     * @return 3D galaxy data, or empty if galaxy not found
     */
    public Optional<Galaxy3DData> getGalaxy3DData(String galaxyName, ColorScheme colorScheme) {
        if (galaxyName == null || galaxyName.isBlank()) {
            return Optional.empty();
        }

        // Check cache first
        String cacheKey = galaxyName + ":" + (colorScheme != null ? colorScheme.getId() : "realistic");
        Galaxy3DData cached = galaxyCache.get(cacheKey);
        if (cached != null) {
            return Optional.of(cached);
        }

        // Generate 3D data from galaxy name using deterministic seed
        long seed = galaxyName.hashCode();
        Random random = new Random(seed);

        List<CelestialBody3D> bodies = generateBodies(galaxyName, random, colorScheme);
        List<CameraPreset> presets = cameraPresets.getOrDefault("default", Collections.emptyList());

        Galaxy3DData data = Galaxy3DData.builder(galaxyName)
                .bodies(bodies)
                .cameraPresets(presets)
                .colorScheme(colorScheme != null ? colorScheme : ColorScheme.REALISTIC)
                .build();

        galaxyCache.put(cacheKey, data);
        return Optional.of(data);
    }

    /**
     * Generate celestial bodies for a galaxy.
     */
    private List<CelestialBody3D> generateBodies(String galaxyName, Random random, ColorScheme colorScheme) {
        List<CelestialBody3D> bodies = new ArrayList<>();

        // Generate stars
        int starCount = 50 + random.nextInt(150);
        for (int i = 0; i < starCount && bodies.size() < MAX_BODIES_PER_QUERY; i++) {
            double x = (random.nextDouble() - 0.5) * 1000;
            double y = (random.nextDouble() - 0.5) * 100;
            double z = (random.nextDouble() - 0.5) * 1000;
            String color = getStarColor(random, colorScheme);
            double size = 2.0 + random.nextDouble() * 8.0;

            bodies.add(CelestialBody3D.builder("star-" + i, BodyType.STAR)
                    .name(galaxyName + "-Star-" + i)
                    .position(x, y, z)
                    .size(size)
                    .color(color)
                    .property("luminosity", 0.5 + random.nextDouble() * 1.5)
                    .property("temperature", 3000 + random.nextInt(15000))
                    .build());
        }

        // Generate planets orbiting stars
        int planetCount = starCount * (2 + random.nextInt(4));
        for (int i = 0; i < planetCount && bodies.size() < MAX_BODIES_PER_QUERY; i++) {
            int parentStar = i % starCount;
            double orbitRadius = 10 + random.nextDouble() * 50;
            double angle = random.nextDouble() * 2 * Math.PI;
            double x = Math.cos(angle) * orbitRadius + (random.nextDouble() - 0.5) * 1000;
            double y = (random.nextDouble() - 0.5) * 20;
            double z = Math.sin(angle) * orbitRadius + (random.nextDouble() - 0.5) * 1000;
            String color = getPlanetColor(random, colorScheme);
            double size = 0.5 + random.nextDouble() * 3.0;

            bodies.add(CelestialBody3D.builder("planet-" + i, BodyType.PLANET)
                    .name(galaxyName + "-Planet-" + i)
                    .position(x, y, z)
                    .size(size)
                    .color(color)
                    .parentId("star-" + parentStar)
                    .property("orbitRadius", orbitRadius)
                    .property("habitable", random.nextDouble() < 0.1)
                    .build());
        }

        // Generate gas giants
        int gasGiantCount = 10 + random.nextInt(30);
        for (int i = 0; i < gasGiantCount && bodies.size() < MAX_BODIES_PER_QUERY; i++) {
            double x = (random.nextDouble() - 0.5) * 800;
            double y = (random.nextDouble() - 0.5) * 50;
            double z = (random.nextDouble() - 0.5) * 800;
            String color = getGasGiantColor(random, colorScheme);
            double size = 4.0 + random.nextDouble() * 6.0;

            bodies.add(CelestialBody3D.builder("gas-giant-" + i, BodyType.GAS_GIANT)
                    .name(galaxyName + "-GasGiant-" + i)
                    .position(x, y, z)
                    .size(size)
                    .color(color)
                    .property("atmosphere", "hydrogen_helium")
                    .build());
        }

        // Generate nebulae
        int nebulaCount = 5 + random.nextInt(15);
        for (int i = 0; i < nebulaCount && bodies.size() < MAX_BODIES_PER_QUERY; i++) {
            double x = (random.nextDouble() - 0.5) * 1200;
            double y = (random.nextDouble() - 0.5) * 200;
            double z = (random.nextDouble() - 0.5) * 1200;
            String color = getNebulaColor(random, colorScheme);
            double size = 50 + random.nextDouble() * 100;

            bodies.add(CelestialBody3D.builder("nebula-" + i, BodyType.NEBULA)
                    .name(galaxyName + "-Nebula-" + i)
                    .position(x, y, z)
                    .size(size)
                    .color(color)
                    .property("type", random.nextBoolean() ? "emission" : "reflection")
                    .build());
        }

        // Generate asteroid belts
        int asteroidBeltCount = 10 + random.nextInt(20);
        for (int i = 0; i < asteroidBeltCount && bodies.size() < MAX_BODIES_PER_QUERY; i++) {
            double x = (random.nextDouble() - 0.5) * 600;
            double y = (random.nextDouble() - 0.5) * 30;
            double z = (random.nextDouble() - 0.5) * 600;

            bodies.add(CelestialBody3D.builder("asteroid-belt-" + i, BodyType.ASTEROID_BELT)
                    .name(galaxyName + "-AsteroidBelt-" + i)
                    .position(x, y, z)
                    .size(20 + random.nextDouble() * 30)
                    .color("#888888")
                    .property("asteroidCount", 100 + random.nextInt(900))
                    .build());
        }

        // Generate black holes
        int blackHoleCount = 1 + random.nextInt(3);
        for (int i = 0; i < blackHoleCount && bodies.size() < MAX_BODIES_PER_QUERY; i++) {
            double x = (random.nextDouble() - 0.5) * 500;
            double y = (random.nextDouble() - 0.5) * 50;
            double z = (random.nextDouble() - 0.5) * 500;

            bodies.add(CelestialBody3D.builder("black-hole-" + i, BodyType.BLACK_HOLE)
                    .name(galaxyName + "-BlackHole-" + i)
                    .position(x, y, z)
                    .size(5.0 + random.nextDouble() * 10.0)
                    .color("#000000")
                    .property("mass", 100 + random.nextDouble() * 900)
                    .property("eventHorizon", 2.0 + random.nextDouble() * 5.0)
                    .build());
        }

        // Generate space stations
        int stationCount = 20 + random.nextInt(50);
        for (int i = 0; i < stationCount && bodies.size() < MAX_BODIES_PER_QUERY; i++) {
            double x = (random.nextDouble() - 0.5) * 400;
            double y = (random.nextDouble() - 0.5) * 20;
            double z = (random.nextDouble() - 0.5) * 400;

            bodies.add(CelestialBody3D.builder("station-" + i, BodyType.SPACE_STATION)
                    .name(galaxyName + "-Station-" + i)
                    .position(x, y, z)
                    .size(1.0 + random.nextDouble() * 2.0)
                    .color("#4488ff")
                    .property("capacity", 100 + random.nextInt(900))
                    .build());
        }

        return bodies;
    }

    /**
     * Get star color based on type and color scheme.
     */
    private String getStarColor(Random random, ColorScheme colorScheme) {
        if (colorScheme == ColorScheme.HEATMAP) {
            return random.nextDouble() > 0.5 ? "#ff0000" : "#0000ff";
        }
        // Realistic star colors based on temperature
        String[] colors = {"#ffffcc", "#cccccc", "#ffccaa", "#ffaaaa", "#aaccff", "#9999ff"};
        return colors[random.nextInt(colors.length)];
    }

    /**
     * Get planet color based on type and color scheme.
     */
    private String getPlanetColor(Random random, ColorScheme colorScheme) {
        if (colorScheme == ColorScheme.HEATMAP) {
            return random.nextDouble() > 0.5 ? "#00ff00" : "#ffff00";
        }
        // Realistic planet colors
        String[] colors = {"#4488ff", "#884422", "#228844", "#888888", "#ff8844", "#44ff88"};
        return colors[random.nextInt(colors.length)];
    }

    /**
     * Get gas giant color based on color scheme.
     */
    private String getGasGiantColor(Random random, ColorScheme colorScheme) {
        if (colorScheme == ColorScheme.SCIFI) {
            String[] colors = {"#ff44ff", "#44ffff", "#ffff44", "#ff8844"};
            return colors[random.nextInt(colors.length)];
        }
        // Realistic gas giant colors
        String[] colors = {"#ddaa77", "#cc8866", "#bb9977", "#aa8866"};
        return colors[random.nextInt(colors.length)];
    }

    /**
     * Get nebula color based on color scheme.
     */
    private String getNebulaColor(Random random, ColorScheme colorScheme) {
        if (colorScheme == ColorScheme.SCIFI) {
            String[] colors = {"#ff00ff", "#00ffff", "#ff0088", "#8800ff"};
            return colors[random.nextInt(colors.length)];
        }
        // Realistic nebula colors
        String[] colors = {"#ff4488", "#4488ff", "#88ff44", "#ff8844", "#44ff88"};
        return colors[random.nextInt(colors.length)];
    }

    /**
     * Get all available body types.
     */
    public List<BodyType> getBodyTypes() {
        return Arrays.asList(BodyType.values());
    }

    /**
     * Get all available color schemes.
     */
    public List<ColorScheme> getColorSchemes() {
        return Arrays.asList(ColorScheme.values());
    }

    /**
     * Get camera presets.
     */
    public List<CameraPreset> getCameraPresets() {
        return cameraPresets.getOrDefault("default", Collections.emptyList());
    }

    /**
     * Get bodies filtered by type.
     */
    public List<CelestialBody3D> getBodiesByType(String galaxyName, BodyType type) {
        return getGalaxy3DData(galaxyName, ColorScheme.REALISTIC)
                .map(data -> data.getBodies().stream()
                        .filter(b -> b.getType() == type)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    /**
     * Get bodies within a radius of a position.
     */
    public List<CelestialBody3D> getBodiesNearPosition(String galaxyName, Position3D position, double radius) {
        return getGalaxy3DData(galaxyName, ColorScheme.REALISTIC)
                .map(data -> data.getBodies().stream()
                        .filter(b -> b.getPosition().distanceTo(position) <= radius)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    /**
     * Get galaxy statistics.
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cachedGalaxies", galaxyCache.size());
        stats.put("cameraPresets", cameraPresets.size());
        stats.put("bodyTypes", BodyType.values().length);
        stats.put("colorSchemes", ColorScheme.values().length);
        return stats;
    }

    /**
     * Clear the cache.
     */
    public void clearCache() {
        galaxyCache.clear();
    }

    /**
     * Remove a galaxy from cache.
     */
    public void removeGalaxyCache(String galaxyName) {
        galaxyCache.keySet().removeIf(key -> key.startsWith(galaxyName + ":"));
    }
}
