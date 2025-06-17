package com.app.auth_server.initializers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class RegisteredClientInitializer implements CommandLineRunner {

	private final RegisteredClientRepository registeredClientRepository;
	private final PasswordEncoder passwordEncoder;

	public RegisteredClientInitializer(RegisteredClientRepository registeredClientRepository, PasswordEncoder passwordEncoder) {
		this.registeredClientRepository = registeredClientRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {
		RegisteredClient existingGatewayClient = registeredClientRepository.findByClientId("gateway");

		if (existingGatewayClient == null) {
			RegisteredClient confidentialClient = RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId("gateway")
				.clientSecret(passwordEncoder.encode("secret"))
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
				.redirectUri("http://localhost:8081/login/oauth2/code/gateway")
				.scope(OidcScopes.OPENID)
				.scope(OidcScopes.PROFILE)
				.scope("resource.read")
				.clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
				.build();

			registeredClientRepository.save(confidentialClient);
			log.info("Created new gateway client: {}", confidentialClient.getClientId());
		} else {
			log.info("Existing gateway client found: {}", existingGatewayClient.getClientId());
		}
	}
}
