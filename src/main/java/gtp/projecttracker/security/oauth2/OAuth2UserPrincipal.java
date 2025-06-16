package gtp.projecttracker.security.oauth2;

import gtp.projecttracker.model.jpa.User;

import gtp.projecttracker.security.service.UserDetailsImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

/**
 * Custom principal that combines:
 * - Spring Security's OAuth2User (for social login data)
 * - Your User entity (for role/permissions)
 * - JWT compatibility
 */
public class OAuth2UserPrincipal implements OAuth2User, OidcUser {
    private final UserDetailsImpl userDetails;
    private final Map<String, Object> attributes;
    private OidcIdToken idToken;

    public OAuth2UserPrincipal(User user, Map<String, Object> attributes) {
        this.userDetails = new UserDetailsImpl(user);
        this.attributes = attributes;
    }

    public OAuth2UserPrincipal(User user, OidcUser oidcUser) {
        this.userDetails = new  UserDetailsImpl(user);
        this.attributes = oidcUser.getAttributes();
        this.idToken = oidcUser.getIdToken();
    }

    @Override
    public String getName() {
        return userDetails.getUsername();
    }

    public boolean isOauth2User() {
        return userDetails.getUser().isOauth2User();
    }

    public UUID getUserId() {
        return userDetails.getUser().getId();
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return null; // Return null if not using user info
    }

    @Override
    public Map<String, Object> getClaims() {
        return attributes;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userDetails.getAuthorities();
    }

    public User getUser() {
        return userDetails.getUser();
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }
}
