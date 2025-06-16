package gtp.projecttracker.security.jwt;

import gtp.projecttracker.model.jpa.User;
import gtp.projecttracker.security.config.JwtConfig;

import gtp.projecttracker.security.service.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtProvider {
    private static final Logger log = LoggerFactory.getLogger(JwtProvider.class);

    private final JwtConfig jwtConfig;

    public JwtProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public String generateToken(UserDetails userDetails) {
        UserDetailsImpl user = (UserDetailsImpl) userDetails;

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getUsername());
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpirationMs()))
                .signWith(
                        SignatureAlgorithm.HS512,
                        jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8)
                )
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8)) // Explicit encoding
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.printf("Error extracting email from JWT: {%s}", e.getMessage()); // Use proper logging
            return false;
        }
    }

    public long getExpirationDuration() {
        return jwtConfig.getExpirationMs();
    }

    public String getUsernameFromJwt(String jwt) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtConfig.getSecret())
                    .parseClaimsJws(jwt)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            System.out.printf("Error extracting username from JWT: {%s}", e.getMessage());
            return null;
        }
    }

    public String getEmailFromJwt(String jwt) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();

            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            System.out.printf("Error extracting email from JWT: {%s}", e.getMessage());
            return null;
        }
    }

    public String getClaimFromJwt(String jwt, String claimName) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
            return claims.get(claimName, String.class);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error extracting claim '{}' from JWT: {}", claimName, e.getMessage());
            return null;
        }
    }

    public Date getExpirationDateFromJwt(String jwt) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
            return claims.getExpiration();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error extracting expiration date from JWT: {}", e.getMessage());
            return null;
        }
    }
}
