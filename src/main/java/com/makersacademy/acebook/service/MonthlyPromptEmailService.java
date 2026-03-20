package com.makersacademy.acebook.service;

import com.makersacademy.acebook.model.Group;
import com.makersacademy.acebook.model.GroupMembership;
import com.makersacademy.acebook.repository.GroupMembershipRepository;
import com.makersacademy.acebook.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MonthlyPromptEmailService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMembershipRepository groupMembershipRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Scheduled(cron = "${prompt.email.cron:0 0 9 * * *}")
    public void sendDuePromptEmails() {
        LocalDate today = LocalDate.now();

        for (Group group : groupRepository.findAll()) {
            if (group.getCreatedAt() == null) {
                continue;
            }

            int scheduledDay = Math.min(group.getCreatedAt().getDayOfMonth(), today.lengthOfMonth());
            if (today.getDayOfMonth() != scheduledDay) {
                continue;
            }

            sendPromptEmailsForGroup(group);
        }
    }

    public int sendPromptEmailsForGroup(Long groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow();
        return sendPromptEmailsForGroup(group);
    }

    private int sendPromptEmailsForGroup(Group group) {
        List<GroupMembership> memberships = groupMembershipRepository.findByGroup(group);
        String promptFormLink = appBaseUrl + "/groups/" + group.getId() + "/prompts/open";

        int recipients = 0;
        for (GroupMembership membership : memberships) {
            if (membership.getUser() == null || membership.getUser().getEmail() == null) {
                continue;
            }

            emailService.sendPromptFormEmail(
                    membership.getUser().getEmail(),
                    group.getName(),
                    promptFormLink
            );
            recipients++;
        }

        return recipients;
    }
}
