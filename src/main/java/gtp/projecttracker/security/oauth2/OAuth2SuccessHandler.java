package gtp.projecttracker.security.oauth2;

import gtp.projecttracker.model.jpa.User;
import gtp.projecttracker.model.jpa.User.Role;
import gtp.projecttracker.repository.jpa.UserRepository;
import gtp.projecttracker.security.jwt.JwtProvider;

import gtp.projecttracker.security.service.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtProvider jwtProvider;
    private final RedirectStrategy redirectStrategy;
    private final UserRepository userRepository;

    public OAuth2SuccessHandler(JwtProvider jwtProvider, UserRepository userRepository) {
        this.jwtProvider = jwtProvider;
        this.redirectStrategy = new DefaultRedirectStrategy();
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        UserDetails userDetails = convertToUserDetails(authentication.getPrincipal());

        String token = jwtProvider.generateToken(userDetails);

        String targetUrl = UriComponentsBuilder.fromUriString("/api/v1/auth/oauth2/login/success")
                .queryParam("token", token)
                .build().toUriString();

        redirectStrategy.sendRedirect(request, response, targetUrl);
    }

    private UserDetails convertToUserDetails(Object principal) {
        if (principal instanceof OAuth2UserPrincipal) {
            return ((OAuth2UserPrincipal) principal).getUserDetails();
        }

        User user = extractUserFromPrincipal(principal);
        return new UserDetailsImpl(user);
    }

    private User extractUserFromPrincipal(Object principal) {
        if (principal instanceof OidcUser oidcUser) {
            return getOrCreateOAuthUser(oidcUser.getEmail(), oidcUser.getFullName());
        }

        if (principal instanceof OAuth2User oauth2User) {
            return getOrCreateOAuthUser(
                    oauth2User.getAttribute("email"),
                    oauth2User.getAttribute("name")
            );
        }

        throw new IllegalStateException("Unknown principal type: " + principal.getClass());
    }

    private User getOrCreateOAuthUser(String email, String name) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setRole(Role.ROLE_CONTRACTOR);
                    newUser.setOauth2User(true);
                    newUser.setPassword(null);
                    return userRepository.save(newUser);
                });
    }
}
