package com.yisy.modules.doramcu.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Exposes API version information for client discovery.
 * <p>
 * Clients can call {@code GET /api/version} to discover available API versions,
 * their current status (current/deprecated/sunset), and migration guidance.
 * This endpoint is not versioned itself — it lives outside the versioned API paths.
 * </p>
 */
@RestController
@RequestMapping("/api")
public class ApiVersionController {

    private static final DateTimeFormatter HTTP_DATE = DateTimeFormatter.RFC_1123_DATE_TIME
            .withZone(ZoneOffset.UTC);

    /**
     * Returns information about all available API versions.
     * <p>
     * Response example:
     * </p>
     * <pre>{
     *   "versions": [
     *     { "version": "1", "status": "deprecated", "sunset": "Sun, 31 Dec 2027 00:00:00 GMT", "successorVersion": "2" },
     *     { "version": "2", "status": "current" }
     *   ],
     *   "currentVersion": "2",
     *   "deprecatedVersions": ["1"]
     * }</pre>
     *
     * @return version information map
     */
    @GetMapping("/version")
    public ResponseEntity<Map<String, Object>> getVersionInfo() {
        // Mirror the version metadata from ApiDeprecationFilter
        List<Map<String, Object>> versions = List.of(
                versionEntry("1", "deprecated", "2027-12-31", "2"),
                versionEntry("2", "current", null, null)
        );

        String currentVersion = "2";
        List<String> deprecatedVersions = List.of("1");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("versions", versions);
        response.put("currentVersion", currentVersion);
        response.put("deprecatedVersions", deprecatedVersions);
        response.put("documentation", "/swagger-ui.html");

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> versionEntry(String version, String status, String sunsetDate, String successorVersion) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("version", version);
        entry.put("status", status);
        if (sunsetDate != null) {
            Instant sunsetInstant = Instant.parse(sunsetDate + "T00:00:00Z");
            entry.put("sunset", HTTP_DATE.format(sunsetInstant));
        }
        if (successorVersion != null) {
            entry.put("successorVersion", successorVersion);
        }
        if ("deprecated".equals(status)) {
            entry.put("migrationGuide", "/api/v" + successorVersion + "/");
        }
        return entry;
    }
}
