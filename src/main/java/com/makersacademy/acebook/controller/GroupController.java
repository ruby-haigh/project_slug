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

    private User getCurrentUser() {
        DefaultOidcUser principal = (DefaultOidcUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        String email = (String) principal.getAttributes().get("email");
        return userRepository.findUserByEmail(email).orElseThrow();
    }

    // Dashboard page
    @GetMapping
    public String getGroupsPage(Model model) {
        User user = getCurrentUser();
        List<Group> groups = membershipRepository.findGroupsByUser(user);
        if (groups == null) groups = List.of();
        model.addAttribute("groups", groups);
        return "groups";
    }

    // Separate "Create Circle" page
    @GetMapping("/create")
    public String getCreateCirclePage() {
        return "create-circle"; // new Thymeleaf template
    }

    @PostMapping("/create")
    public String createGroup(@RequestParam String name) {
        Group group = new Group(name);
        groupRepository.save(group);

        User user = getCurrentUser();
        GroupMembership membership = new GroupMembership(user, group);
        membershipRepository.save(membership);

        return "redirect:/groups"; // back to dashboard
    }


    // Rename a group
    @PostMapping("/{id}/update")
    public String updateGroup(@PathVariable Long id, @RequestParam String name) {
        Group group = groupRepository.findById(id).orElseThrow();
        group.setName(name);
        groupRepository.save(group);
        return "redirect:/groups";
    }

    // Invite a user
    @PostMapping("/{groupId}/invite")
    public String inviteUser(@PathVariable Long groupId, @RequestParam String email) {
        User user = userRepository
                .findUserByEmail(email)
                .orElseGet(() -> userRepository.save(new User(email)));

        Group group = groupRepository.findById(groupId).orElseThrow();

        boolean alreadyMember = membershipRepository.findByUserAndGroup(user, group).isPresent();
        if (!alreadyMember) {
            GroupMembership membership = new GroupMembership(user, group);
            membershipRepository.save(membership);
        }

        String inviteLink = "http://localhost:8080/groups/" + group.getId() + "/join?email=" + email;
        emailService.sendInvite(email, group.getName(), inviteLink);

        return "redirect:/groups";
    }

    @GetMapping("/{groupId}/join")
    public String joinGroup(@PathVariable Long groupId, @RequestParam String email) {
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