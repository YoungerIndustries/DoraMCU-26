package com.yisy.modules.doramcu.gamemode;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.gamecore.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MMORPG game mode module: persistent multiplayer world with guilds,
 * shared economy, social features, player rankings, and world events.
 * <p>
 * Provides guild management, world boss events, player trading,
 * achievement system, and social interactions for MMORPG-style gameplay.
 * </p>
 */
@Component
public class MmORPGModule implements GameModeModule {

    private static final Logger log = LoggerFactory.getLogger(MmORPGModule.class);

    // Galaxy name -> MMORPG state
    private final Map<String, MMORPGState> states = new ConcurrentHashMap<>();

    // Guild name templates
    private static final List<String> GUILD_NAMES = Arrays.asList(
            "Star Vanguard", "Cosmic Guardians", "Void Walkers", "Nebula Knights",
            "Iron Fleet", "Phoenix Rising", "Shadow Syndicate", "Dragon's Claw",
            "Celestial Order", "Thunder Legion", "Mystic Circle", "Steel Wolves"
    );

    // World event types
    private static final List<String> WORLD_EVENTS = Arrays.asList(
            "World Boss: Ancient Dragon appears",
            "World Boss: Alien Mothership invasion",
            "Territory War: Factions clash for control",
            "Rare Spawn: Legendary creature detected",
            "Trade Caravan: Mobile market arrives",
            "Festival: Galactic celebration begins",
            "Raid Dungeon: New instance unlocked",
            "PvP Tournament: Arena championship"
    );

    // Achievement categories
    private static final List<String> ACHIEVEMENTS = Arrays.asList(
            "First Blood â€” Defeat your first enemy",
            "Guild Founder â€” Create a new guild",
            "Wealthy â€” Accumulate 10000 credits",
            "Explorer â€” Visit 50 different systems",
            "Master Trader â€” Complete 100 trades",
            "Hero â€” Save a planet from destruction",
            "Legendary â€” Reach maximum level",
            "Diplomat â€” Broker peace between factions",
            "Collector â€” Obtain all rare items",
            "Champion â€” Win a PvP tournament"
    );

    // Social interaction types
    private static final List<String> SOCIAL_EVENTS = Arrays.asList(
            "Players formed an alliance",
            "A trade agreement was signed",
            "Guild recruitment surge detected",
            "Player wedding ceremony held",
            "Community building project completed",
            "Inter-guild competition announced",
            "Player marketplace boom",
            "World chat trending topic emerges"
    );

    // MMORPG event templates by lore category
    private static final Map<String, List<String>> EVENT_TEMPLATES = new HashMap<>();

    static {
        EVENT_TEMPLATES.put("sci_fi", Arrays.asList(
                "Fleet battle erupts in the outer rim",
                "Corporate espionage scandal rocks the galaxy",
                "New hyperspace route discovered by players",
                "AI uprising threat detected in sector 7",
                "Diplomatic summit between player factions"
        ));
        EVENT_TEMPLATES.put("fantasy", Arrays.asList(
                "Dragon migration threatens nearby settlements",
                "Ancient magic surge empowers all players",
                "Dark lord's forces mass at the border",
                "Enchanted treasure vault discovered",
                "Elven council calls for player representatives"
        ));
        EVENT_TEMPLATES.put("horror", Arrays.asList(
                "Alien infestation spreads across the station",
                "Players report strange signals from deep space",
                "Survival mode activated in sector 12",
                "Mysterious disappearances increase",
                "Emergency evacuation drill announced"
        ));
        EVENT_TEMPLATES.put("post_apocalyptic", Arrays.asList(
                "Raider gang attacks player settlements",
                "Clean water source discovered by explorers",
                "Pre-war vault opened for looting",
                "Mutant horde approaches the walls",
                "Trading post established in the wasteland"
        ));
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.MMORPG;
    }

    @Override
    public String getModuleName() {
        return "MMORPG Module";
    }

    @Override
    public String getDescription() {
        return "Persistent multiplayer world with guilds, shared economy, and world events";
    }

    @Override
    public void initializeForGalaxy(String galaxyName, String loreSlug) {
        log.info("Initializing MMORPG module for galaxy: {} with lore: {}", galaxyName, loreSlug);
        MMORPGState state = new MMORPGState();
        state.active = true;
        state.loreCategory = determineLoreCategory(loreSlug);
        state.economyCredits = 100000;
        state.economyInflation = 0.0;

        // Create initial guilds
        Random random = new Random();
        int initialGuilds = 2 + random.nextInt(3);
        for (int i = 0; i < initialGuilds; i++) {
            String guildName = GUILD_NAMES.get(random.nextInt(GUILD_NAMES.size()));
            state.guilds.add(guildName);
            state.guildActivity.put(guildName, 0);
        }

        states.put(galaxyName, state);
        log.info("MMORPG module initialized for galaxy: {} with {} guilds", galaxyName, state.guilds.size());
    }

