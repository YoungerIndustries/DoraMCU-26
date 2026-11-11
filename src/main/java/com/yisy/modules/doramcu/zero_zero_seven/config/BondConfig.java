package com.yisy.modules.doramcu.zero_zero_seven.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the 007 Cold War 4X RPG module.
 * <p>
 * Enables component scanning for the zero_zero_seven package so all
 * @Service, @Repository, @Component, and @Controller beans are auto-registered.
 * </p>
 */
@Configuration
@ComponentScan(basePackages = "com.yisy.modules.doramcu.zero_zero_seven")
public class BondConfig {
}
