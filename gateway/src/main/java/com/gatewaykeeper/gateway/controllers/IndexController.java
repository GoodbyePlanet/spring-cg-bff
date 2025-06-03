package com.gatewaykeeper.gateway.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

	@Value("${fe-app.base-uri}")
	private String feAppBaseUrl;

	@GetMapping("/")
	public String root() {
		return "redirect:" + this.feAppBaseUrl;
	}

	@GetMapping("/authorized")
	public String authorized() {
		return "redirect:" + this.feAppBaseUrl;
	}

	@GetMapping("/logged-out")
	public String loggedOut() {
		return "logged-out";
	}
}
