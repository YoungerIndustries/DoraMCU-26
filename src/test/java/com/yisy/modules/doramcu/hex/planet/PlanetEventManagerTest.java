package com.yisy.modules.doramcu.hex.planet;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class PlanetEventManagerTest {

    // ---- Listener registration ----

    @Test
    void registerListener_addsTypeSpecificListener() {
        PlanetEventManager manager = new PlanetEventManager();
        List<PlanetEvent> received = new ArrayList<>();
        manager.registerListener(PlanetEventType.SETTLEMENT_FOUND, received::add);
        assertEquals(1, manager.getListenerCount());
    }

    @Test
    void registerListener_nullEventType_throws() {
        PlanetEventManager manager = new PlanetEventManager();
        assertThrows(IllegalArgumentException.class, () ->
                manager.registerListener(null, e -> {}));
    }

    @Test
    void registerListener_nullListener_throws() {
        PlanetEventManager manager = new PlanetEventManager();
        assertThrows(IllegalArgumentException.class, () ->
                manager.registerListener(PlanetEventType.SETTLEMENT_FOUND, null));
    }

    @Test
    void registerGlobalListener_addsGlobalListener() {
        PlanetEventManager manager = new PlanetEventManager();
        List<PlanetEvent> received = new ArrayList<>();
        manager.registerGlobalListener(received::add);
        assertEquals(1, manager.getListenerCount());
    }

    @Test
    void registerGlobalListener_nullListener_throws() {
        PlanetEventManager manager = new PlanetEventManager();
        assertThrows(IllegalArgumentException.class, () ->
                manager.registerGlobalListener(null));
    }

    @Test
    void registerMultipleListeners_forSameType() {
        PlanetEventManager manager = new PlanetEventManager();
        List<PlanetEvent> list1 = new ArrayList<>();
        List<PlanetEvent> list2 = new ArrayList<>();
        manager.registerListener(PlanetEventType.SETTLEMENT_FOUND, list1::add);
        manager.registerListener(PlanetEventType.SETTLEMENT_FOUND, list2::add);
        assertEquals(2, manager.getListenerCount());
    }

    // ---- Listener unregistration ----

    @Test
    void unregisterListener_removesListener() {
        PlanetEventManager manager = new PlanetEventManager();
        java.util.function.Consumer<PlanetEvent> listener = e -> {};
        manager.registerListener(PlanetEventType.SETTLEMENT_FOUND, listener);
        assertTrue(manager.unregisterListener(PlanetEventType.SETTLEMENT_FOUND, listener));
        assertEquals(0, manager.getListenerCount());
    }

    @Test
    void unregisterListener_notFound_returnsFalse() {
        PlanetEventManager manager = new PlanetEventManager();
        assertFalse(manager.unregisterListener(PlanetEventType.SETTLEMENT_FOUND, e -> {}));
    }

    @Test
    void unregisterGlobalListener_removesListener() {
        PlanetEventManager manager = new PlanetEventManager();
        java.util.function.Consumer<PlanetEvent> listener = e -> {};
        manager.registerGlobalListener(listener);
        assertTrue(manager.unregisterGlobalListener(listener));
        assertEquals(0, manager.getListenerCount());
    }

    @Test
    void unregisterGlobalListener_notFound_returnsFalse() {
        PlanetEventManager manager = new PlanetEventManager();
        assertFalse(manager.unregisterGlobalListener(e -> {}));
    }

    // ---- Dispatch ----

    @Test
    void dispatch_toTypeSpecificListeners() {
        PlanetEventManager manager = new PlanetEventManager();
        List<PlanetEvent> received = new ArrayList<>();
        manager.registerListener(PlanetEventType.SETTLEMENT_FOUND, received::add);

        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 1, "settlement1", null);
        manager.dispatch(event);

        assertEquals(1, received.size());
        assertSame(event, received.get(0));
    }

    @Test
    void dispatch_toGlobalListeners() {
        PlanetEventManager manager = new PlanetEventManager();
        List<PlanetEvent> received = new ArrayList<>();
        manager.registerGlobalListener(received::add);

        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 1, "settlement1", null);
        manager.dispatch(event);

        assertEquals(1, received.size());
        assertSame(event, received.get(0));
    }

    @Test
    void dispatch_bothTypeAndGlobalListeners() {
        PlanetEventManager manager = new PlanetEventManager();
        List<PlanetEvent> typeReceived = new ArrayList<>();
        List<PlanetEvent> globalReceived = new ArrayList<>();
        manager.registerListener(PlanetEventType.SETTLEMENT_FOUND, typeReceived::add);
        manager.registerGlobalListener(globalReceived::add);

        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 1, "settlement1", null);
        manager.dispatch(event);

        assertEquals(1, typeReceived.size());
        assertEquals(1, globalReceived.size());
    }

    @Test
    void dispatch_nullEvent_isNoOp() {
        PlanetEventManager manager = new PlanetEventManager();
        List<PlanetEvent> received = new ArrayList<>();
        manager.registerGlobalListener(received::add);

        manager.dispatch(null);

        assertTrue(received.isEmpty());
    }

    @Test
    void dispatch_toWrongTypeListeners_noDelivery() {
        PlanetEventManager manager = new PlanetEventManager();
        List<PlanetEvent> received = new ArrayList<>();
        manager.registerListener(PlanetEventType.INDUSTRY_BUILT, received::add);

        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 1, "settlement1", null);
        manager.dispatch(event);

        assertTrue(received.isEmpty());
    }

    // ---- History ----

    @Test
    void dispatch_addsToHistory() {
        PlanetEventManager manager = new PlanetEventManager();
        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 1, "settlement1", null);
        manager.dispatch(event);

        List<PlanetEvent> history = manager.getEventHistory();
        assertEquals(1, history.size());
        assertSame(event, history.get(0));
    }

    @Test
    void getEventHistory_returnsUnmodifiableList() {
        PlanetEventManager manager = new PlanetEventManager();
        assertThrows(UnsupportedOperationException.class, () ->
                manager.getEventHistory().add(new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 1)));
    }

    @Test
    void history_cappedAtMaxHistorySize() {
        PlanetEventManager manager = new PlanetEventManager(3);
        manager.dispatch(new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 1));
        manager.dispatch(new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 2));
        manager.dispatch(new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 3));
        manager.dispatch(new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 4));

        List<PlanetEvent> history = manager.getEventHistory();
        assertEquals(3, history.size());
        assertEquals(2, history.get(0).getTimestamp()); // oldest evicted
        assertEquals(4, history.get(2).getTimestamp());
    }

    @Test
    void getEventsByType_filtersCorrectly() {
        PlanetEventManager manager = new PlanetEventManager();
        manager.dispatch(new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 1, "s1", null));
        manager.dispatch(new PlanetEvent(PlanetEventType.INDUSTRY_BUILT, 2, "i1", null));
        manager.dispatch(new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 3, "s3", null));

        List<PlanetEvent> settlementEvents = manager.getEventsByType(PlanetEventType.SETTLEMENT_FOUND);
        assertEquals(2, settlementEvents.size());
    }

    @Test
    void getEventsBySource_filtersCorrectly() {
        PlanetEventManager manager = new PlanetEventManager();
        manager.dispatch(new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 1, "source1", null));
        manager.dispatch(new PlanetEvent(PlanetEventType.INDUSTRY_BUILT, 2, "source2", null));
        manager.dispatch(new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 3, "source1", null));

        List<PlanetEvent> source1Events = manager.getEventsBySource("source1");
        assertEquals(2, source1Events.size());
    }

    // ---- Fire shortcuts ----

    @Test
    void fireEvent_withSourceAndPayload() {
        PlanetEventManager manager = new PlanetEventManager();
        List<PlanetEvent> received = new ArrayList<>();
        manager.registerGlobalListener(received::add);

        manager.fireEvent(PlanetEventType.SETTLEMENT_FOUND, 5, "settlement1", "payload");

        assertEquals(1, received.size());
        PlanetEvent event = received.get(0);
        assertEquals(PlanetEventType.SETTLEMENT_FOUND, event.getType());
        assertEquals(5, event.getTimestamp());
        assertEquals("settlement1", event.getSourceId());
        assertEquals("payload", event.getPayload());
    }

    @Test
    void fireEvent_withoutSource() {
        PlanetEventManager manager = new PlanetEventManager();
        List<PlanetEvent> received = new ArrayList<>();
        manager.registerGlobalListener(received::add);

        manager.fireEvent(PlanetEventType.BIOME_GENERATED, 10);

        assertEquals(1, received.size());
        PlanetEvent event = received.get(0);
        assertEquals(PlanetEventType.BIOME_GENERATED, event.getType());
        assertEquals(10, event.getTimestamp());
        assertNull(event.getSourceId());
    }

    // ---- Clear methods ----

    @Test
    void clearHistory_removesEventsButKeepsListeners() {
        PlanetEventManager manager = new PlanetEventManager();
        List<PlanetEvent> received = new ArrayList<>();
        manager.registerGlobalListener(received::add);

        manager.dispatch(new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 1));
        manager.clearHistory();

        assertTrue(manager.getEventHistory().isEmpty());
        assertEquals(1, manager.getListenerCount());
    }

    @Test
    void clearListeners_removesListenersButKeepsHistory() {
        PlanetEventManager manager = new PlanetEventManager();
        manager.registerGlobalListener(e -> {});
        manager.dispatch(new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 1));

        manager.clearListeners();

        assertEquals(0, manager.getListenerCount());
        assertEquals(1, manager.getEventHistory().size());
    }

    // ---- Edge cases ----

    @Test
    void defaultConstructor_maxHistorySize1000() {
        PlanetEventManager manager = new PlanetEventManager();
        for (int i = 0; i < 1001; i++) {
            manager.dispatch(new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, i));
        }
        assertEquals(1000, manager.getEventHistory().size());
    }

    @Test
    void listenerRemovedDuringDispatch_doesNotAffectNextIteration() {
        PlanetEventManager manager = new PlanetEventManager();
        List<PlanetEvent> received = new ArrayList<>();
        // Use an array holder so the lambda can reference itself
        @SuppressWarnings("unchecked")
        java.util.function.Consumer<PlanetEvent>[] holder = new java.util.function.Consumer[1];
        holder[0] = (event) -> {
            received.add(event);
            manager.unregisterGlobalListener(holder[0]);
        };
        manager.registerGlobalListener(holder[0]);
        manager.registerGlobalListener(received::add);

        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 1);
        manager.dispatch(event);

        assertEquals(2, received.size());
        assertEquals(1, manager.getListenerCount());
    }
}
