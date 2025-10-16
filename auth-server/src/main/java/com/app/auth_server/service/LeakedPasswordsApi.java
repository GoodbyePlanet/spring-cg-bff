package com.app.auth_server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
public class LeakedPasswordsApi {

	@Value("${leaked-passwords-api.url}")
	private String leakedPasswordApi;

	@Bean
	public LeakedPasswordsClient leakedPasswordsClient() {
		RestClient restClient = RestClient.builder()
			.baseUrl(leakedPasswordApi)
			.build();

		HttpServiceProxyFactory factory =
			HttpServiceProxyFactory.builder()
				.exchangeAdapter(RestClientAdapter.create(restClient))
				.build();

		return factory.createClient(LeakedPasswordsClient.class);
	}
}
