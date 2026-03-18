package com.makersacademy.acebook.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendInvite(String toEmail, String groupName, String inviteLink) {

        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("You're invited to join " + groupName);

            // HTML email content
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
}