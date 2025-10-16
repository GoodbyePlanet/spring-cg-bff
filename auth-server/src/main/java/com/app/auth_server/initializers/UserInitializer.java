package com.app.auth_server.initializers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserInitializer implements CommandLineRunner {

	private final JdbcUserDetailsManager userDetailsManager;
	private final PasswordEncoder passwordEncoder;

	public UserInitializer(JdbcUserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder) {
		this.userDetailsManager = userDetailsManager;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {
		String username = "devnull";

		if (!userDetailsManager.userExists(username)) {
			UserDetails user = User.withUsername(username)
				.password(passwordEncoder.encode("movies15"))
				.roles("USER")
				.build();

			userDetailsManager.createUser(user);
			log.info("User 'devnull' created.");
		} else {
			log.info("User 'devnull' already exists.");
		}
	}
}
