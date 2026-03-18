package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Group;
import com.makersacademy.acebook.model.GroupMembership;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.GroupMembershipRepository;
import com.makersacademy.acebook.repository.GroupRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/groups")
public class GroupController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        DefaultOidcUser principal = (DefaultOidcUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String email = (String) principal.getAttributes().get("email");

        return userRepository.findUserByEmail(email).orElseThrow();
    }

    // show groups page
    @GetMapping
    public String getGroupsPage(Model model) {
        model.addAttribute("groups", groupRepository.findAll());
        return "groups";
    }

    // create group
    @PostMapping
    public String createGroup(@RequestParam String name) {

        Group group = new Group(name); // add default frequency in model if needed
        groupRepository.save(group);

        User user = getCurrentUser();
        GroupMembership membership = new GroupMembership(user, group);
        membershipRepository.save(membership);

        return "redirect:/groups";
    }

    // update group name
    @PostMapping("/{id}/update")
    public String updateGroup(@PathVariable Long id, @RequestParam String name) {
        Group group = groupRepository.findById(id).orElseThrow();
        group.setName(name);
        groupRepository.save(group);

        return "redirect:/groups";
    }

    // invite user
    @PostMapping("/{groupId}/invite")
    public String inviteUser(@PathVariable Long groupId, @RequestParam String email) {

        User user = userRepository
                .findUserByEmail(email)
                .orElseGet(() -> userRepository.save(new User(email)));

        Group group = groupRepository.findById(groupId).orElseThrow();

        GroupMembership membership = new GroupMembership(user, group);
        membershipRepository.save(membership);

        return "redirect:/groups";
    }
}