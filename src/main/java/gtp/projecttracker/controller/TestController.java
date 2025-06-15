package gtp.projecttracker.controller;

import gtp.projecttracker.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST controller for testing email functionality.
 * Provides endpoints for sending test emails with template parameters.
 * Used primarily for development and testing purposes.
 * All endpoints are accessible under the /api/v1/email path.
 */
@RestController
@RequestMapping("api/v1/email")
public class TestController {

    private final EmailService emailService;

    /**
     * Constructs a TestController with the required email service dependency.
     *
     * @param emailService The service for sending emails
     */
    public TestController(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Sends a test email with predefined template parameters.
     * If no recipient is provided, a default email address is used.
     * The email uses the "task-overdue" template with sample task data.
     *
     * @param recipient Optional email recipient address
     * @param templateParams Optional additional template parameters (not currently used)
     * @return A response indicating the success or failure of the email sending operation
     */
    @PostMapping("/send")
    public ResponseEntity<String> sendTestEmail(
            @RequestParam(required = false) String recipient,
            @RequestBody(required = false) Map<String, Object> templateParams
    ) {
        try {
            // Create a very simple, explicit Map
            Map<String, Object> testData = new LinkedHashMap<>();
            testData.put("assigneeName", "John Doe");
            testData.put("daysOverdue", Integer.valueOf(3));
            testData.put("taskTitle", "Fix Bug #123");
            testData.put("projectName", "Web Application");
            testData.put("dueDate", LocalDate.of(2025, 6, 4)); // Specific date

            System.out.println("=== CONTROLLER DEBUG ===");
            System.out.println("Test data: " + testData);
            testData.forEach((k, v) ->
                    System.out.println(k + " = " + v + " (type: " + v.getClass().getSimpleName() + ")")
            );
            System.out.println("=== END CONTROLLER DEBUG ===");

            emailService.sendEmailWithTemplate(
                    recipient != null ? recipient : "patricknobletappiah@gmail.com",
                    "Task Overdue - Test Email",
                    "task-overdue",
                    testData
            );

            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            System.err.println("Controller error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send email: " + e.getMessage());
        }
    }
}
