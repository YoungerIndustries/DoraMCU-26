package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Country;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CountryRepository;
import com.yisy.modules.doramcu.hex.planet.fps.EspionageEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Espionage system for G20 countries.
 * <p>
 * Features:
 * <ul>
 *   <li>Spy deployment to target countries</li>
 *   <li>Sabotage of industries and military bases</li>
 *   <li>Intelligence gathering on enemy capabilities</li>
 *   <li>Counter-espionage to detect and capture spies</li>
 *   <li>Economic sabotage affecting trade routes</li>
 * </ul>
 * Spies are funded by the country's economic power and can be detected by the target's
 * diplomatic influence (counter-intelligence capability).
 * </p>
 */
@Service
public class EspionageService {

    private static final Logger log = LoggerFactory.getLogger(EspionageService.class);

    private final Earth2026CountryRepository countryRepository;
    private final CountryDiplomacyService diplomacyService;
    private final EspionageEventService eventService;

    // In-memory espionage state
    private final Map<String, List<Spy>> deployedSpies = new ConcurrentHashMap<>();
    private final Map<String, List<IntelReport>> intelligenceReports = new ConcurrentHashMap<>();
    private final AtomicLong spyIdCounter = new AtomicLong(1);

    public EspionageService(Earth2026CountryRepository countryRepository,
                           CountryDiplomacyService diplomacyService,
                           EspionageEventService eventService) {
        this.countryRepository = countryRepository;
        this.diplomacyService = diplomacyService;
        this.eventService = eventService;
    }

    /**
     * Deploy a spy to a target country.
     * <p>
     * Success depends on:
     * - Deployer's economic power (funding)
     * - Target's diplomatic influence (counter-intelligence)
     * - Diplomatic relations (at war = harder to infiltrate)
     * </p>
     *
     * @param deployerIso the ISO code of the country deploying the spy
     * @param targetIso the ISO code of the target country
     * @param spyType the type of spy operation
     * @return the deployment result
     */
    public SpyDeploymentResult deploySpy(String deployerIso, String targetIso, SpyType spyType) {
        Earth2026Country deployer = countryRepository.findByIsoCode(deployerIso);
        if (deployer == null) throw new IllegalArgumentException("Country not found: " + deployerIso);
        Earth2026Country target = countryRepository.findByIsoCode(targetIso);
        if (target == null) throw new IllegalArgumentException("Target country not found: " + targetIso);

        // Calculate success probability
        double successChance = calculateSuccessChance(deployer, target, spyType);

        Random random = new Random();
        boolean success = random.nextDouble() < successChance;

        SpyDeploymentResult result = new SpyDeploymentResult();
        result.setDeployerIso(deployerIso);
        result.setTargetIso(targetIso);
        result.setSpyType(spyType);
        result.setSuccess(success);
        result.setSuccessChance(successChance);
        result.setTimestamp(System.currentTimeMillis());

        if (success) {
            Spy spy = new Spy();
            spy.setId("SPY-" + spyIdCounter.getAndIncrement());
            spy.setDeployerIso(deployerIso);
            spy.setTargetIso(targetIso);
            spy.setSpyType(spyType);
            spy.setDeployedAt(System.currentTimeMillis());
            spy.setActive(true);
            spy.setDetected(false);

            deployedSpies.computeIfAbsent(targetIso, k -> new ArrayList<>()).add(spy);

            result.setSpyId(spy.getId());
            result.setMessage("Spy " + spy.getId() + " deployed successfully to " + target.getName());
            log.info("Spy deployed: {} -> {} (type: {})", deployerIso, targetIso, spyType);
            eventService.broadcastSpyDeployment(deployerIso, targetIso, spyType.name(), true, spy.getId());
        } else {
            result.setMessage("Spy deployment failed - detected by " + target.getName() + " counter-intelligence");
            log.info("Spy deployment failed: {} -> {} (type: {})", deployerIso, targetIso, spyType);
            eventService.broadcastSpyDeployment(deployerIso, targetIso, spyType.name(), false, null);
        }

        return result;
    }

