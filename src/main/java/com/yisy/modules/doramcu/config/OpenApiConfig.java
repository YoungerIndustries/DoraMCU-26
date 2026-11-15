package com.yisy.modules.doramcu.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

/**
 * OpenAPI 3.0 configuration for DoraMCU API documentation.
 * <p>
 * Provides Swagger UI at /swagger-ui.html and OpenAPI JSON at /v3/api-docs.
 * Organizes all 100+ REST endpoints into logical groups for easy navigation.
 * </p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI doraMCUOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DoraMCU API")
                        .version("1.0.0")
                        .description("Multi-game platform API supporting Traveller, Star Trek 4X, Babylon 5, " +
                                "Star Fleet Battles, D&D 5E, MechWarrior, Star Wars, Starship Troopers, " +
                                "Third Reich, 007, Earth 2026, and more. Features include galaxy generation, " +
                                "game sessions, deep learning, real-time communication, modding support, " +
                                "and community server hosting.")
                        .contact(new Contact()
                                .name("DoraMCU Team")
                                .url("https://github.com/YISY/DoraMCU"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development"),
                        new Server().url("https://api.doramcu.local").description("Production")
                ));
    }

    // --- API Group Definitions ---
    // Each group exposes a subset of controllers for organized documentation

    @Bean
    public GroupedOpenApi corePlatformApi() {
        return GroupedOpenApi.builder()
                .group("1-core-platform")
                .pathsToMatch(
                        "/api/v1/4x/**",
                        "/api/v1/game/**",
                        "/api/v1/sessions/**",
                        "/api/v1/save/**",
                        "/api/v1/replay/**",
                        "/api/v1/chat/**",
                        "/api/v1/tts/**",
                        "/api/v1/spectator/**",
                        "/api/v1/voice-chat/**",
                        "/api/v1/galaxy-3d/**",
                        "/api/v1/gamemaster/**",
                        "/api/v1/community-servers/**",
                        "/api/v1/cross-game/**",
                        "/ws/v1/**")
                .build();
    }

    @Bean
    public GroupedOpenApi travellerApi() {
        return GroupedOpenApi.builder()
                .group("2-traveller")
                .pathsToMatch(
                        "/api/v1/traveller/**",
                        "/api/v1/psionic/**",
                        "/api/v1/smuggling/**",
                        "/api/v1/alien-races/**")
                .build();
    }

    @Bean
    public GroupedOpenApi starTrekApi() {
        return GroupedOpenApi.builder()
                .group("3-star-trek-4x")
                .pathsToMatch(
                        "/api/v1/highguard/**",
                        "/api/v1/diplomacy/**",
                        "/api/v1/expansion/**",
                        "/api/v1/exploitation/**",
                        "/api/v1/exploration/**")
                .build();
    }

    @Bean
    public GroupedOpenApi sfbApi() {
        return GroupedOpenApi.builder()
                .group("4-star-fleet-battles")
                .pathsToMatch("/api/v1/sfb/**")
                .build();
    }

    @Bean
    public GroupedOpenApi gameModulesApi() {
        return GroupedOpenApi.builder()
                .group("5-game-modules")
                .pathsToMatch(
                        "/api/v1/starwars/**",
                        "/api/v1/starshiptroopers/**",
                        "/api/v1/mechwarrior/**",
                        "/api/v1/thirdreich/**",
                        "/api/v1/zero-zero-seven/**",
                        "/api/v1/dnd5e/**",
                        "/api/v1/b5/**",
                        "/api/v1/earth2026/**",
                        "/api/v1/fps/**")
                .build();
    }

    @Bean
    public GroupedOpenApi galaxyApi() {
        return GroupedOpenApi.builder()
                .group("6-galaxy")
                .pathsToMatch(
                        "/api/v1/galaxy/**",
                        "/api/v1/sector/**",
                        "/api/v1/planet/**",
                        "/api/v1/station/**")
                .build();
    }

    @Bean
    public GroupedOpenApi aiAndModsApi() {
        return GroupedOpenApi.builder()
                .group("7-ai-and-mods")
                .pathsToMatch(
                        "/api/v1/ai/**",
                        "/api/v1/mods/**",
                        "/api/v1/marketplace/**",
                        "/api/v1/admin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authenticationApi() {
        return GroupedOpenApi.builder()
                .group("8-authentication")
                .pathsToMatch(
                        "/api/v1/auth/**",
                        "/api/v1/device/**",
                        "/api/v1/onboarding/**")
                .build();
    }

    @Bean
    public GroupedOpenApi corporationApi() {
        return GroupedOpenApi.builder()
                .group("9-corporation")
                .pathsToMatch("/api/v1/corporation/**")
                .build();
    }

    @Bean
    public GroupedOpenApi neuronApi() {
        return GroupedOpenApi.builder()
                .group("10-neuron")
                .pathsToMatch("/api/v1/neuron/**")
                .build();
    }
}
