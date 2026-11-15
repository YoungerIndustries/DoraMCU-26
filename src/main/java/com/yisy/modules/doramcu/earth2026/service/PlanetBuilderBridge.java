package com.yisy.modules.doramcu.earth2026.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Country;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Location;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Region;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CountryRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026LocationRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026RegionRepository;
import com.yisy.modules.doramcu.event.GalaxyEventService;
import com.yisy.modules.doramcu.event.GalaxyEventType;
import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.planet.*;
import com.yisy.modules.doramcu.service.GalaxyStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Bridge service that maps Earth2026 JPA entities to hex-based planet objects
 * managed by the Transport Tycoon {@link PlanetSurfaceBuilder}.
 * <p>
 * This service converts:
 * <ul>
 *   <li>Earth2026Country â†’ Settlement on the hex grid (at capital position)</li>
 *   <li>Earth2026Region â†’ Industries based on region economy</li>
 *   <li>Earth2026Location â†’ Stations on the hex grid</li>
 * </ul>
 * <p>
 * All built objects are registered in {@link GalaxyStateService} for lifecycle tracking.
 * The bridge uses latitude/longitude from Earth2026 data to find the closest hex cell
 * on the planet surface for placement.
 * </p>
 */
@Service
public class PlanetBuilderBridge {

    private static final Logger log = LoggerFactory.getLogger(PlanetBuilderBridge.class);

    private final PlanetSurfaceBuilder surfaceBuilder;
    private final Earth2026CountryRepository countryRepository;
    private final Earth2026LocationRepository locationRepository;
    private final Earth2026RegionRepository regionRepository;
    private final GalaxyStateService galaxyStateService;
    private final GalaxyEventService galaxyEventService;

    /** Cache: country ISO code â†’ Settlement */
    private final Map<String, Settlement> settlementCache = new HashMap<>();

    /** Cache: location ID â†’ Station */
    private final Map<Long, Station> stationCache = new HashMap<>();

    /** Cache: region ID â†’ list of Industries */
    private final Map<Long, List<Industry>> industryCache = new HashMap<>();

    public PlanetBuilderBridge(PlanetSurfaceBuilder surfaceBuilder,
                                Earth2026CountryRepository countryRepository,
                                Earth2026LocationRepository locationRepository,
                                Earth2026RegionRepository regionRepository,
                                GalaxyStateService galaxyStateService,
                                GalaxyEventService galaxyEventService) {
        this.surfaceBuilder = surfaceBuilder;
        this.countryRepository = countryRepository;
        this.locationRepository = locationRepository;
        this.regionRepository = regionRepository;
        this.galaxyStateService = galaxyStateService;
        this.galaxyEventService = galaxyEventService;
    }

    // ---- Main Build Methods ----

    /**
     * Builds the entire planet surface from Earth2026 country data.
     * <p>
     * This method:
     * <ol>
     *   <li>Generates biomes on the planet surface using procedural generation</li>
     *   <li>Creates settlements at all country capital positions</li>
     *   <li>Creates stations at all location entries</li>
     *   <li>Registers all objects in GalaxyStateService</li>
     * </ol>
     *
     * @param planet the hex-based planet to build on
     * @param galaxyName the galaxy name for event publishing
     * @return the number of settlements created
     */
    public int buildPlanetFromCountries(Planet planet, String galaxyName) {
        String planetName = planet.getName() != null ? planet.getName() : "Planet " + planet.getId();
        log.info("Building planet surface for {} from Earth2026 country data", planetName);

        // Publish planet build started event
        galaxyEventService.publish(GalaxyEventType.PLANET_BUILD_STARTED, planetName, Long.valueOf(planet.getId()),
                "planetName", planetName, "galaxyName", galaxyName);

        // Step 1: Generate biomes
        int hexCount = surfaceBuilder.generateBiomes(planet, planet.getId());
        log.info("Generated biomes for {} hex cells on {}", hexCount, planetName);

        galaxyEventService.publish(GalaxyEventType.SECTOR_GENERATED, planetName, Long.valueOf(planet.getId()),
                "planetName", planetName, "hexCount", String.valueOf(hexCount));

        // Step 2: Create settlements at country capitals
        List<Earth2026Country> countries = countryRepository.findAll();
        log.info("Creating settlements for {} countries on {}", countries.size(), planetName);

        int settlementCount = 0;
        for (Earth2026Country country : countries) {
            try {
                Settlement settlement = createSettlementForCountry(planet, country, galaxyName);
                if (settlement != null) {
                    settlementCache.put(country.getIsoCode(), settlement);
                    settlementCount++;
                }
            } catch (Exception e) {
                log.warn("Failed to create settlement for country {}: {}", country.getName(), e.getMessage());
            }
        }

        log.info("Created {} settlements on {}", settlementCount, planetName);

        // Step 3: Create stations at all locations
        int stationCount = buildStationsAtLocations(planet, galaxyName);
        log.info("Created {} stations on {}", stationCount, planetName);

        // Publish planet build completed event
        galaxyEventService.publish(GalaxyEventType.PLANET_BUILD_COMPLETED, planetName, Long.valueOf(planet.getId()),
                "planetName", planetName, "totalCapitals", String.valueOf(settlementCount),
                "totalOffices", String.valueOf(stationCount));

        return settlementCount;
    }

