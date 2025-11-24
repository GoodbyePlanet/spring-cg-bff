package com.app.auth_server.initializers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
	private final String gatewayUrl;
	private final String feClientUrl;

	public RegisteredClientInitializer(RegisteredClientRepository registeredClientRepository,
		PasswordEncoder passwordEncoder,
		@Value("${oauth2.gateway-client-url}") String gatewayUrl,
		@Value("${fe-app.base-uri}") String feClientUrl) {
		this.registeredClientRepository = registeredClientRepository;
		this.passwordEncoder = passwordEncoder;
		this.gatewayUrl = gatewayUrl;
		this.feClientUrl = feClientUrl;
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
				.redirectUri(gatewayUrl + "/login/oauth2/code/gateway")
				.postLogoutRedirectUri("http://localhost/")
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
