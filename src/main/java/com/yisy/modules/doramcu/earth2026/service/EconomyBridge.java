package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Country;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Economy;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CountryRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026EconomyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Bridge service that connects Earth2026 static economy data to the Transport Tycoon
 * dynamic cargo production and consumption system.
 * <p>
 * This service:
 * <ul>
 *   <li>Maps Earth2026 country GDP and population to cargo production chains</li>
 *   <li>Sets settlement cargo goals based on population and GDP per capita</li>
 *   <li>Determines which industries to place based on country economy</li>
 *   <li>Processes turn-based economy simulation</li>
 * </ul>
 * <p>
 * The bridge uses {@link TradeRouteManager} for cargo flow and {@link MarketShareTracker}
 * for economic dominance tracking.
 * </p>
 */
@Service
public class EconomyBridge {

    private static final Logger log = LoggerFactory.getLogger(EconomyBridge.class);

    private final Earth2026EconomyRepository economyRepository;
    private final Earth2026CountryRepository countryRepository;
    private final TradeRouteManager tradeRouteManager;
    private final MarketShareTracker marketShareTracker;
    private final PlanetBuilderBridge planetBuilderBridge;

    /** Cache: country ISO â†’ list of recommended IndustryTypes */
    private final Map<String, List<IndustryType>> countryIndustryMap = new HashMap<>();

    /** Cache: country ISO â†’ cargo demand map */
    private final Map<String, Map<CargoType, Integer>> countryCargoDemand = new HashMap<>();

    public EconomyBridge(Earth2026EconomyRepository economyRepository,
                          Earth2026CountryRepository countryRepository,
                          TradeRouteManager tradeRouteManager,
                          MarketShareTracker marketShareTracker,
                          PlanetBuilderBridge planetBuilderBridge) {
        this.economyRepository = economyRepository;
        this.countryRepository = countryRepository;
        this.tradeRouteManager = tradeRouteManager;
        this.marketShareTracker = marketShareTracker;
        this.planetBuilderBridge = planetBuilderBridge;
    }

    // ---- Initialization ----

    /**
     * Initializes the economy from all Earth2026 countries.
     * <p>
     * For each country, this method:
     * <ol>
     *   <li>Determines which industries to place based on GDP and dominant sectors</li>
     *   <li>Sets cargo demand goals for the country's settlement</li>
     *   <li>Creates trade routes between connected countries</li>
     * </ol>
     *
     * @return the number of countries processed
     */
    public int initializeEconomyFromCountries() {
        log.info("Initializing economy from Earth2026 country data");

        List<Earth2026Country> countries = countryRepository.findAll();
        int processed = 0;

        for (Earth2026Country country : countries) {
            try {
                // Map industries for this country
                List<IndustryType> industries = mapCountryToIndustries(country);
                countryIndustryMap.put(country.getIsoCode(), industries);

                // Set cargo demand for this country
                Map<CargoType, Integer> demand = mapCountryToCargoDemand(country);
                countryCargoDemand.put(country.getIsoCode(), demand);

                // Apply cargo goals to settlement if it exists
                Settlement settlement = planetBuilderBridge.getSettlementForCountry(country.getIsoCode());
                if (settlement != null) {
                    applyCargoGoalsToSettlement(settlement, demand);
                }

                processed++;
                log.info("  {} ({}): {} industries, {} cargo demands",
                        country.getName(), country.getIsoCode(), industries.size(), demand.size());
            } catch (Exception e) {
                log.warn("Failed to initialize economy for {}: {}", country.getName(), e.getMessage());
            }
        }

        log.info("Initialized economy for {} countries", processed);
        return processed;
    }

    // ---- Industry Mapping ----

