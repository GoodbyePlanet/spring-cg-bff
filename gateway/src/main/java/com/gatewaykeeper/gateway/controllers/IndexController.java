package com.gatewaykeeper.gateway.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    @GetMapping("/user-attributes")
    public String index(Model model,
                        @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient,
                        @AuthenticationPrincipal OAuth2User oauth2User) {
        model.addAttribute("userName", oauth2User.getName());
        model.addAttribute("clientName", authorizedClient.getClientRegistration().getClientName());
        model.addAttribute("userAttributes", oauth2User.getAttributes());
        return "index";
    }

    @GetMapping("/logged-out")
    public String loggedOut() {
        return "logged-out";
    }
}
