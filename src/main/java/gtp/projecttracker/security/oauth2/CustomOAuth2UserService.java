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

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final OidcUserService oidcUserService = new OidcUserService();

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        if (request instanceof OidcUserRequest) {
            return loadOidcUser((OidcUserRequest) request);
        }
        return loadOAuth2User(request);
    }

    private OAuth2UserPrincipal loadOidcUser(OidcUserRequest request) {
        OidcUser oidcUser = oidcUserService.loadUser(request);
        return convertToPrincipal(oidcUser.getEmail(), oidcUser.getAttributes());
    }

    private OAuth2UserPrincipal loadOAuth2User(OAuth2UserRequest request) {
        OAuth2User oauth2User = super.loadUser(request);
        return convertToPrincipal(oauth2User.getAttribute("email"), oauth2User.getAttributes());
    }

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
