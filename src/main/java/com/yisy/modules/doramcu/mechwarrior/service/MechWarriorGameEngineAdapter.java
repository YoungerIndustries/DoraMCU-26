package com.yisy.modules.doramcu.mechwarrior.service;

import com.yisy.modules.doramcu.mechwarrior.config.MechWarriorGameModule;
import com.yisy.modules.doramcu.unified.AbstractGameEngine;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.springframework.stereotype.Component;

/**
 * GameEngine implementation for MechWarrior.
 * <p>
 * Thin wrapper around {@link MechWarriorGameModule} that extends {@link AbstractGameEngine}
 * for unified action processing. MechWarrior is non-turn-based (real-time combat).
 * </p>
 */
@Component
public class MechWarriorGameEngineAdapter extends AbstractGameEngine {

    private final MechWarriorGameModule gameModule;

    public MechWarriorGameEngineAdapter(MechWarriorGameModule gameModule) {
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
