package com.yisy.modules.doramcu.thirdreich.service;

import com.yisy.modules.doramcu.unified.AbstractGameEngine;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.springframework.stereotype.Component;

/**
 * GameEngine implementation for Third Reich (3R4).
 * <p>
 * Standalone implementation — Third Reich uses the 3R4 adapter pattern and does not
 * have a {@code GameModule}. Extends {@link AbstractGameEngine} with null module
 * for unified action processing. Third Reich is turn-based with CRT combat rounds.
 * </p>
 */
@Component
public class ThirdReichGameEngine extends AbstractGameEngine {

    public ThirdReichGameEngine() {
        super(null);
    }

    @Override
    public String getName() {
        return "Third Reich (3R4)";
    }

    @Override
    public GameType getGameType() {
        return GameType.THIRD_REICH;
    }
}
