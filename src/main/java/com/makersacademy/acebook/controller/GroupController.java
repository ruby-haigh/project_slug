package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Group;
import com.makersacademy.acebook.model.GroupMembership;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.GroupMembershipRepository;
import com.makersacademy.acebook.repository.GroupRepository;
import com.makersacademy.acebook.repository.UserRepository;
import com.makersacademy.acebook.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/groups")
public class GroupController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // Utility to get currently logged-in user
    private User getCurrentUser() {
        DefaultOidcUser principal = (DefaultOidcUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String email = (String) principal.getAttributes().get("email");
        return userRepository.findUserByEmail(email).orElseThrow();
    }

    // Dashboard: create group + list groups
    @GetMapping
    public String getGroupsPage(Model model) {
        User user = getCurrentUser();
        List<Group> groups = membershipRepository.findGroupsByUser(user);
        if (groups == null) groups = List.of(); // never null
        model.addAttribute("groups", groups);
        return "groups"; // Thymeleaf template
    }

    // Create a new group
    @PostMapping
    public String createGroup(@RequestParam String name) {
        Group group = new Group(name);
        groupRepository.save(group);

        User user = getCurrentUser();
        GroupMembership membership = new GroupMembership(user, group);
        membershipRepository.save(membership);

        return "redirect:/groups";
    }

    // Rename a group
    @PostMapping("/{id}/update")
    public String updateGroup(@PathVariable Long id, @RequestParam String name) {
        Group group = groupRepository.findById(id).orElseThrow();
        group.setName(name);
        groupRepository.save(group);
        return "redirect:/groups";
    }

    // Invite a user to a group
    @PostMapping("/{groupId}/invite")
    public String inviteUser(@PathVariable Long groupId, @RequestParam String email) {
        User user = userRepository
                .findUserByEmail(email)
                .orElseGet(() -> userRepository.save(new User(email)));

        Group group = groupRepository.findById(groupId).orElseThrow();

        // Only add membership if not already a member
        boolean alreadyMember = membershipRepository.findByUserAndGroup(user, group).isPresent();
        if (!alreadyMember) {
            GroupMembership membership = new GroupMembership(user, group);
            membershipRepository.save(membership);
        }

        // Send invite email with clickable link
        String inviteLink = "http://localhost:8080/groups/" + group.getId() + "/join?email=" + email;
        emailService.sendInvite(email, group.getName(), inviteLink);

        return "redirect:/groups";
    }

    // Join a group via invite link
    @GetMapping("/{groupId}/join")
    public String joinGroup(@PathVariable Long groupId, @RequestParam String email) {
        // If user already exists, use that, otherwise create
        User user = userRepository
                .findUserByEmail(email)
                .orElseGet(() -> userRepository.save(new User(email)));

        Group group = groupRepository.findById(groupId).orElseThrow();

        boolean alreadyMember = membershipRepository.findByUserAndGroup(user, group).isPresent();
        if (!alreadyMember) {
            GroupMembership membership = new GroupMembership(user, group);
            membershipRepository.save(membership);
        }

        return "redirect:/groups";
    }
}