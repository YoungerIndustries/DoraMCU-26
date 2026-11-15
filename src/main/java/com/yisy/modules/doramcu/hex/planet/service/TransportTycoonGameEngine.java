package com.yisy.modules.doramcu.hex.planet.service;

import com.yisy.modules.doramcu.hex.planet.config.TransportTycoonGameModule;
import com.yisy.modules.doramcu.unified.AbstractGameEngine;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.springframework.stereotype.Component;

/**
 * GameEngine implementation for Transport Tycoon.
 * <p>
 * Thin wrapper around {@link TransportTycoonGameModule} that extends {@link AbstractGameEngine}
 * for unified action processing via {@code processAction()}.
 * </p>
 */
@Component
public class TransportTycoonGameEngine extends AbstractGameEngine {

    private final TransportTycoonGameModule gameModule;

    public TransportTycoonGameEngine(TransportTycoonGameModule gameModule) {
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
