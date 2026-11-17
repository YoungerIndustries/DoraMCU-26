package com.yisy.modules.doradl.config;

import com.yisy.modules.doradl.dl.service.DlGameAiService;
import com.yisy.modules.doradl.dl.service.DlGameAiServiceRest;
import com.yisy.modules.doradl.dto.GameStateResponse;
import com.yisy.modules.doradl.rest.DlRestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * DL service configuration after Phase 1 separation.
 * DL4J has been removed from the main server; all DL operations go through
 * the Python DL microservice via REST.
 * <p>
 * This configuration is disabled by default. Enable it by setting
 * {@code dl.rest-client.enabled=true} in application.properties.
 * </p>
 */
@Configuration
@ConditionalOnProperty(prefix = "dl.rest-client", name = "enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties(DlConfig.class)
public class DlRestConfig {

    @Bean
    public DlGameAiService dlGameAiService(DlRestClient restClient, DlConfig config) {
        return new DlGameAiServiceRest(restClient, config);
    }
}
