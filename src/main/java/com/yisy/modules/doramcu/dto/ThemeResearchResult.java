package com.yisy.modules.doramcu.dto;

import java.util.List;

/**
 * Result of web search + AI analysis for a custom game theme.
 * <p>
 * Contains structured information extracted about a theme (e.g., "Star Trek TNG")
 * that can be used to generate galaxy configuration and rules entries.
 * </p>
 */
public class ThemeResearchResult {

    private final String themeName;
    private final String setting;
    private final List<String> factions;
    private final String technologyLevel;
    private final String tone;
    private final String namingConvention;
    private final int suggestedEmpireCount;
    private final String galaxyShape;

    public ThemeResearchResult(
            String themeName,
            String setting,
            List<String> factions,
            String technologyLevel,
            String tone,
            String namingConvention,
            int suggestedEmpireCount,
            String galaxyShape
    ) {
        this.themeName = themeName;
        this.setting = setting;
        this.factions = factions;
        this.technologyLevel = technologyLevel;
        this.tone = tone;
        this.namingConvention = namingConvention;
        this.suggestedEmpireCount = suggestedEmpireCount;
        this.galaxyShape = galaxyShape;
    }

    public String getThemeName() {
        return themeName;
    }

    public String getSetting() {
        return setting;
    }

    public List<String> getFactions() {
        return factions;
    }

    public String getTechnologyLevel() {
        return technologyLevel;
    }

    public String getTone() {
        return tone;
    }

    public String getNamingConvention() {
        return namingConvention;
    }

    public int getSuggestedEmpireCount() {
        return suggestedEmpireCount;
    }

    public String getGalaxyShape() {
        return galaxyShape;
    }

    @Override
    public String toString() {
        return "ThemeResearchResult{" +
                "themeName='" + themeName + '\'' +
                ", setting='" + setting + '\'' +
                ", factions=" + factions +
                ", technologyLevel='" + technologyLevel + '\'' +
                ", tone='" + tone + '\'' +
                ", namingConvention='" + namingConvention + '\'' +
                ", suggestedEmpireCount=" + suggestedEmpireCount +
                ", galaxyShape='" + galaxyShape + '\'' +
                '}';
    }
}
