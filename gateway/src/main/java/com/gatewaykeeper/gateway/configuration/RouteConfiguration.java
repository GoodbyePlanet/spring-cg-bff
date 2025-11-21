package com.gatewaykeeper.gateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.factory.TokenRelayGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfiguration {

	private final String secureResourceUrl;
	private final String authServerUrl;
	private final String passkeysServiceUrl;
	private final TokenRelayGatewayFilterFactory tokenRelay;

	public RouteConfiguration(TokenRelayGatewayFilterFactory tokenRelay,
		@Value("${oauth2.secure-resource-url}") String secureResourceUrl,
		@Value("${oauth2.auth-server-url}") String authServerUrl,
		@Value("${oauth2.passkeys-service-url}") String passkeysServiceUrl) {
		this.tokenRelay = tokenRelay;
		this.secureResourceUrl = secureResourceUrl;
		this.authServerUrl = authServerUrl;
		this.passkeysServiceUrl = passkeysServiceUrl;
	}

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes()
			.route("resource", r -> r.path("/resource")
				.filters(f -> f.filters(tokenRelay.apply())
					.removeRequestHeader("Cookie")) // Prevents cookie being sent downstream
				.uri(secureResourceUrl))
			.route("userinfo", r -> r.path("/userinfo")
				.filters(f -> f.filters(tokenRelay.apply())
					.removeRequestHeader("Cookie"))
				.uri(authServerUrl))
			.route("registration-begin", r -> r.path("/registration-begin")
				.filters(f -> f
					.rewritePath("/registration-begin", "/api/register/begin")
					.preserveHostHeader()
					.removeRequestHeader("Origin")
				)
				.uri(passkeysServiceUrl))
			.route("registration-finish", r -> r.path("/registration-finish")
				.filters(f -> f
					.rewritePath("/registration-finish", "/api/register/finish")
					.preserveHostHeader()
					.removeRequestHeader("Origin")
				)
				.uri(passkeysServiceUrl))
			.build();
	}
}
