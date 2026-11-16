package com.yisy.modules.doramcu.corporation.service;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.EveCorporationRepository;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateContractRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateHiringRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporationOfficeStateRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporationSaveRepository;
import com.yisy.modules.doramcu.service.MultiUniverseContractService;
import org.mockito.Mockito;
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
 * Minimal Spring test configuration for corporate office integration tests.
 * <p>
 * Uses an H2 in-memory database with create-drop DDL auto, so each test
 * runs against a fresh schema. Follows the same pattern as {@code RealEstateTestConfig}.
 * </p>
 */
@Configuration
@PropertySource("classpath:application-test.properties")
@EnableJpaRepositories(
        basePackages = {"com.yisy.modules.doramcu.repository", "com.yisy.modules.doramcu.corporation.repository"},
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        CorporateContractRepository.class,
                        CorporateHiringRepository.class,
                        AiCharacterRepository.class,
                        CorporationOfficeStateRepository.class,
                        CorporationSaveRepository.class,
                        EveCorporationRepository.class
                }
        )
)
@EnableTransactionManagement
@ComponentScan(
        basePackages = {"com.yisy.modules.doramcu.service", "com.yisy.modules.doramcu.corporation.service"},
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        CorporateOfficeService.class
                }
        ),
        useDefaultFilters = false
)
public class CorporateOfficeTestConfig {

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("office_testdb")
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan(
                "com.yisy.modules.doramcu.model",
                "com.yisy.modules.doramcu.corporation.model",
                "com.yisy.modules.doramcu.eve.model"
        );

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.show_sql", "false");
        properties.put("jakarta.persistence.sharedCache.mode", "DISABLE_SELECTIVE");
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
    public CorporationPersistenceService corporationPersistenceService(
            CorporationOfficeStateRepository officeStateRepository,
            CorporationSaveRepository saveRepository,
            EveCorporationRepository corporationRepository,
            CorporateContractRepository contractRepository,
            CorporateHiringRepository hiringRepository) {
        return new CorporationPersistenceService(
                officeStateRepository,
                saveRepository,
                corporationRepository,
                contractRepository,
                hiringRepository
        );
    }

    @Bean
    public CorporateOfficeService corporateOfficeService(
            CorporateContractRepository contractRepository,
            CorporateHiringRepository hiringRepository,
            AiCharacterRepository aiCharacterRepository,
            CorporationPersistenceService persistenceService,
            EveCorporationRepository eveCorporationRepository) {
        ContractExecutionService contractExecutionService = Mockito.mock(ContractExecutionService.class);
        CorporationEconomyService economyService = Mockito.mock(CorporationEconomyService.class);
        CorporationInteractionService interactionService = Mockito.mock(CorporationInteractionService.class);
        MultiUniverseContractService multiUniverseService = Mockito.mock(MultiUniverseContractService.class);
        CharacterTrainingService characterTrainingService = Mockito.mock(CharacterTrainingService.class);
        CrewService crewService = Mockito.mock(CrewService.class);
        return new CorporateOfficeService(
                contractRepository,
                hiringRepository,
                aiCharacterRepository,
                contractExecutionService,
                economyService,
                interactionService,
                persistenceService,
                multiUniverseService,
                eveCorporationRepository,
                characterTrainingService,
                crewService
        );
    }
}
