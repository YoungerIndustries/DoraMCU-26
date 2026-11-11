package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.hex.GalaxyFactory;
import com.yisy.modules.doramcu.model.PromoCode;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.repository.SectorRepository;
import com.yisy.modules.doramcu.repository.SubSectorRepository;
import com.yisy.modules.doramcu.repository.UserRepository;
import com.yisy.modules.doramcu.service.GalaxyProgressService;
import com.yisy.modules.doramcu.service.GalaxyService;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.service.PromoCodeService;
import com.yisy.modules.doramcu.service.ResetService;
import com.yisy.modules.doramcu.service.TurnScheduler;
import com.yisy.modules.doradl.dl.service.DoraAiLearningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminController Promo Code Tests")
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class AdminControllerPromoCodeTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private GalaxyRepository galaxyRepository;
    @Mock
    private SectorRepository sectorRepository;
    @Mock
    private SubSectorRepository subSectorRepository;
    @Mock
    private GalaxyService galaxyService;

    @Mock
    private GalaxyProgressService galaxyProgressService;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TurnScheduler turnScheduler;

    @Mock
    private GameSessionService gameSessionService;

    @Mock
    private DoraAiLearningService doraAiLearningService;

    @Mock
    private PromoCodeService promoCodeService;

    @Mock
    private ResetService resetService;

    private AdminController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminController(
                userRepository, passwordEncoder, galaxyRepository, sectorRepository, subSectorRepository,
                galaxyService, galaxyProgressService, playerRepository, turnScheduler,
                gameSessionService, doraAiLearningService, promoCodeService
        );
    }

    private PromoCode createTestPromoCode(String code) {
        PromoCode pc = new PromoCode();
        pc.setId(1L);
        pc.setCode(code.toUpperCase());
        pc.setDescription("Test promo code");
        pc.setCodeType(PromoCode.CodeType.PLAYER);
        pc.setMaxUses(100);
        pc.setUsedCount(5);
        pc.setActive(true);
        pc.setExpiryDate(LocalDateTime.now().plusDays(30));
        pc.setBonuses("{\"credits\": 1000}");
        return pc;
    }

    // ==================== GET /promo-codes (List All) ====================

    @Nested
    @DisplayName("GET /promo-codes - List All Promo Codes")
    class GetAllPromoCodes {

        @Test
        @DisplayName("should return empty list when no promo codes exist")
        void shouldReturnEmptyList() {
            when(promoCodeService.findAll()).thenReturn(new ArrayList<>());

            ResponseEntity<?> response = controller.getAllPromoCodes();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals(0, body.get("count"));
            assertTrue(((List<?>) body.get("promoCodes")).isEmpty());
        }

        @Test
        @DisplayName("should return all promo codes with correct fields")
        void shouldReturnAllPromoCodes() {
            PromoCode pc = createTestPromoCode("WELCOME2026");
            when(promoCodeService.findAll()).thenReturn(List.of(pc));

            ResponseEntity<?> response = controller.getAllPromoCodes();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals(1, body.get("count"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> promoCodes = (List<Map<String, Object>>) body.get("promoCodes");
            assertEquals(1, promoCodes.size());

            Map<String, Object> item = promoCodes.get(0);
            assertEquals(1L, item.get("id"));
            assertEquals("WELCOME2026", item.get("code"));
            assertEquals("Test promo code", item.get("description"));
            assertEquals(100, item.get("maxUses"));
            assertEquals(5, item.get("usedCount"));
            assertEquals(true, item.get("active"));
            assertEquals("{\"credits\": 1000}", item.get("bonuses"));
        }

        @Test
        @DisplayName("should return multiple promo codes")
        void shouldReturnMultiplePromoCodes() {
            PromoCode pc1 = createTestPromoCode("WELCOME2026");
            pc1.setId(1L);
            PromoCode pc2 = createTestPromoCode("EARLYBIRD");
            pc2.setId(2L);
            pc2.setUsedCount(10);
            when(promoCodeService.findAll()).thenReturn(List.of(pc1, pc2));

            ResponseEntity<?> response = controller.getAllPromoCodes();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals(2, body.get("count"));
        }
    }

    // ==================== GET /promo-codes/{code} (Get Single) ====================

    @Nested
    @DisplayName("GET /promo-codes/{code} - Get Single Promo Code")
    class GetPromoCode {

        @Test
        @DisplayName("should return promo code when found")
        void shouldReturnPromoCode() {
            PromoCode pc = createTestPromoCode("WELCOME2026");
            when(promoCodeService.findByCode("WELCOME2026")).thenReturn(Optional.of(pc));

            ResponseEntity<?> response = controller.getPromoCode("WELCOME2026");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals(1L, body.get("id"));
            assertEquals("WELCOME2026", body.get("code"));
            assertEquals("Test promo code", body.get("description"));
            assertEquals(100, body.get("maxUses"));
            assertEquals(5, body.get("usedCount"));
            assertEquals(true, body.get("active"));
        }

        @Test
        @DisplayName("should return 404 when promo code not found")
        void shouldReturnNotFound() {
            when(promoCodeService.findByCode("NONEXISTENT")).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getPromoCode("NONEXISTENT");

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("should handle case-insensitive lookup")
        void shouldHandleCaseInsensitiveLookup() {
            PromoCode pc = createTestPromoCode("WELCOME2026");
            when(promoCodeService.findByCode("welcome2026")).thenReturn(Optional.of(pc));

            ResponseEntity<?> response = controller.getPromoCode("welcome2026");

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    // ==================== POST /promo-codes (Create) ====================

    @Nested
    @DisplayName("POST /promo-codes - Create Promo Code")
    class CreatePromoCode {

        @Test
        @DisplayName("should create promo code successfully")
        void shouldCreatePromoCode() {
            PromoCode pc = createTestPromoCode("NEWCODE");
            when(promoCodeService.createPromoCode(eq("NEWCODE"), eq("New test code"), eq(50), any(PromoCode.CodeType.class))).thenReturn(pc);

            Map<String, Object> request = new HashMap<>();
            request.put("code", "NEWCODE");
            request.put("description", "New test code");
            request.put("maxUses", 50);

            ResponseEntity<?> response = controller.createPromoCode(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("Promo code created successfully", body.get("message"));
            assertEquals("NEWCODE", body.get("code"));
            assertEquals(1L, body.get("id"));
        }

        @Test
        @DisplayName("should return 400 when code is missing")
        void shouldReturnBadRequestWhenCodeMissing() {
            Map<String, Object> request = new HashMap<>();
            request.put("description", "No code provided");

            ResponseEntity<?> response = controller.createPromoCode(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("code is required", body.get("error"));
        }

        @Test
        @DisplayName("should return 400 when code is blank")
        void shouldReturnBadRequestWhenCodeBlank() {
            Map<String, Object> request = new HashMap<>();
            request.put("code", "   ");

            ResponseEntity<?> response = controller.createPromoCode(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 400 when code already exists")
        void shouldReturnBadRequestWhenCodeExists() {
            when(promoCodeService.createPromoCode(eq("DUPLICATE"), eq("Dup"), isNull(), any(PromoCode.CodeType.class)))
                    .thenThrow(new IllegalArgumentException("Promo code already exists: DUPLICATE"));

            Map<String, Object> request = new HashMap<>();
            request.put("code", "DUPLICATE");
            request.put("description", "Dup");

            ResponseEntity<?> response = controller.createPromoCode(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertTrue(((String) body.get("error")).contains("already exists"));
        }

        @Test
        @DisplayName("should create promo code without optional fields")
        void shouldCreateWithoutOptionalFields() {
            PromoCode pc = createTestPromoCode("SIMPLE");
            pc.setDescription(null);
            pc.setMaxUses(null);
            when(promoCodeService.createPromoCode(eq("SIMPLE"), isNull(), isNull(), any(PromoCode.CodeType.class))).thenReturn(pc);

            Map<String, Object> request = new HashMap<>();
            request.put("code", "SIMPLE");

            ResponseEntity<?> response = controller.createPromoCode(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }
    }

    // ==================== PUT /promo-codes/{code} (Update) ====================

    @Nested
    @DisplayName("PUT /promo-codes/{code} - Update Promo Code")
    class UpdatePromoCode {

        @Test
        @DisplayName("should update description successfully")
        void shouldUpdateDescription() {
            PromoCode pc = createTestPromoCode("WELCOME2026");
            pc.setDescription("Updated description");
            when(promoCodeService.updatePromoCode("WELCOME2026", "Updated description", null, null, null))
                    .thenReturn(pc);

            Map<String, Object> request = new HashMap<>();
            request.put("description", "Updated description");

            ResponseEntity<?> response = controller.updatePromoCode("WELCOME2026", request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("Promo code updated successfully", body.get("message"));
            assertEquals("Updated description", body.get("description"));
        }

        @Test
        @DisplayName("should update maxUses successfully")
        void shouldUpdateMaxUses() {
            PromoCode pc = createTestPromoCode("WELCOME2026");
            pc.setMaxUses(200);
            when(promoCodeService.updatePromoCode("WELCOME2026", null, 200, null, null))
                    .thenReturn(pc);

            Map<String, Object> request = new HashMap<>();
            request.put("maxUses", 200);

            ResponseEntity<?> response = controller.updatePromoCode("WELCOME2026", request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals(200, body.get("maxUses"));
        }

        @Test
        @DisplayName("should update bonuses successfully")
        void shouldUpdateBonuses() {
            PromoCode pc = createTestPromoCode("WELCOME2026");
            pc.setBonuses("{\"credits\": 5000, \"items\": [\"sword\"]}");
            when(promoCodeService.updatePromoCode(eq("WELCOME2026"), isNull(), isNull(), isNull(),
                    eq("{\"credits\": 5000, \"items\": [\"sword\"]}")))
                    .thenReturn(pc);

            Map<String, Object> request = new HashMap<>();
            request.put("bonuses", "{\"credits\": 5000, \"items\": [\"sword\"]}");

            ResponseEntity<?> response = controller.updatePromoCode("WELCOME2026", request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("{\"credits\": 5000, \"items\": [\"sword\"]}", body.get("bonuses"));
        }

        @Test
        @DisplayName("should update expiryDate successfully")
        void shouldUpdateExpiryDate() {
            LocalDateTime newExpiry = LocalDateTime.of(2027, 12, 31, 23, 59, 59);
            PromoCode pc = createTestPromoCode("WELCOME2026");
            pc.setExpiryDate(newExpiry);
            when(promoCodeService.updatePromoCode(eq("WELCOME2026"), isNull(), isNull(), eq(newExpiry), isNull()))
                    .thenReturn(pc);

            Map<String, Object> request = new HashMap<>();
            request.put("expiryDate", "2027-12-31T23:59:59");

            ResponseEntity<?> response = controller.updatePromoCode("WELCOME2026", request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 400 when updating non-existent code")
        void shouldReturnBadRequestWhenNotFound() {
            when(promoCodeService.updatePromoCode(eq("NONEXISTENT"), any(), any(), any(), any()))
                    .thenThrow(new IllegalArgumentException("Promo code not found: NONEXISTENT"));

            Map<String, Object> request = new HashMap<>();
            request.put("description", "Updated");

            ResponseEntity<?> response = controller.updatePromoCode("NONEXISTENT", request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertTrue(((String) body.get("error")).contains("not found"));
        }

        @Test
        @DisplayName("should update multiple fields at once")
        void shouldUpdateMultipleFields() {
            LocalDateTime newExpiry = LocalDateTime.of(2027, 6, 30, 0, 0);
            PromoCode pc = createTestPromoCode("WELCOME2026");
            pc.setDescription("Multi update");
            pc.setMaxUses(500);
            pc.setBonuses("{\"credits\": 9999}");
            pc.setExpiryDate(newExpiry);
            when(promoCodeService.updatePromoCode(eq("WELCOME2026"), eq("Multi update"), eq(500),
                    eq(newExpiry), eq("{\"credits\": 9999}")))
                    .thenReturn(pc);

            Map<String, Object> request = new HashMap<>();
            request.put("description", "Multi update");
            request.put("maxUses", 500);
            request.put("bonuses", "{\"credits\": 9999}");
            request.put("expiryDate", "2027-06-30T00:00");

            ResponseEntity<?> response = controller.updatePromoCode("WELCOME2026", request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("Multi update", body.get("description"));
            assertEquals(500, body.get("maxUses"));
        }
    }

    // ==================== DELETE /promo-codes/{code} ====================

    @Nested
    @DisplayName("DELETE /promo-codes/{code} - Delete Promo Code")
    class DeletePromoCode {

        @Test
        @DisplayName("should delete promo code successfully")
        void shouldDeletePromoCode() {
            when(promoCodeService.deletePromoCode("WELCOME2026")).thenReturn(true);

            ResponseEntity<?> response = controller.deletePromoCode("WELCOME2026");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("Promo code deleted successfully", body.get("message"));
        }

        @Test
        @DisplayName("should return 404 when deleting non-existent code")
        void shouldReturnNotFound() {
            when(promoCodeService.deletePromoCode("NONEXISTENT")).thenReturn(false);

            ResponseEntity<?> response = controller.deletePromoCode("NONEXISTENT");

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    // ==================== POST /promo-codes/{code}/deactivate ====================

    @Nested
    @DisplayName("POST /promo-codes/{code}/deactivate - Deactivate Promo Code")
    class DeactivatePromoCode {

        @Test
        @DisplayName("should deactivate promo code successfully")
        void shouldDeactivatePromoCode() {
            PromoCode pc = createTestPromoCode("WELCOME2026");
            when(promoCodeService.deactivatePromoCode("WELCOME2026")).thenReturn(pc);

            ResponseEntity<?> response = controller.deactivatePromoCode("WELCOME2026");

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 404 when deactivating non-existent code")
        void shouldReturnNotFound() {
            when(promoCodeService.deactivatePromoCode("NONEXISTENT"))
                    .thenThrow(new IllegalArgumentException("Promo code not found: NONEXISTENT"));

            ResponseEntity<?> response = controller.deactivatePromoCode("NONEXISTENT");

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    // ==================== POST /promo-codes/{code}/activate ====================

    @Nested
    @DisplayName("POST /promo-codes/{code}/activate - Activate Promo Code")
    class ActivatePromoCode {

        @Test
        @DisplayName("should activate promo code successfully")
        void shouldActivatePromoCode() {
            PromoCode pc = createTestPromoCode("WELCOME2026");
            pc.setActive(false);
            when(promoCodeService.findByCode("WELCOME2026")).thenReturn(Optional.of(pc));
            when(promoCodeService.updatePromoCode(eq("WELCOME2026"), isNull(), isNull(), isNull(), isNull()))
                    .thenReturn(pc);

            ResponseEntity<?> response = controller.activatePromoCode("WELCOME2026");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("Promo code activated successfully", body.get("message"));
        }

        @Test
        @DisplayName("should return 404 when activating non-existent code")
        void shouldReturnNotFound() {
            when(promoCodeService.findByCode("NONEXISTENT")).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.activatePromoCode("NONEXISTENT");

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }
}
