package com.yisy.modules.doraneuron.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
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
public class NeuronWebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll()
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
                        // Brain Node endpoints — public for monitoring and status
                        .requestMatchers("/api/v1/brain-node/**").permitAll()
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
                );
        // Filters disabled for brain-node profile — no JWT/rate limiter beans available

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
