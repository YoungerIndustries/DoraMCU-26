package com.yisy.modules.doramcu.starwars.service;

import com.yisy.modules.doramcu.starwars.config.StarWarsGameModule;
import com.yisy.modules.doramcu.unified.AbstractGameEngine;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.springframework.stereotype.Component;

/**
 * GameEngine implementation for Star Wars.
 * <p>
 * Thin wrapper around {@link StarWarsGameModule} that extends {@link AbstractGameEngine}
 * for unified action processing via {@code processAction()}.
 * </p>
 */
@Component
public class StarWarsGameEngine extends AbstractGameEngine {

    private final StarWarsGameModule gameModule;

    public StarWarsGameEngine(StarWarsGameModule gameModule) {
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
