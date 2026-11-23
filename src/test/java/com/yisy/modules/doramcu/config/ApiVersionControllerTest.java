package com.yisy.modules.doramcu.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ApiVersionController Tests")
class ApiVersionControllerTest {

    private ApiVersionController controller;

    @BeforeEach
    void setUp() {
        controller = new ApiVersionController();
    }

    // ---- GET /api/version ----

    @Test
    @DisplayName("getVersionInfo - should return 200 OK")
    void getVersionInfo_shouldReturn200() {
        ResponseEntity<Map<String, Object>> response = controller.getVersionInfo();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("getVersionInfo - should return version list")
    void getVersionInfo_shouldReturnVersionList() {
        ResponseEntity<Map<String, Object>> response = controller.getVersionInfo();
        Map<String, Object> body = response.getBody();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> versions = (List<Map<String, Object>>) body.get("versions");
        assertNotNull(versions);
        assertEquals(2, versions.size());
    }

    @Test
    @DisplayName("getVersionInfo - v1 should be deprecated")
    void getVersionInfo_v1_shouldBeDeprecated() {
        ResponseEntity<Map<String, Object>> response = controller.getVersionInfo();
        Map<String, Object> body = response.getBody();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> versions = (List<Map<String, Object>>) body.get("versions");
        Map<String, Object> v1 = versions.get(0);

        assertEquals("1", v1.get("version"));
        assertEquals("deprecated", v1.get("status"));
        assertNotNull(v1.get("sunset"));
        assertEquals("2", v1.get("successorVersion"));
        assertNotNull(v1.get("migrationGuide"));
    }

    @Test
    @DisplayName("getVersionInfo - v2 should be current")
    void getVersionInfo_v2_shouldBeCurrent() {
        ResponseEntity<Map<String, Object>> response = controller.getVersionInfo();
        Map<String, Object> body = response.getBody();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> versions = (List<Map<String, Object>>) body.get("versions");
        Map<String, Object> v2 = versions.get(1);

        assertEquals("2", v2.get("version"));
        assertEquals("current", v2.get("status"));
        assertNull(v2.get("sunset"));
        assertNull(v2.get("successorVersion"));
    }

    @Test
    @DisplayName("getVersionInfo - should return current version")
    void getVersionInfo_shouldReturnCurrentVersion() {
        ResponseEntity<Map<String, Object>> response = controller.getVersionInfo();
        Map<String, Object> body = response.getBody();

        assertEquals("2", body.get("currentVersion"));
    }

    @Test
    @DisplayName("getVersionInfo - should return deprecated versions list")
    void getVersionInfo_shouldReturnDeprecatedVersions() {
        ResponseEntity<Map<String, Object>> response = controller.getVersionInfo();
        Map<String, Object> body = response.getBody();

        @SuppressWarnings("unchecked")
        List<String> deprecated = (List<String>) body.get("deprecatedVersions");
        assertNotNull(deprecated);
        assertEquals(1, deprecated.size());
        assertEquals("1", deprecated.get(0));
    }

    @Test
    @DisplayName("getVersionInfo - should return documentation link")
    void getVersionInfo_shouldReturnDocumentationLink() {
        ResponseEntity<Map<String, Object>> response = controller.getVersionInfo();
        Map<String, Object> body = response.getBody();

        assertEquals("/swagger-ui.html", body.get("documentation"));
    }

    @Test
    @DisplayName("getVersionInfo - sunset should be RFC 1123 format")
    void getVersionInfo_sunset_shouldBeRfc1123Format() {
        ResponseEntity<Map<String, Object>> response = controller.getVersionInfo();
        Map<String, Object> body = response.getBody();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> versions = (List<Map<String, Object>>) body.get("versions");
        Map<String, Object> v1 = versions.get(0);

        String sunset = (String) v1.get("sunset");
        // RFC 1123 format: "Tue, 31 Dec 2027 00:00:00 GMT" (Dec 31, 2027 is a Tuesday)
        assertTrue(sunset.contains("31 Dec 2027"));
        assertTrue(sunset.endsWith("GMT"));
    }
}
