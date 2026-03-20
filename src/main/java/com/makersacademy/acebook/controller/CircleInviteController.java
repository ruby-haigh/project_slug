package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Group;
import com.makersacademy.acebook.model.GroupMembership;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.GroupMembershipRepository;
import com.makersacademy.acebook.repository.GroupRepository;
import com.makersacademy.acebook.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class CircleInviteController {
    private static final Pattern GROUP_JOIN_PATH = Pattern.compile(".*/groups/(\\d+)/join(?:\\?.*)?$");

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/circles/join")
    public String joinByLinkPage(@RequestParam(required = false) String inviteLink,
                                 @RequestParam(required = false) String error,
                                 @RequestParam(required = false) String message,
                                 Model model,
                                 HttpSession session) {
        Optional<User> currentUser = getCurrentUser();
        String resolvedInviteLink = inviteLink;
        if (resolvedInviteLink == null || resolvedInviteLink.isBlank()) {
            Object pendingInvite = session.getAttribute(GroupController.PENDING_INVITE_LINK_SESSION_KEY);
            if (pendingInvite instanceof String pendingInviteLink) {
                resolvedInviteLink = pendingInviteLink;
            }
        } else {
            session.setAttribute(GroupController.PENDING_INVITE_LINK_SESSION_KEY, resolvedInviteLink);
        }

        if (currentUser.isEmpty()
                && resolvedInviteLink != null
                && !resolvedInviteLink.isBlank()
                && error == null
                && message == null) {
            return "redirect:/oauth2/authorization/okta";
        }

        InviteDetails inviteDetails = parseInviteLink(resolvedInviteLink);

        model.addAttribute("inviteLink", resolvedInviteLink == null ? "" : resolvedInviteLink);
        model.addAttribute("inviteDetails", inviteDetails);
        model.addAttribute("errorMessage", error);
        model.addAttribute("successMessage", message);
        model.addAttribute("loggedInUser", currentUser.orElse(null));
        return "join-circle";
    }

    @PostMapping("/circles/join")
    public String joinByInviteLink(@RequestParam String inviteLink, HttpSession session) {
        Optional<User> currentUser = getCurrentUser();
        if (currentUser.isEmpty()) {
            session.setAttribute(GroupController.PENDING_INVITE_LINK_SESSION_KEY, inviteLink);
            return "redirect:/oauth2/authorization/okta";
        }

        InviteDetails inviteDetails = parseInviteLink(inviteLink);
        if (!inviteDetails.isValid()) {
            return "redirect:/circles/join?inviteLink="
                    + encode(inviteLink)
                    + "&error="
                    + encode("That invite link does not look valid yet.");
        }

        User user = currentUser.get();
        if (inviteDetails.invitedEmail() != null
                && !inviteDetails.invitedEmail().equalsIgnoreCase(user.getEmail())) {
            return "redirect:/circles/join?inviteLink="
                    + encode(inviteLink)
                    + "&error="
                    + encode("This invite was sent to " + inviteDetails.invitedEmail() + ". Sign in with that email to join this circle.");
        }

        Group group = groupRepository.findById(inviteDetails.groupId()).orElseThrow();
        boolean alreadyMember = membershipRepository.findByUserAndGroup(user, group).isPresent();
        if (!alreadyMember) {
            membershipRepository.save(new GroupMembership(user, group));
        }

        session.removeAttribute(GroupController.PENDING_INVITE_LINK_SESSION_KEY);

        return "redirect:/circles/join?message="
                + encode("You joined " + group.getName() + ".")
                + "&inviteLink="
                + encode(inviteLink);
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

    private InviteDetails parseInviteLink(String inviteLink) {
        if (inviteLink == null || inviteLink.isBlank()) {
            return InviteDetails.invalid();
        }

        try {
            URI uri = inviteLink.startsWith("http") ? URI.create(inviteLink) : URI.create("http://localhost" + inviteLink);
            Matcher matcher = GROUP_JOIN_PATH.matcher(uri.getPath());
            if (!matcher.matches()) {
                return InviteDetails.invalid();
            }

            Long groupId = Long.parseLong(matcher.group(1));
            String query = uri.getQuery();
            String invitedEmail = null;
            if (query != null) {
                for (String part : query.split("&")) {
                    String[] keyValue = part.split("=", 2);
                    if (keyValue.length == 2 && keyValue[0].equals("email")) {
                        invitedEmail = java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    }
                }
            }

            Optional<Group> group = groupRepository.findById(groupId);
            return new InviteDetails(groupId, invitedEmail, group.map(Group::getName).orElse("Circle"), group.isPresent());
        } catch (Exception e) {
            return InviteDetails.invalid();
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public record InviteDetails(Long groupId, String invitedEmail, String groupName, boolean valid) {
        static InviteDetails invalid() {
            return new InviteDetails(null, null, null, false);
        }

        boolean isValid() {
            return valid;
        }
    }
}