    /**
     * Determines which industry types to place for a country based on its GDP and characteristics.
     * <p>
     * Mapping logic:
     * <ul>
     *   <li>High GDP (>2T) â†’ Electronics, Machine Shop, Luxury Factory</li>
     *   <li>Medium GDP (0.5-2T) â†’ Steel Mill, Bakery, Sawmill</li>
     *   <li>Low GDP (<0.5T) â†’ Farm, Ranch, Logging Camp</li>
     *   <li>Oil-rich countries â†’ Oil Well, Refinery</li>
     *   <li>Forest-rich countries â†’ Logging Camp, Sawmill</li>
     * </ul>
     */
    public List<IndustryType> mapCountryToIndustries(Earth2026Country country) {
        List<IndustryType> industries = new ArrayList<>();

        Double gdp = country.getGdpTrillions();
        if (gdp == null) gdp = 0.0;

        // Base industries on GDP level
        if (gdp > 2.0) {
            // High GDP: full production chain
            industries.add(IndustryType.ELECTRONICS_FACTORY);
            industries.add(IndustryType.MACHINE_SHOP);
            industries.add(IndustryType.LUXURY_FACTORY);
            industries.add(IndustryType.STEEL_MILL);
        } else if (gdp > 0.5) {
            // Medium GDP: secondary industries
            industries.add(IndustryType.STEEL_MILL);
            industries.add(IndustryType.BAKERY);
            industries.add(IndustryType.SAWMILL);
        } else {
            // Low GDP: primary industries
            industries.add(IndustryType.FARM);
            industries.add(IndustryType.RANCH);
        }

        // Add primary industries for all countries
        industries.add(IndustryType.FARM);

        // Country-specific industries
        String name = country.getName().toLowerCase();
        String iso = country.getIsoCode();

        // Oil-rich countries
        if (iso.equals("SA") || iso.equals("RU") || iso.equals("MX") || iso.equals("NG")) {
            industries.add(IndustryType.OIL_WELL);
            industries.add(IndustryType.REFINERY);
        }

        // Mining countries
        if (iso.equals("AU") || iso.equals("BR") || iso.equals("ZA") || iso.equals("RU")) {
            industries.add(IndustryType.MINE);
            industries.add(IndustryType.COAL_MINE);
        }

        // Agricultural countries
        if (iso.equals("US") || iso.equals("BR") || iso.equals("AR") || iso.equals("IN") || iso.equals("FR")) {
            industries.add(IndustryType.FARM);
            industries.add(IndustryType.BAKERY);
            industries.add(IndustryType.RANCH);
        }

        // Forest-rich countries
        if (iso.equals("CA") || iso.equals("RU") || iso.equals("BR") || iso.equals("ID")) {
            industries.add(IndustryType.LOGGING_CAMP);
            industries.add(IndustryType.SAWMILL);
        }

        // Fishing countries
        if (iso.equals("JP") || iso.equals("KR") || iso.equals("NO")) {
            industries.add(IndustryType.FISHING_PORT);
        }

        // Remove duplicates while preserving order
        return industries.stream().distinct().collect(Collectors.toList());
    }

    // ---- Cargo Demand Mapping ----

    /**
     * Sets cargo demand goals for a country based on its population and GDP per capita.
     * <p>
     * Higher population â†’ more food demand. Higher GDP per capita â†’ more luxury demand.
     */
    public Map<CargoType, Integer> mapCountryToCargoDemand(Earth2026Country country) {
        Map<CargoType, Integer> demand = new EnumMap<>(CargoType.class);

        Double population = country.getPopulationMillions();
        Integer gdpPerCapita = country.getGdpPerCapita();

        if (population == null) population = 10.0;
        if (gdpPerCapita == null) gdpPerCapita = 10000;

        // Food demand scales with population
        int foodDemand = (int) (population * 2);
        demand.put(CargoType.BREAD, foodDemand);
        demand.put(CargoType.MEAT, (int) (population * 1));
        demand.put(CargoType.FISH, (int) (population * 0.5));

        // Mail and passenger demand scales with population
        demand.put(CargoType.MAIL, (int) (population * 0.5));
        demand.put(CargoType.PASSENGERS, (int) (population * 0.3));

        // Luxury demand scales with GDP per capita
        if (gdpPerCapita > 30000) {
            demand.put(CargoType.LUXURY, (int) (population * 0.5));
            demand.put(CargoType.ELECTRONICS, (int) (population * 0.3));
        }

        // Industrial demand scales with GDP
        Double gdp = country.getGdpTrillions();
        if (gdp != null && gdp > 1.0) {
            demand.put(CargoType.STEEL, (int) (gdp * 5));
            demand.put(CargoType.MACHINERY, (int) (gdp * 2));
        }

        return demand;
    }

    /**
     * Applies cargo goals to a settlement.
     */
    private void applyCargoGoalsToSettlement(Settlement settlement, Map<CargoType, Integer> demand) {
        for (Map.Entry<CargoType, Integer> entry : demand.entrySet()) {
            settlement.setCargoGoal(entry.getKey(), entry.getValue());
        }
    }

