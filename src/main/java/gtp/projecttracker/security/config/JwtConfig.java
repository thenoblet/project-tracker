package gtp.projecttracker.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {
    private String secret;
    private long expirationMs;
    private long refreshExpirationMs;

    public byte[] getSecretBytes() {
        return Base64.getEncoder().encodeToString(secret.getBytes(StandardCharsets.UTF_8)).getBytes();
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    public void setRefreshExpirationMs(long refreshExpirationMs) {
        this.refreshExpirationMs = refreshExpirationMs;
    }
}
