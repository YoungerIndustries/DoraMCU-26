package com.yisy.modules.doramcu.thirdreich.service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Country;
import com.yisy.modules.doramcu.earth2026.service.PlanetService;
import com.yisy.modules.doramcu.hex.planet.military.MilitaryProductionService;
import com.yisy.modules.doramcu.thirdreich.commander.AICommander;
import com.yisy.modules.doramcu.thirdreich.commander.AICommanderService;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichGroundUnit;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichUnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service that maps Earth2026 countries to 3rd Reich military campaigns.
 * <p>
 * Each country becomes a faction with its own army, navy, and space navy.
 * The country's military strength determines the initial unit count,
 * the AI leader becomes the commander, and the economy determines
 * the military production budget.
 * </p>
 */
@Service
public class CountryCampaignService {

    private static final Logger log = LoggerFactory.getLogger(CountryCampaignService.class);

    private final PlanetService planetService;
    private final AICommanderService aiCommanderService;
    private final MilitaryProductionService militaryProductionService;

    /** Maps country ID to its campaign state */
    private final Map<Long, CountryCampaign> campaigns = new ConcurrentHashMap<>();

    private final AtomicInteger campaignIdCounter = new AtomicInteger(1);

    public CountryCampaignService(
            PlanetService planetService,
            AICommanderService aiCommanderService,
            MilitaryProductionService militaryProductionService) {
        this.planetService = planetService;
        this.aiCommanderService = aiCommanderService;
        this.militaryProductionService = militaryProductionService;
    }

    /**
     * Initialize a campaign for a country.
     * <p>
     * This creates the faction, generates initial units based on military strength,
     * assigns an AI commander from the country's leader, and sets the production
     * budget based on the country's economy.
     * </p>
     *
     * @param country the country to initialize
     * @return the created campaign
     */
    public CountryCampaign initializeCampaign(Earth2026Country country) {
        // Check if campaign already exists
        if (campaigns.containsKey(country.getId())) {
            log.debug("Campaign already exists for country {}", country.getName());
            return campaigns.get(country.getId());
        }

        CountryCampaign campaign = new CountryCampaign(country);

        // Create AI commander from country leader
        AICommander commander = createCommanderFromCountry(country);
        campaign.setCommander(commander);

        // Generate initial military units based on military strength
        List<ThirdReichGroundUnit> units = generateInitialUnits(country);
        campaign.setArmyUnits(units);

        // Set production budget based on economy
        campaign.setProductionBudget(calculateProductionBudget(country));

        // Determine available unit types based on tech level
        Set<ThirdReichUnitType> availableTypes = getAvailableUnitTypes(country.getTechLevel());
        campaign.setAvailableUnitTypes(availableTypes);

        campaigns.put(country.getId(), campaign);
        log.info("Initialized campaign for {}: {} units, commander: {}",
                country.getName(), units.size(), commander != null ? commander.getName() : "None");

        return campaign;
    }

    /**
     * Get the campaign for a country.
     *
     * @param countryId the country ID
     * @return the campaign, or null if not initialized
     */
    public CountryCampaign getCampaign(Long countryId) {
        return campaigns.get(countryId);
    }

    /**
     * Get all campaigns for a planet.
     *
     * @param planetId the planet ID
     * @return list of campaigns
     */
    public List<CountryCampaign> getCampaignsForPlanet(Long planetId) {
        List<Earth2026Country> countries = planetService.getCountries(planetId);
        List<CountryCampaign> result = new ArrayList<>();
        for (Earth2026Country country : countries) {
            CountryCampaign campaign = campaigns.get(country.getId());
            if (campaign != null) {
                result.add(campaign);
            }
        }
        return result;
    }

    /**
     * Get all campaigns across all planets.
     *
     * @return list of all campaigns
     */
    public List<CountryCampaign> getAllCampaigns() {
        return new ArrayList<>(campaigns.values());
    }

    /**
     * Get units for a faction by ISO code.
     * Used by the battle resolver to find country campaign units.
     *
     * @param isoCode the country ISO code (e.g., "US", "RU")
     * @return list of combat-effective units, or empty list if not found
     */
    public List<ThirdReichGroundUnit> getUnitsByIsoCode(String isoCode) {
        for (CountryCampaign campaign : campaigns.values()) {
            if (campaign.getIsoCode().equals(isoCode)) {
                return new ArrayList<>(campaign.getCombatEffectiveUnits());
            }
        }
        return Collections.emptyList();
    }

    /**
     * Initialize campaigns for all countries on a planet.
     *
     * @param planetId the planet ID
     * @return list of initialized campaigns
     */
    public List<CountryCampaign> initializeAllCampaigns(Long planetId) {
        List<Earth2026Country> countries = planetService.getCountries(planetId);
        List<CountryCampaign> result = new ArrayList<>();
        for (Earth2026Country country : countries) {
            result.add(initializeCampaign(country));
        }
        log.info("Initialized {} campaigns for planet {}", result.size(), planetId);
        return result;
    }

