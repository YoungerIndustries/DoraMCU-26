package com.yisy.modules.doramcu.warhammer40k.integration;

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
 * Warhammer 40K overlay builder that creates persistent empires and AI characters
 * for Warhammer 40K factions.
 * <p>
 * Creates the following empire structures:
 * <ul>
 *   <li><b>Imperium of Man</b> — Human empire ruled by the God-Emperor</li>
 *   <li><b>Chaos Forces</b> — Servants of the Ruinous Powers</li>
 *   <li><b>Ork Waaagh!</b> — Green-skinned warrior hordes</li>
 *   <li><b>Necron Dynasties</b> — Ancient robotic undead</li>
 *   <li><b>Eldar Craftworlds</b> — Dying ancient race</li>
 *   <li><b>Tyranid Hive Fleet</b> — Alien consumption swarm</li>
 * </ul>
 * </p>
 * <p>
 * Each empire has AI character leaders saved to the database via {@link LoreGroupEmpireService}.
 * </p>
 */
@Component
public class Warhammer40KOverlayBuilder implements GameOverlayBuilder {

    private static final Logger log = LoggerFactory.getLogger(Warhammer40KOverlayBuilder.class);

    private final LoreGroupEmpireService loreGroupEmpireService;

    public Warhammer40KOverlayBuilder(LoreGroupEmpireService loreGroupEmpireService) {
        this.loreGroupEmpireService = loreGroupEmpireService;
    }

    @Override
    public GameType getGameType() {
        return GameType.WARHAMMER_40K;
    }

    @Override
    public void buildOverlay(Galaxy galaxy, GalaxyGenerationConfig config) {
        log.info("Building Warhammer 40K overlay for galaxy: {}", galaxy.getName());

        String galaxyName = galaxy.getName();

        // Phase 1: Major faction empires
        createMajorFactions(galaxyName);

        // Phase 2: Xenos and threat empires
        createThreatEmpires(galaxyName);

        log.info("Warhammer 40K overlay complete for galaxy: {}", galaxy.getName());
    }

    private void createMajorFactions(String galaxyName) {
        log.info("Creating Warhammer 40K major factions for galaxy: {}", galaxyName);

        List<LoreGroup> empires = new ArrayList<>();

        // Imperium of Man
        empires.add(new LoreGroup(
                "Imperium of Man",
                "Primarch",
                new String[]{"Roboute Guilliman"},
                "The Imperium of Man is the vast human empire ruled by the God-Emperor from the Golden Throne of Terra.",
                "Military"
        ));

        // Chaos Forces
        empires.add(new LoreGroup(
                "Chaos Forces",
                "Chaos Lord",
                new String[]{"Angron"},
                "The forces of Chaos serve the Ruinous Powers: Khorne, Tzeentch, Nurgle, and Slaanesh.",
                "Dark Power"
        ));

        // Ork Waaagh!
        empires.add(new LoreGroup(
                "Ork Waaagh!",
                "Warboss",
                new String[]{"Ghazghkull"},
                "The Orks are green-skinned warriors who live for battle and conquest.",
                "Combat"
        ));

        loreGroupEmpireService.createEmpireLeaders(galaxyName, empires);

        // Add faction NPCs
        List<LoreGroupEmpireService.NpcData> npcs = new ArrayList<>();
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Dorn", "Primarch of Imperium", "Imperium of Man",
                "The Primarch of the Imperial Fists, defender of Terra"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Ezekiel", "Space Marine Captain", "Imperium of Man",
                "A veteran Space Marine captain"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Krag", "Ork Warboss", "Ork Waaagh!",
                "A fierce Ork warboss leading a Waaagh!"
        ));
        loreGroupEmpireService.createNpcs(galaxyName, npcs);
    }

    private void createThreatEmpires(String galaxyName) {
        log.info("Creating Warhammer 40K threat empires for galaxy: {}", galaxyName);

        List<LoreGroup> empires = new ArrayList<>();

        // Necron Dynasties
        empires.add(new LoreGroup(
                "Necron Dynasties",
                "Dynasty Lord",
                new String[]{"Szarekh"},
                "The Necrons are ancient robotic undead who seek to reclaim their former galactic empire.",
                "Ancient Technology"
        ));

        // Eldar Craftworlds
        empires.add(new LoreGroup(
                "Eldar Craftworlds",
                "Farseer",
                new String[]{"Yvraine"},
                "The Eldar are a dying ancient race who survive on massive generation ships called Craftworlds.",
                "Psychic Power"
        ));

        // Tyranid Hive Fleet
        empires.add(new LoreGroup(
                "Tyranid Hive Fleet",
                "Hive Tyrant",
                new String[]{"Hive Tyrant"},
                "The Tyranids are an alien swarm that consumes all biomass in their path, driven by the Hive Mind.",
                "Assimilation"
        ));

        loreGroupEmpireService.createEmpireLeaders(galaxyName, empires);

        // Threat NPCs
        List<LoreGroupEmpireService.NpcData> npcs = new ArrayList<>();
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Faelarion", "Necron Lord", "Necron Dynasties",
                "A Necron Lord commanding legions of undead warriors"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Farseer Masque", "Eldar Farseer", "Eldar Craftworlds",
                "An Eldar seer who can glimpse possible futures"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Cronos", "Tyranid Hive Tyrant", "Tyranid Hive Fleet",
                "A massive Tyranid bio-weapon that commands the swarm"
        ));
        loreGroupEmpireService.createNpcs(galaxyName, npcs);
    }
}
