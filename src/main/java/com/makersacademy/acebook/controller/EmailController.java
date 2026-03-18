package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.service.EmailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
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
}