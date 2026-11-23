package com.yisy.modules.doramcu.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts the API version from the request URI and adds version-related response headers.
 * <p>
 * Supports URI-based versioning: {@code /api/v1/...}, {@code /api/v2/...}, etc.
 * For each known version, the filter adds:
 * </p>
 * <ul>
 *   <li>{@code X-Api-Version} — the detected API version</li>
 *   <li>{@code Deprecation: true} — if the version is deprecated</li>
 *   <li>{@code Sunset} — RFC 7231 HTTP-date when the version will be removed</li>
 *   <li>{@code Link} — successor-version link header pointing to the next version</li>
 * </ul>
 * <p>
 * Unknown versions receive a warning header and are still processed (fail-open).
 * </p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@SuppressWarnings("UastIncorrectHttpHeaderInspection")
public class ApiDeprecationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiDeprecationFilter.class);

    private static final String VERSION_HEADER = "X-Api-Version";
    private static final String DEPRECATION_HEADER = "Deprecation";
    private static final String SUNSET_HEADER = "Sunset";
    private static final String LINK_HEADER = "Link";
    private static final String WARNING_HEADER = "Warning";

    /**
     * Regex to extract the version number from {@code /api/v{N}/...}.
     */
    private static final Pattern VERSION_PATTERN = Pattern.compile("^/api/v(\\d+)/");

    /**
     * Sunset date for v1 APIs: December 31, 2027 00:00:00 UTC.
     * Update this date when setting a new deprecation timeline.
     */
    private static final String V1_SUNSET_DATE = "2027-12-31";

    /**
     * Known API versions and their metadata.
     * Add new entries here when introducing a new API version.
     */
    private static final Map<String, VersionInfo> KNOWN_VERSIONS = Map.of(
            "1", new VersionInfo("1", "deprecated", V1_SUNSET_DATE, "2"),
            "2", new VersionInfo("2", "current", null, null)
    );

    /**
     * Metadata for a single API version.
     */
    private record VersionInfo(String version, String status, String sunsetDate, String successorVersion) {
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // Skip non-API paths
        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip /api/version endpoint itself
        if (path.equals("/api/version") || path.equals("/api/version/")) {
            filterChain.doFilter(request, response);
            return;
        }

        Matcher matcher = VERSION_PATTERN.matcher(path);
        if (!matcher.find()) {
            // No version in path — skip (e.g. /api/something without version)
            filterChain.doFilter(request, response);
            return;
        }

        String version = matcher.group(1);
        VersionInfo info = KNOWN_VERSIONS.get(version);

        if (info == null) {
            // Unknown version — warn but allow through (fail-open)
            log.warn("Unknown API version: {} for path: {}", version, path);
            response.setHeader(WARNING_HEADER, "299 - \"Unknown API version: " + version + "\"");
            response.setHeader(VERSION_HEADER, version);
            filterChain.doFilter(request, response);
            return;
        }

        // Always set the version header
        response.setHeader(VERSION_HEADER, info.version());

        // Add deprecation headers for deprecated versions
        if ("deprecated".equals(info.status()) || "sunset".equals(info.status())) {
            response.setHeader(DEPRECATION_HEADER, "true");
            if (info.sunsetDate() != null) {
                Instant sunsetInstant = Instant.parse(info.sunsetDate() + "T00:00:00Z");
                String sunsetFormatted = DateTimeFormatter.RFC_1123_DATE_TIME
                        .withZone(ZoneOffset.UTC)
                        .format(sunsetInstant);
                response.setHeader(SUNSET_HEADER, sunsetFormatted);
            }
        }

        // Add successor-version link if available
        if (info.successorVersion() != null) {
            response.setHeader(LINK_HEADER, "</api/v" + info.successorVersion() + "/>; rel=\"successor-version\"");
        }

        filterChain.doFilter(request, response);
    }
}
