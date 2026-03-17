package com.makersacademy.acebook.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class HomeController {
	@RequestMapping(value = "index")
	public RedirectView index() {
		return new RedirectView("/");
	}
}
