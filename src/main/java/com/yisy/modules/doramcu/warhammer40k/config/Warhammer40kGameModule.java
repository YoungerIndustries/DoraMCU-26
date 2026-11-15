package com.yisy.modules.doramcu.warhammer40k.config;

import com.yisy.modules.doramcu.gamecore.AbstractGameModule;
import com.yisy.modules.doramcu.gamecore.GameModuleRegistry;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.gamecore.MechanicType;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import com.yisy.modules.doramcu.warhammer40k.config.WarhammerTechLevelAdapter;
import com.yisy.modules.doramcu.warhammer40k.service.Warhammer40kRulesEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Warhammer 40K game module adapter.
 * <p>
 * Integrates the Warhammer 40K universe into the DoraMCU game system.
 * Uses the compact style (Earth 2026 pattern) with separate adapter classes.
 * </p>
 */
@Component
public class Warhammer40kGameModule extends AbstractGameModule {

    private final Warhammer40kRulesEngine rulesEngine;
    private final WarhammerTechLevelAdapter techLevelAdapter;

    @Autowired
    public Warhammer40kGameModule(
            Warhammer40kRulesEngine rulesEngine,
            WarhammerTechLevelAdapter techLevelAdapter,
            GameModuleRegistry registry) {
        super(registry);
        this.rulesEngine = rulesEngine;
        this.techLevelAdapter = techLevelAdapter;
    }

    @Override
    public GameType getGameType() {
        return GameType.WARHAMMER_40K;
    }

    @Override
    public String getModuleName() {
        return "Warhammer 40K";
    }

    @Override
    public RulesEngine getRulesEngine() {
        return rulesEngine;
    }

    @Override
    public TechLevelAdapter getTechLevelAdapter() {
        return techLevelAdapter;
    }

    @Override
    public Set<MechanicType> getMechanics() {
        return Set.of(
                MechanicType.COMBAT,
                MechanicType.TACTICAL,
                MechanicType.SUPERNATURAL,
                MechanicType.ECONOMY
        );
    }
}
