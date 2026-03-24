package com.makersacademy.acebook.service;

import com.makersacademy.acebook.model.GroupCycle;
import com.makersacademy.acebook.model.GroupMembership;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.GroupCycleRepository;
import com.makersacademy.acebook.repository.GroupMembershipRepository;
import com.makersacademy.acebook.repository.GroupResponseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class UserProfileStatsService {

    private final GroupMembershipRepository groupMembershipRepository;
    private final GroupCycleRepository groupCycleRepository;
    private final GroupResponseRepository groupResponseRepository;

    public UserProfileStatsService(GroupMembershipRepository groupMembershipRepository,
                                   GroupCycleRepository groupCycleRepository,
                                   GroupResponseRepository groupResponseRepository) {
        this.groupMembershipRepository = groupMembershipRepository;
        this.groupCycleRepository = groupCycleRepository;
        this.groupResponseRepository = groupResponseRepository;
    }

    public ProfileStats buildStats(User user) {
        long circleCount = groupMembershipRepository.countByUser(user);
        long updateCount = groupResponseRepository.findByUserId(user.getId()).stream()
                .map(response -> response.getGroupCycleId())
                .distinct()
                .count();

        return new ProfileStats(circleCount, updateCount, calculateCurrentStreak(user));
    }

    private long calculateCurrentStreak(User user) {
        List<GroupMembership> memberships = groupMembershipRepository.findByUser(user);
        if (memberships.isEmpty()) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        List<CycleOpportunity> opportunities = new ArrayList<>();

        for (GroupMembership membership : memberships) {
            List<GroupCycle> cycles = groupCycleRepository.findByGroupIdOrderByCycleStartDesc(membership.getGroup().getId());
            for (GroupCycle cycle : cycles) {
                LocalDateTime promptWindowEnd = cycle.getCycleStart().plusWeeks(1);
                if (membership.getCreatedAt() != null && !membership.getCreatedAt().isBefore(promptWindowEnd)) {
                    continue;
                }
                opportunities.add(new CycleOpportunity(cycle.getId(), cycle.getCycleStart()));
            }
        }

        opportunities.sort(Comparator.comparing(CycleOpportunity::cycleStart).reversed());

        long streak = 0;
        for (CycleOpportunity opportunity : opportunities) {
            LocalDateTime promptWindowEnd = opportunity.cycleStart().plusWeeks(1);
            boolean submittedInWindow = groupResponseRepository.findByGroupCycleIdAndUserId(opportunity.cycleId(), user.getId())
                    .stream()
                    .anyMatch(response -> response.getCreatedAt() != null
                            && !response.getCreatedAt().isBefore(opportunity.cycleStart())
                            && response.getCreatedAt().isBefore(promptWindowEnd));

            if (now.isBefore(promptWindowEnd)) {
                if (submittedInWindow) {
                    streak++;
                }
                continue;
            }

            if (!submittedInWindow) {
                return streak;
            }

            streak++;
        }

        return streak;
    }

    public record ProfileStats(long circleCount, long updateCount, long streakCount) {
    }

    private record CycleOpportunity(Long cycleId, LocalDateTime cycleStart) {
    }
}
