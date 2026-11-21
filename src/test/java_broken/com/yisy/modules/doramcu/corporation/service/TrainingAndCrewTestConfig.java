package com.yisy.modules.doramcu.corporation.service;

import com.yisy.modules.doramcu.corporation.model.CharacterTrainingTour;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.model.Crew;
import com.yisy.modules.doramcu.corporation.model.CrewMember;
import com.yisy.modules.doramcu.corporation.repository.CharacterTrainingTourRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateContractRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateHiringRepository;
import com.yisy.modules.doramcu.corporation.repository.CrewMemberRepository;
import com.yisy.modules.doramcu.corporation.repository.CrewRepository;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import com.yisy.modules.doramcu.service.CharacterService;
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
 * Minimal Spring test configuration for training and crew integration tests.
 * <p>
 * Uses an H2 in-memory database with create-drop DDL auto, so each test
 * runs against a fresh schema. External services are mocked.
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
                        CharacterTrainingTourRepository.class,
                        CrewRepository.class,
                        CrewMemberRepository.class
                }
        )
)
@EnableTransactionManagement
@ComponentScan(
        basePackages = {"com.yisy.modules.doramcu.corporation.service"},
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        CrewService.class,
                        CharacterTrainingService.class,
                        TrainingSimulationService.class
                }
        ),
        useDefaultFilters = false
)
public class TrainingAndCrewTestConfig {

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("training_crew_testdb")
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
    public CrewService crewService(
            CrewRepository crewRepository,
            CrewMemberRepository crewMemberRepository,
            CorporateHiringRepository hiringRepository,
            CorporateContractRepository contractRepository) {
        return new CrewService(
                crewRepository,
                crewMemberRepository,
                hiringRepository,
                contractRepository
        );
    }

    @Bean
    public TrainingSimulationService trainingSimulationService() {
        return new TrainingSimulationService();
    }

    @Bean
    public CharacterTrainingService characterTrainingService(
            CharacterTrainingTourRepository tourRepository,
            TrainingSimulationService simulationService,
            CorporateHiringRepository hiringRepository,
            AiCharacterRepository aiCharacterRepository) {
        CareerProgressionService careerProgressionService = Mockito.mock(CareerProgressionService.class);
        CorporationEconomyService economyService = Mockito.mock(CorporationEconomyService.class);
        CharacterService travellerCharacterService = Mockito.mock(CharacterService.class);
        return new CharacterTrainingService(
                tourRepository,
                simulationService,
                careerProgressionService,
                economyService,
                travellerCharacterService,
                aiCharacterRepository,
                hiringRepository
        );
    }
}
