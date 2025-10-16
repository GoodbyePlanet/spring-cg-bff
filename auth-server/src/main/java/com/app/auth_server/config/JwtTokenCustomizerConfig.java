package com.app.auth_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.Map;

@Configuration
public class JwtTokenCustomizerConfig {

	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
		return context -> {
			if (context.getPrincipal() instanceof UsernamePasswordAuthenticationToken auth) {
				Object details = auth.getDetails();
				if (details instanceof Map<?, ?> map && Boolean.TRUE.equals(map.get("passwordLeaked"))) {
					context.getClaims().claim("passwordLeaked", true);
				}
			}
		};
	}
}
