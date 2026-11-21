package com.yisy.modules.doramcu.controller.auth;
import com.yisy.modules.doramcu.model.misc.CharacterType;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.model.user.User;
import com.yisy.modules.doramcu.dto.auth.RegisterRequest;
import com.yisy.modules.doramcu.event.GalaxyEvent;
import com.yisy.modules.doramcu.event.GalaxyEventService;
import com.yisy.modules.doramcu.corporation.model.SilverCurrencyConstants;
import com.yisy.modules.doramcu.corporation.service.TreasuryLoanService;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.eve.model.EveCorporation;
import com.yisy.modules.doramcu.hex.GalaxyFactory;
import com.yisy.modules.doramcu.model.misc.PromoCode;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import com.yisy.modules.doramcu.repository.misc.EveCorporationRepository;
import com.yisy.modules.doramcu.repository.galaxy.GalaxyRepository;
import com.yisy.modules.doramcu.repository.players.PlayerRepository;
import com.yisy.modules.doramcu.repository.misc.PromoCodeRepository;
import com.yisy.modules.doramcu.repository.user.UserRepository;
import com.yisy.modules.doramcu.service.auth.JwtService;
import com.yisy.modules.doramcu.service.players.PlayerService;
import com.yisy.modules.doramcu.service.admin.PromoCodeService;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CharacterRepository;
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
    private final PromoCodeRepository promoCodeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PlayerService playerService;
    private final PlayerRepository playerRepository;
    private final GalaxyRepository galaxyRepository;
    private final Earth2026Service earth2026Service;
    private final Earth2026CharacterRepository earth2026CharacterRepository;
    private final EveCorporationRepository eveCorporationRepository;
    private final AiCharacterRepository aiCharacterRepository;
    private final GalaxyEventService galaxyEventService;
    private final TreasuryLoanService treasuryLoanService;

    // Seed promo code names that should never be deleted
    private static final Set<String> SEED_PROMO_CODES = Set.of("IMMORTAL", "DRAGONS", "RESET", "GALAXY_RESET", "WELCOME2026", "EARLYBIRD");

    // Seed user/player emails that should never be deleted
    private static final Set<String> SEED_PLAYER_EMAILS = Set.of(
            "butch.younger@empire.com"
    );

    // Seed user names that should never be deleted
    private static final Set<String> SEED_USERNAMES = Set.of(
            "Butch Younger"
    );

    // Seed corporation tickers that should never be deleted
    private static final Set<String> SEED_CORP_TICKERS = Set.of("YISY");

    public OnboardingController(
            PromoCodeService promoCodeService,
            PromoCodeRepository promoCodeRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            ObjectProvider<PlayerService> playerServiceProvider,
            ObjectProvider<PlayerRepository> playerRepositoryProvider,
            ObjectProvider<GalaxyRepository> galaxyRepositoryProvider,
            ObjectProvider<Earth2026Service> earth2026ServiceProvider,
            ObjectProvider<Earth2026CharacterRepository> earth2026CharacterRepositoryProvider,
            ObjectProvider<EveCorporationRepository> eveCorporationRepositoryProvider,
            ObjectProvider<AiCharacterRepository> aiCharacterRepositoryProvider,
            ObjectProvider<GalaxyEventService> galaxyEventServiceProvider,
            TreasuryLoanService treasuryLoanService) {
        this.promoCodeService = promoCodeService;
        this.promoCodeRepository = promoCodeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.playerService = playerServiceProvider.getIfAvailable();
        this.playerRepository = playerRepositoryProvider.getIfAvailable();
        this.galaxyRepository = galaxyRepositoryProvider.getIfAvailable();
        this.earth2026Service = earth2026ServiceProvider.getIfAvailable();
        this.earth2026CharacterRepository = earth2026CharacterRepositoryProvider.getIfAvailable();
        this.eveCorporationRepository = eveCorporationRepositoryProvider.getIfAvailable();
        this.aiCharacterRepository = aiCharacterRepositoryProvider.getIfAvailable();
        this.galaxyEventService = galaxyEventServiceProvider.getIfAvailable();
        this.treasuryLoanService = treasuryLoanService;
    }

    /**
     * GET /api/v1/onboarding/door
     * Returns the door panel configuration for the landing page.
     * This is the first step of the onboarding flow — the "Do You Want to Live Forever?" panel.
     */
    @GetMapping("/door")
    public ResponseEntity<Map<String, Object>> getDoorPanel() {
        Map<String, Object> door = new HashMap<>();
        door.put("title", "Do You Want to Live Forever?");
        door.put("subtitle", "Step through the door and begin your journey across the DoraMCU Universe.");
        door.put("description", "Create your account, define your corporation, and land on Earth 2026 to start your adventure.");
        door.put("galaxyName", GalaxyFactory.EARTH_2026_GALAXY_NAME);
        door.put("landingLocation", "San Diego, CA, USA");
        door.put("homeType", "Apartment");

        // Include recent news events if available
        if (galaxyEventService != null) {
            List<GalaxyEvent> events = galaxyEventService.getEventHistory(null);
            List<Map<String, Object>> recentNews = new ArrayList<>();
            int fromIndex = Math.max(0, events.size() - 10);
            for (int i = fromIndex; i < events.size(); i++) {
                GalaxyEvent event = events.get(i);
                Map<String, Object> item = new HashMap<>();
                item.put("type", event.getType().name());
                item.put("title", event.getType().formatTitle(event.getGalaxyName()));
                item.put("galaxyName", event.getGalaxyName());
                item.put("timestamp", event.getTimestamp().toString());
                recentNews.add(item);
            }
            door.put("recentNews", recentNews);
            door.put("newsCount", recentNews.size());
        }

        return ResponseEntity.ok(door);
    }

    /**
     * GET /api/v1/onboarding/config
     * Returns the configuration for the onboarding flow, including available payment methods.
     * If the user provides a username query parameter, it also returns the playerId and existing characters.
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getOnboardingConfig(@RequestParam(required = false) String username) {
        Map<String, Object> config = new HashMap<>();
        config.put("steps", Arrays.asList("door", "promo", "register", "character", "earth"));
        config.put("paymentMethods", getPaymentMethods());
        config.put("galaxyName", GalaxyFactory.EARTH_2026_GALAXY_NAME);

        // If username is provided, look up the player and their characters
        if (username != null && !username.trim().isEmpty() && playerRepository != null) {
            Optional<User> userOpt = userRepository.findByUsername(username.trim());
            if (userOpt.isPresent()) {
                Optional<Player> playerOpt = playerRepository.findByUser(userOpt.get());
                if (playerOpt.isPresent()) {
                    Long playerId = playerOpt.get().getId();
                    config.put("playerId", playerId);
                    
                    // Check if the player already has characters on Earth
                    if (earth2026Service != null) {
                        List<Earth2026Character> characters = earth2026Service.getCharactersByPlayer(playerId);
                        config.put("existingCharacters", characters);
                        config.put("hasCharacters", !characters.isEmpty());
                    }
                }
            }
        }

        // Include recent news events if available
        if (galaxyEventService != null) {
            List<GalaxyEvent> events = galaxyEventService.getEventHistory(null);
            List<Map<String, Object>> recentNews = new ArrayList<>();
            int fromIndex = Math.max(0, events.size() - 10);
            for (int i = fromIndex; i < events.size(); i++) {
                GalaxyEvent event = events.get(i);
                Map<String, Object> item = new HashMap<>();
                item.put("type", event.getType().name());
                item.put("title", event.getType().formatTitle(event.getGalaxyName()));
                item.put("galaxyName", event.getGalaxyName());
                item.put("timestamp", event.getTimestamp().toString());
                recentNews.add(item);
            }
            config.put("recentNews", recentNews);
        }

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
     * Supports all code types: PLAYER, CORPORATION, CHARACTER, ADMIN.
     * <p>
     * For CORPORATION codes: returns corporationId so the frontend can navigate to the corporation screen.
     * For CHARACTER codes: returns characterId so the frontend can land on Earth with that character.
     * For PLAYER codes: returns bonuses and description for the onboarding flow.
     * </p>
     * Used by the landing page to determine where to route the player.
     */
    @PostMapping("/promo/validate")
    public ResponseEntity<?> validatePromoCode(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Promo code is required"));
        }

        // Use validateAny to accept all code types (PLAYER, CORPORATION, CHARACTER, ADMIN)
        PromoCodeService.ValidationResult result = promoCodeService.validateAny(code);

        if (result.isValid()) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("message", "Promo code is valid");
            response.put("codeType", result.getPromoCode().getCodeType().name());
            response.put("description", result.getPromoCode().getDescription());
            if (result.getPromoCode().getBonuses() != null) {
                response.put("bonuses", result.getPromoCode().getBonuses());
            }

            // For CORPORATION codes, return the associated corporation (player) ID
            if (result.getPromoCode().getCodeType() == PromoCode.CodeType.CORPORATION) {
                if (result.getPromoCode().getCorporationId() != null) {
                    response.put("corporationId", result.getPromoCode().getCorporationId());
                }
            }

            // For CHARACTER codes, return the associated character ID
            if (result.getPromoCode().getCodeType() == PromoCode.CodeType.CHARACTER) {
                if (result.getPromoCode().getCharacterId() != null) {
                    response.put("characterId", result.getPromoCode().getCharacterId());
                }
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
                Player player = new Player();
                String email = (String) request.get("email");
                player.setEmail(email != null ? email : username + "@doramcu.local");
                player.setLoginPin(pin);
                player.setEmpireName(empireName);

                String govTypeStr = (String) request.get("governmentType");
                if (govTypeStr != null && !govTypeStr.trim().isEmpty()) {
                    try {
                        player.setGovernmentType(Player.GovernmentType.valueOf(govTypeStr.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        player.setGovernmentType(Player.GovernmentType.DEMOCRACY);
                    }
                } else {
                    player.setGovernmentType(Player.GovernmentType.DEMOCRACY);
                }

                String ethosStr = (String) request.get("ethos");
                if (ethosStr != null && !ethosStr.trim().isEmpty()) {
                    try {
                        player.setEthos(Player.Ethos.valueOf(ethosStr.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        player.setEthos(Player.Ethos.MATERIALIST);
                    }
                } else {
                    player.setEthos(Player.Ethos.MATERIALIST);
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

        // Find the player ID for linking promo codes
        Long playerId = null;
        if (playerRepository != null) {
            Optional<User> userOpt = userRepository.findByUsername(username.trim());
            if (userOpt.isPresent()) {
                Optional<Player> playerOpt = playerRepository.findByUser(userOpt.get());
                if (playerOpt.isPresent()) {
                    playerId = playerOpt.get().getId();
                }
            }
        }

        // Create the player's corporation and AI VP as part of account creation
        Long corporationId = null;
        String corporationTicker = null;
        String aiVpName = null;
        Earth2026Character firstChar = null;
        if (playerId != null && eveCorporationRepository != null && aiCharacterRepository != null) {
            try {
                // Derive corporation name and ticker from the player's empire name
                String corpName = empireName != null ? empireName.trim() : username.trim() + " Industries";
                String tickerBase = corpName.replaceAll("[^A-Za-z]", "").substring(0, Math.min(corpName.replaceAll("[^A-Za-z]", "").length(), 4)).toUpperCase();
                // Ensure unique ticker
                String ticker = tickerBase;
                int suffix = 1;
                while (eveCorporationRepository.findByTicker(ticker).isPresent()) {
                    ticker = tickerBase + suffix;
                    suffix++;
                }

                // Create the corporation
                EveCorporation corporation = new EveCorporation();
                corporation.setName(corpName);
                corporation.setTicker(ticker);
                corporation.setCeoName(username.trim());
                corporation.setCeoPlayerId(String.valueOf(playerId));
                corporation.setDescription("Corporation founded by " + username.trim() + ".");
                // Silver-backed starting capital: $500 USD = 100,000 credits
                // Player receives full amount, with a treasury loan for $495 of it
                corporation.setCreditBalance(SilverCurrencyConstants.STARTING_CAPITAL_CREDITS);
                corporation = eveCorporationRepository.save(corporation);
                corporationId = corporation.getId();
                corporationTicker = ticker;
                log.info("Onboarding: created corporation '{}' ({}) for user: {} with {} starting capital",
                        corpName, ticker, username,
                        SilverCurrencyConstants.formatCredits(SilverCurrencyConstants.STARTING_CAPITAL_CREDITS));

                // Create the treasury loan for the remaining $495 USD
                try {
                    treasuryLoanService.createStartupLoan(corporationId);
                    log.info("Onboarding: created treasury loan for corporation {} ({})", corpName, ticker);
                } catch (Exception e) {
                    log.error("Onboarding: failed to create treasury loan for corporation {}: {}",
                            corpName, e.getMessage());
                }

                // Create the AI VP for the corporation
                String vpName = username.trim().replace(" ", "") + " AI";
                AiCharacter aiVp = new AiCharacter();
                aiVp.setName(vpName);
                aiVp.setRole("VP");
                aiVp.setEmpireName(corpName);
                aiVp.setLinkedPlayerId(String.valueOf(playerId));
                aiVp.setActive(true);
                aiVp.setHirable(false);
                aiVp.setHiringStatus(AiCharacter.HiringStatus.UNAVAILABLE);
                aiVp.setTotalExperience(100);
                aiVp.setLeadershipRating(7);
                aiVp.setDescription("AI Vice President of " + corpName + ". " +
                        "Serves as the intelligent operations manager and strategic advisor. " +
                        "Can assume the CEO role when the founder is unavailable.");
                aiVp.setCharacterType(AiCharacter.CharacterType.HUMAN_LEADER);
                aiVp.setCanPromoteToCoServer(true);
                aiVp = aiCharacterRepository.save(aiVp);
                aiVpName = vpName;

                // Set the AI VP on the corporation
                corporation.setVpName(vpName);
                corporation.setVpPlayerId(String.valueOf(aiVp.getId()));
                eveCorporationRepository.save(corporation);
                log.info("Onboarding: created AI VP '{}' for corporation '{}' (user: {})", vpName, corpName, username);

                // Generate a CORPORATION-type promo code for the corporation
                String corpPromoCode = "C" + ticker + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
                PromoCode corpPromo = promoCodeService.createPromoCode(
                        corpPromoCode,
                        "Corporation code for " + corpName,
                        null,
                        PromoCode.CodeType.CORPORATION,
                        null, corporationId, null
                );
                corporation.setPromoCodeId(corpPromo.getId());
                eveCorporationRepository.save(corporation);
                log.info("Onboarding: generated corporation promo code: {} for user: {}", corpPromoCode, username);

                // Create the player's first character as CEO of their corporation
                try {
                    String charName = username.trim() + " Prime";
                    firstChar = earth2026Service.createCharacterAsEmployee(
                            charName, 25, "CEO", playerId, corporationId, true, "EARTH_2026");
                    log.info("Onboarding: created first character '{}' as CEO for user: {} (corporation: {})",
                            charName, username, corpName);
                } catch (Exception e) {
                    log.warn("Onboarding: could not create first character for {}: {}", username, e.getMessage());
                }

            } catch (Exception e) {
                log.warn("Onboarding: could not create corporation/AI VP for {}: {}", username, e.getMessage());
            }
        }

        // Generate a unique promo code for this new player so they can reference it
        String generatedPromoCode = null;
        try {
            // Create a unique, permanent promo code tied to this player
            String codeString = "P" + username.trim().toUpperCase().replace(" ", "_").substring(0, Math.min(username.trim().length(), 6))
                    + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            PromoCode newPromo = promoCodeService.createPromoCode(
                    codeString,
                    "Welcome code for " + username.trim(),
                    null,
                    PromoCode.CodeType.PLAYER,
                    playerId, null, null
            );
            generatedPromoCode = newPromo.getCode();
            log.info("Onboarding: generated promo code: {} for user: {}", generatedPromoCode, username);

            // Link the promo code back to the player entity
            if (playerId != null) {
                Optional<User> userOpt = userRepository.findByUsername(username.trim());
                if (userOpt.isPresent()) {
                    Optional<Player> playerOpt = playerRepository.findByUser(userOpt.get());
                    if (playerOpt.isPresent()) {
                        Player player = playerOpt.get();
                        player.setPromoCodeId(newPromo.getId());
                        playerRepository.save(player);
                        log.info("Onboarding: linked promo code {} to player {}", newPromo.getId(), playerId);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Onboarding: could not generate promo code for {}: {}", username, e.getMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", username);
        response.put("empireName", empireName);
        response.put("galaxy", galaxyName);
        response.put("nextStep", "earth2026");
        response.put("message", "Registration complete — your corporation, AI VP, and first character have been created. You are now the CEO at your corporation HQ.");
        if (firstChar != null) {
            response.put("characterId", firstChar.getId());
            response.put("characterName", firstChar.getName());
        }
        if (pin != null) {
            response.put("playerPin", pin);
        }
        if (playerId != null) {
            response.put("playerId", playerId);
        }
        if (corporationId != null) {
            response.put("corporationId", corporationId);
            response.put("corporationTicker", corporationTicker);
            response.put("corporationName", empireName != null ? empireName.trim() : username.trim() + " Industries");
            response.put("aiVpName", aiVpName);
        }
        if (generatedPromoCode != null) {
            response.put("promoCode", generatedPromoCode);
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
                Optional<Player> playerOpt = playerRepository.findByUser(userOpt.get());
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

        // Generate a CHARACTER-type promo code for this character
        String characterPromoCode = null;
        try {
            String codeString = "CH" + characterName.trim().toUpperCase().replace(" ", "_").substring(0, Math.min(characterName.trim().length(), 6))
                    + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            PromoCode charPromo = promoCodeService.createPromoCode(
                    codeString,
                    "Character landing key for " + characterName.trim(),
                    null,
                    PromoCode.CodeType.CHARACTER,
                    null, null, character.getId()
            );
            characterPromoCode = charPromo.getCode();

            // Link the promo code back to the character entity
            character.setPromoCodeId(charPromo.getId());
            earth2026Service.saveCharacter(character);
            log.info("Onboarding: generated character promo code: {} for character {}", characterPromoCode, character.getId());
        } catch (Exception e) {
            log.warn("Onboarding: could not generate character promo code for {}: {}", characterName, e.getMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("character", character);
        response.put("location", "Earth 2026");
        response.put("nextStep", "earth");
        response.put("message", "Welcome to Earth 2026 — your journey begins");
        if (characterPromoCode != null) {
            response.put("characterPromoCode", characterPromoCode);
        }
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
        response.put("landingCity", "San Diego, CA, USA");

        if (earth2026Service != null) {
            response.put("locations", earth2026Service.getAllLocations());
            response.put("scenarios", earth2026Service.getAvailableScenarios(1));
            response.put("characters", earth2026Service.getCharactersOnEarth());
        }

        response.put("message", "You have arrived on Earth 2026 in San Diego, CA. Your apartment is ready. Choose your first scenario to begin.");
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/promo-codes
     * Returns all generated promo codes with their details.
     * Used by the admin dashboard to display and manage promo codes.
     */
    @GetMapping("/promo-codes")
    public ResponseEntity<List<Map<String, Object>>> getAllPromoCodes() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (PromoCode pc : promoCodeService.findAll()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", pc.getId());
            item.put("code", pc.getCode());
            item.put("description", pc.getDescription());
            item.put("type", pc.getCodeType().name());
            item.put("usedCount", pc.getUsedCount());
            item.put("maxUses", pc.getMaxUses());
            item.put("expiryDate", pc.getExpiryDate());
            item.put("active", pc.isActive());
            item.put("bonuses", pc.getBonuses());
            item.put("expired", pc.isExpired());
            item.put("usageLimitReached", pc.isUsageLimitReached());
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v1/onboarding/cleanup
     * Cleans the database by removing all non-seed players, corporations, characters,
     * and promo codes created during onboarding sessions.
     * <p>
     * Preserves the seed data created at startup:
     * - Users: Butch Younger
     * - Players: Butch Younger
     * - Corporations: Younger Industries (YISY)
     * - AI Characters: Sif Younger (AI VP of Younger Industries) and all world leaders/governors
     * - Promo codes: IMMORTAL, DRAGONS, RESET, GALAXY_RESET, WELCOME2026, EARLYBIRD
     * </p>
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupNonSeedData() {
        log.warn("=== DATABASE CLEANUP INITIATED ===");
        Map<String, Object> result = new HashMap<>();
        int playersDeleted = 0;
        int charactersDeleted = 0;
        int corporationsDeleted = 0;
        int promoCodesDeleted = 0;
        int usersDeleted = 0;

        // 1. Delete non-seed Earth 2026 characters (those linked to non-seed players)
        if (earth2026CharacterRepository != null && playerRepository != null) {
            List<Long> seedPlayerIds = new ArrayList<>();
            for (Player p : playerRepository.findAll()) {
                if (SEED_PLAYER_EMAILS.contains(p.getEmail())) {
                    seedPlayerIds.add(p.getId());
                }
            }
            List<Earth2026Character> allCharacters = earth2026CharacterRepository.findAll();
            for (Earth2026Character charEntity : allCharacters) {
                if (charEntity.getPlayerId() != null && !seedPlayerIds.contains(charEntity.getPlayerId())) {
                    earth2026CharacterRepository.delete(charEntity);
                    charactersDeleted++;
                    log.info("Cleanup: deleted non-seed character: {}", charEntity.getName());
                }
            }
        }

        // 2. Delete non-seed corporations
        if (eveCorporationRepository != null) {
            List<EveCorporation> allCorps = eveCorporationRepository.findAll();
            for (EveCorporation corp : allCorps) {
                if (!SEED_CORP_TICKERS.contains(corp.getTicker())) {
                    eveCorporationRepository.delete(corp);
                    corporationsDeleted++;
                    log.info("Cleanup: deleted non-seed corporation: {}", corp.getName());
                }
            }
        }

        // 3. Delete non-seed promo codes
        if (promoCodeRepository != null) {
            List<PromoCode> allPromos = promoCodeRepository.findAll();
            for (PromoCode promo : allPromos) {
                if (!SEED_PROMO_CODES.contains(promo.getCode())) {
                    promoCodeRepository.delete(promo);
                    promoCodesDeleted++;
                    log.info("Cleanup: deleted non-seed promo code: {}", promo.getCode());
                }
            }
        }

        // 4. Delete non-seed players
        if (playerRepository != null) {
            List<Player> allPlayers = playerRepository.findAll();
            for (Player player : allPlayers) {
                if (!SEED_PLAYER_EMAILS.contains(player.getEmail())) {
                    playerRepository.delete(player);
                    playersDeleted++;
                    log.info("Cleanup: deleted non-seed player: {} ({})", player.getEmpireName(), player.getEmail());
                }
            }
        }

        // 5. Delete non-seed users
        if (userRepository != null) {
            List<User> allUsers = userRepository.findAll();
            for (User user : allUsers) {
                if (!SEED_USERNAMES.contains(user.getUsername())) {
                    userRepository.delete(user);
                    usersDeleted++;
                    log.info("Cleanup: deleted non-seed user: {}", user.getUsername());
                }
            }
        }

        result.put("success", true);
        result.put("message", "Database cleanup complete");
        result.put("playersDeleted", playersDeleted);
        result.put("charactersDeleted", charactersDeleted);
        result.put("corporationsDeleted", corporationsDeleted);
        result.put("promoCodesDeleted", promoCodesDeleted);
        result.put("usersDeleted", usersDeleted);

        log.info("=== DATABASE CLEANUP COMPLETED: {} players, {} characters, {} corporations, {} promo codes, {} users deleted ===",
                playersDeleted, charactersDeleted, corporationsDeleted, promoCodesDeleted, usersDeleted);
        return ResponseEntity.ok(result);
    }

}