    /**
     * Execute sabotage operation on a target country.
     * <p>
     * Requires an active spy in the target country. Effects:
     * - Industry sabotage: reduces production by 10-30%
     * - Military sabotage: reduces unit strength by 5-15%
     * - Economic sabotage: reduces trade income by 10-20%
     * </p>
     *
     * @param targetIso the target country ISO code
     * @param sabotageType the type of sabotage
     * @return the sabotage result
     */
    public SabotageResult executeSabotage(String targetIso, SabotageType sabotageType) {
        List<Spy> spies = deployedSpies.get(targetIso);
        if (spies == null || spies.isEmpty()) {
            return new SabotageResult(false, "No active spies in " + targetIso, targetIso);
        }

        // Find an active, undetected spy
        Optional<Spy> activeSpy = spies.stream()
                .filter(s -> s.isActive() && !s.isDetected())
                .findFirst();

        if (!activeSpy.isPresent()) {
            return new SabotageResult(false, "No active undetected spies in " + targetIso, targetIso);
        }

        Spy spy = activeSpy.get();
        Earth2026Country target = countryRepository.findByIsoCode(targetIso);
        if (target == null) throw new IllegalArgumentException("Target country not found: " + targetIso);

        // Calculate sabotage effectiveness
        double effectiveness = calculateSabotageEffectiveness(spy, target, sabotageType);

        // Check for detection
        double detectionChance = 0.3 + (target.getDiplomaticInfluence() != null ? target.getDiplomaticInfluence() / 200.0 : 0.15);
        Random random = new Random();
        boolean detected = random.nextDouble() < detectionChance;

        if (detected) {
            spy.setDetected(true);
            spy.setActive(false);
            log.info("Spy {} detected during sabotage in {}", spy.getId(), targetIso);
        }

        SabotageResult result = new SabotageResult(true, "Sabotage executed", targetIso);
        result.setSpyId(spy.getId());
        result.setSabotageType(sabotageType);
        result.setEffectiveness(effectiveness);
        result.setDetected(detected);
        result.setTimestamp(System.currentTimeMillis());

        // Apply sabotage effects
        switch (sabotageType) {
            case INDUSTRY:
                result.setProductionLoss(effectiveness * 20 + 10); // 10-30% production loss
                result.setMessage("Industry sabotage: " + String.format("%.1f", result.getProductionLoss()) + "% production loss");
                break;
            case MILITARY:
                result.setMilitaryLoss(effectiveness * 10 + 5); // 5-15% military loss
                result.setMessage("Military sabotage: " + String.format("%.1f", result.getMilitaryLoss()) + "% unit strength reduction");
                break;
            case ECONOMIC:
                result.setEconomicLoss(effectiveness * 10 + 10); // 10-20% economic loss
                result.setMessage("Economic sabotage: " + String.format("%.1f", result.getEconomicLoss()) + "% trade income reduction");
                break;
        }

        log.info("Sabotage executed: {} on {} (type: {}, effectiveness: {})",
                spy.getId(), targetIso, sabotageType, effectiveness);

        eventService.broadcastSabotage(spy.getId(), targetIso, sabotageType.name(), effectiveness, detected);

        return result;
    }

