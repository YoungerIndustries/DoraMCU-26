package com.yisy.modules.doramcu.sfb.integration;

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
 * Star Frontiers overlay builder that creates persistent empires and AI characters
 * for Star Frontiers sectors and factions.
 * <p>
 * Creates the following empire structures:
 * <ul>
 *   <li><b>United Worlds</b> — Human democratic federation</li>
 *   <li><b>Kzinti Syndicate</b> — Feline warrior traders</li>
 *   <li><b>Tharanla Collective</b> — Insectoid hive civilization</li>
 *   <li><b>Shivans</b> — Reptilian warrior race</li>
 *   <li><b>Enfen Empire</b> — Ancient amphibious empire</li>
 * </ul>
 * </p>
 * <p>
 * Each empire has AI character leaders saved to the database via {@link LoreGroupEmpireService}.
 * </p>
 */
@Component
public class StarFrontiersOverlayBuilder implements GameOverlayBuilder {

    private static final Logger log = LoggerFactory.getLogger(StarFrontiersOverlayBuilder.class);

    private final LoreGroupEmpireService loreGroupEmpireService;

    public StarFrontiersOverlayBuilder(LoreGroupEmpireService loreGroupEmpireService) {
        this.loreGroupEmpireService = loreGroupEmpireService;
    }

    @Override
    public GameType getGameType() {
        return GameType.SFB;
    }

    @Override
    public void buildOverlay(Galaxy galaxy, GalaxyGenerationConfig config) {
        log.info("Building Star Frontiers overlay for galaxy: {}", galaxy.getName());

        String galaxyName = galaxy.getName();

        // Phase 1: Major faction empires
        createMajorFactions(galaxyName);

        log.info("Star Frontiers overlay complete for galaxy: {}", galaxy.getName());
    }

    private void createMajorFactions(String galaxyName) {
        log.info("Creating Star Frontiers major factions for galaxy: {}", galaxyName);

        List<LoreGroup> empires = new ArrayList<>();

        // United Worlds
        empires.add(new LoreGroup(
                "United Worlds",
                "President",
                new String[]{"President"},
                "The United Worlds is a human democratic federation exploring the Alpha Quadrant.",
                "Diplomacy"
        ));

        // Kzinti Syndicate
        empires.add(new LoreGroup(
                "Kzinti Syndicate",
                "Warlord",
                new String[]{"Kzinti Warlord"},
                "The Kzinti are feline warriors who trade, raid, and conquer across the frontier.",
                "Combat"
        ));

        // Tharanla Collective
        empires.add(new LoreGroup(
                "Tharanla Collective",
                "Hive Mind",
                new String[]{"Tharanla Prime"},
                "The Tharanla are an insectoid hive civilization with a collective consciousness.",
                "Biology"
        ));

        // Shivans
        empires.add(new LoreGroup(
                "Shivans",
                "Warlord",
                new String[]{"Shivan Warlord"},
                "The Shivans are a reptilian warrior race with a fierce expansionist culture.",
                "Military"
        ));

        // Enfen Empire
        empires.add(new LoreGroup(
                "Enfen Empire",
                "Emperor",
                new String[]{"Enfen Emperor"},
                "The Enfen are an ancient amphibious empire with vast technological knowledge.",
                "Technology"
        ));

        loreGroupEmpireService.createEmpireLeaders(galaxyName, empires);

        // Add faction NPCs
        List<LoreGroupEmpireService.NpcData> npcs = new ArrayList<>();
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Captain Blake", "Explorer", "United Worlds",
                "A United Worlds explorer on the frontier"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Kzin Commander", "Warrior", "Kzinti Syndicate",
                "A fierce Kzinti warrior commander"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Shivan Champion", "Warrior", "Shivans",
                "A Shivan champion of the warrior caste"
        ));
        loreGroupEmpireService.createNpcs(galaxyName, npcs);
    }
}
