package com.yisy.modules.doramcu.starwars.integration;

import com.yisy.modules.doramcu.gamecore.GameOverlayBuilder;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;
import com.yisy.modules.doramcu.service.game.LoreGroupEmpireService;
import com.yisy.modules.doramcu.service.game.LoreGroupEmpireService.LoreGroup;
import com.yisy.modules.doramcu.starwars.model.Faction;
import com.yisy.modules.doramcu.starwars.model.Lore;
import com.yisy.modules.doramcu.starwars.service.StarWarsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Star Wars overlay builder that creates persistent empires and AI characters
 * for Star Wars factions.
 * <p>
 * Creates the following empire structures:
 * <ul>
 *   <li><b>Rebel Alliance</b> — Led by Mon Mothma, with Luke Skywalker and Han Solo</li>
 *   <li><b>Galactic Empire</b> — Led by Emperor Palpatine, with Darth Vader</li>
 *   <li><b>Galactic Republic</b> — Led by Palpatine/Yoda, with Jedi Order</li>
 *   <li><b>Separatist Alliance</b> — Led by Count Dooku, with General Grievous</li>
 *   <li><b>Mandalorian Clan</b> — Led by Din Djarin, with Boba Fett</li>
 *   <li><b>Hutt Cartel</b> — Led by Jabba the Hutt</li>
 * </ul>
 * </p>
 * <p>
 * Each empire has AI character leaders saved to the database via {@link LoreGroupEmpireService}.
 * </p>
 */
@Component
public class StarWarsOverlayBuilder implements GameOverlayBuilder {

    private static final Logger log = LoggerFactory.getLogger(StarWarsOverlayBuilder.class);

    private final StarWarsService starWarsService;
    private final LoreGroupEmpireService loreGroupEmpireService;

    public StarWarsOverlayBuilder(StarWarsService starWarsService,
                                  LoreGroupEmpireService loreGroupEmpireService) {
        this.starWarsService = starWarsService;
        this.loreGroupEmpireService = loreGroupEmpireService;
    }

    @Override
    public GameType getGameType() {
        return GameType.STAR_WARS;
    }

    @Override
    public void buildOverlay(Galaxy galaxy, GalaxyGenerationConfig config) {
        log.info("Building Star Wars overlay for galaxy: {}", galaxy.getName());

        String galaxyName = galaxy.getName();

        // Phase 1: Faction-based empires from service data
        addFactionEmpires(galaxyName);

        // Phase 2: Hardcoded major Star Wars empires
        createMajorEmpires(galaxyName);

        // Phase 3: Lore systems
        addLoreSystems(galaxyName);

        log.info("Star Wars overlay complete for galaxy: {}", galaxy.getName());
    }

    private void addFactionEmpires(String galaxyName) {
        List<Faction> factions = starWarsService.getFactions();
        int count = 0;

        for (Faction faction : factions) {
            log.info("  Star Wars faction empire: {} ({}, ideology: {})",
                    faction.name(), faction.era(), faction.ideology());
            count++;
        }
        log.info("  Total Star Wars faction empires from service: {}", count);
    }

    /**
     * Creates the major Star Wars empires with their leaders as AI characters.
     */
    private void createMajorEmpires(String galaxyName) {
        log.info("Creating Star Wars major empires for galaxy: {}", galaxyName);

        List<LoreGroup> empires = new ArrayList<>();

        // Rebel Alliance
        empires.add(new LoreGroup(
                "Rebel Alliance",
                "General",
                new String[]{"Mon Mothma"},
                "The Rebel Alliance fights against the Galactic Empire to restore freedom to the galaxy.",
                "Leadership"
        ));

        // Galactic Empire
        empires.add(new LoreGroup(
                "Galactic Empire",
                "Emperor",
                new String[]{"Palpatine"},
                "The Galactic Empire rules the galaxy with an iron fist, enforcing order through fear.",
                "Dark Side"
        ));

        // Galactic Republic
        empires.add(new LoreGroup(
                "Galactic Republic",
                "Grand Master",
                new String[]{"Yoda"},
                "The Galactic Republic and the Jedi Order maintain peace and justice in the galaxy.",
                "Light Side"
        ));

        // Separatist Alliance
        empires.add(new LoreGroup(
                "Separatist Alliance",
                "Leader",
                new String[]{"Count Dooku"},
                "The Separatist Alliance seeks independence from the Republic.",
                "Dark Side"
        ));

        // Mandalorian Clan
        empires.add(new LoreGroup(
                "Mandalorian Clan",
                "Mandalorian",
                new String[]{"Din Djarin"},
                "The Mandalorian warriors follow the ancient ways of their people.",
                "Combat"
        ));

        // Hutt Cartel
        empires.add(new LoreGroup(
                "Hutt Cartel",
                "Hutt Lord",
                new String[]{"Jabba the Hutt"},
                "The Hutt Cartel controls the underworld of the Outer Rim Territories.",
                "Crime"
        ));

        loreGroupEmpireService.createEmpireLeaders(galaxyName, empires);

        // Add faction NPCs
        List<LoreGroupEmpireService.NpcData> npcs = new ArrayList<>();

        // Rebel NPCs
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Luke Skywalker", "Jedi Knight", "Rebel Alliance",
                "A Jedi Knight who destroys the Death Star"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Han Solo", "Smuggler", "Rebel Alliance",
                "A smuggler who becomes a Rebel hero"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Princess Leia", "Rebel Leader", "Rebel Alliance",
                "A senator who becomes a Rebel leader"
        ));

        // Empire NPCs
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Darth Vader", "Sith Lord", "Galactic Empire",
                "The Dark Lord of the Sith, enforcer of the Emperor"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Grand Moff Tarkin", "Imperial Governor", "Galactic Empire",
                "Governor of the Outer Rim Territories"
        ));

        // Republic NPCs
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Obi-Wan Kenobi", "Jedi Master", "Galactic Republic",
                "A Jedi Master who trains Anakin Skywalker"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Anakin Skywalker", "Jedi Knight", "Galactic Republic",
                "The Chosen One who becomes Darth Vader"
        ));

        // Separatist NPCs
        npcs.add(new LoreGroupEmpireService.NpcData(
                "General Grievous", "Separatist General", "Separatist Alliance",
                "The cyborg general of the Separatist droid army"
        ));

        // Mandalorian NPCs
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Boba Fett", "Bounty Hunter", "Mandalorian Clan",
                "A legendary bounty hunter"
        ));

        // Hutt NPCs
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Bib Fortuna", "Hutt Advisor", "Hutt Cartel",
                "Jabba's majordomo"
        ));

        loreGroupEmpireService.createNpcs(galaxyName, npcs);
    }

    private void addLoreSystems(String galaxyName) {
        List<Lore> loreEntries = starWarsService.getLore();
        int count = 0;

        for (Lore lore : loreEntries) {
            if (lore.notableLocations() != null) {
                for (String location : lore.notableLocations()) {
                    log.info("  Star Wars lore system: {} (from {})", location, lore.name());
                    count++;
                }
            }
        }
        log.info("  Total Star Wars lore systems configured: {}", count);
    }
}
