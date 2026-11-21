package com.yisy.modules.doramcu.facebook.dto;
import com.yisy.modules.doramcu.model.players.Player;

/**
 * Data transfer object for Facebook profile data fetched from the Graph API.
 * <p>
 * Contains all public profile fields that can be mapped to a Player entity:
 * basic name fields, email (if public), location, bio, birthday, and profile picture URL.
 * </p>
 */
public class FacebookProfileData {

    private String facebookId;
    private String firstName;
    private String lastName;
    private String displayName;
    private String email;
    private String location;
    private String bio;
    private String birthday;
    private String profilePictureUrl;
    private String profileUrl;

    public FacebookProfileData() {
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }
}
