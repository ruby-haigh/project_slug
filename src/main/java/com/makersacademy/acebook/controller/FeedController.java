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
    public String showFeed(@PathVariable Long groupId, Model model) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        GroupCycle cycle = groupCycleRepository
                .findCurrentCycleByGroupId(groupId, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("No cycle"));

        List<GroupResponse> responses =
                groupResponseRepository.findByGroupCycleId(cycle.getId());

        Map<Prompt, List<GroupResponse>> newsletter = new LinkedHashMap<>();
        for (GroupResponse r : responses) {
            Prompt prompt = promptRepository.findById(r.getPromptId()).orElseThrow();
            newsletter.computeIfAbsent(prompt, k -> new ArrayList<>()).add(r);
        }


        model.addAttribute("group", group);
        model.addAttribute("newsletter", newsletter);

        return "feed";
    }

    @PostMapping("/{groupId}")
    public RedirectView leaveGroup(@AuthenticationPrincipal DefaultOidcUser oidcUser, @PathVariable Long groupId) {
        String email = oidcUser.getEmail();
        User user = userRepository.findUserByEmail(email).get();
        Group group = groupRepository.findById(groupId).get();
        GroupMembership groupMembership = membershipRepository.findByUserAndGroup(user, group).get();
        membershipRepository.delete(groupMembership);
        return new RedirectView("/");
    }
}
