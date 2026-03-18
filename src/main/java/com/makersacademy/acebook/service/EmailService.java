package com.makersacademy.acebook.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendInvite(String toEmail, String groupName, String inviteLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("You're invited to join " + groupName);
        message.setText("Hi!\n\nYou've been invited to join the group '" + groupName + "'. " +
                "Click this link to join: " + inviteLink + "\n\nSee you there!");
        mailSender.send(message);
    }
}
