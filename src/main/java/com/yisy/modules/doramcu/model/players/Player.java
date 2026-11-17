package com.yisy.modules.doramcu.model.players;
import com.yisy.modules.doramcu.model.misc.CorporateRole;
import com.yisy.modules.doramcu.model.misc.PromoCode;
import com.yisy.modules.doramcu.model.user.User;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * JPA entity representing a player in the 4X empire game.
 * <p>
 * Extends the basic User entity with 4X game-specific characteristics:
 * email for account management, login PIN for authentication, and empire
 * attributes (name, government type, ethos) for gameplay.
 * </p>
 */
@Entity
@Table(name = "players",
        indexes = {
                @Index(name = "idx_player_email", columnList = "email"),
                @Index(name = "idx_player_login_pin", columnList = "loginPin"),
                @Index(name = "idx_player_user_id", columnList = "userId")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_player_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_player_login_pin", columnNames = "loginPin")
        })
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 6)
    private String loginPin;

    @Column(nullable = false)
    private String empireName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
    private GovernmentType governmentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
    private Ethos ethos;

    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 50)
    private String corporateRole;

    @Column(length = 200)
    private String displayName;

    /**
     * Current active corporate role: CEO (full control) or VP (delegated to AI assistant).
     * Part of the dual-identity system — defaults to CEO at character creation.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "\"current_role\"", nullable = false, length = 10, columnDefinition = "varchar(10)")
    private CorporateRole currentRole = CorporateRole.CEO;

    /**
     * Reference to the AI assistant player session ID.
     * Populated when the player switches to VP mode and an AI clone is created.
     */
    @Column(name = "ai_assistant_id")
    private Long aiAssistantId;

    @ManyToOne
    private User user;

    /**
     * Whether this player has administrative rights to access the admin panel
     * and manage server configuration, galaxies, and users.
     */
    @Column(nullable = false)
    private boolean isAdmin = false;

    /**
     * Reference to the PLAYER-type promo code generated for this player.
     * Used as a permanent landing key that routes back to this player's corporation or characters.
     */
    @Column(name = "promo_code_id")
    private Long promoCodeId;

    /**
     * Facebook user ID linked to this player.
     * Used for Facebook login and profile lookup.
     */
    @Column(name = "facebook_profile_id", unique = true, length = 64)
    private String facebookProfileId;

    /**
     * Full Facebook profile URL for this player.
     */
    @Column(name = "facebook_profile_url", length = 500)
    private String facebookProfileUrl;

    /**
     * URL of the player's profile picture from Facebook.
     */
    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    /**
     * Player's location from Facebook profile.
     */
    @Column(length = 200)
    private String location;

    /**
     * Player's bio/about section from Facebook profile.
     */
    @Column(length = 1000)
    private String bio;

    /**
     * Player's birthday from Facebook profile.
     */
    @Column(length = 50)
    private String birthday;

    /**
     * Government types available for an Empire.
     * Based on Stellaris government mechanics.
     */
    public enum GovernmentType {
        DEMOCRACY,
        AUTHORITARIAN,
        MONARCHY,
        OLIGARCHY,
        THEOCRACY,
        DICTATORSHIP,
        CONFEDERATION,
        CORPORATE
    }

    /**
     * Ethos types that define the core values of an Empire.
     * Based on Stellaris ethos system.
     */
    public enum Ethos {
        MATERIALIST,
        SPIRITUALIST,
        LIBERTARIAN,
        EQUALIST,
        MILITARIST,
        PACIFIST
    }

    public Player() {
    }

    public Player(Long id, String email, String loginPin, String empireName,
                  GovernmentType governmentType, Ethos ethos) {
        this.id = id;
        this.email = email;
        this.loginPin = loginPin;
        this.empireName = empireName;
        this.governmentType = governmentType;
        this.ethos = ethos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLoginPin() {
        return loginPin;
    }

    public void setLoginPin(String loginPin) {
        this.loginPin = loginPin;
    }

    public String getEmpireName() {
        return empireName;
    }

    public void setEmpireName(String empireName) {
        this.empireName = empireName;
    }

    public GovernmentType getGovernmentType() {
        return governmentType;
    }

    public void setGovernmentType(GovernmentType governmentType) {
        this.governmentType = governmentType;
    }

    public Ethos getEthos() {
        return ethos;
    }

    public void setEthos(Ethos ethos) {
        this.ethos = ethos;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public Long getPromoCodeId() {
        return promoCodeId;
    }

    public void setPromoCodeId(Long promoCodeId) {
        this.promoCodeId = promoCodeId;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getCorporateRole() { return corporateRole; }
    public void setCorporateRole(String corporateRole) { this.corporateRole = corporateRole; }

    public CorporateRole getCurrentRole() { return currentRole; }
    public void setCurrentRole(CorporateRole currentRole) { this.currentRole = currentRole; }

    public Long getAiAssistantId() { return aiAssistantId; }
    public void setAiAssistantId(Long aiAssistantId) { this.aiAssistantId = aiAssistantId; }

    public String getFacebookProfileId() { return facebookProfileId; }
    public void setFacebookProfileId(String facebookProfileId) { this.facebookProfileId = facebookProfileId; }

    public String getFacebookProfileUrl() { return facebookProfileUrl; }
    public void setFacebookProfileUrl(String facebookProfileUrl) { this.facebookProfileUrl = facebookProfileUrl; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    /**
     * Checks if the player has an active role (not null).
     * Always true since currentRole defaults to CEO, but useful for API responses.
     */
    public boolean isRoleActive() {
        return currentRole != null;
    }

    /** Returns display name, or falls back to first/last name or empire name */
    public String getDisplayName() {
        if (displayName != null && !displayName.isBlank()) {
            return displayName;
        }
        if (firstName != null && !firstName.isBlank()) {
            if (lastName != null && !lastName.isBlank()) {
                return firstName + " " + lastName;
            }
            return firstName;
        }
        return empireName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /** Returns the number of empires owned (always 1 for this model) */
    public int getEmpireCount() {
        return empireName != null ? 1 : 0;
    }

    /** Returns whether this player is owned by a corporation */
    public boolean getOwnedByCorporation() {
        return corporateRole != null && !corporateRole.isBlank();
    }

    /** Adds a skill — no-op placeholder for compatibility */
    public void addSkill(Object skill) {
        // No-op placeholder
    }

    @Override
    public String toString() {
        return "Player{id=" + id + ", email='" + email + "', empire='" + empireName +
                "', government=" + governmentType + ", ethos=" + ethos + ", admin=" + isAdmin +
                ", promoCode=" + promoCodeId + ", currentRole=" + currentRole + ", aiAssistantId=" + aiAssistantId + "}";
    }
}
