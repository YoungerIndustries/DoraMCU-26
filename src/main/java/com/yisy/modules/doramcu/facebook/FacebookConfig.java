package com.yisy.modules.doramcu.facebook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for Facebook Graph API integration.
 * <p>
 * Provides a RestTemplate bean tuned for Facebook Graph API calls with configurable
 * timeout. The Graph API base URL and timeout are configured via application properties.
 * </p>
 * <p>
 * Properties:
 * <ul>
 *   <li>{@code doramcu.facebook.enabled} (default: true) — enable/disable Facebook integration</li>
 *   <li>{@code doramcu.facebook.graph-api-url} (default: https://graph.facebook.com/v18.0)</li>
 *   <li>{@code doramcu.facebook.timeout-ms} (default: 10000) — HTTP timeout in milliseconds</li>
 * </ul>
 * </p>
 */
@Configuration
public class FacebookConfig {

    private static final Logger log = LoggerFactory.getLogger(FacebookConfig.class);

    @Value("${doramcu.facebook.enabled:true}")
    private boolean enabled;

    @Value("${doramcu.facebook.graph-api-url:https://graph.facebook.com/v18.0}")
    private String graphApiUrl;

    @Value("${doramcu.facebook.timeout-ms:10000}")
    private int timeoutMs;

    @Bean
    @ConditionalOnProperty(name = "doramcu.facebook.enabled", havingValue = "true", matchIfMissing = true)
    public RestTemplate facebookRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofMillis(timeoutMs);
        factory.setConnectTimeout((int) timeout.toMillis());
        factory.setReadTimeout((int) timeout.toMillis());

        RestTemplate restTemplate = new RestTemplate(factory);
        log.info("Facebook Graph API RestTemplate initialized with timeout: {}ms", timeoutMs);
        return restTemplate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getGraphApiUrl() {
        return graphApiUrl;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }
}
