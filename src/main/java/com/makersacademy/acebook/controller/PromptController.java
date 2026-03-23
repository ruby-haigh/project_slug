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
import com.makersacademy.acebook.service.CloudinaryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class PromptController {
    public static final String PENDING_PROMPT_LINK_SESSION_KEY = "pendingPromptLink";

    @Autowired
    private PromptRepository promptRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupCycleRepository groupCycleRepository;

    @Autowired
    private GroupCyclePromptRepository groupCyclePromptRepository;

    @Autowired
    private GroupResponseRepository groupResponseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping("/groups/{groupId}/prompts/open")
    public String openPromptFormFromEmail(@PathVariable Long groupId, HttpSession session) {
        String promptFormPath = "/groups/" + groupId + "/prompts";

        if (getCurrentUser().isEmpty()) {
            session.setAttribute(PENDING_PROMPT_LINK_SESSION_KEY, promptFormPath);
            return "redirect:/oauth2/authorization/okta";
        }

        return "redirect:" + promptFormPath;
    }

    @GetMapping("/groups/{groupId}/prompts")
    public String showPromptForm(@PathVariable Long groupId, Model model) {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            return "redirect:/groups";
        }

        Group group = optionalGroup.get();

        LocalDateTime now = LocalDateTime.now();
        Optional<GroupCycle> optionalCycle = groupCycleRepository.findCurrentCycleByGroupId(groupId, now);

        GroupCycle currentCycle;
        if (optionalCycle.isEmpty()) {
            long cycleLengthDays;

            if (group.getFrequency() != null && group.getFrequency().equalsIgnoreCase("FORTNIGHTLY")) {
                cycleLengthDays = 14;
            } else {
                cycleLengthDays = 30;
            }

            currentCycle = new GroupCycle(groupId, now, now.plusDays(cycleLengthDays));
            currentCycle = groupCycleRepository.save(currentCycle);
        } else {
            currentCycle = optionalCycle.get();
        }

        User dbUser = getCurrentUser().orElseThrow();
        Long userId = dbUser.getId();

        boolean alreadySubmitted =
                groupResponseRepository.existsByGroupCycleIdAndUserId(currentCycle.getId(), userId);

        List<GroupCyclePrompt> cyclePromptLinks =
                groupCyclePromptRepository.findByGroupCycleId(currentCycle.getId());

        List<Prompt> promptsForForm = new ArrayList<>();

        if (cyclePromptLinks.isEmpty()) {
            List<Prompt> randomPrompts = promptRepository.findRandomPrompts();

            for (Prompt prompt : randomPrompts) {
                GroupCyclePrompt link = new GroupCyclePrompt(currentCycle.getId(), prompt.getId());
                groupCyclePromptRepository.save(link);
                promptsForForm.add(prompt);
            }
        } else {
            for (GroupCyclePrompt link : cyclePromptLinks) {
                promptRepository.findById(link.getPromptId()).ifPresent(promptsForForm::add);
            }
        }

        model.addAttribute("group", group);
        model.addAttribute("groupId", groupId);
        model.addAttribute("groupCycleId", currentCycle.getId());
        model.addAttribute("prompts", promptsForForm);
        model.addAttribute("alreadySubmitted", alreadySubmitted);

        return "prompts/form";
    }

    @PostMapping("/groups/{groupId}/prompts")
    public String submitPromptForm(
            @PathVariable Long groupId,
            @RequestParam Long groupCycleId,
            @RequestParam List<Long> promptIds,
            @RequestParam List<String> responseTexts,
            @RequestParam(required = false) List<MultipartFile> images
    ) {
        User dbUser = getCurrentUser().orElseThrow();
        Long userId = dbUser.getId();

        boolean alreadySubmitted =
                groupResponseRepository.existsByGroupCycleIdAndUserId(groupCycleId, userId);

        if (alreadySubmitted) {
            return "redirect:/groups/" + groupId + "/prompts";
        }

        // Loop through each prompt that was submitted in the form
        for (int i = 0; i < promptIds.size(); i++) {
            Long promptId = promptIds.get(i);
            String responseText = responseTexts.get(i);

            // Skip this prompt if the user left the text field empty
            if (responseText == null || responseText.trim().isEmpty()) {
                continue;
            }

            // Create a new response object with the cycle, group, user and prompt context
            GroupResponse response = new GroupResponse(
<<<<<<< Updated upstream
                    groupCycleId, groupId, userId, promptId, responseText
=======
                    groupCycleId,
                    groupId,
                    userId,
                    promptId,
                    responseText,
                    LocalDateTime.now()
>>>>>>> Stashed changes
            );

            // Upload image to Cloudinary if one was provided for this prompt
            if (images != null && i < images.size() && !images.get(i).isEmpty()) {
                try {
                    String imageUrl = cloudinaryService.uploadFile(images.get(i));
                    response.setImageUrl(imageUrl);
                } catch (Exception e) {
                    System.out.println("Image upload failed: " + e.getMessage());
                }
            }

            groupResponseRepository.save(response);
        }

        return "redirect:/groups/" + groupId + "/prompts";
    }

    private Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof DefaultOidcUser oidcUser)) {
            return Optional.empty();
        }

        return userRepository.findUserByEmail(oidcUser.getEmail());
    }
}