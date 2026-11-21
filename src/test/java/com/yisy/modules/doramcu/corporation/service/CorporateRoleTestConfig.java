package com.yisy.modules.doramcu.corporation.service;

import com.yisy.modules.doramcu.ai.PlayerCloneService;
import com.yisy.modules.doramcu.ai.framework.AiPlayerServiceAdapter;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.service.ai.AiPlayerService;
import com.yisy.modules.doramcu.repository.players.PlayerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Minimal Spring test configuration for corporate role integration tests.
 * <p>
 * Uses an H2 in-memory database with create-drop DDL auto. Mocks {@code PlayerCloneService}
 * and {@code AiPlayerService} since they have complex dependencies not needed for role
 * switching persistence tests.
 * </p>
 */
@Configuration
@PropertySource("classpath:application-test.properties")
@EnableJpaRepositories(
        basePackages = {"com.yisy.modules.doramcu.repository", "com.yisy.modules.doramcu.corporation.repository"},
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        PlayerRepository.class
                }
        )
)
@EnableTransactionManagement
@ComponentScan(
        basePackages = {"com.yisy.modules.doramcu.service", "com.yisy.modules.doramcu.corporation.service"},
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        CorporateRoleService.class
                }
        ),
        useDefaultFilters = false
)
public class CorporateRoleTestConfig {

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("role_testdb")
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.yisy.modules.doramcu.model", "com.yisy.modules.doramcu.corporation.model");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.show_sql", "false");
        /* H2 + Hibernate 6: use basic varchar types for enums to avoid native enum DDL */
        properties.put("hibernate.type.preferred_instant_jdbc_type", "TIMESTAMP");
        properties.put("jakarta.persistence.validation.mode", "none");
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }

    /**
     * Mock PlayerCloneService that returns a simple AI core.
     */
    @Bean
    public PlayerCloneService playerCloneService() {
        return new PlayerCloneService(null) {
            @Override
            public com.yisy.modules.doramcu.ai.EmpireAiCore createClone(int playerId,
                    com.yisy.modules.doramcu.hex.Player player, int researchLevel) {
                return new com.yisy.modules.doramcu.ai.EmpireAiCore(playerId, player, researchLevel);
            }
        };
    }

    /**
     * Mock AiPlayerService that does nothing.
     * CorporateRoleService stores this dependency but only uses it for logging,
     * so a simple no-op instance is sufficient.
     */
    @Bean
    public AiPlayerService aiPlayerService() {
        return new AiPlayerService(
                null, null, null, null, null, null, null, null);
    }

    @Bean
    public AiPlayerServiceAdapter aiPlayerServiceAdapter(AiPlayerService aiPlayerService) {
        return new AiPlayerServiceAdapter(aiPlayerService);
    }

    @Bean
    public CorporateRoleService corporateRoleService(
            PlayerRepository playerRepository,
            PlayerCloneService playerCloneService,
            AiPlayerService aiPlayerService,
            AiPlayerServiceAdapter aiPlayerServiceAdapter) {
        return new CorporateRoleService(
                playerRepository,
                playerCloneService,
                aiPlayerService,
                aiPlayerServiceAdapter
        );
    }
}
