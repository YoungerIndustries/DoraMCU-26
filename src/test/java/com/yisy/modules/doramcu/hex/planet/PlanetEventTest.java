package com.yisy.modules.doramcu.hex.planet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlanetEventTest {

    // ---- Construction ----

    @Test
    void constructor_fourArgs_setsAllFields() {
        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 5, "settlement1", "payload");
        assertEquals(PlanetEventType.SETTLEMENT_FOUND, event.getType());
        assertEquals(5, event.getTimestamp());
        assertEquals("settlement1", event.getSourceId());
        assertEquals("payload", event.getPayload());
    }

    @Test
    void constructor_twoArgs_nullSourceAndPayload() {
        PlanetEvent event = new PlanetEvent(PlanetEventType.BIOME_GENERATED, 10);
        assertEquals(PlanetEventType.BIOME_GENERATED, event.getType());
        assertEquals(10, event.getTimestamp());
        assertNull(event.getSourceId());
        assertNull(event.getPayload());
    }

    @Test
    void constructor_nullType_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new PlanetEvent(null, 1));
    }

    @Test
    void constructor_nullType_fourArgs_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new PlanetEvent(null, 1, "source", "payload"));
    }

    // ---- Accessors ----

    @Test
    void getType_returnsType() {
        PlanetEvent event = new PlanetEvent(PlanetEventType.INDUSTRY_BUILT, 1);
        assertEquals(PlanetEventType.INDUSTRY_BUILT, event.getType());
    }

    @Test
    void getTimestamp_returnsTimestamp() {
        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 42);
        assertEquals(42, event.getTimestamp());
    }

    @Test
    void getSourceId_returnsSourceId() {
        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 1, "mySource", null);
        assertEquals("mySource", event.getSourceId());
    }

    @Test
    void getPayload_returnsPayload() {
        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 1, null, "myPayload");
        assertEquals("myPayload", event.getPayload());
    }

    // ---- hasSource / hasPayload ----

    @Test
    void hasSource_trueWhenPresent() {
        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 1, "source", null);
        assertTrue(event.hasSource());
    }

    @Test
    void hasSource_falseWhenNull() {
        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 1, null, null);
        assertFalse(event.hasSource());
    }

    @Test
    void hasPayload_trueWhenPresent() {
        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 1, null, "data");
        assertTrue(event.hasPayload());
    }

    @Test
    void hasPayload_falseWhenNull() {
        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 1, null, null);
        assertFalse(event.hasPayload());
    }

    // ---- getPayloadAs ----

    @Test
    void getPayloadAs_correctType_returnsValue() {
        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 1, null, "text");
        String result = event.getPayloadAs(String.class);
        assertEquals("text", result);
    }

    @Test
    void getPayloadAs_wrongType_throwsClassCastException() {
        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 1, null, "text");
        assertThrows(ClassCastException.class, () -> event.getPayloadAs(Integer.class));
    }

    @Test
    void getPayloadAs_nullPayload_returnsNull() {
        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 1, null, null);
        assertNull(event.getPayloadAs(String.class));
    }

    @Test
    void getPayloadAs_integerPayload() {
        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 1, null, 42);
        Integer result = event.getPayloadAs(Integer.class);
        assertEquals(42, result);
    }

    // ---- equals / hashCode ----

    @Test
    void equals_sameTypeTimestampSource_areEqual() {
        PlanetEvent e1 = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 5, "s1", "payload1");
        PlanetEvent e2 = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 5, "s1", "payload2");
        assertEquals(e1, e2); // payload is not part of equals
    }

    @Test
    void equals_differentType_notEqual() {
        PlanetEvent e1 = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 5, "s1", null);
        PlanetEvent e2 = new PlanetEvent(PlanetEventType.INDUSTRY_BUILT, 5, "s1", null);
        assertNotEquals(e1, e2);
    }

    @Test
    void equals_differentTimestamp_notEqual() {
        PlanetEvent e1 = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 5, "s1", null);
        PlanetEvent e2 = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 6, "s1", null);
        assertNotEquals(e1, e2);
    }

    @Test
    void equals_differentSourceId_notEqual() {
        PlanetEvent e1 = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 5, "s1", null);
        PlanetEvent e2 = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 5, "s2", null);
        assertNotEquals(e1, e2);
    }

    @Test
    void equals_null_notEqual() {
        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 5, "s1", null);
        assertNotEquals(null, event);
    }

    @Test
    void equals_sameObject_returnsTrue() {
        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 5, "s1", null);
        assertEquals(event, event);
    }

    @Test
    void hashCode_consistentWithEquals() {
        PlanetEvent e1 = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 5, "s1", "p1");
        PlanetEvent e2 = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 5, "s1", "p2");
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    // ---- toString ----

    @Test
    void toString_containsType() {
        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 5, "s1", null);
        String str = event.toString();
        assertTrue(str.contains("SETTLEMENT_FOUND"));
    }

    @Test
    void toString_containsTimestamp() {
        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 42, "s1", null);
        String str = event.toString();
        assertTrue(str.contains("42"));
    }

    @Test
    void toString_containsSourceId() {
        PlanetEvent event = new PlanetEvent(PlanetEventType.SETTLEMENT_FOUND, 5, "mySource", null);
        String str = event.toString();
        assertTrue(str.contains("mySource"));
    }
}
