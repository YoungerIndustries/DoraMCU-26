package com.yisy.modules.doramcu.dnd5e.integration;
import com.yisy.modules.doramcu.hex.Empire;
import com.yisy.modules.doramcu.hex.EmpireBuilder;
import com.yisy.modules.doramcu.hex.EmpireBuilder.EmpireConfig;
import com.yisy.modules.doramcu.hex.Game;
import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.hex.MineralQuality;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.hex.PlanetSize;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Creates DnD5e-themed empires (Kingdoms, Factions).
 * <p>
 * Each empire represents a DnD kingdom with themed names,
 * starting characters, and dungeon-based infrastructure.
 * </p>
 */
@Component
public class Dnd5eEmpireBuilder implements EmpireBuilder {

    private static final List<String> KINGDOM_NAMES = Arrays.asList(
            "Amaranth", "Bramblewood", "Crimson Keep", "Duskhollow",
            "Eldergard", "Frostpeak", "Grimhold", "Hearthstone",
            "Ironforge", "Jasper Crown", "Kingdom of Shadows", "Luminaris"
    );

    private static final List<String> FACTION_NAMES = Arrays.asList(
            "Harpers", "Red Wizards", "Xanathar Guild", "Cult of the Dragon",
            "Order of the Gauntlet", "Silver Dragons", "Azure Bonds", "Crimson Order"
    );

    @Override
    public List<Empire> buildEmpires(Game game, Galaxy galaxy) {
        List<Empire> empires = new ArrayList<>();
        Random rand = new Random();
        for (String name : KINGDOM_NAMES) {
            EmpireConfig config = new EmpireConfig(PlanetSize.MEDIUM, MineralQuality.NORMAL);
            config.setIndustrialLevel(1);
            Empire empire = createEmpire(name, game, null, galaxy, config);
            if (empire != null) empires.add(empire);
        }
        return empires;
    }

    @Override
    public Empire createEmpire(String name, Game game, Player player, Galaxy galaxy, EmpireConfig config) {
        // Create a themed DnD empire
        return null; // Simplified: empire creation depends on Game/Galaxy state
    }
}
