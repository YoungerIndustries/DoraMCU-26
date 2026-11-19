package com.yisy.modules.doramcu.fullthrust;

import com.yisy.modules.doramcu.fullthrust.service.AIService;
import com.yisy.modules.doramcu.fullthrust.service.GameService;
import com.yisy.modules.doramcu.fullthrust.service.PersistenceService;
import com.yisy.modules.doramcu.fullthrust.service.PlayerAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SerializationTest {

    private GameService gameService;

    @BeforeEach
    void setUp() {
        PersistenceService persistenceService = mock(PersistenceService.class);
        AIService aiService = mock(AIService.class);
        PlayerAccountService playerAccountService = mock(PlayerAccountService.class);
        gameService = new GameService(persistenceService, aiService, playerAccountService);
    }

    @Test
    void testListSessions() {
        var sessions = gameService.listSessions();
        assertNotNull(sessions);
    }

    @Test
    void testCreateAndList() {
        gameService.createGame("Test");
        var sessions = gameService.listSessions();
        assertEquals(1, sessions.size());
    }
}
