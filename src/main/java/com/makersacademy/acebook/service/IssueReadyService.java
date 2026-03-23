package com.makersacademy.acebook.service;

import com.makersacademy.acebook.model.GroupCycle;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class IssueReadyService {

    public Map<String, Object> getIssueInfo(GroupCycle cycle) {
        Map<String, Object> data = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextIssue = cycle.getCycleStart().plusWeeks(1);

        boolean issueReady = now.isAfter(nextIssue);

        long daysUntilNextIssue = Duration.between(now, nextIssue).toDays();
        if (daysUntilNextIssue < 0) daysUntilNextIssue = 0;

        String monthName = nextIssue.getMonth()
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        data.put("issueReady", issueReady);
        data.put("daysUntilNextIssue", daysUntilNextIssue);
        data.put("monthName", monthName);

        return data;
    }
}
