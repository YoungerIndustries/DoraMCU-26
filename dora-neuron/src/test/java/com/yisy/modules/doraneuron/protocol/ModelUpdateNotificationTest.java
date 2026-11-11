package com.yisy.modules.doraneuron.protocol;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ModelUpdateNotification Tests")
class ModelUpdateNotificationTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes null/zero defaults")
        void defaults() {
            ModelUpdateNotification notification = new ModelUpdateNotification();
            assertEquals(0, notification.getVersion());
            assertNull(notification.getUrl());
            assertNull(notification.getSha256Hash());
            assertNull(notification.getModelSignature());
            assertEquals(0, notification.getPreviousVersion());
            assertFalse(notification.isRollback());
        }
    }

    @Nested
    @DisplayName("3-arg constructor")
    class Constructor3Arg {

        @Test
        @DisplayName("Sets version, url, and sha256Hash")
        void setsFields() {
            ModelUpdateNotification notification = new ModelUpdateNotification(42L, "http://example.com/model.onnx", "abc123");
            assertEquals(42L, notification.getVersion());
            assertEquals("http://example.com/model.onnx", notification.getUrl());
            assertEquals("abc123", notification.getSha256Hash());
            assertNull(notification.getModelSignature());
            assertEquals(0, notification.getPreviousVersion());
            assertFalse(notification.isRollback());
        }
    }

    @Nested
    @DisplayName("4-arg constructor")
    class Constructor4Arg {

        @Test
        @DisplayName("Sets version, url, sha256Hash, and modelSignature")
        void setsFields() {
            ModelUpdateNotification notification = new ModelUpdateNotification(42L, "http://example.com/model.onnx", "abc123", "sig456");
            assertEquals(42L, notification.getVersion());
            assertEquals("http://example.com/model.onnx", notification.getUrl());
            assertEquals("abc123", notification.getSha256Hash());
            assertEquals("sig456", notification.getModelSignature());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get version")
        void version() {
            ModelUpdateNotification notification = new ModelUpdateNotification();
            notification.setVersion(100L);
            assertEquals(100L, notification.getVersion());
        }

        @Test
        @DisplayName("Set and get url")
        void url() {
            ModelUpdateNotification notification = new ModelUpdateNotification();
            notification.setUrl("http://new-url.com/model.onnx");
            assertEquals("http://new-url.com/model.onnx", notification.getUrl());
        }

        @Test
        @DisplayName("Set and get sha256Hash")
        void sha256Hash() {
            ModelUpdateNotification notification = new ModelUpdateNotification();
            notification.setSha256Hash("def456");
            assertEquals("def456", notification.getSha256Hash());
        }

        @Test
        @DisplayName("Set and get modelSignature")
        void modelSignature() {
            ModelUpdateNotification notification = new ModelUpdateNotification();
            notification.setModelSignature("sig789");
            assertEquals("sig789", notification.getModelSignature());
        }

        @Test
        @DisplayName("Set and get previousVersion")
        void previousVersion() {
            ModelUpdateNotification notification = new ModelUpdateNotification();
            notification.setPreviousVersion(41L);
            assertEquals(41L, notification.getPreviousVersion());
        }

        @Test
        @DisplayName("Set and get rollback")
        void rollback() {
            ModelUpdateNotification notification = new ModelUpdateNotification();
            notification.setRollback(true);
            assertTrue(notification.isRollback());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("toString includes version, url, sha256Hash, modelSignature, previousVersion, and isRollback")
        void toStringContent() {
            ModelUpdateNotification notification = new ModelUpdateNotification(42L, "http://example.com/model.onnx", "abc123", "sig456");
            notification.setPreviousVersion(41L);
            notification.setRollback(true);
            String result = notification.toString();
            assertTrue(result.contains("version=42"));
            assertTrue(result.contains("http://example.com/model.onnx"));
            assertTrue(result.contains("abc123"));
            assertTrue(result.contains("sig456"));
            assertTrue(result.contains("previousVersion=41"));
            assertTrue(result.contains("isRollback=true"));
        }
    }
}
