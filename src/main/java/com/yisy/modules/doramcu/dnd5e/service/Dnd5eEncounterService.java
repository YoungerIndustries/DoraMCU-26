package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.dto.EncounterAnalysis;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eEncounter;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eEncounterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for D&D 5e Encounter Balancing.
 * <p>
 * Implements the DMG encounter building rules (p.82-86) for determining
 * encounter difficulty based on party size, level, monster XP, and
 * multiple encounter adjustments.
 * </p>
 * <p>
 * Key mechanics:
 * - XP Thresholds: Table of XP values by party size and level for each difficulty
 * - Difficulty Determination: Compare adjusted XP against thresholds
 * - Multiple Encounter Adjustment: Multiplier based on recent encounters
 * - Tension Bonus: Additional XP from tension pools to increase difficulty
 * - CR to XP: Challenge Rating to XP value mapping
 * - Encounter Analysis: Full analysis with recommendation
 * </p>
 * <p>
 * XP Thresholds (DMG p.82) — per character, scaled by party size:
 * Level | Trivial | Easy | Medium | Hard | Deadly
 * 1     | 25      | 50   | 100    | 200  | 300
 * 2     | 50      | 100  | 200    | 400  | 600
 * 3     | 75      | 150  | 300    | 600  | 900
 * 4     | 125     | 250  | 500    | 1000 | 1500
 * ... (scales with level)
 * </p>
 * <p>
 * Multiple Encounter Adjustment (DMG p.85):
 * 0 recent encounters: 1.0x
 * 1 recent encounter:  1.5x
 * 2 recent encounters: 1.5x
 * 3+ recent encounters: 2.0x
 * </p>
 */
