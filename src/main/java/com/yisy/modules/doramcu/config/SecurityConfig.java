package com.yisy.modules.doramcu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Common security configuration shared between dev and prod profiles.
     * All controllers use /api/v1/* paths; legacy paths have been removed.
     */
    private void configureCommonSecurity(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/4x/auth/**", "/api/v1/4x/health/**", "/api/v1/4x/sectors/**").permitAll()
                        .requestMatchers("/api/v1/galaxy-3d/**", "/api/v1/planet-surface/**", "/api/v1/resource-pipeline/**", "/api/v1/planets/**", "/api/v1/achievements/**").permitAll()
                        .requestMatchers("/ws/fleet-tracker", "/ws/presence", "/ws/narrative", "/ws/resource-pipeline", "/ws/replay", "/ws/spectator", "/ws/achievements", "/ws/game-state").permitAll()
                        .requestMatchers("/api/v1/captains-log/**", "/api/v1/traveller/rules/**", "/api/v1/traveller/trade/**", "/api/v1/traveller/psionics/**", "/api/v1/mechwarrior/**").permitAll()
                        .requestMatchers("/api/v1/skins/**").permitAll()
                        .requestMatchers("/api/v1/onboarding/**").permitAll()
                        .requestMatchers("/api/v1/corporate/**").permitAll()
                        .requestMatchers("/api/v1/earth2026/**", "/earth2026/**", "/earth2026", "/earth2026-fps/", "/earth2026-tycoon/").permitAll()
                        .requestMatchers("/api/v1/fps/**", "/ws/fps").permitAll()
                        .requestMatchers("/api/v1/tycoon/**").permitAll()
                                                .requestMatchers("/api/v1/san-diego-map/**", "/san-diego-map/").permitAll()
                        .requestMatchers("/api/v1/google-maps/**").permitAll()
                        .requestMatchers("/api/v1/players/**").permitAll()
                        .requestMatchers("/api/v1/unified/**").permitAll()
                        .requestMatchers("/api/v1/news/**").permitAll()
                        .requestMatchers("/api/v1/clients/**").permitAll()
                        .requestMatchers("/api/v1/player/**").permitAll()
                        .requestMatchers("/api/v1/game-sync/**").permitAll()
                        .requestMatchers("/api/v1/missions/**", "/api/v1/characters/**", "/api/v1/combat/**").permitAll()
                        .requestMatchers("/ws/game-sync").permitAll()
                        .requestMatchers("/api/v1/game/**").permitAll()
                        .requestMatchers("/api/v1/ai-network/**").permitAll()
                        .requestMatchers("/api/v1/neuron/**", "/api/v1/co-server/**").permitAll()
                        .requestMatchers("/ws/neuron/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/", "/index.html", "/static/**", "/game.html", "/earth2026.html", "/younger-hq.html", "/landing.html", "/health.html", "/traveller-rules.html", "/css/**", "/js/**", "/favicon.ico", "/icons/**", "/manifest.json", "/sw.js", "/config/**", "/clients/**", "/config.json").permitAll()
                        .requestMatchers("/corporation/**", "/game/**").permitAll()
                        .requestMatchers("/api/v1/events/**").permitAll()
                        .requestMatchers("/api/v1/4x/characters").permitAll()
                        .requestMatchers("/promo.html", "/register.html", "/character.html").permitAll()
                        .requestMatchers("/api/v1/4x/galaxy/progress").permitAll()
                        .requestMatchers("/admin.html").authenticated()
                        .requestMatchers("/ws/**", "/ws/v1/**").authenticated()
                        .requestMatchers("/api/v1/4x/sessions/**").authenticated()
                        .requestMatchers("/api/v1/4x/admin/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Profile("dev")
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        configureCommonSecurity(http);
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        return http.build();
    }

    @Bean
    @Profile("!dev")
    public SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) throws Exception {
        configureCommonSecurity(http);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
