package com.makersacademy.acebook.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    // Constructor injection (recommended)
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Send HTML invite email
    public void sendInvite(String toEmail, String groupName, String inviteLink) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("You're invited to join " + groupName);

            // HTML content
            String htmlMsg = "<p>Hi!</p>"
                    + "<p>You've been invited to join the group '<strong>" + groupName + "</strong>'.</p>"
                    + "<p>Click <a href='" + inviteLink + "'>here</a> to join the group.</p>"
                    + "<p>See you there!</p>";

            helper.setText(htmlMsg, true); // true = HTML
            mailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendPromptFormEmail(String toEmail, String groupName, String frequencyLabel, String promptFormLink) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Your " + frequencyLabel + " prompts for " + groupName);

            String htmlMsg = "<p>Hi!</p>"
                    + "<p>It’s time to fill in your " + frequencyLabel + " update for '<strong>" + groupName + "</strong>'.</p>"
                    + "<p>Click <a href='" + promptFormLink + "'>here</a> to open your form.</p>"
                    + "<p>You can also open the form from the app.</p>";

            helper.setText(htmlMsg, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    // Send simple plain text email (general purpose)
    public void sendSimpleEmail(String[] to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("🐌 " + subject);
        message.setText(body);
        message.setFrom("snailmail@slug.co"); // arbitrary, works for MailHog/Mailtrap
        mailSender.send(message);
    }
}
