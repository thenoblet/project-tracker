package gtp.projecttracker.security.jwt;

import gtp.projecttracker.model.jpa.User;
import gtp.projecttracker.security.config.JwtConfig;
import gtp.projecttracker.security.service.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtProvider {
    private static final Logger log = LoggerFactory.getLogger(JwtProvider.class);

    private final JwtConfig jwtConfig;
    private Key signingKey;
    private JwtParser jwtParser;

    public JwtProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
        this.jwtParser = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build();
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
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            jwtParser.parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public long getExpirationDuration() {
        return jwtConfig.getExpirationMs();
    }

    public String getUsernameFromJwt(String jwt) {
        return getClaimFromJwt(jwt, Claims::getSubject);
    }

    public String getEmailFromJwt(String jwt) {
        return getClaimFromJwt(jwt, Claims::getSubject);
    }

    public String getClaimFromJwt(String jwt, String claimName) {
        return getClaimFromJwt(jwt, claims -> claims.get(claimName, String.class));
    }

    public Date getExpirationDateFromJwt(String jwt) {
        return getClaimFromJwt(jwt, Claims::getExpiration);
    }

    private <T> T getClaimFromJwt(String jwt, ClaimsResolver<T> resolver) {
        try {
            Claims claims = jwtParser.parseClaimsJws(jwt).getBody();
            return resolver.resolve(claims);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error extracting claim from JWT: {}", e.getMessage());
            return null;
        }
    }

    @FunctionalInterface
    private interface ClaimsResolver<T> {
        T resolve(Claims claims);
    }
}