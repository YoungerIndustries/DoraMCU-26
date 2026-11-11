package com.yisy.modules.doraneuron.security;

import com.yisy.modules.doraneuron.config.NeuronSecurityConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filters incoming HTTP requests and validates JWT tokens.
 *
 * This filter extracts the Authorization header, validates the JWT token,
 * and sets the authentication in the SecurityContext if valid.
 * Public endpoints (health, auth, portal) are excluded from authentication.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final NeuronSecurityConfig securityConfig;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                    NeuronSecurityConfig securityConfig) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.securityConfig = securityConfig;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Public endpoints — no authentication required
        return path.startsWith("/api/health") ||
                path.startsWith("/api/auth") ||
                path.startsWith("/") && !path.startsWith("/api/v1/neuron") ||
                path.startsWith("/actuator") ||
                path.startsWith("/ws/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            if (!shouldNotFilter(request)) {
                log.warn("Missing or invalid Authorization header for: {}", request.getRequestURI());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid token");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        if (!securityConfig.isJwtEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (jwtTokenProvider.validateToken(token)) {
                String nodeId = jwtTokenProvider.extractNodeId(token);

                // Create authentication token
                UserDetails userDetails = User.builder()
                        .username(nodeId)
                        .password("")
                        .authorities(List.of())
                        .build();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authenticated neuron: {}", nodeId);
            } else {
                log.warn("Invalid JWT token for: {}", request.getRequestURI());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }
        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
