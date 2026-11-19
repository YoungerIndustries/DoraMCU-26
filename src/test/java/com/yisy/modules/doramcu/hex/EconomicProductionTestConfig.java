package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.controller.misc.EconomicProductionWebSocketHandler;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.hex.model.BuildContract;
import com.yisy.modules.doramcu.hex.model.CrewMemberSkill;
import com.yisy.modules.doramcu.hex.model.InfrastructureItem;
import com.yisy.modules.doramcu.hex.model.MaterialCost;
import com.yisy.modules.doramcu.hex.model.ProductionChainTracking;
import com.yisy.modules.doramcu.hex.repository.BuildContractRepository;
import com.yisy.modules.doramcu.hex.repository.CrewMemberSkillRepository;
import com.yisy.modules.doramcu.hex.repository.InfrastructureItemRepository;
import com.yisy.modules.doramcu.hex.repository.MaterialCostRepository;
import com.yisy.modules.doramcu.hex.repository.ProductionChainTrackingRepository;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Minimal Spring test configuration for economic production integration tests.
 * <p>
 * Uses an H2 in-memory database with create-drop DDL auto, so each test
 * runs against a fresh schema. External services like SpaceStationBuilder are mocked.
 * </p>
 */
@Configuration
@PropertySource("classpath:application-test.properties")
@EnableJpaRepositories(
        basePackages = {
                "com.yisy.modules.doramcu.repository",
                "com.yisy.modules.doramcu.repository.ai",
                "com.yisy.modules.doramcu.hex.repository",
                "com.yisy.modules.doramcu.corporation.repository"
        },
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        MaterialCostRepository.class,
                        BuildContractRepository.class,
                        InfrastructureItemRepository.class,
                        ProductionChainTrackingRepository.class,
                        CrewMemberSkillRepository.class,
                        AiCharacterRepository.class
                }
        )
)
@EnableTransactionManagement
@ComponentScan(
        basePackages = {"com.yisy.modules.doramcu.hex"},
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        MaterialCostService.class,
                        CostCalculatorService.class,
                        BuildContractService.class,
                        ProductionChainService.class,
                        SpaceStationContractService.class
                }
        ),
        useDefaultFilters = false
)
public class EconomicProductionTestConfig {

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("economic_production_testdb")
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        // Scan only hex and eve models, excluding corporation module schema issues
        em.setPackagesToScan(
                "com.yisy.modules.doramcu.hex.model",
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
    public MaterialCostService materialCostService(
            MaterialCostRepository materialCostRepository,
            InfrastructureItemRepository infrastructureItemRepository) {
        return new MaterialCostService(materialCostRepository, infrastructureItemRepository);
    }

    @Bean
    public CostCalculatorService costCalculatorService(
            MaterialCostService materialCostService,
            AiCharacterRepository aiCharacterRepository,
            BuildContractRepository buildContractRepository,
            ProductionChainTrackingRepository chainRepository) {
        return new CostCalculatorService(
                materialCostService,
                aiCharacterRepository,
                buildContractRepository,
                chainRepository
        );
    }

    @Bean
    public EconomicProductionWebSocketHandler economicProductionWebSocketHandler() {
        return Mockito.mock(EconomicProductionWebSocketHandler.class);
    }

    @Bean
    public BuildContractService buildContractService(
            BuildContractRepository contractRepository,
            InfrastructureItemRepository infrastructureItemRepository,
            AiCharacterRepository aiCharacterRepository,
            CostCalculatorService costCalculatorService) {
        return new BuildContractService(
                contractRepository,
                infrastructureItemRepository,
                aiCharacterRepository,
                costCalculatorService
        );
    }

    @Bean
    public ProductionChainService productionChainService(
            ProductionChainTrackingRepository chainRepository,
            BuildContractService buildContractService,
            MaterialCostService materialCostService) {
        return new ProductionChainService(
                chainRepository,
                buildContractService,
                materialCostService
        );
    }

    @Bean
    public SpaceStationContractService spaceStationContractService(
            BuildContractService buildContractService,
            ProductionChainService productionChainService,
            CostCalculatorService costCalculatorService,
            MaterialCostService materialCostService,
            AiCharacterRepository aiCharacterRepository,
            BuildContractRepository contractRepository) {
        SpaceStationBuilder spaceStationBuilder = Mockito.mock(SpaceStationBuilder.class);
        return new SpaceStationContractService(
                buildContractService,
                productionChainService,
                costCalculatorService,
                materialCostService,
                spaceStationBuilder,
                aiCharacterRepository,
                contractRepository
        );
    }
}
