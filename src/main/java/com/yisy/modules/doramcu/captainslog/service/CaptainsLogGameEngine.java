package com.yisy.modules.doramcu.captainslog.service;

import com.yisy.modules.doramcu.captainslog.config.CaptainsLogGameModule;
import com.yisy.modules.doramcu.unified.AbstractGameEngine;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.springframework.stereotype.Component;

/**
 * GameEngine implementation for Captain's Log.
 * <p>
 * Thin wrapper around {@link CaptainsLogGameModule} that extends {@link AbstractGameEngine}
 * for unified action processing via {@code processAction()}.
 * </p>
 */
@Component
public class CaptainsLogGameEngine extends AbstractGameEngine {

    private final CaptainsLogGameModule gameModule;

    public CaptainsLogGameEngine(CaptainsLogGameModule gameModule) {
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
