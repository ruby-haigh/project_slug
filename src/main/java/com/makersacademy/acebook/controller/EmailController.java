package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.service.MonthlyPromptEmailService;
import com.makersacademy.acebook.service.EmailService;
import com.makersacademy.acebook.service.FeedReadyEmailService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailController {

    private final EmailService emailService;
    private final MonthlyPromptEmailService monthlyPromptEmailService;
    private final FeedReadyEmailService feedReadyEmailService;

    public EmailController(EmailService emailService,
                           MonthlyPromptEmailService monthlyPromptEmailService,
                           FeedReadyEmailService feedReadyEmailService) {
        this.emailService = emailService;
        this.monthlyPromptEmailService = monthlyPromptEmailService;
        this.feedReadyEmailService = feedReadyEmailService;
    }

    @GetMapping("/send-test-email")
    public String sendTestEmail(@RequestParam(required = false) String to) {
        if (to == null || to.isBlank()) {
            return "Add ?to=you@example.com to this URL so the test email goes to a real inbox.";
        }

        if (!emailService.hasMailConfiguration()) {
            return "Mail is not configured in the running app yet. Restart the app after setting SMTP_USERNAME, SMTP_PASSWORD, and MAIL_FROM.";
        }

        boolean sent = emailService.sendSimpleEmail(
                new String[]{to},
                "Hello from Spring Boot",
                "This is a test email."
        );

        if (!sent) {
            return "The app tried to send the email, but Gmail rejected it. Check that 2-Step Verification is on, the app password has no spaces, and the app was restarted after setting env vars.";
        }

        return "Test email sent to " + to;
    }

    @GetMapping("/groups/{groupId}/send-prompt-email")
    public String sendPromptEmailForGroup(@PathVariable Long groupId) {
        int recipients = monthlyPromptEmailService.sendPromptEmailsForGroup(groupId);
        if (recipients == 0) {
            return "No prompt emails were sent. This usually means email sending failed or the group has no members with email addresses.";
        }
        return "Prompt emails sent to " + recipients + " group member(s).";
    }

    @GetMapping("/groups/{groupId}/send-feed-email")
    public String sendFeedEmailForGroup(@PathVariable Long groupId,
                                        @RequestParam(defaultValue = "false") boolean force) {
        try {
            int recipients = feedReadyEmailService.sendFeedReadyEmailsForLatestCycle(groupId, force);
            if (recipients == 0) {
                if (force) {
                    return "No feed ready emails were sent, even with force=true. This usually means there are no responses yet, or email sending failed.";
                }

                return "No feed ready emails were sent. This usually means the latest cycle already had its email, there are no responses yet, or email sending failed. Add ?force=true to resend for testing.";
            }

            if (force) {
                return "Feed ready emails force-sent to " + recipients + " group member(s).";
            }

            return "Feed ready emails sent to " + recipients + " group member(s).";
        } catch (RuntimeException error) {
            return error.getMessage();
        }
    }
}
