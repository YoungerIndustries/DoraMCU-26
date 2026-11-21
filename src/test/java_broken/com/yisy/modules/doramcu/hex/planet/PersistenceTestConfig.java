package com.yisy.modules.doramcu.hex.planet;

import com.yisy.modules.doramcu.repository.CampaignTradeRouteRepository;
import com.yisy.modules.doramcu.repository.PlanetCorporationRepository;
import com.yisy.modules.doramcu.repository.PlanetContractRepository;
import com.yisy.modules.doramcu.repository.PlanetIndustryRepository;
import com.yisy.modules.doramcu.repository.PlanetSettlementRepository;
import com.yisy.modules.doramcu.repository.PlanetStationRepository;
import com.yisy.modules.doramcu.repository.PlanetTerrainCellRepository;
import com.yisy.modules.doramcu.repository.PlanetVehicleRepository;
import com.yisy.modules.doramcu.repository.RpgQuestRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.support.ResourcePropertySource;
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
 * Minimal Spring test configuration for planet surface persistence integration tests.
 * <p>
 * Manually configures DataSource, EntityManagerFactory, and TransactionManager
 * to avoid loading the full Spring Boot application context.
 * </p>
 */
@Configuration
@PropertySource("classpath:application-test.properties")
@EnableJpaRepositories(
        basePackages = "com.yisy.modules.doramcu.repository",
        includeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = {
                        PlanetTerrainCellRepository.class,
                        PlanetSettlementRepository.class,
                        PlanetIndustryRepository.class,
                        PlanetStationRepository.class,
                        PlanetVehicleRepository.class,
                        PlanetCorporationRepository.class,
                        PlanetContractRepository.class,
                        RpgQuestRepository.class,
                        CampaignTradeRouteRepository.class
                }
        )
)
@EnableTransactionManagement
@ComponentScan(
        basePackages = "com.yisy.modules.doramcu.hex.planet",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        PlanetSurfacePersistenceService.class,
                        DefaultPlanetSurfaceBuilder.class
                }
        ),
        useDefaultFilters = false
)
public class PersistenceTestConfig {

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.yisy.modules.doramcu.model");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.show_sql", "false");
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
     * Creates the persistence service bean with all 5 repositories.
     */
    @Bean
    public PlanetSurfacePersistenceService planetSurfacePersistenceService(
            PlanetTerrainCellRepository terrainRepository,
            PlanetSettlementRepository settlementRepository,
            PlanetIndustryRepository industryRepository,
            PlanetStationRepository stationRepository,
            PlanetVehicleRepository vehicleRepository,
            PlanetCorporationRepository corporationRepository,
            PlanetContractRepository contractRepository,
            RpgQuestRepository questRepository,
            CampaignTradeRouteRepository tradeRouteRepository) {
        return new PlanetSurfacePersistenceService(
                terrainRepository,
                settlementRepository,
                industryRepository,
                stationRepository,
                vehicleRepository,
                corporationRepository,
                contractRepository,
                questRepository,
                tradeRouteRepository
        );
    }
}
