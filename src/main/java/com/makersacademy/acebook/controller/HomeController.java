package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Group;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.GroupMembershipRepository;
import com.makersacademy.acebook.repository.GroupResponseRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

	@Autowired
	private GroupMembershipRepository membershipRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private GroupResponseRepository groupResponseRepository;

	private User getCurrentUser() {
		DefaultOidcUser principal = (DefaultOidcUser) SecurityContextHolder
				.getContext()
				.getAuthentication()
				.getPrincipal();
		String email = (String) principal.getAttributes().get("email");
		return userRepository.findUserByEmail(email).orElseThrow();
	}

	@GetMapping("/")
	public ModelAndView index() {
		ModelAndView mav = new ModelAndView("home");

		User user = getCurrentUser();
		List<Group> groups = membershipRepository.findGroupsByUser(user);

		if (groups == null) groups = List.of();
		Map<Long, Boolean> hasFeedMap = new HashMap<>();

		for (Group group : groups) {
			boolean hasFeed = groupResponseRepository.existsByGroupId(group.getId());
			hasFeedMap.put(group.getId(), hasFeed);
		}

		mav.addObject("hasFeedMap", hasFeedMap);

		mav.addObject("groups", groups);

		return mav;
	}
}