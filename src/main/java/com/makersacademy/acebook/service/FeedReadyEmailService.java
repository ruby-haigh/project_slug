package com.makersacademy.acebook.service;

import com.makersacademy.acebook.model.Group;
import com.makersacademy.acebook.model.GroupCycle;
import com.makersacademy.acebook.model.GroupMembership;
import com.makersacademy.acebook.repository.GroupCycleRepository;
import com.makersacademy.acebook.repository.GroupMembershipRepository;
import com.makersacademy.acebook.repository.GroupRepository;
import com.makersacademy.acebook.repository.GroupResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedReadyEmailService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupCycleRepository groupCycleRepository;

    @Autowired
    private GroupMembershipRepository groupMembershipRepository;

    @Autowired
    private GroupResponseRepository groupResponseRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Scheduled(cron = "${feed.email.cron:0 30 9 * * *}")
    public void sendDueFeedEmails() {
        LocalDate today = LocalDate.now();

        for (GroupCycle cycle : groupCycleRepository.findAll()) {
            if (cycle.getFeedEmailSentAt() != null) {
                continue;
            }

            LocalDate readyDate = cycle.getCycleStart().toLocalDate().plusDays(7);
            if (readyDate.isAfter(today)) {
                continue;
            }

            if (!groupResponseRepository.existsByGroupCycleId(cycle.getId())) {
                continue;
            }

            sendFeedReadyEmailsForCycle(cycle.getId());
        }
    }

    public int sendFeedReadyEmailsForLatestCycle(Long groupId) {
        return sendFeedReadyEmailsForLatestCycle(groupId, false);
    }

    public int sendFeedReadyEmailsForLatestCycle(Long groupId, boolean force) {
        GroupCycle cycle = groupCycleRepository.findTopByGroupIdOrderByCycleStartDesc(groupId)
                .orElseThrow(() -> new RuntimeException("No cycle found for this group yet."));
        return sendFeedReadyEmailsForCycle(cycle.getId(), force);
    }

    public int sendFeedReadyEmailsForCycle(Long cycleId) {
        return sendFeedReadyEmailsForCycle(cycleId, false);
    }

    public int sendFeedReadyEmailsForCycle(Long cycleId, boolean force) {
        GroupCycle cycle = groupCycleRepository.findById(cycleId).orElseThrow();
        Group group = groupRepository.findById(cycle.getGroupId()).orElseThrow();

        if (!groupResponseRepository.existsByGroupCycleId(cycle.getId())) {
            return 0;
        }

        if (!force && cycle.getFeedEmailSentAt() != null) {
            return 0;
        }

        List<GroupMembership> memberships = groupMembershipRepository.findByGroup(group);

        String frequencyLabel;
        if (group.getFrequency() != null && group.getFrequency().equalsIgnoreCase("FORTNIGHTLY")) {
            frequencyLabel = "fortnightly";
        } else {
            frequencyLabel = "monthly";
        }

        String feedLink = appBaseUrl + "/feed/" + group.getId() + "?cycleId=" + cycle.getId();

        int recipients = 0;
        for (GroupMembership membership : memberships) {
            if (membership.getUser() == null) {
                continue;
            }

            String emailAddress = membership.getUser().getEmail();
            if (emailAddress == null || emailAddress.isBlank()) {
                continue;
            }

            boolean sent = emailService.sendFeedReadyEmail(
                    emailAddress,
                    group.getName(),
                    frequencyLabel,
                    feedLink
            );
            if (sent) {
                recipients++;
            }
        }

        if (recipients > 0) {
            cycle.setFeedEmailSentAt(LocalDateTime.now());
            groupCycleRepository.save(cycle);
        }

        return recipients;
    }
}
