package com.yisy.modules.doramcu.dto;

import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;
import com.yisy.modules.doramcu.shared.model.RulesEntry;

import java.util.List;

/**
 * Result of theme-based game generation.
 * <p>
 * Contains a fully configured {@link GalaxyGenerationConfig}, generated {@link RulesEntry} objects,
 * and metadata about the theme (naming convention, empire count, galaxy shape).
 * </p>
 */
public class ThemeGameResult {

    private final GalaxyGenerationConfig config;
    private final List<RulesEntry> rules;
    private final String themeName;
    private final String namingConvention;
    private final int empireCount;
    private final String galaxyShape;

    public ThemeGameResult(
            GalaxyGenerationConfig config,
            List<RulesEntry> rules,
            String themeName,
            String namingConvention,
            int empireCount,
            String galaxyShape
    ) {
        this.config = config;
        this.rules = rules;
        this.themeName = themeName;
        this.namingConvention = namingConvention;
        this.empireCount = empireCount;
        this.galaxyShape = galaxyShape;
    }

    public GalaxyGenerationConfig getConfig() {
        return config;
    }

    public List<RulesEntry> getRules() {
        return rules;
    }

    public String getThemeName() {
        return themeName;
    }

    public String getNamingConvention() {
        return namingConvention;
    }

    public int getEmpireCount() {
        return empireCount;
    }

    public String getGalaxyShape() {
        return galaxyShape;
    }

    @Override
    public String toString() {
        return "ThemeGameResult{" +
                "themeName='" + themeName + '\'' +
                ", namingConvention='" + namingConvention + '\'' +
                ", empireCount=" + empireCount +
                ", galaxyShape='" + galaxyShape + '\'' +
                ", ruleCount=" + (rules != null ? rules.size() : 0) +
                '}';
    }
}
