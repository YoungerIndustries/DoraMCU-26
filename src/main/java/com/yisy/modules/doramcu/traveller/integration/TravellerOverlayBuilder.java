package com.yisy.modules.doramcu.traveller.integration;

import com.yisy.modules.doramcu.gamecore.GameOverlayBuilder;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;
import com.yisy.modules.doramcu.service.LoreGroupEmpireService;
import com.yisy.modules.doramcu.service.LoreGroupEmpireService.LoreGroup;
import com.yisy.modules.doramcu.traveller.service.TravellerMonsterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Traveller overlay builder that creates persistent empires and AI characters
 * for Traveller races.
 * <p>
 * Creates the following empire structures:
 * <ul>
 *   <li><b>Aslan Empire</b> — Feline humanoids from the Aslan sector</li>
 *   <li><b>Vargr Clans</b> — Wolf-like humanoids with a clan-based society</li>
 *   <li><b>Mellanni Federation</b> — Insectoid humanoids with a hive mind</li>
 *   <li><b>Loxen Empire</b> — Reptilian humanoids with a militaristic culture</li>
 *   <li><b>Hiigaran Republic</b> — Avian humanoids with a democratic government</li>
 *   <li><b>Insectoid Hive</b> — Insectoid species with a hive-based society</li>
 *   <li><b>Human Imperium</b> — Human-dominated empire with a feudal structure</li>
 * </ul>
 * </p>
 * <p>
 * Each empire has AI character leaders saved to the database via {@link LoreGroupEmpireService}.
 * </p>
 */
@Component
public class TravellerOverlayBuilder implements GameOverlayBuilder {

    private static final Logger log = LoggerFactory.getLogger(TravellerOverlayBuilder.class);

    private final TravellerMonsterService monsterService;
    private final LoreGroupEmpireService loreGroupEmpireService;

    public TravellerOverlayBuilder(TravellerMonsterService monsterService,
                                   LoreGroupEmpireService loreGroupEmpireService) {
        this.monsterService = monsterService;
        this.loreGroupEmpireService = loreGroupEmpireService;
    }

    @Override
    public GameType getGameType() {
        return GameType.TRAVELLER;
    }

    @Override
    public void buildOverlay(Galaxy galaxy, GalaxyGenerationConfig config) {
        log.info("Building Traveller overlay for galaxy: {}", galaxy.getName());

        String galaxyName = galaxy.getName();

        // Phase 1: Race-based empires
        createRaceEmpires(galaxyName);

        // Phase 2: Lore from monsters
        addLoreFromMonsters(galaxyName);

        log.info("Traveller overlay complete for galaxy: {}", galaxy.getName());
    }

    private void createRaceEmpires(String galaxyName) {
        log.info("Creating Traveller race empires for galaxy: {}", galaxyName);

        List<LoreGroup> empires = new ArrayList<>();

        // Aslan Empire
        empires.add(new LoreGroup(
                "Aslan Empire",
                "Emperor",
                new String[]{"Aslan Emperor"},
                "The Aslan Empire is a feline humanoid civilization with a feudal structure.",
                "Diplomacy"
        ));

        // Vargr Clans
        empires.add(new LoreGroup(
                "Vargr Clans",
                "Clan Leader",
                new String[]{"Vargr Alpha"},
                "The Vargr are wolf-like humanoids organized into clans with a warrior culture.",
                "Combat"
        ));

        // Mellanni Federation
        empires.add(new LoreGroup(
                "Mellanni Federation",
                "Hive Mind",
                new String[]{"Mellanni Prime"},
                "The Mellanni are insectoid humanoids with a collective consciousness.",
                "Technology"
        ));

        // Loxen Empire
        empires.add(new LoreGroup(
                "Loxen Empire",
                "Warlord",
                new String[]{"Loxen Warlord"},
                "The Loxen are reptilian humanoids with a militaristic expansionist culture.",
                "Military"
        ));

        // Hiigaran Republic
        empires.add(new LoreGroup(
                "Hiigaran Republic",
                "President",
                new String[]{"Hiigaran President"},
                "The Hiigarans are avian humanoids with a democratic government.",
                "Diplomacy"
        ));

        // Insectoid Hive
        empires.add(new LoreGroup(
                "Insectoid Hive",
                "Queen",
                new String[]{"Hive Queen"},
                "The Insectoid Hive is a collective of insectoid species under a single queen.",
                "Biology"
        ));

        // Human Imperium
        empires.add(new LoreGroup(
                "Human Imperium",
                "Emperor",
                new String[]{"Human Emperor"},
                "The Human Imperium is a human-dominated empire with a feudal structure.",
                "Leadership"
        ));

        loreGroupEmpireService.createEmpireLeaders(galaxyName, empires);

        // Add race NPCs
        List<LoreGroupEmpireService.NpcData> npcs = new ArrayList<>();
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Aslan Noble", "Noble", "Aslan Empire",
                "A noble Aslan warrior"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Vargr Shaman", "Shaman", "Vargr Clans",
                "A Vargr spiritual leader"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Loxen Commander", "Commander", "Loxen Empire",
                "A Loxen military commander"
        ));
        loreGroupEmpireService.createNpcs(galaxyName, npcs);
    }

    private void addLoreFromMonsters(String galaxyName) {
        List<?> monsters = monsterService.listMonsters();
        log.info("  Traveller monsters available for lore: {}", monsters.size());
    }
}
