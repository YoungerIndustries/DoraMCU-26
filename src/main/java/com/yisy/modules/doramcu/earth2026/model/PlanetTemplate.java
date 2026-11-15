package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a reusable planet template.
 * <p>
 * Planet templates define the structure for creating fully-built planets in other
 * galaxies. The Earth 2026 template serves as the default blueprint, containing
 * country definitions, capital cities, Younger Industries offices, and transport
 * routes that can be applied when creating new planets.
 * </p>
 * <p>
 * When a new planet is created using a template, the GalaxyCreationService clones
 * the template's structure, adapting names and IDs for the new planet while
 * preserving the overall layout of capitals, offices, and transport connections.
 * </p>
 */
@Entity
@Table(name = "planet_templates", indexes = {
        @Index(name = "idx_planet_template_name", columnList = "name"),
        @Index(name = "idx_planet_template_type", columnList = "template_type")
})
public class PlanetTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Template name (e.g., "Earth 2026", "Standard Earth-like").
     */
    @Column(nullable = false, length = 100, unique = true)
    private String name;

    /**
     * Template type (e.g., "Earth", "Terraformed", "Gas Giant Station").
     */
    @Column(name = "template_type", nullable = false, length = 50)
    private String templateType;

    /**
     * Description of the template and what it provides.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Number of countries/empires in the template.
     */
    @Column(name = "country_count")
    private Integer countryCount;

    /**
     * Number of capital cities in the template.
     */
    @Column(name = "capital_count")
    private Integer capitalCount;

    /**
     * Number of Younger Industries offices in the template.
     */
    @Column(name = "office_count")
    private Integer officeCount;

    /**
     * Number of transport routes in the template.
     */
    @Column(name = "route_count")
    private Integer routeCount;

    /**
     * Default Tech Level for planets created from this template.
     */
    @Column(name = "default_tech_level", length = 10)
    private String defaultTechLevel;

    /**
     * Whether this template includes Younger Industries offices.
     */
    @Column(name = "includes_offices", nullable = false)
    private boolean includesOffices = true;

    /**
     * Whether this template includes transport routes.
     */
    @Column(name = "includes_routes", nullable = false)
    private boolean includesRoutes = true;

    /**
     * Whether this template is available for new planet creation.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Source planet ID if this template was derived from an existing planet.
     */
    @Column(name = "source_planet_id")
    private Long sourcePlanetId;

    public PlanetTemplate() {
    }

    public PlanetTemplate(String name, String templateType, String description) {
        this.name = name;
        this.templateType = templateType;
        this.description = description;
    }

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

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCountryCount() {
        return countryCount;
    }

    public void setCountryCount(Integer countryCount) {
        this.countryCount = countryCount;
    }

    public Integer getCapitalCount() {
        return capitalCount;
    }

    public void setCapitalCount(Integer capitalCount) {
        this.capitalCount = capitalCount;
    }

    public Integer getOfficeCount() {
        return officeCount;
    }

    public void setOfficeCount(Integer officeCount) {
        this.officeCount = officeCount;
    }

    public Integer getRouteCount() {
        return routeCount;
    }

    public void setRouteCount(Integer routeCount) {
        this.routeCount = routeCount;
    }

    public String getDefaultTechLevel() {
        return defaultTechLevel;
    }

    public void setDefaultTechLevel(String defaultTechLevel) {
        this.defaultTechLevel = defaultTechLevel;
    }

    public boolean isIncludesOffices() {
        return includesOffices;
    }

    public void setIncludesOffices(boolean includesOffices) {
        this.includesOffices = includesOffices;
    }

    public boolean isIncludesRoutes() {
        return includesRoutes;
    }

    public void setIncludesRoutes(boolean includesRoutes) {
        this.includesRoutes = includesRoutes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getSourcePlanetId() {
        return sourcePlanetId;
    }

    public void setSourcePlanetId(Long sourcePlanetId) {
        this.sourcePlanetId = sourcePlanetId;
    }

    @Override
    public String toString() {
        return "PlanetTemplate{id=" + id + ", name='" + name + "', type='" + templateType +
                "', countries=" + countryCount + ", capitals=" + capitalCount + "}";
    }
}
