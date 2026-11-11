package com.yisy.modules.doramcu.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO for AI-analyzed theme research result.
 * <p>
 * Represents the structured JSON output from the AI provider when analyzing
 * web search results for a given theme. Used to convert unstructured search
 * text into a {@link ThemeResearchResult}.
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ThemeAnalysisResult {

    @JsonProperty("themeName")
    private String themeName;

    @JsonProperty("setting")
    private String setting;

    @JsonProperty("factions")
    private List<String> factions;

    @JsonProperty("technologyLevel")
    private String technologyLevel;

    @JsonProperty("tone")
    private String tone;

    @JsonProperty("namingConvention")
    private String namingConvention;

    @JsonProperty("suggestedEmpireCount")
    private int suggestedEmpireCount;

    @JsonProperty("galaxyShape")
    private String galaxyShape;

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    public String getSetting() {
        return setting;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }

    public List<String> getFactions() {
        return factions;
    }

    public void setFactions(List<String> factions) {
        this.factions = factions;
    }

    public String getTechnologyLevel() {
        return technologyLevel;
    }

    public void setTechnologyLevel(String technologyLevel) {
        this.technologyLevel = technologyLevel;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public String getNamingConvention() {
        return namingConvention;
    }

    public void setNamingConvention(String namingConvention) {
        this.namingConvention = namingConvention;
    }

    public int getSuggestedEmpireCount() {
        return suggestedEmpireCount;
    }

    public void setSuggestedEmpireCount(int suggestedEmpireCount) {
        this.suggestedEmpireCount = suggestedEmpireCount;
    }

    public String getGalaxyShape() {
        return galaxyShape;
    }

    public void setGalaxyShape(String galaxyShape) {
        this.galaxyShape = galaxyShape;
    }

    /**
     * Converts this AI analysis result into a {@link ThemeResearchResult}.
     *
     * @return a ThemeResearchResult with all fields populated
     */
    public ThemeResearchResult toThemeResearchResult() {
        return new ThemeResearchResult(
                themeName != null ? themeName : "Unknown",
                setting != null ? setting : "Unknown setting",
                factions != null && !factions.isEmpty() ? factions : List.of("Faction A", "Faction B"),
                technologyLevel != null ? technologyLevel : "Intermediate",
                tone != null ? tone : "Exploration",
                namingConvention != null ? namingConvention : "astronomical",
                suggestedEmpireCount > 0 ? suggestedEmpireCount : 2,
                galaxyShape != null ? galaxyShape : "spiral"
        );
    }
}
