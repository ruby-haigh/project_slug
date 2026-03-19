package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

@Controller
public class UsersController {
    @Autowired
    UserRepository userRepository;

    @GetMapping("/users/after-login")
    public RedirectView afterLogin() {
        DefaultOidcUser principal = (DefaultOidcUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String email = (String) principal.getAttributes().get("email");
        userRepository
                .findUserByEmail(email)
                .orElseGet(() -> userRepository.save(new User(email)));

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
    public RedirectView updateAccount(@RequestParam String name, @RequestParam String bio, @PathVariable Long id) {
        Optional <User> currentUser = userRepository.findById(id);
        currentUser.get().setName(name);
        currentUser.get().setBio(bio);
        userRepository.save(currentUser.get());
        return new RedirectView("/users/{id}");
    }
}
