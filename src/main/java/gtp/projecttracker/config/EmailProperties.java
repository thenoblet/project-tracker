package gtp.projecttracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.email")
public record EmailProperties(
        String from,
        String replyTo,
        @DefaultValue("") String adminBcc
) {


}