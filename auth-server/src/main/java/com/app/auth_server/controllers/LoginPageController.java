package com.app.auth_server.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginPageController {

	@GetMapping("/login")
	public String login() {
		return "loginPage";
	}
}
