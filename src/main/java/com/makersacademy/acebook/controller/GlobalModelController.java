package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelController {

    @Autowired
    UserRepository userRepository;

    @ModelAttribute("loggedInUser")
    public User addLoggedInUser() {
        try {
            DefaultOidcUser principal = (DefaultOidcUser) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();
            String email = (String) principal.getAttributes().get("email");
            return userRepository.findUserByEmail(email).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}