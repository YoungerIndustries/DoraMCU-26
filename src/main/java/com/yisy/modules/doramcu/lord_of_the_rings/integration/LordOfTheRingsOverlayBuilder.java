package com.yisy.modules.doramcu.lord_of_the_rings.integration;

import com.yisy.modules.doramcu.gamecore.GameOverlayBuilder;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;
import com.yisy.modules.doramcu.service.game.LoreGroupEmpireService;
import com.yisy.modules.doramcu.lord_of_the_rings.model.Faction;
import com.yisy.modules.doramcu.lord_of_the_rings.model.Lore;
import com.yisy.modules.doramcu.lord_of_the_rings.service.LordOfTheRingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Lord of the Rings overlay builder that creates persistent empires and AI characters
 * for LOTR factions.
 */
@Component
public class LordOfTheRingsOverlayBuilder implements GameOverlayBuilder {

    private static final Logger log = LoggerFactory.getLogger(LordOfTheRingsOverlayBuilder.class);

    private final LordOfTheRingsService lotrService;
    private final LoreGroupEmpireService loreGroupEmpireService;

    public LordOfTheRingsOverlayBuilder(LordOfTheRingsService lotrService,
                                        LoreGroupEmpireService loreGroupEmpireService) {
        this.lotrService = lotrService;
        this.loreGroupEmpireService = loreGroupEmpireService;
    }

    @Override
    public GameType getGameType() {
        return GameType.LORD_OF_THE_RINGS;
    }

    @Override
    public void buildOverlay(Galaxy galaxy, GalaxyGenerationConfig config) {
        log.info("Building Lord of the Rings overlay for galaxy: {}", galaxy.getName());

        String galaxyName = galaxy.getName();

        // Phase 1: Faction-based empires from service data
        addFactionEmpires(galaxyName);

        // Phase 2: Hardcoded major LOTR empires
        createMajorEmpires(galaxyName);

        // Phase 3: Lore systems
        addLoreSystems(galaxyName);

        log.info("Lord of the Rings overlay complete for galaxy: {}", galaxy.getName());
    }

    private void addFactionEmpires(String galaxyName) {
        List<Faction> factions = lotrService.getFactions();
        int count = 0;

        for (Faction faction : factions) {
            log.info("  LOTR faction empire: {} ({}, ideology: {})",
                    faction.name(), faction.era(), faction.ideology());
            count++;
        }
        log.info("  Total LOTR faction empires from service: {}", count);
    }

    private void createMajorEmpires(String galaxyName) {
        log.info("Creating LOTR major empires for galaxy: {}", galaxyName);

        List<LoreGroup> empires = new ArrayList<>();

        // Free Peoples Alliance
        empires.add(new LoreGroup(
                "Free Peoples Alliance",
                "King",
                new String[]{"Aragorn"},
                "The Free Peoples of Middle-earth unite against the forces of Sauron.",
                "Light"
        ));

        // Gondor
        empires.add(new LoreGroup(
                "Gondor",
                "Steward",
                new String[]{"Denethor II"},
                "The White Tree of Gondor stands as a beacon of hope in the West.",
                "Light"
        ));

        // Rohan
        empires.add(new LoreGroup(
                "Rohan",
                "King",
                new String[]{"Theoden"},
                "The horse-lords of Rohan ride to defend their allies.",
                "Light"
        ));

        // Rivendell
        empires.add(new LoreGroup(
                "Rivendell",
                "Lord",
                new String[]{"Elrond"},
                "The Last Homely House east of the Sea, refuge of the Elves.",
                "Light"
        ));

        // Lothlórien
        empires.add(new LoreGroup(
                "Lothlórien",
                "Lady",
                new String[]{"Galadriel"},
                "The Golden Wood, realm of the Elves under the light of the Two Trees.",
                "Light"
        ));

        // Mordor
        empires.add(new LoreGroup(
                "Mordor",
                "Dark Lord",
                new String[]{"Sauron"},
                "The land of shadow where the Dark Lord seeks to dominate Middle-earth.",
                "Dark"
        ));

        // Isengard
        empires.add(new LoreGroup(
                "Isengard",
                "Lord",
                new String[]{"Saruman"},
                "The fortress of the White Tower, corrupted by the desire for power.",
                "Dark"
        ));

        loreGroupEmpireService.createEmpireLeaders(galaxyName, empires);

        // Add faction NPCs
        List<LoreGroupEmpireService.NpcData> npcs = new ArrayList<>();

        // Free Peoples NPCs
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Gandalf", "Wizard", "Free Peoples Alliance",
                "The Grey Pilgrim, servant of the Secret Fire"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Frodo Baggins", "Ring-bearer", "Free Peoples Alliance",
                "The Hobbit who bears the One Ring to Mount Doom"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Legolas", "Elven Prince", "Free Peoples Alliance",
                "Prince of Mirkwood, master archer"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Gimli", "Dwarven Lord", "Free Peoples Alliance",
                "Son of Glóin, warrior of the Lonely Mountain"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Éowyn", "Shieldmaiden", "Rohan",
                "Lady of Rohan who slays the Witch-king"
        ));

        // Mordor NPCs
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Witch-king of Angmar", "Nazgûl", "Mordor",
                "Lord of the Nine Rings, captain of the Ringwraiths"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Shelob", "Spider", "Mordor",
                "Ancient spider dwelling in Cirith Ungol"
        ));

        // Isengard NPCs
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Gríma Wormtongue", "Advisor", "Isengard",
                "Corrupted advisor to King Théoden"
        ));

        loreGroupEmpireService.createNpcs(galaxyName, npcs);
    }

    private void addLoreSystems(String galaxyName) {
        List<Lore> loreEntries = lotrService.getLore();
        int count = 0;

        for (Lore lore : loreEntries) {
            if (lore.notableLocations() != null) {
                for (String location : lore.notableLocations()) {
                    log.info("  LOTR lore system: {} (from {})", location, lore.name());
                    count++;
                }
            }
        }
        log.info("  Total LOTR lore systems configured: {}", count);
    }
}
