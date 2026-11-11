package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eTensionPool;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eTensionPoolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing D&D 5e Tension Pools.
 * <p>
 * Tension pools track the consequences of player actions. As tension increases,
 * wandering encounters, alarms, and other complications become more likely.
 * This creates a dynamic, reactive game world where player choices matter.
 * </p>
 * <p>
 * Key mechanics:
 * - Increase tension when players make noise, fail checks, or trigger alarms
 * - Decrease tension when players are stealthy, rest, or leave an area
 * - Thresholds trigger consequences at specific tension levels
 * - Ultimate consequence fires when max tension is reached
 * - Tension resets at rest stops or when leaving an area
 * </p>
 */
@Service
public class Dnd5eTensionPoolService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eTensionPoolService.class);

    private final Dnd5eTensionPoolRepository poolRepository;

    public Dnd5eTensionPoolService(Dnd5eTensionPoolRepository poolRepository) {
        this.poolRepository = poolRepository;
        initializeTensionDatabase();
    }

    // ==================== Pool CRUD ====================

    @Transactional
    public Dnd5eTensionPool createPool(Dnd5eTensionPool pool) {
        log.info("Creating tension pool: {}", pool.getName());
        return poolRepository.save(pool);
    }

    public Optional<Dnd5eTensionPool> getPool(Long id) {
        return poolRepository.findById(id);
    }

    public List<Dnd5eTensionPool> listPools() {
        return poolRepository.findByActiveTrue();
    }

    public List<Dnd5eTensionPool> listPoolsByArea(String areaId) {
        return poolRepository.findByAreaIdAndActiveTrue(areaId);
    }

    public List<Dnd5eTensionPool> searchPools(String keyword) {
        return poolRepository.searchByKeyword(keyword);
    }

    @Transactional
    public void deletePool(Long id) {
        poolRepository.findById(id).ifPresent(pool -> {
            pool.setActive(false);
            poolRepository.save(pool);
            log.info("Deactivated tension pool: {}", pool.getName());
        });
    }

    // ==================== Tension Management ====================

    /**
     * Increase tension by the specified amount.
     * Checks thresholds and triggers consequences as tension crosses them.
     *
     * @param id tension pool id
     * @param amount amount to increase tension (clamped to 0..maxTension)
     * @return list of consequences triggered, empty if none
     */
    @Transactional
    public List<TensionConsequence> increaseTension(Long id, int amount) {
        Optional<Dnd5eTensionPool> opt = poolRepository.findById(id);
        if (opt.isEmpty()) {
            log.warn("Tension pool not found: {}", id);
            return new ArrayList<>();
        }

        Dnd5eTensionPool pool = opt.get();
        int oldTension = pool.getCurrentTension();
        int newTension = Math.min(pool.getMaxTension(), oldTension + amount);
        pool.setCurrentTension(newTension);
        poolRepository.save(pool);

        log.info("Tension increased: {} from {} to {} (area: {})", pool.getName(), oldTension, newTension, pool.getAreaId());

        // Check thresholds
        List<TensionConsequence> triggered = checkThresholds(pool, oldTension, newTension);

        // Check ultimate consequence
        if (newTension >= pool.getMaxTension() && oldTension < pool.getMaxTension()) {
            triggered.add(new TensionConsequence(
                    pool.getMaxTension(),
                    "ULTIMATE",
                    pool.getUltimateConsequence() != null ? pool.getUltimateConsequence() : "Maximum tension reached — ultimate consequence triggers."
            ));
            log.warn("ULTIMATE CONSEQUENCE triggered for pool: {}", pool.getName());
        }

        return triggered;
    }

    /**
     * Decrease tension by the specified amount (e.g., stealthy behavior, rest).
     *
     * @param id tension pool id
     * @param amount amount to decrease tension
     * @return updated tension pool
     */
    @Transactional
    public Dnd5eTensionPool decreaseTension(Long id, int amount) {
        Optional<Dnd5eTensionPool> opt = poolRepository.findById(id);
        if (opt.isEmpty()) {
            log.warn("Tension pool not found: {}", id);
            return null;
        }

        Dnd5eTensionPool pool = opt.get();
        int oldTension = pool.getCurrentTension();
        pool.setCurrentTension(oldTension - amount);
        poolRepository.save(pool);

        log.info("Tension decreased: {} from {} to {} (area: {})", pool.getName(), oldTension, pool.getCurrentTension(), pool.getAreaId());
        return pool;
    }

    /**
     * Set tension to a specific value (DM override).
     */
    @Transactional
    public Dnd5eTensionPool setTension(Long id, int tension) {
        Optional<Dnd5eTensionPool> opt = poolRepository.findById(id);
        if (opt.isEmpty()) {
            log.warn("Tension pool not found: {}", id);
            return null;
        }

        Dnd5eTensionPool pool = opt.get();
        pool.setCurrentTension(tension);
        poolRepository.save(pool);
        log.info("Tension set: {} to {} (area: {})", pool.getName(), tension, pool.getAreaId());
        return pool;
    }

    /**
     * Reset tension to 0 and clear triggered flags (e.g., rest, leaving area).
     */
    @Transactional
    public Dnd5eTensionPool resetTension(Long id) {
        Optional<Dnd5eTensionPool> opt = poolRepository.findById(id);
        if (opt.isEmpty()) {
            log.warn("Tension pool not found: {}", id);
            return null;
        }

        Dnd5eTensionPool pool = opt.get();
        int oldTension = pool.getCurrentTension();
        pool.setCurrentTension(0);
        // Reset triggered flags
        int thresholdCount = parseThresholds(pool.getThresholds()).size();
        pool.setTriggered(buildTriggeredArray(thresholdCount, false));
        poolRepository.save(pool);

        log.info("Tension reset: {} from {} to 0 (area: {})", pool.getName(), oldTension, pool.getAreaId());
        return pool;
    }

    /**
     * Reset all active tension pools (e.g., long rest, campaign reset).
     */
    @Transactional
    public int resetAllPools() {
        List<Dnd5eTensionPool> pools = poolRepository.findByActiveTrue();
        for (Dnd5eTensionPool pool : pools) {
            resetTension(pool.getId());
        }
        log.info("Reset all {} tension pools", pools.size());
        return pools.size();
    }

    // ==================== Threshold Checking ====================

    /**
     * Check which thresholds were crossed between oldTension and newTension.
     */
    List<TensionConsequence> checkThresholds(Dnd5eTensionPool pool, int oldTension, int newTension) {
        List<TensionConsequence> triggered = new ArrayList<>();
        List<Integer> thresholds = parseThresholds(pool.getThresholds());
        List<String> consequences = parseConsequences(pool.getConsequences());
        List<Boolean> triggeredFlags = parseTriggered(pool.getTriggered());

        for (int i = 0; i < thresholds.size(); i++) {
            int threshold = thresholds.get(i);
            boolean alreadyTriggered = i < triggeredFlags.size() && triggeredFlags.get(i);

            // Check if this threshold was crossed and not already triggered
            if (newTension >= threshold && oldTension < threshold && !alreadyTriggered) {
                String consequence = i < consequences.size()
                        ? consequences.get(i)
                        : "Tension reached " + threshold + " — a consequence occurs.";

                triggered.add(new TensionConsequence(threshold, "THRESHOLD", consequence));

                // Mark as triggered
                if (i < triggeredFlags.size()) {
                    triggeredFlags.set(i, true);
                }
                log.info("Threshold {} triggered for pool {}: {}", threshold, pool.getName(), consequence);
            }
        }

        // Update triggered flags
        if (!triggered.isEmpty()) {
            pool.setTriggered(buildTriggeredArray(triggeredFlags.size(), triggeredFlags));
            poolRepository.save(pool);
        }

        return triggered;
    }

    // ==================== Wandering Encounters ====================

    /**
     * Roll for a wandering encounter based on current tension level.
     * Higher tension = higher chance of encounter.
     *
     * @param id tension pool id
     * @param roll d20 roll result
     * @return encounter result with encounter triggered flag
     */
    public WanderingEncounterResult rollWanderingEncounter(Long id, int roll) {
        Optional<Dnd5eTensionPool> opt = poolRepository.findById(id);
        if (opt.isEmpty()) {
            log.warn("Tension pool not found: {}", id);
            return new WanderingEncounterResult(id, 0, roll, false, "Tension pool not found");
        }

        Dnd5eTensionPool pool = opt.get();
        int tension = pool.getCurrentTension();
        int maxTension = pool.getMaxTension();

        // Encounter DC decreases as tension increases
        // At tension 0: DC 20 (very unlikely)
        // At tension max: DC 5 (very likely)
        int dc = Math.max(5, 20 - tension);

        boolean encounterTriggered = roll >= dc;
        String description;

        if (encounterTriggered) {
            description = String.format(
                    "Encounter triggered! Tension %d/%d, DC %d, roll %d. " +
                    "Area: %s. The party's actions have drawn attention.",
                    tension, maxTension, dc, roll, pool.getAreaId()
            );
        } else {
            description = String.format(
                    "No encounter. Tension %d/%d, DC %d, roll %d. The party remains undetected.",
                    tension, maxTension, dc, roll, pool.getAreaId()
            );
        }

        log.info("Wandering encounter roll for {}: roll={}, dc={}, triggered={}", pool.getName(), roll, dc, encounterTriggered);
        return new WanderingEncounterResult(id, tension, roll, encounterTriggered, description);
    }

    /**
     * Get the encounter DC for a tension pool based on current tension.
     */
    public int getEncounterDc(Long id) {
        Optional<Dnd5eTensionPool> opt = poolRepository.findById(id);
        if (opt.isEmpty()) {
            return 20;
        }
        Dnd5eTensionPool pool = opt.get();
        return Math.max(5, 20 - pool.getCurrentTension());
    }

    // ==================== Reference Data ====================

    public List<String> listAreas() {
        return poolRepository.findDistinctAreaIds();
    }

    public List<Dnd5eTensionPool> listMaxedPools() {
        return poolRepository.findMaxedTensionPools();
    }

    public List<Dnd5eTensionPool> listEmptyPools() {
        return poolRepository.findEmptyTensionPools();
    }

    // ==================== JSON Helpers ====================

    List<Integer> parseThresholds(String thresholdsJson) {
        if (thresholdsJson == null || thresholdsJson.isEmpty() || thresholdsJson.equals("[]")) {
            return new ArrayList<>();
        }
        List<Integer> thresholds = new ArrayList<>();
        String cleaned = thresholdsJson.replaceAll("[\\[\\]]", "").trim();
        if (cleaned.isEmpty()) return thresholds;
        for (String part : cleaned.split(",")) {
            try {
                thresholds.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException e) {
                log.warn("Invalid threshold value: {}", part);
            }
        }
        return thresholds;
    }

    List<String> parseConsequences(String consequencesJson) {
        if (consequencesJson == null || consequencesJson.isEmpty() || consequencesJson.equals("[]")) {
            return new ArrayList<>();
        }
        List<String> consequences = new ArrayList<>();
        String cleaned = consequencesJson.replaceAll("[\\[\\]]", "").trim();
        if (cleaned.isEmpty()) return consequences;
        // Split by ", " but respect quoted strings
        String[] parts = cleaned.split(",(?=\\s*(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String part : parts) {
            String trimmed = part.trim().replaceAll("^\"|\"$", "");
            if (!trimmed.isEmpty()) {
                consequences.add(trimmed);
            }
        }
        return consequences;
    }

    List<Boolean> parseTriggered(String triggeredJson) {
        if (triggeredJson == null || triggeredJson.isEmpty() || triggeredJson.equals("[]")) {
            return new ArrayList<>();
        }
        List<Boolean> triggered = new ArrayList<>();
        String cleaned = triggeredJson.replaceAll("[\\[\\]]", "").trim();
        if (cleaned.isEmpty()) return triggered;
        for (String part : cleaned.split(",")) {
            try {
                triggered.add(Boolean.parseBoolean(part.trim()));
            } catch (Exception e) {
                triggered.add(false);
            }
        }
        return triggered;
    }

    String buildTriggeredArray(int size, boolean value) {
        boolean[] arr = new boolean[size];
        Arrays.fill(arr, value);
        return Arrays.toString(arr).replace(" ", "");
    }

    String buildTriggeredArray(int size, List<Boolean> values) {
        boolean[] arr = new boolean[size];
        for (int i = 0; i < size && i < values.size(); i++) {
            arr[i] = values.get(i);
        }
        return Arrays.toString(arr).replace(" ", "");
    }

    // ==================== Initialization ====================

    @Transactional
    public void initializeTensionDatabase() {
        if (poolRepository.findByActiveTrue().size() > 0) {
            log.info("Tension pool database already populated");
            return;
        }

        log.info("Initializing tension pool database...");
        populateExamplePools();
        log.info("Tension pool database initialized: {} pools", poolRepository.findByActiveTrue().size());
    }

    @Transactional
    public void populateExamplePools() {
        // Castle Ward — Guard Alertness
        Dnd5eTensionPool castle = new Dnd5eTensionPool();
        castle.setName("Guard Alertness");
        castle.setDescription("Tracks how suspicious the castle guards are of the party's presence. " +
                "Increases when guards spot the party, when noise is made, or when the party fails stealth checks.");
        castle.setAreaId("Castle Ward");
        castle.setMaxTension(10);
        castle.setThresholds("[3, 5, 7, 9]");
        castle.setConsequences("[\"Guards become watchful and patrol more frequently\", " +
                "\"A guard captain is alerted and investigates suspicious areas\", " +
                "\"Reinforcements arrive from the barracks\", " +
                "\"Full alarm — all guards are on high alert and search parties are dispatched\"]");
        castle.setUltimateConsequence("The party is discovered and confronted by the entire guard force. " +
                "Escape becomes nearly impossible without combat or powerful magic.");
        castle.setTriggered("[false, false, false, false]");
        poolRepository.save(castle);

        // Dark Forest — Predator Attention
        Dnd5eTensionPool forest = new Dnd5eTensionPool();
        forest.setName("Predator Attention");
        forest.setDescription("Tracks how much attention the party has drawn from creatures in the dark forest. " +
                "Increases when fire is lit, when loud noises occur, or when creatures are killed.");
        forest.setAreaId("Dark Forest");
        forest.setMaxTension(8);
        forest.setThresholds("[2, 4, 6]");
        forest.setConsequences("[\"Small predators begin stalking the party from the shadows\", " +
                "\"A pack of wolves or similar creatures circles the perimeter\", " +
                "\"A powerful apex predator takes notice and begins hunting the party\"]");
        forest.setUltimateConsequence("The forest itself seems to turn against the party. " +
                "Multiple predators attack simultaneously, and the party is surrounded.");
        forest.setTriggered("[false, false, false]");
        poolRepository.save(forest);

        // Underground Lair — Insect Agitation
        Dnd5eTensionPool lair = new Dnd5eTensionPool();
        lair.setName("Insect Agitation");
        lair.setDescription("Tracks the agitation level of insects in an underground lair. " +
                "Increases when vibrations occur, when light is used, or when insects are harmed.");
        lair.setAreaId("Underground Lair");
        lair.setMaxTension(10);
        lair.setThresholds("[3, 6, 8]");
        lair.setConsequences("[\"Insects begin swarming and buzzing louder\", " +
                "\"Swarm creatures appear and harass the party\", " +
                "\"The insect queen becomes aware and sends elite guards\"]");
        lair.setUltimateConsequence("The entire insect colony attacks. The queen emerges, " +
                "and the lair floods with hostile creatures from every direction.");
        lair.setTriggered("[false, false, false]");
        poolRepository.save(lair);

        // Feywild Grove — Fey Suspicion
        Dnd5eTensionPool fey = new Dnd5eTensionPool();
        fey.setName("Fey Suspicion");
        fey.setDescription("Tracks how suspicious the fey creatures are of mortal intruders. " +
                "Increases when the party breaks fey rules, harms fey creatures, or disrespects sacred sites.");
        fey.setAreaId("Feywild Grove");
        fey.setMaxTension(10);
        fey.setThresholds("[2, 5, 7, 9]");
        fey.setConsequences("[\"Fey creatures give the party warning glares and whisper among themselves\", " +
                "\"Minor fey pranks target the party — items go missing, paths shift\", " +
                "\"A fey lord takes notice and sends emissaries to interrogate the party\", " +
                "\"The fey court declares the party enemies of the grove\"]");
        fey.setUltimateConsequence("The feywild itself turns hostile. The party is trapped in an endless loop " +
                "until they make amends or find a way to break the enchantment.");
        fey.setTriggered("[false, false, false, false]");
        poolRepository.save(fey);

        // Dragon's Lair — Dragon's Patience
        Dnd5eTensionPool dragon = new Dnd5eTensionPool();
        dragon.setName("Dragon's Patience");
        dragon.setDescription("Tracks a dragon's patience with intruders in its lair. " +
                "Increases when treasure is disturbed, when the dragon is insulted, or when attacks occur.");
        dragon.setAreaId("Dragon's Lair");
        dragon.setMaxTension(6);
        dragon.setThresholds("[2, 4]");
        dragon.setConsequences("[\"The dragon becomes aware and begins watching the party closely\", " +
                "\"The dragon confronts the party directly, demanding they leave\"]");
        dragon.setUltimateConsequence("The dragon's patience runs out. It attacks with full force, " +
                "using all breath weapons and lair actions to destroy the intruders.");
        dragon.setTriggered("[false, false]");
        poolRepository.save(dragon);
    }

    // ==================== DTOs ====================

    /**
     * Result of a threshold check — contains the threshold level and consequence description.
     */
    public static class TensionConsequence {
        private final int threshold;
        private final String type;
        private final String description;

        public TensionConsequence(int threshold, String type, String description) {
            this.threshold = threshold;
            this.type = type;
            this.description = description;
        }

        public int getThreshold() {
            return threshold;
        }

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Result of a wandering encounter roll.
     */
    public static class WanderingEncounterResult {
        private final Long poolId;
        private final int tension;
        private final int roll;
        private final boolean encounterTriggered;
        private final String description;

        public WanderingEncounterResult(Long poolId, int tension, int roll, boolean encounterTriggered, String description) {
            this.poolId = poolId;
            this.tension = tension;
            this.roll = roll;
            this.encounterTriggered = encounterTriggered;
            this.description = description;
        }

        public Long getPoolId() {
            return poolId;
        }

        public int getTension() {
            return tension;
        }

        public int getRoll() {
            return roll;
        }

        public boolean isEncounterTriggered() {
            return encounterTriggered;
        }

        public String getDescription() {
            return description;
        }
    }
}
