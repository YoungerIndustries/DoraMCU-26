package com.yisy.modules.doramcu.traveller.exploration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("SectorExplorationService Tests")
@ExtendWith(MockitoExtension.class)
class SectorExplorationServiceTest {

    @Mock
    private StarSystemRepository systemRepository;

    @Mock
    private PlanetLandingRepository landingRepository;

    @Mock
    private CharacterPositionRepository positionRepository;

    private SectorExplorationService service;

    @BeforeEach
    void setUp() {
        service = new SectorExplorationService(systemRepository, landingRepository, positionRepository);
    }

    // ---- Position Management ----

    @Nested
    @DisplayName("Position Management Tests")
    class PositionManagementTests {

        @Test
        @DisplayName("setPosition should set character position")
        void setPosition_shouldSet() {
            StarSystem sol = new StarSystem("Sol", "1717");
            sol.setId(1L);
            when(systemRepository.findByHexSector("1717")).thenReturn(sol);
            when(positionRepository.save(any(CharacterPosition.class))).thenAnswer(inv -> inv.getArgument(0));

            service.setPosition("player1", "1717");

            verify(positionRepository).save(any(CharacterPosition.class));
        }

        @Test
        @DisplayName("getCurrentSystem should return system for positioned character")
        void getCurrentSystem_shouldReturnSystem() {
            StarSystem sol = new StarSystem("Sol", "1717");
            sol.setId(1L);
            CharacterPosition pos = new CharacterPosition("player1", "1717");
            when(positionRepository.findByCharacterId("player1")).thenReturn(Optional.of(pos));
            when(systemRepository.findByHexSector("1717")).thenReturn(sol);

            Optional<StarSystem> result = service.getCurrentSystem("player1");

            assertTrue(result.isPresent());
            assertEquals("Sol", result.get().getName());
        }

        @Test
        @DisplayName("getCurrentSystem should return first system for unpositioned character")
        void getCurrentSystem_shouldReturnFirstSystem() {
            StarSystem sol = new StarSystem("Sol", "1717");
            sol.setId(1L);
            when(systemRepository.findAll()).thenReturn(List.of(sol));

            Optional<StarSystem> result = service.getCurrentSystem("newPlayer");

            assertTrue(result.isPresent());
            assertEquals("Sol", result.get().getName());
        }

        @Test
        @DisplayName("getCurrentSystem should return empty for no systems")
        void getCurrentSystem_shouldReturnEmpty() {
            when(systemRepository.findAll()).thenReturn(Collections.emptyList());

            Optional<StarSystem> result = service.getCurrentSystem("newPlayer");

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("getCurrentSystem should return empty for unknown hex")
        void getCurrentSystem_shouldReturnEmptyForUnknownHex() {
            CharacterPosition pos = new CharacterPosition("player1", "9999");
            when(positionRepository.findByCharacterId("player1")).thenReturn(Optional.of(pos));
            when(systemRepository.findByHexSector("9999")).thenReturn(null);

            Optional<StarSystem> result = service.getCurrentSystem("player1");

            assertFalse(result.isPresent());
        }
    }

    // ---- Hex Navigation ----

    @Nested
    @DisplayName("Hex Navigation Tests")
    class HexNavigationTests {

        @Test
        @DisplayName("navigate North should increment Y")
        void navigateNorth_shouldIncrementY() {
            StarSystem target = new StarSystem("North System", "1718");
            target.setId(2L);
            CharacterPosition pos = new CharacterPosition("player1", "1717");
            when(positionRepository.findByCharacterId("player1")).thenReturn(Optional.of(pos));
            when(systemRepository.findByHexSector("1718")).thenReturn(target);
            when(positionRepository.save(any(CharacterPosition.class))).thenAnswer(inv -> inv.getArgument(0));

            SectorExplorationService.NavigationResult result = service.navigate("player1", "N");

            assertTrue(result.isSuccess());
            assertEquals("North System", result.getSystem().getName());
        }

