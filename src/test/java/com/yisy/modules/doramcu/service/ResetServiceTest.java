package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.eve.model.EveCorporation;
import com.yisy.modules.doramcu.eve.model.Treasury;
import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.model.PromoCode;
import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import com.yisy.modules.doramcu.repository.EveCorporationRepository;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.repository.PromoCodeRepository;
import com.yisy.modules.doramcu.repository.TreasuryRepository;
import com.yisy.modules.doramcu.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResetService Tests")
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class ResetServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PromoCodeRepository promoCodeRepository;

    @Mock
    private EveCorporationRepository eveCorporationRepository;

    @Mock
    private TreasuryRepository treasuryRepository;

    @Mock
    private AiCharacterRepository aiCharacterRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private GalaxyService galaxyService;

    private ResetService resetService;

    @BeforeEach
    void setUp() {
        // Mock EntityManager.createNativeQuery() to return a mock Query
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(0);

        // Mock password encoder
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "encoded-" + invocation.getArgument(0));

        // Mock repository save methods to return the saved object
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            if (u.getId() == null) {
                u.setId(1L);
            }
            return u;
        });
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> {
            Player p = invocation.getArgument(0);
            if (p.getId() == null) {
                p.setId(1L);
            }
            return p;
        });
        when(eveCorporationRepository.save(any(EveCorporation.class))).thenAnswer(invocation -> {
            EveCorporation c = invocation.getArgument(0);
            if (c.getId() == null) {
                c.setId(1L);
            }
            return c;
        });
        when(treasuryRepository.save(any(Treasury.class))).thenAnswer(invocation -> {
            Treasury t = invocation.getArgument(0);
            if (t.getId() == null) {
                t.setId(1L);
            }
            return t;
        });
        when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(invocation -> {
            AiCharacter a = invocation.getArgument(0);
            if (a.getId() == null) {
                a.setId(1L);
            }
            return a;
        });

        resetService = new ResetService(userRepository, playerRepository, promoCodeRepository,
                eveCorporationRepository, treasuryRepository, aiCharacterRepository,
                passwordEncoder, galaxyService);

        // Inject the mocked entityManager via reflection since it's @PersistenceContext
        try {
            java.lang.reflect.Field emField = ResetService.class.getDeclaredField("entityManager");
            emField.setAccessible(true);
            emField.set(resetService, entityManager);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to inject entityManager", e);
        }
    }

    // ==================== ResetResult ====================

    @Nested
    @DisplayName("ResetResult")
    class ResetResultTests {

        @Test
        @DisplayName("should return correct values for all getters")
        void resetResult_shouldReturnCorrectValues() {
            List<String> tables = List.of("table1", "table2");
            List<String> entities = List.of("entity1");
            List<String> errors = List.of("error1");

            ResetService.ResetResult result = new ResetService.ResetResult(
                    true, "message", tables, entities, errors);

            assertTrue(result.isSuccess());
            assertEquals("message", result.getMessage());
            assertEquals(tables, result.getTablesCleaned());
            assertEquals(entities, result.getEntitiesRepopulated());
            assertEquals(errors, result.getErrors());
        }

        @Test
        @DisplayName("should support failure result")
        void resetResult_shouldSupportFailure() {
            ResetService.ResetResult result = new ResetService.ResetResult(
                    false, "failed", List.of(), List.of(), List.of("error"));

            assertFalse(result.isSuccess());
            assertEquals("failed", result.getMessage());
            assertTrue(result.getTablesCleaned().isEmpty());
            assertTrue(result.getEntitiesRepopulated().isEmpty());
            assertEquals(1, result.getErrors().size());
        }
    }

    // ==================== executeFullReset() ====================

    @Nested
    @DisplayName("executeFullReset()")
    class ExecuteFullResetTests {

        @Test
        @DisplayName("should return success when all tables truncated and seed data repopulated")
        void executeFullReset_success_shouldReturnSuccess() {
            // Mock RESET promo code found
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            resetCode.setUsedCount(5);
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            ResetService.ResetResult result = resetService.executeFullReset();

            assertTrue(result.isSuccess());
            assertNotNull(result.getMessage());
            assertTrue(result.getMessage().contains("Reset complete"));
        }

        @Test
        @DisplayName("should truncate all 37 tables on success")
        void executeFullReset_success_shouldTruncateAllTables() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            ResetService.ResetResult result = resetService.executeFullReset();

            // 12 captain's log + 3 shared char + 6 gameplay + 15 galaxy + 1 users = 37
            assertEquals(37, result.getTablesCleaned().size());
        }

        @Test
        @DisplayName("should include all captain's log tables in cleaned list")
        void executeFullReset_success_shouldIncludeCaptainLogTables() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            ResetService.ResetResult result = resetService.executeFullReset();

            List<String> cleaned = result.getTablesCleaned();
            assertTrue(cleaned.contains("cl_combats"));
            assertTrue(cleaned.contains("cl_crew_members"));
            assertTrue(cleaned.contains("cl_character_talents"));
            assertTrue(cleaned.contains("cl_ship_talents"));
            assertTrue(cleaned.contains("cl_equipment"));
            assertTrue(cleaned.contains("cl_values"));
            assertTrue(cleaned.contains("cl_focuses"));
            assertTrue(cleaned.contains("cl_log_entries"));
            assertTrue(cleaned.contains("cl_missions"));
            assertTrue(cleaned.contains("cl_campaigns"));
            assertTrue(cleaned.contains("cl_starships"));
            assertTrue(cleaned.contains("cl_characters"));
        }

        @Test
        @DisplayName("should include all shared character tables in cleaned list")
        void executeFullReset_success_shouldIncludeSharedCharTables() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            ResetService.ResetResult result = resetService.executeFullReset();

            List<String> cleaned = result.getTablesCleaned();
            assertTrue(cleaned.contains("shared_character_travel_log"));
            assertTrue(cleaned.contains("shared_character_skills"));
            assertTrue(cleaned.contains("shared_characters"));
        }

        @Test
        @DisplayName("should include all gameplay tables in cleaned list")
        void executeFullReset_success_shouldIncludeGameplayTables() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            ResetService.ResetResult result = resetService.executeFullReset();

            List<String> cleaned = result.getTablesCleaned();
            assertTrue(cleaned.contains("game_sessions_persisted"));
            assertTrue(cleaned.contains("treasuries"));
            assertTrue(cleaned.contains("eve_corporations"));
            assertTrue(cleaned.contains("ai_characters"));
            assertTrue(cleaned.contains("devices"));
            assertTrue(cleaned.contains("players"));
        }

        @Test
        @DisplayName("should include all galaxy tables in cleaned list")
        void executeFullReset_success_shouldIncludeGalaxyTables() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            ResetService.ResetResult result = resetService.executeFullReset();

            List<String> cleaned = result.getTablesCleaned();
            assertTrue(cleaned.contains("wormholes"));
            assertTrue(cleaned.contains("space_stations"));
            assertTrue(cleaned.contains("comets"));
            assertTrue(cleaned.contains("black_holes"));
            assertTrue(cleaned.contains("nebulas"));
            assertTrue(cleaned.contains("gas_giants"));
            assertTrue(cleaned.contains("asteroid_belts"));
            assertTrue(cleaned.contains("asteroids"));
            assertTrue(cleaned.contains("moons"));
            assertTrue(cleaned.contains("planets"));
            assertTrue(cleaned.contains("stars"));
            assertTrue(cleaned.contains("solar_systems"));
            assertTrue(cleaned.contains("sub_sectors"));
            assertTrue(cleaned.contains("sectors"));
            assertTrue(cleaned.contains("galaxies"));
        }

        @Test
        @DisplayName("should include users table in cleaned list")
        void executeFullReset_success_shouldIncludeUsersTable() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            ResetService.ResetResult result = resetService.executeFullReset();

            assertTrue(result.getTablesCleaned().contains("users"));
        }

        @Test
        @DisplayName("should repopulate all seed entities on success")
        void executeFullReset_success_shouldRepopulateAllEntities() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            ResetService.ResetResult result = resetService.executeFullReset();

            List<String> entities = result.getEntitiesRepopulated();

            // Users: Sif Younger, Butch Younger, player1/player2/player3 = 3 entries
            assertTrue(entities.contains("User: Sif Younger (admin)"));
            assertTrue(entities.contains("User: Butch Younger (player)"));
            assertTrue(entities.contains("Users: player1, player2, player3"));

            // Players: Sif, Butch, player1-6 = 8 entries
            assertTrue(entities.contains("Player: Sif Younger (admin, CEO, Interstellar Alliance)"));
            assertTrue(entities.contains("Player: Butch Younger (VP, Interstellar Alliance)"));
            assertTrue(entities.contains("Player: Terran Republic"));
            assertTrue(entities.contains("Player: Solar Imperium"));
            assertTrue(entities.contains("Player: United Colonies"));
            assertTrue(entities.contains("Player: Stellar Hegemony"));
            assertTrue(entities.contains("Player: Divine Theocracy"));
            assertTrue(entities.contains("Player: Corporate Syndicate"));

            // Corporation
            assertTrue(entities.contains("Corporation: Younger Industries (YISY)"));

            // Treasuries
            assertTrue(entities.contains("Treasury: Sif Younger (13 oz silver)"));
            assertTrue(entities.contains("Treasury: Butch Younger (2 oz silver)"));

            // AI Character
            assertTrue(entities.contains("AI Character: Sif Younger (Leader)"));
        }

        @Test
        @DisplayName("should repopulate exactly 17 entity entries")
        void executeFullReset_success_shouldRepopulateCorrectCount() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            ResetService.ResetResult result = resetService.executeFullReset();

            // 3 user entries + 8 player entries + 1 corporation + 2 treasuries + 1 AI = 15
            assertEquals(15, result.getEntitiesRepopulated().size());
        }

        @Test
        @DisplayName("should have no errors on success")
        void executeFullReset_success_shouldHaveNoErrors() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            ResetService.ResetResult result = resetService.executeFullReset();

            assertTrue(result.getErrors().isEmpty());
        }

        @Test
        @DisplayName("should disable and re-enable foreign key checks")
        void executeFullReset_shouldToggleForeignKeyChecks() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            resetService.executeFullReset();

            // Verify SET FOREIGN_KEY_CHECKS called twice: once to disable, once to re-enable
            verify(entityManager, atLeast(2)).createNativeQuery(anyString());
            verify(entityManager).createNativeQuery("SET FOREIGN_KEY_CHECKS = 0");
            verify(entityManager).createNativeQuery("SET FOREIGN_KEY_CHECKS = 1");
        }

        @Test
        @DisplayName("should save Sif Younger user with correct properties")
        void executeFullReset_shouldSaveSifUser() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            resetService.executeFullReset();

            verify(userRepository).save(argThat(user ->
                    "Sif Younger".equals(user.getUsername())
                            && "Sif".equals(user.getFirstName())
                            && "Younger".equals(user.getLastName())
                            && "6196596073".equals(user.getPhoneNumber())
                            && "Milky Way".equals(user.getGalaxyName())
            ));
        }

        @Test
        @DisplayName("should save Butch Younger user with correct properties")
        void executeFullReset_shouldSaveButchUser() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            resetService.executeFullReset();

            verify(userRepository).save(argThat(user ->
                    "Butch Younger".equals(user.getUsername())
                            && "Butch".equals(user.getFirstName())
                            && "Younger".equals(user.getLastName())
                            && "Milky Way".equals(user.getGalaxyName())
            ));
        }

        @Test
        @DisplayName("should save Sif Younger player with admin and CEO role")
        void executeFullReset_shouldSaveSifPlayer() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            resetService.executeFullReset();

            verify(playerRepository).save(argThat(player ->
                    "YoungerIndustries@yahoo.com".equals(player.getEmail())
                            && "Interstellar Alliance".equals(player.getEmpireName())
                            && player.isAdmin()
                            && "CEO".equals(player.getCorporateRole())
                            && Player.GovernmentType.CORPORATE.equals(player.getGovernmentType())
                            && Player.Ethos.LIBERTARIAN.equals(player.getEthos())
            ));
        }

        @Test
        @DisplayName("should save Butch Younger player with VP role")
        void executeFullReset_shouldSaveButchPlayer() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            resetService.executeFullReset();

            verify(playerRepository).save(argThat(player ->
                    "butch.younger@empire.com".equals(player.getEmail())
                            && "Interstellar Alliance".equals(player.getEmpireName())
                            && !player.isAdmin()
                            && "Vice President".equals(player.getCorporateRole())
            ));
        }

        @Test
        @DisplayName("should save Younger Industries corporation")
        void executeFullReset_shouldSaveCorporation() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            resetService.executeFullReset();

            verify(eveCorporationRepository).save(argThat(corp ->
                    "Younger Industries".equals(corp.getName())
                            && "YISY".equals(corp.getTicker())
                            && "Sif Younger".equals(corp.getCeoName())
            ));
        }

        @Test
        @DisplayName("should save Sif Younger treasury with 13 oz silver")
        void executeFullReset_shouldSaveSifTreasury() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            resetService.executeFullReset();

            verify(treasuryRepository).save(argThat(treasury ->
                    "SILVER".equals(treasury.getResourceType())
                            && new BigDecimal("13.00").equals(treasury.getQuantityOunces())
                            && "Sif Younger".equals(treasury.getOwnerId())
                            && "PLAYER".equals(treasury.getOwnerType())
            ));
        }

        @Test
        @DisplayName("should save Butch Younger treasury with 2 oz silver")
        void executeFullReset_shouldSaveButchTreasury() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            ResetService.ResetResult result = resetService.executeFullReset();

            verify(treasuryRepository).save(argThat(treasury ->
                    "SILVER".equals(treasury.getResourceType())
                            && new BigDecimal("2.00").equals(treasury.getQuantityOunces())
                            && "Butch Younger".equals(treasury.getOwnerId())
            ));
        }

        @Test
        @DisplayName("should save Sif Younger AI character")
        void executeFullReset_shouldSaveAiCharacter() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            resetService.executeFullReset();

            verify(aiCharacterRepository).save(argThat(ai ->
                    "Sif Younger".equals(ai.getName())
                            && "Leader".equals(ai.getRole())
                            && "Interstellar Alliance".equals(ai.getEmpireName())
                            && ai.isActive()
                            && ai.isCanPromoteToCoServer()
            ));
        }

        @Test
        @DisplayName("should encode passwords using PasswordEncoder")
        void executeFullReset_shouldEncodePasswords() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            resetService.executeFullReset();

            verify(passwordEncoder).encode("Lazarus2026");
            verify(passwordEncoder).encode("3214");
            verify(passwordEncoder).encode("123456");
            verify(passwordEncoder).encode("234567");
            verify(passwordEncoder).encode("345678");
        }

        @Test
        @DisplayName("should reset RESET promo code usage count to 0")
        void executeFullReset_shouldResetPromoCodeUsage() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            resetCode.setUsedCount(5);
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            resetService.executeFullReset();

            assertEquals(0, resetCode.getUsedCount());
            verify(promoCodeRepository).save(resetCode);
        }

        @Test
        @DisplayName("should return result with correct message format")
        void executeFullReset_shouldReturnFormattedMessage() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            ResetService.ResetResult result = resetService.executeFullReset();

            assertTrue(result.getMessage().contains("37 tables cleaned"));
            assertTrue(result.getMessage().contains("15 entities repopulated"));
        }

        // ---- Partial table truncation failures ----

        @Test
        @DisplayName("should continue and record error when a table truncation fails")
        void executeFullReset_tableTruncateFailure_shouldRecordError() {
            // Make one table throw an exception
            when(entityManager.createNativeQuery("TRUNCATE TABLE cl_combats"))
                    .thenThrow(new RuntimeException("Table not found"));

            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            ResetService.ResetResult result = resetService.executeFullReset();

            // Still succeeds overall
            assertTrue(result.isSuccess());
            // But has an error recorded
            assertFalse(result.getErrors().isEmpty());
            assertTrue(result.getErrors().get(0).contains("cl_combats"));
            // One fewer table cleaned
            assertEquals(36, result.getTablesCleaned().size());
            assertFalse(result.getTablesCleaned().contains("cl_combats"));
        }

        @Test
        @DisplayName("should continue when multiple table truncations fail")
        void executeFullReset_multipleTableFailures_shouldRecordAllErrors() {
            when(entityManager.createNativeQuery("TRUNCATE TABLE cl_combats"))
                    .thenThrow(new RuntimeException("Error 1"));
            when(entityManager.createNativeQuery("TRUNCATE TABLE galaxies"))
                    .thenThrow(new RuntimeException("Error 2"));

            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            ResetService.ResetResult result = resetService.executeFullReset();

            assertTrue(result.isSuccess());
            assertEquals(2, result.getErrors().size());
            assertEquals(35, result.getTablesCleaned().size());
        }

        // ---- Seed data repopulation failure ----

        @Test
        @DisplayName("should record error when seed data repopulation fails")
        void executeFullReset_seedDataFailure_shouldRecordError() {
            // Make userRepository.save throw on the first call
            when(userRepository.save(any(User.class)))
                    .thenThrow(new RuntimeException("Database connection lost"));

            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            ResetService.ResetResult result = resetService.executeFullReset();

            // Still succeeds overall (tables are cleaned)
            assertTrue(result.isSuccess());
            // But has an error about seed data
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Seed data repopulation failed")));
            // No entities repopulated
            assertTrue(result.getEntitiesRepopulated().isEmpty());
        }

        // ---- Promo code reset edge cases ----

        @Test
        @DisplayName("should handle RESET promo code not found gracefully")
        void executeFullReset_promoCodeNotFound_shouldSkipUsageReset() {
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.empty());

            ResetService.ResetResult result = resetService.executeFullReset();

            assertTrue(result.isSuccess());
            assertTrue(result.getErrors().isEmpty());
            // Should not attempt to save
            verify(promoCodeRepository, never()).save(any(PromoCode.class));
        }

        @Test
        @DisplayName("should record error when promo code reset fails")
        void executeFullReset_promoCodeResetFailure_shouldRecordError() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class)))
                    .thenThrow(new RuntimeException("Lock timeout"));

            ResetService.ResetResult result = resetService.executeFullReset();

            assertTrue(result.isSuccess());
            assertFalse(result.getErrors().isEmpty());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Failed to reset promo code usage")));
        }

        // ---- Table order verification ----

        @Test
        @DisplayName("should truncate tables in correct dependency order")
        void executeFullReset_shouldTruncateInOrder() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            ResetService.ResetResult result = resetService.executeFullReset();

            List<String> cleaned = result.getTablesCleaned();

            // Galaxy leaf entities should come before parent entities
            int galaxiesIndex = cleaned.indexOf("galaxies");
            int sectorsIndex = cleaned.indexOf("sectors");
            int starsIndex = cleaned.indexOf("stars");
            int planetsIndex = cleaned.indexOf("planets");

            // Parents (galaxies, sectors) should be after children (stars, planets)
            assertTrue(galaxiesIndex > starsIndex, "galaxies should be truncated after stars");
            assertTrue(sectorsIndex > planetsIndex, "sectors should be truncated after planets");
        }

        @Test
        @DisplayName("should truncate users table last among data tables")
        void executeFullReset_shouldTruncateUsersLast() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            ResetService.ResetResult result = resetService.executeFullReset();

            List<String> cleaned = result.getTablesCleaned();
            // Users should be the last table truncated (index 36, 0-based)
            assertEquals("users", cleaned.get(cleaned.size() - 1));
        }

        @Test
        @DisplayName("should call truncate for each table exactly once")
        void executeFullReset_shouldCallTruncateOncePerTable() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            resetService.executeFullReset();

            // 37 TRUNCATE calls + 2 SET FOREIGN_KEY_CHECKS calls = 39 native queries
            verify(entityManager, times(39)).createNativeQuery(anyString());
        }

        @Test
        @DisplayName("should save users in correct order (Sif, Butch, player1-3)")
        void executeFullReset_shouldSaveUsersInOrder() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            resetService.executeFullReset();

            // 5 users saved: Sif, Butch, player1, player2, player3
            verify(userRepository, times(5)).save(any(User.class));
        }

        @Test
        @DisplayName("should save 8 players total")
        void executeFullReset_shouldSaveEightPlayers() {
            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            resetService.executeFullReset();

            verify(playerRepository, times(8)).save(any(Player.class));
        }

        @Test
        @DisplayName("should set correct invariants on foreign key checks even on error")
        void executeFullReset_shouldReenableForeignKeyChecksOnError() {
            // Make a table truncation fail
            when(entityManager.createNativeQuery("TRUNCATE TABLE cl_combats"))
                    .thenThrow(new RuntimeException("Table not found"));

            PromoCode resetCode = new PromoCode();
            resetCode.setCode("RESET");
            when(promoCodeRepository.findByCode("RESET")).thenReturn(Optional.of(resetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(resetCode);

            resetService.executeFullReset();

            // FOREIGN_KEY_CHECKS = 1 must be called in the finally block
            verify(entityManager).createNativeQuery("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    // ==================== executeGalaxyReset() ====================

    @Nested
    @DisplayName("executeGalaxyReset()")
    class ExecuteGalaxyResetTests {

        @Test
        @DisplayName("should return success when all galaxy tables truncated and Milky Way regenerated")
        void executeGalaxyReset_success_shouldReturnSuccess() {
            // Mock GALAXY_RESET promo code found
            PromoCode galaxyResetCode = new PromoCode();
            galaxyResetCode.setCode("GALAXY_RESET");
            galaxyResetCode.setUsedCount(3);
            when(promoCodeRepository.findByCode("GALAXY_RESET")).thenReturn(Optional.of(galaxyResetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(galaxyResetCode);

            // Mock player repository — return empty list (no players)
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());

            ResetService.ResetResult result = resetService.executeGalaxyReset();

            assertTrue(result.isSuccess());
            assertNotNull(result.getMessage());
            assertTrue(result.getMessage().contains("Galaxy reset complete"));
        }

        @Test
        @DisplayName("should truncate exactly 15 galaxy tables")
        void executeGalaxyReset_success_shouldTruncate15GalaxyTables() {
            PromoCode galaxyResetCode = new PromoCode();
            galaxyResetCode.setCode("GALAXY_RESET");
            when(promoCodeRepository.findByCode("GALAXY_RESET")).thenReturn(Optional.of(galaxyResetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(galaxyResetCode);
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());

            ResetService.ResetResult result = resetService.executeGalaxyReset();

            assertEquals(15, result.getTablesCleaned().size());
        }

        @Test
        @DisplayName("should include all galaxy tables in cleaned list")
        void executeGalaxyReset_success_shouldIncludeAllGalaxyTables() {
            PromoCode galaxyResetCode = new PromoCode();
            galaxyResetCode.setCode("GALAXY_RESET");
            when(promoCodeRepository.findByCode("GALAXY_RESET")).thenReturn(Optional.of(galaxyResetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(galaxyResetCode);
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());

            ResetService.ResetResult result = resetService.executeGalaxyReset();

            List<String> cleaned = result.getTablesCleaned();
            assertTrue(cleaned.contains("wormholes"));
            assertTrue(cleaned.contains("space_stations"));
            assertTrue(cleaned.contains("comets"));
            assertTrue(cleaned.contains("black_holes"));
            assertTrue(cleaned.contains("nebulas"));
            assertTrue(cleaned.contains("gas_giants"));
            assertTrue(cleaned.contains("asteroid_belts"));
            assertTrue(cleaned.contains("asteroids"));
            assertTrue(cleaned.contains("moons"));
            assertTrue(cleaned.contains("planets"));
            assertTrue(cleaned.contains("stars"));
            assertTrue(cleaned.contains("solar_systems"));
            assertTrue(cleaned.contains("sub_sectors"));
            assertTrue(cleaned.contains("sectors"));
            assertTrue(cleaned.contains("galaxies"));
        }

        @Test
        @DisplayName("should NOT truncate players, users, or character tables")
        void executeGalaxyReset_success_shouldNotTruncateNonGalaxyTables() {
            PromoCode galaxyResetCode = new PromoCode();
            galaxyResetCode.setCode("GALAXY_RESET");
            when(promoCodeRepository.findByCode("GALAXY_RESET")).thenReturn(Optional.of(galaxyResetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(galaxyResetCode);
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());

            ResetService.ResetResult result = resetService.executeGalaxyReset();

            List<String> cleaned = result.getTablesCleaned();
            // These tables should NOT be truncated
            assertFalse(cleaned.contains("players"));
            assertFalse(cleaned.contains("users"));
            assertFalse(cleaned.contains("cl_characters"));
            assertFalse(cleaned.contains("shared_characters"));
            assertFalse(cleaned.contains("ai_characters"));
            assertFalse(cleaned.contains("treasuries"));
            assertFalse(cleaned.contains("eve_corporations"));
            assertFalse(cleaned.contains("devices"));
            assertFalse(cleaned.contains("game_sessions_persisted"));
        }

        @Test
        @DisplayName("should call GalaxyService to regenerate both Milky Way and The Void")
        void executeGalaxyReset_success_shouldRegenerateBothGalaxies() {
            PromoCode galaxyResetCode = new PromoCode();
            galaxyResetCode.setCode("GALAXY_RESET");
            when(promoCodeRepository.findByCode("GALAXY_RESET")).thenReturn(Optional.of(galaxyResetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(galaxyResetCode);
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());

            resetService.executeGalaxyReset();

            // GalaxyService.generateAndSaveGalaxyWithEmpires should be called twice: Milky Way + The Void
            verify(galaxyService, times(2)).generateAndSaveGalaxyWithEmpires(
                    anyString(), anyMap(), anyInt(), anyInt(), anyDouble(), anyDouble(),
                    anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                    anyDouble(), anyLong()
            );
        }

        @Test
        @DisplayName("should regenerate Milky Way with Human players and The Void with Alien players")
        void executeGalaxyReset_withPlayers_shouldSeparateEmpires() {
            PromoCode galaxyResetCode = new PromoCode();
            galaxyResetCode.setCode("GALAXY_RESET");
            when(promoCodeRepository.findByCode("GALAXY_RESET")).thenReturn(Optional.of(galaxyResetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(galaxyResetCode);

            // Mock players: 1 Human (Interstellar Alliance) + 2 Alien
            Player human = new Player();
            human.setId(1L);
            human.setEmpireName("Interstellar Alliance");
            Player alien1 = new Player();
            alien1.setId(2L);
            alien1.setEmpireName("Terran Republic");
            Player alien2 = new Player();
            alien2.setId(3L);
            alien2.setEmpireName("Solar Imperium");
            when(playerRepository.findAll()).thenReturn(List.of(human, alien1, alien2));

            resetService.executeGalaxyReset();

            // Verify Milky Way got the Human empire
            verify(galaxyService).generateAndSaveGalaxyWithEmpires(eq("Milky Way"), argThat(map -> map.size() == 1 && map.containsKey(1)),
                    anyInt(), anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                    anyDouble(), anyDouble(), anyLong());
            // Verify The Void got the Alien empires
            verify(galaxyService).generateAndSaveGalaxyWithEmpires(eq("The Void"), argThat(map -> map.size() == 2 && map.containsKey(2) && map.containsKey(3)),
                    anyInt(), anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                    anyDouble(), anyDouble(), anyLong());
        }

        @Test
        @DisplayName("should repopulate both Milky Way and The Void entity entries plus AI characters")
        void executeGalaxyReset_success_shouldRepopulateBothGalaxies() {
            PromoCode galaxyResetCode = new PromoCode();
            galaxyResetCode.setCode("GALAXY_RESET");
            when(promoCodeRepository.findByCode("GALAXY_RESET")).thenReturn(Optional.of(galaxyResetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(galaxyResetCode);
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());
            when(aiCharacterRepository.findByEmpireName(anyString())).thenReturn(new ArrayList<>());

            ResetService.ResetResult result = resetService.executeGalaxyReset();

            List<String> entities = result.getEntitiesRepopulated();
            // 2 galaxies (Milky Way + The Void) + 7 Traveller AI + 7 Star Trek AI = 16 entities
            assertEquals(16, entities.size());
            assertTrue(entities.stream().anyMatch(e -> e.contains("Milky Way")));
            assertTrue(entities.stream().anyMatch(e -> e.contains("The Void")));
            assertTrue(entities.stream().anyMatch(e -> e.contains("Aslan Warlord")));
            assertTrue(entities.stream().anyMatch(e -> e.contains("Vargr Pack Lord")));
            assertTrue(entities.stream().anyMatch(e -> e.contains("Klingon Warlord")));
            assertTrue(entities.stream().anyMatch(e -> e.contains("Ferengi Grand Nagus")));
        }

        @Test
        @DisplayName("should reset GALAXY_RESET promo code usage count")
        void executeGalaxyReset_success_shouldResetPromoCodeUsage() {
            PromoCode galaxyResetCode = new PromoCode();
            galaxyResetCode.setCode("GALAXY_RESET");
            galaxyResetCode.setUsedCount(5);
            when(promoCodeRepository.findByCode("GALAXY_RESET")).thenReturn(Optional.of(galaxyResetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(galaxyResetCode);
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());

            resetService.executeGalaxyReset();

            verify(promoCodeRepository).save(argThat(code -> code.getUsedCount() == 0));
        }

        @Test
        @DisplayName("should disable and re-enable foreign key checks")
        void executeGalaxyReset_shouldToggleForeignKeyChecks() {
            PromoCode galaxyResetCode = new PromoCode();
            galaxyResetCode.setCode("GALAXY_RESET");
            when(promoCodeRepository.findByCode("GALAXY_RESET")).thenReturn(Optional.of(galaxyResetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(galaxyResetCode);
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());

            resetService.executeGalaxyReset();

            verify(entityManager).createNativeQuery("SET FOREIGN_KEY_CHECKS = 0");
            verify(entityManager).createNativeQuery("SET FOREIGN_KEY_CHECKS = 1");
        }

        @Test
        @DisplayName("should have no errors on success")
        void executeGalaxyReset_success_shouldHaveNoErrors() {
            PromoCode galaxyResetCode = new PromoCode();
            galaxyResetCode.setCode("GALAXY_RESET");
            when(promoCodeRepository.findByCode("GALAXY_RESET")).thenReturn(Optional.of(galaxyResetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(galaxyResetCode);
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());

            ResetService.ResetResult result = resetService.executeGalaxyReset();

            assertTrue(result.getErrors().isEmpty());
        }

        @Test
        @DisplayName("should record error when galaxy table truncation fails")
        void executeGalaxyReset_tableTruncationFailure_shouldRecordError() {
            PromoCode galaxyResetCode = new PromoCode();
            galaxyResetCode.setCode("GALAXY_RESET");
            when(promoCodeRepository.findByCode("GALAXY_RESET")).thenReturn(Optional.of(galaxyResetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(galaxyResetCode);
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());

            // Make one galaxy table truncation fail
            when(entityManager.createNativeQuery("TRUNCATE TABLE wormholes"))
                    .thenThrow(new RuntimeException("Access denied"));

            ResetService.ResetResult result = resetService.executeGalaxyReset();

            assertTrue(result.isSuccess());
            assertFalse(result.getErrors().isEmpty());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("wormholes")));
        }

        @Test
        @DisplayName("should record error when Milky Way regeneration fails")
        void executeGalaxyReset_milkyWayRegenerationFailure_shouldRecordError() {
            PromoCode galaxyResetCode = new PromoCode();
            galaxyResetCode.setCode("GALAXY_RESET");
            when(promoCodeRepository.findByCode("GALAXY_RESET")).thenReturn(Optional.of(galaxyResetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(galaxyResetCode);
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());

            // Make galaxy generation fail
            doThrow(new RuntimeException("Out of memory"))
                    .when(galaxyService).generateAndSaveGalaxyWithEmpires(
                            anyString(), anyMap(), anyInt(), anyInt(), anyDouble(), anyDouble(),
                            anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                            anyDouble(), anyLong());

            ResetService.ResetResult result = resetService.executeGalaxyReset();

            assertTrue(result.isSuccess());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Milky Way regeneration failed")));
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("The Void regeneration failed")));
        }

        @Test
        @DisplayName("should handle GALAXY_RESET promo code not found gracefully")
        void executeGalaxyReset_promoCodeNotFound_shouldSkipUsageReset() {
            when(promoCodeRepository.findByCode("GALAXY_RESET")).thenReturn(Optional.empty());
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());

            ResetService.ResetResult result = resetService.executeGalaxyReset();

            assertTrue(result.isSuccess());
            // Should not throw — promo code not found is handled gracefully
        }

        @Test
        @DisplayName("should truncate galaxy tables in correct dependency order")
        void executeGalaxyReset_shouldTruncateInOrder() {
            PromoCode galaxyResetCode = new PromoCode();
            galaxyResetCode.setCode("GALAXY_RESET");
            when(promoCodeRepository.findByCode("GALAXY_RESET")).thenReturn(Optional.of(galaxyResetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(galaxyResetCode);
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());

            ResetService.ResetResult result = resetService.executeGalaxyReset();

            List<String> cleaned = result.getTablesCleaned();

            // Galaxy leaf entities should come before parent entities
            int galaxiesIndex = cleaned.indexOf("galaxies");
            int sectorsIndex = cleaned.indexOf("sectors");
            int starsIndex = cleaned.indexOf("stars");
            int planetsIndex = cleaned.indexOf("planets");

            // Parents (galaxies, sectors) should be after children (stars, planets)
            assertTrue(galaxiesIndex > starsIndex, "galaxies should be truncated after stars");
            assertTrue(sectorsIndex > planetsIndex, "sectors should be truncated after planets");
        }

        @Test
        @DisplayName("should re-enable foreign key checks even on error")
        void executeGalaxyReset_shouldReenableForeignKeyChecksOnError() {
            // Make a table truncation fail
            when(entityManager.createNativeQuery("TRUNCATE TABLE wormholes"))
                    .thenThrow(new RuntimeException("Table not found"));

            PromoCode galaxyResetCode = new PromoCode();
            galaxyResetCode.setCode("GALAXY_RESET");
            when(promoCodeRepository.findByCode("GALAXY_RESET")).thenReturn(Optional.of(galaxyResetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(galaxyResetCode);
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());

            resetService.executeGalaxyReset();

            // FOREIGN_KEY_CHECKS = 1 must be called in the finally block
            verify(entityManager).createNativeQuery("SET FOREIGN_KEY_CHECKS = 1");
        }

        @Test
        @DisplayName("should call truncate for each galaxy table exactly once")
        void executeGalaxyReset_shouldCallTruncateOncePerTable() {
            PromoCode galaxyResetCode = new PromoCode();
            galaxyResetCode.setCode("GALAXY_RESET");
            when(promoCodeRepository.findByCode("GALAXY_RESET")).thenReturn(Optional.of(galaxyResetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(galaxyResetCode);
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());

            resetService.executeGalaxyReset();

            // 15 TRUNCATE calls + 2 SET FOREIGN_KEY_CHECKS calls = 17 native queries minimum
            // (GalaxyService may create additional queries during generation)
            verify(entityManager, times(15)).createNativeQuery(startsWith("TRUNCATE TABLE"));
        }

        @Test
        @DisplayName("should record errors when multiple galaxy tables fail to truncate")
        void executeGalaxyReset_multipleTableFailures_shouldRecordAllErrors() {
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());

            // Make multiple galaxy table truncations fail
            when(entityManager.createNativeQuery("TRUNCATE TABLE wormholes"))
                    .thenThrow(new RuntimeException("Access denied"));
            when(entityManager.createNativeQuery("TRUNCATE TABLE planets"))
                    .thenThrow(new RuntimeException("Lock timeout"));

            ResetService.ResetResult result = resetService.executeGalaxyReset();

            assertTrue(result.isSuccess());
            assertEquals(2, result.getErrors().size());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("wormholes")));
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("planets")));
        }

        @Test
        @DisplayName("should record error when GALAXY_RESET promo code save fails")
        void executeGalaxyReset_promoCodeSaveFailure_shouldRecordError() {
            PromoCode galaxyResetCode = new PromoCode();
            galaxyResetCode.setCode("GALAXY_RESET");
            when(promoCodeRepository.findByCode("GALAXY_RESET")).thenReturn(Optional.of(galaxyResetCode));
            when(promoCodeRepository.save(any(PromoCode.class)))
                    .thenThrow(new RuntimeException("Connection lost"));
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());

            ResetService.ResetResult result = resetService.executeGalaxyReset();

            assertTrue(result.isSuccess());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("GALAXY_RESET")));
        }

        @Test
        @DisplayName("should include correct table and entity counts in message")
        void executeGalaxyReset_success_shouldHaveCorrectCountsInMessage() {
            PromoCode galaxyResetCode = new PromoCode();
            galaxyResetCode.setCode("GALAXY_RESET");
            when(promoCodeRepository.findByCode("GALAXY_RESET")).thenReturn(Optional.of(galaxyResetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(galaxyResetCode);
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());
            when(aiCharacterRepository.findByEmpireName(anyString())).thenReturn(new ArrayList<>());

            ResetService.ResetResult result = resetService.executeGalaxyReset();

            assertTrue(result.getMessage().contains("15"));
            // 2 galaxies + 7 Traveller AI + 7 Star Trek AI = 16 entities repopulated
            assertTrue(result.getMessage().contains("16"));
            assertTrue(result.getMessage().contains("Players and characters retained"));
        }

        @Test
        @DisplayName("should use GAME_GALAXY_SEED for Milky Way and VOID_GALAXY_SEED for The Void")
        void executeGalaxyReset_shouldUseCorrectSeeds() {
            PromoCode galaxyResetCode = new PromoCode();
            galaxyResetCode.setCode("GALAXY_RESET");
            when(promoCodeRepository.findByCode("GALAXY_RESET")).thenReturn(Optional.of(galaxyResetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(galaxyResetCode);
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());

            resetService.executeGalaxyReset();

            // Verify Milky Way uses GAME_GALAXY_SEED (42L)
            verify(galaxyService).generateAndSaveGalaxyWithEmpires(
                    eq("Milky Way"), anyMap(), anyInt(), anyInt(), anyDouble(), anyDouble(),
                    anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                    anyDouble(), eq(42L)
            );
            // Verify The Void uses VOID_GALAXY_SEED (54321L)
            verify(galaxyService).generateAndSaveGalaxyWithEmpires(
                    eq("The Void"), anyMap(), anyInt(), anyInt(), anyDouble(), anyDouble(),
                    anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                    anyDouble(), eq(54321L)
            );
        }

        @Test
        @DisplayName("should pass correct galaxy names to GalaxyService")
        void executeGalaxyReset_shouldPassBothGalaxyNames() {
            PromoCode galaxyResetCode = new PromoCode();
            galaxyResetCode.setCode("GALAXY_RESET");
            when(promoCodeRepository.findByCode("GALAXY_RESET")).thenReturn(Optional.of(galaxyResetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(galaxyResetCode);
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());

            resetService.executeGalaxyReset();

            verify(galaxyService).generateAndSaveGalaxyWithEmpires(eq("Milky Way"), anyMap(), anyInt(), anyInt(),
                    anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                    anyDouble(), anyDouble(), anyLong());
            verify(galaxyService).generateAndSaveGalaxyWithEmpires(eq("The Void"), anyMap(), anyInt(), anyInt(),
                    anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                    anyDouble(), anyDouble(), anyLong());
        }

        @Test
        @DisplayName("should handle single non-first table truncation failure")
        void executeGalaxyReset_nonFirstTableFailure_shouldContinueAndRecordError() {
            PromoCode galaxyResetCode = new PromoCode();
            galaxyResetCode.setCode("GALAXY_RESET");
            when(promoCodeRepository.findByCode("GALAXY_RESET")).thenReturn(Optional.of(galaxyResetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(galaxyResetCode);
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());

            // Make the last galaxy table (galaxies) fail
            when(entityManager.createNativeQuery("TRUNCATE TABLE galaxies"))
                    .thenThrow(new RuntimeException("Foreign key constraint"));

            ResetService.ResetResult result = resetService.executeGalaxyReset();

            assertTrue(result.isSuccess());
            // Should still have truncated the other 14 tables
            assertEquals(14, result.getTablesCleaned().size());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("galaxies")));
        }

        @Test
        @DisplayName("should continue regeneration even with no players")
        void executeGalaxyReset_noPlayers_shouldStillRegenerate() {
            PromoCode galaxyResetCode = new PromoCode();
            galaxyResetCode.setCode("GALAXY_RESET");
            when(promoCodeRepository.findByCode("GALAXY_RESET")).thenReturn(Optional.of(galaxyResetCode));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(galaxyResetCode);
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());

            ResetService.ResetResult result = resetService.executeGalaxyReset();

            // GalaxyService should be called twice (Milky Way + The Void) even with empty empire map
            verify(galaxyService, times(2)).generateAndSaveGalaxyWithEmpires(
                    anyString(), anyMap(), anyInt(), anyInt(), anyDouble(), anyDouble(),
                    anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                    anyDouble(), anyLong()
            );
            // Entity repopulated should mention 0 empires for both galaxies
            assertTrue(result.getEntitiesRepopulated().stream()
                    .anyMatch(e -> e.contains("0")));
        }
    }
}
