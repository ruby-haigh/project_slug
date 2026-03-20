package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Group;
import com.makersacademy.acebook.model.GroupCycle;
import com.makersacademy.acebook.model.GroupCyclePrompt;
import com.makersacademy.acebook.model.GroupResponse;
import com.makersacademy.acebook.model.Prompt;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.GroupCyclePromptRepository;
import com.makersacademy.acebook.repository.GroupCycleRepository;
import com.makersacademy.acebook.repository.GroupRepository;
import com.makersacademy.acebook.repository.GroupResponseRepository;
import com.makersacademy.acebook.repository.PromptRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class PromptController {

    @Autowired
    private PromptRepository promptRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupCycleRepository groupCycleRepository;

    @Autowired
    private GroupCyclePromptRepository groupCyclePromptRepository;

    // new: repo for saving responses
    @Autowired
    private GroupResponseRepository groupResponseRepository;

    // new: needed to get logged in user
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/groups/{groupId}/prompts")
    public String showPromptForm(@PathVariable Long groupId, Model model) {

        System.out.println("PROMPT ROUTE HIT for group " + groupId);

        // First make sure the group actually exists
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            return "redirect:/groups";
        }

        Group group = optionalGroup.get();

        // Find the current active cycle for this group using the current time
        LocalDateTime now = LocalDateTime.now();
        Optional<GroupCycle> optionalCycle = groupCycleRepository.findCurrentCycleByGroupId(groupId, now);

        GroupCycle currentCycle;

        // If no cycle exists yet, create a simple default cycle for 30 days
        if (optionalCycle.isEmpty()) {
            currentCycle = new GroupCycle(
                    groupId,
                    now,
                    now.plusDays(30)
            );
            currentCycle = groupCycleRepository.save(currentCycle);
        } else {
            currentCycle = optionalCycle.get();
        }

        // Check whether prompts have already been assigned to this cycle
        List<GroupCyclePrompt> cyclePromptLinks =
                groupCyclePromptRepository.findByGroupCycleId(currentCycle.getId());

        List<Prompt> promptsForForm = new ArrayList<>();

        // If no prompts exist yet for this cycle, randomise and save them
        if (cyclePromptLinks.isEmpty()) {
            List<Prompt> randomPrompts = promptRepository.findRandomPrompts();

            for (Prompt prompt : randomPrompts) {
                GroupCyclePrompt link = new GroupCyclePrompt(currentCycle.getId(), prompt.getId());
                groupCyclePromptRepository.save(link);
                promptsForForm.add(prompt);
            }
        } else {
            // If prompts already exist for this cycle, load those exact prompts
            for (GroupCyclePrompt link : cyclePromptLinks) {
                promptRepository.findById(link.getPromptId()).ifPresent(promptsForForm::add);
            }
        }

        // Send data to the HTML page
        model.addAttribute("group", group);
        model.addAttribute("groupId", groupId);
        model.addAttribute("groupCycleId", currentCycle.getId());
        model.addAttribute("prompts", promptsForForm);

        return "prompts/form";
    }

    // =========================
    // POST: handle form submit
    // =========================
    @PostMapping("/groups/{groupId}/prompts")
    public String submitPromptForm(
            @PathVariable Long groupId,
            @RequestParam Long groupCycleId,
            @RequestParam List<Long> promptIds,
            @RequestParam List<String> responseTexts
    ) {

        // get logged in user from auth0
        DefaultOidcUser user = (DefaultOidcUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String email = user.getEmail();

        // find user in DB
        User dbUser = userRepository.findUserByEmail(email).orElseThrow();
        Long userId = dbUser.getId();

        // check if user already submitted this cycle (prevent duplicates)
        boolean alreadySubmitted =
                groupResponseRepository.existsByGroupCycleIdAndUserId(groupCycleId, userId);

        if (alreadySubmitted) {
            return "redirect:/groups/" + groupId;
        }

        // loop through prompts + responses and save each one
        for (int i = 0; i < promptIds.size(); i++) {

            Long promptId = promptIds.get(i);
            String responseText = responseTexts.get(i);

            // skip empty answers
            if (responseText == null || responseText.trim().isEmpty()) {
                continue;
            }

            GroupResponse response = new GroupResponse(
                    groupCycleId,
                    groupId,
                    userId,
                    promptId,
                    responseText
            );

            groupResponseRepository.save(response);
        }

        // redirect back to group page after submit
        return "redirect:/groups/" + groupId;
    }
}