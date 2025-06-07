package gtp.projecttracker.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);

    void sendEmailWithTemplate(String to, String subject, String templateName, Object context);
}