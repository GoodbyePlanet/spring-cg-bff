package com.app.auth_server.config.auth_provider;

import com.app.auth_server.service.PwnedPasswordService;
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
import java.util.Set;

@Component
@Slf4j
public class PwnedPasswordCheckingAuthenticationProvider implements AuthenticationProvider {

	private final DaoAuthenticationProvider delegate;
	private final PwnedPasswordService pwnedPasswordService;

	public PwnedPasswordCheckingAuthenticationProvider(
		UserDetailsService userDetailsService,
		PwnedPasswordService pwnedPasswordService,
		PasswordEncoder passwordEncoder) {
		this.delegate = new DaoAuthenticationProvider();
		this.delegate.setUserDetailsService(userDetailsService);
		this.delegate.setPasswordEncoder(passwordEncoder);
		this.pwnedPasswordService = pwnedPasswordService;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Authentication result = delegate.authenticate(authentication);

		String rawPassword = authentication.getCredentials().toString();

		boolean pwned = pwnedPasswordService.isPwned(rawPassword);

		log.info("IS PWNED {}", pwned);
		if (pwned) {
			((UsernamePasswordAuthenticationToken) result).setDetails(new HashMap<>(Map.of("pwned", true)));
		}
		log.info("AUTHENTICATED {}", result);

		return result;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
