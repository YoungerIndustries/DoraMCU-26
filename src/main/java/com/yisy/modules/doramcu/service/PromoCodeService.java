package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.model.PromoCode;
import com.yisy.modules.doramcu.repository.PromoCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for validating and managing promo codes.
 * <p>
 * Handles promo code validation during registration, including checking
 * if codes exist, are active, haven't expired, and haven't exceeded usage limits.
 * </p>
 */
@Service
public class PromoCodeService {

    private static final Logger log = LoggerFactory.getLogger(PromoCodeService.class);

    private final PromoCodeRepository promoCodeRepository;

    public PromoCodeService(PromoCodeRepository promoCodeRepository) {
        this.promoCodeRepository = promoCodeRepository;
    }

    /**
     * Result of validating a promo code.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final PromoCode promoCode;

        public ValidationResult(boolean valid, String message, PromoCode promoCode) {
            this.valid = valid;
            this.message = message;
            this.promoCode = promoCode;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public PromoCode getPromoCode() {
            return promoCode;
        }

        public static ValidationResult valid(PromoCode promoCode) {
            return new ValidationResult(true, null, promoCode);
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message, null);
        }
    }

    /**
     * Validates a promo code for use during registration (PLAYER type only).
     * <p>
     * Checks that the code exists, is of type PLAYER, is active, has not expired,
     * and has not reached its usage limit.
     * </p>
     *
     * @param code the promo code string to validate
     * @return validation result indicating whether the code is valid
     */
    public ValidationResult validate(String code) {
        return validate(code, PromoCode.CodeType.PLAYER);
    }

    /**
     * Validates a promo code of a specific type.
     * <p>
     * Checks that the code exists, matches the expected type, is active, has not expired,
     * and has not reached its usage limit.
     * </p>
     *
     * @param code the promo code string to validate
     * @param expectedType the expected code type (PLAYER or ADMIN)
     * @return validation result indicating whether the code is valid
     */
    public ValidationResult validate(String code, PromoCode.CodeType expectedType) {
        if (code == null || code.trim().isEmpty()) {
            return ValidationResult.invalid("Promo code is required");
        }

        // Normalize to uppercase for case-insensitive matching
        String normalizedCode = code.trim().toUpperCase();

        // Find the promo code in the database
        Optional<PromoCode> optional = promoCodeRepository.findByCode(normalizedCode);
        if (optional.isEmpty()) {
            log.warn("Invalid promo code attempted: {}", code);
            return ValidationResult.invalid("Invalid promo code");
        }

        PromoCode promoCode = optional.get();

        // Check if the code type matches the expected type
        if (!promoCode.getCodeType().equals(expectedType)) {
            log.warn("Promo code type mismatch: {} expected {}, got {}", code, expectedType, promoCode.getCodeType());
            return ValidationResult.invalid("Invalid promo code type");
        }

        // Check if the code is active
        if (!promoCode.isActive()) {
            log.warn("Inactive promo code attempted: {}", code);
            return ValidationResult.invalid("Promo code is no longer active");
        }

        // Check if the code has expired
        if (promoCode.isExpired()) {
            log.warn("Expired promo code attempted: {}", code);
            return ValidationResult.invalid("Promo code has expired");
        }

        // Check if the usage limit has been reached
        if (promoCode.isUsageLimitReached()) {
            log.warn("Usage limit reached for promo code: {}", code);
            return ValidationResult.invalid("Promo code usage limit has been reached");
        }

        log.info("Valid promo code validated: {} (type: {})", code, expectedType);
        return ValidationResult.valid(promoCode);
    }

    /**
     * Increments the usage count for a promo code after successful registration.
     *
     * @param code the promo code string
     * @return true if the usage count was updated
     */
    @Transactional
    public boolean incrementUsageCount(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }

        String normalizedCode = code.trim().toUpperCase();
        Optional<PromoCode> optional = promoCodeRepository.findByCode(normalizedCode);

        if (optional.isPresent()) {
            PromoCode promoCode = optional.get();
            promoCode.setUsedCount(promoCode.getUsedCount() + 1);
            promoCodeRepository.save(promoCode);
            log.info("Incremented usage count for promo code: {} (now {})", code, promoCode.getUsedCount());
            return true;
        }

