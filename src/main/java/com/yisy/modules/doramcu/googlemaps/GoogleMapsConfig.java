package com.yisy.modules.doramcu.googlemaps;

import com.google.maps.GeoApiContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for Google Maps API integration.
 * <p>
 * Provides the GeoApiContext bean used by all Google Maps API calls.
 * The API key is configured via application properties: google.maps.api-key
 * </p>
 */
@Configuration
public class GoogleMapsConfig {

    private static final Logger log = LoggerFactory.getLogger(GoogleMapsConfig.class);

    @Value("${google.maps.api-key:}")
    private String apiKey;

    @Value("${google.maps.enabled:false}")
    private boolean enabled;

    @Bean
    @ConditionalOnProperty(name = "google.maps.enabled", havingValue = "true", matchIfMissing = false)
    public GeoApiContext geoApiContext() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Google Maps API is enabled but no API key is configured. " +
                    "Set google.maps.api-key in application.properties.");
            return null;
        }

        log.info("Initializing Google Maps API context with key: {}...", 
                apiKey.substring(0, Math.min(10, apiKey.length())));

        return new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    public boolean isEnabled() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    public String getApiKey() {
        return apiKey;
    }
}
