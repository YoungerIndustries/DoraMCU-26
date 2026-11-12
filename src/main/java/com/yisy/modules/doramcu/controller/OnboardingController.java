package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.RegisterRequest;
import com.yisy.modules.doramcu.hex.GalaxyFactory;
import com.yisy.modules.doramcu.model.PromoCode;
import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.repository.UserRepository;
import com.yisy.modules.doramcu.service.JwtService;
import com.yisy.modules.doramcu.service.PlayerService;
import com.yisy.modules.doramcu.service.PromoCodeService;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.service.Earth2026Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * REST API for the new onboarding flow.
 * <p>
 * Guides new players through a multi-step journey:
 * <ol>
 *   <li><b>Door</b> — Landing page with the "Do You Want to Live Forever?" panel</li>
 *   <li><b>Promo/Payment</b> — Enter a promo code or select a payment method</li>
 *   <li><b>Registration</b> — Create account credentials</li>
 *   <li><b>Corporation/Character</b> — Define empire and create Earth 2026 character</li>
 *   <li><b>Earth 2026</b> — Land on Earth and begin the game</li>
 * </ol>
 * </p>
 */
@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {

    private static final Logger log = LoggerFactory.getLogger(OnboardingController.class);

    private final PromoCodeService promoCodeService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PlayerService playerService;
    private final PlayerRepository playerRepository;
    private final GalaxyRepository galaxyRepository;
    private final Earth2026Service earth2026Service;

    public OnboardingController(
            PromoCodeService promoCodeService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            ObjectProvider<PlayerService> playerServiceProvider,
            ObjectProvider<PlayerRepository> playerRepositoryProvider,
            ObjectProvider<GalaxyRepository> galaxyRepositoryProvider,
            ObjectProvider<Earth2026Service> earth2026ServiceProvider) {
        this.promoCodeService = promoCodeService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.playerService = playerServiceProvider.getIfAvailable();
        this.playerRepository = playerRepositoryProvider.getIfAvailable();
        this.galaxyRepository = galaxyRepositoryProvider.getIfAvailable();
        this.earth2026Service = earth2026ServiceProvider.getIfAvailable();
    }

    /**
     * GET /api/v1/onboarding/config
     * Returns the configuration for the onboarding flow, including available payment methods.
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getOnboardingConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("steps", Arrays.asList("door", "promo", "register", "character", "earth"));
        config.put("paymentMethods", getPaymentMethods());
        config.put("galaxyName", GalaxyFactory.EARTH_2026_GALAXY_NAME);
        return ResponseEntity.ok(config);
    }

    /**
     * Returns the list of available payment methods.
     * Currently supports: PayPal, Google Pay, Chime (placeholder integrations).
     */
    private List<Map<String, Object>> getPaymentMethods() {
        List<Map<String, Object>> methods = new ArrayList<>();

        Map<String, Object> paypal = new HashMap<>();
        paypal.put("id", "paypal");
        paypal.put("name", "PayPal");
        paypal.put("description", "Pay with your PayPal account");
        paypal.put("enabled", true);
        paypal.put("type", "wallet");
        methods.add(paypal);

        Map<String, Object> googlePay = new HashMap<>();
        googlePay.put("id", "google_pay");
        googlePay.put("name", "Google Pay");
        googlePay.put("description", "Pay quickly with Google Pay");
        googlePay.put("enabled", true);
        googlePay.put("type", "wallet");
        methods.add(googlePay);

        Map<String, Object> chime = new HashMap<>();
        chime.put("id", "chime");
        chime.put("name", "Chime");
        chime.put("description", "Pay with your Chime account");
        chime.put("enabled", true);
        chime.put("type", "bank");
        methods.add(chime);

        return methods;
    }

    /**
     * POST /api/v1/onboarding/promo/validate
     * Validates a promo code without consuming it.
     * Used by the Promo/Payment step to check code validity before registration.
     */
    @PostMapping("/promo/validate")
    public ResponseEntity<?> validatePromoCode(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Promo code is required"));
        }

        PromoCodeService.ValidationResult result = promoCodeService.validate(code);

        if (result.isValid()) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("message", "Promo code is valid");
            response.put("description", result.getPromoCode().getDescription());
            if (result.getPromoCode().getBonuses() != null) {
                response.put("bonuses", result.getPromoCode().getBonuses());
            }
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "error", result.getMessage() != null ? result.getMessage() : "Invalid promo code"
            ));
        }
    }

    /**
     * POST /api/v1/onboarding/payment/verify
     * Verifies a payment method selection.
     * Currently returns a placeholder success — actual payment gateway integration
     * (PayPal, Google Pay, Chime) would be wired in here.
     */
    @PostMapping("/payment/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, Object> request) {
        String method = (String) request.get("method");
        if (method == null || method.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Payment method is required"));
        }

        // Validate payment method is known
        List<Map<String, Object>> methods = getPaymentMethods();
        boolean found = false;
        for (Map<String, Object> m : methods) {
            if (method.equals(m.get("id")) && Boolean.TRUE.equals(m.get("enabled"))) {
                found = true;
                break;
            }
        }
        if (!found) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown or disabled payment method: " + method));
        }

        // Placeholder: in production, this would initiate a payment intent with the selected provider
        // For now, we accept the payment method selection and return a token for the next step
        String paymentToken = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Payment method selected: {} (token: {})", method, paymentToken);

        Map<String, Object> response = new HashMap<>();
        response.put("verified", true);
        response.put("paymentToken", paymentToken);
        response.put("method", method);
        response.put("message", "Payment method verified — proceed to registration");
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/onboarding/register
     * Completes the registration step: creates the user account, optionally validates
     * a promo code, and provisions a player record.
     * <p>
     * Request body:
     * {
     *   "username": "...",
     *   "password": "...",
     *   "email": "...",
     *   "promoCode": "optional",
     *   "paymentToken": "optional, from payment/verify",
     *   "empireName": "optional",
     *   "governmentType": "optional",
     *   "ethos": "optional"
     * }
     * </p>
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String password = (String) request.get("password");

        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
        }
        if (password == null || password.length() < 4) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 4 characters"));
        }

        // Check for existing user
        if (userRepository.findByUsername(username.trim()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }

        // Validate promo code if provided
        String promoCode = (String) request.get("promoCode");
        boolean promoValid = false;
        if (promoCode != null && !promoCode.trim().isEmpty()) {
            PromoCodeService.ValidationResult result = promoCodeService.validate(promoCode);
            if (!result.isValid()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid promo code: " + result.getMessage()));
            }
            promoValid = true;
        }

        // Determine galaxy
        String galaxyName;
        if (galaxyRepository != null && !galaxyRepository.findAll().isEmpty()) {
            galaxyName = galaxyRepository.findAll().get(0).getName();
        } else {
            galaxyName = GalaxyFactory.EARTH_2026_GALAXY_NAME;
        }

        // Create user
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setGalaxyName(galaxyName);
        userRepository.save(user);
        log.info("Onboarding: new user registered: {} (galaxy: {})", username, galaxyName);

        // Consume promo code if valid
        if (promoValid) {
            promoCodeService.incrementUsageCount(promoCode);
            log.info("Onboarding: promo code consumed: {} for user: {}", promoCode, username);
        }

        // Provision player record
        String pin = null;
        String empireName = (String) request.get("empireName");
        if (empireName == null || empireName.trim().isEmpty()) {
            empireName = username.trim() + " Empire";
        }

        if (playerService != null && playerRepository != null) {
            try {
                pin = playerService.generateUniquePin();
                com.yisy.modules.doramcu.model.Player player = new com.yisy.modules.doramcu.model.Player();
                String email = (String) request.get("email");
                player.setEmail(email != null ? email : username + "@doramcu.local");
                player.setLoginPin(pin);
                player.setEmpireName(empireName);

                String govTypeStr = (String) request.get("governmentType");
                if (govTypeStr != null && !govTypeStr.trim().isEmpty()) {
                    try {
                        player.setGovernmentType(com.yisy.modules.doramcu.model.Player.GovernmentType.valueOf(govTypeStr.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        player.setGovernmentType(com.yisy.modules.doramcu.model.Player.GovernmentType.DEMOCRACY);
                    }
                } else {
                    player.setGovernmentType(com.yisy.modules.doramcu.model.Player.GovernmentType.DEMOCRACY);
                }

                String ethosStr = (String) request.get("ethos");
                if (ethosStr != null && !ethosStr.trim().isEmpty()) {
                    try {
                        player.setEthos(com.yisy.modules.doramcu.model.Player.Ethos.valueOf(ethosStr.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        player.setEthos(com.yisy.modules.doramcu.model.Player.Ethos.MATERIALIST);
                    }
                } else {
                    player.setEthos(com.yisy.modules.doramcu.model.Player.Ethos.MATERIALIST);
                }

                player.setUser(user);
                playerRepository.save(player);
                log.info("Onboarding: provisioned player PIN: {} for user: {}", pin, username);
            } catch (Exception e) {
                log.warn("Onboarding: could not provision player for {}: {}", username, e.getMessage());
            }
        }

        // Generate JWT token
        String token = jwtService.generateToken(username.trim());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", username);
        response.put("empireName", empireName);
        response.put("galaxy", galaxyName);
        response.put("nextStep", "character");
        response.put("message", "Registration complete — create your character to enter Earth 2026");
        if (pin != null) {
            response.put("playerPin", pin);
        }
        if (promoValid) {
            response.put("promoApplied", promoCode);
        }
        String paymentToken = (String) request.get("paymentToken");
        if (paymentToken != null) {
            response.put("paymentToken", paymentToken);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/v1/onboarding/character
     * Creates the player's Earth 2026 character and lands them on Earth.
     * <p>
     * Request body:
     * {
     *   "username": "...",
     *   "characterName": "...",
     *   "age": 25,
     *   "occupation": "optional"
     * }
     * </p>
     */
    @PostMapping("/character")
    public ResponseEntity<?> createCharacter(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String characterName = (String) request.get("characterName");

        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
        }
        if (characterName == null || characterName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Character name is required"));
        }

        // Find the user's player ID
        Long playerId = null;
        if (playerRepository != null) {
            Optional<User> userOpt = userRepository.findByUsername(username.trim());
            if (userOpt.isPresent()) {
                Optional<com.yisy.modules.doramcu.model.Player> playerOpt = playerRepository.findByUser(userOpt.get());
                if (playerOpt.isPresent()) {
                    playerId = playerOpt.get().getId();
                }
            }
        }

        // Create Earth 2026 character
        int age = request.get("age") != null ? ((Number) request.get("age")).intValue() : 25;
        String occupation = (String) request.get("occupation");

        if (earth2026Service == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Earth 2026 service not available"));
        }

        Earth2026Character character = earth2026Service.createCharacter(characterName.trim(), age, occupation, playerId);

        Map<String, Object> response = new HashMap<>();
        response.put("character", character);
        response.put("location", "Earth 2026");
        response.put("nextStep", "earth");
        response.put("message", "Welcome to Earth 2026 — your journey begins");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/onboarding/earth
     * Returns the Earth 2026 landing page data: available locations, scenarios, and the player's character.
     */
    @GetMapping("/earth")
    public ResponseEntity<?> getEarthLanding(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        response.put("galaxy", GalaxyFactory.EARTH_2026_GALAXY_NAME);
        response.put("location", "Earth 2026");

        if (earth2026Service != null) {
            response.put("locations", earth2026Service.getAllLocations());
            response.put("scenarios", earth2026Service.getAvailableScenarios(1));
            response.put("characters", earth2026Service.getCharactersOnEarth());
        }

        response.put("message", "You have arrived on Earth 2026. Choose your first scenario to begin.");
        return ResponseEntity.ok(response);
    }
}
