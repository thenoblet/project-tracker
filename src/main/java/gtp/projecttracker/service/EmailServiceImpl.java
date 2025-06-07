package gtp.projecttracker.service;

import gtp.projecttracker.config.EmailProperties;
import gtp.projecttracker.exception.EmailException;
import jakarta.annotation.PostConstruct;
import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
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
            // Create test message
            MimeMessage message = mailSender.createMimeMessage();
            message.setFrom(new InternetAddress(emailProperties.from()));
            message.setRecipient(Message.RecipientType.TO,
                    new InternetAddress(emailProperties.from()));
            message.setSubject("Connection Test");
            message.setText("This is a test message");

            // Try to send (will fail fast if connection problems)
            mailSender.send(message);
            System.out.println("SMTP connection test successful");
        } catch (Exception e) {
            throw new IllegalStateException("SMTP connection test failed", e);
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

            // Improved context handling
            if (context instanceof Map<?, ?> map) {
                map.forEach(thymeleafContext::setVariable);
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