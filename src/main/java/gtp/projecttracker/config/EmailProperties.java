package gtp.projecttracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration properties for email functionality.
 * Binds properties with the prefix "app.email" from the application configuration.
 * Contains settings for email sender, reply-to address, and admin BCC.
 */
@ConfigurationProperties(prefix = "app.email")
public record EmailProperties(
        String from,
        String replyTo,
        @DefaultValue("") String adminBcc
) {
}
