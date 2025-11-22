package com.app.auth_server.config;

import com.app.auth_server.config.auth_provider.LeakedPasswordsAuthenticationProvider;
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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import javax.sql.DataSource;
import java.util.function.Function;

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
				authorizationServer.oidc(oidc ->
					oidc.userInfoEndpoint(userInfo ->
						userInfo.userInfoMapper(userInfoMapper())));
			}).authorizeHttpRequests((authorize) ->
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
	public SecurityFilterChain defaultSecurityFilterChain(
		HttpSecurity http,
		LeakedPasswordsAuthenticationProvider leakedPasswordsAuthenticationProvider) throws Exception {
		http.authenticationProvider(leakedPasswordsAuthenticationProvider)
			.csrf(csrf -> csrf.ignoringRequestMatchers("/webauthn/begin"))
			.authorizeHttpRequests((authorize) -> authorize
				.requestMatchers("/main.css", "/login", "/webauthn/begin").permitAll().anyRequest().authenticated())
			// Form login handles the redirect to the login page from the authorization server filter chain
			.formLogin(formLogin -> formLogin.loginPage("/login").permitAll());

		return http.build();
	}

	@Bean
	public AuthorizationServerSettings authorizationServerSettings() {
		return AuthorizationServerSettings.builder().issuer(authServerUrl).build();
	}

	@Bean
	public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
	}

	@Bean
	public JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {
		return new JdbcUserDetailsManager(dataSource);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	private static Function<OidcUserInfoAuthenticationContext, OidcUserInfo> userInfoMapper() {
		return (context) -> {
			OidcUserInfoAuthenticationToken authentication = context.getAuthentication();
			JwtAuthenticationToken principal = (JwtAuthenticationToken) authentication.getPrincipal();
			return new OidcUserInfo(principal.getToken().getClaims());
		};
	}

}