    /**
     * Gather intelligence on a target country.
     * <p>
     * Reveals:
     * - Military strength and unit composition
     * - Economic output and trade routes
     * - Diplomatic relations and treaties
     * - Infrastructure and industrial capacity
     * </p>
     *
     * @param targetIso the target country ISO code
     * @return the intelligence report
     */
    public IntelReport gatherIntelligence(String targetIso) {
        List<Spy> spies = deployedSpies.get(targetIso);
        if (spies == null || spies.isEmpty()) {
            return new IntelReport(targetIso, "No active spies in " + targetIso);
        }

        Optional<Spy> activeSpy = spies.stream()
                .filter(s -> s.isActive() && !s.isDetected())
                .findFirst();

        if (!activeSpy.isPresent()) {
            return new IntelReport(targetIso, "No active undetected spies in " + targetIso);
        }

        Earth2026Country target = countryRepository.findByIsoCode(targetIso);
        if (target == null) throw new IllegalArgumentException("Target country not found: " + targetIso);

        IntelReport report = new IntelReport();
        report.setTargetIso(targetIso);
        report.setTargetName(target.getName());
        report.setSpyId(activeSpy.get().getId());
        report.setTimestamp(System.currentTimeMillis());
        report.setSuccess(true);

        // Gather intelligence
        report.setMilitaryStrength(target.getMilitaryStrength());
        report.setEconomicPower(target.getEconomicPower());
        report.setDiplomaticInfluence(target.getDiplomaticInfluence());
        report.setPopulation(target.getPopulationMillions() != null ? target.getPopulationMillions().longValue() : 0L);
        report.setTechLevel(target.getTechLevel() != null ? target.getTechLevel().getLevel() : 0);
        report.setGovernmentType(target.getGovernmentType());

        // Get diplomatic relations
        List<String> allies = new ArrayList<>();
        List<String> enemies = new ArrayList<>();
        diplomacyService.getRelations(targetIso).forEach(relation -> {
            String otherFaction = relation.getFaction1Name().equals(targetIso)
                    ? relation.getFaction2Name() : relation.getFaction1Name();
            if (relation.isAtWar()) {
                enemies.add(otherFaction);
            } else if (relation.getActiveTreaties().contains("MUTUAL_DEFENSE")) {
                allies.add(otherFaction);
            }
        });
        report.setAllies(allies);
        report.setEnemies(enemies);

        // Check for detection risk
        double detectionChance = 0.1 + (target.getDiplomaticInfluence() != null ? target.getDiplomaticInfluence() / 500.0 : 0.05);
        Random random = new Random();
        if (random.nextDouble() < detectionChance) {
            activeSpy.get().setDetected(true);
            activeSpy.get().setActive(false);
            report.setSpyDetected(true);
            report.setMessage("Intelligence gathered but spy detected!");
        } else {
            report.setMessage("Intelligence gathered successfully on " + target.getName());
        }

        // Store report
        intelligenceReports.computeIfAbsent(targetIso, k -> new ArrayList<>()).add(report);

        log.info("Intelligence gathered: {} on {} by {}", report.getSpyId(), targetIso, report.getSpyId());
        eventService.broadcastIntelligenceGathered(report.getSpyId(), targetIso, report.isSpyDetected());
        return report;
    }

    /**
     * Run counter-espionage operation to detect and capture enemy spies.
     *
     * @param countryIso the country running counter-espionage
     * @return the counter-espionage result
     */
    public CounterEspionageResult runCounterEspionage(String countryIso) {
        Earth2026Country country = countryRepository.findByIsoCode(countryIso);
        if (country == null) throw new IllegalArgumentException("Country not found: " + countryIso);

        List<Spy> spiesInCountry = deployedSpies.get(countryIso);
        if (spiesInCountry == null || spiesInCountry.isEmpty()) {
            return new CounterEspionageResult(countryIso, 0, 0, "No spies detected in " + countryIso);
        }

        // Counter-intelligence capability based on diplomatic influence
        double ciCapability = country.getDiplomaticInfluence() != null ? country.getDiplomaticInfluence() / 100.0 : 0.5;
        Random random = new Random();

        int detected = 0;
        int captured = 0;

        for (Spy spy : spiesInCountry) {
            if (!spy.isDetected() && spy.isActive()) {
                if (random.nextDouble() < ciCapability) {
                    spy.setDetected(true);
                    detected++;

                    // Try to capture
                    if (random.nextDouble() < 0.7) {
                        spy.setActive(false);
                        captured++;
                        log.info("Spy {} captured in {}", spy.getId(), countryIso);
                    } else {
                        log.info("Spy {} detected in {} - escaped capture", spy.getId(), countryIso);
                    }
                }
            }
        }

        CounterEspionageResult result = new CounterEspionageResult(countryIso, detected, captured,
                "Counter-espionage: " + detected + " detected, " + captured + " captured");
        result.setTimestamp(System.currentTimeMillis());
        result.setCiCapability(ciCapability);

        log.info("Counter-espionage in {}: {} detected, {} captured", countryIso, detected, captured);
        eventService.broadcastCounterEspionage(countryIso, detected, captured);
        return result;
    }

    /**
     * Get all active spies deployed by a country.
     */
    public List<Spy> getSpiesByDeployer(String deployerIso) {
        List<Spy> result = new ArrayList<>();
        for (List<Spy> spies : deployedSpies.values()) {
            for (Spy spy : spies) {
                if (spy.getDeployerIso().equals(deployerIso) && spy.isActive()) {
                    result.add(spy);
                }
            }
        }
        return result;
    }

    /**
     * Get all spies deployed in a target country.
     */
    public List<Spy> getSpiesInTarget(String targetIso) {
        return deployedSpies.getOrDefault(targetIso, Collections.emptyList());
    }

