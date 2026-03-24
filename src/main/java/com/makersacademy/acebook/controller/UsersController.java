package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.UserRepository;
import com.makersacademy.acebook.repository.GroupMembershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import com.makersacademy.acebook.service.CloudinaryService;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Controller
public class UsersController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    CloudinaryService cloudinaryService;

    @Autowired
    GroupMembershipRepository groupMembershipRepository;

    @GetMapping("/users/after-login")
    public RedirectView afterLogin(HttpSession session) {
        DefaultOidcUser principal = (DefaultOidcUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String email = (String) principal.getAttributes().get("email");
        User user = userRepository
                .findUserByEmail(email)
                .orElseGet(() -> userRepository.save(new User(email, "", "")));

        if (!user.isProfileComplete()) {
            return new RedirectView("/users/setup");
        }

        Object pendingInvite = session.getAttribute(GroupController.PENDING_INVITE_LINK_SESSION_KEY);

        if (pendingInvite instanceof String pendingInviteLink && !pendingInviteLink.isBlank()) {
            return new RedirectView("/circles/join?inviteLink="
                    + URLEncoder.encode(pendingInviteLink, StandardCharsets.UTF_8));
        }

        Object pendingPrompt = session.getAttribute(PromptController.PENDING_PROMPT_LINK_SESSION_KEY);

        if (pendingPrompt instanceof String pendingPromptLink && !pendingPromptLink.isBlank()) {
            session.removeAttribute(PromptController.PENDING_PROMPT_LINK_SESSION_KEY);
            return new RedirectView(pendingPromptLink);
        }

        return new RedirectView("/");
    }

    @GetMapping("/users/setup")
    public String setupAccountForm(Model model) {
        User user = getCurrentUser();
        if (user.isProfileComplete()) {
            return "redirect:/";
        }

        model.addAttribute("user", user);
        return "account/setup-account-form";
    }

    @PostMapping("/users/setup")
    public RedirectView setupAccount(@RequestParam String name,
                                     @RequestParam Integer age,
                                     @RequestParam String phoneNumber,
                                     HttpSession session) {
        User user = getCurrentUser();
        user.setName(name);
        user.setAge(age);
        user.setPhoneNumber(phoneNumber);
        userRepository.save(user);

        Object pendingInvite = session.getAttribute(GroupController.PENDING_INVITE_LINK_SESSION_KEY);

        if (pendingInvite instanceof String pendingInviteLink && !pendingInviteLink.isBlank()) {
            return new RedirectView("/circles/join?inviteLink="
                    + URLEncoder.encode(pendingInviteLink, StandardCharsets.UTF_8));
        }

        Object pendingPrompt = session.getAttribute(PromptController.PENDING_PROMPT_LINK_SESSION_KEY);

        if (pendingPrompt instanceof String pendingPromptLink && !pendingPromptLink.isBlank()) {
            session.removeAttribute(PromptController.PENDING_PROMPT_LINK_SESSION_KEY);
            return new RedirectView(pendingPromptLink);
        }

        return new RedirectView("/");
    }

    @PostMapping("/users/profile-picture")
    public RedirectView uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        try {
            DefaultOidcUser principal = (DefaultOidcUser) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();
            String email = (String) principal.getAttributes().get("email");
            User user = userRepository.findUserByEmail(email).orElseThrow();

            String imageUrl = cloudinaryService.uploadFile(file);

            user.setProfilePictureUrl(imageUrl);
            userRepository.save(user);

        } catch (Exception e) {
            System.out.println("Image upload failed: " + e.getMessage());
        }

        return new RedirectView("/");
    }

    @GetMapping("/users/{id}")
    public String account(@PathVariable Long id, Model model, @AuthenticationPrincipal DefaultOidcUser oidcUser) {
        Optional<User> urlPathUser = userRepository.findById(id);

        Optional<User> user = userRepository.findUserByEmail(oidcUser.getEmail());

        boolean isCorrectUser = urlPathUser.get().getId() == user.get().getId();

        if (!isCorrectUser) {
            throw new RuntimeException("Incorrect user");
        }

        model.addAttribute("user", user.get());

        return "account/account";
    }

    @GetMapping("/users/{id}/edit")
    public String editAccountForm(@PathVariable Long id, Model model) {
        Optional<User> user = userRepository.findById(id);
        model.addAttribute("user", user.get());
        return "account/edit-account-form";
    }

    @PostMapping("/users/{id}/edit")
    public RedirectView updateAccount(@RequestParam String name,
                                      @RequestParam Integer age,
                                      @RequestParam String phoneNumber,
                                      @RequestParam String bio,
                                      @PathVariable Long id) {
        Optional<User> currentUser = userRepository.findById(id);
        currentUser.get().setName(name);
        currentUser.get().setAge(age);
        currentUser.get().setPhoneNumber(phoneNumber);
        currentUser.get().setBio(bio);
        userRepository.save(currentUser.get());
        return new RedirectView("/users/" + id);
    }

    @Transactional
    @PostMapping("/users/{id}/delete")
    public RedirectView deleteAccount(@PathVariable Long id,
                                      @AuthenticationPrincipal DefaultOidcUser oidcUser,
                                      HttpSession session) {

        // find the currently logged-in app user
        User currentUser = userRepository.findUserByEmail(oidcUser.getEmail())
                .orElseThrow();

        // only allow users to delete their own profile
        if (!currentUser.getId().equals(id)) {
            throw new RuntimeException("You cannot delete another user's account");
        }

        // remove user from all groups first
        groupMembershipRepository.deleteByUser(currentUser);

        // then delete the user itself
        userRepository.delete(currentUser);

        // clear session data
        session.invalidate();

        // log out fully
        return new RedirectView("/logout");
    }

    private User getCurrentUser() {
        DefaultOidcUser principal = (DefaultOidcUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String email = (String) principal.getAttributes().get("email");
        return userRepository.findUserByEmail(email).orElseThrow();
    }
}