    /**
     * Create an AI commander from a country's leader.
     */
    private AICommander createCommanderFromCountry(Earth2026Country country) {
        String commanderId = "CMD-" + country.getIsoCode() + "-" + campaignIdCounter.getAndIncrement();
        String name = country.getLeaderName() != null ? country.getLeaderName() : country.getName() + " Commander";

        AICommander commander = new AICommander(commanderId, name);
        commander.setLeadership(country.getMilitaryStrength());
        commander.setTacticalPreference(
                country.getMilitaryStrength() >= 8 ? AICommander.TacticalPreference.AGGRESSIVE :
                country.getMilitaryStrength() >= 5 ? AICommander.TacticalPreference.BALANCED :
                AICommander.TacticalPreference.DEFENSIVE
        );
        commander.setSourceGame("Earth2026");
        return commander;
    }

    /**
     * Generate initial military units based on country's military strength.
     */
    private List<ThirdReichGroundUnit> generateInitialUnits(Earth2026Country country) {
        List<ThirdReichGroundUnit> units = new ArrayList<>();
        int militaryStrength = country.getMilitaryStrength();
        int techLevel = country.getTechLevel() != null ? country.getTechLevel().getLevel() : 5;
        String isoCode = country.getIsoCode();

        // Infantry - every country has infantry
        int infantryCount = Math.max(1, militaryStrength * 2);
        for (int i = 0; i < infantryCount; i++) {
            units.add(createUnit(isoCode, "Infantry Battalion " + (i + 1),
                    ThirdReichUnitType.INFANTRY, militaryStrength));
        }

        // Mechanized - TL5+
        if (techLevel >= 5) {
            int mechCount = Math.max(0, militaryStrength - 3);
            for (int i = 0; i < mechCount; i++) {
                units.add(createUnit(isoCode, "Mechanized Regiment " + (i + 1),
                        ThirdReichUnitType.MECHANIZED, militaryStrength));
            }
        }

        // Tanks - TL6+
        if (techLevel >= 6) {
            int tankCount = Math.max(0, militaryStrength - 4);
            for (int i = 0; i < tankCount; i++) {
                units.add(createUnit(isoCode, "Tank Division " + (i + 1),
                        ThirdReichUnitType.TANK, militaryStrength));
            }
        }

        // Artillery - TL5+
        if (techLevel >= 5) {
            int artyCount = Math.max(0, militaryStrength - 2);
            for (int i = 0; i < artyCount; i++) {
                units.add(createUnit(isoCode, "Artillery Battery " + (i + 1),
                        ThirdReichUnitType.ARTILLERY, militaryStrength));
            }
        }

        // Recon - TL6+
        if (techLevel >= 6) {
            units.add(createUnit(isoCode, "Recon Squadron",
                    ThirdReichUnitType.RECON, militaryStrength));
        }

        // Engineers - TL5+
        if (techLevel >= 5) {
            units.add(createUnit(isoCode, "Engineer Corps",
                    ThirdReichUnitType.ENGINEER, militaryStrength));
        }

        // Anti-Air - TL6+
        if (techLevel >= 6) {
            int aaCount = Math.max(0, militaryStrength - 5);
            for (int i = 0; i < aaCount; i++) {
                units.add(createUnit(isoCode, "Anti-Air Battery " + (i + 1),
                        ThirdReichUnitType.ANTI_AIR, militaryStrength));
            }
        }

        // Air Support - TL7+
        if (techLevel >= 7) {
            int airCount = Math.max(0, militaryStrength - 6);
            for (int i = 0; i < airCount; i++) {
                units.add(createUnit(isoCode, "Air Support Wing " + (i + 1),
                        ThirdReichUnitType.AIR_SUPPORT, militaryStrength));
            }
        }

        // BattleMechs - TL10+
        if (techLevel >= 10) {
            int mechWarriorCount = Math.max(0, militaryStrength - 8);
            for (int i = 0; i < mechWarriorCount; i++) {
                units.add(createUnit(isoCode, "BattleMech Company " + (i + 1),
                        ThirdReichUnitType.MECH, militaryStrength));
            }
        }

        log.info("Generated {} units for {} (military strength: {})",
                units.size(), country.getName(), militaryStrength);
        return units;
    }

    /**
     * Create a single military unit.
     */
    private ThirdReichGroundUnit createUnit(String isoCode, String name,
                                            ThirdReichUnitType type, int militaryStrength) {
        String id = isoCode + "-" + type.name() + "-" + UUID.randomUUID().toString().substring(0, 8);
        ThirdReichGroundUnit unit = new ThirdReichGroundUnit(id, name, type);

        // Adjust stats based on military strength
        int strength = Math.max(10, 50 + (militaryStrength - 5) * 10);
        unit.setStrength(strength);
        unit.setMaxStrength(strength);
        unit.setMorale(Math.max(50, 70 + militaryStrength * 2));
        unit.setAttackValue(type.getBaseAttack() + militaryStrength / 2);
        unit.setDefenseValue(type.getBaseDefense() + militaryStrength / 2);
        unit.setSourceGame("Earth2026");

        return unit;
    }

