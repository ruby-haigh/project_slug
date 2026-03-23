package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.*;
import com.makersacademy.acebook.repository.GroupCycleRepository;
import com.makersacademy.acebook.repository.GroupRepository;
import com.makersacademy.acebook.repository.GroupResponseReactionRepository;
import com.makersacademy.acebook.repository.GroupResponseRepository;
import com.makersacademy.acebook.repository.PromptRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private GroupResponseReactionRepository groupResponseReactionRepository;

    @Autowired
    private UserRepository userRepository;

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

        // get logged-in auth user
        DefaultOidcUser user = (DefaultOidcUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String email = user.getEmail();

        // match auth user to DB user
        User dbUser = userRepository.findUserByEmail(email).orElseThrow();
        Long userId = dbUser.getId();

        // build emoji counts for each response
        Map<Long, Map<String, Integer>> reactionCountsByResponse = new LinkedHashMap<>();

        // store current user's selected emoji for each response
        Map<Long, String> userReactionByResponse = new LinkedHashMap<>();

        for (GroupResponse response : responses) {
            List<GroupResponseReaction> reactions =
                    groupResponseReactionRepository.findByGroupResponseId(response.getId());

            // always start all 3 emoji counts so they appear consistently
            Map<String, Integer> emojiCounts = new LinkedHashMap<>();
            emojiCounts.put("❤️", 0);
            emojiCounts.put("😂", 0);
            emojiCounts.put("👍", 0);

            for (GroupResponseReaction reaction : reactions) {
                emojiCounts.put(
                        reaction.getEmoji(),
                        emojiCounts.getOrDefault(reaction.getEmoji(), 0) + 1
                );

                // if this reaction belongs to current user, remember it
                if (reaction.getUserId().equals(userId)) {
                    userReactionByResponse.put(response.getId(), reaction.getEmoji());
                }
            }

            reactionCountsByResponse.put(response.getId(), emojiCounts);
        }

        Map<Prompt, List<Map<String, Object>>> newsletter = new LinkedHashMap<>();

        for (GroupResponse r : responses) {
            Prompt prompt = promptRepository.findById(r.getPromptId()).orElseThrow();

            // create a small map with response + user info
            Map<String, Object> responseData = new LinkedHashMap<>();
            responseData.put("responseId", r.getId());
            responseData.put("responseText", r.getResponseText());
            responseData.put("userId", r.getUserId());
            responseData.put("userName", r.getUser().getName());
            responseData.put("imageUrl", r.getImageUrl());

            newsletter.computeIfAbsent(prompt, k -> new ArrayList<>()).add(responseData);
        }

        model.addAttribute("group", group);
        model.addAttribute("cycle", cycle);
        model.addAttribute("newsletter", newsletter);
        model.addAttribute("reactionCountsByResponse", reactionCountsByResponse);
        model.addAttribute("userReactionByResponse", userReactionByResponse);

        return "feed";
    }

    // toggle reaction on a single response
    @PostMapping("/{groupId}/responses/{responseId}/react")
    public String reactToResponse(@PathVariable Long groupId,
                                  @PathVariable Long responseId,
                                  @RequestParam String emoji) {

        // get logged-in auth user
        DefaultOidcUser user = (DefaultOidcUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String email = user.getEmail();

        // find matching DB user
        User dbUser = userRepository.findUserByEmail(email).orElseThrow();
        Long userId = dbUser.getId();

        // only one reaction per user per response
        Optional<GroupResponseReaction> existingReaction =
                groupResponseReactionRepository.findByGroupResponseIdAndUserId(responseId, userId);

        if (existingReaction.isPresent()) {
            GroupResponseReaction reaction = existingReaction.get();

            // same emoji clicked again = remove reaction
            if (reaction.getEmoji().equals(emoji)) {
                groupResponseReactionRepository.delete(reaction);
            } else {
                // different emoji clicked = switch reaction
                reaction.setEmoji(emoji);
                groupResponseReactionRepository.save(reaction);
            }
        } else {
            // no reaction yet = create one
            GroupResponseReaction newReaction = new GroupResponseReaction(responseId, userId, emoji);
            groupResponseReactionRepository.save(newReaction);
        }

        return "redirect:/feed/" + groupId;
    }
}