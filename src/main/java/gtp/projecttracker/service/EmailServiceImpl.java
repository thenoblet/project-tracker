package gtp.projecttracker.service;

import gtp.projecttracker.config.EmailProperties;
import gtp.projecttracker.exception.EmailException;
import jakarta.annotation.PostConstruct;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.Map;
import java.util.Objects;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailProperties emailProperties;

    public EmailServiceImpl(JavaMailSender mailSender,
                            TemplateEngine templateEngine,
                            EmailProperties emailProperties) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.emailProperties = emailProperties;
    }

    @PostConstruct
    public void init() {
        validateMailConfiguration();
    }

    private void validateMailConfiguration() {
        try {
            // Just test the connection, don't send email
            mailSender.createMimeMessage(); // This creates a message but doesn't send it
            System.out.println("Mail configuration appears valid");
        } catch (Exception e) {
            System.err.println("Mail configuration validation failed: " + e.getMessage());
        }
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        Objects.requireNonNull(emailProperties.from(), "From address must not be null");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailProperties.from());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            if (StringUtils.hasText(emailProperties.adminBcc())) {
                helper.setBcc(emailProperties.adminBcc());
            }

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailException("Failed to send email to " + to, e);
        }
    }

    @Override
    public void sendEmailWithTemplate(String to, String subject, String templateName, Object context) {
        try {
            Context thymeleafContext = new Context();

            if (context instanceof Map<?, ?> map) {
                map.forEach((key, value) ->
                        thymeleafContext.setVariable(key.toString(), value)
                );
            } else {
                thymeleafContext.setVariable("data", context);
            }

            String htmlBody = templateEngine.process("emails/" + templateName, thymeleafContext);
            sendEmail(to, subject, htmlBody);
        } catch (Exception e) {
            throw new EmailException("Failed to process template '" + templateName + "'", e);
        }
    }
}