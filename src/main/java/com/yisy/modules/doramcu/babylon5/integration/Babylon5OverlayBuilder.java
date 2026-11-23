package com.yisy.modules.doramcu.babylon5.integration;

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
 * Babylon 5 overlay builder that creates persistent empires and AI characters
 * for Babylon 5 factions.
 * <p>
 * Creates the following empire structures:
 * <ul>
 *   <li><b>Earth Alliance</b> — Human-dominated government from Earth</li>
 *   <li><b>Centauri Republic</b> — Former empire turned republic</li>
 *   <li><b>Narn Union</b> — Independent Narn worlds seeking freedom</li>
 *   <li><b>Minbari Federation</b> — Advanced alien federation</li>
 *   <li><b>Shadow Collective</b> — Ancient dark beings manipulating galactic affairs</li>
 *   <li><b>Vorlon Empire</b> — Mysterious ancient beings</li>
 * </ul>
 * </p>
 * <p>
 * Each empire has AI character leaders saved to the database via {@link LoreGroupEmpireService}.
 * </p>
 */
@Component
public class Babylon5OverlayBuilder implements GameOverlayBuilder {

    private static final Logger log = LoggerFactory.getLogger(Babylon5OverlayBuilder.class);

    private final LoreGroupEmpireService loreGroupEmpireService;

    public Babylon5OverlayBuilder(LoreGroupEmpireService loreGroupEmpireService) {
        this.loreGroupEmpireService = loreGroupEmpireService;
    }

    @Override
    public GameType getGameType() {
        return GameType.BABYLON_5;
    }

    @Override
    public void buildOverlay(Galaxy galaxy, GalaxyGenerationConfig config) {
        log.info("Building Babylon 5 overlay for galaxy: {}", galaxy.getName());

        String galaxyName = galaxy.getName();

        // Phase 1: Major faction empires
        createMajorFactions(galaxyName);

        // Phase 2: Ancient race empires
        createAncientRaces(galaxyName);

        log.info("Babylon 5 overlay complete for galaxy: {}", galaxy.getName());
    }

    private void createMajorFactions(String galaxyName) {
        log.info("Creating Babylon 5 major factions for galaxy: {}", galaxyName);

        List<LoreGroup> empires = new ArrayList<>();

        // Earth Alliance
        empires.add(new LoreGroup(
                "Earth Alliance",
                "President",
                new String[]{"Sheridan"},
                "The Earth Alliance is the human-dominated government of the Sol system and beyond.",
                "Diplomacy"
        ));

        // Centauri Republic
        empires.add(new LoreGroup(
                "Centauri Republic",
                "President",
                new String[]{"G'Kar"},
                "The Centauri Republic was once a mighty empire, now struggling to regain its former glory.",
                "Politics"
        ));

        // Narn Union
        empires.add(new LoreGroup(
                "Narn Union",
                "Council Leader",
                new String[]{"G'Kar"},
                "The Narn Union fights for independence from Centauri oppression.",
                "Combat"
        ));

        // Minbari Federation
        empires.add(new LoreGroup(
                "Minbari Federation",
                "Ambassador",
                new String[]{"Delenn"},
                "The Minbari Federation is an advanced alien civilization with a warrior monk tradition.",
                "Technology"
        ));

        loreGroupEmpireService.createEmpireLeaders(galaxyName, empires);

        // Add faction NPCs
        List<LoreGroupEmpireService.NpcData> npcs = new ArrayList<>();
        npcs.add(new LoreGroupEmpireService.NpcData(
                "John Sheridan", "Commander", "Earth Alliance",
                "Commanding officer of Babylon 5, later President of the Interstellar Alliance"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Susan Ivanova", "Executive Officer", "Earth Alliance",
                "Executive Officer of Babylon 5"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Londo Mollari", "Ambassador", "Centauri Republic",
                "Centauri Ambassador to Babylon 5"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Garibaldi", "Chief of Security", "Earth Alliance",
                "Chief of Security on Babylon 5"
        ));
        loreGroupEmpireService.createNpcs(galaxyName, npcs);
    }

    private void createAncientRaces(String galaxyName) {
        log.info("Creating Babylon 5 ancient races for galaxy: {}", galaxyName);

        List<LoreGroup> empires = new ArrayList<>();

        // Shadow Collective
        empires.add(new LoreGroup(
                "Shadow Collective",
                "Shadow",
                new String[]{"The Shadow"},
                "The Shadows are ancient beings who manipulate younger races through conflict to spur evolution.",
                "Dark Power"
        ));

        // Vorlon Empire
        empires.add(new LoreGroup(
                "Vorlon Empire",
                "Vorlon",
                new String[]{"The Vorlon"},
                "The Vorlons are mysterious ancient beings who guide younger races through order and hierarchy.",
                "Mystery"
        ));

        loreGroupEmpireService.createEmpireLeaders(galaxyName, empires);

        // Ancient race NPCs
        List<LoreGroupEmpireService.NpcData> npcs = new ArrayList<>();
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Refan", "Shadow Warrior", "Shadow Collective",
                "A Shadow warrior who tests younger races"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Lord Refan", "Vorlon Leader", "Vorlon Empire",
                "A Vorlon leader in human form"
        ));
        loreGroupEmpireService.createNpcs(galaxyName, npcs);
    }
}
