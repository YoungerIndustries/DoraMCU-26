package com.yisy.modules.doraneuron.config;

import com.yisy.modules.doraneuron.security.JwtAuthenticationFilter;
import com.yisy.modules.doraneuron.security.RateLimiterFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security configuration for the Neuron node.
 *
 * This configuration enables JWT-based authentication, disables CSRF for REST APIs,
 * and configures the security filter chain with rate limiting and JWT validation.
 *
 * Only active when running the neuron standalone (not when integrated into DoraMCU).
 * The main DoraMCU app has its own SecurityConfig that handles security for all
 * neuron endpoints when the neuron is included as a subproject.
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(prefix = "neuron", name = "standalone", havingValue = "true", matchIfMissing = false)
public class NeuronWebSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimiterFilter rateLimiterFilter;

    public NeuronWebSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          RateLimiterFilter rateLimiterFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimiterFilter = rateLimiterFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints — no authentication required
                        .requestMatchers("/api/health", "/api/health/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/game/**").permitAll()
                        .requestMatchers("/static/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        // Unity client endpoints — public for game clients
                        .requestMatchers("/api/v1/unity/**").permitAll()
                        // Edge monitoring endpoints — public for health checks
                        .requestMatchers("/api/v1/neuron/edge/**").permitAll()
                        // Multi-game management endpoints — public for game discovery
                        .requestMatchers("/api/v1/game/list", "/api/v1/game/active",
                                "/api/v1/game/default", "/api/v1/game/status",
                                "/api/v1/game/{gameId}").permitAll()
                        // Player auth endpoints — public for login/registration
                        .requestMatchers("/api/v1/player/**").permitAll()
                        // Client distribution endpoints — public for PWA/extension delivery
                        .requestMatchers("/api/v1/clients/**").permitAll()
                        // Game sync endpoints — public for game node state synchronization
                        .requestMatchers("/api/v1/game-sync/**").permitAll()
                        // Unified endpoints — public for dashboard browsing
                        .requestMatchers("/api/v1/missions/**", "/api/v1/characters/**", "/api/v1/combat/**").permitAll()
                        // Corporation dashboard — public (auth checked client-side)
                        .requestMatchers("/corporation/").permitAll()
                        // Earth 2026 game page — public (auth checked client-side)
                        .requestMatchers("/earth2026/", "/earth2026").permitAll()
                        // PWA assets — public for SPA loading
                        .requestMatchers("/assets/**").permitAll()
                        .requestMatchers("/manifest.json").permitAll()
                        // All other API endpoints require authentication
                        .anyRequest().authenticated()
                )
                // Add filters before the standard authentication filter
                .addFilterBefore(rateLimiterFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