    /**
     * Get intelligence reports for a country.
     */
    public List<IntelReport> getIntelligenceReports(String targetIso) {
        return intelligenceReports.getOrDefault(targetIso, Collections.emptyList());
    }

    /**
     * Calculate the success chance of spy deployment.
     */
    private double calculateSuccessChance(Earth2026Country deployer, Earth2026Country target, SpyType spyType) {
        double baseChance = 0.5;

        // Economic power helps (funding)
        if (deployer.getEconomicPower() != null) {
            baseChance += deployer.getEconomicPower() / 200.0;
        }

        // Target's diplomatic influence hinders (counter-intelligence)
        if (target.getDiplomaticInfluence() != null) {
            baseChance -= target.getDiplomaticInfluence() / 300.0;
        }

        // At war = harder to infiltrate
        if (diplomacyService.isAtWar(deployer.getIsoCode(), target.getIsoCode())) {
            baseChance -= 0.2;
        }

        // Spy type modifiers
        switch (spyType) {
            case INFILTRATOR: baseChance += 0.1; break;
            case SABOTEUR: baseChance -= 0.05; break;
            case ANALYST: baseChance += 0.15; break;
            case ASSASSIN: baseChance -= 0.15; break;
        }

        return Math.max(0.1, Math.min(0.9, baseChance));
    }

    /**
     * Calculate sabotage effectiveness.
     */
    private double calculateSabotageEffectiveness(Spy spy, Earth2026Country target, SabotageType sabotageType) {
        double baseEffectiveness = 0.5;

        // Spy type affects sabotage
        if (spy.getSpyType() == SpyType.SABOTEUR) {
            baseEffectiveness += 0.3;
        }

        // Target's defenses
        if (target.getMilitaryStrength() != null) {
            baseEffectiveness -= target.getMilitaryStrength() / 300.0;
        }

        // Sabotage type modifiers
        switch (sabotageType) {
            case INDUSTRY: baseEffectiveness += 0.1; break;
            case MILITARY: baseEffectiveness -= 0.1; break;
            case ECONOMIC: baseEffectiveness += 0.05; break;
        }

        return Math.max(0.1, Math.min(1.0, baseEffectiveness));
    }

    // =====================================================================
    // Inner Classes
    // =====================================================================

    public enum SpyType {
        INFILTRATOR,    // Stealth infiltration
        SABOTEUR,       // Sabotage operations
        ANALYST,        // Intelligence gathering
        ASSASSIN        // High-risk elimination
    }

    public enum SabotageType {
        INDUSTRY,       // Reduce production
        MILITARY,       // Weaken military
        ECONOMIC        // Disrupt trade
    }

    public static class Spy {
        private String id;
        private String deployerIso;
        private String targetIso;
        private SpyType spyType;
        private long deployedAt;
        private boolean active;
        private boolean detected;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getDeployerIso() { return deployerIso; }
        public void setDeployerIso(String deployerIso) { this.deployerIso = deployerIso; }
        public String getTargetIso() { return targetIso; }
        public void setTargetIso(String targetIso) { this.targetIso = targetIso; }
        public SpyType getSpyType() { return spyType; }
        public void setSpyType(SpyType spyType) { this.spyType = spyType; }
        public long getDeployedAt() { return deployedAt; }
        public void setDeployedAt(long deployedAt) { this.deployedAt = deployedAt; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public boolean isDetected() { return detected; }
        public void setDetected(boolean detected) { this.detected = detected; }
    }

    public static class SpyDeploymentResult {
        private String deployerIso;
        private String targetIso;
        private SpyType spyType;
        private boolean success;
        private double successChance;
        private String spyId;
        private String message;
        private long timestamp;

