package com.gatewaykeeper.gateway.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.DelegatingServerAuthenticationEntryPoint;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Configuration(proxyBeanMethods = true)
@EnableWebFluxSecurity
public class SecurityConfiguration {

	@Value("${fe-app.base-uri}")
	private String feAppBaseUrl;

	@Bean
	public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http,
		ReactiveClientRegistrationRepository clientRegistrationRepository) {

		CookieServerCsrfTokenRepository cookieCsrfTokenRepository = CookieServerCsrfTokenRepository.withHttpOnlyFalse();
		ServerCsrfTokenRequestAttributeHandler csrfTokenRequestHandler = new ServerCsrfTokenRequestAttributeHandler();
		RedirectServerAuthenticationSuccessHandler redirectSuccessHandler =
			new RedirectServerAuthenticationSuccessHandler(feAppBaseUrl);

		http.authorizeExchange(auth -> auth
				.anyExchange()
				.authenticated())
			.cors(Customizer.withDefaults())
			.csrf(csrf -> csrf
				.csrfTokenRepository(cookieCsrfTokenRepository)
				.csrfTokenRequestHandler(csrfTokenRequestHandler))
			.exceptionHandling(exceptionHandling ->
				exceptionHandling.authenticationEntryPoint(authenticationEntryPoint()))
			.oauth2Login(oauth2Login ->
				oauth2Login.authenticationSuccessHandler(redirectSuccessHandler))
			.oauth2Client(Customizer.withDefaults())
			.logout(logout -> logout.logoutSuccessHandler(logoutSuccessHandler(clientRegistrationRepository)));

		return http.build();
	}

	@Bean
	public ServerLogoutSuccessHandler logoutSuccessHandler(ReactiveClientRegistrationRepository repo) {
		OidcClientInitiatedServerLogoutSuccessHandler handler = new OidcClientInitiatedServerLogoutSuccessHandler(repo);
		handler.setPostLogoutRedirectUri(feAppBaseUrl);
		return handler;
	}

	@Bean
	WebFilter csrfCookieWebFilter() {
		return (exchange, chain) -> {
			Mono<CsrfToken> csrfToken = exchange.getAttributeOrDefault(CsrfToken.class.getName(), Mono.empty());
			return csrfToken.doOnSuccess(token -> {
				/* Ensures the token is subscribed to. */
			}).then(chain.filter(exchange));
		};
	}

	private ServerAuthenticationEntryPoint authenticationEntryPoint() {
		MediaTypeServerWebExchangeMatcher htmlMatcher = new MediaTypeServerWebExchangeMatcher(MediaType.TEXT_HTML);
		htmlMatcher.setUseEquals(true);

		// For HTML requests, redirect to log in
		ServerAuthenticationEntryPoint loginRedirectEntryPoint =
			new RedirectServerAuthenticationEntryPoint("/oauth2/authorization/gateway");

		// For others, return 401
		ServerAuthenticationEntryPoint unauthorizedEntryPoint = new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED);

		DelegatingServerAuthenticationEntryPoint delegatingEntryPoint =
			new DelegatingServerAuthenticationEntryPoint(
				List.of(new DelegatingServerAuthenticationEntryPoint.DelegateEntry(htmlMatcher, loginRedirectEntryPoint)));
		delegatingEntryPoint.setDefaultEntryPoint(unauthorizedEntryPoint);

		return delegatingEntryPoint;
	}
}
