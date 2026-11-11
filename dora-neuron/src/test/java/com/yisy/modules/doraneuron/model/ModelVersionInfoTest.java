package com.yisy.modules.doraneuron.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ModelVersionInfo Tests")
class ModelVersionInfoTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes null/zero defaults")
        void defaults() {
            ModelVersionInfo info = new ModelVersionInfo();
            assertEquals(0, info.getVersion());
            assertNull(info.getSha256Hash());
            assertNull(info.getStatus());
            assertNull(info.getDownloadUrl());
            assertEquals(0, info.getPreviousVersion());
            assertFalse(info.isRollback());
            assertEquals(0, info.getFileSize());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets version, sha256Hash, and status")
        void setsFields() {
            ModelVersionInfo info = new ModelVersionInfo(42L, "abc123", "ACTIVE");
            assertEquals(42L, info.getVersion());
            assertEquals("abc123", info.getSha256Hash());
            assertEquals("ACTIVE", info.getStatus());
            assertNull(info.getDownloadUrl());
            assertEquals(0, info.getPreviousVersion());
            assertFalse(info.isRollback());
            assertEquals(0, info.getFileSize());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get version")
        void version() {
            ModelVersionInfo info = new ModelVersionInfo();
            info.setVersion(100L);
            assertEquals(100L, info.getVersion());
        }

        @Test
        @DisplayName("Set and get sha256Hash")
        void sha256Hash() {
            ModelVersionInfo info = new ModelVersionInfo();
            info.setSha256Hash("def456");
            assertEquals("def456", info.getSha256Hash());
        }

        @Test
        @DisplayName("Set and get status")
        void status() {
            ModelVersionInfo info = new ModelVersionInfo();
            info.setStatus("TESTING");
            assertEquals("TESTING", info.getStatus());
        }

        @Test
        @DisplayName("Set and get downloadUrl")
        void downloadUrl() {
            ModelVersionInfo info = new ModelVersionInfo();
            info.setDownloadUrl("http://example.com/model.onnx");
            assertEquals("http://example.com/model.onnx", info.getDownloadUrl());
        }

        @Test
        @DisplayName("Set and get previousVersion")
        void previousVersion() {
            ModelVersionInfo info = new ModelVersionInfo();
            info.setPreviousVersion(41L);
            assertEquals(41L, info.getPreviousVersion());
        }

        @Test
        @DisplayName("Set and get rollback")
        void rollback() {
            ModelVersionInfo info = new ModelVersionInfo();
            info.setRollback(true);
            assertTrue(info.isRollback());
        }

        @Test
        @DisplayName("Set and get fileSize")
        void fileSize() {
            ModelVersionInfo info = new ModelVersionInfo();
            info.setFileSize(1024L);
            assertEquals(1024L, info.getFileSize());
        }
    }
}
