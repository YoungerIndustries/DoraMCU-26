package com.yisy.modules.doramcu.earth2026.controller;

import com.yisy.modules.doramcu.earth2026.model.*;
import com.yisy.modules.doramcu.earth2026.service.PlanetService;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Earth2026PlanetControllerTest {

    @Mock
    private PlanetService planetService;

    private Earth2026PlanetController controller;

    @BeforeEach
    void setUp() {
        controller = new Earth2026PlanetController(planetService);
    }

    // ------------------------------------------------------------------
    // Planet operations
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("GET /planets")
    class GetAllPlanets {

        @Test
        @DisplayName("returns empty list when no planets")
        void returnsEmptyList() {
            when(planetService.getAllPlanets()).thenReturn(Collections.emptyList());

            ResponseEntity<List<Earth2026Planet>> response = controller.getAllPlanets();

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isEmpty());
        }

        @Test
        @DisplayName("returns all planets")
        void returnsAllPlanets() {
            Earth2026Planet earth = new Earth2026Planet();
            earth.setName("Earth");
            when(planetService.getAllPlanets()).thenReturn(Arrays.asList(earth));

            ResponseEntity<List<Earth2026Planet>> response = controller.getAllPlanets();

            assertEquals(200, response.getStatusCode().value());
            assertEquals(1, response.getBody().size());
            assertEquals("Earth", response.getBody().get(0).getName());
        }
    }

    @Nested
    @DisplayName("GET /planets/{id}")
    class GetPlanet {

        @Test
        @DisplayName("returns planet when found")
        void returnsPlanetWhenFound() {
            Earth2026Planet earth = new Earth2026Planet();
            earth.setName("Earth");
            when(planetService.getPlanet(1L)).thenReturn(Optional.of(earth));

            ResponseEntity<?> response = controller.getPlanet(1L);

            assertEquals(200, response.getStatusCode().value());
            assertEquals(earth, response.getBody());
        }

        @Test
        @DisplayName("returns 404 when not found")
        void returns404WhenNotFound() {
            when(planetService.getPlanet(999L)).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getPlanet(999L);

            assertEquals(404, response.getStatusCode().value());
        }
    }

    @Nested
    @DisplayName("GET /planets/name/{name}")
    class GetPlanetByName {

        @Test
        @DisplayName("returns planet when found by name")
        void returnsPlanetWhenFound() {
            Earth2026Planet earth = new Earth2026Planet();
            earth.setName("Earth");
            when(planetService.getPlanet("Earth")).thenReturn(Optional.of(earth));

            ResponseEntity<?> response = controller.getPlanetByName("Earth");

            assertEquals(200, response.getStatusCode().value());
            assertEquals("Earth", ((Earth2026Planet) response.getBody()).getName());
        }

        @Test
        @DisplayName("returns 404 when not found")
        void returns404WhenNotFound() {
            when(planetService.getPlanet("Mars")).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getPlanetByName("Mars");

            assertEquals(404, response.getStatusCode().value());
        }
    }

    // ------------------------------------------------------------------
    // Country operations
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("GET /countries")
    class GetAllCountries {

        @Test
        @DisplayName("returns countries for planet")
        void returnsCountriesForPlanet() {
            Earth2026Country us = new Earth2026Country();
            us.setIsoCode("US");
            when(planetService.getCountries(1L)).thenReturn(Arrays.asList(us));

            ResponseEntity<List<Earth2026Country>> response = controller.getCountries(1L);

            assertEquals(200, response.getStatusCode().value());
            assertEquals(1, response.getBody().size());
            assertEquals("US", response.getBody().get(0).getIsoCode());
        }
    }

    @Nested
    @DisplayName("GET /countries/{id}")
    class GetCountry {

        @Test
        @DisplayName("returns country when found")
        void returnsCountryWhenFound() {
            Earth2026Country us = new Earth2026Country();
            us.setIsoCode("US");
            when(planetService.getCountry(1L)).thenReturn(Optional.of(us));

            ResponseEntity<?> response = controller.getCountry(1L);

            assertEquals(200, response.getStatusCode().value());
            assertEquals("US", ((Earth2026Country) response.getBody()).getIsoCode());
        }

        @Test
        @DisplayName("returns 404 when not found")
        void returns404WhenNotFound() {
            when(planetService.getCountry(999L)).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getCountry(999L);

            assertEquals(404, response.getStatusCode().value());
        }
    }

    @Nested
    @DisplayName("GET /countries/iso/{isoCode}")
    class GetCountryByIsoCode {

        @Test
        @DisplayName("returns country by ISO code")
        void returnsCountryByIsoCode() {
            Earth2026Country us = new Earth2026Country();
            us.setIsoCode("US");
            when(planetService.getCountryByIsoCode("US")).thenReturn(Optional.of(us));

            ResponseEntity<?> response = controller.getCountryByIsoCode("US");

            assertEquals(200, response.getStatusCode().value());
            assertEquals("US", ((Earth2026Country) response.getBody()).getIsoCode());
        }

        @Test
        @DisplayName("returns 404 for unknown ISO code")
        void returns404ForUnknownIsoCode() {
            when(planetService.getCountryByIsoCode("XX")).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getCountryByIsoCode("XX");

            assertEquals(404, response.getStatusCode().value());
        }
    }

    @Nested
    @DisplayName("GET /countries/continent/{continent}")
    class GetCountriesByContinent {

        @Test
        @DisplayName("returns countries by continent")
        void returnsCountriesByContinent() {
            Earth2026Country us = new Earth2026Country();
            us.setIsoCode("US");
            Earth2026Country ca = new Earth2026Country();
            ca.setIsoCode("CA");
            when(planetService.getCountriesByContinent("North America"))
                    .thenReturn(Arrays.asList(us, ca));

            ResponseEntity<List<Earth2026Country>> response =
                    controller.getCountriesByContinent("North America");

            assertEquals(200, response.getStatusCode().value());
            assertEquals(2, response.getBody().size());
        }

        @Test
        @DisplayName("returns empty list for unknown continent")
        void returnsEmptyForUnknownContinent() {
            when(planetService.getCountriesByContinent("Atlantis"))
                    .thenReturn(Collections.emptyList());

            ResponseEntity<List<Earth2026Country>> response =
                    controller.getCountriesByContinent("Atlantis");

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isEmpty());
        }
    }

    // ------------------------------------------------------------------
    // Region operations
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("GET /countries/{countryId}/regions")
    class GetRegions {

        @Test
        @DisplayName("returns regions for country")
        void returnsRegionsForCountry() {
            Earth2026Region ca = new Earth2026Region();
            ca.setName("California");
            when(planetService.getRegions(1L)).thenReturn(Arrays.asList(ca));

            ResponseEntity<List<Earth2026Region>> response = controller.getRegions(1L);

            assertEquals(200, response.getStatusCode().value());
            assertEquals(1, response.getBody().size());
            assertEquals("California", response.getBody().get(0).getName());
        }
    }

    @Nested
    @DisplayName("GET /regions/{id}")
    class GetRegion {

        @Test
        @DisplayName("returns region when found")
        void returnsRegionWhenFound() {
            Earth2026Region ca = new Earth2026Region();
            ca.setName("California");
            when(planetService.getRegion(1L)).thenReturn(Optional.of(ca));

            ResponseEntity<?> response = controller.getRegion(1L);

            assertEquals(200, response.getStatusCode().value());
            assertEquals("California", ((Earth2026Region) response.getBody()).getName());
        }

        @Test
        @DisplayName("returns 404 when not found")
        void returns404WhenNotFound() {
            when(planetService.getRegion(999L)).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getRegion(999L);

            assertEquals(404, response.getStatusCode().value());
        }
    }

    // ------------------------------------------------------------------
    // Economy operations
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("GET /economy/{planetId}")
    class GetEconomy {

        @Test
        @DisplayName("returns economy when found")
        void returnsEconomyWhenFound() {
            Earth2026Economy economy = new Earth2026Economy();
            economy.setGdpTrillions(100.0);
            when(planetService.getEconomy(1L)).thenReturn(Optional.of(economy));

            ResponseEntity<?> response = controller.getEconomy(1L);

            assertEquals(200, response.getStatusCode().value());
            assertEquals(100.0, ((Earth2026Economy) response.getBody()).getGdpTrillions());
        }

        @Test
        @DisplayName("returns 404 when not found")
        void returns404WhenNotFound() {
            when(planetService.getEconomy(999L)).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getEconomy(999L);

            assertEquals(404, response.getStatusCode().value());
        }
    }

    // ------------------------------------------------------------------
    // Job operations
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("GET /jobs")
    class GetAllJobs {

        @Test
        @DisplayName("returns all jobs for planet")
        void returnsAllJobsForPlanet() {
            Earth2026Job job = new Earth2026Job();
            job.setTitle("Software Engineer");
            when(planetService.getJobs(1L)).thenReturn(Arrays.asList(job));

            ResponseEntity<List<Earth2026Job>> response = controller.getJobs(1L);

            assertEquals(200, response.getStatusCode().value());
            assertEquals(1, response.getBody().size());
            assertEquals("Software Engineer", response.getBody().get(0).getTitle());
        }
    }

    @Nested
    @DisplayName("GET /jobs/{id}")
    class GetJob {

        @Test
        @DisplayName("returns job when found")
        void returnsJobWhenFound() {
            Earth2026Job job = new Earth2026Job();
            job.setTitle("Software Engineer");
            when(planetService.getJob(1L)).thenReturn(Optional.of(job));

            ResponseEntity<?> response = controller.getJob(1L);

            assertEquals(200, response.getStatusCode().value());
            assertEquals("Software Engineer", ((Earth2026Job) response.getBody()).getTitle());
        }

        @Test
        @DisplayName("returns 404 when not found")
        void returns404WhenNotFound() {
            when(planetService.getJob(999L)).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getJob(999L);

            assertEquals(404, response.getStatusCode().value());
        }
    }

    @Nested
    @DisplayName("GET /jobs/category/{category}")
    class GetJobsByCategory {

        @Test
        @DisplayName("returns jobs by category")
        void returnsJobsByCategory() {
            Earth2026Job job = new Earth2026Job();
            job.setTitle("Software Engineer");
            when(planetService.getJobsByCategory(1L, "Technology"))
                    .thenReturn(Arrays.asList(job));

            ResponseEntity<List<Earth2026Job>> response =
                    controller.getJobsByCategory(1L, "Technology");

            assertEquals(200, response.getStatusCode().value());
            assertEquals(1, response.getBody().size());
        }
    }

    // ------------------------------------------------------------------
    // Contract operations
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("GET /contracts")
    class GetAvailableContracts {

        @Test
        @DisplayName("returns available contracts for planet")
        void returnsAvailableContractsForPlanet() {
            Earth2026Contract contract = new Earth2026Contract();
            contract.setTitle("Security Consultant");
            when(planetService.getAvailableContracts(1L)).thenReturn(Arrays.asList(contract));

            ResponseEntity<List<Earth2026Contract>> response =
                    controller.getAvailableContracts(1L);

            assertEquals(200, response.getStatusCode().value());
            assertEquals(1, response.getBody().size());
        }
    }

    @Nested
    @DisplayName("GET /contracts/{id}")
    class GetContract {

        @Test
        @DisplayName("returns contract when found")
        void returnsContractWhenFound() {
            Earth2026Contract contract = new Earth2026Contract();
            contract.setTitle("Security Consultant");
            when(planetService.getContract(1L)).thenReturn(Optional.of(contract));

            ResponseEntity<?> response = controller.getContract(1L);

            assertEquals(200, response.getStatusCode().value());
            assertEquals("Security Consultant",
                    ((Earth2026Contract) response.getBody()).getTitle());
        }

        @Test
        @DisplayName("returns 404 when not found")
        void returns404WhenNotFound() {
            when(planetService.getContract(999L)).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getContract(999L);

            assertEquals(404, response.getStatusCode().value());
        }
    }

    @Nested
    @DisplayName("GET /contracts/character/{characterId}")
    class GetContractsByCharacter {

        @Test
        @DisplayName("returns contracts for character")
        void returnsContractsForCharacter() {
            Earth2026Contract contract = new Earth2026Contract();
            contract.setTitle("Security Consultant");
            when(planetService.getContractsByCharacter(100L))
                    .thenReturn(Arrays.asList(contract));

            ResponseEntity<List<Earth2026Contract>> response =
                    controller.getContractsByCharacter(100L);

            assertEquals(200, response.getStatusCode().value());
            assertEquals(1, response.getBody().size());
        }

        @Test
        @DisplayName("returns empty list when no contracts")
        void returnsEmptyWhenNoContracts() {
            when(planetService.getContractsByCharacter(100L))
                    .thenReturn(Collections.emptyList());

            ResponseEntity<List<Earth2026Contract>> response =
                    controller.getContractsByCharacter(100L);

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isEmpty());
        }
    }

    // ------------------------------------------------------------------
    // AI Character operations
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("GET /countries/{countryId}/ai-leader")
    class GetCountryAiLeader {

        @Test
        @DisplayName("returns AI leader when assigned")
        void returnsAiLeaderWhenAssigned() {
            AiCharacter leader = new AiCharacter();
            leader.setName("Joe Biden");
            when(planetService.getCountryAiLeader(1L)).thenReturn(Optional.of(leader));

            ResponseEntity<?> response = controller.getCountryAiLeader(1L);

            assertEquals(200, response.getStatusCode().value());
            assertEquals("Joe Biden", ((AiCharacter) response.getBody()).getName());
        }

        @Test
        @DisplayName("returns 404 when no leader assigned")
        void returns404WhenNoLeader() {
            when(planetService.getCountryAiLeader(1L)).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getCountryAiLeader(1L);

            assertEquals(404, response.getStatusCode().value());
        }
    }

    @Nested
    @DisplayName("GET /regions/{regionId}/ai-governor")
    class GetRegionAiGovernor {

        @Test
        @DisplayName("returns AI governor when assigned")
        void returnsAiGovernorWhenAssigned() {
            AiCharacter governor = new AiCharacter();
            governor.setName("Gavin Newsom");
            when(planetService.getRegionAiGovernor(1L)).thenReturn(Optional.of(governor));

            ResponseEntity<?> response = controller.getRegionAiGovernor(1L);

            assertEquals(200, response.getStatusCode().value());
            assertEquals("Gavin Newsom", ((AiCharacter) response.getBody()).getName());
        }

        @Test
        @DisplayName("returns 404 when no governor assigned")
        void returns404WhenNoGovernor() {
            when(planetService.getRegionAiGovernor(1L)).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getRegionAiGovernor(1L);

            assertEquals(404, response.getStatusCode().value());
        }
    }

    @Nested
    @DisplayName("GET /planets/{planetId}/ai-leaders")
    class GetAllCountryAiLeaders {

        @Test
        @DisplayName("returns all AI leaders for planet")
        void returnsAllAiLeaders() {
            AiCharacter leader1 = new AiCharacter();
            leader1.setName("Joe Biden");
            AiCharacter leader2 = new AiCharacter();
            leader2.setName("Xi Jinping");
            when(planetService.getAllCountryAiLeaders(1L))
                    .thenReturn(Arrays.asList(leader1, leader2));

            ResponseEntity<List<AiCharacter>> response =
                    controller.getAllCountryAiLeaders(1L);

            assertEquals(200, response.getStatusCode().value());
            assertEquals(2, response.getBody().size());
        }
    }

    @Nested
    @DisplayName("GET /countries/{countryId}/ai-governors")
    class GetAllRegionAiGovernors {

        @Test
        @DisplayName("returns all AI governors for country")
        void returnsAllAiGovernors() {
            AiCharacter gov1 = new AiCharacter();
            gov1.setName("Gavin Newsom");
            when(planetService.getAllRegionAiGovernors(1L))
                    .thenReturn(Arrays.asList(gov1));

            ResponseEntity<List<AiCharacter>> response =
                    controller.getAllRegionAiGovernors(1L);

            assertEquals(200, response.getStatusCode().value());
            assertEquals(1, response.getBody().size());
        }
    }

    @Nested
    @DisplayName("POST /countries/{countryId}/ai-leader")
    class AssignCountryAiLeader {

        @Test
        @DisplayName("assigns AI leader to country")
        void assignsAiLeader() {
            Earth2026Country country = new Earth2026Country();
            country.setIsoCode("US");
            when(planetService.assignCountryAiLeader(1L, 50L))
                    .thenReturn(Optional.of(country));

            Map<String, Object> body = new HashMap<>();
            body.put("aiCharacterId", 50L);

            ResponseEntity<?> response = controller.assignCountryAiLeader(1L, body);

            assertEquals(200, response.getStatusCode().value());
            assertEquals("US", ((Earth2026Country) response.getBody()).getIsoCode());
            verify(planetService).assignCountryAiLeader(1L, 50L);
        }

        @Test
        @DisplayName("returns 404 when country not found")
        void returns404WhenCountryNotFound() {
            when(planetService.assignCountryAiLeader(999L, 50L))
                    .thenReturn(Optional.empty());

            Map<String, Object> body = new HashMap<>();
            body.put("aiCharacterId", 50L);

            ResponseEntity<?> response = controller.assignCountryAiLeader(999L, body);

            assertEquals(404, response.getStatusCode().value());
        }
    }

    @Nested
    @DisplayName("POST /regions/{regionId}/ai-governor")
    class AssignRegionAiGovernor {

        @Test
        @DisplayName("assigns AI governor to region")
        void assignsAiGovernor() {
            Earth2026Region region = new Earth2026Region();
            region.setName("California");
            when(planetService.assignRegionAiGovernor(1L, 50L))
                    .thenReturn(Optional.of(region));

            Map<String, Object> body = new HashMap<>();
            body.put("aiCharacterId", 50L);

            ResponseEntity<?> response = controller.assignRegionAiGovernor(1L, body);

            assertEquals(200, response.getStatusCode().value());
            assertEquals("California", ((Earth2026Region) response.getBody()).getName());
            verify(planetService).assignRegionAiGovernor(1L, 50L);
        }

        @Test
        @DisplayName("returns 404 when region not found")
        void returns404WhenRegionNotFound() {
            when(planetService.assignRegionAiGovernor(999L, 50L))
                    .thenReturn(Optional.empty());

            Map<String, Object> body = new HashMap<>();
            body.put("aiCharacterId", 50L);

            ResponseEntity<?> response = controller.assignRegionAiGovernor(999L, body);

            assertEquals(404, response.getStatusCode().value());
        }
    }

    // ------------------------------------------------------------------
    // Planet summary
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("GET /planets/{planetId}/summary")
    class GetPlanetSummary {

        @Test
        @DisplayName("returns complete planet summary")
        void returnsCompleteSummary() {
            Earth2026Planet earth = new Earth2026Planet();
            earth.setName("Earth");
            Earth2026Economy economy = new Earth2026Economy();
            economy.setGdpTrillions(100.0);

            when(planetService.getPlanet(1L)).thenReturn(Optional.of(earth));
            when(planetService.countCountries(1L)).thenReturn(5L);
            when(planetService.countJobs(1L)).thenReturn(10L);
            when(planetService.countAvailableContracts(1L)).thenReturn(3L);
            when(planetService.getEconomy(1L)).thenReturn(Optional.of(economy));

            ResponseEntity<Map<String, Object>> response =
                    controller.getPlanetSummary(1L);

            assertEquals(200, response.getStatusCode().value());
            Map<String, Object> body = response.getBody();
            assertNotNull(body);
            assertEquals(earth, body.get("planet"));
            assertEquals(5L, body.get("countryCount"));
            assertEquals(10L, body.get("jobCount"));
            assertEquals(3L, body.get("contractCount"));
            assertNotNull(body.get("economy"));
        }

        @Test
        @DisplayName("returns 404 when planet not found")
        void returns404WhenPlanetNotFound() {
            when(planetService.getPlanet(999L)).thenReturn(Optional.empty());

            ResponseEntity<Map<String, Object>> response =
                    controller.getPlanetSummary(999L);

            assertEquals(404, response.getStatusCode().value());
        }
    }
}
