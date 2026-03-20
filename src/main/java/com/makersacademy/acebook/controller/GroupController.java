package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Group;
import com.makersacademy.acebook.model.GroupMembership;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.GroupMembershipRepository;
import com.makersacademy.acebook.repository.GroupRepository;
import com.makersacademy.acebook.repository.UserRepository;
import com.makersacademy.acebook.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/groups")
public class GroupController {
    public static final String PENDING_INVITE_LINK_SESSION_KEY = "pendingInviteLink";

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
    //create group

    @GetMapping("/create")
    public String getCreateCirclePage(Model model) {
        User loggedInUser = getCurrentUser();
        model.addAttribute("loggedInUser", loggedInUser);
        return "create-circle";
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
    public String inviteUser(@PathVariable Long groupId, @RequestParam String email, HttpServletRequest request) {
        Group group = groupRepository.findById(groupId).orElseThrow();

        String inviteLink = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath("/groups/" + group.getId() + "/join")
                .replaceQueryParam("email", email)
                .build()
                .toUriString();
        emailService.sendInvite(email, group.getName(), inviteLink);

        return "redirect:/groups";
    }

    @GetMapping("/{groupId}/join")
    public String joinGroup(@PathVariable Long groupId,
                            @RequestParam(required = false) String email,
                            HttpServletRequest request,
                            HttpSession session) {
        groupRepository.findById(groupId).orElseThrow();

        String inviteLink = ServletUriComponentsBuilder.fromRequest(request)
                .replaceQueryParam("email", email)
                .build()
                .toUriString();

        session.setAttribute(PENDING_INVITE_LINK_SESSION_KEY, inviteLink);

        return "redirect:/circles/join?inviteLink=" + URLEncoder.encode(inviteLink, StandardCharsets.UTF_8);
    }
}
