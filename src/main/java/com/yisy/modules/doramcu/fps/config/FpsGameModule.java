package com.yisy.modules.doramcu.fps.config;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.fps.FpsGameServerWrapper;
import com.yisy.modules.doramcu.fps.FpsWeaponService;
import com.yisy.modules.doramcu.gamecore.AbstractGameModule;
import com.yisy.modules.doramcu.gamecore.GameModuleRegistry;
import com.yisy.modules.doramcu.gamecore.MechanicType;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * FPS Game Module adapter.
 * <p>
 * Implements the {@link GameModule} interface to register the FPS framework
 * with the {@link GameModuleRegistry}. This allows the FPS to be discovered
 * and launched like any other game module in DoraMCU.
 * </p>
 * <p>
 * The FPS module provides:
 * <ul>
 *   <li>Real-time multiplayer FPS gameplay</li>
 *   <li>Server-authoritative game loop with client prediction</li>
 *   <li>Extensible weapon and map system</li>
 *   <li>Cross-platform support (Unity and Web clients)</li>
 * </ul>
 * </p>
 */
@Component
public class FpsGameModule extends AbstractGameModule {

    private final FpsGameServerWrapper fpsGameServer;
    private final FpsWeaponService fpsWeaponService;

    public FpsGameModule(
            GameModuleRegistry gameModuleRegistry,
            FpsGameServerWrapper fpsGameServer,
            FpsWeaponService fpsWeaponService) {
        super(gameModuleRegistry);
        this.fpsGameServer = fpsGameServer;
        this.fpsWeaponService = fpsWeaponService;
    }

    @Override
    public GameType getGameType() {
        return GameType.FPS;
    }

    @Override
    public String getModuleName() {
        return "First-Person Shooter";
    }

    @Override
    public Set<MechanicType> getMechanics() {
        return Set.of(
                MechanicType.FPS,
                MechanicType.COMBAT,
                MechanicType.SURVIVAL
        );
    }
}
