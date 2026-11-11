package com.yisy.modules.doramcu.thirdreich.commander;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for managing AI commanders in the 3R4 ground combat engine.
 * <p>
 * Provides methods to create commanders from historical profiles, generate
 * commanders from game characters, and retrieve existing commanders.
 * </p>
 */
public class AICommanderService {

    private final Map<String, AICommander> commanders = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final AtomicInteger idCounter = new AtomicInteger(1);

    /**
     * Creates a commander from a historical profile.
     *
     * @param profile the historical profile to use
     * @return the created AICommander
     */
    public AICommander createFromProfile(AICommanderProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("Profile must not be null");
        }
        AICommander commander = profile.createCommander();
        commanders.put(commander.getId(), commander);
        return commander;
    }

    /**
     * Creates a commander from a game character by mapping stats to leadership and traits.
     * <p>
     * This method accepts generic character stats and derives appropriate commander
     * properties. The expected stats map should contain:
     * <ul>
     *   <li>"name" - Character name (String)</li>
     *   <li>"leadership" - Leadership rating 1-10 (Integer, optional)</li>
     *   <li>"aggression" - Aggression level 1-10 (Integer, optional)</li>
     *   <li>"tactics" - Tactical skill 1-10 (Integer, optional)</li>
     *   <li>"morale" - Morale influence 1-10 (Integer, optional)</li>
     * </ul>
     *
     * @param stats the character stats map
     * @param sourceGame the source game system name
     * @return the generated AICommander
     */
    public AICommander generateFromCharacter(Map<String, Object> stats, String sourceGame) {
        if (stats == null || stats.isEmpty()) {
            throw new IllegalArgumentException("Character stats must not be null or empty");
        }

        String name = (String) stats.getOrDefault("name", "Unknown Commander");
        String id = "cmd-" + sourceGame + "-" + idCounter.getAndIncrement();

        AICommander commander = new AICommander(id, name);
        commander.setSourceGame(sourceGame);

        // Map leadership stat (default 5)
        int leadership = 5;
        if (stats.containsKey("leadership")) {
            leadership = ((Number) stats.get("leadership")).intValue();
        }
        commander.setLeadership(leadership);

        // Determine tactical preference based on aggression
        int aggression = 5;
        if (stats.containsKey("aggression")) {
            aggression = ((Number) stats.get("aggression")).intValue();
        }
        if (aggression >= 7) {
            commander.setTacticalPreference(AICommander.TacticalPreference.AGGRESSIVE);
        } else if (aggression <= 3) {
            commander.setTacticalPreference(AICommander.TacticalPreference.DEFENSIVE);
        } else {
            commander.setTacticalPreference(AICommander.TacticalPreference.BALANCED);
        }

        // Assign traits based on stats
        int tactics = 5;
        if (stats.containsKey("tactics")) {
            tactics = ((Number) stats.get("tactics")).intValue();
        }
        int morale = 5;
        if (stats.containsKey("morale")) {
            morale = ((Number) stats.get("morale")).intValue();
        }

        // Assign up to 3 traits based on highest stats
        if (aggression >= 7) {
            commander.addTrait(CommanderTrait.AGGRESSIVE);
        }
        if (tactics >= 7) {
            commander.addTrait(CommanderTrait.FLANKER);
        }
        if (morale >= 7) {
            commander.addTrait(CommanderTrait.INSPIRATIONAL);
        }
        if (leadership >= 8) {
            commander.addTrait(CommanderTrait.LOGISTICIAN);
        }

        // If no traits assigned, give a random one
        if (commander.getTraits().isEmpty()) {
            CommanderTrait[] allTraits = CommanderTrait.values();
            commander.addTrait(allTraits[random.nextInt(allTraits.length)]);
        }

        commanders.put(id, commander);
        return commander;
    }

    /**
     * Creates a random commander with generated traits.
     *
     * @param name the commander's name
     * @return a randomly generated AICommander
     */
    public AICommander createRandomCommander(String name) {
        String id = "cmd-random-" + idCounter.getAndIncrement();
        AICommander commander = new AICommander(id, name);
        commander.setLeadership(3 + random.nextInt(8));
        commander.setTacticalPreference(
                AICommander.TacticalPreference.values()[random.nextInt(3)]);

        // Assign 1-3 random traits
        CommanderTrait[] allTraits = CommanderTrait.values();
        int traitCount = 1 + random.nextInt(3);
        List<CommanderTrait> available = new ArrayList<>(List.of(allTraits));
        for (int i = 0; i < traitCount && !available.isEmpty(); i++) {
            int idx = random.nextInt(available.size());
            commander.addTrait(available.remove(idx));
        }

        commanders.put(id, commander);
        return commander;
    }

    /**
     * Retrieves a commander by ID.
     *
     * @param id the commander ID
     * @return the commander, or null if not found
     */
    public AICommander getCommander(String id) {
        return commanders.get(id);
    }

    /**
     * Returns all registered commanders.
     *
     * @return list of all commanders
     */
    public List<AICommander> getAllCommanders() {
        return new ArrayList<>(commanders.values());
    }

    /**
     * Returns all available historical profiles.
     *
     * @return list of all historical profiles
     */
    public List<AICommanderProfile> getAllProfiles() {
        return AICommanderProfile.getAllProfiles();
    }
}
