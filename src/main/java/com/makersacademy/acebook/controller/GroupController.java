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
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
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
    @PostMapping
    public Group createGroup(@RequestBody Map<String, String> body) {

        //get the group name
        String name = body.get("name");

        //create the group
        Group group = new Group(name);
        groupRepository.save(group);

        //get current user
        User user = getCurrentUser();

        //link user to group - this user belongs to this group
        GroupMembership membership = new GroupMembership(user, group);
        membershipRepository.save(membership);

        return group;
    }
    //Edit Group Name
    @PutMapping("/{id}")
    public Group updateGroup(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Group group = groupRepository.findById(id).orElseThrow();
        group.setName(body.get("name"));
        return groupRepository.save(group);

    }
    //invite users
    @PostMapping("/{groupId}/invite")
    public String inviteUser(@PathVariable Long groupId, @RequestBody Map<String, String> body) {
        String email = body.get("email");
        User user = userRepository
                .findUserByEmail(email)
                .orElseGet(() -> userRepository.save(new User(email)));
        Group group = groupRepository.findById(groupId).orElseThrow();
        GroupMembership membership = new GroupMembership(user, group);
        membershipRepository.save(membership);
        return "User added to group";

    }

}