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
 * DoraMCU 4X overlay builder that creates persistent empires and AI characters
 * for G8/G20 country-based empires.
 * <p>
 * Creates the following empire structures:
 * <ul>
 *   <li><b>United States</b> — American space-faring empire</li>
 *   <li><b>United Kingdom</b> — British interstellar dominion</li>
 *   <li><b>France</b> — French galactic republic</li>
 *   <li><b>Germany</b> — German space federation</li>
 *   <li><b>Japan</b> — Japanese stellar empire</li>
 *   <li><b>China</b> — Chinese cosmic dynasty</li>
 * </ul>
 * </p>
 * <p>
 * Each empire has AI character leaders saved to the database via {@link LoreGroupEmpireService}.
 * </p>
 */
@Component
public class DoraMCU4XOverlayBuilder implements GameOverlayBuilder {

    private static final Logger log = LoggerFactory.getLogger(DoraMCU4XOverlayBuilder.class);

    private final LoreGroupEmpireService loreGroupEmpireService;

    public DoraMCU4XOverlayBuilder(LoreGroupEmpireService loreGroupEmpireService) {
        this.loreGroupEmpireService = loreGroupEmpireService;
    }

    @Override
    public GameType getGameType() {
        return GameType.DORAMCU_4X;
    }

    @Override
    public void buildOverlay(Galaxy galaxy, GalaxyGenerationConfig config) {
        log.info("Building DoraMCU 4X overlay for galaxy: {}", galaxy.getName());

        String galaxyName = galaxy.getName();

        // Phase 1: G8/G20 country empires
        createCountryEmpires(galaxyName);

        log.info("DoraMCU 4X overlay complete for galaxy: {}", galaxy.getName());
    }

    private void createCountryEmpires(String galaxyName) {
        log.info("Creating DoraMCU 4X country empires for galaxy: {}", galaxyName);

        List<LoreGroup> empires = new ArrayList<>();

        // United States
        empires.add(new LoreGroup(
                "United States",
                "President",
                new String[]{"President"},
                "The United States space empire leads the G8 coalition with advanced technology and military power.",
                "Technology"
        ));

        // United Kingdom
        empires.add(new LoreGroup(
                "United Kingdom",
                "Prime Minister",
                new String[]{"Prime Minister"},
                "The British interstellar dominion brings naval tradition and diplomatic prowess to the stars.",
                "Diplomacy"
        ));

        // France
        empires.add(new LoreGroup(
                "France",
                "President",
                new String[]{"President"},
                "The French galactic republic excels in culture, science, and strategic innovation.",
                "Science"
        ));

        // Germany
        empires.add(new LoreGroup(
                "Germany",
                "Chancellor",
                new String[]{"Chancellor"},
                "The German space federation is renowned for engineering precision and industrial might.",
                "Industry"
        ));

        // Japan
        empires.add(new LoreGroup(
                "Japan",
                "Prime Minister",
                new String[]{"Prime Minister"},
                "The Japanese stellar empire combines ancient tradition with cutting-edge technology.",
                "Technology"
        ));

        // China
        empires.add(new LoreGroup(
                "China",
                "President",
                new String[]{"President"},
                "The Chinese cosmic dynasty brings millennia of wisdom to interstellar conquest.",
                "Strategy"
        ));

        loreGroupEmpireService.createEmpireLeaders(galaxyName, empires);

        // Add country NPCs
        List<LoreGroupEmpireService.NpcData> npcs = new ArrayList<>();
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Admiral Hayes", "Fleet Commander", "United States",
                "A veteran fleet commander of the US Space Force"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Lord Admiral Windsor", "Naval Commander", "United Kingdom",
                "The British naval commander of the interstellar fleet"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "Dr. Tanaka", "Science Officer", "Japan",
                "A brilliant Japanese scientist leading the tech division"
        ));
        npcs.add(new LoreGroupEmpireService.NpcData(
                "General Wei", "Military Strategist", "China",
                "A Chinese military strategist with centuries of tactical wisdom"
        ));
        loreGroupEmpireService.createNpcs(galaxyName, npcs);
    }
}
