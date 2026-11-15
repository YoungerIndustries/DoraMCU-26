package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.config.Dnd5eGameModule;
import com.yisy.modules.doramcu.unified.AbstractGameEngine;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.springframework.stereotype.Component;

/**
 * GameEngine implementation for D&amp;D 5e.
 * <p>
 * Thin wrapper around {@link Dnd5eGameModule} that extends {@link AbstractGameEngine}
 * for unified action processing via {@code processAction()}.
 * </p>
 */
@Component
public class Dnd5eGameEngine extends AbstractGameEngine {

    private final Dnd5eGameModule gameModule;

    public Dnd5eGameEngine(Dnd5eGameModule gameModule) {
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
