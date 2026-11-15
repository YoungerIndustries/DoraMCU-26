package com.yisy.modules.doramcu.hex.planet.config;

import com.yisy.modules.doramcu.hex.planet.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spring configuration for Transport Tycoon planet surface building.
 * <p>
 * Provides shared instances of the planet surface builder and related services
 * so they can be injected into other Spring services like {@code PlanetBuilderBridge}.
 * </p>
 */
@Configuration
public class PlanetSurfaceConfig {

    /**
     * Provides a shared DefaultPlanetSurfaceBuilder instance (primary bean for both
     * the concrete type and the PlanetSurfaceBuilder interface).
     */
    @Bean
    @Primary
    public DefaultPlanetSurfaceBuilder defaultPlanetSurfaceBuilder() {
        return new DefaultPlanetSurfaceBuilder();
    }

    /**
     * Provides a shared TradeRouteManager instance for cargo flow management.
     */
    @Bean
    public TradeRouteManager tradeRouteManager() {
        TradeRouteManager manager = new TradeRouteManager();
        manager.setMarketShareTracker(marketShareTracker());
        return manager;
    }

    /**
     * Provides a shared MarketShareTracker instance for economic dominance tracking.
     */
    @Bean
    public MarketShareTracker marketShareTracker() {
        return new MarketShareTracker(60, 3);
    }
}
