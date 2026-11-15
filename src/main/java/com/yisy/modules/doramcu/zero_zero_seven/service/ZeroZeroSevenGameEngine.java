package com.yisy.modules.doramcu.zero_zero_seven.service;

import com.yisy.modules.doramcu.zero_zero_seven.config.ZeroZeroSevenGameModule;
import com.yisy.modules.doramcu.unified.AbstractGameEngine;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.springframework.stereotype.Component;

/**
 * GameEngine implementation for 007 (James Bond).
 * <p>
 * Thin wrapper around {@link ZeroZeroSevenGameModule} that extends {@link AbstractGameEngine}
 * for unified action processing. 007 is non-turn-based (espionage missions).
 * </p>
 */
@Component
public class ZeroZeroSevenGameEngine extends AbstractGameEngine {

    private final ZeroZeroSevenGameModule gameModule;

    public ZeroZeroSevenGameEngine(ZeroZeroSevenGameModule gameModule) {
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

    @Override
    public boolean isTurnBased() {
        return false;
    }
}
