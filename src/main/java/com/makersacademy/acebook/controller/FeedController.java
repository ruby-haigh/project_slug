package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.*;
import com.makersacademy.acebook.repository.GroupCycleRepository;
import com.makersacademy.acebook.repository.GroupRepository;
import com.makersacademy.acebook.repository.GroupResponseRepository;
import com.makersacademy.acebook.repository.PromptRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/feed")
public class FeedController {

    private final GroupRepository groupRepository;
    private final GroupCycleRepository groupCycleRepository;
    private final GroupResponseRepository groupResponseRepository;
    private final PromptRepository promptRepository;

    public FeedController(GroupRepository groupRepository, GroupCycleRepository groupCycleRepository, GroupResponseRepository groupResponseRepository, PromptRepository promptRepository) {
        this.groupRepository = groupRepository;
        this.groupCycleRepository = groupCycleRepository;
        this.groupResponseRepository = groupResponseRepository;
        this.promptRepository = promptRepository;

    }
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
}
