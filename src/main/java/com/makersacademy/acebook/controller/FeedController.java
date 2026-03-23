package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.*;
import com.makersacademy.acebook.repository.GroupCycleRepository;
import com.makersacademy.acebook.repository.GroupRepository;
import com.makersacademy.acebook.repository.GroupResponseRepository;
import com.makersacademy.acebook.repository.PromptRepository;
import com.makersacademy.acebook.service.SpotifyPlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private SpotifyPlaylistService spotifyPlaylistService;

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

        Set<String> soundtrackLinks = new LinkedHashSet<>();
        for (GroupResponse response : responses) {
            String spotifyTrackUrl = response.getSpotifyTrackUrl();
            if (spotifyTrackUrl == null || spotifyTrackUrl.isBlank()) {
                spotifyTrackUrl = spotifyPlaylistService.normalizeSpotifyTrackUrl(response.getResponseText());
            }

            if (spotifyTrackUrl != null && !spotifyTrackUrl.isBlank()) {
                soundtrackLinks.add(spotifyTrackUrl);
            }
        }

        Map<Prompt, List<Map<String, Object>>> newsletter = new LinkedHashMap<>();

        for (GroupResponse r : responses) {
            Prompt prompt = promptRepository.findById(r.getPromptId()).orElseThrow();

            Map<String, Object> responseData = new LinkedHashMap<>();
            responseData.put("responseText", r.getResponseText());
            responseData.put("userId", r.getUserId());
            responseData.put("userName", r.getUser() != null ? r.getUser().getName() : null);
            responseData.put("imageUrl", r.getImageUrl());
            responseData.put("spotifyTrackUrl", r.getSpotifyTrackUrl());

            newsletter.computeIfAbsent(prompt, k -> new ArrayList<>()).add(responseData);
        }

        model.addAttribute("group", group);
        model.addAttribute("cycle", cycle);
        model.addAttribute("newsletter", newsletter);
        model.addAttribute("soundtrackLinks", spotifyPlaylistService.buildTrackLinks(new ArrayList<>(soundtrackLinks)));

        return "feed";
    }
}
