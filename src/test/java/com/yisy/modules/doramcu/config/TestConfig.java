package com.yisy.modules.doramcu.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Shared test configuration that provides beans needed by integration tests.
 *
 * Spring Boot 4.x uses Jackson JSON (tools.jackson.databind.json.JsonMapper) by default,
 * but some components still depend on the legacy com.fasterxml.jackson.databind.ObjectMapper.
 * This configuration bridges that gap for the test profile.
 */
@Configuration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
