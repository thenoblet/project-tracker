package gtp.projecttracker.security.config;

import gtp.projecttracker.security.jwt.JwtAuthEntryPoint;
import gtp.projecttracker.security.jwt.JwtAuthFilter;
import gtp.projecttracker.security.jwt.JwtProvider;
import gtp.projecttracker.security.oauth2.CustomOAuth2UserService;
import gtp.projecttracker.security.oauth2.OAuth2SuccessHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.springframework.security.config.Customizer.withDefaults;

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
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("audit-logs/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/tasks/**").authenticated()
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

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