        @Test
        @DisplayName("navigate South should decrement Y")
        void navigateSouth_shouldDecrementY() {
            StarSystem target = new StarSystem("South System", "1716");
            target.setId(2L);
            CharacterPosition pos = new CharacterPosition("player1", "1717");
            when(positionRepository.findByCharacterId("player1")).thenReturn(Optional.of(pos));
            when(systemRepository.findByHexSector("1716")).thenReturn(target);
            when(positionRepository.save(any(CharacterPosition.class))).thenAnswer(inv -> inv.getArgument(0));

            SectorExplorationService.NavigationResult result = service.navigate("player1", "S");

            assertTrue(result.isSuccess());
            assertEquals("South System", result.getSystem().getName());
        }

        @Test
        @DisplayName("navigate East should increment X")
        void navigateEast_shouldIncrementX() {
            StarSystem target = new StarSystem("East System", "1817");
            target.setId(2L);
            CharacterPosition pos = new CharacterPosition("player1", "1717");
            when(positionRepository.findByCharacterId("player1")).thenReturn(Optional.of(pos));
            when(systemRepository.findByHexSector("1817")).thenReturn(target);
            when(positionRepository.save(any(CharacterPosition.class))).thenAnswer(inv -> inv.getArgument(0));

            SectorExplorationService.NavigationResult result = service.navigate("player1", "E");

            assertTrue(result.isSuccess());
            assertEquals("East System", result.getSystem().getName());
        }

        @Test
        @DisplayName("navigate West should decrement X")
        void navigateWest_shouldDecrementX() {
            StarSystem target = new StarSystem("West System", "1617");
            target.setId(2L);
            CharacterPosition pos = new CharacterPosition("player1", "1717");
            when(positionRepository.findByCharacterId("player1")).thenReturn(Optional.of(pos));
            when(systemRepository.findByHexSector("1617")).thenReturn(target);
            when(positionRepository.save(any(CharacterPosition.class))).thenAnswer(inv -> inv.getArgument(0));

            SectorExplorationService.NavigationResult result = service.navigate("player1", "W");

            assertTrue(result.isSuccess());
            assertEquals("West System", result.getSystem().getName());
        }

