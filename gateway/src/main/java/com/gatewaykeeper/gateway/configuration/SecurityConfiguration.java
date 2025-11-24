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
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.DelegatingServerAuthenticationEntryPoint;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
import org.springframework.web.server.WebFilter;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Slf4j
@Configuration(proxyBeanMethods = true)
@EnableWebFluxSecurity
public class SecurityConfiguration {

	@Value("${fe-app.base-uri}")
	private String feAppBaseUrl;

	@Value("${oauth2.auth-server-url}")
	private String authServerUrl;

	@Bean
	public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http,
		ReactiveClientRegistrationRepository clientRegistrationRepository) {

//		// 1. Create the OIDC Handler
//		OidcClientInitiatedServerLogoutSuccessHandler oidcLogoutHandler =
//			new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
//
//		// 2. Configure the redirect URI (Auth Server will send user here after logout)
//		// Ensure this matches what is registered in Auth Server!
//		oidcLogoutHandler.setPostLogoutRedirectUri("{baseUrl}/");

		CookieServerCsrfTokenRepository cookieCsrfTokenRepository = CookieServerCsrfTokenRepository.withHttpOnlyFalse();
		ServerCsrfTokenRequestAttributeHandler csrfTokenRequestHandler = new ServerCsrfTokenRequestAttributeHandler();
		RedirectServerAuthenticationSuccessHandler redirectSuccessHandler =
			new RedirectServerAuthenticationSuccessHandler(feAppBaseUrl);

		http.authorizeExchange(auth -> auth
				// Permit /login to avoid loops if fallback happens
				.pathMatchers("/login").permitAll()
				.anyExchange().authenticated())
			.cors(Customizer.withDefaults())
			.csrf(csrf -> csrf
				.csrfTokenRepository(cookieCsrfTokenRepository)
				.csrfTokenRequestHandler(csrfTokenRequestHandler))
			.exceptionHandling(exceptionHandling ->
				exceptionHandling.authenticationEntryPoint(authenticationEntryPoint()))
			.oauth2Login(oauth2Login ->
				oauth2Login.authenticationSuccessHandler(redirectSuccessHandler))
			.oauth2Client(Customizer.withDefaults())
			.logout(logout -> logout
				// 3. EXPLICITLY register the handler here
				.logoutSuccessHandler(manualOidcLogoutHandler())
			);

		return http.build();
	}

	private ServerLogoutSuccessHandler manualOidcLogoutHandler() {
		return (exchange, authentication) -> {
			// Construct the URL: http://auth.localhost/connect/logout?post_logout_redirect_uri=http://localhost/
			String idToken = "";
			if (authentication.getPrincipal() instanceof OidcUser user) {
				idToken = user.getIdToken().getTokenValue();
			}
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(authServerUrl + "/connect/logout")
				.queryParam("post_logout_redirect_uri", feAppBaseUrl + "/");

			log.info("Logging out with principal: {}", authentication.getPrincipal());
			log.info("Logging out with principal name: {}", authentication.getName());
			log.info("Logging out with principal authorities: {}", authentication.getAuthorities());
			log.info("TOKEN {}", idToken);
			if (!idToken.isEmpty()) {
				log.info("Logging out with ID Token: {}", idToken);
				builder.queryParam("id_token_hint", idToken);
			}

			RedirectServerLogoutSuccessHandler redirectHandler = new RedirectServerLogoutSuccessHandler();
			redirectHandler.setLogoutSuccessUrl(URI.create(builder.build().toUriString()));

			return redirectHandler.onLogoutSuccess(exchange, authentication);
		};
	}

//	@Bean
//	public ServerLogoutSuccessHandler logoutSuccessHandler(ReactiveClientRegistrationRepository repo) {
//		OidcClientInitiatedServerLogoutSuccessHandler handler = new OidcClientInitiatedServerLogoutSuccessHandler(repo);
//		handler.setPostLogoutRedirectUri("{baseUrl}/");
//		return handler;
//	}

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