    /**
     * Builds industries for a specific region based on its economy data.
     *
     * @param planet the hex-based planet
     * @param region the Earth2026 region
     * @param galaxyName the galaxy name for event publishing
     * @return the number of industries created
     */
    public int buildRegionIndustries(Planet planet, Earth2026Region region, String galaxyName) {
        String planetName = planet.getName() != null ? planet.getName() : "Planet " + planet.getId();
        log.info("Building industries for region {} on {}", region.getName(), planetName);

        List<IndustryType> industryTypes = determineIndustriesForRegion(region);
        int industryCount = 0;
        List<Industry> industries = new ArrayList<>();

        for (IndustryType industryType : industryTypes) {
            try {
                // Find a suitable hex near the region's capital settlement
                Hex3D hex = findSuitableHexForIndustry(planet, region, industryType);
                if (hex != null) {
                    String empireId = "country_" + region.getCountryId();
                    Industry industry = surfaceBuilder.buildIndustry(planet, industryType, hex, empireId);
                    industries.add(industry);
                    industryCount++;

                    // Register in GalaxyStateService
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("industryType", industryType.getDisplayName());
                    meta.put("regionId", region.getId());
                    meta.put("regionName", region.getName());
                    meta.put("hexX", hex.getX());
                    meta.put("hexY", hex.getY());
                    meta.put("hexZ", hex.getZ());

                    galaxyStateService.registerObject(galaxyName, "INDUSTRY",
                            "industry_" + industry.getId(), industry.getId(),
                            null, null, meta);
                    galaxyStateService.markBuilt(galaxyName, "INDUSTRY", "industry_" + industry.getId(), meta);

                    galaxyEventService.publish(GalaxyEventType.BUILD_REQUEST_COMPLETED, planetName, Long.valueOf(planet.getId()),
                            "planetName", planetName, "objectType", "INDUSTRY",
                            "objectName", industryType.getDisplayName(), "regionName", region.getName());

                    log.info("  Built {} at hex ({},{},{}) for region {}",
                            industryType.getDisplayName(), hex.getX(), hex.getY(), hex.getZ(), region.getName());
                }
            } catch (Exception e) {
                log.warn("Failed to build {} for region {}: {}",
                        industryType.getDisplayName(), region.getName(), e.getMessage());
            }
        }

        industryCache.put(region.getId(), industries);
        log.info("Created {} industries for region {} on {}", industryCount, region.getName(), planetName);
        return industryCount;
    }

    /**
     * Builds stations at all Earth2026Location entries.
     *
     * @param planet the hex-based planet
     * @param galaxyName the galaxy name for event publishing
     * @return the number of stations created
     */
    public int buildStationsAtLocations(Planet planet, String galaxyName) {
        String planetName = planet.getName() != null ? planet.getName() : "Planet " + planet.getId();
        log.info("Building stations at all Earth2026 locations on {}", planetName);

        List<Earth2026Location> locations = locationRepository.findAll();
        int stationCount = 0;

        for (Earth2026Location location : locations) {
            try {
                Station station = createStationForLocation(planet, location, galaxyName);
                if (station != null) {
                    stationCache.put(location.getId(), station);
                    stationCount++;
                }
            } catch (Exception e) {
                log.warn("Failed to create station for location {}: {}", location.getName(), e.getMessage());
            }
        }

        log.info("Created {} stations on {}", stationCount, planetName);
        return stationCount;
    }