    /**
     * Calculate production budget based on country's economy.
     */
    private int calculateProductionBudget(Earth2026Country country) {
        // Budget scales with economic power and GDP
        int baseBudget = country.getEconomicPower() * 100;
        double gdpFactor = country.getGdpTrillions() * 10;
        return (int) (baseBudget + gdpFactor);
    }

    /**
     * Get available unit types based on tech level.
     */
    private Set<ThirdReichUnitType> getAvailableUnitTypes(com.yisy.modules.doramcu.earth2026.model.TechLevel techLevel) {
        Set<ThirdReichUnitType> types = new HashSet<>();
        int level = techLevel != null ? techLevel.getLevel() : 0;

        // Always available
        types.add(ThirdReichUnitType.INFANTRY);

        if (level >= 5) {
            types.add(ThirdReichUnitType.MECHANIZED);
            types.add(ThirdReichUnitType.ARTILLERY);
            types.add(ThirdReichUnitType.ENGINEER);
        }
        if (level >= 6) {
            types.add(ThirdReichUnitType.TANK);
            types.add(ThirdReichUnitType.RECON);
            types.add(ThirdReichUnitType.ANTI_AIR);
        }
        if (level >= 7) {
            types.add(ThirdReichUnitType.AIR_SUPPORT);
        }
        if (level >= 10) {
            types.add(ThirdReichUnitType.MECH);
        }

        return types;
    }

    /**
     * Campaign state for a country.
     */
    public static class CountryCampaign {
        private final Long countryId;
        private final String countryName;
        private final String isoCode;
        private final int militaryStrength;
        private final int economicPower;
        private final int techLevel;

        private AICommander commander;
        private List<ThirdReichGroundUnit> armyUnits;
        private int productionBudget;
        private Set<ThirdReichUnitType> availableUnitTypes;
        private int currentTurn;
        private int totalCasualties;
        private int battlesWon;
        private int battlesLost;

        public CountryCampaign(Earth2026Country country) {
            this.countryId = country.getId();
            this.countryName = country.getName();
            this.isoCode = country.getIsoCode();
            this.militaryStrength = country.getMilitaryStrength();
            this.economicPower = country.getEconomicPower();
            this.techLevel = country.getTechLevel() != null ? country.getTechLevel().getLevel() : 5;
            this.armyUnits = new ArrayList<>();
            this.availableUnitTypes = new HashSet<>();
        }

        public Long getCountryId() { return countryId; }
        public String getCountryName() { return countryName; }
        public String getIsoCode() { return isoCode; }
        public int getMilitaryStrength() { return militaryStrength; }
        public int getEconomicPower() { return economicPower; }
        public int getTechLevel() { return techLevel; }
        public AICommander getCommander() { return commander; }
        public void setCommander(AICommander commander) { this.commander = commander; }
        public List<ThirdReichGroundUnit> getArmyUnits() { return armyUnits; }
        public void setArmyUnits(List<ThirdReichGroundUnit> armyUnits) { this.armyUnits = armyUnits; }
        public int getProductionBudget() { return productionBudget; }
        public void setProductionBudget(int productionBudget) { this.productionBudget = productionBudget; }
        public Set<ThirdReichUnitType> getAvailableUnitTypes() { return availableUnitTypes; }
        public void setAvailableUnitTypes(Set<ThirdReichUnitType> availableUnitTypes) {
            this.availableUnitTypes = availableUnitTypes;
        }
        public int getCurrentTurn() { return currentTurn; }
        public void advanceTurn() { this.currentTurn++; }
        public int getTotalCasualties() { return totalCasualties; }
        public void addCasualties(int count) { this.totalCasualties += count; }
        public int getBattlesWon() { return battlesWon; }
        public void recordVictory() { this.battlesWon++; }
        public int getBattlesLost() { return battlesLost; }
        public void recordDefeat() { this.battlesLost++; }

        /**
         * Get combat-effective units (strength > 0 and morale > 0).
         */
        public List<ThirdReichGroundUnit> getCombatEffectiveUnits() {
            return armyUnits.stream()
                    .filter(ThirdReichGroundUnit::isCombatEffective)
                    .toList();
        }

        /**
         * Total combat power of all units.
         */
        public int getTotalCombatPower() {
            return armyUnits.stream()
                    .filter(ThirdReichGroundUnit::isCombatEffective)
                    .mapToInt(u -> u.getAttackValue() + u.getDefenseValue())
                    .sum();
        }

        @Override
        public String toString() {
            return String.format("Campaign{%s, units=%d, power=%d, won=%d, lost=%d}",
                    countryName, armyUnits.size(), getTotalCombatPower(), battlesWon, battlesLost);
        }
    }
}
