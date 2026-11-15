package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.sfb.config.SfbGameModule;
import com.yisy.modules.doramcu.unified.AbstractGameEngine;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.springframework.stereotype.Component;

/**
 * GameEngine implementation for SFB (Star Frontiers Battle).
 * <p>
 * Thin wrapper around {@link SfbGameModule} that extends {@link AbstractGameEngine}
 * for unified action processing via {@code processAction()}.
 * </p>
 */
@Component
public class SfbGameEngine extends AbstractGameEngine {

    private final SfbGameModule gameModule;

    public SfbGameEngine(SfbGameModule gameModule) {
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
