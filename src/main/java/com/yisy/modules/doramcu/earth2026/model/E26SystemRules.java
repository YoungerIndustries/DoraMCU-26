package com.yisy.modules.doramcu.earth2026.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Star system creation rules from the Earth 2026 rules database.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class E26SystemRules {

    private List<E26StarType> starTypes;
    private List<E26PlanetType> planetTypes;
    private E26HabitableZone habitableZone;

    public E26SystemRules() {
    }

    public List<E26StarType> getStarTypes() { return starTypes; }
    public void setStarTypes(List<E26StarType> starTypes) { this.starTypes = starTypes; }

    public List<E26PlanetType> getPlanetTypes() { return planetTypes; }
    public void setPlanetTypes(List<E26PlanetType> planetTypes) { this.planetTypes = planetTypes; }

    public E26HabitableZone getHabitableZone() { return habitableZone; }
    public void setHabitableZone(E26HabitableZone habitableZone) { this.habitableZone = habitableZone; }

    /**
     * Star type definition.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class E26StarType {
        private String id;
        private String name;
        private String mass;
        private String lifespan;
        private String temperature;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getMass() { return mass; }
        public void setMass(String mass) { this.mass = mass; }

        public String getLifespan() { return lifespan; }
        public void setLifespan(String lifespan) { this.lifespan = lifespan; }

        public String getTemperature() { return temperature; }
        public void setTemperature(String temperature) { this.temperature = temperature; }
    }

    /**
     * Planet type definition.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class E26PlanetType {
        private String id;
        private String name;
        private String description;
        private double minRadius;
        private double maxRadius;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public double getMinRadius() { return minRadius; }
        public void setMinRadius(double minRadius) { this.minRadius = minRadius; }

        public double getMaxRadius() { return maxRadius; }
        public void setMaxRadius(double maxRadius) { this.maxRadius = maxRadius; }
    }

    /**
     * Habitable zone definition.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class E26HabitableZone {
        private double minDistance;
        private double maxDistance;
        private String unit;

        public double getMinDistance() { return minDistance; }
        public void setMinDistance(double minDistance) { this.minDistance = minDistance; }

        public double getMaxDistance() { return maxDistance; }
        public void setMaxDistance(double maxDistance) { this.maxDistance = maxDistance; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }
}
