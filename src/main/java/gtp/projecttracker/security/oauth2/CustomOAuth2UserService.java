package gtp.projecttracker.security.oauth2;

import gtp.projecttracker.model.jpa.User;
import gtp.projecttracker.model.jpa.User.Role;
import gtp.projecttracker.repository.jpa.UserRepository;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Custom implementation of OAuth2 user service that handles both OIDC and standard OAuth2 user authentication.
 *
 * <p>This service extends {@link DefaultOAuth2UserService} to provide custom user loading and registration
 * functionality for OAuth2 authentication flows. It handles:</p>
 * <ul>
 *   <li>Loading user details from OAuth2 providers</li>
 *   <li>Converting provider-specific user attributes to application user principal</li>
 *   <li>Automatically registering new users on first login</li>
 *   <li>Supporting both OIDC (OpenID Connect) and standard OAuth2 providers</li>
 * </ul>
 *
 * <p>New users are automatically registered with {@link Role#ROLE_CONTRACTOR} role by default.</p>
 *
 * @see DefaultOAuth2UserService
 * @see OidcUserService
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final OidcUserService oidcUserService = new OidcUserService();

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads user information from the OAuth2 provider.
     *
     * <p>This method determines whether the request is an OIDC request and delegates to the appropriate
     * handler method. Supports both OpenID Connect and standard OAuth2 providers.</p>
     *
     * @param request the OAuth2 user request containing access token and client information
     * @return OAuth2UserPrincipal containing both user details and OAuth2 attributes
     * @throws OAuth2AuthenticationException if user loading fails
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        if (request instanceof OidcUserRequest) {
            return loadOidcUser((OidcUserRequest) request);
        }
        return loadOAuth2User(request);
    }

    /**
     * Handles loading of OpenID Connect (OIDC) user information.
     *
     * @param request the OIDC user request
     * @return OAuth2UserPrincipal containing user details and OIDC attributes
     */
    private OAuth2UserPrincipal loadOidcUser(OidcUserRequest request) {
        OidcUser oidcUser = oidcUserService.loadUser(request);
        return convertToPrincipal(oidcUser.getEmail(), oidcUser.getAttributes());
    }

    private OAuth2UserPrincipal loadOAuth2User(OAuth2UserRequest request) {
        OAuth2User oauth2User = super.loadUser(request);
        return convertToPrincipal(oauth2User.getAttribute("email"), oauth2User.getAttributes());
    }

    /**
     * Converts OAuth2 provider attributes to application user principal.
     *
     * <p>If the user doesn't exist in the local database, a new user record is automatically created.</p>
     *
     * @param email the user's email address from the OAuth2 provider
     * @param attributes the complete set of attributes from the OAuth2 provider
     * @return OAuth2UserPrincipal combining local user details and OAuth2 attributes
     */
    private OAuth2UserPrincipal convertToPrincipal(String email, Map<String, Object> attributes) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> registerNewUser(attributes));
        return new OAuth2UserPrincipal(user, attributes);
    }

    private User registerNewUser(Map<String, Object> attributes) {
        User newUser = new User();
        newUser.setEmail((String) attributes.get("email"));
        newUser.setName((String) attributes.get("name"));
        newUser.setRole(Role.ROLE_CONTRACTOR);
        return userRepository.save(newUser);
    }
}
