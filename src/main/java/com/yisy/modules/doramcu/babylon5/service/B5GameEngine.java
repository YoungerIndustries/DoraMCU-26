package com.yisy.modules.doramcu.babylon5.service;

import com.yisy.modules.doramcu.babylon5.config.Babylon5GameModule;
import com.yisy.modules.doramcu.unified.AbstractGameEngine;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.springframework.stereotype.Component;

/**
 * GameEngine implementation for Babylon 5.
 * <p>
 * Thin wrapper around {@link Babylon5GameModule} that extends {@link AbstractGameEngine}
 * for unified action processing via {@code processAction()}.
 * </p>
 */
@Component
public class B5GameEngine extends AbstractGameEngine {

    private final Babylon5GameModule gameModule;

    public B5GameEngine(Babylon5GameModule gameModule) {
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
