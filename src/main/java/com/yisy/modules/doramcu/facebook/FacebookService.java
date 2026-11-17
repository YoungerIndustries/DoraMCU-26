package com.yisy.modules.doramcu.facebook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.facebook.dto.FacebookProfileData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for Facebook Graph API integration to fetch public profile data.
 * <p>
 * This service parses Facebook profile URLs to extract user IDs, calls the Facebook
 * Graph API to fetch public profile data, and maps the results to {@link FacebookProfileData}.
 * </p>
 * <p>
 * No OAuth or access token is required — only public profile fields are fetched.
 * The integration can be disabled via {@code doramcu.facebook.enabled=false}.
 * </p>
 */
@Service
public class FacebookService {

    private static final Logger log = LoggerFactory.getLogger(FacebookService.class);

    // Facebook profile URL patterns to extract user ID or username
    private static final Pattern FACEBOOK_ID_PATTERN = Pattern.compile(
            "facebook\\.com/?(\\d+)");
    private static final Pattern FACEBOOK_USERNAME_PATTERN = Pattern.compile(
            "facebook\\.com/([a-zA-Z0-9_.-]+)/?");

    private final FacebookConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public FacebookService(FacebookConfig config, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Checks if Facebook integration is enabled.
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        return config.isEnabled();
    }

    /**
     * Parses a Facebook profile URL or raw user ID to extract the Facebook user ID.
     * <p>
     * Supports multiple URL formats:
     * <ul>
     *   <li>https://www.facebook.com/123456789 (numeric ID)</li>
     *   <li>https://www.facebook.com/username (username)</li>
     *   <li>https://facebook.com/123456789 (without www)</li>
     *   <li>123456789 (raw numeric ID)</li>
     * </ul>
     * </p>
     *
     * @param profileUrlOrId the Facebook profile URL or user ID
     * @return the extracted Facebook user ID or username
     * @throws IllegalArgumentException if the input is invalid
     */
    public String parseProfileUrl(String profileUrlOrId) {
        if (profileUrlOrId == null || profileUrlOrId.isBlank()) {
            throw new IllegalArgumentException("Facebook profile URL or ID must not be null or blank");
        }

        String input = profileUrlOrId.trim();

        // Check if it's a raw numeric ID
        if (input.matches("\\d+")) {
            log.debug("Parsed raw Facebook ID: {}", input);
            return input;
        }

        // Try to extract numeric ID from URL
        Matcher idMatcher = FACEBOOK_ID_PATTERN.matcher(input);
        if (idMatcher.find()) {
            String id = idMatcher.group(1);
            log.debug("Extracted Facebook ID from URL: {}", id);
            return id;
        }

        // Try to extract username from URL
        Matcher usernameMatcher = FACEBOOK_USERNAME_PATTERN.matcher(input);
        if (usernameMatcher.find()) {
            String username = usernameMatcher.group(1);
            log.debug("Extracted Facebook username from URL: {}", username);
            return username;
        }

        throw new IllegalArgumentException("Invalid Facebook profile URL or ID: " + input);
    }

    /**
     * Fetches public profile data from Facebook Graph API for the given user ID or username.
     * <p>
     * Calls the Graph API endpoint: {graphApiUrl}/{facebookId}?fields=id,name,first_name,last_name,email,about,birthday,location,picture.width(400)
     * </p>
     * <p>
     * Note: Without an access token, only limited public data is available.
     * Fields like email, birthday, and about may not be returned.
     * </p>
     *
     * @param facebookId the Facebook user ID or username
     * @return optional Facebook profile data if the API call succeeds
     */
    public Optional<FacebookProfileData> fetchProfile(String facebookId) {
        if (!config.isEnabled()) {
            log.warn("Facebook integration is disabled. Cannot fetch profile for: {}", facebookId);
            return Optional.empty();
        }

        if (facebookId == null || facebookId.isBlank()) {
            throw new IllegalArgumentException("Facebook ID must not be null or blank");
        }

        String fields = "id,name,first_name,last_name,email,about,birthday,location,picture.width(400)";
        String url = config.getGraphApiUrl() + "/" + facebookId + "?fields=" + fields;

        log.info("Fetching Facebook profile data for: {}", facebookId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());

                // Check for error response from Facebook
                if (root.has("error")) {
                    String errorMessage = root.path("error").path("message").asText("Unknown error");
                    log.warn("Facebook Graph API returned error for {}: {}", facebookId, errorMessage);
                    return Optional.empty();
                }

                // Check if the user was found (id field present)
                if (!root.has("id")) {
                    log.warn("Facebook profile not found for: {}. Profile may be private or ID is invalid.", facebookId);
                    return Optional.empty();
                }

                FacebookProfileData data = mapJsonToProfileData(root, facebookId);
                log.info("Successfully fetched Facebook profile for: {} (ID: {})", 
                        data.getDisplayName(), data.getFacebookId());
                return Optional.of(data);
            }

            log.warn("Facebook Graph API returned non-success status for: {}", facebookId);
            return Optional.empty();

        } catch (RestClientException e) {
            log.error("Failed to fetch Facebook profile for {}: {}", facebookId, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unexpected error fetching Facebook profile for {}: {}", facebookId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Maps a JSON response from Facebook Graph API to a {@link FacebookProfileData} DTO.
     *
     * @param root the JSON root node from the Graph API response
     * @param originalInput the original user ID or username used for the profile URL
     * @return the mapped profile data
     */
    FacebookProfileData mapJsonToProfileData(JsonNode root, String originalInput) {
        FacebookProfileData data = new FacebookProfileData();

        // Facebook ID (always present for successful responses)
        data.setFacebookId(root.path("id").asText(null));

        // Name fields
        data.setDisplayName(root.path("name").asText(null));
        data.setFirstName(root.path("first_name").asText(null));
        data.setLastName(root.path("last_name").asText(null));

        // Optional fields (may not be present without OAuth)
        data.setEmail(root.path("email").asText(null));
        data.setBio(root.path("about").asText(null));
        data.setBirthday(root.path("birthday").asText(null));

        // Location (can be an object or string)
        JsonNode locationNode = root.path("location");
        if (locationNode.isObject()) {
            data.setLocation(locationNode.path("name").asText(null));
        } else {
            data.setLocation(locationNode.asText(null));
        }

        // Profile picture URL
        JsonNode pictureNode = root.path("picture");
        if (pictureNode.isObject()) {
            data.setProfilePictureUrl(pictureNode.path("data").path("url").asText(null));
        }

        // Construct the full profile URL
        String facebookId = data.getFacebookId();
        if (facebookId != null) {
            data.setProfileUrl("https://www.facebook.com/" + facebookId);
        } else {
            data.setProfileUrl("https://www.facebook.com/" + originalInput);
        }

        return data;
    }
}
