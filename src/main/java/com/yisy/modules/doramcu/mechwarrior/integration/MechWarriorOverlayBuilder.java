package com.yisy.modules.doramcu.mechwarrior.integration;

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
 * MechWarrior overlay builder that creates persistent empires and AI characters
 * for BattleTech factions.
 * <p>
 * Creates the following empire structures:
 * <ul>
 *   <li><b>Sunrise Federation</b> — Human federation of the Inner Sphere</li>
 *   <li><b>Clan Alpha</b> — Elite warrior clans</li>
 *   <li><b>Clan Blood Spirit</b> — Fanatical warrior clan</li>
 *   <li><b>Clan Ghost Bear</b> — Honorable warrior clan</li>
 *   <li><b>ComStar</b> — Religious military order controlling communication</li>
 * </ul>
 * </p>
 * <p>
 * Each empire has AI character leaders saved to the database via {@link LoreGroupEmpireService}.
 * </p>
 */
@Component
public class MechWarriorOverlayBuilder implements GameOverlayBuilder {

    private static final Logger log = LoggerFactory.getLogger(MechWarriorOverlayBuilder.class);

    private final LoreGroupEmpireService loreGroupEmpireService;

    public MechWarriorOverlayBuilder(LoreGroupEmpireService loreGroupEmpireService) {
        this.loreGroupEmpireService = loreGroupEmpireService;
    }

    @Override
    public GameType getGameType() {
        return GameType.MECHWARRIOR;
    }

    @Override
    public void buildOverlay(Galaxy galaxy, GalaxyGenerationConfig config) {
        log.info("Building MechWarrior overlay for galaxy: {}", galaxy.getName());

        String galaxyName = galaxy.getName();

        // Phase 1: Inner Sphere factions
        createInnerSphereFactions(galaxyName);

        // Phase 2: Clan factions
        createClanFactions(galaxyName);

        log.info("MechWarrior overlay complete for galaxy: {}", galaxy.getName());
    }

    private void createInnerSphereFactions(String galaxyName) {
        log.info("Creating MechWarrior Inner Sphere factions for galaxy: {}", galaxyName);

        List<LoreGroup> empires = new ArrayList<>();

        // Sunrise Federation
        empires.add(new LoreGroup(
                "Sunrise Federation",
                "President",
                new String[]{"President"},
                "The Sunrise Federation is a human federation rebuilding the Inner Sphere after the Clan invasions.",
                "Diplomacy"
        ));

        // ComStar
        empires.add(new LoreGroup(
                "ComStar",
                "Capellan",
                new String[]{"Capellan"},
                "ComStar controls the communication network of the Inner Sphere and maintains a powerful military.",
                "Technology"
        ));

        loreGroupEmpireService.createEmpireLeaders(galaxyName, empires);

        // Add Inner Sphere NPCs
        List<LoreGroupEmpireService.NpcData> npcs = new ArrayList<>();
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Johannes", "MechWarrior Captain", "Sunrise Federation",
                "A veteran MechWarrior captain of the Federated Suns"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Cardinal", "ComStar Leader", "ComStar",
                "The spiritual leader of ComStar"
        ));
        loreGroupEmpireService.createNpcs(galaxyName, npcs);
    }

    private void createClanFactions(String galaxyName) {
        log.info("Creating MechWarrior Clan factions for galaxy: {}", galaxyName);

        List<LoreGroup> empires = new ArrayList<>();

        // Clan Alpha
        empires.add(new LoreGroup(
                "Clan Alpha",
                "Khan",
                new String[]{"Khan"},
                "Clan Alpha is an elite warrior clan that values strength and honor above all.",
                "Combat"
        ));

        // Clan Blood Spirit
        empires.add(new LoreGroup(
                "Clan Blood Spirit",
                "Khan",
                new String[]{"Khan"},
                "Clan Blood Spirit is a fanatical warrior clan that believes in the supremacy of the warrior spirit.",
                "Military"
        ));

        // Clan Ghost Bear
        empires.add(new LoreGroup(
                "Clan Ghost Bear",
                "Khan",
                new String[]{"Khan"},
                "Clan Ghost Bear is a honorable warrior clan that values tradition and discipline.",
                "Honor"
        ));

        loreGroupEmpireService.createEmpireLeaders(galaxyName, empires);

        // Add Clan NPCs
        List<LoreGroupEmpireService.NpcData> npcs = new ArrayList<>();
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Alexander", "Clan Khan", "Clan Alpha",
                "The Khan of Clan Alpha, a legendary MechWarrior"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Yuri", "Clan Ghost Bear Khan", "Clan Ghost Bear",
                "The Khan of Clan Ghost Bear, known for his honor"
        ));
        loreGroupEmpireService.createNpcs(galaxyName, npcs);
    }
}
