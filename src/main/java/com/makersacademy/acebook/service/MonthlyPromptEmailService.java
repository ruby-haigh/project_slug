package com.makersacademy.acebook.service;

import com.makersacademy.acebook.model.Group;
import com.makersacademy.acebook.model.GroupCycle;
import com.makersacademy.acebook.model.GroupMembership;
import com.makersacademy.acebook.repository.GroupCycleRepository;
import com.makersacademy.acebook.repository.GroupMembershipRepository;
import com.makersacademy.acebook.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class MonthlyPromptEmailService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupCycleRepository groupCycleRepository;

    @Autowired
    private GroupMembershipRepository groupMembershipRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Scheduled(cron = "${prompt.email.cron:0 0 9 * * *}") //the schedule is “when to check,” not automatically “when to send everyone an email.”
    //second: 0, minute: 0, hour: 9, day of month: * = every day, month: * = every month, day of week: * = every day of week
    public void sendDuePromptEmails() {
        LocalDate today = LocalDate.now();

        for (Group group : groupRepository.findAll()) {
            if (group.getCreatedAt() == null) {
                continue;
            }

            LocalDate createdDate = group.getCreatedAt().toLocalDate();
            String frequency = group.getFrequency();

            if (frequency != null && frequency.equalsIgnoreCase("FORTNIGHTLY")) {
                long daysSinceCreated = ChronoUnit.DAYS.between(createdDate, today);

                if (daysSinceCreated < 0) {
                    continue;
                }

                if (daysSinceCreated % 14 != 0) {
                    continue;
                }
            } else {
                int createdDayOfMonth = createdDate.getDayOfMonth();
                int todayDayOfMonth = today.getDayOfMonth();
                int sendDayThisMonth = Math.min(createdDayOfMonth, today.lengthOfMonth());

                if (todayDayOfMonth != sendDayThisMonth) {
                    continue;
                }
            }

            sendPromptEmailsForGroup(group);
        }
    }

    public int sendPromptEmailsForGroup(Long groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow();

        LocalDateTime now = LocalDateTime.now();
        if (groupCycleRepository.findCurrentCycleByGroupId(groupId, now).isEmpty()) {
            long cycleLengthDays;
            if (group.getFrequency() != null && group.getFrequency().equalsIgnoreCase("FORTNIGHTLY")) {
                cycleLengthDays = 14;
            } else {
                cycleLengthDays = 30;
            }

            GroupCycle cycle = new GroupCycle(groupId, now, now.plusDays(cycleLengthDays));
            groupCycleRepository.save(cycle);
        }

        List<GroupMembership> memberships = groupMembershipRepository.findByGroup(group);
        String promptFormLink = appBaseUrl + "/groups/" + group.getId() + "/prompts/open";

        String frequencyLabel;
        if (group.getFrequency() != null && group.getFrequency().equalsIgnoreCase("FORTNIGHTLY")) {
            frequencyLabel = "fortnightly";
        } else {
            frequencyLabel = "monthly";
        }

        int recipients = 0;
        for (GroupMembership membership : memberships) {
            if (membership.getUser() == null) {
                continue;
            }

            String emailAddress = membership.getUser().getEmail();
            if (emailAddress == null || emailAddress.isBlank()) {
                continue;
            }

            emailService.sendPromptFormEmail(
                    emailAddress,
                    group.getName(),
                    frequencyLabel,
                    promptFormLink
            );
            recipients++;
        }

        return recipients;
    }

    private void sendPromptEmailsForGroup(Group group) {
        sendPromptEmailsForGroup(group.getId());
    }
}
