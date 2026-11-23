# DoraMCU API Versioning Strategy

## Overview

DoraMCU uses **URI-based API versioning** to protect clients from breaking changes. Each major version lives under a distinct URI prefix:

- `/api/v1/...` — Original API (deprecated, sunset 2027-12-31)
- `/api/v2/...` — Current API (stable)

## Version Discovery

Clients can discover available API versions by calling:

```
GET /api/version
```

Response:
```json
{
  "versions": [
    {
      "version": "1",
      "status": "deprecated",
      "sunset": "Sun, 31 Dec 2027 00:00:00 GMT",
      "successorVersion": "2",
      "migrationGuide": "/api/v2/"
    },
    {
      "version": "2",
      "status": "current"
    }
  ],
  "currentVersion": "2",
  "deprecatedVersions": ["1"],
  "documentation": "/swagger-ui.html"
}
```

## Response Headers

Every API response includes version-related headers:

| Header | Description | Example |
|--------|-------------|---------|
| `X-Api-Version` | The API version that handled the request | `1` |
| `Deprecation` | `true` if the version is deprecated | `true` |
| `Sunset` | RFC 7231 date when the version will be removed | `Sun, 31 Dec 2027 00:00:00 GMT` |
| `Link` | Link to the successor version | `</api/v2/>; rel="successor-version"` |
| `Warning` | Shown for unknown API versions | `299 - "Unknown API version: 3"` |

## Deprecation Policy

1. **Minimum notice**: Deprecated versions receive at least 12 months notice before sunset.
2. **Sunset date**: Published in the `Sunset` response header and `/api/version` endpoint.
3. **Successor version**: Always available before a version is deprecated.
4. **Fail-open**: Unknown versions (e.g., `/api/v99/`) return a `Warning` header but are still processed, allowing forward compatibility.

## Version Lifecycle

```
Current → Deprecated → Sunset → Removed
```

- **Current**: Actively developed, no deprecation headers.
- **Deprecated**: `Deprecation: true` and `Sunset` headers added. Bug fixes only, no new features.
- **Sunset**: The sunset date has passed. The version may be removed in the next release.
- **Removed**: The version is no longer available. Requests return 410 Gone.

## Adding a New API Version

When introducing a new major version (e.g., v3):

### 1. Update `ApiDeprecationFilter.java`

Add the new version to `KNOWN_VERSIONS`:

```java
private static final Map<String, VersionInfo> KNOWN_VERSIONS = Map.of(
    "1", new VersionInfo("1", "sunset", "2027-12-31", "2"),
    "2", new VersionInfo("2", "deprecated", "2028-12-31", "3"),
    "3", new VersionInfo("3", "current", null, null)
);
```

### 2. Update `ApiVersionController.java`

Mirror the version metadata in the version info endpoint.

### 3. Create v3 Controllers

Create new controllers under `/api/v3/...` alongside the existing v1 controllers. The old controllers remain unchanged and continue to serve v1 traffic.

Example:
```java
@RestController
@RequestMapping("/api/v3/4x/sessions/{sessionId}")
public class GameControllerV3 {
    // New implementation with breaking changes
}
```

### 4. Update OpenAPI Config

Add path patterns for the new version in `OpenApiConfig.java`:

```java
.pathsToMatch("/api/v1/4x/**", "/api/v3/4x/**")
```

### 5. Update Swagger UI

The Swagger UI automatically picks up the new endpoints. Clients can switch between versions using the server selector.

## Migration Guide for Clients

### Detecting Deprecation

Check the `Deprecation` header in API responses:

```javascript
const response = await fetch('/api/v1/4x/sessions/abc123/state');
if (response.headers.get('Deprecation') === 'true') {
  const sunset = response.headers.get('Sunset');
  console.warn(`API v1 is deprecated. Sunset: ${sunset}`);
}
```

### Switching to a New Version

1. Replace `/api/v1/` with `/api/v2/` in all API calls.
2. Review the changelog for breaking changes.
3. Test thoroughly before deploying.

## Implementation Details

### Components

| File | Purpose |
|------|---------|
| `ApiDeprecationFilter.java` | Servlet filter that extracts version from URI and adds response headers |
| `ApiVersionController.java` | `GET /api/version` endpoint for version discovery |
| `OpenApiConfig.java` | OpenAPI/Swagger configuration with version metadata |

### Filter Order

`ApiDeprecationFilter` runs at `Ordered.HIGHEST_PRECEDENCE + 1` to ensure version headers are added before any other filter processes the response.

### Version Extraction

The filter uses a regex pattern `^/api/v(\d+)/` to extract the version number from the request URI. Paths that don't match this pattern (e.g., `/api/version`) are skipped.

## Changelog

| Date | Version | Change |
|------|---------|--------|
| 2026-11-21 | v1 | Initial versioning infrastructure. v1 marked deprecated, v2 marked current. |
