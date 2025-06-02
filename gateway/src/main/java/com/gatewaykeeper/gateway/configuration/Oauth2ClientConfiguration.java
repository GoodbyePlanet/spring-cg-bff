package com.gatewaykeeper.gateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

@Configuration
public class Oauth2ClientConfiguration {

	private final String authServerUrl;

	public Oauth2ClientConfiguration(@Value("${oauth2.auth-server-url}") String authServerUrl) {
		this.authServerUrl = authServerUrl;
	}

	@Bean
	public ReactiveClientRegistrationRepository clientRegistrationRepository() {
		ClientRegistration gateway = ClientRegistrations.fromIssuerLocation(authServerUrl)
			.clientId("gateway")
			.clientName("gateway")
			.registrationId("gateway")
			.clientSecret("secret")
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
//			.redirectUri("{baseUrl}/authorized")
			.scope("openid", "profile", "resource.read")
			.build();

		return new InMemoryReactiveClientRegistrationRepository(gateway);
	}
}
