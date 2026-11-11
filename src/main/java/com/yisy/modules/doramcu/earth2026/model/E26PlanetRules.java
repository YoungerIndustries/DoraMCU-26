package com.yisy.modules.doramcu.earth2026.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Planet creation rules from the Earth 2026 rules database.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class E26PlanetRules {

    private List<E26GovernmentType> governmentTypes;
    private E26LawLevels lawLevels;
    private List<E26WorldModifier> worldModifiers;

    public E26PlanetRules() {
    }

    public List<E26GovernmentType> getGovernmentTypes() { return governmentTypes; }
    public void setGovernmentTypes(List<E26GovernmentType> governmentTypes) { this.governmentTypes = governmentTypes; }

    public E26LawLevels getLawLevels() { return lawLevels; }
    public void setLawLevels(E26LawLevels lawLevels) { this.lawLevels = lawLevels; }

    public List<E26WorldModifier> getWorldModifiers() { return worldModifiers; }
    public void setWorldModifiers(List<E26WorldModifier> worldModifiers) { this.worldModifiers = worldModifiers; }

    /**
     * Government type definition.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class E26GovernmentType {
        private String id;
        private String name;
        private int lawLevel;
        private int stability;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getLawLevel() { return lawLevel; }
        public void setLawLevel(int lawLevel) { this.lawLevel = lawLevel; }

        public int getStability() { return stability; }
        public void setStability(int stability) { this.stability = stability; }
    }

    /**
     * Law level definitions.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class E26LawLevels {
        private int min;
        private int max;
        private Map<String, String> descriptions;

        public int getMin() { return min; }
        public void setMin(int min) { this.min = min; }

        public int getMax() { return max; }
        public void setMax(int max) { this.max = max; }

        public Map<String, String> getDescriptions() { return descriptions; }
        public void setDescriptions(Map<String, String> descriptions) { this.descriptions = descriptions; }
    }

    /**
     * World modifier definition.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class E26WorldModifier {
        private String id;
        private String name;
        private String description;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
