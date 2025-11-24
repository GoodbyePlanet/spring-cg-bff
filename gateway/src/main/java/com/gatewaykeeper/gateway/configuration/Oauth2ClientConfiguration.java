package com.gatewaykeeper.gateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

@Configuration
public class Oauth2ClientConfiguration {

	private final String authServerUrl;

	public Oauth2ClientConfiguration(@Value("${oauth2.auth-server-url}") String authServerUrl) {
		this.authServerUrl = authServerUrl;
	}

	@Bean
	public ReactiveClientRegistrationRepository clientRegistrationRepository() {
		final String GATEWAY = "gateway";

		// authServerUrl from properties should be: http://auth.localhost

		ClientRegistration gateway = ClientRegistration.withRegistrationId(GATEWAY)
			.clientId(GATEWAY)
			.clientSecret("secret")
			.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
			.scope("openid", "profile", "resource.read")

			// 1. Public Issuer (Browser sees this)
			.issuerUri(authServerUrl)
			.authorizationUri(authServerUrl + "/oauth2/authorize")

			// 2. Internal Docker Network (Gateway container uses this)
			.tokenUri("http://auth-server:9000/oauth2/token")
			.jwkSetUri("http://auth-server:9000/oauth2/jwks")
			.userInfoUri("http://auth-server:9000/userinfo")

			.userNameAttributeName(IdTokenClaimNames.SUB)
			.clientName(GATEWAY)
			.build();

		return new InMemoryReactiveClientRegistrationRepository(gateway);
	}
}
