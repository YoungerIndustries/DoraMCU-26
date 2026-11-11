package com.yisy.modules.doramcu.earth2026.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Galaxy creation rules from the Earth 2026 rules database.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class E26GalaxyRules {

    private long defaultGalaxySeed;
    private long voidGalaxySeed;
    private long earthGalaxySeed;
    private long testGalaxySeed;
    private int maxSectorsPerGalaxy;
    private int maxSystemsPerSector;
    private int maxPlanetsPerSystem;
    private int maxMoonsPerPlanet;
    private int defaultTechLevel;
    private int minTechLevel;
    private int maxTechLevel;
    private List<E26GalaxyType> galaxyTypes;

    public E26GalaxyRules() {
    }

    public long getDefaultGalaxySeed() { return defaultGalaxySeed; }
    public void setDefaultGalaxySeed(long defaultGalaxySeed) { this.defaultGalaxySeed = defaultGalaxySeed; }

    public long getVoidGalaxySeed() { return voidGalaxySeed; }
    public void setVoidGalaxySeed(long voidGalaxySeed) { this.voidGalaxySeed = voidGalaxySeed; }

    public long getEarthGalaxySeed() { return earthGalaxySeed; }
    public void setEarthGalaxySeed(long earthGalaxySeed) { this.earthGalaxySeed = earthGalaxySeed; }

    public long getTestGalaxySeed() { return testGalaxySeed; }
    public void setTestGalaxySeed(long testGalaxySeed) { this.testGalaxySeed = testGalaxySeed; }

    public int getMaxSectorsPerGalaxy() { return maxSectorsPerGalaxy; }
    public void setMaxSectorsPerGalaxy(int maxSectorsPerGalaxy) { this.maxSectorsPerGalaxy = maxSectorsPerGalaxy; }

    public int getMaxSystemsPerSector() { return maxSystemsPerSector; }
    public void setMaxSystemsPerSector(int maxSystemsPerSector) { this.maxSystemsPerSector = maxSystemsPerSector; }

    public int getMaxPlanetsPerSystem() { return maxPlanetsPerSystem; }
    public void setMaxPlanetsPerSystem(int maxPlanetsPerSystem) { this.maxPlanetsPerSystem = maxPlanetsPerSystem; }

    public int getMaxMoonsPerPlanet() { return maxMoonsPerPlanet; }
    public void setMaxMoonsPerPlanet(int maxMoonsPerPlanet) { this.maxMoonsPerPlanet = maxMoonsPerPlanet; }

    public int getDefaultTechLevel() { return defaultTechLevel; }
    public void setDefaultTechLevel(int defaultTechLevel) { this.defaultTechLevel = defaultTechLevel; }

    public int getMinTechLevel() { return minTechLevel; }
    public void setMinTechLevel(int minTechLevel) { this.minTechLevel = minTechLevel; }

    public int getMaxTechLevel() { return maxTechLevel; }
    public void setMaxTechLevel(int maxTechLevel) { this.maxTechLevel = maxTechLevel; }

    public List<E26GalaxyType> getGalaxyTypes() { return galaxyTypes; }
    public void setGalaxyTypes(List<E26GalaxyType> galaxyTypes) { this.galaxyTypes = galaxyTypes; }

    /**
     * Galaxy type definition.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class E26GalaxyType {
        private String id;
        private String name;
        private String description;
        private int maxArms;
        private int maxSystemsPerArm;
        private int maxSystemsPerCore;
        private int maxSystems;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public int getMaxArms() { return maxArms; }
        public void setMaxArms(int maxArms) { this.maxArms = maxArms; }

        public int getMaxSystemsPerArm() { return maxSystemsPerArm; }
        public void setMaxSystemsPerArm(int maxSystemsPerArm) { this.maxSystemsPerArm = maxSystemsPerArm; }

        public int getMaxSystemsPerCore() { return maxSystemsPerCore; }
        public void setMaxSystemsPerCore(int maxSystemsPerCore) { this.maxSystemsPerCore = maxSystemsPerCore; }

        public int getMaxSystems() { return maxSystems; }
        public void setMaxSystems(int maxSystems) { this.maxSystems = maxSystems; }
    }
}
