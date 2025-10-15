package com.app.auth_server.config;

import com.app.auth_server.config.auth_provider.PwnedPasswordCheckingAuthenticationProvider;
import com.app.auth_server.jpa.service.authorization.JpaAuthorizationService;
import com.app.auth_server.jpa.service.authorizationconsent.JpaAuthorizationConsentService;
import com.app.auth_server.jpa.service.client.JpaClientRepository;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Configuration
@EnableWebSecurity
@Slf4j
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

		Function<OidcUserInfoAuthenticationContext, OidcUserInfo> userInfoMapper = (context) -> {
			OidcUserInfoAuthenticationToken authentication = context.getAuthentication();
			JwtAuthenticationToken principal = (JwtAuthenticationToken) authentication.getPrincipal();

			Map<String, Object> claims = new HashMap<>();
			claims.put("sub", principal.getName());

			if (authentication.getPrincipal() instanceof UsernamePasswordAuthenticationToken auth) {
				Object details = auth.getDetails();
				if (details instanceof Map<?, ?> map && Boolean.TRUE.equals(map.get("pwned"))) {
					claims.put("pwned", true);
				}
			}

			OidcUserInfo oidcUserInfo = new OidcUserInfo(principal.getToken().getClaims());
			return oidcUserInfo;
		};

		http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
			.with(authorizationServerConfigurer, (authorizationServer) -> {
				authorizationServer.authorizationService(authorizationService);
				authorizationServer.authorizationConsentService(authorizationConsentService);
				authorizationServer.registeredClientRepository(registeredClientRepository);
				authorizationServer.oidc(oidc ->
					oidc.userInfoEndpoint(userInfo ->
						userInfo.userInfoMapper(userInfoMapper)));
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
		PwnedPasswordCheckingAuthenticationProvider pwnedPasswordCheckingAuthenticationProvider) throws Exception {
		http.authenticationProvider(pwnedPasswordCheckingAuthenticationProvider)
			.authorizeHttpRequests((authorize) -> authorize
				.requestMatchers("/main.css", "/login").permitAll().anyRequest().authenticated())
			// Form login handles the redirect to the login page from the authorization server filter chain
			.formLogin(formLogin -> formLogin.loginPage("/login").permitAll());

		return http.build();
	}

	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
		return context -> {
			if (context.getPrincipal() instanceof UsernamePasswordAuthenticationToken auth) {
				Object details = auth.getDetails();
				if (details instanceof Map<?, ?> map && Boolean.TRUE.equals(map.get("pwned"))) {
					log.info("ADDING CLAIMS {}", map);
					context.getClaims().claim("pwned", true);
				}
			}
		};
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
}
