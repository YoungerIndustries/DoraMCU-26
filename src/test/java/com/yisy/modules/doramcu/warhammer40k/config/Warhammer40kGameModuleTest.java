package com.yisy.modules.doramcu.warhammer40k.config;

import com.yisy.modules.doramcu.gamecore.GameModuleRegistry;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.gamecore.MechanicType;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.warhammer40k.config.WarhammerTechLevelAdapter;
import com.yisy.modules.doramcu.warhammer40k.service.Warhammer40kRulesEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Warhammer40kGameModule")
@ExtendWith(MockitoExtension.class)
class Warhammer40kGameModuleTest {

    @Mock
    private Warhammer40kRulesEngine rulesEngine;

    @Mock
    private WarhammerTechLevelAdapter techLevelAdapter;

    @Mock
    private GameModuleRegistry registry;

    private Warhammer40kGameModule module;

    @BeforeEach
    void setUp() {
        module = new Warhammer40kGameModule(rulesEngine, techLevelAdapter, registry);
    }

    @Nested
    @DisplayName("Game Type and Name")
    class GameTypeAndName {

        @Test
        @DisplayName("returns WARHAMMER_40K game type")
        void returnsGameType() {
            assertEquals(GameType.WARHAMMER_40K, module.getGameType());
        }

        @Test
        @DisplayName("returns Warhammer 40K module name")
        void returnsModuleName() {
            assertEquals("Warhammer 40K", module.getModuleName());
        }
    }

    @Nested
    @DisplayName("Rules Engine")
    class RulesEngine {

        @Test
        @DisplayName("returns injected rules engine")
        void returnsRulesEngine() {
            assertSame(rulesEngine, module.getRulesEngine());
        }
    }

    @Nested
    @DisplayName("Tech Level Adapter")
    class TechLevelAdapter {

        @Test
        @DisplayName("returns injected tech level adapter")
        void returnsTechLevelAdapter() {
            assertSame(techLevelAdapter, module.getTechLevelAdapter());
        }
    }

    @Nested
    @DisplayName("Mechanics")
    class Mechanics {

        @Test
        @DisplayName("returns expected mechanics")
        void returnsExpectedMechanics() {
            Set<MechanicType> mechanics = module.getMechanics();

            assertNotNull(mechanics);
            assertTrue(mechanics.contains(MechanicType.COMBAT));
            assertTrue(mechanics.contains(MechanicType.TACTICAL));
            assertTrue(mechanics.contains(MechanicType.SUPERNATURAL));
            assertTrue(mechanics.contains(MechanicType.ECONOMY));
            assertEquals(4, mechanics.size());
        }
    }
}
