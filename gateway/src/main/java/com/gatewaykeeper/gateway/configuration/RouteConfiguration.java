package com.gatewaykeeper.gateway.configuration;

import org.springframework.cloud.gateway.filter.factory.TokenRelayGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfiguration {

    private final TokenRelayGatewayFilterFactory filterFactory;

    public RouteConfiguration(TokenRelayGatewayFilterFactory filterFactory) {
        this.filterFactory = filterFactory;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("resource", r -> r.path("/resource")
                        .filters(f -> f.filters(filterFactory.apply())
                                .removeRequestHeader("Cookie")) // Prevents cookie being sent downstream
                        .uri("http://secure-resource:9100"))
                .build();
    }

}
