package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Group;
import com.makersacademy.acebook.model.GroupCycle;
import com.makersacademy.acebook.model.GroupCyclePrompt;
import com.makersacademy.acebook.model.Prompt;
import com.makersacademy.acebook.repository.GroupCyclePromptRepository;
import com.makersacademy.acebook.repository.GroupCycleRepository;
import com.makersacademy.acebook.repository.GroupRepository;
import com.makersacademy.acebook.repository.PromptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
}