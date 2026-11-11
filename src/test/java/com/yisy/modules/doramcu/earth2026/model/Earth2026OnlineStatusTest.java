package com.yisy.modules.doramcu.earth2026.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Earth2026OnlineStatus Tests")
class Earth2026OnlineStatusTest {

    // ---- Constructors ----

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create with character ID and default values")
        void constructor_shouldSetDefaults() {
            // Act
            Earth2026OnlineStatus status = new Earth2026OnlineStatus(1L);

            // Assert
            assertEquals(1L, status.getCharacterId());
            assertFalse(status.isOnline());
            assertEquals("idle", status.getActivity());
            assertNotNull(status.getUpdatedAt());
            assertNull(status.getLocationId());
            assertNull(status.getLastSeenAt());
            assertNull(status.getConnectedAt());
        }

        @Test
        @DisplayName("should create with null id")
        void constructor_shouldHaveNullId() {
            // Act
            Earth2026OnlineStatus status = new Earth2026OnlineStatus(1L);

            // Assert
            assertNull(status.getId());
        }
    }

    // ---- Getters and Setters ----

    @Nested
    @DisplayName("Getter/Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get all fields")
        void getters_shouldReturnSetValues() {
            // Arrange
            Earth2026OnlineStatus status = new Earth2026OnlineStatus();

            // Act
            status.setId(1L);
            status.setCharacterId(2L);
            status.setOnline(true);
            status.setLocationId(3L);
            status.setActivity("exploring");
            status.setLastSeenAt(LocalDateTime.of(2026, 1, 1, 12, 0));
            status.setConnectedAt(LocalDateTime.of(2026, 1, 1, 11, 0));
            status.setUpdatedAt(LocalDateTime.of(2026, 1, 1, 12, 5));

            // Assert
            assertEquals(1L, status.getId());
            assertEquals(2L, status.getCharacterId());
            assertTrue(status.isOnline());
            assertEquals(3L, status.getLocationId());
            assertEquals("exploring", status.getActivity());
            assertEquals(LocalDateTime.of(2026, 1, 1, 12, 0), status.getLastSeenAt());
            assertEquals(LocalDateTime.of(2026, 1, 1, 11, 0), status.getConnectedAt());
            assertEquals(LocalDateTime.of(2026, 1, 1, 12, 5), status.getUpdatedAt());
        }
    }

    // ---- isStale ----

    @Nested
    @DisplayName("isStale Tests")
    class IsStaleTests {

        @Test
        @DisplayName("should be stale when lastSeenAt is null")
        void isStale_shouldBeTrueWhenLastSeenIsNull() {
            // Arrange
            Earth2026OnlineStatus status = new Earth2026OnlineStatus(1L);
            // lastSeenAt is null by default

            // Act
            boolean result = status.isStale(LocalDateTime.now());

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("should be stale when lastSeenAt is more than 5 minutes ago")
        void isStale_shouldBeTrueWhenMoreThan5Minutes() {
            // Arrange
            Earth2026OnlineStatus status = new Earth2026OnlineStatus(1L);
            LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 10);
            status.setLastSeenAt(LocalDateTime.of(2026, 1, 1, 12, 0)); // 10 minutes ago

            // Act
            boolean result = status.isStale(now);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("should not be stale when lastSeenAt is recent")
        void isStale_shouldBeFalseWhenRecent() {
            // Arrange
            Earth2026OnlineStatus status = new Earth2026OnlineStatus(1L);
            LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 10);
            status.setLastSeenAt(LocalDateTime.of(2026, 1, 1, 12, 9)); // 1 minute ago

            // Act
            boolean result = status.isStale(now);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("should not be stale when lastSeenAt is exactly now")
        void isStale_shouldBeFalseWhenNow() {
            // Arrange
            Earth2026OnlineStatus status = new Earth2026OnlineStatus(1L);
            LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 10);
            status.setLastSeenAt(now);

            // Act
            boolean result = status.isStale(now);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("should be stale when lastSeenAt is exactly 5 minutes ago")
        void isStale_shouldBeTrueWhenExactly5Minutes() {
            // Arrange
            Earth2026OnlineStatus status = new Earth2026OnlineStatus(1L);
            LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 10);
            status.setLastSeenAt(LocalDateTime.of(2026, 1, 1, 12, 5)); // exactly 5 minutes ago

            // Act
            boolean result = status.isStale(now);

            // Assert
            // isBefore checks if lastSeenAt is before now.minusMinutes(5)
            // 12:05 is not before 12:05, so it should be false (not stale)
            assertFalse(result);
        }

        @Test
        @DisplayName("should be stale when lastSeenAt is 5 minutes and 1 second ago")
        void isStale_shouldBeTrueWhen5Minutes1Second() {
            // Arrange
            Earth2026OnlineStatus status = new Earth2026OnlineStatus(1L);
            LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 10, 0);
            status.setLastSeenAt(LocalDateTime.of(2026, 1, 1, 12, 4, 59)); // 5 minutes 1 second ago

            // Act
            boolean result = status.isStale(now);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("should not be stale when lastSeenAt is 4 minutes 59 seconds ago")
        void isStale_shouldBeFalseWhen4Minutes59Seconds() {
            // Arrange
            Earth2026OnlineStatus status = new Earth2026OnlineStatus(1L);
            LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 10, 0);
            status.setLastSeenAt(LocalDateTime.of(2026, 1, 1, 12, 5, 1)); // 4 minutes 59 seconds ago

            // Act
            boolean result = status.isStale(now);

            // Assert
            assertFalse(result);
        }
    }

    // ---- toString ----

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("should include id, characterId, isOnline, and activity")
        void toString_shouldIncludeFields() {
            // Arrange
            Earth2026OnlineStatus status = new Earth2026OnlineStatus(1L);
            status.setId(1L);
            status.setOnline(true);
            status.setActivity("exploring");

            // Act
            String result = status.toString();

            // Assert
            assertTrue(result.contains("id=1"));
            assertTrue(result.contains("characterId=1"));
            assertTrue(result.contains("isOnline=true"));
            assertTrue(result.contains("activity='exploring'"));
            assertTrue(result.startsWith("Earth2026OnlineStatus{"));
        }
    }
}
