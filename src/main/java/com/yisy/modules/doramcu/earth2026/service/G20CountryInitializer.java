package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Country;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CountryRepository;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service that initializes the G20 countries with pre-configured real-world stats.
 * <p>
 * The G20 consists of 19 countries plus the European Union. Each country is
 * configured with approximate real-world data for population, GDP, military
 * strength, economic power, diplomatic influence, and tech level.
 * </p>
 */
@Service
public class G20CountryInitializer {

    private static final Logger log = LoggerFactory.getLogger(G20CountryInitializer.class);

    private final Earth2026CountryRepository countryRepository;

    public G20CountryInitializer(Earth2026CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    /**
     * Get the list of all G20 country configurations.
     *
     * @return list of G20 country configurations
     */
    public List<G20CountryConfig> getG20Countries() {
        return List.of(
                new G20CountryConfig("Argentina", "AR", "ARG", "Buenos Aires",
                        "Presidential Republic", "Liberal Democracy",
                        45.8, 0.634, 13800, TechLevel.TL6,
                        5, 5, 4, "USD - Argentine Peso",
                        "Spanish", "South America"),

                new G20CountryConfig("Australia", "AU", "AUS", "Canberra",
                        "Constitutional Monarchy", "Liberal Democracy",
                        26.0, 1.671, 64000, TechLevel.TL6,
                        6, 7, 5, "AUD - Australian Dollar",
                        "English", "Oceania"),

                new G20CountryConfig("Brazil", "BR", "BRA", "Brasília",
                        "Presidential Republic", "Liberal Democracy",
                        214.3, 1.920, 8900, TechLevel.TL6,
                        6, 6, 6, "BRL - Brazilian Real",
                        "Portuguese", "South America"),

                new G20CountryConfig("Canada", "CA", "CAN", "Ottawa",
                        "Constitutional Monarchy", "Liberal Democracy",
                        38.9, 2.139, 55000, TechLevel.TL7,
                        6, 7, 6, "CAD - Canadian Dollar",
                        "English, French", "North America"),

                new G20CountryConfig("China", "CN", "CHN", "Beijing",
                        "People's Republic", "Authoritarian",
                        1412.0, 17.963, 12500, TechLevel.TL7,
                        10, 9, 8, "CNY - Chinese Yuan",
                        "Mandarin Chinese", "Asia"),

                new G20CountryConfig("France", "FR", "FRA", "Paris",
                        "Semi-Presidential Republic", "Liberal Democracy",
                        67.8, 2.923, 43000, TechLevel.TL7,
                        8, 8, 8, "EUR - Euro",
                        "French", "Europe"),

                new G20CountryConfig("Germany", "DE", "DEU", "Berlin",
                        "Parliamentary Republic", "Social Democracy",
                        83.2, 4.259, 51000, TechLevel.TL7,
                        7, 9, 7, "EUR - Euro",
                        "German", "Europe"),

                new G20CountryConfig("India", "IN", "IND", "New Delhi",
                        "Parliamentary Republic", "Liberal Democracy",
                        1408.0, 3.737, 2600, TechLevel.TL6,
                        7, 6, 6, "INR - Indian Rupee",
                        "Hindi, English", "Asia"),

                new G20CountryConfig("Indonesia", "ID", "IDN", "Jakarta",
                        "Presidential Republic", "Liberal Democracy",
                        275.5, 1.319, 4800, TechLevel.TL6,
                        5, 5, 5, "IDR - Indonesian Rupiah",
                        "Indonesian", "Asia"),

                new G20CountryConfig("Italy", "IT", "ITA", "Rome",
                        "Parliamentary Republic", "Liberal Democracy",
                        59.1, 2.010, 35000, TechLevel.TL7,
                        6, 7, 6, "EUR - Euro",
                        "Italian", "Europe"),

                new G20CountryConfig("Japan", "JP", "JPN", "Tokyo",
                        "Constitutional Monarchy", "Liberal Democracy",
                        125.7, 4.231, 34000, TechLevel.TL7,
                        7, 8, 7, "JPY - Japanese Yen",
                        "Japanese", "Asia"),

                new G20CountryConfig("Mexico", "MX", "MEX", "Mexico City",
                        "Presidential Republic", "Liberal Democracy",
                        128.9, 1.322, 10000, TechLevel.TL6,
                        5, 5, 5, "MXN - Mexican Peso",
                        "Spanish", "North America"),

                new G20CountryConfig("Russia", "RU", "RUS", "Moscow",
                        "Semi-Presidential Republic", "Authoritarian",
                        144.1, 2.240, 15000, TechLevel.TL7,
                        9, 6, 6, "RUB - Russian Ruble",
                        "Russian", "Europe/Asia"),

                new G20CountryConfig("Saudi Arabia", "SA", "SAU", "Riyadh",
                        "Absolute Monarchy", "Authoritarian",
                        35.9, 1.069, 29000, TechLevel.TL6,
                        6, 7, 6, "SAR - Saudi Riyal",
                        "Arabic", "Asia"),

                new G20CountryConfig("South Africa", "ZA", "ZAF", "Pretoria",
                        "Parliamentary Republic", "Liberal Democracy",
                        60.6, 0.405, 6700, TechLevel.TL6,
                        4, 4, 4, "ZAR - South African Rand",
                        "11 Official Languages", "Africa"),

                new G20CountryConfig("South Korea", "KR", "KOR", "Seoul",
                        "Presidential Republic", "Liberal Democracy",
                        51.7, 1.713, 33000, TechLevel.TL7,
                        6, 7, 6, "KRW - South Korean Won",
                        "Korean", "Asia"),

                new G20CountryConfig("Turkey", "TR", "TUR", "Ankara",
                        "Presidential Republic", "Authoritarian",
                        85.3, 0.906, 10600, TechLevel.TL6,
                        6, 5, 5, "TRY - Turkish Lira",
                        "Turkish", "Europe/Asia"),

                new G20CountryConfig("United Kingdom", "GB", "GBR", "London",
                        "Constitutional Monarchy", "Liberal Democracy",
                        67.7, 3.070, 46000, TechLevel.TL7,
                        7, 8, 7, "GBP - British Pound",
                        "English", "Europe"),

                new G20CountryConfig("United States", "US", "USA", "Washington, D.C.",
                        "Presidential Republic", "Liberal Democracy",
                        331.9, 25.463, 76000, TechLevel.TL7,
                        10, 10, 9, "USD - US Dollar",
                        "English", "North America"),

                new G20CountryConfig("European Union", "EU", "EUU", "Brussels",
                        "Supranational Union", "Liberal Democracy",
                        447.0, 16.687, 37000, TechLevel.TL7,
                        8, 10, 9, "EUR - Euro",
                        "24 Official Languages", "Europe")
        );
    }

    /**
     * Initialize G20 countries on a specific planet.
     * <p>
     * This creates all 20 G20 countries with pre-configured stats and links them
     * to the specified planet. Existing countries with the same ISO code are skipped.
     * </p>
     *
     * @param planetId the planet ID to associate with these countries
     * @return list of created countries
     */
    @Transactional
    public List<Earth2026Country> initializeG20OnPlanet(Long planetId) {
        List<Earth2026Country> created = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (G20CountryConfig config : getG20Countries()) {
            // Check if country already exists
            Earth2026Country existing = countryRepository.findByIsoCode(config.isoCode);
            if (existing != null) {
                log.debug("Country {} already exists, skipping", config.isoCode);
                continue;
            }

            Earth2026Country country = new Earth2026Country();
            country.setName(config.name);
            country.setIsoCode(config.isoCode);
            country.setIsoCode3(config.isoCode3);
            country.setCapitalCity(config.capitalCity);
            country.setGovernmentType(config.governmentType);
            country.setPoliticalIdeology(config.politicalIdeology);
            country.setPopulationMillions(config.populationMillions);
            country.setGdpTrillions(config.gdpTrillions);
            country.setGdpPerCapita(config.gdpPerCapita);
            country.setTechLevel(config.techLevel);
            country.setMilitaryStrength(config.militaryStrength);
            country.setEconomicPower(config.economicPower);
            country.setDiplomaticInfluence(config.diplomaticInfluence);
            country.setActive(true);
            country.setPlanetId(planetId);
            country.setDescription(config.name + " - G20 member nation");
            country.setContinent(config.continent);
            country.setOfficialLanguages(config.officialLanguages);
            country.setCurrency(config.currency);
            country.setCreatedAt(now);
            country.setUpdatedAt(now);

            country = countryRepository.save(country);
            created.add(country);
            log.info("Created G20 country: {} ({}) on planet {}", config.name, config.isoCode, planetId);
        }

        log.info("Initialized {} G20 countries on planet {}", created.size(), planetId);
        return created;
    }

    /**
     * Configuration for a single G20 country.
     */
    public static class G20CountryConfig {
        public final String name;
        public final String isoCode;
        public final String isoCode3;
        public final String capitalCity;
        public final String governmentType;
        public final String politicalIdeology;
        public final Double populationMillions;
        public final Double gdpTrillions;
        public final Integer gdpPerCapita;
        public final TechLevel techLevel;
        public final Integer militaryStrength;
        public final Integer economicPower;
        public final Integer diplomaticInfluence;
        public final String currency;
        public final String officialLanguages;
        public final String continent;

        public G20CountryConfig(String name, String isoCode, String isoCode3, String capitalCity,
                                String governmentType, String politicalIdeology,
                                Double populationMillions, Double gdpTrillions, Integer gdpPerCapita,
                                TechLevel techLevel,
                                Integer militaryStrength, Integer economicPower, Integer diplomaticInfluence,
                                String currency, String officialLanguages, String continent) {
            this.name = name;
            this.isoCode = isoCode;
            this.isoCode3 = isoCode3;
            this.capitalCity = capitalCity;
            this.governmentType = governmentType;
            this.politicalIdeology = politicalIdeology;
            this.populationMillions = populationMillions;
            this.gdpTrillions = gdpTrillions;
            this.gdpPerCapita = gdpPerCapita;
            this.techLevel = techLevel;
            this.militaryStrength = militaryStrength;
            this.economicPower = economicPower;
            this.diplomaticInfluence = diplomaticInfluence;
            this.currency = currency;
            this.officialLanguages = officialLanguages;
            this.continent = continent;
        }
    }
}
