package com.makersacademy.acebook.scheduler;

import com.makersacademy.acebook.model.Group;
import com.makersacademy.acebook.repository.GroupRepository;
import com.makersacademy.acebook.service.GroupEmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NewsletterScheduler {

    private final GroupEmailService groupEmailService;
    private final GroupRepository groupRepository;

    public NewsletterScheduler(GroupEmailService groupEmailService,
                               GroupRepository groupRepository) {
        this.groupEmailService = groupEmailService;
        this.groupRepository = groupRepository;
    }

    /**
     * Run on the 1st of every month at 9 AM
     */
    @Scheduled(cron = "0 18 15 19 * *")
    public void sendMonthlyNewsletters() {
        List<Group> groups = groupRepository.findAll();
        for (Group g : groups) {
            groupEmailService.sendGroupNewsletter(g.getId());
        }
    }
}