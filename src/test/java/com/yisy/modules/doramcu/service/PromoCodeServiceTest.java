package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.model.PromoCode;
import com.yisy.modules.doramcu.repository.PromoCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PromoCodeService Tests")
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class PromoCodeServiceTest {

    @Mock
    private PromoCodeRepository promoCodeRepository;

    private PromoCodeService promoCodeService;

    @BeforeEach
    void setUp() {
        promoCodeService = new PromoCodeService(promoCodeRepository);
    }

    private PromoCode createValidPromoCode(String code) {
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

    // ==================== ValidationResult ====================

    @Nested
    @DisplayName("ValidationResult")
    class ValidationResultTests {

        @Test
        @DisplayName("valid() factory should create a valid result with the promo code")
        void validFactory_shouldCreateValidResult() {
            PromoCode pc = createValidPromoCode("WELCOME2026");
            PromoCodeService.ValidationResult result = PromoCodeService.ValidationResult.valid(pc);

            assertTrue(result.isValid());
            assertNull(result.getMessage());
            assertEquals(pc, result.getPromoCode());
        }

        @Test
        @DisplayName("invalid() factory should create an invalid result with a message")
        void invalidFactory_shouldCreateInvalidResult() {
            PromoCodeService.ValidationResult result = PromoCodeService.ValidationResult.invalid("Invalid promo code");

            assertFalse(result.isValid());
            assertEquals("Invalid promo code", result.getMessage());
            assertNull(result.getPromoCode());
        }
    }

    // ==================== validate() ====================

    @Nested
    @DisplayName("validate() - Promo Code Validation")
    class ValidateTests {

        @Test
        @DisplayName("should return invalid when code is null")
        void validate_nullCode_shouldReturnInvalid() {
            PromoCodeService.ValidationResult result = promoCodeService.validate(null);

            assertFalse(result.isValid());
            assertEquals("Promo code is required", result.getMessage());
            verify(promoCodeRepository, never()).findByCode(anyString());
        }

        @Test
        @DisplayName("should return invalid when code is empty")
        void validate_emptyCode_shouldReturnInvalid() {
            PromoCodeService.ValidationResult result = promoCodeService.validate("");

            assertFalse(result.isValid());
            assertEquals("Promo code is required", result.getMessage());
        }

        @Test
        @DisplayName("should return invalid when code is whitespace only")
        void validate_whitespaceCode_shouldReturnInvalid() {
            PromoCodeService.ValidationResult result = promoCodeService.validate("   ");

            assertFalse(result.isValid());
            assertEquals("Promo code is required", result.getMessage());
        }

        @Test
        @DisplayName("should return invalid when code does not exist in database")
        void validate_nonExistentCode_shouldReturnInvalid() {
            when(promoCodeRepository.findByCode("NONEXISTENT")).thenReturn(Optional.empty());

            PromoCodeService.ValidationResult result = promoCodeService.validate("NONEXISTENT");

            assertFalse(result.isValid());
            assertEquals("Invalid promo code", result.getMessage());
            verify(promoCodeRepository).findByCode("NONEXISTENT");
        }

        @Test
        @DisplayName("should normalize code to uppercase before lookup")
        void validate_lowercaseCode_shouldNormalizeToUppercase() {
            when(promoCodeRepository.findByCode("WELCOME2026")).thenReturn(Optional.empty());

            promoCodeService.validate("welcome2026");

            verify(promoCodeRepository).findByCode("WELCOME2026");
        }

        @Test
        @DisplayName("should trim whitespace from code before lookup")
        void validate_codeWithWhitespace_shouldTrim() {
            when(promoCodeRepository.findByCode("WELCOME2026")).thenReturn(Optional.empty());

            promoCodeService.validate("  welcome2026  ");

            verify(promoCodeRepository).findByCode("WELCOME2026");
        }

        @Test
        @DisplayName("should return invalid when code is inactive")
        void validate_inactiveCode_shouldReturnInvalid() {
            PromoCode pc = createValidPromoCode("INACTIVE");
            pc.setActive(false);
            when(promoCodeRepository.findByCode("INACTIVE")).thenReturn(Optional.of(pc));

            PromoCodeService.ValidationResult result = promoCodeService.validate("INACTIVE");

            assertFalse(result.isValid());
            assertEquals("Promo code is no longer active", result.getMessage());
        }

        @Test
        @DisplayName("should return invalid when code has expired")
        void validate_expiredCode_shouldReturnInvalid() {
            PromoCode pc = createValidPromoCode("EXPIRED");
            pc.setExpiryDate(LocalDateTime.now().minusDays(1));
            when(promoCodeRepository.findByCode("EXPIRED")).thenReturn(Optional.of(pc));

            PromoCodeService.ValidationResult result = promoCodeService.validate("EXPIRED");

            assertFalse(result.isValid());
            assertEquals("Promo code has expired", result.getMessage());
        }

        @Test
        @DisplayName("should return invalid when usage limit is reached")
        void validate_usageLimitReached_shouldReturnInvalid() {
            PromoCode pc = createValidPromoCode("EXHAUSTED");
            pc.setMaxUses(10);
            pc.setUsedCount(10);
            when(promoCodeRepository.findByCode("EXHAUSTED")).thenReturn(Optional.of(pc));

            PromoCodeService.ValidationResult result = promoCodeService.validate("EXHAUSTED");

            assertFalse(result.isValid());
            assertEquals("Promo code usage limit has been reached", result.getMessage());
        }

        @Test
        @DisplayName("should return invalid when usedCount exceeds maxUses")
        void validate_usedCountExceedsMax_shouldReturnInvalid() {
            PromoCode pc = createValidPromoCode("OVERUSED");
            pc.setMaxUses(10);
            pc.setUsedCount(15);
            when(promoCodeRepository.findByCode("OVERUSED")).thenReturn(Optional.of(pc));

            PromoCodeService.ValidationResult result = promoCodeService.validate("OVERUSED");

            assertFalse(result.isValid());
            assertEquals("Promo code usage limit has been reached", result.getMessage());
        }

        @Test
        @DisplayName("should return valid when code passes all checks")
        void validate_validCode_shouldReturnValid() {
            PromoCode pc = createValidPromoCode("WELCOME2026");
            when(promoCodeRepository.findByCode("WELCOME2026")).thenReturn(Optional.of(pc));

            PromoCodeService.ValidationResult result = promoCodeService.validate("WELCOME2026");

            assertTrue(result.isValid());
            assertNull(result.getMessage());
            assertEquals(pc, result.getPromoCode());
        }

        @Test
        @DisplayName("should return valid for unlimited use code (maxUses null)")
        void validate_unlimitedCode_shouldReturnValid() {
            PromoCode pc = createValidPromoCode("UNLIMITED");
            pc.setMaxUses(null);
            pc.setUsedCount(9999);
            when(promoCodeRepository.findByCode("UNLIMITED")).thenReturn(Optional.of(pc));

            PromoCodeService.ValidationResult result = promoCodeService.validate("UNLIMITED");

            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("should return valid for code without expiry date")
        void validate_noExpiryCode_shouldReturnValid() {
            PromoCode pc = createValidPromoCode("NOEXPIRY");
            pc.setExpiryDate(null);
            when(promoCodeRepository.findByCode("NOEXPIRY")).thenReturn(Optional.of(pc));

            PromoCodeService.ValidationResult result = promoCodeService.validate("NOEXPIRY");

            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("should check active status before expiry")
        void validate_activeCheckBeforeExpiry() {
            PromoCode pc = createValidPromoCode("INACTIVE_EXPIRED");
            pc.setActive(false);
            pc.setExpiryDate(LocalDateTime.now().minusDays(1));
            when(promoCodeRepository.findByCode("INACTIVE_EXPIRED")).thenReturn(Optional.of(pc));

            PromoCodeService.ValidationResult result = promoCodeService.validate("INACTIVE_EXPIRED");

            assertFalse(result.isValid());
            assertEquals("Promo code is no longer active", result.getMessage());
        }

        @Test
        @DisplayName("should check expiry before usage limit")
        void validate_expiryCheckBeforeUsageLimit() {
            PromoCode pc = createValidPromoCode("EXPIRED_EXHAUSTED");
            pc.setExpiryDate(LocalDateTime.now().minusDays(1));
            pc.setMaxUses(10);
            pc.setUsedCount(10);
            when(promoCodeRepository.findByCode("EXPIRED_EXHAUSTED")).thenReturn(Optional.of(pc));

            PromoCodeService.ValidationResult result = promoCodeService.validate("EXPIRED_EXHAUSTED");

            assertFalse(result.isValid());
            assertEquals("Promo code has expired", result.getMessage());
        }
    }

    // ==================== incrementUsageCount() ====================

    @Nested
    @DisplayName("incrementUsageCount() - Usage Count Increment")
    class IncrementUsageCountTests {

        @Test
        @DisplayName("should return false when code is null")
        void increment_nullCode_shouldReturnFalse() {
            boolean result = promoCodeService.incrementUsageCount(null);

            assertFalse(result);
            verify(promoCodeRepository, never()).findByCode(anyString());
        }

        @Test
        @DisplayName("should return false when code is empty")
        void increment_emptyCode_shouldReturnFalse() {
            boolean result = promoCodeService.incrementUsageCount("");

            assertFalse(result);
        }

        @Test
        @DisplayName("should return false when code is not found")
        void increment_notFound_shouldReturnFalse() {
            when(promoCodeRepository.findByCode("NONEXISTENT")).thenReturn(Optional.empty());

            boolean result = promoCodeService.incrementUsageCount("NONEXISTENT");

            assertFalse(result);
            verify(promoCodeRepository, never()).save(any());
        }

        @Test
        @DisplayName("should increment usage count and save when code is found")
        void increment_found_shouldIncrementAndSave() {
            PromoCode pc = createValidPromoCode("WELCOME2026");
            pc.setUsedCount(5);
            when(promoCodeRepository.findByCode("WELCOME2026")).thenReturn(Optional.of(pc));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(pc);

            boolean result = promoCodeService.incrementUsageCount("WELCOME2026");

            assertTrue(result);
            assertEquals(6, pc.getUsedCount());
            verify(promoCodeRepository).save(pc);
        }

        @Test
        @DisplayName("should normalize code to uppercase")
        void increment_lowercaseCode_shouldNormalize() {
            PromoCode pc = createValidPromoCode("WELCOME2026");
            when(promoCodeRepository.findByCode("WELCOME2026")).thenReturn(Optional.of(pc));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(pc);

            promoCodeService.incrementUsageCount("welcome2026");

            verify(promoCodeRepository).findByCode("WELCOME2026");
        }
    }

    // ==================== createPromoCode() ====================

    @Nested
    @DisplayName("createPromoCode() - Create Promo Code")
    class CreatePromoCodeTests {

        @Test
        @DisplayName("should throw IllegalArgumentException when code is null")
        void create_nullCode_shouldThrow() {
            assertThrows(IllegalArgumentException.class, () ->
                    promoCodeService.createPromoCode(null, "Description", 100)
            );
            verify(promoCodeRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when code is empty")
        void create_emptyCode_shouldThrow() {
            assertThrows(IllegalArgumentException.class, () ->
                    promoCodeService.createPromoCode("", "Description", 100)
            );
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when code already exists")
        void create_duplicateCode_shouldThrow() {
            when(promoCodeRepository.existsByCode("EXISTING")).thenReturn(true);

            assertThrows(IllegalArgumentException.class, () ->
                    promoCodeService.createPromoCode("EXISTING", "Description", 100)
            );
            verify(promoCodeRepository, never()).save(any());
        }

        @Test
        @DisplayName("should create new promo code with default values")
        void create_newCode_shouldCreateWithDefaults() {
            when(promoCodeRepository.existsByCode("NEWCODE")).thenReturn(false);
            when(promoCodeRepository.save(any(PromoCode.class))).thenAnswer(invocation -> {
                PromoCode pc = invocation.getArgument(0);
                pc.setId(1L);
                return pc;
            });

            PromoCode result = promoCodeService.createPromoCode("NEWCODE", "New Code", 100);

            assertNotNull(result);
            assertEquals("NEWCODE", result.getCode());
            assertEquals("New Code", result.getDescription());
            assertEquals(100, result.getMaxUses());
            assertTrue(result.isActive());
            assertEquals(0, result.getUsedCount());
            verify(promoCodeRepository).save(any(PromoCode.class));
        }

        @Test
        @DisplayName("should normalize code to uppercase")
        void create_lowercaseCode_shouldNormalize() {
            when(promoCodeRepository.existsByCode("NEWCODE")).thenReturn(false);
            when(promoCodeRepository.save(any(PromoCode.class))).thenAnswer(invocation -> {
                PromoCode pc = invocation.getArgument(0);
                pc.setId(1L);
                return pc;
            });

            PromoCode result = promoCodeService.createPromoCode("newcode", "New Code", 100);

            assertEquals("NEWCODE", result.getCode());
        }

        @Test
        @DisplayName("should create promo code with null description")
        void create_nullDescription_shouldCreate() {
            when(promoCodeRepository.existsByCode("NEWCODE")).thenReturn(false);
            when(promoCodeRepository.save(any(PromoCode.class))).thenAnswer(invocation -> {
                PromoCode pc = invocation.getArgument(0);
                pc.setId(1L);
                return pc;
            });

            PromoCode result = promoCodeService.createPromoCode("NEWCODE", null, 100);

            assertNotNull(result);
            assertNull(result.getDescription());
        }

        @Test
        @DisplayName("should create promo code with null maxUses (unlimited)")
        void create_nullMaxUses_shouldCreateUnlimited() {
            when(promoCodeRepository.existsByCode("UNLIMITED")).thenReturn(false);
            when(promoCodeRepository.save(any(PromoCode.class))).thenAnswer(invocation -> {
                PromoCode pc = invocation.getArgument(0);
                pc.setId(1L);
                return pc;
            });

            PromoCode result = promoCodeService.createPromoCode("UNLIMITED", "Unlimited", null);

            assertNotNull(result);
            assertNull(result.getMaxUses());
        }
    }

    // ==================== findByCode() ====================

    @Nested
    @DisplayName("findByCode() - Find by Code")
    class FindByCodeTests {

        @Test
        @DisplayName("should return empty when code is null")
        void findByCode_null_shouldReturnEmpty() {
            Optional<PromoCode> result = promoCodeService.findByCode(null);

            assertTrue(result.isEmpty());
            verify(promoCodeRepository, never()).findByCode(anyString());
        }

        @Test
        @DisplayName("should return empty when code is empty")
        void findByCode_empty_shouldReturnEmpty() {
            Optional<PromoCode> result = promoCodeService.findByCode("");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return promo code when found")
        void findByCode_found_shouldReturnPromoCode() {
            PromoCode pc = createValidPromoCode("WELCOME2026");
            when(promoCodeRepository.findByCode("WELCOME2026")).thenReturn(Optional.of(pc));

            Optional<PromoCode> result = promoCodeService.findByCode("WELCOME2026");

            assertTrue(result.isPresent());
            assertEquals("WELCOME2026", result.get().getCode());
        }

        @Test
        @DisplayName("should return empty when not found")
        void findByCode_notFound_shouldReturnEmpty() {
            when(promoCodeRepository.findByCode("NONEXISTENT")).thenReturn(Optional.empty());

            Optional<PromoCode> result = promoCodeService.findByCode("NONEXISTENT");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should normalize code to uppercase")
        void findByCode_lowercase_shouldNormalize() {
            when(promoCodeRepository.findByCode("WELCOME2026")).thenReturn(Optional.empty());

            promoCodeService.findByCode("welcome2026");

            verify(promoCodeRepository).findByCode("WELCOME2026");
        }
    }

    // ==================== deactivatePromoCode() ====================

    @Nested
    @DisplayName("deactivatePromoCode() - Deactivate Promo Code")
    class DeactivatePromoCodeTests {

        @Test
        @DisplayName("should deactivate promo code when found")
        void deactivate_found_shouldDeactivate() {
            PromoCode pc = createValidPromoCode("WELCOME2026");
            when(promoCodeRepository.findByCode("WELCOME2026")).thenReturn(Optional.of(pc));
            when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(pc);

            PromoCode result = promoCodeService.deactivatePromoCode("WELCOME2026");

            assertNotNull(result);
            assertFalse(result.isActive());
            assertFalse(pc.isActive());
            verify(promoCodeRepository).save(pc);
        }

        @Test
        @DisplayName("should return false when promo code not found")
        void deactivate_notFound_shouldReturnFalse() {
            when(promoCodeRepository.findByCode("NONEXISTENT")).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                    promoCodeService.deactivatePromoCode("NONEXISTENT"));
            verify(promoCodeRepository, never()).save(any());
        }
    }

    // ==================== deletePromoCode() ====================

    @Nested
    @DisplayName("deletePromoCode() - Delete Promo Code")
    class DeletePromoCodeTests {

        @Test
        @DisplayName("should delete promo code when found")
        void delete_found_shouldDelete() {
            PromoCode pc = createValidPromoCode("WELCOME2026");
            when(promoCodeRepository.findByCode("WELCOME2026")).thenReturn(Optional.of(pc));

            boolean result = promoCodeService.deletePromoCode("WELCOME2026");

            assertTrue(result);
            verify(promoCodeRepository).delete(pc);
        }

        @Test
        @DisplayName("should return false when promo code not found")
        void delete_notFound_shouldReturnFalse() {
            when(promoCodeRepository.findByCode("NONEXISTENT")).thenReturn(Optional.empty());

            boolean result = promoCodeService.deletePromoCode("NONEXISTENT");

            assertFalse(result);
            verify(promoCodeRepository, never()).delete(any());
        }
    }

    // ==================== findAll() ====================

    @Nested
    @DisplayName("findAll() - List All Promo Codes")
    class FindAllTests {

        @Test
        @DisplayName("should return empty list when no promo codes exist")
        void findAll_empty_shouldReturnEmptyList() {
            when(promoCodeRepository.findAll()).thenReturn(new ArrayList<>());

            List<PromoCode> result = promoCodeService.findAll();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return all promo codes")
        void findAll_withCodes_shouldReturnAll() {
            PromoCode pc1 = createValidPromoCode("WELCOME2026");
            PromoCode pc2 = createValidPromoCode("EARLYBIRD");
            pc2.setId(2L);
            when(promoCodeRepository.findAll()).thenReturn(List.of(pc1, pc2));

            List<PromoCode> result = promoCodeService.findAll();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("WELCOME2026", result.get(0).getCode());
            assertEquals("EARLYBIRD", result.get(1).getCode());
        }
    }

    // ==================== PromoCode Entity Helper Methods ====================

    @Nested
    @DisplayName("PromoCode Entity - Helper Methods")
    class PromoCodeEntityTests {

        @Test
        @DisplayName("isUsageLimitReached - should return false when maxUses is null (unlimited)")
        void isUsageLimitReached_unlimited_shouldReturnFalse() {
            PromoCode pc = new PromoCode();
            pc.setMaxUses(null);
            pc.setUsedCount(9999);

            assertFalse(pc.isUsageLimitReached());
        }

        @Test
        @DisplayName("isUsageLimitReached - should return false when usedCount < maxUses")
        void isUsageLimitReached_underLimit_shouldReturnFalse() {
            PromoCode pc = new PromoCode();
            pc.setMaxUses(100);
            pc.setUsedCount(50);

            assertFalse(pc.isUsageLimitReached());
        }

        @Test
        @DisplayName("isUsageLimitReached - should return true when usedCount == maxUses")
        void isUsageLimitReached_atLimit_shouldReturnTrue() {
            PromoCode pc = new PromoCode();
            pc.setMaxUses(100);
            pc.setUsedCount(100);

            assertTrue(pc.isUsageLimitReached());
        }

        @Test
        @DisplayName("isUsageLimitReached - should return true when usedCount > maxUses")
        void isUsageLimitReached_overLimit_shouldReturnTrue() {
            PromoCode pc = new PromoCode();
            pc.setMaxUses(100);
            pc.setUsedCount(150);

            assertTrue(pc.isUsageLimitReached());
        }

        @Test
        @DisplayName("isUsageLimitReached - should return false when usedCount is 0 and maxUses > 0")
        void isUsageLimitReached_zeroUsed_shouldReturnFalse() {
            PromoCode pc = new PromoCode();
            pc.setMaxUses(100);
            pc.setUsedCount(0);

            assertFalse(pc.isUsageLimitReached());
        }

        @Test
        @DisplayName("isExpired - should return false when expiryDate is null (no expiry)")
        void isExpired_noExpiry_shouldReturnFalse() {
            PromoCode pc = new PromoCode();
            pc.setExpiryDate(null);

            assertFalse(pc.isExpired());
        }

        @Test
        @DisplayName("isExpired - should return false when expiryDate is in the future")
        void isExpired_future_shouldReturnFalse() {
            PromoCode pc = new PromoCode();
            pc.setExpiryDate(LocalDateTime.now().plusDays(30));

            assertFalse(pc.isExpired());
        }

        @Test
        @DisplayName("isExpired - should return true when expiryDate is in the past")
        void isExpired_past_shouldReturnTrue() {
            PromoCode pc = new PromoCode();
            pc.setExpiryDate(LocalDateTime.now().minusDays(1));

            assertTrue(pc.isExpired());
        }

        @Test
        @DisplayName("isExpired - should return false when expiryDate equals now (not after)")
        void isExpired_now_shouldReturnFalse() {
            PromoCode pc = new PromoCode();
            pc.setExpiryDate(LocalDateTime.now());

            assertFalse(pc.isExpired());
        }
    }
}