    // ---- Settlement Creation ----

    /**
     * Creates a settlement for a country at its capital position.
     */
    private Settlement createSettlementForCountry(Planet planet, Earth2026Country country, String galaxyName) {
        String planetName = planet.getName() != null ? planet.getName() : "Planet " + planet.getId();

        // Find the hex closest to the country's capital latitude
        Hex3D hex = findHexForCapital(planet, country);
        if (hex == null) {
            log.warn("No suitable hex found for {} capital {}", country.getName(), country.getCapitalCity());
            return null;
        }

        // Determine settlement size from population
        Settlement.Size size = mapPopulationToSize(country.getPopulationMillions());

        // Create the settlement
        String empireId = "country_" + country.getId();
        Settlement settlement = surfaceBuilder.foundSettlement(planet, country.getCapitalCity(), hex, empireId);

        // Update settlement size based on population
        updateSettlementSize(settlement, size);

        // Register in GalaxyStateService
        Map<String, Object> meta = new HashMap<>();
        meta.put("isoCode", country.getIsoCode());
        meta.put("countryName", country.getName());
        meta.put("capitalCity", country.getCapitalCity());
        meta.put("population", country.getPopulationMillions());
        meta.put("gdp", country.getGdpTrillions());
        meta.put("settlementSize", size.name());
        meta.put("hexX", hex.getX());
        meta.put("hexY", hex.getY());
        meta.put("hexZ", hex.getZ());

        galaxyStateService.registerObject(galaxyName, "SETTLEMENT",
                "settlement_" + settlement.getId(), settlement.getName(),
                null, null, meta);
        galaxyStateService.markBuilt(galaxyName, "SETTLEMENT", "settlement_" + settlement.getId(), meta);

        galaxyEventService.publish(GalaxyEventType.CAPITAL_BUILT, planetName, Long.valueOf(planet.getId()),
                "planetName", planetName, "capitalName", settlement.getName(),
                "countryName", country.getName(), "settlementSize", size.name());

        // Publish country added event
        galaxyEventService.publish(GalaxyEventType.COUNTRY_ADDED, planetName, Long.valueOf(planet.getId()),
                "planetName", planetName, "countryName", country.getName(),
                "capitalName", country.getCapitalCity(),
                "population", String.valueOf(country.getPopulationMillions() != null ? country.getPopulationMillions() : "0"));

        log.info("  Created settlement '{}' (size: {}) for {} at hex ({},{},{})",
                settlement.getName(), size, country.getName(), hex.getX(), hex.getY(), hex.getZ());
        return settlement;
    }

    /**
     * Maps country population in millions to Settlement size category.
     */
    private Settlement.Size mapPopulationToSize(Double populationMillions) {
        if (populationMillions == null) {
            return Settlement.Size.VILLAGE;
        }
        if (populationMillions < 1.0) {
            return Settlement.Size.VILLAGE;
        } else if (populationMillions < 10.0) {
            return Settlement.Size.TOWN;
        } else if (populationMillions < 50.0) {
            return Settlement.Size.CITY;
        } else {
            return Settlement.Size.METROPOLIS;
        }
    }

    /**
     * Updates a settlement's growth rate based on its size.
     * Smaller settlements grow faster (lower growth interval).
     */
    private void updateSettlementSize(Settlement settlement, Settlement.Size size) {
        // Settlement size is set during creation; we can only adjust growth rate
        settlement.setGrowthRate(size.getGrowthInterval());
    }

    // ---- Station Creation ----