        @Test
        @DisplayName("navigate Northeast should increment both")
        void navigateNortheast_shouldIncrementBoth() {
            StarSystem target = new StarSystem("NE System", "1818");
            target.setId(2L);
            CharacterPosition pos = new CharacterPosition("player1", "1717");
            when(positionRepository.findByCharacterId("player1")).thenReturn(Optional.of(pos));
            when(systemRepository.findByHexSector("1818")).thenReturn(target);
            when(positionRepository.save(any(CharacterPosition.class))).thenAnswer(inv -> inv.getArgument(0));

            SectorExplorationService.NavigationResult result = service.navigate("player1", "NE");

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("navigate Northwest should decrement X, increment Y")
        void navigateNorthwest_shouldDecXIncY() {
            StarSystem target = new StarSystem("NW System", "1618");
            target.setId(2L);
            CharacterPosition pos = new CharacterPosition("player1", "1717");
            when(positionRepository.findByCharacterId("player1")).thenReturn(Optional.of(pos));
            when(systemRepository.findByHexSector("1618")).thenReturn(target);
            when(positionRepository.save(any(CharacterPosition.class))).thenAnswer(inv -> inv.getArgument(0));

            SectorExplorationService.NavigationResult result = service.navigate("player1", "NW");

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("navigate Southeast should increment X, decrement Y")
        void navigateSoutheast_shouldIncXDecY() {
            StarSystem target = new StarSystem("SE System", "1816");
            target.setId(2L);
            CharacterPosition pos = new CharacterPosition("player1", "1717");
            when(positionRepository.findByCharacterId("player1")).thenReturn(Optional.of(pos));
            when(systemRepository.findByHexSector("1816")).thenReturn(target);
            when(positionRepository.save(any(CharacterPosition.class))).thenAnswer(inv -> inv.getArgument(0));

            SectorExplorationService.NavigationResult result = service.navigate("player1", "SE");

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("navigate Southwest should decrement both")
        void navigateSouthwest_shouldDecrementBoth() {
            StarSystem target = new StarSystem("SW System", "1616");
            target.setId(2L);
            CharacterPosition pos = new CharacterPosition("player1", "1717");
            when(positionRepository.findByCharacterId("player1")).thenReturn(Optional.of(pos));
            when(systemRepository.findByHexSector("1616")).thenReturn(target);
            when(positionRepository.save(any(CharacterPosition.class))).thenAnswer(inv -> inv.getArgument(0));

            SectorExplorationService.NavigationResult result = service.navigate("player1", "SW");

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("navigate at X boundary should clamp to 99")
        void navigateAtXBoundary_shouldClamp() {
            StarSystem target = new StarSystem("Boundary", "9917");
            target.setId(2L);
            CharacterPosition pos = new CharacterPosition("player1", "9917");
            when(positionRepository.findByCharacterId("player1")).thenReturn(Optional.of(pos));
            when(systemRepository.findByHexSector("9917")).thenReturn(target);
            when(positionRepository.save(any(CharacterPosition.class))).thenAnswer(inv -> inv.getArgument(0));

            SectorExplorationService.NavigationResult result = service.navigate("player1", "E");

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("navigate at Y boundary should clamp to 99")
        void navigateAtYBoundary_shouldClamp() {
            StarSystem target = new StarSystem("Boundary", "1799");
            target.setId(2L);
            CharacterPosition pos = new CharacterPosition("player1", "1799");
            when(positionRepository.findByCharacterId("player1")).thenReturn(Optional.of(pos));
            when(systemRepository.findByHexSector("1799")).thenReturn(target);
            when(positionRepository.save(any(CharacterPosition.class))).thenAnswer(inv -> inv.getArgument(0));

            SectorExplorationService.NavigationResult result = service.navigate("player1", "N");

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("navigate at 00 boundary should clamp to 00")
        void navigateAtZeroBoundary_shouldClamp() {
            StarSystem target = new StarSystem("Zero", "0000");
            target.setId(2L);
            CharacterPosition pos = new CharacterPosition("player1", "0000");
            when(positionRepository.findByCharacterId("player1")).thenReturn(Optional.of(pos));
            when(systemRepository.findByHexSector("0000")).thenReturn(target);
            when(positionRepository.save(any(CharacterPosition.class))).thenAnswer(inv -> inv.getArgument(0));

            SectorExplorationService.NavigationResult result = service.navigate("player1", "W");

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("navigate with full direction names")
        void navigateWithFullNames_shouldWork() {
            StarSystem target = new StarSystem("North", "1718");
            target.setId(2L);
            CharacterPosition pos = new CharacterPosition("player1", "1717");
            when(positionRepository.findByCharacterId("player1")).thenReturn(Optional.of(pos));
            when(systemRepository.findByHexSector("1718")).thenReturn(target);
            when(positionRepository.save(any(CharacterPosition.class))).thenAnswer(inv -> inv.getArgument(0));

            SectorExplorationService.NavigationResult result = service.navigate("player1", "north");

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("navigate to unexplored hex should generate random system")
        void navigateToUnexplored_shouldGenerateSystem() {
            CharacterPosition pos = new CharacterPosition("player1", "1717");
            when(positionRepository.findByCharacterId("player1")).thenReturn(Optional.of(pos));
            when(systemRepository.findByHexSector("1718")).thenReturn(null);
            when(systemRepository.save(any(StarSystem.class))).thenAnswer(inv -> inv.getArgument(0));
            when(positionRepository.save(any(CharacterPosition.class))).thenAnswer(inv -> inv.getArgument(0));

            SectorExplorationService.NavigationResult result = service.navigate("player1", "N");

            assertTrue(result.isSuccess());
            assertNotNull(result.getSystem());
            assertTrue(result.getSystem().getName().startsWith("System-"));
        }

        @Test
        @DisplayName("navigate without position should use first system")
        void navigateWithoutPosition_shouldUseFirstSystem() {
            StarSystem sol = new StarSystem("Sol", "1717");
            sol.setId(1L);
            StarSystem target = new StarSystem("Target", "1718");
            target.setId(2L);
            when(positionRepository.findByCharacterId("player1")).thenReturn(Optional.empty());
            when(systemRepository.findAll()).thenReturn(List.of(sol));
            when(systemRepository.findByHexSector("1718")).thenReturn(target);
            when(positionRepository.save(any(CharacterPosition.class))).thenAnswer(inv -> inv.getArgument(0));

            SectorExplorationService.NavigationResult result = service.navigate("player1", "N");

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("navigate with no systems should fail")
        void navigateWithNoSystems_shouldFail() {
            when(systemRepository.findAll()).thenReturn(Collections.emptyList());

            SectorExplorationService.NavigationResult result = service.navigate("player1", "N");

            assertFalse(result.isSuccess());
        }
    }

    // ---- Planet Landing ----

    @Nested
    @DisplayName("Planet Landing Tests")
    class PlanetLandingTests {

        @Test
        @DisplayName("attemptLanding should save landing record")
        void attemptLanding_shouldSave() {
            StarSystem sol = new StarSystem("Sol", "1717");
            sol.setId(1L);
            sol.setStarports("A");
            sol.setPrimaryPlanet(3);
            CharacterPosition pos = new CharacterPosition("player1", "1717");
            when(positionRepository.findByCharacterId("player1")).thenReturn(Optional.of(pos));
            when(systemRepository.findByHexSector("1717")).thenReturn(sol);
            when(landingRepository.save(any(PlanetLanding.class))).thenAnswer(inv -> inv.getArgument(0));

            PlanetLanding landing = service.attemptLanding("player1");

            assertNotNull(landing);
            assertEquals("player1", landing.getCharacterId());
            assertEquals("Sol", landing.getSystemName());
            assertNotNull(landing.getOutcome());
        }

        @Test
        @DisplayName("attemptLanding should throw for invalid position")
        void attemptLanding_shouldThrowForInvalid() {
            CharacterPosition pos = new CharacterPosition("player1", "9999");
            when(positionRepository.findByCharacterId("player1")).thenReturn(Optional.of(pos));
            when(systemRepository.findByHexSector("9999")).thenReturn(null);

            assertThrows(IllegalArgumentException.class, () -> service.attemptLanding("player1"));
        }

        @Test
        @DisplayName("attemptLanding outcomes should be one of SUCCESS, ACCIDENT, FAILURE, ENCOUNTER")
        void attemptLanding_outcomes_shouldBeValid() {
            StarSystem sol = new StarSystem("Sol", "1717");
            sol.setId(1L);
            sol.setStarports("A");
            sol.setPrimaryPlanet(3);
            CharacterPosition pos = new CharacterPosition("player1", "1717");
            when(positionRepository.findByCharacterId("player1")).thenReturn(Optional.of(pos));
            when(systemRepository.findByHexSector("1717")).thenReturn(sol);
            when(landingRepository.save(any(PlanetLanding.class))).thenAnswer(inv -> inv.getArgument(0));

            // Run multiple times to check different outcomes
            for (int i = 0; i < 100; i++) {
                PlanetLanding landing = service.attemptLanding("player1");
                String outcome = landing.getOutcome();
                assertTrue(outcome.equals("SUCCESS") || outcome.equals("ACCIDENT") ||
                        outcome.equals("FAILURE") || outcome.equals("ENCOUNTER"),
                        "Invalid outcome: " + outcome);
            }
        }
    }

    // ---- Landing History ----

    @Nested
    @DisplayName("Landing History Tests")
    class LandingHistoryTests {

        @Test
        @DisplayName("getLandingHistory should return list")
        void getLandingHistory_shouldReturnList() {
            PlanetLanding landing = new PlanetLanding();
            landing.setId(1L);
            landing.setCharacterId("player1");
            landing.setOutcome("SUCCESS");
            when(landingRepository.findByCharacterIdOrderByTimestampDesc("player1"))
                    .thenReturn(List.of(landing));

            List<PlanetLanding> history = service.getLandingHistory("player1");

            assertEquals(1, history.size());
            assertEquals("SUCCESS", history.get(0).getOutcome());
        }

        @Test
        @DisplayName("getLandingHistory should return empty for no landings")
        void getLandingHistory_shouldReturnEmpty() {
            when(landingRepository.findByCharacterIdOrderByTimestampDesc("player1"))
                    .thenReturn(Collections.emptyList());

            List<PlanetLanding> history = service.getLandingHistory("player1");

            assertTrue(history.isEmpty());
        }
    }

    // ---- Trade Goods ----

    @Nested
    @DisplayName("Trade Goods Tests")
    class TradeGoodsTests {

        @Test
        @DisplayName("getTradeGoods should return commodities")
        void getTradeGoods_shouldReturnCommodities() {
            StarSystem sol = new StarSystem("Sol", "1717");
            sol.setId(1L);
            sol.setCommodities("Food,Electronics,Luxuries");
            CharacterPosition pos = new CharacterPosition("player1", "1717");
            when(positionRepository.findByCharacterId("player1")).thenReturn(Optional.of(pos));
            when(systemRepository.findByHexSector("1717")).thenReturn(sol);

            List<String> goods = service.getTradeGoods("player1");

            assertNotNull(goods);
            assertTrue(goods.contains("Food"));
        }

        @Test
        @DisplayName("getTradeGoods should return empty for no system")
        void getTradeGoods_shouldReturnEmpty() {
            CharacterPosition pos = new CharacterPosition("player1", "9999");
            when(positionRepository.findByCharacterId("player1")).thenReturn(Optional.of(pos));
            when(systemRepository.findByHexSector("9999")).thenReturn(null);

            List<String> goods = service.getTradeGoods("player1");

            assertTrue(goods.isEmpty());
        }
    }

    // ---- All Systems ----

    @Nested
    @DisplayName("All Systems Tests")
    class AllSystemsTests {

        @Test
        @DisplayName("getAllSystems should return all systems")
        void getAllSystems_shouldReturnAll() {
            StarSystem sol = new StarSystem("Sol", "1717");
            sol.setId(1L);
            StarSystem rokuga = new StarSystem("Rokuga", "1716");
            rokuga.setId(2L);
            when(systemRepository.findAll()).thenReturn(Arrays.asList(sol, rokuga));

            List<StarSystem> systems = service.getAllSystems();

            assertEquals(2, systems.size());
        }
    }

    // ---- NavigationResult ----

    @Nested
    @DisplayName("NavigationResult Tests")
    class NavigationResultTests {

        @Test
        @DisplayName("NavigationResult should store all fields")
        void navigationResult_shouldStoreFields() {
            StarSystem system = new StarSystem("Test", "1717");
            SectorExplorationService.NavigationResult result =
                    new SectorExplorationService.NavigationResult(true, "Message", system, "Hazard");

            assertTrue(result.isSuccess());
            assertEquals("Message", result.getMessage());
            assertEquals(system, result.getSystem());
            assertEquals("Hazard", result.getHazard());
        }

        @Test
        @DisplayName("NavigationResult simple constructor")
        void navigationResult_simpleConstructor() {
            SectorExplorationService.NavigationResult result =
                    new SectorExplorationService.NavigationResult(false, "Failed");

            assertFalse(result.isSuccess());
            assertEquals("Failed", result.getMessage());
            assertNull(result.getSystem());
            assertNull(result.getHazard());
        }
    }
}
