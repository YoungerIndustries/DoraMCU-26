package com.yisy.modules.doramcu.traveller.service;

import com.yisy.modules.doramcu.traveller.config.TravellerGameModule;
import com.yisy.modules.doramcu.unified.AbstractGameEngine;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.springframework.stereotype.Component;

/**
 * GameEngine implementation for Traveller.
 * <p>
 * Thin wrapper around {@link TravellerGameModule} that extends {@link AbstractGameEngine}
 * for unified action processing via {@code processAction()}.
 * </p>
 */
@Component
public class TravellerGameEngine extends AbstractGameEngine {

    private final TravellerGameModule gameModule;

    public TravellerGameEngine(TravellerGameModule gameModule) {
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
