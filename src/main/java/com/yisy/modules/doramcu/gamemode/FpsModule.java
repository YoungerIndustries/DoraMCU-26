package com.yisy.modules.doramcu.gamemode;

import com.yisy.modules.doramcu.fps.FpsGameServerWrapper;
import com.yisy.modules.doramcu.gamecore.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FPS (First-Person Shooter) game mode module: real-time combat with
 * weapon management, map-based gameplay, and player-vs-player action.
 * <p>
 * Provides FPS session management, match tracking, weapon loadouts,
 * and combat statistics for FPS-style gameplay integrated with the
 * existing FpsGameServer infrastructure.
 * </p>
 */
@Component
public class FpsModule implements GameModeModule {

    private static final Logger log = LoggerFactory.getLogger(FpsModule.class);

    // Galaxy name -> FPS session state
    private final Map<String, FpsSessionState> sessions = new ConcurrentHashMap<>();

    // Galaxy name -> match history
    private final Map<String, List<MatchRecord>> matchHistory = new ConcurrentHashMap<>();

    // Weapon loadouts
    private static final List<String> WEAPON_LOADOUTS = Arrays.asList(
            "Assault Rifle", "Sniper Rifle", "Shotgun", "SMG",
            "Machine Gun", "Rocket Launcher", "Pistol", "Melee"
    );

    // Map types
    private static final List<String> MAP_TYPES = Arrays.asList(
            "Urban Combat", "Corporate HQ", "Military Base", "Space Station",
            "Alien Planet", "Underground Bunker", "Skyscraper", "Desert Outpost"
    );

    private final FpsGameServerWrapper fpsGameServer;

    public FpsModule(FpsGameServerWrapper fpsGameServer) {
        this.fpsGameServer = fpsGameServer;
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.FPS;
    }

    @Override
    public String getModuleName() {
        return "FPS Module";
    }

    @Override
    public String getDescription() {
        return "First-person shooter with real-time combat and tactical movement";
    }

    @Override
    public void initializeForGalaxy(String galaxyName, String loreSlug) {
        log.info("Initializing FPS module for galaxy: {} with lore: {}", galaxyName, loreSlug);
        sessions.put(galaxyName, new FpsSessionState());
        matchHistory.put(galaxyName, new ArrayList<>());

        // Initialize FPS game server for this galaxy
        FpsSessionState session = sessions.get(galaxyName);
        session.active = true;
        session.mapCount = MAP_TYPES.size();
        session.weaponCount = WEAPON_LOADOUTS.size();

        log.info("FPS module initialized for galaxy: {} with {} maps and {} weapons",
                galaxyName, session.mapCount, session.weaponCount);
    }

    @Override
    public String processTurn(String galaxyName, int turnNumber, String playerId) {
        FpsSessionState session = sessions.getOrDefault(galaxyName, new FpsSessionState());
        List<MatchRecord> matches = matchHistory.getOrDefault(galaxyName, Collections.emptyList());

        StringBuilder summary = new StringBuilder();
        summary.append("FPS Turn ").append(turnNumber).append(" in ").append(galaxyName).append(":\n");

        // Simulate a match
        Random random = new Random();
        String map = MAP_TYPES.get(random.nextInt(MAP_TYPES.size()));
        String weapon = WEAPON_LOADOUTS.get(random.nextInt(WEAPON_LOADOUTS.size()));

        // Generate match results
        int kills = random.nextInt(10);
        int deaths = random.nextInt(8);
        int assists = random.nextInt(6);
        boolean won = kills > deaths || random.nextDouble() < 0.5;

        MatchRecord match = new MatchRecord(map, weapon, kills, deaths, assists, won);
        matches.add(match);

        // Update session stats
        session.totalMatches++;
        if (won) {
            session.totalWins++;
        }
        session.totalKills += kills;
        session.totalDeaths += deaths;

        summary.append("  ðŸ”« Match on ").append(map).append(" with ").append(weapon).append("\n");
        summary.append("  K/D/A: ").append(kills).append("/").append(deaths).append("/").append(assists).append("\n");
        summary.append("  Result: ").append(won ? "Victory âœ“" : "Defeat âœ—").append("\n");

        // Win/loss ratio
        double kdRatio = session.totalDeaths > 0 ?
                (double) session.totalKills / session.totalDeaths : session.totalKills;
        summary.append("  Overall: ").append(session.totalWins).append("W - ")
               .append(session.totalMatches - session.totalWins).append("L (")
               .append(session.totalMatches).append(" matches)")
               .append(" | K/D: ").append(String.format("%.2f", kdRatio));

        return summary.toString();
    }

    @Override
    public String getStateSummary(String galaxyName) {
        FpsSessionState session = sessions.getOrDefault(galaxyName, new FpsSessionState());
        double kdRatio = session.totalDeaths > 0 ?
                (double) session.totalKills / session.totalDeaths : session.totalKills;
        return String.format("FPS: %d matches, %dW-%dL, K/D: %.2f",
                session.totalMatches, session.totalWins,
                session.totalMatches - session.totalWins, kdRatio);
    }

    @Override
    public boolean isActiveForGalaxy(String galaxyName) {
        FpsSessionState session = sessions.get(galaxyName);
        return session != null && session.active;
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String serializeState(String galaxyName) {
        try {
            FpsSessionState session = sessions.get(galaxyName);
            if (session == null) return null;
            return objectMapper.writeValueAsString(session);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize FPS state for {}: {}", galaxyName, e.getMessage());
            return null;
        }
    }

    @Override
    public void restoreState(String galaxyName, String stateJson) {
        if (stateJson == null || stateJson.isEmpty()) return;
        try {
            FpsSessionState session = objectMapper.readValue(stateJson, FpsSessionState.class);
            sessions.put(galaxyName, session);
            log.info("Restored FPS state for {}: {} matches, {} kills", galaxyName, session.totalMatches, session.totalKills);
        } catch (Exception e) {
            log.error("Failed to restore FPS state for {}: {}", galaxyName, e.getMessage(), e);
        }
    }

    /**
     * FPS session state for a galaxy.
     */
    public static class FpsSessionState {
        boolean active = false;
        int totalMatches = 0;
        int totalWins = 0;
        int totalKills = 0;
        int totalDeaths = 0;
        int mapCount = 0;
        int weaponCount = 0;
    }

    /**
     * Record of a single FPS match.
     */
    public static class MatchRecord {
        private final String map;
        private final String weapon;
        private final int kills;
        private final int deaths;
        private final int assists;
        private final boolean won;

        public MatchRecord(String map, String weapon, int kills, int deaths, int assists, boolean won) {
            this.map = map;
            this.weapon = weapon;
            this.kills = kills;
            this.deaths = deaths;
            this.assists = assists;
            this.won = won;
        }

        public String getMap() { return map; }
        public String getWeapon() { return weapon; }
        public int getKills() { return kills; }
        public int getDeaths() { return deaths; }
        public int getAssists() { return assists; }
        public boolean isWon() { return won; }
    }
}
