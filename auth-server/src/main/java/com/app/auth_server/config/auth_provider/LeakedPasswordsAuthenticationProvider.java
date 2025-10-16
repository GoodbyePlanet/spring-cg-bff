package com.app.auth_server.config.auth_provider;

import com.app.auth_server.service.LeakedPasswordsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class LeakedPasswordsAuthenticationProvider implements AuthenticationProvider {

	private final DaoAuthenticationProvider delegate;
	private final LeakedPasswordsService leakedPasswordsService;

	public LeakedPasswordsAuthenticationProvider(
		UserDetailsService userDetailsService,
		LeakedPasswordsService leakedPasswordsService,
		PasswordEncoder passwordEncoder) {
		this.delegate = new DaoAuthenticationProvider();
		this.delegate.setUserDetailsService(userDetailsService);
		this.delegate.setPasswordEncoder(passwordEncoder);
		this.leakedPasswordsService = leakedPasswordsService;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Authentication auth = delegate.authenticate(authentication);
		String rawPassword = authentication.getCredentials().toString();

		boolean leaked = leakedPasswordsService.isPasswordLeaked(rawPassword);

		if (leaked) {
			((UsernamePasswordAuthenticationToken) auth)
				.setDetails(new HashMap<>(Map.of("passwordLeaked", true)));
		}

		return auth;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
