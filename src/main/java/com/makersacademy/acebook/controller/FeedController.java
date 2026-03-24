package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Group;
import com.makersacademy.acebook.model.GroupCycle;
import com.makersacademy.acebook.model.GroupMembership;
import com.makersacademy.acebook.model.GroupResponse;
import com.makersacademy.acebook.model.GroupResponseReaction;
import com.makersacademy.acebook.model.Prompt;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.GroupCycleRepository;
import com.makersacademy.acebook.repository.GroupMembershipRepository;
import com.makersacademy.acebook.repository.GroupRepository;
import com.makersacademy.acebook.repository.GroupResponseReactionRepository;
import com.makersacademy.acebook.repository.GroupResponseRepository;
import com.makersacademy.acebook.repository.PromptRepository;
import com.makersacademy.acebook.repository.UserRepository;
import com.makersacademy.acebook.service.SpotifyPlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Autowired
    private GroupResponseReactionRepository groupResponseReactionRepository;

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

        DefaultOidcUser user = (DefaultOidcUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String email = user.getEmail();
        User dbUser = userRepository.findUserByEmail(email).orElseThrow();
        Long userId = dbUser.getId();

        Map<Long, Map<String, Integer>> reactionCountsByResponse = new LinkedHashMap<>();
        Map<Long, String> userReactionByResponse = new LinkedHashMap<>();

        for (GroupResponse response : responses) {
            List<GroupResponseReaction> reactions =
                    groupResponseReactionRepository.findByGroupResponseId(response.getId());

            Map<String, Integer> emojiCounts = new LinkedHashMap<>();
            emojiCounts.put("❤️", 0);
            emojiCounts.put("😂", 0);
            emojiCounts.put("👍", 0);

            for (GroupResponseReaction reaction : reactions) {
                emojiCounts.put(
                        reaction.getEmoji(),
                        emojiCounts.getOrDefault(reaction.getEmoji(), 0) + 1
                );

                if (reaction.getUserId().equals(userId)) {
                    userReactionByResponse.put(response.getId(), reaction.getEmoji());
                }
            }

            reactionCountsByResponse.put(response.getId(), emojiCounts);
        }

        Map<Prompt, List<Map<String, Object>>> newsletter = new LinkedHashMap<>();

        for (GroupResponse r : responses) {
            Prompt prompt = promptRepository.findById(r.getPromptId()).orElseThrow();

            Map<String, Object> responseData = new LinkedHashMap<>();
            responseData.put("responseId", r.getId());
            responseData.put("responseText", r.getResponseText());
            responseData.put("userId", r.getUserId());
            responseData.put("userName", r.getUser() != null ? r.getUser().getName() : null);
            responseData.put("imageUrl", r.getImageUrl());
            responseData.put("spotifyTrackUrl", r.getSpotifyTrackUrl());

            newsletter.computeIfAbsent(prompt, k -> new ArrayList<>()).add(responseData);
        }

        model.addAttribute("group", group);
        model.addAttribute("cycle", cycle);

        String monthName = cycle.getCycleStart()
                .getMonth()
                .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);

        model.addAttribute("monthName", monthName);
        model.addAttribute("newsletter", newsletter);
        model.addAttribute("soundtrackLinks", spotifyPlaylistService.buildTrackLinks(new ArrayList<>(soundtrackLinks)));
        model.addAttribute("reactionCountsByResponse", reactionCountsByResponse);
        model.addAttribute("userReactionByResponse", userReactionByResponse);

        return "feed";
    }

    @PostMapping("/{groupId}")
    public RedirectView leaveGroup(@AuthenticationPrincipal DefaultOidcUser oidcUser,
                                   @PathVariable Long groupId) {
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

    @PostMapping("/{groupId}/responses/{responseId}/react")
    public String reactToResponse(@PathVariable Long groupId,
                                  @PathVariable Long responseId,
                                  @RequestParam String emoji) {

        DefaultOidcUser user = (DefaultOidcUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String email = user.getEmail();
        User dbUser = userRepository.findUserByEmail(email).orElseThrow();
        Long userId = dbUser.getId();

        Optional<GroupResponseReaction> existingReaction =
                groupResponseReactionRepository.findByGroupResponseIdAndUserId(responseId, userId);

        if (existingReaction.isPresent()) {
            GroupResponseReaction reaction = existingReaction.get();

            if (reaction.getEmoji().equals(emoji)) {
                groupResponseReactionRepository.delete(reaction);
            } else {
                reaction.setEmoji(emoji);
                groupResponseReactionRepository.save(reaction);
            }
        } else {
            GroupResponseReaction newReaction = new GroupResponseReaction(responseId, userId, emoji);
            groupResponseReactionRepository.save(newReaction);
        }

        return "redirect:/feed/" + groupId;
    }
}