    // ---- Turn Processing ----

    /**
     * Processes one economy turn: cargo production, delivery, and market share tracking.
     *
     * @return total cargo value delivered this turn
     */
    public long processEconomyTurn() {
        log.info("Processing economy turn");

        // Process trade routes for cargo delivery
        long totalCargoValue = tradeRouteManager.processTurn();

        log.info("Economy turn complete: {} cargo value delivered, {} active routes",
                totalCargoValue, tradeRouteManager.getActiveRouteCount());
        return totalCargoValue;
    }

    // ---- Economy Summary ----

    /**
     * Returns a cargo flow summary for a country.
     *
     * @param isoCode the country ISO code
     * @return map with production, consumption, and trade balance info
     */
    public Map<String, Object> getCountryEconomySummary(String isoCode) {
        Map<String, Object> summary = new HashMap<>();

        Earth2026Country country = countryRepository.findByIsoCode(isoCode);
        if (country == null) {
            summary.put("error", "Country not found: " + isoCode);
            return summary;
        }

        summary.put("countryName", country.getName());
        summary.put("isoCode", country.getIsoCode());
        summary.put("gdpTrillions", country.getGdpTrillions());
        summary.put("populationMillions", country.getPopulationMillions());
        summary.put("gdpPerCapita", country.getGdpPerCapita());

        // Industries
        List<IndustryType> industries = countryIndustryMap.getOrDefault(isoCode, new ArrayList<>());
        summary.put("industries", industries.stream()
                .map(IndustryType::getDisplayName)
                .collect(Collectors.toList()));

        // Cargo demand
        Map<CargoType, Integer> demand = countryCargoDemand.getOrDefault(isoCode, new EnumMap<>(CargoType.class));
        Map<String, Integer> demandMap = new HashMap<>();
        demand.forEach((cargoType, amount) -> demandMap.put(cargoType.getDisplayName(), amount));
        summary.put("cargoDemand", demandMap);

        // Settlement info
        Settlement settlement = planetBuilderBridge.getSettlementForCountry(isoCode);
        if (settlement != null) {
            summary.put("settlementName", settlement.getName());
            summary.put("settlementId", settlement.getId());
            summary.put("population", settlement.getPopulation());
            summary.put("active", settlement.isActive());
        }

        // Trade routes for this country
        List<CampaignTradeRoute> routes = tradeRouteManager.getRoutesByCorporation("country_" + country.getId());
        summary.put("routeCount", routes.size());
        summary.put("activeRouteCount", (long) routes.stream().filter(CampaignTradeRoute::isActive).count());

        return summary;
    }

    /**
     * Returns a summary of the entire planet economy.
     */
    public Map<String, Object> getPlanetEconomySummary() {
        Map<String, Object> summary = new HashMap<>();

        // Economy stats
        Earth2026Economy economy = economyRepository.findByPlanetId(1L).orElse(null);
        if (economy != null) {
            summary.put("gdpTrillions", economy.getGdpTrillions());
            summary.put("growthRate", economy.getGrowthRate());
            summary.put("inflationRate", economy.getInflationRate());
            summary.put("marketCondition", economy.getMarketCondition());
            summary.put("tradeOpenness", economy.getTradeOpenness());
        }

        // Trade route stats
        summary.put("totalRoutes", (long) tradeRouteManager.getRouteCount());
        summary.put("activeRoutes", (long) tradeRouteManager.getActiveRouteCount());

        // Country count
        summary.put("countriesProcessed", (long) countryIndustryMap.size());

        return summary;
    }

    // ---- Lookup Methods ----

    /**
     * Returns the recommended industries for a country.
     */
    public List<IndustryType> getIndustriesForCountry(String isoCode) {
        return countryIndustryMap.getOrDefault(isoCode, Collections.emptyList());
    }

    /**
     * Returns the cargo demand for a country.
     */
    public Map<CargoType, Integer> getCargoDemandForCountry(String isoCode) {
        return countryCargoDemand.getOrDefault(isoCode, Collections.emptyMap());
    }

    /**
     * Returns the TradeRouteManager instance.
     */
    public TradeRouteManager getTradeRouteManager() {
        return tradeRouteManager;
    }

    /**
     * Returns the MarketShareTracker instance.
     */
    public MarketShareTracker getMarketShareTracker() {
        return marketShareTracker;
    }
}
