package com.yisy.modules.doramcu.gamecore;

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
 * 007 overlay builder that creates persistent empires and AI characters
 * for espionage organizations.
 * <p>
 * Creates the following empire structures:
 * <ul>
 *   <li><b>MI6</b> — British Secret Intelligence Service</li>
 *   <li><b>SPECTRE</b> — Spectacular Execution for Terror, Revenge, and Extortion</li>
 *   <li><b>SVR</b> — Russian Foreign Intelligence Service</li>
 *   <li><b>CIA</b> — American Central Intelligence Agency</li>
 *   <li><b>Interpol</b> — International criminal police organization</li>
 * </ul>
 * </p>
 * <p>
 * Each empire has AI character leaders saved to the database via {@link LoreGroupEmpireService}.
 * </p>
 */
@Component
public class ZeroZeroSevenOverlayBuilder implements GameOverlayBuilder {

    private static final Logger log = LoggerFactory.getLogger(ZeroZeroSevenOverlayBuilder.class);

    private final LoreGroupEmpireService loreGroupEmpireService;

    public ZeroZeroSevenOverlayBuilder(LoreGroupEmpireService loreGroupEmpireService) {
        this.loreGroupEmpireService = loreGroupEmpireService;
    }

    @Override
    public GameType getGameType() {
        return GameType.ZERO_ZERO_SEVEN;
    }

    @Override
    public void buildOverlay(Galaxy galaxy, GalaxyGenerationConfig config) {
        log.info("Building 007 overlay for galaxy: {}", galaxy.getName());

        String galaxyName = galaxy.getName();

        // Phase 1: Government intelligence agencies
        createIntelligenceAgencies(galaxyName);

        // Phase 2: Criminal organizations
        createCriminalOrganizations(galaxyName);

        log.info("007 overlay complete for galaxy: {}", galaxy.getName());
    }

    private void createIntelligenceAgencies(String galaxyName) {
        log.info("Creating 007 intelligence agencies for galaxy: {}", galaxyName);

        List<LoreGroup> empires = new ArrayList<>();

        // MI6
        empires.add(new LoreGroup(
                "MI6",
                "M",
                new String[]{"M"},
                "MI6 is the British Secret Intelligence Service, led by the enigmatic M.",
                "Espionage"
        ));

        // CIA
        empires.add(new LoreGroup(
                "CIA",
                "Director",
                new String[]{"Director"},
                "The CIA is the American Central Intelligence Agency, the world's premier intelligence service.",
                "Intelligence"
        ));

        // SVR
        empires.add(new LoreGroup(
                "SVR",
                "Director",
                new String[]{"Director"},
                "The SVR is the Russian Foreign Intelligence Service, a formidable adversary.",
                "Espionage"
        ));

        // Interpol
        empires.add(new LoreGroup(
                "Interpol",
                "President",
                new String[]{"President"},
                "Interpol coordinates international criminal investigations across borders.",
                "Law Enforcement"
        ));

        loreGroupEmpireService.createEmpireLeaders(galaxyName, empires);

        // Add agency NPCs
        List<LoreGroupEmpireService.NpcData> npcs = new ArrayList<>();
        npcs.add(new LoreGroupEmpireService.NpcData(
                "James Bond", "00 Agent", "MI6",
                "The legendary British secret agent with a license to kill"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Q", "Quartermaster", "MI6",
                "The brilliant quartermaster who equips agents with cutting-edge gadgets"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Moneypenny", "Secretary", "MI6",
                "M's efficient secretary who manages the flow of information"
        ));
        loreGroupEmpireService.createNpcs(galaxyName, npcs);
    }

    private void createCriminalOrganizations(String galaxyName) {
        log.info("Creating 007 criminal organizations for galaxy: {}", galaxyName);

        List<LoreGroup> empires = new ArrayList<>();

        // SPECTRE
        empires.add(new LoreGroup(
                "SPECTRE",
                "Number One",
                new String[]{"Ernst Stavro Blofeld"},
                "SPECTRE is a criminal organization dedicated to world domination through terror and extortion.",
                "Crime"
        ));

        loreGroupEmpireService.createEmpireLeaders(galaxyName, empires);

        // Add criminal NPCs
        List<LoreGroupEmpireService.NpcData> npcs = new ArrayList<>();
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Red Grant", "00 Agent", "SPECTRE",
                "A deadly SPECTRE assassin trained in martial arts"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Scaramanga", "Assassin", "SPECTRE",
                "The man with the golden gun, a legendary assassin"
        ));
        loreGroupEmpireService.createNpcs(galaxyName, npcs);
    }
}
