package com.makersacademy.acebook.service;

import com.makersacademy.acebook.model.Group;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.GroupRepository;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.util.List;

@Service
public class GroupEmailService {
    private final EmailService emailService;
    private final GroupRepository groupRepository;


    public GroupEmailService(EmailService emailService, GroupRepository groupRepository) {
        this.emailService = emailService;
        this.groupRepository = groupRepository;
    }

    public void sendGroupNewsletter(Long groupId) {
        Group group = groupRepository.findByIdWithMembers(groupId);

        String body = "Your monthly updates are ready! Click the link below to see the latest responses from your circle:\n\n" +
                "http://localhost:8080/feed/" + group.getId() + "\n\n" +
                "Happy Slugging!";


        String[] recipients = group.getMembers()
                .stream()
                .map(User::getEmail)
                .toArray(String[]::new);


        emailService.sendSimpleEmail(recipients, "Your Monthly Newsletter", body.toString());
    }

}
