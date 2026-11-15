package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.repository.ApartmentBuildingRepository;
import com.yisy.modules.doramcu.earth2026.repository.ApartmentUnitRepository;
import com.yisy.modules.doramcu.earth2026.repository.DungeonInstanceRepository;
import com.yisy.modules.doramcu.earth2026.repository.DungeonInstanceTemplateRepository;
import com.yisy.modules.doramcu.earth2026.repository.OfficeUnitRepository;
import com.yisy.modules.doramcu.earth2026.repository.PropertyListingRepository;
import com.yisy.modules.doramcu.earth2026.repository.PropertyTransactionRepository;
import com.yisy.modules.doramcu.earth2026.repository.YoungerIndustriesHQRepository;
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
 * Minimal Spring test configuration for real estate integration tests.
 * <p>
 * Manually configures DataSource, EntityManagerFactory, and TransactionManager
 * to avoid loading the full Spring Boot application context.
 * </p>
 */
@Configuration
@PropertySource("classpath:application-test.properties")
@EnableJpaRepositories(
        basePackages = "com.yisy.modules.doramcu.earth2026.repository",
        includeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = {
                        YoungerIndustriesHQRepository.class,
                        ApartmentBuildingRepository.class,
                        OfficeUnitRepository.class,
                        ApartmentUnitRepository.class,
                        PropertyListingRepository.class,
                        PropertyTransactionRepository.class,
                        DungeonInstanceRepository.class,
                        DungeonInstanceTemplateRepository.class
                }
        )
)
@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan(
        basePackages = "com.yisy.modules.doramcu.earth2026.service",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        Earth2026RealEstateService.class,
                        Earth2026BuildingInstanceService.class
                }
        ),
        useDefaultFilters = false
)
public class RealEstateTestConfig {

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
        em.setPackagesToScan("com.yisy.modules.doramcu.earth2026.model");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }

    @Bean
    public Earth2026RealEstateService earth2026RealEstateService(
            PropertyListingRepository listingRepository,
            PropertyTransactionRepository transactionRepository,
            OfficeUnitRepository officeUnitRepository,
            ApartmentUnitRepository apartmentUnitRepository,
            YoungerIndustriesHQRepository hqRepository,
            ApartmentBuildingRepository apartmentBuildingRepository) {
        return new Earth2026RealEstateService(
                listingRepository,
                transactionRepository,
                officeUnitRepository,
                apartmentUnitRepository,
                hqRepository,
                apartmentBuildingRepository
        );
    }

    @Bean
    public Earth2026BuildingInstanceService earth2026BuildingInstanceService(
            YoungerIndustriesHQRepository hqRepository,
            ApartmentBuildingRepository apartmentBuildingRepository,
            OfficeUnitRepository officeUnitRepository,
            ApartmentUnitRepository apartmentUnitRepository,
            PropertyListingRepository listingRepository,
            DungeonInstanceRepository dungeonInstanceRepository,
            DungeonInstanceTemplateRepository templateRepository) {
        return new Earth2026BuildingInstanceService(
                hqRepository,
                apartmentBuildingRepository,
                officeUnitRepository,
                apartmentUnitRepository,
                listingRepository,
                dungeonInstanceRepository,
                templateRepository
        );
    }

}
