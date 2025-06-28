package gtp.projecttracker.security.jwt;

import gtp.projecttracker.security.service.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final WebAuthenticationDetailsSource authDetailsSource;

    // Initialize all reusable objects
    public JwtAuthFilter(JwtProvider jwtProvider,
                         UserDetailsServiceImpl userDetailsService) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
        this.authDetailsSource = new WebAuthenticationDetailsSource();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt == null) {
                filterChain.doFilter(request, response);
                return;
            }

            if (!jwtProvider.validateToken(jwt)) {
                sendError(response, "Invalid token");
                return;
            }

            String email = jwtProvider.getEmailFromJwt(jwt);
            if (email == null || email.isEmpty()) {
                sendError(response, "Missing email in token");
                return;
            }

            authenticateUser(request, email);
        } catch (Exception e) {
            log.error("Authentication error", e);
            sendError(response, e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(HttpServletRequest request, String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

        authentication.setDetails(authDetailsSource.buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Authenticated user: {}", email);
    }

    private String parseJwt(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        log.error(message);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
    }
}