    @Override
    public String processTurn(String galaxyName, int turnNumber, String playerId) {
        MMORPGState state = states.getOrDefault(galaxyName, new MMORPGState());

        StringBuilder summary = new StringBuilder();
        summary.append("MMORPG Turn ").append(turnNumber).append(" in ").append(galaxyName).append(":\n");

        Random random = new Random();

        // World event progression
        if (random.nextDouble() < 0.4) {
            String event = WORLD_EVENTS.get(random.nextInt(WORLD_EVENTS.size()));
            state.activeEvents++;
            state.totalEvents++;
            summary.append("  ðŸŒ World Event: ").append(event).append("\n");
        }

        // Guild activity
        if (!state.guilds.isEmpty()) {
            String activeGuild = state.guilds.get(random.nextInt(state.guilds.size()));
            int activity = 1 + random.nextInt(5);
            state.guildActivity.merge(activeGuild, activity, Integer::sum);
            summary.append("  âš”ï¸ Guild ").append(activeGuild).append(" activity: ").append(activity).append(" actions\n");

            // New guild formation
            if (random.nextDouble() < 0.15 && state.guilds.size() < GUILD_NAMES.size()) {
                String newGuild = GUILD_NAMES.get(random.nextInt(GUILD_NAMES.size()));
                if (!state.guilds.contains(newGuild)) {
                    state.guilds.add(newGuild);
                    state.guildActivity.put(newGuild, 0);
                    summary.append("  ðŸ° New guild formed: ").append(newGuild).append("\n");
                }
            }
        }

        // Economy fluctuations
        int tradeVolume = 10 + random.nextInt(50);
        state.totalTrades += tradeVolume;
        double priceChange = (random.nextDouble() - 0.4) * 5.0;
        state.economyInflation += priceChange / 100.0;
        state.economyCredits += tradeVolume * 100;

        summary.append("  ðŸ’° Trades: ").append(tradeVolume).append(", Economy: ")
               .append(String.format("%,.0f", (double) state.economyCredits)).append(" credits")
               .append(" (inflation: ").append(String.format("%.1f", state.economyInflation * 100)).append("%)\n");

        // Achievement unlocks
        if (random.nextDouble() < 0.25) {
            String achievement = ACHIEVEMENTS.get(random.nextInt(ACHIEVEMENTS.size()));
            state.achievementsUnlocked++;
            summary.append("  ðŸ† Achievement: ").append(achievement).append("\n");
        }

        // Social interactions
        if (random.nextDouble() < 0.3) {
            String social = SOCIAL_EVENTS.get(random.nextInt(SOCIAL_EVENTS.size()));
            state.socialInteractions++;
            summary.append("  ðŸ¤ Social: ").append(social).append("\n");
        }

        // Lore-specific events
        if (random.nextDouble() < 0.2) {
            List<String> events = EVENT_TEMPLATES.getOrDefault(state.loreCategory, EVENT_TEMPLATES.get("sci_fi"));
            String event = events.get(random.nextInt(events.size()));
            summary.append("  âœ¦ Lore Event: ").append(event).append("\n");
        }

        // Player ranking update
        if (random.nextDouble() < 0.2) {
            int rankChange = random.nextInt(10) - 3;
            state.playerRankingShift += rankChange;
            summary.append("  ðŸ“Š Player ranking shift: ").append(rankChange >= 0 ? "+" : "").append(rankChange);
        }

        return summary.toString();
    }

    @Override
    public String getStateSummary(String galaxyName) {
        MMORPGState state = states.getOrDefault(galaxyName, new MMORPGState());
        return String.format("MMORPG: %d guilds, %d events, %d trades, %d achievements",
                state.guilds.size(), state.totalEvents, state.totalTrades, state.achievementsUnlocked);
    }

    @Override
    public boolean isActiveForGalaxy(String galaxyName) {
        MMORPGState state = states.get(galaxyName);
        return state != null && state.active;
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String serializeState(String galaxyName) {
        try {
            MMORPGState state = states.get(galaxyName);
            if (state == null) return null;
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize MMORPG state for {}: {}", galaxyName, e.getMessage());
            return null;
        }
    }

    @Override
    public void restoreState(String galaxyName, String stateJson) {
        if (stateJson == null || stateJson.isEmpty()) return;
        try {
            MMORPGState state = objectMapper.readValue(stateJson, MMORPGState.class);
            states.put(galaxyName, state);
            log.info("Restored MMORPG state for {}: {} guilds, {} events", galaxyName, state.guilds.size(), state.totalEvents);
        } catch (Exception e) {
            log.error("Failed to restore MMORPG state for {}: {}", galaxyName, e.getMessage(), e);
        }
    }

    private String determineLoreCategory(String loreSlug) {
        if (loreSlug == null) return "sci_fi";
        loreSlug = loreSlug.toLowerCase();
        if (loreSlug.contains("dune") || loreSlug.contains("star") || loreSlug.contains("halo")
                || loreSlug.contains("expanse") || loreSlug.contains("mass") || loreSlug.contains("babylon")
                || loreSlug.contains("firefly") || loreSlug.contains("stargate") || loreSlug.contains("red dwarf")
                || loreSlug.contains("warhammer") || loreSlug.contains("alien") || loreSlug.contains("dead space")
                || loreSlug.contains("avatar") || loreSlug.contains("transformers") || loreSlug.contains("destiny")
                || loreSlug.contains("earth")) {
            return "sci_fi";
        }
        if (loreSlug.contains("dnd") || loreSlug.contains("forgotten") || loreSlug.contains("lotr")
                || loreSlug.contains("warcraft") || loreSlug.contains("elder") || loreSlug.contains("warhammer fantasy")
                || loreSlug.contains("pokemon") || loreSlug.contains("minecraft") || loreSlug.contains("genshin")) {
            return "fantasy";
        }
        if (loreSlug.contains("alien") || loreSlug.contains("dead space")) {
            return "horror";
        }
        if (loreSlug.contains("fallout")) {
            return "post_apocalyptic";
        }
        return "sci_fi";
    }

    /**
     * MMORPG state for a galaxy.
     */
    public static class MMORPGState {
        boolean active = false;
        List<String> guilds = new ArrayList<>();
        Map<String, Integer> guildActivity = new HashMap<>();
        int activeEvents = 0;
        int totalEvents = 0;
        int totalTrades = 0;
        int achievementsUnlocked = 0;
        int socialInteractions = 0;
        long economyCredits = 0;
        double economyInflation = 0.0;
        int playerRankingShift = 0;
        String loreCategory = "sci_fi";
    }
}