        return false;
    }

    /**
     * Creates a new PLAYER promo code (backward compatible).
     *
     * @param code the promo code string
     * @param description human-readable description
     * @param maxUses maximum number of uses (null for unlimited)
     * @return the created promo code
     * @throws IllegalArgumentException if the code already exists
     */
    @Transactional
    public PromoCode createPromoCode(String code, String description, Integer maxUses) {
        return createPromoCode(code, description, maxUses, PromoCode.CodeType.PLAYER);
    }

    /**
     * Creates a new promo code with the specified type.
     *
     * @param code the promo code string
     * @param description human-readable description
     * @param maxUses maximum number of uses (null for unlimited)
     * @param codeType the type of promo code (PLAYER or ADMIN)
     * @return the created promo code
     * @throws IllegalArgumentException if the code already exists
     */
    @Transactional
    public PromoCode createPromoCode(String code, String description, Integer maxUses, PromoCode.CodeType codeType) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Promo code is required");
        }

        String normalizedCode = code.trim().toUpperCase();

        if (promoCodeRepository.existsByCode(normalizedCode)) {
            throw new IllegalArgumentException("Promo code already exists: " + normalizedCode);
        }

        PromoCode promoCode = new PromoCode();
        promoCode.setCode(normalizedCode);
        promoCode.setDescription(description);
        promoCode.setMaxUses(maxUses);
        promoCode.setCodeType(codeType != null ? codeType : PromoCode.CodeType.PLAYER);
        promoCode.setActive(true);

        PromoCode saved = promoCodeRepository.save(promoCode);
        log.info("Created new promo code: {}", saved);
        return saved;
    }

    /**
     * Finds a promo code by its code string.
     *
     * @param code the promo code string
     * @return optional promo code if found
     */
    public Optional<PromoCode> findByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.empty();
        }
        return promoCodeRepository.findByCode(code.trim().toUpperCase());
    }

    /**
     * Deactivates a promo code.
     *
     * @param code the promo code string
     * @return the deactivated promo code
     * @throws IllegalArgumentException if not found
     */
    @Transactional
    public PromoCode deactivatePromoCode(String code) {
        Optional<PromoCode> optional = findByCode(code);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Promo code not found: " + code);
        }
        PromoCode promoCode = optional.get();
        promoCode.setActive(false);
        log.info("Deactivated promo code: {}", code);
        return promoCodeRepository.save(promoCode);
    }

    /**
     * Activates a promo code.
     *
     * @param code the promo code string
     * @return the activated promo code
     * @throws IllegalArgumentException if not found
     */
    @Transactional
    public PromoCode activatePromoCode(String code) {
        Optional<PromoCode> optional = findByCode(code);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Promo code not found: " + code);
        }
        PromoCode promoCode = optional.get();
        promoCode.setActive(true);
        log.info("Activated promo code: {}", code);
        return promoCodeRepository.save(promoCode);
    }

    /**
     * Updates an existing promo code's properties.
     *
     * @param code the promo code string to update
     * @param description new description (null to leave unchanged)
     * @param maxUses new max uses (null to leave unchanged)
     * @param expiryDate new expiry date (null to leave unchanged)
     * @param bonuses new bonuses JSON (null to leave unchanged)
     * @return the updated promo code
     * @throws IllegalArgumentException if the code does not exist
     */
    @Transactional
    public PromoCode updatePromoCode(String code, String description, Integer maxUses,
                                      LocalDateTime expiryDate, String bonuses) {
        Optional<PromoCode> optional = findByCode(code);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Promo code not found: " + code);
        }

        PromoCode promoCode = optional.get();
        if (description != null) {
            promoCode.setDescription(description);
        }
        if (maxUses != null) {
            promoCode.setMaxUses(maxUses);
        }
        // Only update expiryDate if explicitly provided (non-null means set it, null means leave unchanged)
        // To clear expiry, pass a special flag via bonuses or use a separate method
        if (expiryDate != null) {
            promoCode.setExpiryDate(expiryDate);
        }
        if (bonuses != null) {
            promoCode.setBonuses(bonuses);
        }

        PromoCode saved = promoCodeRepository.save(promoCode);
        log.info("Updated promo code: {}", saved);
        return saved;
    }

    /**
     * Deletes a promo code by its code string.
     *
     * @param code the promo code string
     * @return true if the code was deleted
     */
    @Transactional
    public boolean deletePromoCode(String code) {
        Optional<PromoCode> optional = findByCode(code);
        if (optional.isPresent()) {
            PromoCode promoCode = optional.get();
            promoCodeRepository.delete(promoCode);
            log.info("Deleted promo code: {}", code);
            return true;
        }
        return false;
    }

    /**
     * Gets all promo codes.
     *
     * @return list of all promo codes
     */
    public java.util.List<PromoCode> findAll() {
        return promoCodeRepository.findAll();
    }

    /**
     * Gets all promo codes of a specific type.
     *
     * @param codeType the type of promo code (PLAYER or ADMIN)
     * @return list of promo codes of the specified type
     */
    public java.util.List<PromoCode> findAllByType(PromoCode.CodeType codeType) {
        return promoCodeRepository.findByCodeType(codeType);
    }
}