    /**
     * Creates a station for an Earth2026Location.
     */
    private Station createStationForLocation(Planet planet, Earth2026Location location, String galaxyName) {
        String planetName = planet.getName() != null ? planet.getName() : "Planet " + planet.getId();

        // Find the hex closest to the location's coordinates
        Hex3D hex = findHexForLocation(planet, location);
        if (hex == null) {
            log.warn("No suitable hex found for location {}", location.getName());
            return null;
        }

        // Create the station
        String empireId = "country_" + resolveCountryId(location.getCountry());
        Station station = surfaceBuilder.buildStation(planet, location.getName(), hex, empireId);

        // Register in GalaxyStateService
        Map<String, Object> meta = new HashMap<>();
        meta.put("locationId", location.getId());
        meta.put("locationName", location.getName());
        meta.put("city", location.getCity());
        meta.put("country", location.getCountry());
        meta.put("locationType", location.getType());
        meta.put("hexX", hex.getX());
        meta.put("hexY", hex.getY());
        meta.put("hexZ", hex.getZ());

        galaxyStateService.registerObject(galaxyName, "STATION",
                "station_" + station.getId(), station.getName(),
                null, null, meta);
        galaxyStateService.markBuilt(galaxyName, "STATION", "station_" + station.getId(), meta);

        galaxyEventService.publish(GalaxyEventType.OFFICE_OPENED, planetName, Long.valueOf(planet.getId()),
                "planetName", planetName, "officeName", station.getName(),
                "cityName", location.getCity(), "locationType", location.getType());

        log.info("  Created station '{}' for {} at hex ({},{},{})",
                station.getName(), location.getName(), hex.getX(), hex.getY(), hex.getZ());
        return station;
    }

    /**
     * Resolves a country name to its country ID for empire ownership.
     */
    private String resolveCountryId(String countryName) {
        if (countryName == null) return "0";
        try {
            Earth2026Country country = countryRepository.findByIsoCode(countryName);
            if (country != null) return String.valueOf(country.getId());
        } catch (Exception ignored) {
            // Try by name
        }
        return "0";
    }

    // ---- Industry Determination ----

    /**
     * Determines which industry types to place for a region based on its economy.
     */
    private List<IndustryType> determineIndustriesForRegion(Earth2026Region region) {
        List<IndustryType> industries = new ArrayList<>();

        // Base industries on region GDP
        Double gdpBillions = region.getGdpBillions();
        if (gdpBillions == null || gdpBillions < 10) {
            // Small region: primary industries only
            industries.add(IndustryType.FARM);
            industries.add(IndustryType.LOGGING_CAMP);
        } else if (gdpBillions < 100) {
            // Medium region: primary + secondary
            industries.add(IndustryType.FARM);
            industries.add(IndustryType.BAKERY);
            industries.add(IndustryType.SAWMILL);
        } else {
            // Large region: full production chain
            industries.add(IndustryType.FARM);
            industries.add(IndustryType.BAKERY);
            industries.add(IndustryType.STEEL_MILL);
            industries.add(IndustryType.ELECTRONICS_FACTORY);
        }

        // Add region-specific industries based on name
        String name = region.getName().toLowerCase();
        if (name.contains("california") || name.contains("silicon")) {
            industries.add(IndustryType.ELECTRONICS_FACTORY);
        } else if (name.contains("bavaria") || name.contains("berlin")) {
            industries.add(IndustryType.MACHINE_SHOP);
        } else if (name.contains("tokyo") || name.contains("osaka")) {
            industries.add(IndustryType.ELECTRONICS_FACTORY);
            industries.add(IndustryType.MACHINE_SHOP);
        } else if (name.contains("maharashtra") || name.contains("karnataka")) {
            industries.add(IndustryType.ELECTRONICS_FACTORY);
        } else if (name.contains("saudi") || name.contains("riyadh")) {
            industries.add(IndustryType.OIL_WELL);
            industries.add(IndustryType.REFINERY);
        }

        return industries;
    }

    // ---- Hex Coordinate Mapping ----

    /**
     * Finds the hex cell closest to a country's capital latitude.
     * <p>
     * Uses the country's capital city name to find the corresponding Earth2026Location,
     * then maps the location's latitude/longitude to the closest hex on the planet surface.
     * </p>
     */
    private Hex3D findHexForCapital(Planet planet, Earth2026Country country) {
        List<Hex3D> surface = planet.getGeometry().getSurface();
        if (surface.isEmpty()) {
            return null;
        }

        // Try to find the location for this capital to get lat/long
        Double latitude = null;
        try {
            List<Earth2026Location> capitals = locationRepository.findByType("Capital");
            for (Earth2026Location loc : capitals) {
                if (loc.getCity() != null && loc.getCity().equalsIgnoreCase(country.getCapitalCity())) {
                    latitude = loc.getLatitude();
                    break;
                }
            }
        } catch (Exception ignored) {
            // Location not found, use default positioning
        }

        // If we have lat/long, find the closest hex
        if (latitude != null) {
            return findClosestHexByLatitude(surface, latitude);
        }

        // Fallback: distribute countries evenly around the equator
        int index = Math.abs(country.getIsoCode().hashCode()) % surface.size();
        return surface.get(index);
    }

