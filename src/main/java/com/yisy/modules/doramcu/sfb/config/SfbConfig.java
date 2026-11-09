package com.yisy.modules.doramcu.sfb.config;

import com.yisy.modules.doramcu.sfb.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Spring configuration for SFB module.
 */
@Configuration
@EnableWebSocket
public class SfbConfig implements org.springframework.web.socket.config.annotation.WebSocketConfigurer {

    private final SfbWebSocketHandler webSocketHandler;

    public SfbConfig(SfbWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws/sfb")
                .setAllowedOrigins("*");
    }

    @Bean
    public SfbCombatEngine sfbCombatEngine(ImpulseManager impulseManager, 
                                           EnergyAllocationService energyAllocationService,
                                           MovementService movementService,
                                           WeaponsFireService weaponsFireService,
                                           DamageAllocationService damageAllocationService,
                                           SpecialSystemsService specialSystemsService,
                                           SfbAIService aiService,
                                           SfbRoleSystem roleSystem,
                                           SfbCharacterBridge characterBridge) {
        return new SfbCombatEngine(impulseManager, energyAllocationService, movementService,
                weaponsFireService, damageAllocationService, specialSystemsService,
                aiService, roleSystem, characterBridge);
    }

    @Bean
    public ImpulseManager impulseManager() {
        return new ImpulseManager();
    }

    @Bean
    public EnergyAllocationService energyAllocationService() {
        return new EnergyAllocationService();
    }

    @Bean
    public MovementService movementService(ImpulseManager impulseManager) {
        return new MovementService(impulseManager);
    }

    @Bean
    public SfbCampaignEngine sfbCampaignEngine() {
        return new SfbCampaignEngine();
    }

    @Bean
    public SfbCharacterBridge sfbCharacterBridge() {
        return new SfbCharacterBridge();
    }

    @Bean
    public SfbRoleSystem sfbRoleSystem(SfbCharacterBridge characterBridge) {
        return new SfbRoleSystem(characterBridge);
    }

    @Bean
    public SfbGalaxyIntegration sfbGalaxyIntegration() {
        return new SfbGalaxyIntegration();
    }

    @Bean
    public SfbAIService sfbAIService() {
        return new SfbAIService();
    }

    @Bean
    public SpecialSystemsService specialSystemsService() {
        return new SpecialSystemsService();
    }

    @Bean
    public WeaponsFireService weaponsFireService() {
        return new WeaponsFireService();
    }

    @Bean
    public DamageAllocationService damageAllocationService() {
        return new DamageAllocationService();
    }

    @Bean
    public SfbWebSocketHandler sfbWebSocketHandler(SfbCombatEngine combatEngine) {
        return new SfbWebSocketHandler(combatEngine);
    }
}
