package com.gatewaykeeper.gateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class Oauth2ClientConfiguration {

    private final String authServerUrl;

    public Oauth2ClientConfiguration(@Value("${oauth2.auth-server-url}") String authServerUrl) {
        this.authServerUrl = authServerUrl;
    }

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange(auth -> auth
                        .pathMatchers("/public/**").permitAll()
                        .anyExchange().authenticated())
                .oauth2Login(Customizer.withDefaults())
                .oauth2Client(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public ReactiveClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration gateway = ClientRegistrations.fromIssuerLocation(authServerUrl)
                .clientId("gateway")
                .clientSecret("secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/gateway")
                .scope("openid", "profile", "resource.read")
                .build();

        return new InMemoryReactiveClientRegistrationRepository(gateway);
    }
}