@Service
public class Dnd5eEncounterService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eEncounterService.class);

    // XP per character by level (DMG p.82)
    // Index: level-1, Values: [Trivial, Easy, Medium, Hard, Deadly]
    private static final int[][] XP_PER_CHARACTER = {
            {25, 50, 100, 200, 300},      // Level 1
            {50, 100, 200, 400, 600},     // Level 2
            {75, 150, 300, 600, 900},     // Level 3
            {125, 250, 500, 1000, 1500},  // Level 4
            {250, 500, 1000, 2000, 3000}, // Level 5
            {300, 600, 1200, 2400, 3600}, // Level 6
            {350, 750, 1500, 3000, 4800}, // Level 7
            {400, 800, 1600, 3200, 4800}, // Level 8
            {450, 1000, 2000, 4000, 5700},// Level 9
            {500, 1000, 2000, 4000, 6000},// Level 10
            {500, 1000, 2000, 4000, 6000},// Level 11
            {500, 1000, 2000, 4000, 6000},// Level 12
            {550, 1000, 2000, 4000, 6000},// Level 13
            {600, 1000, 2000, 4000, 6000},// Level 14
            {600, 1000, 2000, 4000, 6000},// Level 15
            {600, 1000, 2000, 4000, 6000},// Level 16
            {600, 1000, 2000, 4000, 6000},// Level 17
            {600, 1000, 2000, 4000, 6000},// Level 18
            {600, 1000, 2000, 4000, 6000},// Level 19
            {600, 1000, 2000, 4000, 6000} // Level 20
    };

    // CR to XP mapping (DMG p.282)
    // Index: CR-1, so CR_TO_XP[0] = CR 1, CR_TO_XP[29] = CR 30
    private static final int[] CR_TO_XP = {
            200,   // CR 1
            450,   // CR 2
            700,   // CR 3
            1000,  // CR 4
            1300,  // CR 5
            1600,  // CR 6
            1900,  // CR 7
            2200,  // CR 8
            2500,  // CR 9
            2800,  // CR 10
            3600,  // CR 11
            4500,  // CR 12
            5500,  // CR 13
            6000,  // CR 14
            7000,  // CR 15
            8500,  // CR 16
            10000, // CR 17
            11500, // CR 18
            13000, // CR 19
            15000, // CR 20
            18000, // CR 21
            21000, // CR 22
            23000, // CR 23
            25000, // CR 24
            30000, // CR 25
            33000, // CR 26
            36000, // CR 27
            42000, // CR 28
            47000, // CR 29
            50000  // CR 30
    };

    // More precise CR to XP for fractional CRs
    private static final Map<String, Integer> FRACTIONAL_CR_XP = new LinkedHashMap<>();
    static {
        FRACTIONAL_CR_XP.put("0", 0);
        FRACTIONAL_CR_XP.put("1/8", 25);
        FRACTIONAL_CR_XP.put("1/4", 50);
        FRACTIONAL_CR_XP.put("1/2", 100);
    }

    private final Dnd5eEncounterRepository encounterRepository;

    public Dnd5eEncounterService(Dnd5eEncounterRepository encounterRepository) {
        this.encounterRepository = encounterRepository;
        initializeEncounterDatabase();
    }

    // ==================== Encounter CRUD ====================

    @Transactional
    public Dnd5eEncounter createEncounter(Dnd5eEncounter encounter) {
        log.info("Creating encounter: {}", encounter.getName());
        // Auto-calculate thresholds and difficulty
        int[] thresholds = getXpThresholds(encounter.getPartyLevel(), encounter.getPartySize());
        encounter.setMediumThreshold(thresholds[2]);
        encounter.setDeadlyThreshold(thresholds[4]);

        double multiplier = getEncounterMultiplier(encounter.getRecentEncounters());
        encounter.setEncounterMultiplier(multiplier);

        int adjustedXp = (int) (encounter.getMonsterXp() * multiplier) + encounter.getTensionBonus();
        encounter.setAdjustedXp(adjustedXp);
        encounter.setDifficulty(determineDifficulty(adjustedXp, thresholds));

        return encounterRepository.save(encounter);
    }

    public Optional<Dnd5eEncounter> getEncounter(Long id) {
        return encounterRepository.findById(id);
    }

    public List<Dnd5eEncounter> listEncounters() {
        return encounterRepository.findByActiveTrue();
    }

    public List<Dnd5eEncounter> listEncountersByDifficulty(String difficulty) {
        return encounterRepository.findByDifficultyAndActiveTrue(difficulty);
    }

    public List<Dnd5eEncounter> listEncountersByLevel(int partyLevel) {
        return encounterRepository.findByPartyLevelAndActiveTrue(partyLevel);
    }

    public List<Dnd5eEncounter> listEncountersByLevelRange(int minLevel, int maxLevel) {
        return encounterRepository.findByLevelRange(minLevel, maxLevel);
    }

    public List<Dnd5eEncounter> listEncountersByXpRange(int minXp, int maxXp) {
        return encounterRepository.findByXpRange(minXp, maxXp);
    }

    public List<Dnd5eEncounter> listEncountersByCrRange(double minCr, double maxCr) {
        return encounterRepository.findByCrRange(minCr, maxCr);
    }

    public List<Dnd5eEncounter> searchEncounters(String keyword) {
        return encounterRepository.searchByKeyword(keyword);
    }

    @Transactional
    public void deleteEncounter(Long id) {
        encounterRepository.findById(id).ifPresent(encounter -> {
            encounter.setActive(false);
            encounterRepository.save(encounter);
            log.info("Deactivated encounter: {}", encounter.getName());
        });
    }

    // ==================== XP Threshold Calculations ====================

    /**
     * Get XP thresholds for a given party level and size.
     * Returns array: [Trivial, Easy, Medium, Hard, Deadly]
     *
     * @param partyLevel average party level (1-20)
     * @param partySize number of party members (1-8)
     * @return XP thresholds for each difficulty
     */
    public int[] getXpThresholds(int partyLevel, int partySize) {
        int levelIndex = Math.max(0, Math.min(19, partyLevel - 1));
        int size = Math.max(1, Math.min(8, partySize));

        // For party sizes > 4, scale up; for < 4, scale down
        int[] base = XP_PER_CHARACTER[levelIndex];
        int[] thresholds = new int[5];

        for (int i = 0; i < 5; i++) {
            if (size <= 4) {
                // Scale down: multiply by size/4
                thresholds[i] = (int) Math.round(base[i] * size / 4.0);
            } else {
                // Scale up: use base * 4 for party of 4, then add for extra members
                // DMG formula: for party > 4, multiply by (size - 3) / 1.0
                // Simplified: base * 4 for size 4, then * 1.5 for each additional member
                double multiplier = 1.0;
                for (int s = 5; s <= size; s++) {
                    multiplier *= 1.5;
                }
                thresholds[i] = (int) Math.round(base[i] * 4.0 * multiplier / 4.0);
            }
        }

        return thresholds;
    }

    /**
     * Determine encounter difficulty based on adjusted XP and thresholds.
     *
     * @param adjustedXp the adjusted XP value
     * @param thresholds array of [Trivial, Easy, Medium, Hard, Deadly]
     * @return difficulty string
     */
    public String determineDifficulty(int adjustedXp, int[] thresholds) {
        if (adjustedXp < thresholds[0]) {
            return "Trivial";
        } else if (adjustedXp < thresholds[1]) {
            return "Easy";
        } else if (adjustedXp < thresholds[2]) {
            return "Medium";
        } else if (adjustedXp < thresholds[3]) {
            return "Hard";
        } else {
            return "Deadly";
        }
    }

    /**
     * Get the encounter multiplier based on number of recent encounters.
     *
     * @param recentEncounters number of recent encounters
     * @return multiplier (1.0, 1.5, or 2.0)
     */
    public double getEncounterMultiplier(int recentEncounters) {
        if (recentEncounters == 0) {
            return 1.0;
        } else if (recentEncounters <= 2) {
            return 1.5;
        } else {
            return 2.0;
        }
    }

    /**
     * Get XP value for a given Challenge Rating.
     *
     * @param cr challenge rating (0-30)
     * @return XP value
     */
    public int getCrXp(double cr) {
        if (cr <= 0) {
            return 0;
        } else if (cr < 0.25) {
            return 25;  // CR 1/8
        } else if (cr < 0.5) {
            return 50;  // CR 1/4
        } else if (cr < 1.0) {
            return 100; // CR 1/2
        }

        int crInt = (int) Math.ceil(cr);
        if (crInt < 1) crInt = 1;
        if (crInt > 30) crInt = 30;
        return CR_TO_XP[crInt - 1];
    }

    // ==================== Encounter Analysis ====================

    /**
     * Analyze an encounter to determine its difficulty and provide recommendations.
     *
     * @param partyLevel average party level
     * @param partySize number of party members
     * @param monsterXp total monster XP
     * @param recentEncounters number of recent encounters
     * @param tensionBonus tension pool bonus
     * @return full encounter analysis
     */
    public EncounterAnalysis analyzeEncounter(int partyLevel, int partySize, int monsterXp,
                                               int recentEncounters, int tensionBonus) {
        int level = Math.max(1, Math.min(20, partyLevel));
        int size = Math.max(1, Math.min(8, partySize));

        int[] thresholds = getXpThresholds(level, size);
        double multiplier = getEncounterMultiplier(recentEncounters);
        int adjustedXp = (int) (monsterXp * multiplier) + tensionBonus;
        String difficulty = determineDifficulty(adjustedXp, thresholds);

        String recommendation = generateRecommendation(difficulty, adjustedXp, thresholds, monsterXp, tensionBonus);

        return new EncounterAnalysis(
                level, size, monsterXp, multiplier, tensionBonus, adjustedXp, difficulty,
                thresholds[0], thresholds[1], thresholds[2], thresholds[3], thresholds[4],
                recommendation
        );
    }

    /**
     * Analyze an existing encounter entity.
     *
     * @param id encounter id
     * @return encounter analysis, or null if not found
     */
    public EncounterAnalysis analyzeEncounter(Long id) {
        return encounterRepository.findById(id).map(encounter ->
                analyzeEncounter(encounter.getPartyLevel(), encounter.getPartySize(),
                        encounter.getMonsterXp(), encounter.getRecentEncounters(),
                        encounter.getTensionBonus())
        ).orElse(null);
    }

    /**
     * Generate a recommendation based on the encounter analysis.
     */
    private String generateRecommendation(String difficulty, int adjustedXp, int[] thresholds,
                                          int monsterXp, int tensionBonus) {
        switch (difficulty) {
            case "Trivial":
                return "This encounter is very easy. Consider adding more monsters or increasing CR for a Medium difficulty.";
            case "Easy":
                return "A good warm-up encounter. Good for introducing new mechanics or giving the party a breather.";
            case "Medium":
                return "Well-balanced encounter. This is the standard difficulty for most combats in an adventure.";
            case "Hard":
                return "Challenging encounter. The party will need to use resources and tactics to succeed.";
            case "Deadly":
                return "Very dangerous encounter. Reserve these for climactic battles or optional challenges.";
            default:
                return "Unknown difficulty.";
        }
    }

    // ==================== Tension-Based Encounters ====================

    /**
     * Calculate tension bonus for an encounter.
     * Each tension level adds 10% of the base monster XP as a bonus.
     *
     * @param monsterXp base monster XP
     * @param tensionLevel current tension level
     * @return tension bonus XP
     */
    public int calculateTensionBonus(int monsterXp, int tensionLevel) {
        if (tensionLevel <= 0) return 0;
        return (int) (monsterXp * tensionLevel * 0.1);
    }

    /**
     * Create a tension-based encounter that scales with the current tension level.
     *
     * @param name encounter name
     * @param description encounter description
     * @param partyLevel average party level
     * @param partySize number of party members
     * @param monsterXp base monster XP
     * @param monsterCount number of monsters
     * @param averageCr average challenge rating
     * @param tensionLevel current tension level
     * @return created encounter
     */
    @Transactional
    public Dnd5eEncounter createTensionEncounter(String name, String description,
                                                  int partyLevel, int partySize,
                                                  int monsterXp, int monsterCount,
                                                  double averageCr, int tensionLevel) {
        int tensionBonus = calculateTensionBonus(monsterXp, tensionLevel);

        Dnd5eEncounter encounter = new Dnd5eEncounter();
        encounter.setName(name);
        encounter.setDescription(description);
        encounter.setPartyLevel(partyLevel);
        encounter.setPartySize(partySize);
        encounter.setMonsterXp(monsterXp);
        encounter.setMonsterCount(monsterCount);
        encounter.setAverageCr(averageCr);
        encounter.setTensionBonus(tensionBonus);

        return createEncounter(encounter);
    }

    // ==================== Reference Data ====================

    /**
     * Get all difficulty levels.
     */
    public List<String> listDifficulties() {
        return Arrays.asList("Trivial", "Easy", "Medium", "Hard", "Deadly");
    }

    /**
     * Get XP thresholds for all levels for a given party size.
     */
    public Map<Integer, int[]> getXpThresholdsByLevel(int partySize) {
        Map<Integer, int[]> thresholds = new LinkedHashMap<>();
        for (int level = 1; level <= 20; level++) {
            thresholds.put(level, getXpThresholds(level, partySize));
        }
        return thresholds;
    }

    /**
     * Get CR to XP mapping.
     */
    public Map<String, Integer> getCrXpTable() {
        Map<String, Integer> table = new LinkedHashMap<>();
        table.put("0", 0);
        table.put("1/8", 25);
        table.put("1/4", 50);
        table.put("1/2", 100);
        for (int cr = 1; cr <= 30; cr++) {
            table.put(String.valueOf(cr), getCrXp(cr));
        }
        return table;
    }

    /**
     * Get encounter multiplier table.
     */
    public Map<Integer, Double> getMultiplierTable() {
        Map<Integer, Double> table = new LinkedHashMap<>();
        for (int i = 0; i <= 5; i++) {
            table.put(i, getEncounterMultiplier(i));
        }
        return table;
    }

    // ==================== Database Initialization ====================

    @Transactional
    public void initializeEncounterDatabase() {
        if (encounterRepository.countByActiveTrue() > 0) {
            log.info("Encounter database already populated, skipping initialization.");
            return;
        }

        log.info("Initializing encounter database with example encounters...");

        // Example encounters for different levels
        List<Dnd5eEncounter> encounters = new ArrayList<>();

        // Level 1-4 encounters
        encounters.add(createExampleEncounter("Goblin Ambush", "A group of goblins attacks the party on the road.",
                1, 4, 200, 4, 0.25, 0));
        encounters.add(createExampleEncounter("Skeleton Guard", "Undead skeletons guard a crypt entrance.",
                2, 4, 400, 4, 0.25, 0));
        encounters.add(createExampleEncounter("Orc Warband", "An orc warband blocks the mountain pass.",
                3, 4, 1200, 6, 1.0, 0));
        encounters.add(createExampleEncounter("Giant Spider Nest", "A nest of giant spiders in a dark cave.",
                4, 4, 1600, 8, 1.0, 0));

        // Level 5-8 encounters
        encounters.add(createExampleEncounter("Wight Lord", "A wight and its undead minions ambush the party.",
                5, 4, 2200, 5, 3.0, 0));
        encounters.add(createExampleEncounter("Minotaur Maze", "A minotaur guards a labyrinth.",
                6, 4, 2800, 1, 3.0, 0));
        encounters.add(createExampleEncounter("Dragon Wyrmling", "A young red dragon defends its lair.",
                7, 4, 3400, 1, 6.0, 0));
        encounters.add(createExampleEncounter("Undead Army", "A necromancer's undead army attacks.",
                8, 4, 4000, 12, 1.0, 0));

        // Level 9-12 encounters
        encounters.add(createExampleEncounter("Young Dragon", "A young black dragon attacks the village.",
                10, 4, 5500, 1, 10.0, 0));
        encounters.add(createExampleEncounter("Archdevil Minions", "Devil servants of an archdevil.",
                11, 4, 6000, 8, 6.0, 0));
        encounters.add(createExampleEncounter("Elder Brain Spawn", "Mind flayer spawn guard an elder brain.",
                12, 4, 6000, 6, 8.0, 0));

        // Level 13-16 encounters
        encounters.add(createExampleEncounter("Adult Dragon", "An adult blue dragon terrorizes the region.",
                14, 4, 10000, 1, 14.0, 0));
        encounters.add(createExampleEncounter("Demon Web", "A demon web of nightmares.",
                15, 4, 10000, 10, 8.0, 0));
        encounters.add(createExampleEncounter("Lich Phylactery", "A lich and its guardians.",
                16, 4, 10000, 1, 21.0, 0));

        // Level 17-20 encounters
        encounters.add(createExampleEncounter("Ancient Dragon", "An ancient red dragon.",
                18, 4, 15000, 1, 24.0, 0));
        encounters.add(createExampleEncounter("Archdevil", "An archdevil's personal army.",
                20, 4, 25000, 20, 15.0, 0));

        encounterRepository.saveAll(encounters);
        log.info("Initialized {} example encounters.", encounters.size());
    }

    private Dnd5eEncounter createExampleEncounter(String name, String description,
                                                   int partyLevel, int partySize,
                                                   int monsterXp, int monsterCount,
                                                   double averageCr, int tensionLevel) {
        Dnd5eEncounter encounter = new Dnd5eEncounter();
        encounter.setName(name);
        encounter.setDescription(description);
        encounter.setPartyLevel(partyLevel);
        encounter.setPartySize(partySize);
        encounter.setMonsterXp(monsterXp);
        encounter.setMonsterCount(monsterCount);
        encounter.setAverageCr(averageCr);
        encounter.setTensionBonus(calculateTensionBonus(monsterXp, tensionLevel));
        return encounter;
    }

    /**
     * Populate the encounter database (alias for initialize).
     */
    @Transactional
    public void populateEncounters() {
        initializeEncounterDatabase();
    }
}
