package com.yisy.modules.doramcu.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApiDeprecationFilter Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class ApiDeprecationFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private ApiDeprecationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new ApiDeprecationFilter();
    }

    // ---- v1 deprecated version ----

    @Test
    @DisplayName("doFilterInternal - v1 should add deprecation headers")
    void doFilterInternal_v1_shouldAddDeprecationHeaders() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/4x/sessions/abc123/state");

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader("X-Api-Version", "1");
        verify(response).setHeader("Deprecation", "true");
        verify(response).setHeader("Sunset", "Fri, 31 Dec 2027 00:00:00 GMT");
        verify(response).setHeader("Link", "</api/v2/>; rel=\"successor-version\"");
        verify(filterChain).doFilter(request, response);
    }

    // ---- v2 current version ----

    @Test
    @DisplayName("doFilterInternal - v2 should only add version header")
    void doFilterInternal_v2_shouldOnlyAddVersionHeader() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v2/4x/sessions/abc123/state");

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader("X-Api-Version", "2");
        verify(filterChain).doFilter(request, response);
    }

    // ---- Unknown version ----

    @Test
    @DisplayName("doFilterInternal - unknown version should add warning header")
    void doFilterInternal_unknownVersion_shouldAddWarning() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v99/something");

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader("Warning", "299 - \"Unknown API version: 99\"");
        verify(response).setHeader("X-Api-Version", "99");
        verify(filterChain).doFilter(request, response);
    }

    // ---- Non-API path ----

    @Test
    @DisplayName("doFilterInternal - non-API path should skip")
    void doFilterInternal_nonApiPath_shouldSkip() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/swagger-ui.html");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    // ---- /api/version endpoint ----

    @Test
    @DisplayName("doFilterInternal - /api/version should skip")
    void doFilterInternal_apiVersion_shouldSkip() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/version");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    // ---- /api/version/ with trailing slash ----

    @Test
    @DisplayName("doFilterInternal - /api/version/ should skip")
    void doFilterInternal_apiVersionTrailingSlash_shouldSkip() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/version/");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    // ---- /api/ without version ----

    @Test
    @DisplayName("doFilterInternal - /api/something without version should skip")
    void doFilterInternal_apiWithoutVersion_shouldSkip() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/something");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    // ---- Various v1 paths ----

    @Test
    @DisplayName("doFilterInternal - v1 traveller path should add deprecation headers")
    void doFilterInternal_v1Traveller_shouldAddDeprecationHeaders() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/traveller/sectors");

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader("X-Api-Version", "1");
        verify(response).setHeader("Deprecation", "true");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal - v1 auth path should add deprecation headers")
    void doFilterInternal_v1Auth_shouldAddDeprecationHeaders() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader("X-Api-Version", "1");
        verify(response).setHeader("Deprecation", "true");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal - v1 admin path should add deprecation headers")
    void doFilterInternal_v1Admin_shouldAddDeprecationHeaders() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/admin/status");

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader("X-Api-Version", "1");
        verify(response).setHeader("Deprecation", "true");
        verify(filterChain).doFilter(request, response);
    }
}
