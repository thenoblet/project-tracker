package gtp.projecttracker.security.config;

import gtp.projecttracker.security.jwt.JwtAuthEntryPoint;
import gtp.projecttracker.security.jwt.JwtAuthFilter;
import gtp.projecttracker.security.oauth2.CustomOAuth2UserService;
import gtp.projecttracker.security.oauth2.OAuth2SuccessHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Main security configuration class for the application.
 * Configures authentication, authorization, and security filters for both JWT and OAuth2 authentication.
 *
 * <p>This class enables web security, method-level security, and configures:</p>
 * <ul>
 *   <li>JWT authentication filter</li>
 *   <li>OAuth2 login flow</li>
 *   <li>Password encoding</li>
 *   <li>Authentication manager</li>
 *   <li>CSRF protection</li>
 *   <li>CORS configuration</li>
 *   <li>Session management (stateless)</li>
 *   <li>Exception handling for authentication failures</li>
 * </ul>
 *
 * @see EnableWebSecurity
 * @see EnableMethodSecurity
 * @see SecurityFilterChain
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    OAuth2SuccessHandler oauth2SuccessHandler;
    JwtAuthEntryPoint jwtAuthEntryPoint;
    JwtAuthFilter  jwtAuthFilter;

    public SecurityConfig(OAuth2SuccessHandler oauth2SuccessHandler, JwtAuthEntryPoint jwtAuthEntryPoint,  JwtAuthFilter jwtAuthFilter) {
        this.oauth2SuccessHandler = oauth2SuccessHandler;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Configures the security filter chain with HTTP security settings.
     *
     * <p>This configuration:</p>
     * <ul>
     *   <li>Disables CSRF protection (stateless API)</li>
     *   <li>Enables CORS with default settings</li>
     *   <li>Sets up request authorization rules</li>
     *   <li>Configures OAuth2 login with custom user service and handlers</li>
     *   <li>Sets exception handling for authentication failures</li>
     *   <li>Configures stateless session management</li>
     *   <li>Adds JWT authentication filter</li>
     * </ul>
     *
     * @param http the HttpSecurity to configure
     * @param customOAuth2UserService custom service for OAuth2 user processing
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",
                                "/api/v1/auth/**",
                                "/oauth2/**",
                                "/login/oauth2/code/**",
                                "/auth/oauth2/login/**",
                                "/v3/api-docs.yaml",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("audit-logs/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/tasks/**").authenticated()
                        .requestMatchers("/api/v1/users/me").authenticated()
                        .requestMatchers("/api/v1/users/admin/**").hasRole("ADMIN")
                        .requestMatchers("/projects/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(user -> user
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oauth2SuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            log.error("OAuth2 authentication failed", exception);
                            response.sendRedirect("/api/v1/auth/oauth2/failure?error=" +
                                    URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8));
                        })
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthEntryPoint)
                )
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Provides a password encoder bean for hashing and verifying passwords.
     * Uses BCrypt hashing algorithm with default strength.
     *
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Provides an AuthenticationManager bean configured with the authentication configuration.
     * The AuthenticationManager is used to authenticate users with their credentials.
     *
     * @param config the AuthenticationConfiguration to get the manager from
     * @return configured AuthenticationManager
     * @throws Exception if authentication manager cannot be created
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

