package com.yisy.modules.doramcu.starshiptroopers.integration;

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
 * Starship Troopers overlay builder that creates persistent empires and AI characters
 * for the Federated Solar System vs Arachnid conflict.
 * <p>
 * Creates the following empire structures:
 * <ul>
 *   <li><b>Federated Solar System</b> — Human military government</li>
 *   <li><b>Mobile Infantry</b> — Elite shock troops of the FSS</li>
 *   <li><b>Arachnid Hive</b> — Bug civilization threat</li>
 *   <li><b>Brain Bug Colony</b> — Intelligent bug leadership</li>
 * </ul>
 * </p>
 * <p>
 * Each empire has AI character leaders saved to the database via {@link LoreGroupEmpireService}.
 * </p>
 */
@Component
public class StarshipTroopersOverlayBuilder implements GameOverlayBuilder {

    private static final Logger log = LoggerFactory.getLogger(StarshipTroopersOverlayBuilder.class);

    private final LoreGroupEmpireService loreGroupEmpireService;

    public StarshipTroopersOverlayBuilder(LoreGroupEmpireService loreGroupEmpireService) {
        this.loreGroupEmpireService = loreGroupEmpireService;
    }

    @Override
    public GameType getGameType() {
        return GameType.STARSHIP_TROOPERS;
    }

    @Override
    public void buildOverlay(Galaxy galaxy, GalaxyGenerationConfig config) {
        log.info("Building Starship Troopers overlay for galaxy: {}", galaxy.getName());

        String galaxyName = galaxy.getName();

        // Phase 1: Human factions
        createHumanFactions(galaxyName);

        // Phase 2: Bug factions
        createBugFactions(galaxyName);

        log.info("Starship Troopers overlay complete for galaxy: {}", galaxy.getName());
    }

    private void createHumanFactions(String galaxyName) {
        log.info("Creating Starship Troopers human factions for galaxy: {}", galaxyName);

        List<LoreGroup> empires = new ArrayList<>();

        // Federated Solar System
        empires.add(new LoreGroup(
                "Federated Solar System",
                "President",
                new String[]{"President"},
                "The Federated Solar System is a human military government defending humanity against the Bug threat.",
                "Military"
        ));

        // Mobile Infantry
        empires.add(new LoreGroup(
                "Mobile Infantry",
                "General",
                new String[]{"General Riker"},
                "The Mobile Infantry is the elite shock troop branch of the FSS Defense Forces.",
                "Combat"
        ));

        loreGroupEmpireService.createEmpireLeaders(galaxyName, empires);

        // Add human NPCs
        List<LoreGroupEmpireService.NpcData> npcs = new ArrayList<>();
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Johnny Rico", "Rifleman", "Mobile Infantry",
                "A recruit who rises through the ranks of the Mobile Infantry"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Carl Jenkins", "Officer", "Mobile Infantry",
                "Rico's friend who becomes an officer in the Mobile Infantry"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Dizzy Flores", "Officer", "Mobile Infantry",
                "A tough officer who leads the Mobile Infantry into battle"
        ));
        loreGroupEmpireService.createNpcs(galaxyName, npcs);
    }

    private void createBugFactions(String galaxyName) {
        log.info("Creating Starship Troopers bug factions for galaxy: {}", galaxyName);

        List<LoreGroup> empires = new ArrayList<>();

        // Arachnid Hive
        empires.add(new LoreGroup(
                "Arachnid Hive",
                "Queen Bug",
                new String[]{"Queen Bug"},
                "The Arachnid Hive is a massive bug civilization that threatens human colonies.",
                "Swarm"
        ));

        // Brain Bug Colony
        empires.add(new LoreGroup(
                "Brain Bug Colony",
                "Brain Bug",
                new String[]{"Brain Bug"},
                "The Brain Bug is the intelligent leader of the Arachnid civilization.",
                "Intelligence"
        ));

        loreGroupEmpireService.createEmpireLeaders(galaxyName, empires);

        // Add bug NPCs
        List<LoreGroupEmpireService.NpcData> npcs = new ArrayList<>();
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Plasma Bug", "Energy Weapon", "Arachnid Hive",
                "A bug that fires plasma bolts at human forces"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Tank Bug", "Heavy Armor", "Arachnid Hive",
                "A heavily armored bug that charges through human lines"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Warrior Bug", "Infantry", "Arachnid Hive",
                "A standard bug warrior with powerful mandibles"
        ));
        loreGroupEmpireService.createNpcs(galaxyName, npcs);
    }
}