    /**
     * Finds the hex cell closest to a location's latitude/longitude.
     */
    private Hex3D findHexForLocation(Planet planet, Earth2026Location location) {
        List<Hex3D> surface = planet.getGeometry().getSurface();
        if (surface.isEmpty()) {
            return null;
        }

        if (location.getLatitude() != null) {
            return findClosestHexByLatitude(surface, location.getLatitude());
        }

        // Fallback: distribute by hash
        int index = Math.abs(location.getName().hashCode()) % surface.size();
        return surface.get(index);
    }

    /**
     * Finds the hex cell closest to a given latitude.
     * <p>
     * Latitude is mapped to the Z-coordinate: equator (0Â°) â†’ z=0, north pole (90Â°) â†’ z=radius,
     * south pole (-90Â°) â†’ z=-radius.
     * </p>
     */
    private Hex3D findClosestHexByLatitude(List<Hex3D> surface, Double latitude) {
        int radius = 0;
        for (Hex3D hex : surface) {
            radius = Math.max(radius, Math.abs(hex.getZ()));
            break;
        }
        if (radius == 0) radius = 1;

        // Convert latitude to target Z coordinate
        // Latitude: -90Â° to +90Â° â†’ Z: -radius to +radius
        double targetZ = (latitude / 90.0) * radius;

        Hex3D closest = null;
        double closestDist = Double.MAX_VALUE;

        for (Hex3D hex : surface) {
            double dist = Math.abs(hex.getZ() - targetZ);
            if (dist < closestDist) {
                closestDist = dist;
                closest = hex;
            }
        }

        return closest;
    }

    /**
     * Finds a suitable hex for an industry near a region's capital.
     */
    private Hex3D findSuitableHexForIndustry(Planet planet, Earth2026Region region, IndustryType industryType) {
        List<Hex3D> surface = planet.getGeometry().getSurface();
        if (surface.isEmpty()) {
            return null;
        }

        // Find a hex that matches the industry's terrain requirements
        for (Hex3D hex : surface) {
            TerrainData terrain = surfaceBuilder.getTerrainData(hex);
            if (terrain != null && industryType.canBuildOn(terrain.getTerrainType())) {
                // Check that the hex is not already occupied
                if (!terrain.hasSettlement() && !terrain.hasIndustry()) {
                    return hex;
                }
            }
        }

        // Fallback: return any unoccupied hex
        for (Hex3D hex : surface) {
            TerrainData terrain = surfaceBuilder.getTerrainData(hex);
            if (terrain != null && !terrain.hasSettlement() && !terrain.hasIndustry()) {
                return hex;
            }
        }

        return surface.get(0);
    }

    // ---- Lookup Methods ----

    /**
     * Returns the settlement for a given country ISO code.
     */
    public Settlement getSettlementForCountry(String isoCode) {
        return settlementCache.get(isoCode);
    }

    /**
     * Returns the station for a given location ID.
     */
    public Station getStationForLocation(Long locationId) {
        return stationCache.get(locationId);
    }

    /**
     * Returns all industries for a given region ID.
     */
    public List<Industry> getIndustriesForRegion(Long regionId) {
        return industryCache.getOrDefault(regionId, Collections.emptyList());
    }

    /**
     * Returns all cached settlements.
     */
    public Map<String, Settlement> getAllSettlements() {
        return Collections.unmodifiableMap(settlementCache);
    }

    /**
     * Returns all cached stations.
     */
    public Map<Long, Station> getAllStations() {
        return Collections.unmodifiableMap(stationCache);
    }

    /**
     * Clears all caches (useful for rebuilding).
     */
    public void clearCaches() {
        settlementCache.clear();
        stationCache.clear();
        industryCache.clear();
    }
}
