package com.yisy.modules.doramcu.captainslog.integration;

import com.yisy.modules.doramcu.gamecore.GameOverlayBuilder;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;
import com.yisy.modules.doramcu.service.game.LoreGroupEmpireService;
import com.yisy.modules.doramcu.service.game.LoreGroupEmpireService.LoreGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Captain's Log overlay builder that creates persistent empires and AI characters
 * for Star Trek mission-based solo RPG.
 * <p>
 * Creates the following empire structures:
 * <ul>
 *   <li><b>Starfleet Command</b> — Federation military and exploration force</li>
 *   <li><b>Starfleet Academy</b> — Training ground for future officers</li>
 *   <li><b>Section 31</b> — Covert black ops division</li>
 *   <li><b>Denali Station</b> — Deep space research outpost</li>
 * </ul>
 * </p>
 * <p>
 * Each empire has AI character leaders saved to the database via {@link LoreGroupEmpireService}.
 * </p>
 */
@Component
public class CaptainsLogOverlayBuilder implements GameOverlayBuilder {

    private static final Logger log = LoggerFactory.getLogger(CaptainsLogOverlayBuilder.class);

    private final LoreGroupEmpireService loreGroupEmpireService;

    public CaptainsLogOverlayBuilder(LoreGroupEmpireService loreGroupEmpireService) {
        this.loreGroupEmpireService = loreGroupEmpireService;
    }

    @Override
    public GameType getGameType() {
        return GameType.CAPTAINS_LOG;
    }

    @Override
    public void buildOverlay(Galaxy galaxy, GalaxyGenerationConfig config) {
        log.info("Building Captain's Log overlay for galaxy: {}", galaxy.getName());

        String galaxyName = galaxy.getName();

        // Phase 1: Starfleet organizations
        createStarfleetOrganizations(galaxyName);

        log.info("Captain's Log overlay complete for galaxy: {}", galaxy.getName());
    }

    private void createStarfleetOrganizations(String galaxyName) {
        log.info("Creating Captain's Log Starfleet organizations for galaxy: {}", galaxyName);

        List<LoreGroup> empires = new ArrayList<>();

        // Starfleet Command
        empires.add(new LoreGroup(
                "Starfleet Command",
                "Fleet Admiral",
                new String[]{"Fleet Admiral"},
                "Starfleet Command oversees all Federation exploration, diplomatic, and military operations.",
                "Leadership"
        ));

        // Starfleet Academy
        empires.add(new LoreGroup(
                "Starfleet Academy",
                "Commandant",
                new String[]{"Commandant"},
                "Starfleet Academy trains the next generation of Starfleet officers on Earth.",
                "Education"
        ));

        // Section 31
        empires.add(new LoreGroup(
                "Section 31",
                "Director",
                new String[]{"Director"},
                "Section 31 is a covert black ops division that operates outside normal Starfleet protocols.",
                "Espionage"
        ));

        // Denali Station
        empires.add(new LoreGroup(
                "Denali Station",
                "Station Commander",
                new String[]{"Station Commander"},
                "Denali Station is a deep space research outpost studying the Babel Array.",
                "Science"
        ));

        loreGroupEmpireService.createEmpireLeaders(galaxyName, empires);

        // Add Starfleet NPCs
        List<LoreGroupEmpireService.NpcData> npcs = new ArrayList<>();
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Captain", "Ship Commander", "Starfleet Command",
                "The player's character, a Starfleet captain on a solo mission"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Number One", "First Officer", "Starfleet Command",
                "The captain's trusted first officer"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Dr. Crusher", "Chief Medical Officer", "Starfleet Command",
                "The ship's chief medical officer"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Lt. Commander Data", "Android Officer", "Starfleet Command",
                "An android seeking to understand humanity"
        ));
        loreGroupEmpireService.createNpcs(galaxyName, npcs);
    }
}
