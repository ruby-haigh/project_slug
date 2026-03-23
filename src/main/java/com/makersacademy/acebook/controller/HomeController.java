package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Group;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.GroupCycleRepository;
import com.makersacademy.acebook.repository.GroupMembershipRepository;
import com.makersacademy.acebook.repository.GroupResponseRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
public class HomeController {

	@Autowired
	GroupMembershipRepository groupMembershipRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	GroupResponseRepository groupResponseRepository;

	@Autowired
	GroupCycleRepository groupCycleRepository;

	@GetMapping("/")
	public ModelAndView index() {
		ModelAndView modelAndView = new ModelAndView("home");

		DefaultOidcUser principal = (DefaultOidcUser) SecurityContextHolder
				.getContext()
				.getAuthentication()
				.getPrincipal();
		String email = (String) principal.getAttributes().get("email");
		User user = userRepository.findUserByEmail(email).orElseThrow();

		List<Group> groups = groupMembershipRepository.findGroupsByUser(user);


		Map<Long, Long> memberCounts = groups.stream()
				.collect(Collectors.toMap(
						Group::getId,
						group -> groupMembershipRepository.countByGroup(group)
				));

		modelAndView.addObject("groups", groups);
		modelAndView.addObject("memberCounts", memberCounts);



		Map<Long, Boolean> hasFeedMap = groups.stream()
				.collect(Collectors.toMap(
						Group::getId,
						group -> groupResponseRepository.existsByGroupId(group.getId())
				));

		modelAndView.addObject("hasFeedMap", hasFeedMap);
		return modelAndView;
	}
}