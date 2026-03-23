package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.service.MonthlyPromptEmailService;
import com.makersacademy.acebook.service.EmailService;
import com.makersacademy.acebook.service.FeedReadyEmailService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
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
    public String sendTestEmail() {
        String[] recipients = {
                "test1@example.com",
                "test2@example.com",
                "test3@example.com"
        };

        emailService.sendSimpleEmail(
                recipients,
                "Hello from Spring Boot",
                "This is a test email."
        );

        return "Email sent!";
    }

    @GetMapping("/groups/{groupId}/send-prompt-email")
    public String sendPromptEmailForGroup(@PathVariable Long groupId) {
        int recipients = monthlyPromptEmailService.sendPromptEmailsForGroup(groupId);
        return "Prompt emails sent to " + recipients + " group member(s).";
    }

    @GetMapping("/groups/{groupId}/send-feed-email")
    public String sendFeedEmailForGroup(@PathVariable Long groupId) {
        try {
            int recipients = feedReadyEmailService.sendFeedReadyEmailsForLatestCycle(groupId);
            if (recipients == 0) {
                return "No feed ready emails were sent yet. This group may not have any responses in its latest cycle.";
            }

            return "Feed ready emails sent to " + recipients + " group member(s).";
        } catch (RuntimeException error) {
            return error.getMessage();
        }
    }
}
