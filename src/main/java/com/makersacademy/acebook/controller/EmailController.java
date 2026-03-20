package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.service.MonthlyPromptEmailService;
import com.makersacademy.acebook.service.EmailService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailController {

    private final EmailService emailService;
    private final MonthlyPromptEmailService monthlyPromptEmailService;

    public EmailController(EmailService emailService, MonthlyPromptEmailService monthlyPromptEmailService) {
        this.emailService = emailService;
        this.monthlyPromptEmailService = monthlyPromptEmailService;
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
}
