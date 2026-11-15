package com.yisy.modules.doramcu.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ClientManifestController Tests")
class ClientManifestControllerTest {

    private final ClientManifestController controller = new ClientManifestController();

    // ---- manifest endpoint ----

    @Test
    @DisplayName("getClientManifest - should return 200 OK")
    void getClientManifest_shouldReturnOk() {
        ResponseEntity<Map<String, Object>> response = controller.getClientManifest();

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @DisplayName("getClientManifest - should return version")
    void getClientManifest_shouldReturnVersion() {
        ResponseEntity<Map<String, Object>> response = controller.getClientManifest();
        Map<String, Object> body = response.getBody();

        assertNotNull(body);
        assertTrue(body.containsKey("version"));
    }

    @Test
    @DisplayName("getClientManifest - should return clients list")
    void getClientManifest_shouldReturnClients() {
        ResponseEntity<Map<String, Object>> response = controller.getClientManifest();
        Map<String, Object> body = response.getBody();

        assertNotNull(body);
        assertTrue(body.containsKey("clients"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> clients = (List<Map<String, Object>>) body.get("clients");
        assertNotNull(clients);
        assertTrue(clients.size() > 0);
    }

    @Test
    @DisplayName("getClientManifest - should include Unity client")
    void getClientManifest_shouldIncludeUnityClient() {
        ResponseEntity<Map<String, Object>> response = controller.getClientManifest();
        Map<String, Object> body = response.getBody();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> clients = (List<Map<String, Object>>) body.get("clients");
        boolean hasUnity = clients.stream()
                .anyMatch(c -> "unity".equals(c.get("id")));
        assertTrue(hasUnity, "Manifest should contain Unity client");
    }

    @Test
    @DisplayName("getClientManifest - should include Electron client")
    void getClientManifest_shouldIncludeElectronClient() {
        ResponseEntity<Map<String, Object>> response = controller.getClientManifest();
        Map<String, Object> body = response.getBody();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> clients = (List<Map<String, Object>>) body.get("clients");
        boolean hasElectron = clients.stream()
                .anyMatch(c -> "electron".equals(c.get("id")));
        assertTrue(hasElectron, "Manifest should contain Electron client");
    }

    @Test
    @DisplayName("getClientManifest - should include Web client")
    void getClientManifest_shouldIncludeWebClient() {
        ResponseEntity<Map<String, Object>> response = controller.getClientManifest();
        Map<String, Object> body = response.getBody();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> clients = (List<Map<String, Object>>) body.get("clients");
        boolean hasWeb = clients.stream()
                .anyMatch(c -> "web".equals(c.get("id")));
        assertTrue(hasWeb, "Manifest should contain Web client");
    }

    @Test
    @DisplayName("getClientManifest - should include Mobile client")
    void getClientManifest_shouldIncludeMobileClient() {
        ResponseEntity<Map<String, Object>> response = controller.getClientManifest();
        Map<String, Object> body = response.getBody();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> clients = (List<Map<String, Object>>) body.get("clients");
        boolean hasMobile = clients.stream()
                .anyMatch(c -> "mobile".equals(c.get("id")));
        assertTrue(hasMobile, "Manifest should contain Mobile client");
    }

    @Test
    @DisplayName("getClientManifest - should include Tablet client")
    void getClientManifest_shouldIncludeTabletClient() {
        ResponseEntity<Map<String, Object>> response = controller.getClientManifest();
        Map<String, Object> body = response.getBody();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> clients = (List<Map<String, Object>>) body.get("clients");
        boolean hasTablet = clients.stream()
                .anyMatch(c -> "tablet".equals(c.get("id")));
        assertTrue(hasTablet, "Manifest should contain Tablet client");
    }

    @Test
    @DisplayName("getClientManifest - Unity client should have Windows platform")
    void getClientManifest_unityShouldHaveWindows() {
        ResponseEntity<Map<String, Object>> response = controller.getClientManifest();
        Map<String, Object> body = response.getBody();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> clients = (List<Map<String, Object>>) body.get("clients");
        Map<String, Object> unity = clients.stream()
                .filter(c -> "unity".equals(c.get("id")))
                .findFirst()
                .orElseThrow();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> platforms = (List<Map<String, Object>>) unity.get("platforms");
        boolean hasWindows = platforms.stream()
                .anyMatch(p -> "windows".equals(p.get("os")));
        assertTrue(hasWindows, "Unity client should support Windows");
    }

    @Test
    @DisplayName("getClientManifest - Unity client should have macOS platform")
    void getClientManifest_unityShouldHaveMacos() {
        ResponseEntity<Map<String, Object>> response = controller.getClientManifest();
        Map<String, Object> body = response.getBody();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> clients = (List<Map<String, Object>>) body.get("clients");
        Map<String, Object> unity = clients.stream()
                .filter(c -> "unity".equals(c.get("id")))
                .findFirst()
                .orElseThrow();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> platforms = (List<Map<String, Object>>) unity.get("platforms");
        boolean hasMacos = platforms.stream()
                .anyMatch(p -> "macos".equals(p.get("os")));
        assertTrue(hasMacos, "Unity client should support macOS");
    }

    @Test
    @DisplayName("getClientManifest - Unity client should have Linux platform")
    void getClientManifest_unityShouldHaveLinux() {
        ResponseEntity<Map<String, Object>> response = controller.getClientManifest();
        Map<String, Object> body = response.getBody();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> clients = (List<Map<String, Object>>) body.get("clients");
        Map<String, Object> unity = clients.stream()
                .filter(c -> "unity".equals(c.get("id")))
                .findFirst()
                .orElseThrow();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> platforms = (List<Map<String, Object>>) unity.get("platforms");
        boolean hasLinux = platforms.stream()
                .anyMatch(p -> "linux".equals(p.get("os")));
        assertTrue(hasLinux, "Unity client should support Linux");
    }

    @Test
    @DisplayName("getClientManifest - Unity Windows should have download URL")
    void getClientManifest_unityWindowsShouldHaveDownloadUrl() {
        ResponseEntity<Map<String, Object>> response = controller.getClientManifest();
        Map<String, Object> body = response.getBody();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> clients = (List<Map<String, Object>>) body.get("clients");
        Map<String, Object> unity = clients.stream()
                .filter(c -> "unity".equals(c.get("id")))
                .findFirst()
                .orElseThrow();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> platforms = (List<Map<String, Object>>) unity.get("platforms");
        Map<String, Object> windows = platforms.stream()
                .filter(p -> "windows".equals(p.get("os")))
                .findFirst()
                .orElseThrow();

        assertEquals("/api/v1/clients/download/unity/windows", windows.get("downloadUrl"));
    }

    @Test
    @DisplayName("getClientManifest - Unity Windows should have filename")
    void getClientManifest_unityWindowsShouldHaveFilename() {
        ResponseEntity<Map<String, Object>> response = controller.getClientManifest();
        Map<String, Object> body = response.getBody();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> clients = (List<Map<String, Object>>) body.get("clients");
        Map<String, Object> unity = clients.stream()
                .filter(c -> "unity".equals(c.get("id")))
                .findFirst()
                .orElseThrow();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> platforms = (List<Map<String, Object>>) unity.get("platforms");
        Map<String, Object> windows = platforms.stream()
                .filter(p -> "windows".equals(p.get("os")))
                .findFirst()
                .orElseThrow();

        assertEquals("DoraMCU-Setup.exe", windows.get("filename"));
    }

    @Test
    @DisplayName("getClientManifest - Web client should support all platforms")
    void getClientManifest_webShouldSupportAll() {
        ResponseEntity<Map<String, Object>> response = controller.getClientManifest();
        Map<String, Object> body = response.getBody();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> clients = (List<Map<String, Object>>) body.get("clients");
        Map<String, Object> web = clients.stream()
                .filter(c -> "web".equals(c.get("id")))
                .findFirst()
                .orElseThrow();

        assertEquals("/clients/desktop/", web.get("url"));
    }

    @Test
    @DisplayName("getClientManifest - Mobile client should have correct URL")
    void getClientManifest_mobileShouldHaveUrl() {
        ResponseEntity<Map<String, Object>> response = controller.getClientManifest();
        Map<String, Object> body = response.getBody();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> clients = (List<Map<String, Object>>) body.get("clients");
        Map<String, Object> mobile = clients.stream()
                .filter(c -> "mobile".equals(c.get("id")))
                .findFirst()
                .orElseThrow();

        assertEquals("/clients/mobile/", mobile.get("url"));
    }

    @Test
    @DisplayName("getClientManifest - Tablet client should have correct URL")
    void getClientManifest_tabletShouldHaveUrl() {
        ResponseEntity<Map<String, Object>> response = controller.getClientManifest();
        Map<String, Object> body = response.getBody();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> clients = (List<Map<String, Object>>) body.get("clients");
        Map<String, Object> tablet = clients.stream()
                .filter(c -> "tablet".equals(c.get("id")))
                .findFirst()
                .orElseThrow();

        assertEquals("/clients/tablet/", tablet.get("url"));
    }

    @Test
    @DisplayName("getClientManifest - each client should have name and description")
    void getClientManifest_eachClientShouldHaveNameAndDescription() {
        ResponseEntity<Map<String, Object>> response = controller.getClientManifest();
        Map<String, Object> body = response.getBody();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> clients = (List<Map<String, Object>>) body.get("clients");
        for (Map<String, Object> client : clients) {
            assertNotNull(client.get("name"), "Client should have a name");
            assertNotNull(client.get("description"), "Client should have a description");
            assertNotNull(client.get("id"), "Client should have an id");
            assertNotNull(client.get("type"), "Client should have a type");
        }
    }

    @Test
    @DisplayName("getClientManifest - should return 5 clients total")
    void getClientManifest_shouldReturnFiveClients() {
        ResponseEntity<Map<String, Object>> response = controller.getClientManifest();
        Map<String, Object> body = response.getBody();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> clients = (List<Map<String, Object>>) body.get("clients");
        assertEquals(5, clients.size(), "Should have Unity, Electron, Web, Mobile, Tablet");
    }

    // ---- download endpoint ----

    @Test
    @DisplayName("downloadClient - should return 404 when file not found")
    void downloadClient_shouldReturn404WhenNotFound() {
        ResponseEntity<?> response = controller.downloadClient("unity", "windows");

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    @DisplayName("downloadClient - should return 404 for unknown platform")
    void downloadClient_shouldReturn404ForUnknownPlatform() {
        ResponseEntity<?> response = controller.downloadClient("unknown", "windows");

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    @DisplayName("downloadClient - should return 404 for unknown OS")
    void downloadClient_shouldReturn404ForUnknownOS() {
        ResponseEntity<?> response = controller.downloadClient("unity", "unknown");

        assertEquals(404, response.getStatusCode().value());
    }
}
