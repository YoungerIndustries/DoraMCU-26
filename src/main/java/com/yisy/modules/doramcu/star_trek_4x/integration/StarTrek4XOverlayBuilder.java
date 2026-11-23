package com.yisy.modules.doramcu.star_trek_4x.integration;

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
 * Star Trek 4X overlay builder that creates persistent empires and AI characters
 * for Star Trek factions.
 * <p>
 * Creates the following empire structures:
 * <ul>
 *   <li><b>United Federation of Planets</b> — Human-led democratic federation</li>
 *   <li><b>Klingon Empire</b> — Warrior honor culture</li>
 *   <li><b>Romulan Star Empire</b> — Secretive militaristic empire</li>
 *   <li><b>Cardassian Union</b> — Authoritarian expansionist regime</li>
 *   <li><b>Ferengi Alliance</b> — Profit-driven merchant civilization</li>
 *   <li><b>Borg Collective</b> — Cybernetic assimilation threat</li>
 * </ul>
 * </p>
 * <p>
 * Each empire has AI character leaders saved to the database via {@link LoreGroupEmpireService}.
 * </p>
 */
@Component
public class StarTrek4XOverlayBuilder implements GameOverlayBuilder {

    private static final Logger log = LoggerFactory.getLogger(StarTrek4XOverlayBuilder.class);

    private final LoreGroupEmpireService loreGroupEmpireService;

    public StarTrek4XOverlayBuilder(LoreGroupEmpireService loreGroupEmpireService) {
        this.loreGroupEmpireService = loreGroupEmpireService;
    }

    @Override
    public GameType getGameType() {
        return GameType.STAR_TREK_4X;
    }

    @Override
    public void buildOverlay(Galaxy galaxy, GalaxyGenerationConfig config) {
        log.info("Building Star Trek 4X overlay for galaxy: {}", galaxy.getName());

        String galaxyName = galaxy.getName();

        // Phase 1: Major faction empires
        createMajorFactions(galaxyName);

        // Phase 2: Threat empires
        createThreatEmpires(galaxyName);

        log.info("Star Trek 4X overlay complete for galaxy: {}", galaxy.getName());
    }

    private void createMajorFactions(String galaxyName) {
        log.info("Creating Star Trek major factions for galaxy: {}", galaxyName);

        List<LoreGroup> empires = new ArrayList<>();

        // United Federation of Planets
        empires.add(new LoreGroup(
                "United Federation of Planets",
                "Captain",
                new String[]{"Jean-Luc Picard"},
                "The United Federation of Planets is a democratic interstellar federation dedicated to peace and exploration.",
                "Diplomacy"
        ));

        // Klingon Empire
        empires.add(new LoreGroup(
                "Klingon Empire",
                "Chancellor",
                new String[]{"Gowron"},
                "The Klingon Empire is a warrior culture that values honor, strength, and battle above all.",
                "Combat"
        ));

        // Romulan Star Empire
        empires.add(new LoreGroup(
                "Romulan Star Empire",
                "Praetor",
                new String[]{"Tal'Shiar"},
                "The Romulan Star Empire is a secretive militaristic power hiding behind the Neutral Zone.",
                "Stealth"
        ));

        // Cardassian Union
        empires.add(new LoreGroup(
                "Cardassian Union",
                "Head of Central Command",
                new String[]{"Gul Dukat"},
                "The Cardassian Union is an authoritarian regime that expands through occupation and oppression.",
                "Military"
        ));

        // Ferengi Alliance
        empires.add(new LoreGroup(
                "Ferengi Alliance",
                "Grand Nagus",
                new String[]{"Grand Nagus Zek"},
                "The Ferengi Alliance is a profit-driven civilization governed by the Rules of Acquisition.",
                "Trade"
        ));

        loreGroupEmpireService.createEmpireLeaders(galaxyName, empires);

        // Add faction NPCs
        List<LoreGroupEmpireService.NpcData> npcs = new ArrayList<>();
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Data", "Android Officer", "United Federation of Planets",
                "An android seeking to understand humanity"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Worf", "Klingon Security Officer", "Klingon Empire",
                "A Klingon warrior serving Starfleet"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Sisko", "Station Commander", "United Federation of Planets",
                "Commander of Deep Space 9"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Quark", "Bar Owner", "Ferengi Alliance",
                "A Ferengi merchant operating on Deep Space 9"
        ));
        loreGroupEmpireService.createNpcs(galaxyName, npcs);
    }

    private void createThreatEmpires(String galaxyName) {
        log.info("Creating Star Trek threat empires for galaxy: {}", galaxyName);

        List<LoreGroup> empires = new ArrayList<>();

        // Borg Collective
        empires.add(new LoreGroup(
                "Borg Collective",
                "Queen",
                new String[]{"Borg Queen"},
                "The Borg Collective assimilates all life forms, eliminating individuality for collective perfection.",
                "Assimilation"
        ));

        // Dominion
        empires.add(new LoreGroup(
                "Dominion",
                "Changeling Founder",
                new String[]{"The Founders"},
                "The Dominion is a shape-shifting race that rules through fear and manipulation.",
                "Shapeshifting"
        ));

        loreGroupEmpireService.createEmpireLeaders(galaxyName, empires);

        // Threat NPCs
        List<LoreGroupEmpireService.NpcData> npcs = new ArrayList<>();
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Seven of Nine", "Former Borg", "Borg Collective",
                "A former Borg drone who was liberated by Voyager"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Odo", "Security Chief", "Dominion",
                "A Changeling who serves as security chief on Deep Space 9"
        ));
        loreGroupEmpireService.createNpcs(galaxyName, npcs);
    }
}
