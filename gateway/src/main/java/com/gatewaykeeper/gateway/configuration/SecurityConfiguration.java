package com.gatewaykeeper.gateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.server.DelegatingServerAuthenticationEntryPoint;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.DelegatingServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.HttpStatusReturningServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfServerLogoutHandler;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRepository;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableWebFluxSecurity
public class SecurityConfiguration {

	@Value("${fe-app.base-uri}")
	private String feAppBaseUrl;

	@Bean
	public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http,
		ReactiveClientRegistrationRepository registry) {
		CookieServerCsrfTokenRepository cookieCsrfTokenRepository = CookieServerCsrfTokenRepository.withHttpOnlyFalse();

//		ServerCsrfTokenRequestHandler csrfHandler = new ServerCsrfTokenRequestHandler();
		// Opt-out of deferred CSRF token loading

		http.authorizeExchange(auth -> auth
				.pathMatchers("/logged-out").permitAll()
				.anyExchange().authenticated())
			.cors(Customizer.withDefaults())
			.csrf(csrf -> csrf.csrfTokenRepository(cookieCsrfTokenRepository))
			.exceptionHandling(exceptionHandling ->
				exceptionHandling.authenticationEntryPoint(authenticationEntryPoint()))
			.oauth2Login(oauth2Login ->
				oauth2Login.authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler(this.feAppBaseUrl)))
			.oauth2Client(Customizer.withDefaults())
			.logout(logout ->
				logout
					.logoutHandler(logoutHandler(cookieCsrfTokenRepository))
					.logoutSuccessHandler(new HttpStatusReturningServerLogoutSuccessHandler())
			);
//			.logout(logout -> logout.logoutSuccessHandler(oidcLogoutSuccessHandler(registry)));

		return http.build();
	}

	@Bean
	public WebFilter csrfTokenWebFilter() {
		return (exchange, chain) -> {
			// Forces resolution of CSRF token, so it will be sent back (e.g., in cookie)
			return exchange.getAttributeOrDefault(CsrfToken.class.getName(), Mono.empty())
				.then(chain.filter(exchange));
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


	private ServerLogoutHandler logoutHandler(ServerCsrfTokenRepository csrfTokenRepository) {
		return new DelegatingServerLogoutHandler(
			new SecurityContextServerLogoutHandler(),
			new CsrfServerLogoutHandler(csrfTokenRepository)
		);
	}

	/*private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(
		ReactiveClientRegistrationRepository registrationRepository) {
		OidcClientInitiatedServerLogoutSuccessHandler oidcLogoutSuccessHandler =
			new OidcClientInitiatedServerLogoutSuccessHandler(registrationRepository);

		// Set the location that the End-User's User Agent will be redirected to
		oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/logged-out");

		return oidcLogoutSuccessHandler;
	}*/
}
