package com.yisy.modules.doramcu.hex.planet;

import com.yisy.modules.doramcu.gamecore.DungeonRoomService;
import com.yisy.modules.doramcu.hex.planet.cargo.CargoGenerationService;
import com.yisy.modules.doramcu.hex.planet.controller.HexPlanetSurfaceController;
import com.yisy.modules.doramcu.hex.planet.depot.VehicleDepotService;
import com.yisy.modules.doramcu.fps.FpsMapService;
import com.yisy.modules.doramcu.fps.core.FpsGameServer;
import com.yisy.modules.doramcu.hex.planet.fps.TycoonFpsBridgeService;
import com.yisy.modules.doramcu.hex.planet.fps.TycoonFpsIntegrationService;
import com.yisy.modules.doramcu.hex.planet.trade.PlayerTradeService;
import com.yisy.modules.doramcu.hex.planet.hullmodule.HullModuleService;
import com.yisy.modules.doramcu.hex.planet.military.MilitaryProductionChainService;
import com.yisy.modules.doramcu.hex.planet.military.MilitaryProductionService;
import com.yisy.modules.doramcu.hex.planet.military.ModuleBattleAdapter;
import com.yisy.modules.doramcu.hex.planet.military.UnifiedBattleResolver;
import com.yisy.modules.doramcu.hex.planet.military.UniversalBattleManager;
import com.yisy.modules.doramcu.hex.planet.pathfinding.InfrastructurePathfinder;
import com.yisy.modules.doramcu.hex.planet.service.CampaignTurnProcessor;

import java.util.Collections;
import com.yisy.modules.doramcu.earth2026.service.PlanetService;
import com.yisy.modules.doramcu.thirdreich.commander.AICommanderService;
import com.yisy.modules.doramcu.hex.planet.military.PlayerMilitaryService;
import com.yisy.modules.doramcu.thirdreich.service.CountryCampaignService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

/**
 * Test configuration that extends {@link PersistenceTestConfig} with the
 * REST API controller and mock dungeon room service.
 */
@Configuration
@Import(PersistenceTestConfig.class)
@ComponentScan(
        basePackages = "com.yisy.modules.doramcu.hex.planet",
        includeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                                PlanetSurfacePersistenceService.class,
                                DefaultPlanetSurfaceBuilder.class,
                                HexPlanetSurfaceController.class,
                                MockDungeonRoomService.class
                        }
                )
        },
        useDefaultFilters = false
)
public class ControllerTestConfig {

    @Bean
    public DungeonRoomService dungeonRoomService() {
        return new MockDungeonRoomService();
    }

    @Bean
    public InfrastructurePathfinder infrastructurePathfinder() {
        return new InfrastructurePathfinder();
    }

    @Bean
    public VehicleDepotService vehicleDepotService(InfrastructurePathfinder pathfinder) {
        return new VehicleDepotService(pathfinder);
    }

    @Bean
    public HullModuleService hullModuleService() {
        return new HullModuleService();
    }

    @Bean
    public MilitaryProductionService militaryProductionService(HullModuleService hullModuleService) {
        return new MilitaryProductionService(hullModuleService);
    }

    @Bean
    public UnifiedBattleResolver battleResolver() {
        return new UnifiedBattleResolver();
    }

    @Bean
    public UniversalBattleManager universalBattleManager() {
        return new UniversalBattleManager(Collections.emptyList());
    }

    @Bean
    public AICommanderService aiCommanderService() {
        return new AICommanderService();
    }

    @Bean
    public TycoonFpsBridgeService fpsBridgeService(
            HullModuleService hullModuleService,
            MilitaryProductionService militaryProductionService,
            UnifiedBattleResolver battleResolver,
            CountryCampaignService countryCampaignService) {
        return new TycoonFpsBridgeService(hullModuleService, militaryProductionService, battleResolver, countryCampaignService);
    }

    @Bean
    public CountryCampaignService countryCampaignService(
            PlanetService planetService,
            AICommanderService aiCommanderService,
            MilitaryProductionService militaryProductionService) {
        return new CountryCampaignService(planetService, aiCommanderService, militaryProductionService);
    }

    @Bean
    public PlayerMilitaryService playerMilitaryService(
            PlanetService planetService,
            CountryCampaignService countryCampaignService) {
        return new PlayerMilitaryService(planetService, countryCampaignService);
    }

    @Bean
    public CargoGenerationService cargoGenerationService() {
        return new CargoGenerationService();
    }

    @Bean
    public MilitaryProductionChainService militaryProductionChainService(
            CargoGenerationService cargoGenerationService,
            MilitaryProductionService militaryProductionService,
            HullModuleService hullModuleService) {
        return new MilitaryProductionChainService(cargoGenerationService, militaryProductionService, hullModuleService);
    }

    @Bean
    public FpsMapService fpsMapService() {
        return new FpsMapService();
    }

    @Bean
    public FpsGameServer fpsGameServer() {
        return new FpsGameServer();
    }

    @Bean
    public TycoonFpsIntegrationService tycoonFpsIntegrationService(
            TycoonFpsBridgeService fpsBridgeService,
            FpsMapService fpsMapService,
            FpsGameServer fpsGameServer,
            HullModuleService hullModuleService,
            MilitaryProductionService militaryProductionService,
            DefaultPlanetSurfaceBuilder planetSurfaceBuilder) {
        return new TycoonFpsIntegrationService(fpsBridgeService, fpsMapService, fpsGameServer,
                hullModuleService, militaryProductionService, planetSurfaceBuilder);
    }

    @Bean
    public PlayerTradeService playerTradeService() {
        return new PlayerTradeService();
    }

    @Bean
    public HexPlanetSurfaceController planetSurfaceController(
            DefaultPlanetSurfaceBuilder builder,
            PlanetSurfacePersistenceService persistenceService,
            DungeonRoomService dungeonRoomService,
            VehicleDepotService vehicleDepotService,
            InfrastructurePathfinder infrastructurePathfinder,
            TycoonFpsBridgeService fpsBridgeService,
            HullModuleService hullModuleService,
            MilitaryProductionService militaryProductionService,
            MilitaryProductionChainService productionChainService,
            UnifiedBattleResolver battleResolver,
            UniversalBattleManager universalBattleManager,
            CountryCampaignService countryCampaignService,
            PlanetService planetService,
            PlayerMilitaryService playerMilitaryService,
            TycoonFpsIntegrationService tycoonFpsIntegrationService,
            PlayerTradeService playerTradeService) {
        return new HexPlanetSurfaceController(builder, persistenceService, dungeonRoomService,
                vehicleDepotService, infrastructurePathfinder, fpsBridgeService,
                hullModuleService, militaryProductionService, productionChainService, battleResolver,
                universalBattleManager, countryCampaignService, planetService, playerMilitaryService,
                tycoonFpsIntegrationService, playerTradeService);
    }
}
