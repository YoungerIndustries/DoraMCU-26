package com.yisy.modules.doramcu.corporation.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AiAssistantActionTest {

    @Test
    void constructor_defaults_timestamp() {
        AiAssistantAction action = new AiAssistantAction();
        assertNotNull(action.getTimestamp());
        assertTrue(action.getTimestamp().isAfter(LocalDateTime.now().minusMinutes(1)));
    }

    @Test
    void constructor_sets_all_fields() {
        String snapshot = "{\"aggression\":0.7}";
        AiAssistantAction action = new AiAssistantAction(
                1L, AiAssistantAction.ActionType.ACCEPT_CONTRACT, 100L,
                AiAssistantAction.Result.SUCCESS, "Test action", snapshot);

        assertEquals(1L, action.getPlayerId());
        assertEquals(AiAssistantAction.ActionType.ACCEPT_CONTRACT, action.getActionType());
        assertEquals(100L, action.getTargetId());
        assertEquals(AiAssistantAction.Result.SUCCESS, action.getResult());
        assertEquals("Test action", action.getDescription());
        assertEquals(snapshot, action.getPersonalitySnapshot());
        assertNotNull(action.getTimestamp());
    }

    @Test
    void action_type_enum_values() {
        AiAssistantAction.ActionType[] types = AiAssistantAction.ActionType.values();
        assertEquals(5, types.length);
        assertContains(types, AiAssistantAction.ActionType.PROCESS_CONTRACT);
        assertContains(types, AiAssistantAction.ActionType.ACCEPT_CONTRACT);
        assertContains(types, AiAssistantAction.ActionType.HIRE_CREW);
        assertContains(types, AiAssistantAction.ActionType.FIRE_CREW);
        assertContains(types, AiAssistantAction.ActionType.SKIP_TURN);
    }

    @Test
    void result_enum_values() {
        AiAssistantAction.Result[] results = AiAssistantAction.Result.values();
        assertEquals(3, results.length);
        assertContains(results, AiAssistantAction.Result.SUCCESS);
        assertContains(results, AiAssistantAction.Result.FAILED);
        assertContains(results, AiAssistantAction.Result.SKIPPED);
    }

    @Test
    void setters_update_values() {
        AiAssistantAction action = new AiAssistantAction();
        action.setId(42L);
        action.setPlayerId(7L);
        action.setActionType(AiAssistantAction.ActionType.HIRE_CREW);
        action.setTargetId(200L);
        action.setResult(AiAssistantAction.Result.FAILED);
        action.setDescription("Hire failed");
        action.setPersonalitySnapshot("{\"economic\":0.8}");
        LocalDateTime ts = LocalDateTime.of(2025, 1, 1, 0, 0);
        action.setTimestamp(ts);

        assertEquals(42L, action.getId());
        assertEquals(7L, action.getPlayerId());
        assertEquals(AiAssistantAction.ActionType.HIRE_CREW, action.getActionType());
        assertEquals(200L, action.getTargetId());
        assertEquals(AiAssistantAction.Result.FAILED, action.getResult());
        assertEquals("Hire failed", action.getDescription());
        assertEquals("{\"economic\":0.8}", action.getPersonalitySnapshot());
        assertEquals(ts, action.getTimestamp());
    }

    @Test
    void toString_contains_key_fields() {
        AiAssistantAction action = new AiAssistantAction(
                1L, AiAssistantAction.ActionType.PROCESS_CONTRACT, 50L,
                AiAssistantAction.Result.SUCCESS, "Processed", null);
        action.setId(99L);

        String str = action.toString();
        assertTrue(str.contains("id=99"));
        assertTrue(str.contains("playerId=1"));
        assertTrue(str.contains("PROCESS_CONTRACT"));
        assertTrue(str.contains("targetId=50"));
        assertTrue(str.contains("SUCCESS"));
    }

    private static <T> void assertContains(T[] array, T expected) {
        for (T item : array) {
            if (item == expected) return;
        }
        fail("Array does not contain: " + expected);
    }
}
