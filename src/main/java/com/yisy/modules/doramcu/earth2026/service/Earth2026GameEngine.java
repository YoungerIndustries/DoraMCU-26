package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.config.Earth2026GameModule;
import com.yisy.modules.doramcu.unified.AbstractGameEngine;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.springframework.stereotype.Component;

/**
 * GameEngine implementation for Earth 2026.
 * <p>
 * Thin wrapper around {@link Earth2026GameModule} that extends {@link AbstractGameEngine}
 * for unified action processing via {@code processAction()}.
 * </p>
 */
@Component
public class Earth2026GameEngine extends AbstractGameEngine {

    private final Earth2026GameModule gameModule;

    public Earth2026GameEngine(Earth2026GameModule gameModule) {
        super(gameModule);
        this.gameModule = gameModule;
    }

    @Override
    public String getName() {
        return gameModule.getModuleName();
    }

    @Override
    public GameType getGameType() {
        return gameModule.getGameType();
    }
}
