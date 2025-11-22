package com.app.auth_server.webauthn;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class WebAuthnService {

	private final RestTemplate restTemplate;
	private final String passkeysServiceUrl;

	public WebAuthnService(
		RestTemplateBuilder restTemplateBuilder,
		@Value("${oauth2.passkeys-service-url}") String passkeysServiceUrl
	) {
		this.restTemplate = restTemplateBuilder.build();
		this.passkeysServiceUrl = passkeysServiceUrl;
	}

	public String beginAuthentication(String username) {
		final String url = passkeysServiceUrl + "/api/authenticate/begin";
		Map<String, Object> payload = Map.of("username", username);

		try {
			ResponseEntity<String> response = restTemplate.postForEntity(url, payload, String.class);
			return response.getBody();
		} catch (HttpClientErrorException e) {
			throw new WebAuthnException("Passkey begin failed", e);
		}
	}

	public String finishAuthentication(Map<String, Object> finishBody) {
		final String url = passkeysServiceUrl + "/api/authenticate/finish";

		try {
			ResponseEntity<String> response = restTemplate.postForEntity(url, finishBody, String.class);
			return response.getBody();
		} catch (HttpClientErrorException e) {
			throw new WebAuthnException("Passkey finish failed", e);
		}
	}
}