        public String getDeployerIso() { return deployerIso; }
        public void setDeployerIso(String deployerIso) { this.deployerIso = deployerIso; }
        public String getTargetIso() { return targetIso; }
        public void setTargetIso(String targetIso) { this.targetIso = targetIso; }
        public SpyType getSpyType() { return spyType; }
        public void setSpyType(SpyType spyType) { this.spyType = spyType; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public double getSuccessChance() { return successChance; }
        public void setSuccessChance(double successChance) { this.successChance = successChance; }
        public String getSpyId() { return spyId; }
        public void setSpyId(String spyId) { this.spyId = spyId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    public static class SabotageResult {
        private boolean success;
        private String message;
        private String targetIso;
        private String spyId;
        private SabotageType sabotageType;
        private double effectiveness;
        private boolean detected;
        private double productionLoss;
        private double militaryLoss;
        private double economicLoss;
        private long timestamp;

        public SabotageResult(boolean success, String message, String targetIso) {
            this.success = success;
            this.message = message;
            this.targetIso = targetIso;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getTargetIso() { return targetIso; }
        public String getSpyId() { return spyId; }
        public void setSpyId(String spyId) { this.spyId = spyId; }
        public SabotageType getSabotageType() { return sabotageType; }
        public void setSabotageType(SabotageType sabotageType) { this.sabotageType = sabotageType; }
        public double getEffectiveness() { return effectiveness; }
        public void setEffectiveness(double effectiveness) { this.effectiveness = effectiveness; }
        public boolean isDetected() { return detected; }
        public void setDetected(boolean detected) { this.detected = detected; }
        public double getProductionLoss() { return productionLoss; }
        public void setProductionLoss(double productionLoss) { this.productionLoss = productionLoss; }
        public double getMilitaryLoss() { return militaryLoss; }
        public void setMilitaryLoss(double militaryLoss) { this.militaryLoss = militaryLoss; }
        public double getEconomicLoss() { return economicLoss; }
        public void setEconomicLoss(double economicLoss) { this.economicLoss = economicLoss; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    public static class IntelReport {
        private String targetIso;
        private String targetName;
        private String spyId;
        private boolean success;
        private String message;
        private Integer militaryStrength;
        private Integer economicPower;
        private Integer diplomaticInfluence;
        private Long population;
        private Integer techLevel;
        private String governmentType;
        private List<String> allies = new ArrayList<>();
        private List<String> enemies = new ArrayList<>();
        private boolean spyDetected;
        private long timestamp;

        public IntelReport() {}

        public IntelReport(String targetIso, String message) {
            this.targetIso = targetIso;
            this.message = message;
            this.success = false;
        }

        public String getTargetIso() { return targetIso; }
        public void setTargetIso(String targetIso) { this.targetIso = targetIso; }
        public String getTargetName() { return targetName; }
        public void setTargetName(String targetName) { this.targetName = targetName; }
        public String getSpyId() { return spyId; }
        public void setSpyId(String spyId) { this.spyId = spyId; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Integer getMilitaryStrength() { return militaryStrength; }
        public void setMilitaryStrength(Integer militaryStrength) { this.militaryStrength = militaryStrength; }
        public Integer getEconomicPower() { return economicPower; }
        public void setEconomicPower(Integer economicPower) { this.economicPower = economicPower; }
        public Integer getDiplomaticInfluence() { return diplomaticInfluence; }
        public void setDiplomaticInfluence(Integer diplomaticInfluence) { this.diplomaticInfluence = diplomaticInfluence; }
        public Long getPopulation() { return population; }
        public void setPopulation(Long population) { this.population = population; }
        public Integer getTechLevel() { return techLevel; }
        public void setTechLevel(Integer techLevel) { this.techLevel = techLevel; }
        public String getGovernmentType() { return governmentType; }
        public void setGovernmentType(String governmentType) { this.governmentType = governmentType; }
        public List<String> getAllies() { return allies; }
        public void setAllies(List<String> allies) { this.allies = allies; }
        public List<String> getEnemies() { return enemies; }
        public void setEnemies(List<String> enemies) { this.enemies = enemies; }
        public boolean isSpyDetected() { return spyDetected; }
        public void setSpyDetected(boolean spyDetected) { this.spyDetected = spyDetected; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    public static class CounterEspionageResult {
        private String countryIso;
        private int detected;
        private int captured;
        private String message;
        private long timestamp;
        private double ciCapability;

        public CounterEspionageResult(String countryIso, int detected, int captured, String message) {
            this.countryIso = countryIso;
            this.detected = detected;
            this.captured = captured;
            this.message = message;
        }

        public String getCountryIso() { return countryIso; }
        public int getDetected() { return detected; }
        public int getCaptured() { return captured; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public double getCiCapability() { return ciCapability; }
        public void setCiCapability(double ciCapability) { this.ciCapability = ciCapability; }
    }
}
