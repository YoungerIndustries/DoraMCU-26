package com.yisy.modules.doramcu.sfb.config;

import com.yisy.modules.doramcu.sfb.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for SFB module.
 */
@Configuration
public class SfbConfig {

    @Bean
    public SfbCombatEngine sfbCombatEngine() {
        return new SfbCombatEngine();
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
}
