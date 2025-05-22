package com.app.auth_server;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

import java.util.UUID;

@SpringBootApplication
public class AuthServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(RegisteredClientRepository registeredClientRepository) {
        return args -> {
            RegisteredClient existingGatewayClient = registeredClientRepository.findByClientId("gateway");

            if (existingGatewayClient == null) {
                RegisteredClient confidentialClient = RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId("gateway")
                        .clientSecret("{noop}secret")
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .redirectUri("http://localhost:8081/login/oauth2/code/gateway")
                        .postLogoutRedirectUri("http://localhost:8081/logged-out")
                        .scope(OidcScopes.OPENID)
                        .scope(OidcScopes.PROFILE)
                        .scope("resource.read")
                        .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                        .build();

                registeredClientRepository.save(confidentialClient);
            }
        };
    }
}
