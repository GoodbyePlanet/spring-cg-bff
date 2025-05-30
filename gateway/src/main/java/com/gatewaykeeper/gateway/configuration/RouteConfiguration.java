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
	private final TokenRelayGatewayFilterFactory tokenRelay;

    public RouteConfiguration(TokenRelayGatewayFilterFactory tokenRelay,
                              @Value("${oauth2.secure-resource-url}") String secureResourceUrl,
                              @Value("${oauth2.auth-server-url}") String authServerUrl) {
        this.tokenRelay = tokenRelay;
        this.secureResourceUrl = secureResourceUrl;
        this.authServerUrl = authServerUrl;
    }

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes()
			.route("resource", r -> r.path("/resource")
				.filters(f -> f.filters(tokenRelay.apply())
					.removeRequestHeader("Cookie")) // Prevents cookie being sent downstream
				.uri(secureResourceUrl))
			.route("userinfo", r -> r.path("/userinfo")
				.filters(f -> f.filters(tokenRelay.apply()))
				.uri(authServerUrl))
			.build();
	}
}
