package com.yisy.modules.doramcu.starshiptroopers.service;

import com.yisy.modules.doramcu.starshiptroopers.config.StarshipTroopersGameModule;
import com.yisy.modules.doramcu.unified.AbstractGameEngine;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.springframework.stereotype.Component;

/**
 * GameEngine implementation for Starship Troopers.
 * <p>
 * Thin wrapper around {@link StarshipTroopersGameModule} that extends {@link AbstractGameEngine}
 * for unified action processing via {@code processAction()}.
 * </p>
 */
@Component
public class StarshipTroopersGameEngine extends AbstractGameEngine {

    private final StarshipTroopersGameModule gameModule;

    public StarshipTroopersGameEngine(StarshipTroopersGameModule gameModule) {
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
