package gtp.projecttracker.model.email;

/**
 * Model class representing email details in the project tracking system.
 *
 * This class encapsulates all the information needed to send an email,
 * including the recipient's address, the message body, the subject line,
 * and optionally a template name for rendering the email content.
 * It is used by the email service to send notifications to users.
 */
public class EmailDetails {

    private String recipient;
    private String msgBody;
    private String subject;
    private String template;

    /**
     * Default constructor for EmailDetails.
     * Creates an empty email details object that can be populated using setters.
     */
    public EmailDetails() {
    }

    /**
     * Constructor for creating an email without a template.
     *
     * @param recipient The email address of the recipient
     * @param msgBody The content of the email message
     * @param subject The subject line of the email
     */
    public EmailDetails(String recipient, String msgBody, String subject) {
        this.recipient = recipient;
        this.msgBody = msgBody;
        this.subject = subject;
    }

    /**
     * Constructor for creating an email with a template.
     *
     * @param recipient The email address of the recipient
     * @param msgBody The content of the email message (may contain variables for template)
     * @param subject The subject line of the email
     * @param template The name of the template to use for rendering the email
     */
    public EmailDetails(String recipient, String msgBody, String subject, String template) {
        this.recipient = recipient;
        this.msgBody = msgBody;
        this.subject = subject;
        this.template = template;
    }

    /**
     * Gets the email address of the recipient.
     *
     * @return The recipient's email address
     */
    public String getRecipient() {
        return recipient;
    }

    /**
     * Sets the email address of the recipient.
     *
     * @param recipient The recipient's email address to set
     */
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    /**
     * Gets the content of the email message.
     *
     * @return The email message body
     */
    public String getMsgBody() {
        return msgBody;
    }

    /**
     * Sets the content of the email message.
     *
     * @param msgBody The email message body to set
     */
    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }

    /**
     * Gets the subject line of the email.
     *
     * @return The email subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the subject line of the email.
     *
     * @param subject The email subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Gets the name of the template used for rendering the email.
     *
     * @return The template name, or null if no template is used
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Sets the name of the template to use for rendering the email.
     *
     * @param template The template name to set
     */
    public void setTemplate(String template) {
        this.template = template;
    }

    /**
     * Returns a string representation of the email details.
     * The string includes the recipient, message body, and subject.
     *
     * @return A string representation of this object
     */
    @Override
    public String toString() {
        return "EmailDetails{" +
                "recipient='" + recipient + '\'' +
                ", msgBody='" + msgBody + '\'' +
                ", subject='" + subject + '\'' +
                '}';
    }
}
