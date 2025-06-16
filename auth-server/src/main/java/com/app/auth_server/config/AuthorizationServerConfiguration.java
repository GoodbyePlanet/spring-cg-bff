package com.app.auth_server.config;

import com.app.auth_server.jpa.service.authorization.JpaAuthorizationService;
import com.app.auth_server.jpa.service.authorizationconsent.JpaAuthorizationConsentService;
import com.app.auth_server.jpa.service.client.JpaClientRepository;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfiguration {

	private final JpaAuthorizationService authorizationService;
	private final JpaAuthorizationConsentService authorizationConsentService;
	private final JpaClientRepository registeredClientRepository;
	private final String authServerUrl;

	public AuthorizationServerConfiguration(JpaAuthorizationService authorizationService,
		JpaAuthorizationConsentService authorizationConsentService,
		JpaClientRepository registeredClientRepository,
		@Value("${oauth2.auth-server-url}") String authServerUrl) {
		this.authorizationService = authorizationService;
		this.authorizationConsentService = authorizationConsentService;
		this.registeredClientRepository = registeredClientRepository;
		this.authServerUrl = authServerUrl;
	}

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
		throws Exception {
		OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

		http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
			.with(authorizationServerConfigurer, (authorizationServer) -> {
				authorizationServer.authorizationService(authorizationService);
				authorizationServer.authorizationConsentService(authorizationConsentService);
				authorizationServer.registeredClientRepository(registeredClientRepository);
				authorizationServer.oidc(Customizer.withDefaults());
			})
			.authorizeHttpRequests((authorize) ->
				authorize.anyRequest().authenticated())
			// Redirect to the login page when not authenticated from the authorization endpoint
			.exceptionHandling((exceptions) -> exceptions
				.defaultAuthenticationEntryPointFor(
					new LoginUrlAuthenticationEntryPoint("/login"),
					new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
				)
			);

		return http.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
		throws Exception {
		http.authorizeHttpRequests((authorize) -> authorize
				.requestMatchers("/main.css", "/login").permitAll().anyRequest().authenticated())
			// Form login handles the redirect to the login page from the authorization server filter chain
			.formLogin(formLogin -> formLogin.loginPage("/login").permitAll());

		return http.build();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails userDetails = User.withUsername("devnull")
			.password("{noop}pass")
			.roles("USER")
			.build();

		return new InMemoryUserDetailsManager(userDetails);
	}

	@Bean
	public AuthorizationServerSettings authorizationServerSettings() {
		return AuthorizationServerSettings.builder().issuer(authServerUrl).build();
	}

	@Bean
	public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
	}
}
