package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.*;
import com.makersacademy.acebook.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/feed")
public class FeedController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupCycleRepository groupCycleRepository;

    @Autowired
    private GroupResponseRepository groupResponseRepository;

    @Autowired
    private PromptRepository promptRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupMembershipRepository membershipRepository;

    @GetMapping("/{groupId}")
    public String showFeed(@PathVariable Long groupId,
                           @RequestParam(required = false) Long cycleId,
                           Model model) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        GroupCycle cycle;
        if (cycleId != null) {
            cycle = groupCycleRepository.findById(cycleId)
                    .orElseThrow(() -> new RuntimeException("No cycle"));
            if (!cycle.getGroupId().equals(groupId)) {
                throw new RuntimeException("Cycle does not belong to this group");
            }
        } else {
            cycle = groupCycleRepository
                    .findCurrentCycleByGroupId(groupId, LocalDateTime.now())
                    .orElseThrow(() -> new RuntimeException("No cycle"));
        }

        LocalDateTime start = cycle.getCycleStart();
        LocalDateTime end = start.plusWeeks(1);

        boolean feedLocked = LocalDateTime.now().isBefore(end);

        model.addAttribute("feedLocked", feedLocked);
        model.addAttribute("unlockTime", end);

        List<GroupResponse> responses =
                groupResponseRepository.findResponsesForFirstWeek(cycle.getId(), start, end);

        Map<Prompt, List<Map<String, Object>>> newsletter = new LinkedHashMap<>();

        for (GroupResponse r : responses) {
            Prompt prompt = promptRepository.findById(r.getPromptId()).orElseThrow();

            // create a small map with response + user info
            Map<String, Object> responseData = new LinkedHashMap<>();
            responseData.put("responseText", r.getResponseText());
            responseData.put("userId", r.getUserId());
            responseData.put("userName", r.getUser().getName());
            responseData.put("imageUrl", r.getImageUrl());

            newsletter.computeIfAbsent(prompt, k -> new ArrayList<>()).add(responseData);
        }

        model.addAttribute("group", group);
        model.addAttribute("cycle", cycle);
        model.addAttribute("newsletter", newsletter);

        return "feed";
    }

    @PostMapping("/{groupId}")
    public RedirectView leaveGroup(@AuthenticationPrincipal DefaultOidcUser oidcUser, @PathVariable Long groupId) {
        String email = oidcUser.getEmail();
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        GroupMembership groupMembership = membershipRepository.findByUserAndGroup(user, group)
                .orElseThrow(() -> new RuntimeException("Membership not found"));
        membershipRepository.delete(groupMembership);
        return new RedirectView("/");
    }
}
