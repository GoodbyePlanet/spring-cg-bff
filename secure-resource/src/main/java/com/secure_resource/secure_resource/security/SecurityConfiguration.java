package com.secure_resource.secure_resource.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/resource")
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers("/resource").hasAuthority("SCOPE_resource.read");
                    authorize.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2ResourceServer ->
                        oauth2ResourceServer.jwt(withDefaults()));
        return http.build();
    }
}
