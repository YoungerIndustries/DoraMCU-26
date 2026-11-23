package com.yisy.modules.doramcu.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    // Endpoints excluded from rate limiting (real-time 3D UI, WebSocket, static resources, auth, news proxy)
    private static final String[] EXCLUDED_PREFIXES = {
            "/api/v1/4x/auth/",
            "/api/v1/galaxy-3d/",
            "/api/v1/planet-surface/",
            "/api/v1/resource-pipeline/",
            "/api/v1/achievements/",
            "/api/v1/events/",
            "/api/v1/clients/",
            "/api/v1/news/",
            "/ws/",
            "/css/",
            "/js/",
            "/clients/",
            "/corporation/",
            "/manifest.json",
            "/sw.js",
            "/config/",
            "/config.json",
            "/favicon.ico",
            "/icons/",
            "/index.html",
            "/game.html",
            "/earth2026.html",
            "/younger-hq.html",
            "/landing.html",
            "/health.html",
            "/traveller-rules.html",
            "/actuator/",
            "/static/"
    };

    private final RateLimiterService rateLimiterService;
    private final MetricsConfig metricsConfig;

    public RateLimitFilter(RateLimiterService rateLimiterService, MetricsConfig metricsConfig) {
        this.rateLimiterService = rateLimiterService;
        this.metricsConfig = metricsConfig;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // Skip rate limiting for excluded endpoints (3D UI, WebSocket, events)
        if (isExcluded(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);

        // Apply rate limiting - use stricter limits for login endpoints
        boolean isAllowed;
        if (uri.startsWith("/auth/login")) {
            isAllowed = rateLimiterService.isAllowedForLogin(clientIp);
        } else {
            isAllowed = rateLimiterService.isAllowed(clientIp);
        }

        if (!isAllowed) {
            log.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, uri);
            metricsConfig.getRateLimitRejectedCounter().increment();
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Rate limit exceeded. Please try again later.\"}"
            );
            return;
        }

        log.debug("Rate limit check passed for IP: {} on endpoint: {}", clientIp, uri);
        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Handle IPv6 format
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private boolean isExcluded(String uri) {
        for (String prefix : EXCLUDED_PREFIXES) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
