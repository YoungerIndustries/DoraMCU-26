package com.yisy.modules.doramcu.star_trek_4x.service;

import com.yisy.modules.doramcu.star_trek_4x.config.StarTrek4xGameModule;
import com.yisy.modules.doramcu.unified.AbstractGameEngine;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.springframework.stereotype.Component;

/**
 * GameEngine implementation for Star Trek 4x.
 * <p>
 * Thin wrapper around {@link StarTrek4xGameModule} that extends {@link AbstractGameEngine}
 * for unified action processing via {@code processAction()}.
 * </p>
 */
@Component
public class StarTrek4xGameEngine extends AbstractGameEngine {

    private final StarTrek4xGameModule gameModule;

    public StarTrek4xGameEngine(StarTrek4xGameModule gameModule) {
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
